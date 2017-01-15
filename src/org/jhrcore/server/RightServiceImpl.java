/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jhrcore.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jboss.annotation.ejb.Clustered;
import org.jhrcore.util.DbUtil;
import org.jhrcore.util.SysUtil;
import org.jhrcore.entity.DeptCode;
import org.jhrcore.entity.right.Role;
import org.jhrcore.entity.right.RoleCode;
import org.jhrcore.entity.right.RoleDept;
import org.jhrcore.entity.right.RoleEntity;
import org.jhrcore.entity.salary.ValidateSQLResult;
import org.jhrcore.iservice.RightService;
import org.jhrcore.mutil.RightUtil;
import org.jhrcore.server.util.CountUtil;
import org.jhrcore.server.util.ImplUtil;
import org.jhrcore.util.CipherUtil;

/**
 *
 * @author hflj
 */
@Clustered
@Stateless
@Remote(RightService.class)
public class RightServiceImpl extends SuperImpl implements RightService {

    private static Logger log = Logger.getLogger(RightServiceImpl.class.getName());

    public RightServiceImpl() throws RemoteException {
        super(ServerApp.rmiPort);
    }

    public RightServiceImpl(int port) throws RemoteException {
        super(port);
    }

    @Override
    public ValidateSQLResult defineDeptRight(String dept_code, int mod, final String user_key, boolean isSA, String g_rolea01_key) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        String tableName = CountUtil.getCommTempTable();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            String db_type = HibernateUtil.getDb_type();
            List list = s.createQuery("select 1 from RoleA01 ra where ra.roleA01_key='" + user_key + "'").list();
            if (list == null || list.isEmpty() || list.size() > 1) {
                validateSQLResult.setResult(1);
                validateSQLResult.setMsg("授权用户不存在!");
                ImplUtil.rollBack(tx);
                return validateSQLResult;
            }
            DeptCode dc = (DeptCode) s.createQuery("from DeptCode dc where dc.deptCode_key='" + dept_code + "'").uniqueResult();
            if (dc == null) {
                validateSQLResult.setResult(1);
                validateSQLResult.setMsg("授权部门不存在!");
                ImplUtil.rollBack(tx);
                return validateSQLResult;
            }
            List g_deptrights = null;
            List<DeptCode> k_drights = new ArrayList<DeptCode>();
            boolean con_flag = false;
            if (!isSA) {
                g_deptrights = s.createQuery("from RoleDept rd join fetch rd.deptCode where rd.roleA01.roleA01_key='" + g_rolea01_key + "'").list();
                for (Object obj : g_deptrights) {
                    RoleDept adp = (RoleDept) obj;
                    String obj_string = adp.getDeptCode().getDept_code();
                    if (dc.getDept_code().startsWith(obj_string)) {
                        con_flag = true;
                        break;
                    }
                }
                if (!con_flag) {
                    for (Object obj : g_deptrights) {
                        RoleDept adp = (RoleDept) obj;
                        String obj_string = adp.getDeptCode().getDept_code();
                        if (obj_string.startsWith(dc.getDept_code())) {
                            k_drights.add(adp.getDeptCode());
                        }
                    }
                }
            }
            List deptrights = s.createQuery("from RoleDept rd join fetch rd.deptCode where rd.roleA01.roleA01_key='" + user_key + "'").list();
            if (deptrights.isEmpty()) {
                if (mod == 1) {
                    if (isSA || con_flag) {
                        s.createSQLQuery("insert into RoleDept(RoleDept_key,deptcode_key,rolea01_key,fun_flag)values('" + user_key + "_" + dept_code + "','" + dept_code + "','" + user_key + "',1)").executeUpdate();
                    } else {
                        for (DeptCode d : k_drights) {
                            s.createSQLQuery("insert into RoleDept(RoleDept_key,deptcode_key,rolea01_key,fun_flag)values('" + user_key + "_" + d.getDeptCode_key() + "','" + d.getDeptCode_key() + "','" + user_key + "',1)").executeUpdate();
                        }
                    }
                }
            } else {
                String like_str = "1=0";
                DbUtil.initTempTable(s, tableName, db_type);
                List<DeptCode> deptcodes = new ArrayList<DeptCode>();
                for (Object obj : deptrights) {
                    RoleDept adp = (RoleDept) obj;
                    deptcodes.add(adp.getDeptCode());
                }
                if (isSA || con_flag) {
                    deptcodes.add(dc);
                } else {
                    deptcodes.addAll(k_drights);
                }
                String sql = "";
                if (db_type.equals("oracle")) {
                    sql = "create table " + tableName + " as select dept_code from deptcode where 1=0";
                    for (DeptCode deptcode : deptcodes) {
                        sql += " or instr('" + deptcode.getDept_code() + "',dept_code)=1";
                        like_str += " or d.dept_code like '" + deptcode.getDept_code() + "%'";
                    }
                } else {
                    sql = "select dept_code into " + tableName + " from deptcode where 1=0";
                    for (DeptCode deptcode : deptcodes) {
                        sql += " or charindex(dept_code,'" + deptcode.getDept_code() + "')=1";
                        like_str += " or d.dept_code like '" + deptcode.getDept_code() + "%'";
                    }
                }
                s.createSQLQuery(sql).executeUpdate();
                List depts = s.createSQLQuery("select distinct d.dept_code,d.parent_code,d.deptcode_key from deptcode d," + tableName + " b where (d.dept_code=b.dept_code or d.parent_code=b.dept_code) and (" + like_str + ") order by d.dept_code").list();
                DefaultMutableTreeNode rootNode = RightUtil.initDeptTree(depts);
                Set<String> rights = new HashSet<String>();
                deptcodes.remove(dc);
                for (DeptCode dept : deptcodes) {
                    rights.add(dept.getDeptCode_key());
                }
                RightUtil.refreshDeptRight(rootNode, rights);
                Enumeration enumt = rootNode.breadthFirstEnumeration();
                DefaultMutableTreeNode curNode = null;
                boolean f_flag = false;
                while (enumt.hasMoreElements()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumt.nextElement();
                    Object obj = node.getUserObject();
                    if (isSA || con_flag) {
                        if (obj instanceof DeptCode && (((DeptCode) obj).getDeptCode_key().equals(dept_code))) {
                            ((DeptCode) obj).setFun_flag(mod);
                            curNode = node;
                            break;
                        }
                    } else {
                        if (obj instanceof DeptCode && (((DeptCode) obj).getDeptCode_key().equals(dept_code))) {
                            curNode = node;
                        }
                        if (obj instanceof DeptCode) {
                            DeptCode dd = (DeptCode) obj;
                            if (existDept(k_drights, dd)) {
                                ((DeptCode) obj).setFun_flag(mod);
                            } else {
                                if (dd.getDept_code().startsWith(dc.getDept_code())) {
                                    f_flag = true;
                                }
                            }
                        }
                    }
                }
                if (f_flag && curNode != null && !con_flag) {
                    ((DeptCode) curNode.getUserObject()).setFun_flag(2);
                }
                if (curNode != null) {
                    RightUtil.refreshDeptNode(curNode, rootNode);
                }
                List<DeptCode> newdepts = new ArrayList<DeptCode>();
                RightUtil.getDeptRight(rootNode, newdepts);
                s.createSQLQuery("delete from RoleDept where rolea01_key='" + user_key + "'").executeUpdate();
                sql = "insert into RoleDept(RoleDept_key,rolea01_key,fun_flag,deptcode_key) select " + DbUtil.getUIDForDb(db_type) + ",'" + user_key + "',1,deptcode_key from deptcode where deptcode_key in('-1'";
                for (DeptCode dept : newdepts) {
                    sql += ",'" + dept.getDeptCode_key() + "'";
                }
                sql += ")";
                s.createSQLQuery(sql).executeUpdate();
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            }
            tx.commit();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    createTableView("ra.rolea01_key='" + user_key + "'", false);
                }
            };
            new Thread(run).run();
        } catch (Exception e) {
            ImplUtil.rollBack(tx);
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            } catch (Exception ex) {
            }
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    private boolean existDept(List<DeptCode> k_drights, DeptCode d) {
        for (DeptCode dc : k_drights) {
            if (d.getDept_code().startsWith(dc.getDept_code())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ValidateSQLResult defineFuntionRight(List<String[]> data) throws RemoteException {
        long time1 = System.currentTimeMillis();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        String tableName = CountUtil.getCommTempTable();
        String oraTable = CountUtil.getCommTempTable();
        String create_sql = "";
        String db_type = HibernateUtil.getDb_type();
        String connStr = DbUtil.getPlusStr(db_type);
        if (db_type.equals("sqlserver")) {
            create_sql = "create table " + tableName + "(role_key varchar(255),funtionright_key varchar(255),fun_flag int)";
        } else {
            create_sql = "create table " + tableName + "(role_key varchar2(255),funtionright_key varchar2(255),fun_flag int)";
        }
        try {
            String ex_sql = DbUtil.isTableExistSQL(tableName, db_type);
            int i = Integer.valueOf(s.createSQLQuery(ex_sql).uniqueResult().toString());
            if (i != 0) {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            }
            s.createSQLQuery(create_sql).executeUpdate();
            boolean insable = false;
            for (String[] r_data : data) {
                if (!r_data[2].toString().equals("0")) {
                    insable = true;
                    break;
                }
            }
            if (db_type.equals("sqlserver")) {
                ex_sql = DbUtil.getInstrForMID("insert into " + tableName + "(role_key,funtionright_key,fun_flag)", data, "", ";");
            } else {
                ex_sql = DbUtil.isTableExistSQL(oraTable, db_type);
                i = Integer.valueOf(s.createSQLQuery(ex_sql).uniqueResult().toString());
                if (i != 0) {
                    s.createSQLQuery("drop table " + oraTable).executeUpdate();
                }
                s.createSQLQuery("create table " + oraTable + "(t_id int)").executeUpdate();
                s.createSQLQuery("insert into " + oraTable + "(t_id)values(1)").executeUpdate();
                ex_sql = DbUtil.getInstrForMID("insert into " + tableName + "(role_key,funtionright_key,fun_flag)", data, " from " + oraTable, ";");
            }
            ImplUtil.exSQLs(tx, s, ex_sql, ";");
            s.flush();
            ex_sql = "delete from rolefuntion where exists(select 1 from " + tableName + " b where b.role_key=rolefuntion.role_key and b.funtionright_key=rolefuntion.funtionright_key);";
            if (insable) {
                ex_sql += "insert into rolefuntion (role_funtion_key,role_key,funtionright_key,fun_flag)select role_key"
                        + connStr + "'_'" + connStr + "funtionright_key,role_key,funtionright_key,fun_flag from " + tableName + " b where fun_flag>0 ;";
            }
            ImplUtil.exSQLs(tx, s, ex_sql, ";");
            s.createSQLQuery("drop table " + tableName).executeUpdate();
            if (!db_type.equals("sqlserver")) {
                s.createSQLQuery("drop table " + oraTable).executeUpdate();
            }
            tx.commit();
        } catch (Exception e) {
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
                if (!db_type.equals("sqlserver")) {
                    s.createSQLQuery("drop table " + oraTable).executeUpdate();
                }
            } catch (Exception ex) {
            }
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        long time2 = System.currentTimeMillis();
        System.out.println("t:" + (time2 - time1));
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult defineFieldRight(List<String[]> data) throws RemoteException {
        long time1 = System.currentTimeMillis();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        String tableName = CountUtil.getCommTempTable();
        String oraTable = CountUtil.getCommTempTable();
        String create_sql = "";
        String db_type = HibernateUtil.getDb_type();
        String connStr = DbUtil.getPlusStr(db_type);
        if (db_type.equals("sqlserver")) {
            create_sql = "create table " + tableName + "(role_key varchar(255),field_name varchar(255),fun_flag int)";
        } else {
            create_sql = "create table " + tableName + "(role_key varchar2(255),field_name varchar2(255),fun_flag int)";
        }
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            s.createSQLQuery(create_sql).executeUpdate();
            boolean insable = false;
            for (String[] r_data : data) {
                if (!r_data[2].toString().equals("0")) {
                    insable = true;
                    break;
                }
            }
            String ex_sql = null;
            if (db_type.equals("sqlserver")) {
                ex_sql = DbUtil.getInstrForMID("insert into " + tableName + "(role_key,field_name,fun_flag)", data, "", ";");
            } else {
                DbUtil.initTempTable(s, oraTable, db_type);
                ex_sql = DbUtil.isTableExistSQL(oraTable, db_type);
                s.createSQLQuery("create table " + oraTable + "(t_id int)").executeUpdate();
                s.createSQLQuery("insert into " + oraTable + "(t_id)values(1)").executeUpdate();
                ex_sql = DbUtil.getInstrForMID("insert into " + tableName + "(role_key,field_name,fun_flag)", data, " from " + oraTable, ";");
            }
            ImplUtil.exSQLs(tx, s, ex_sql, ";");
            s.flush();
            ex_sql = "delete from rolefield where exists(select 1 from " + tableName + " b where b.role_key=rolefield.role_key and b.field_name=rolefield.field_name);";
            if (insable) {
                ex_sql += "insert into rolefield (role_field_key,role_key,field_name,fun_flag)select role_key"
                        + connStr + "'_'" + connStr + "field_name,role_key,field_name,fun_flag from " + tableName + " b where fun_flag>0 ;";
            }
            ImplUtil.exSQLs(tx, s, ex_sql, ";");
            s.createSQLQuery("drop table " + tableName).executeUpdate();
            if (!db_type.equals("sqlserver")) {
                s.createSQLQuery("drop table " + oraTable).executeUpdate();
            }
            tx.commit();
        } catch (Exception e) {
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
                if (!db_type.equals("sqlserver")) {
                    s.createSQLQuery("drop table " + oraTable).executeUpdate();
                }
            } catch (Exception ex) {
            }
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        long time2 = System.currentTimeMillis();
        System.out.println("t:" + (time2 - time1));
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult defineReportRight(List<String[]> data) throws RemoteException {
        long time1 = System.currentTimeMillis();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        String tableName = CountUtil.getCommTempTable();
        String oraTable = CountUtil.getCommTempTable();
        String create_sql = "";
        String db_type = HibernateUtil.getDb_type();
        String connStr = DbUtil.getPlusStr(db_type);
        if (db_type.equals("sqlserver")) {
            create_sql = "create table " + tableName + "(role_key varchar(255),reportDef_key varchar(255),fun_flag int)";
        } else {
            create_sql = "create table " + tableName + "(role_key varchar2(255),reportDef_key varchar2(255),fun_flag int)";
        }
        try {
            String ex_sql = DbUtil.isTableExistSQL(tableName, db_type);
            int i = Integer.valueOf(s.createSQLQuery(ex_sql).uniqueResult().toString());
            if (i != 0) {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            }
            s.createSQLQuery(create_sql).executeUpdate();
            boolean insable = false;
            for (String[] r_data : data) {
                if (!r_data[2].toString().equals("0")) {
                    insable = true;
                    break;
                }
            }
            if (db_type.equals("sqlserver")) {
                ex_sql = DbUtil.getInstrForMID("insert into " + tableName + "(role_key,reportDef_key,fun_flag)", data, "", ";");
            } else {
                DbUtil.initTempTable(s, oraTable, db_type);
                s.createSQLQuery("create table " + oraTable + "(t_id int)").executeUpdate();
                s.createSQLQuery("insert into " + oraTable + "(t_id)values(1)").executeUpdate();
                ex_sql = DbUtil.getInstrForMID("insert into " + tableName + "(role_key,reportDef_key,fun_flag)", data, " from " + oraTable, ";");
            }
            ImplUtil.exSQLs(tx, s, ex_sql, ";");
            s.flush();
            ex_sql = "delete from rolereport where exists(select 1 from " + tableName + " b where b.role_key=rolereport.role_key and b.reportDef_key=rolereport.reportDef_key);";
            if (insable) {
                ex_sql += "insert into rolereport (role_report_key,role_key,reportDef_key,fun_flag)select role_key"
                        + connStr + "'_'" + connStr + "reportDef_key,role_key,reportDef_key,fun_flag from " + tableName + " b where fun_flag>0 ;";
            }
            ImplUtil.exSQLs(tx, s, ex_sql, ";");
            s.createSQLQuery("drop table " + tableName).executeUpdate();
            if (!db_type.equals("sqlserver")) {
                s.createSQLQuery("drop table " + oraTable).executeUpdate();
            }
            tx.commit();
        } catch (Exception e) {
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
                if (!db_type.equals("sqlserver")) {
                    s.createSQLQuery("drop table " + oraTable).executeUpdate();
                }
            } catch (Exception ex) {
            }
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        long time2 = System.currentTimeMillis();
        System.out.println("t:" + (time2 - time1));
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult defineEntityRight(String p_role_key, List<String> roleKeys, List<RoleEntity> entityKeys, int mod) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        String roleKey = "";
        for (String key : roleKeys) {
            roleKey = roleKey + ",'" + key + "'";
        }
        roleKey = roleKey.substring(1);
        String tableName = CountUtil.getCommTempTable();
        String create_sql = "";
        String db_type = HibernateUtil.getDb_type();
        String connStr = DbUtil.getPlusStr(db_type);
        if (db_type.equals("sqlserver")) {
            create_sql = "create table " + tableName + "(role_entity_key varchar(255) primary key,entity_key varchar(255),add_flag tinyint,edit_flag tinyint,del_flag tinyint,view_flag tinyint,queryscheme_key varchar(255),right_sql varchar(4000),edit_sql varchar(4000),querySchemeEdit_key varchar(255))";
        } else {
            create_sql = "create table " + tableName + "(role_entity_key varchar2(255) primary key,entity_key varchar2(255),add_flag NUMBER(1),edit_flag NUMBER(1),del_flag NUMBER(1),view_flag NUMBER(1),queryscheme_key varchar2(255),right_sql varchar2(4000),edit_sql varchar2(4000),querySchemeEdit_key varchar(255))";
        }
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            s.createSQLQuery(create_sql).executeUpdate();
            for (RoleEntity rc : entityKeys) {
                String sql = "insert into " + tableName + "(role_entity_key,entity_key,add_flag,edit_flag,del_flag,view_flag,queryScheme_key,right_sql,edit_sql,querySchemeEdit_key)values('"
                        + rc.getRoleEntity_key() + "','" + rc.getEntityDef().getEntity_key() + "'," + (rc.isAdd_flag() ? 1 : 0) + ","
                        + (rc.isEdit_flag() ? 1 : 0) + "," + (rc.isDel_flag() ? 1 : 0) + "," + (rc.isView_flag() ? 1 : 0) + "," + DbUtil.tranStrForSQL(rc.getQueryScheme_key()) + ","
                        + DbUtil.tranStrForSQL(rc.getRight_sql()) + "," + DbUtil.tranStrForSQL(rc.getEdit_sql()) + "," + DbUtil.tranStrForSQL(rc.getQuerySchemeEdit_key()) + ")";
                s.createSQLQuery(sql).executeUpdate();
            }
            s.flush();
            if (!p_role_key.equals("-1")) {
                SQLQuery query = s.createSQLQuery("select rc.add_flag,rc.edit_flag,rc.del_flag,rc.view_flag,t.entity_key from roleentity rc," + tableName + " t where rc.entity_key=t.entity_key and rc.role_key='" + p_role_key + "' and (rc.add_flag<t.add_flag or rc.edit_flag<t.edit_flag or rc.del_flag<t.del_flag)");
                query.setMaxResults(1);
                List list = query.list();
                if (!list.isEmpty()) {
                    String method = "查看";
                    Object[] obj = (Object[]) list.get(0);
                    if (Integer.valueOf(obj[0].toString()) == 0) {
                        method = "新增";
                    } else if (Integer.valueOf(obj[1].toString()) == 0) {
                        method = "编辑";
                    } else if (Integer.valueOf(obj[2].toString()) == 0) {
                        method = "删除";
                    }
                    Object code = s.createQuery("select entityName from EntityDef where entity_key='" + obj[3] + "'").uniqueResult();
                    validateSQLResult.setResult(1);
                    validateSQLResult.setMsg("越权操作,您没有【" + code + "】表的" + method + "权限");
                    ImplUtil.rollBack(tx);
                    return validateSQLResult;
                }
            }
            final Role role = (Role) s.createQuery("from Role where role_key='" + roleKeys.get(0) + "'").uniqueResult();//当前授权节点对应角色
            if (!role.getParent_code().equalsIgnoreCase("ROOT")) {
                SQLQuery query = s.createSQLQuery("select rc.add_flag,rc.edit_flag,rc.del_flag,rc.view_flag,t.entity_key from roleentity rc," + tableName + " t where rc.entity_key=t.entity_key and rc.role_key in(select role_key from Role where role_code='" + role.getParent_code() + "') and (rc.add_flag<t.add_flag or rc.edit_flag<t.edit_flag or rc.del_flag<t.del_flag)");
                query.setMaxResults(1);
                List list = query.list();
                if (!list.isEmpty()) {
                    String method = "查看";
                    Object[] obj = (Object[]) list.get(0);
                    if (Integer.valueOf(obj[0].toString()) == 0) {
                        method = "新增";
                    } else if (Integer.valueOf(obj[1].toString()) == 0) {
                        method = "编辑";
                    } else if (Integer.valueOf(obj[2].toString()) == 0) {
                        method = "删除";
                    }
                    Object code = s.createQuery("select entityName from EntityDef where entity_key='" + obj[3] + "'").uniqueResult();
                    validateSQLResult.setResult(1);
                    validateSQLResult.setMsg("越权操作,该角色上级没有【" + code + "】表的" + method + "权限");
                    ImplUtil.rollBack(tx);
                    return validateSQLResult;
                }
            }
            if (mod == 0) {
                s.createSQLQuery("update RoleEntity set add_flag=0,edit_flag=0,del_flag=0,view_flag=0,queryscheme_key=null,right_sql='',edit_sql='',queryschemeedit_key=null where role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and entity_key in(select entity_key from " + tableName + ")").executeUpdate();
            } else if (mod == 1) {
                String ex_sql = "insert into RoleEntity(roleentity_key,role_key,entity_key,add_flag,edit_flag,del_flag,view_flag,queryscheme_key,right_sql,edit_sql,querySchemeEdit_key)select role_key" + connStr + "'_'" + connStr + "entity_key,"
                        + "role_key,entity_key,add_flag,edit_flag,del_flag,view_flag,queryscheme_key,right_sql,edit_sql,queryschemeedit_key from " + tableName + " t,Role r where r.role_key in(" + roleKey + ") "
                        + "and not exists(select 1 from roleentity rc where rc.role_key=r.role_key and rc.entity_key=t.entity_key)";
                s.createSQLQuery(ex_sql).executeUpdate();
                if (db_type.equals("sqlserver")) {
                    ex_sql = "update RoleEntity set add_flag=t.add_flag,edit_flag=t.edit_flag,del_flag=t.del_flag,view_flag=t.view_flag from roleentity r,"
                            + tableName + " t where r.role_key in(" + roleKey + ") and r.entity_key=t.entity_key;";
                    ex_sql += "update RoleEntity set queryscheme_key=t.queryscheme_key,right_sql=t.right_sql,edit_sql=t.edit_sql,queryschemeedit_key=t.queryschemeedit_key from roleentity r,"
                            + tableName + " t where r.role_key ='" + role.getRole_key() + "' and r.entity_key=t.entity_key;";
                    ex_sql += "update RoleEntity set add_flag=0 from RoleEntity r," + tableName + " t where r.entity_key=t.entity_key "
                            + "and r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and r.add_flag>t.add_flag;";
                    ex_sql += "update RoleEntity set edit_flag=0 from RoleEntity r," + tableName + " t where r.entity_key=t.entity_key "
                            + "and r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and r.edit_flag>t.edit_flag;";
                    ex_sql += "update RoleEntity set del_flag=0 from RoleEntity r," + tableName + " t where r.entity_key=t.entity_key "
                            + "and r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and r.del_flag>t.del_flag;";
                    ex_sql += "update RoleEntity set view_flag=0 from RoleEntity r," + tableName + " t where r.entity_key=t.entity_key "
                            + "and r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and r.view_flag>t.view_flag;";
                } else {
                    ex_sql = "update RoleEntity r set (add_flag,edit_flag,del_flag,view_flag)=(select add_flag,edit_flag,del_flag,view_flag from " + tableName + " t "
                            + "where r.entity_key=t.entity_key) where r.role_key in(" + roleKey + ") and exists(select 1 from " + tableName + " t where r.entity_key=t.entity_key);";
                    ex_sql += "update RoleEntity r set (right_sql,queryscheme_key,edit_sql,queryschemeedit_key)=(select right_sql,queryscheme_key,edit_sql,queryschemeedit_key from " + tableName + " t "
                            + "where r.entity_key=t.entity_key) where r.role_key in(" + roleKey + ") and exists(select 1 from " + tableName + " t where r.entity_key=t.entity_key);";
                    ex_sql += "update RoleEntity r set add_flag=0 where r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') "
                            + "and entity_key in(select entity_key from " + tableName + ") and exists(select 1 from " + tableName + " t where r.entity_key=t.entity_key and r.add_flag>t.add_flag);";
                    ex_sql += "update RoleEntity r set edit_flag=0 where r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') "
                            + "and entity_key in(select entity_key from " + tableName + ") and exists(select 1 from " + tableName + " t where r.entity_key=t.entity_key and r.edit_flag>t.edit_flag);";
                    ex_sql += "update RoleEntity r set del_flag=0 where r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') "
                            + "and entity_key in(select entity_key from " + tableName + ") and exists(select 1 from " + tableName + " t where r.entity_key=t.entity_key and r.del_flag>t.del_flag);";
                    ex_sql += "update RoleEntity r set view_flag=0 where r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') "
                            + "and entity_key in(select entity_key from " + tableName + ") and exists(select 1 from " + tableName + " t where r.entity_key=t.entity_key and r.view_flag>t.view_flag);";
                }
                String[] strs = ex_sql.split(";");
                for (String str : strs) {
                    s.createSQLQuery(str).executeUpdate();
                }
            }
            s.createSQLQuery("drop table " + tableName).executeUpdate();
            tx.commit();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    createTableView("ra.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "')", false);
                }
            };
            new Thread(run).run();
        } catch (Exception e) {
            ImplUtil.rollBack(tx);
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            } catch (Exception ex) {
            }
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult defineCodeRight(String p_role_key, List<String> roleKeys, List<RoleCode> codeKeys, int mod) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        String roleKey = "";
        for (String key : roleKeys) {
            roleKey = roleKey + ",'" + key + "'";
        }
        roleKey = roleKey.substring(1);
        String tableName = CountUtil.getCommTempTable();
        String create_sql = "";
        String db_type = HibernateUtil.getDb_type();
        String connStr = DbUtil.getPlusStr(db_type);
        if (db_type.equals("sqlserver")) {
            create_sql = "create table " + tableName + "(role_code_key varchar(255) primary key,code_key varchar(255),add_flag tinyint,edit_flag tinyint,view_flag tinyint,del_flag tinyint,right_sql varchar(4000),edit_sql varchar(4000))";
        } else {
            create_sql = "create table " + tableName + "(role_code_key varchar2(255) primary key,code_key varchar2(255),add_flag NUMBER(1),edit_flag NUMBER(1),view_flag NUMBER(1),del_flag NUMBER(1),right_sql varchar2(4000),edit_sql varchar2(4000))";
        }
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            s.createSQLQuery(create_sql).executeUpdate();
            for (RoleCode rc : codeKeys) {
                s.createSQLQuery("insert into " + tableName + "(role_code_key,code_key,add_flag,edit_flag,del_flag,view_flag,right_sql,edit_sql)values('"
                        + rc.getRole_code_key() + "','" + rc.getCode().getCode_key() + "'," + (rc.isAdd_flag() ? 1 : 0) + ","
                        + (rc.isEdit_flag() ? 1 : 0) + "," + (rc.isDel_flag() ? 1 : 0) + "," + (rc.isView_flag() ? 1 : 0) + ",'" + SysUtil.objToStr(rc.getRight_sql()) + "','" + SysUtil.objToStr(rc.getEdit_sql()) + "')").executeUpdate();
            }

            s.flush();
            if (!p_role_key.equals("-1")) {
                SQLQuery query = s.createSQLQuery("select rc.add_flag,rc.edit_flag,rc.del_flag,rc.view_flag,t.code_key from rolecode rc," + tableName + " t where rc.code_key=t.code_key and rc.role_key='" + p_role_key + "' and (rc.add_flag<t.add_flag or rc.edit_flag<t.edit_flag or rc.del_flag<t.del_flag)");
                query.setMaxResults(1);
                List list = query.list();
                if (!list.isEmpty()) {
                    String method = "删除";
                    Object[] obj = (Object[]) list.get(0);
                    if (Integer.valueOf(obj[0].toString()) == 0) {
                        method = "新增";
                    } else if (Integer.valueOf(obj[1].toString()) == 0) {
                        method = "编辑";
                    } else if (Integer.valueOf(obj[3].toString()) == 0) {
                        method = "浏览";
                    }
                    Object code = s.createQuery("select code_name from Code where code_key='" + obj[4] + "'").uniqueResult();
                    validateSQLResult.setResult(1);
                    validateSQLResult.setMsg("越权操作,您没有编码【" + code + "】的" + method + "权限");
                    ImplUtil.rollBack(tx);
                    return validateSQLResult;
                }
            }
            Role role = (Role) s.createQuery("from Role where role_key='" + roleKeys.get(0) + "'").uniqueResult();//当前授权节点对应角色
            if (!role.getParent_code().equalsIgnoreCase("ROOT")) {
                SQLQuery query = s.createSQLQuery("select rc.add_flag,rc.edit_flag,rc.del_flag,rc.view_flag,t.code_key from rolecode rc," + tableName + " t where rc.code_key=t.code_key and rc.role_key in(select role_key from Role where role_code='" + role.getParent_code() + "') and (rc.add_flag<t.add_flag or rc.edit_flag<t.edit_flag or rc.del_flag<t.del_flag)");
                query.setMaxResults(1);
                List list = query.list();
                if (!list.isEmpty()) {
                    String method = "删除";
                    Object[] obj = (Object[]) list.get(0);
                    if (Integer.valueOf(obj[0].toString()) == 0) {
                        method = "新增";
                    } else if (Integer.valueOf(obj[1].toString()) == 0) {
                        method = "编辑";
                    } else if (Integer.valueOf(obj[3].toString()) == 0) {
                        method = "浏览";
                    }
                    Object code = s.createQuery("select code_name from Code where code_key='" + obj[4] + "'").uniqueResult();
                    validateSQLResult.setResult(1);
                    validateSQLResult.setMsg("越权操作,该角色上级没有编码【" + code + "】的" + method + "权限");
                    ImplUtil.rollBack(tx);
                    return validateSQLResult;
                }
            }
            if (mod == 0) {
                s.createSQLQuery("update RoleCode set add_flag=0,edit_flag=0,del_flag=0 where role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and code_key in(select code_key from " + tableName + ")").executeUpdate();
            } else if (mod == 1) {
                String ex_sql = "insert into rolecode(role_code_key,role_key,code_key,add_flag,edit_flag,del_flag,view_flag,right_sql,edit_sql)select role_key" + connStr + "'_'" + connStr + "code_key,"
                        + "role_key,code_key,add_flag,edit_flag,view_flag,del_flag,right_sql,edit_sql from " + tableName + " t,Role r where r.role_key in(" + roleKey + ") "
                        + "and not exists(select 1 from rolecode rc where rc.role_key=r.role_key and rc.code_key=t.code_key);";
                if (db_type.equals("sqlserver")) {
                    ex_sql += "update rolecode set add_flag=t.add_flag,edit_flag=t.edit_flag,del_flag=t.del_flag,view_flag=t.view_flag,right_sql=t.right_sql,edit_sql=t.edit_sql from rolecode r," + tableName + " t where r.role_key in(" + roleKey + ") and r.code_key=t.code_key;";
                    ex_sql += "update rolecode set add_flag=0 from rolecode r," + tableName + " t where r.code_key=t.code_key "
                            + "and r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and r.add_flag>t.add_flag;";
                    ex_sql += "update rolecode set edit_flag=0 from rolecode r," + tableName + " t where r.code_key=t.code_key "
                            + "and r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and r.edit_flag>t.edit_flag;";
                    ex_sql += "update rolecode set del_flag=0 from rolecode r," + tableName + " t where r.code_key=t.code_key "
                            + "and r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and r.del_flag>t.del_flag;";
                    ex_sql += "update rolecode set view_flag=0 from rolecode r," + tableName + " t where r.code_key=t.code_key "
                            + "and r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') and r.view_flag>t.view_flag;";
                } else {
                    ex_sql += "update rolecode r set (add_flag,edit_flag,del_flag,view_flag,right_sql,edit_sql)=(select add_flag,edit_flag,del_flag,view_flag,right_sql,edit_sql from " + tableName + " t "
                            + "where r.code_key=t.code_key) where r.role_key in(" + roleKey + ") and exists(select 1 from " + tableName + " t where t.code_key=r.code_key);";
                    ex_sql += "update rolecode r set add_flag=0 where r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') "
                            + "and code_key in(select code_key from " + tableName + ") and exists(select 1 from " + tableName + " t where r.code_key=t.code_key and r.add_flag>t.add_flag);";
                    ex_sql += "update rolecode r set edit_flag=0 where r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') "
                            + "and code_key in(select code_key from " + tableName + ") and exists(select 1 from " + tableName + " t where r.code_key=t.code_key and r.edit_flag>t.edit_flag);";
                    ex_sql += "update rolecode r set del_flag=0 where r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') "
                            + "and code_key in(select code_key from " + tableName + ") and exists(select 1 from " + tableName + " t where r.code_key=t.code_key and r.del_flag>t.del_flag);";
                    ex_sql += "update rolecode r set view_flag=0 where r.role_key in(select role_key from role where role_code like '" + role.getRole_code() + "%') "
                            + "and code_key in(select code_key from " + tableName + ") and exists(select 1 from " + tableName + " t where r.code_key=t.code_key and r.view_flag>t.view_flag);";
                }
                ImplUtil.exSQLs(tx, s, ex_sql, ";");
            }
            s.createSQLQuery("drop table " + tableName).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            ImplUtil.rollBack(tx);
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            } catch (Exception ex) {
            }
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult copyRight(String srcRoleKey, String dstRoleKey, String code) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        String sql = "";
        String db_type = HibernateUtil.getDb_type();
        String connStr = DbUtil.getPlusStr(db_type);
        dstRoleKey = "'" + dstRoleKey.replace(";", "','") + "'";
        try {
            if (code.contains("fun;")) {
                sql = "delete from roleFuntion where role_key in (" + dstRoleKey + ");";
                sql += "insert into roleFuntion(role_funtion_key,role_key,funtionRight_key,fun_flag) select "
                        + "r.role_key" + connStr + "'_'" + connStr + "rf.funtionRight_key,r.role_key,rf.funtionRight_key,"
                        + "rf.fun_flag from roleFuntion rf,role r where rf.role_key ='" + srcRoleKey + "' and r.role_key in (" + dstRoleKey + ");";
            }
            if (code.contains("field;")) {
                sql += "delete from roleField  where roleField.role_key in(" + dstRoleKey + ");";
                sql += "insert into roleField(role_Field_key,role_key,entity_name,field_name,fun_flag) select "
                        + "r.role_key" + connStr + "'_'" + connStr + "rf.field_name,r.role_key,rf.entity_name,rf.field_name,"
                        + "rf.fun_flag from roleField rf,role r where rf.role_key ='" + srcRoleKey + "' and r.role_key in(" + dstRoleKey + ");";
            }
            if (code.contains("entity;")) {
                sql += "delete from roleEntity  where role_key in(" + dstRoleKey + ");";
                sql += "insert into roleEntity(roleEntity_key,role_key,entity_key,queryScheme_key,add_flag,del_flag,"
                        + "edit_flag,view_flag,right_sql,edit_sql,querySchemeEdit_key) select r.role_key" + connStr + "'_'" + connStr + "re.entity_key,r.role_key,"
                        + "re.entity_key,re.queryScheme_key,re.add_flag,re.del_flag,re.edit_flag,re.view_flag,re.right_sql,re.edit_sql,re.querySchemeEdit_key "
                        + "from roleEntity re,role r where re.role_key ='" + srcRoleKey + "' and r.role_key in(" + dstRoleKey + ");";
            }
            if (code.contains("code;")) {
                sql += "delete from roleCode  where role_key in(" + dstRoleKey + ");";
                sql += "insert into roleCode(role_code_key,role_key,add_flag,edit_flag,del_flag,right_sql,code_key,view_flag,edit_sql) "
                        + "select r.role_key" + connStr + "'_'" + connStr + "rf.code_key,r.role_key,rf.add_flag,rf.edit_flag,"
                        + "rf.del_flag,rf.right_sql,rf.code_key,rf.view_flag,rf.edit_sql from roleCode rf,role r where rf.role_key ='" + srcRoleKey + "' and r.role_key in(" + dstRoleKey + ");";
            }
            if (code.contains("report;")) {
                sql += "delete from roleReport where role_key in(" + dstRoleKey + ");";
                sql += "insert into roleReport(role_Report_key,role_key,reportDef_key,fun_flag) select "
                        + "r.role_key" + connStr + "'_'" + connStr + "rf.reportDef_key,r.role_key,rf.reportDef_key,rf.fun_flag "
                        + "from roleReport rf,role r where rf.role_key ='" + srcRoleKey + "' and r.role_key in(" + dstRoleKey + ");";
            }
            ImplUtil.exSQLs(tx, session, validateSQLResult, sql, ";", false);
            tx.commit();
        } catch (Exception e) {
            validateSQLResult.setResult(1);
            log.error(validateSQLResult.getMsg());
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    protected static void createTableView(String s_where, boolean check) {
        String db_type = HibernateUtil.getDb_type();
        String c_sql = "select ra.rolea01_key,e.entitycaption,e.entityname "
                + "from rolea01 ra, roleEntity r,tabname e left join entityClass ec on e.entityClass_key= ec.entityClass_key "
                + "where ra.role_key=r.role_key and r.entity_key = e.entity_key "
                + "and ec.entitytype_code = 'CLASS' and r.view_flag > 0 and ra.role_key!='&&&' and (" + s_where + ")";
        List list_c = HibernateUtil.selectSQL(c_sql, false, 0);
        Hashtable<String, String> table_c = new Hashtable<String, String>();
        HashSet<String> a01Keys = new HashSet<String>();
        for (Object obj : list_c) {
            Object[] objs = (Object[]) obj;
            if (objs[0] == null) {
                continue;
            }
            a01Keys.add(objs[0].toString());
            String str = table_c.get(objs[0].toString());
            if (str == null) {
                str = "'" + objs[1].toString() + "'";
            } else {
                str = str + ",'" + objs[1].toString() + "'";
            }
            table_c.put(objs[0].toString(), str);
        }
        c_sql = "select ra.rolea01_key,d.dept_code from roledept rd,rolea01 ra,deptCode d where d.deptCode_key=rd.deptCode_key "
                + " and rd.rolea01_key=ra.rolea01_key and ra.role_key!='&&&' and (" + s_where + ")";
        List list_d = HibernateUtil.selectSQL(c_sql, false, 0);
        Hashtable<String, String> table_d = new Hashtable<String, String>();
        for (Object obj : list_d) {
            Object[] objs = (Object[]) obj;
            if (objs[0] == null || objs[0].toString().trim().equals("&&&")) {
                continue;
            }
            a01Keys.add(objs[0].toString());
            String str = table_d.get(objs[0].toString());
            if (str == null) {
                str = "DeptCode.dept_code like '" + objs[1].toString() + "%'";
            } else {
                str = str + " or DeptCode.dept_code like '" + objs[1].toString() + "%'";
            }
            table_d.put(objs[0].toString(), str);
        }
        String where_sql = "where A01.deptcode_key = DeptCode.DeptCode_key";
        String a0191_str = "";
        HashSet<String> successViews = new HashSet<String>();
        if (check) {
            List list = HibernateUtil.selectSQL("select roleid,tablename,replace(replace(viewname,'A01_',''),'C21_','') AS VNUM from table2view where success=1 ", false, 0);
            int maxNum = 1;
            for (Object obj : list) {
                Object[] objs = (Object[]) obj;
                successViews.add(objs[0].toString() + "_" + objs[1].toString());
                int num = Integer.valueOf(objs[2].toString().substring(objs[2].toString().indexOf("_") + 1));
                CountUtil.user_view_keys.put(objs[0].toString(), num);
                maxNum = Math.max(maxNum, num);
            }
            CountUtil.user_view_num = maxNum + 1;
        }
        String init_dept_view = ServerApp.getSys_properties().getProperty("HR.init_dept_view");
        boolean create_dept_view = "1".equals(init_dept_view);
        HibernateUtil.excuteSQL("delete from table2view where " + (check ? ("success=0") : "1=1") + " "
                + "and exists (select 1 from rolea01 ra where ra.rolea01_key=table2view.roleid and (" + s_where + "))");
        for (String a01Key : a01Keys) {
            a0191_str = table_c.get(a01Key);
            if (a0191_str == null || "".equals(a0191_str)) {
                a0191_str = " and 1=0 ";
            } else {
                a0191_str = " and A01.a0191 in(" + a0191_str + ")";
            }
            String d_code = table_d.get(a01Key);
            if (d_code == null) {
                d_code = "1=0";
            }
            if (create_dept_view && !successViews.contains(a01Key + "_DeptCode")) {
                RightServiceImpl.createTableView("DeptCode", a01Key, "select * from DeptCode where " + d_code, db_type);
            }
            String viewStr = "select A01.* from A01,DeptCode  " + where_sql + a0191_str + " and (" + d_code + ")";
            if (!successViews.contains(a01Key + "_A01")) {
                RightServiceImpl.createTableView("A01", a01Key, viewStr, db_type);
            }
        }
    }

    public static void createTableView(String tableName, String personKey, String viewStr, String db_type) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            Integer viewNum = CountUtil.getUser_view_num(personKey);
            String scipt = "";
            String viewName = tableName + "_" + viewNum;
            if ("sqlserver".equals(db_type)) {
                String d_sql = "if exists (select 1 from sysobjects where name = '" + viewName + "' and type = 'V') drop view " + viewName + " ;";
                scipt = d_sql;
            }
            scipt += "create " + (db_type.equals("sqlserver") ? "" : " or replace ") + "view " + viewName + " as " + viewStr + ";";
            scipt += "insert into table2View(table2view_key,viewname,roleid,tablename,success)values('" + tableName + "_" + personKey
                    + "','" + viewName + "','" + personKey + "','" + tableName + "',1)";
//            scipt += "insert into table2View(table2view_key,viewname,roleid,tablename,viewscript,success)values('" + tableName + "_" + personKey
//                    + "','" + viewName + "','" + personKey + "','" + tableName + "','" + scipt.replace("'", "''") + "',1)";
            ImplUtil.exSQLs(tx, session, scipt, ";");
            tx.commit();
        } catch (Exception ex) {
            ImplUtil.rollBack(tx);
            log.error(ImplUtil.getSQLExceptionMsg(ex));
        } finally {
            HibernateUtil.closeSession();
        }
    }

    @Override
    public List getReportRight(String roleKey) throws RemoteException {
        List list = new ArrayList();
        if (roleKey == null || roleKey.equals("-1")) {
            return list;
        }
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            List data = s.createSQLQuery("select reportDef_key,min(fun_flag) from rolereport where role_key ='" + roleKey + "' and fun_flag>0 group by reportDef_key").list();
            for (Object obj : data) {
                Object[] objs = (Object[]) obj;
                list.add(objs[0] + "@@" + objs[1]);
            }
            tx.commit();
        } catch (Exception e) {
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return list;
    }

    @Override
    public ValidateSQLResult addUser(List<String[]> users) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        String tableName = CountUtil.getCommTempTable();
        String db_type = HibernateUtil.getDb_type();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            String sql = null;
            if (db_type.equals("oracle")) {
                sql = "create table " + tableName + "(a01_key varchar2(255),role_key varchar2(255))";
            } else {
                sql = "create table " + tableName + "(a01_key varchar(255),role_key varchar(255))";
            }
            s.createSQLQuery(sql).executeUpdate();
            for (String[] data : users) {
                sql = "insert into " + tableName + "(a01_key,role_key)values('" + data[0] + "','" + data[1] + "')";
                s.createSQLQuery(sql).executeUpdate();
            }
            sql = "insert into a01password(a01password_key,a01_key)select distinct a01_key,a01_key from " + tableName
                    + " b where not exists(select 1 from a01password apw where apw.a01_key=b.a01_key);";
            sql += "insert into rolea01(rolea01_key,a01Password_key,role_key)select " + DbUtil.getUIDForDb(db_type)
                    + ",apw.a01password_key,b.role_key from " + tableName + " b,a01password apw where b.a01_key=apw.a01_key "
                    + "and not exists(select 1 from rolea01 ra where ra.role_key=b.role_key and ra.a01password_key=apw.a01password_key)";
            ImplUtil.exSQLs(tx, s, sql, ";");
            s.createSQLQuery("drop table " + tableName).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            } catch (Exception ex) {
            }
            ImplUtil.rollBack(tx);
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;

    }

    @Override
    public ValidateSQLResult delUser(String userKeys, String a01passwordKey) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            StringBuilder ex_str = new StringBuilder();
            ex_str.append("delete from roleDept where roleA01_key in ").append(userKeys).append(";");
            ex_str.append("delete from roleA01 where roleA01_key in ").append(userKeys).append(";");
            ex_str.append("delete from WorkFlowA01 where a01PassWord_key in ").append(a01passwordKey).append(" and not exists(select 1 from rolea01 ra where  ra.a01password_key=WorkFlowA01.a01password_key);");
            ImplUtil.exSQLs(tx, s, ex_str.toString(), ";");
            tx.commit();
        } catch (Exception e) {
            ImplUtil.rollBack(tx);
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;

    }

    @Override
    public ValidateSQLResult defineUserRole(List<String[]> data) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        String tableName = CountUtil.getCommTempTable();
        String create_sql = "";
        String db_type = HibernateUtil.getDb_type();
        if (db_type.equals("sqlserver")) {
            create_sql = "create table " + tableName + "(a01password_key varchar(255),role_key varchar(255),fun_flag int)";
        } else {
            create_sql = "create table " + tableName + "(a01password_key varchar2(255),role_key varchar2(255),fun_flag int)";
        }
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            s.createSQLQuery(create_sql).executeUpdate();
            for (String[] r_data : data) {
                s.createSQLQuery("insert into " + tableName + "(a01password_key,role_key,fun_flag)values('" + r_data[0] + "','" + r_data[1] + "'," + r_data[2] + ")").executeUpdate();
            }
            s.flush();
            String uid = DbUtil.getUIDForDb(db_type);
            String sql = "delete from RoleDept where exists(select 1 from " + tableName + " t,RoleA01 where RoleDept.roleA01_key=RoleA01.roleA01_key and t.a01password_key=RoleA01.a01password_key and t.role_key=RoleA01.role_key and t.fun_flag=0);";
            sql += "delete from RoleA01 where exists(select 1 from " + tableName + " t where t.a01password_key=RoleA01.a01password_key and t.role_key=RoleA01.role_key and t.fun_flag=0);";
            sql += "insert into RoleA01(roleA01_key,a01PassWord_key,role_key)select " + uid + ",a01password_key,role_key from " + tableName + " t where fun_flag=1 and not exists(select 1 from RoleA01 ra where ra.role_key=t.role_key and ra.a01password_key=t.a01password_key);";
            sql += "drop table " + tableName;
            ImplUtil.exSQLs(tx, s, sql, ";");
            tx.commit();
        } catch (Exception e) {
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            } catch (Exception ex) {
            }
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult cryptA01PassWord() throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            List list = s.createSQLQuery("select a01PassWord_key,pswd from A01PassWord").list();
            StringBuilder sbf = new StringBuilder();
            for (Object object : list) {
                Object[] obj = (Object[]) object;
                String crypt;
                if (obj[1] == null || obj[1].toString().equals("")) {
                    crypt = CipherUtil.encodeByMD5("");
                } else {
                    crypt = CipherUtil.encodeByMD5(obj[1].toString());
                }
                sbf.append("update A01PassWord set pswd='").append(crypt).append("'").append(" where a01PassWord_key='").append(obj[0]).append("';");
            }
            String syscrypt;
            Object object = s.createSQLQuery("select sysParameter_key,SysParameter_value from SysParameter where sysParameter_key='SysManPass'").uniqueResult();
            Object[] obj = (Object[]) object;
            if (obj[1] == null || obj[1].toString().equals("")) {
                syscrypt = CipherUtil.encodeByMD5("");
            } else {
                syscrypt = CipherUtil.encodeByMD5(obj[1].toString());
            }
            sbf.append("update SysParameter set sysparameter_value='").append(syscrypt).append("' where sysParameter_key='SysManPass';");
            sbf.append("insert into SysParameter(sysparameter_key,sysparameter_code,sysparameter_value) select 'cryptA01PassWord','sa','0';");
            ImplUtil.exSQLs(tx, s, sbf.toString(), ";");
            tx.commit();
        } catch (Exception e) {
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }
}
