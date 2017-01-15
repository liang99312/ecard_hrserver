/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jhrcore.server;

import org.jhrcore.server.util.ImplUtil;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.jboss.annotation.ejb.Clustered;
import org.jhrcore.iservice.PersonService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jhrcore.client.CommUtil;
import org.jhrcore.util.DateUtil;
import org.jhrcore.util.DbUtil;
import org.jhrcore.util.PinYinMa;
import org.jhrcore.util.SysUtil;
import org.jhrcore.util.PublicUtil;
import org.jhrcore.entity.A01;
import org.jhrcore.entity.AutoNoRule;
import org.jhrcore.entity.BasePersonAppendix;
import org.jhrcore.entity.BasePersonChange;
import org.jhrcore.entity.DeptCode;
import org.jhrcore.entity.RyChgLog;
import org.jhrcore.entity.SysParameter;
import org.jhrcore.util.UtilTool;
import org.jhrcore.entity.change.ChangeField;
import org.jhrcore.entity.change.ChangeItem;
import org.jhrcore.entity.change.ChangeScheme;
import org.jhrcore.entity.VirtualDeptPersonLog;
import org.jhrcore.entity.base.EntityDef;
import org.jhrcore.entity.base.FieldDef;
import org.jhrcore.entity.change.ChangeMethod;
import org.jhrcore.entity.query.Condition;
import org.jhrcore.entity.query.QueryScheme;
import org.jhrcore.entity.right.FuntionRight;
import org.jhrcore.entity.salary.ValidateSQLResult;
import org.jhrcore.entity.showstyle.ShowScheme;
import org.jhrcore.mutil.EmpUtil;
import org.jhrcore.rebuild.EntityBuilder;
import org.jhrcore.server.util.CountUtil;
import org.jhrcore.server.util.FunUtil;
import org.jhrcore.server.util.PersonUtil;
import org.jhrcore.server.util.RebuildUtil;

/**
 *
 * @author hflj
 */
@Clustered
@Stateless
@Remote(PersonService.class)
public class PersonServiceImplForSQL extends SuperImpl implements PersonService {

    private Logger log = Logger.getLogger(PersonServiceImplForSQL.class.getName());
    private static String picture_dir;
    private static String file_dir;
    private static String docu_dir;

    public PersonServiceImplForSQL() throws RemoteException {
        super(ServerApp.rmiPort);
        picture_dir = System.getProperty("user.dir") + "/" + "pic";
        file_dir = System.getProperty("user.dir") + "/" + "file";
    }

    public PersonServiceImplForSQL(int port) throws RemoteException {
        super(port);
        picture_dir = System.getProperty("user.dir") + "/" + "pic";
        file_dir = System.getProperty("user.dir") + "/" + "file";
    }

    @Override
    public byte[] downloadPicture(String pic_path) throws RemoteException {
        if (pic_path == null || pic_path.equals("")) {
            return null;
        }
        if (pic_path.equals("@Logo")) {
            pic_path = ServerApp.getSys_properties().getProperty("@Logo");
            if (pic_path == null || pic_path.trim().equals("")) {
                return null;
            }
        }
        if (pic_path.equals("@Icon")) {
            pic_path = ServerApp.getSys_properties().getProperty("@Icon");
            if (pic_path == null || pic_path.trim().equals("")) {
                return null;
            }
        }
        try {
            File file = new File(picture_dir + "/" + pic_path);
            if (file == null || !file.exists()) {
                log.error("file not exists:" + picture_dir + "/" + pic_path);
                return null;
            }
            byte buffer[] = new byte[(int) file.length()];
            BufferedInputStream input = new BufferedInputStream(
                    new FileInputStream(file));
            input.read(buffer, 0, buffer.length);
            input.close();
            return (buffer);
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    @Override
    public void deletePicture(String pic_path) throws RemoteException {
        if (pic_path == null || pic_path.equals("")) {
            return;
        }
        File file = new File(picture_dir + "/" + pic_path);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void uploadPicture(byte[] p_byte, String pic_path) throws RemoteException {
        if (picture_dir == null || picture_dir.equals("") || pic_path == null || pic_path.trim().equals("")) {
            return;
        }
        if (pic_path.startsWith("@Logo")) {
            pic_path = pic_path.substring(5);
            ServerApp.getSys_properties().setProperty("@Logo", pic_path);
            ServerApp.save();
        }
        if (pic_path.startsWith("@Icon")) {
            pic_path = pic_path.substring(5);
            ServerApp.getSys_properties().setProperty("@Icon", pic_path);
            ServerApp.save();
        }
        if (pic_path == null || pic_path.trim().equals("")) {
            return;
        }
        try {
            File file = new File(picture_dir + "/" + pic_path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            BufferedOutputStream output = new BufferedOutputStream(
                    new FileOutputStream(file));
            output.write(p_byte);
            output.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public void deleteFile(String path) throws RemoteException {
        if (path == null || path.equals("")) {
            return;
        }
        path = path.replace("$", file_dir);
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
            return;
        }
    }

    @Override
    public byte[] downloadFile(String pic_path, List<String> list) throws RemoteException {
        if (pic_path == null || pic_path.equals("")) {
            return null;
        }
        try {
            File file = null;
            if (pic_path.contains("$")) {
                pic_path = pic_path.replace("$", file_dir);
                file = new File(pic_path);
            } else {
                list.add(0, file_dir);
                for (String docu_path : list) {
                    String pic_path2 = docu_path + "/" + pic_path;
                    file = new File(pic_path2);
                    if (file != null && file.exists()) {
                        break;
                    }
                }
            }
            if (file == null || !file.exists()) {
                log.error("file not exists:" + pic_path);
                return null;
            }
            byte buffer[] = new byte[(int) file.length()];
            BufferedInputStream input = new BufferedInputStream(
                    new FileInputStream(file));
            input.read(buffer, 0, buffer.length);
            input.close();
            return (buffer);
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    @Override
    public boolean isNullFloder(String f_path) {
        File file = new File(f_path);
        if (file.isDirectory()) {
            if (file.list().length > 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void changePersonChange(Transaction tx, Session session, HashSet<BasePersonChange> set, ChangeScheme changeScheme, RyChgLog model_log) throws Exception {
        String ip = model_log.getChg_ip();
        String mac = model_log.getChg_mac();
        String user = model_log.getChg_user();
        String db_type = "sqlserver";
        String now = DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd HH:mm:ss", db_type);
        String changeTable = "PersonChange_" + changeScheme.getChangeScheme_no();
        Hashtable<String, Hashtable<String, Integer>> appendix_keys = new Hashtable<String, Hashtable<String, Integer>>();
        Hashtable<String, Hashtable<String, String>> methodKeys = new Hashtable<String, Hashtable<String, String>>();
        HashSet<String> entitys = new HashSet<String>();
        boolean change_ht_flag = changeScheme.contains("deptCode");
        String bpcKey = "";
        String a01Key = "";
        String chgHTKey = "";
        for (BasePersonChange bpc : set) {
            bpcKey += ",'" + bpc.getBasePersonChange_key() + "'";
            a01Key += ",'" + bpc.getA01().getA01_key() + "'";
            if (change_ht_flag && bpc.isChange_ht_flag()) {
                chgHTKey += ",'" + bpc.getA01().getA01_key() + "'";
            }
        }
        bpcKey = bpcKey.substring(1);
        a01Key = a01Key.substring(1);
        if (!chgHTKey.equals("")) {
            chgHTKey = chgHTKey.substring(1);
        }
        List cfields = session.createQuery("from ChangeField cf where cf.changeScheme.changeScheme_key='" + changeScheme.getChangeScheme_key() + "'").list();
        changeScheme.setChangeFields(new HashSet(cfields));
        for (ChangeField cf : changeScheme.getChangeFields()) {
            String entity_name = cf.getAppendix_name();
            if (entity_name.equals("A01") || entity_name.equals("BasePersonChange")) {
                continue;
            }
            try {
                Class theClass = Class.forName("org.jhrcore.entity." + cf.getAppendix_name());
                if (theClass.getSuperclass().getSimpleName().equals("A01")) {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }
            entitys.add(entity_name);
        }
        for (BasePersonChange bpc : set) {
            methodKeys.put(bpc.getA01().getA01_key(), EmpUtil.tranMethod(bpc, changeScheme, entitys));
        }
        String ex_sql = "";
        for (String entity_name : entitys) {
            String hql = " select a01_key,max(a_id) as num from " + entity_name + " where a01_key in (" + a01Key + ") group by a01_key";
            List a_id_list = session.createSQLQuery(hql).list();
            if (a_id_list != null) {
                for (Object obj : a_id_list) {
                    Object[] objs = (Object[]) obj;
                    Hashtable<String, Integer> a_id_key = appendix_keys.get(objs[0].toString());
                    if (a_id_key == null) {
                        a_id_key = new Hashtable<String, Integer>();
                    }
                    a_id_key.put(entity_name, Integer.valueOf(objs[1].toString()));
                    appendix_keys.put(objs[0].toString(), a_id_key);
                }
            }
//            ex_sql = "update " + entity_name + " set last_flag='' where a01_key in (" + a01Key + ");";
        }
        String key = DbUtil.getUIDForDb(db_type);
        ex_sql += "insert into RyChgLog(rychglog_key,a01_key,chg_ip,chg_mac,chg_user,chg_date,a0190,a0101,dept_name,chg_field,chg_type,old_dept_code,new_dept_code,beforestate,afterstate,changeScheme_key) ";
        ex_sql += "select " + key + ",a01.a01_key,'" + ip + "','" + mac + "','" + user + "'," + now + ",a01.a0190,a01.a0101,d.content,ac.chgfieldname,'" + changeScheme.getChangeScheme_name() + "'";
        ex_sql += ",ac.b_dept_code,ac.a_dept_code,ac.beforestate,ac.afterstate,'" + changeScheme.getChangeScheme_key() + "' from A01chg ac,BasePersonChange bpc,A01 a01,DeptCode d where ac.basepersonchange_key=bpc.basepersonchange_key and bpc.a01_key=a01.a01_key and a01.deptCode_key=d.deptCode_key "
                + "and bpc.changescheme_key='" + changeScheme.getChangeScheme_key() + "' and ac.basepersonchange_key in(" + bpcKey + ");";
        String a01_sql = "update A01 set ";
        for (ChangeItem ci : changeScheme.getChangeItems()) {
            a01_sql += PersonUtil.getDbFieldName(ci.getFieldName(), false) + "=" + changeTable + "." + PersonUtil.getDbFieldName(ci.getFieldName(), true) + ",";
        }
        for (ChangeField ce : changeScheme.getChangeFields()) {
            if (ce.getAppendix_name() == null || !"A01".equals(ce.getAppendix_name())) {
                continue;
            }
            String fieldName = PersonUtil.getDbFieldName(ce.getAppendix_field(), false);
            String importField = fieldName;
            String importTable = changeTable;
            if (ce.isFrom_import()) {
                importTable = ce.getImport_name();
                if ("BasePersonChange".equals(importTable) && (importField.startsWith("new_") || importField.startsWith("old_"))) {// fields.contains(ce.getImport_field())) {
                    importTable = changeTable;
                }
                importField = PersonUtil.getDbFieldName(ce.getImport_field(), false);
            }
            a01_sql += fieldName + "=" + importTable + "." + importField + ",";
        }
        a01_sql = a01_sql.substring(0, a01_sql.length() - 1);
        a01_sql += " from A01 A01," + changeTable + ",BasePersonChange where A01.a01_key=BasePersonChange.a01_key and " + changeTable + ".basepersonchange_key=BasePersonChange.basepersonchange_key and BasePersonChange.basepersonchange_key in (" + bpcKey + ");";
        ex_sql += a01_sql;
        String zp_sql = "update Zp_resume set z_state='入职结束' where a01_key in(" + a01Key + ");";
        ex_sql += zp_sql;
        if (!chgHTKey.equals("") && change_ht_flag) {
            ex_sql += "update HT01 set deptCode_key=a01.deptCode_key from A01 a01,HT01 ht01 where ht01.a01_key=a01.a01_key and a01.a01_key in (" + chgHTKey + ");";
        }
        ImplUtil.exSQLs(tx, session, ex_sql, ";");
        session.flush();
        boolean b_include_type = changeScheme.contains("a0191");//判断是否包含类别变动>>>
        for (BasePersonChange bpc : set) {
            ex_sql = "";
            A01 bp = bpc.getA01();
            Hashtable<String, BasePersonAppendix> tmp_table = new Hashtable<String, BasePersonAppendix>();
            Hashtable<String, Integer> a_id_key = appendix_keys.get(bp.getA01_key());
            Hashtable<String, String> methods = methodKeys.get(bp.getA01_key());
            Hashtable<String, String> updateSQLs = new Hashtable<String, String>();
            for (ChangeField ce : changeScheme.getChangeFields()) {
                String entity = ce.getAppendix_name();
                if (entity == null || "A01".equals(entity) || "BasePersonChange".equals(entity)) {
                    continue;
                }
                String appendixMethod = methods.get(entity);
                if (appendixMethod.equals("不做处理")) {
                    continue;
                }
                if ("更新".equals(ce.getC_type())) {
//                if (appendixMethod.contains("更新") && "更新".equals(ce.getC_type())) {
                    if (a_id_key != null) {
                        Object obj = PublicUtil.getProperty(bpc, SysUtil.tranField(ce.getImport_field()));
                        String sql = updateSQLs.get(entity);
                        if (sql == null) {
                            sql = "update " + entity + " set ";
                        }
                        sql += SysUtil.tranField(ce.getAppendix_field()) + "=";
                        String field_type = ce.getField_type().toLowerCase();
                        if (field_type.equals("string") || field_type.equals("code")) {
                            sql += (obj == null) ? null : "'" + obj.toString() + "'";
                        } else if (field_type.equals("int") || field_type.equals("integer") || field_type.equals("float") || field_type.equals("double") || field_type.equals("bigdecimal")) {
                            sql += (obj == null) ? "0" : obj.toString();
                        } else if (field_type.equals("date")) {
                            sql += obj == null ? null : DateUtil.toStringForQuery((Date) obj, "yyyy-MM-dd HH:mm:ss");
                        } else if (field_type.equals("boolean")) {
                            obj = obj == null ? false : obj;
                            sql += ((Boolean) obj) ? "1" : "0";
                        }
                        sql += ",";
                        updateSQLs.put(entity, sql);
                    }
                }
                if (appendixMethod.contains("追加") && !"更新".equals(ce.getC_type())) {
                    BasePersonAppendix bpa = tmp_table.get(ce.getAppendix_name());
                    if (bpa == null) {
                        try {
                            Class theClass = Class.forName("org.jhrcore.entity." + ce.getAppendix_name());
                            if (theClass.getSuperclass().getSimpleName().equals("A01")) {
                                continue;
                            }
                            bpa = (BasePersonAppendix) UtilTool.createUIDEntity(theClass);
                            bpa.setA01(bp);
                            bpa.setLast_flag("最新");
                            Integer a_id = 1;
                            if (a_id_key != null) {
                                if (a_id_key.get(ce.getAppendix_name()) != null) {
                                    a_id = a_id_key.get(ce.getAppendix_name()) + 1;
                                }
                            }
                            bpa.setA_id(a_id);
                            List<String> fields = EntityBuilder.getDeclareFieldNameListOf(theClass, EntityBuilder.COMM_FIELD_ALL);
                            if (fields.contains("wf_state")) {
                                PublicUtil.setValueBy2(bpa, "wf_state", "未提交");
                            }
                            ex_sql += "update " + entity + " set last_flag='' where a01_key='" + bp.getA01_key() + "';";
                        } catch (ClassNotFoundException ex) {
                            log.error(ex);
                        }
                    }
                    if (!ce.isFrom_import()) {
                        try {
                            String fieldName1 = ce.getAppendix_field();
                            Method method = bpc.getClass().getMethod("get" + fieldName1.substring(0, 1).toUpperCase() + fieldName1.substring(1), new Class[]{});
                            Object tmp_obj = method.invoke(bpc, new Object[]{});
                            fieldName1 = ce.getAppendix_field();
                            Class field_class = bpa.getClass().getField(fieldName1).getType();
                            method = bpa.getClass().getMethod("set" + fieldName1.substring(0, 1).toUpperCase() + fieldName1.substring(1), new Class[]{field_class});
                            method.invoke(bpa, new Object[]{tmp_obj});
                        } catch (Exception ex) {
                            log.error(ex);
                        }
                    } else {
                        try {
                            String entity_name = ce.getImport_name();
                            Object tmp_obj = null;
                            String fieldName = ce.getImport_field();
                            Method method;
                            Class old_field_class;
                            if (entity_name.equals("A01")) {
                                method = A01.class.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), new Class[]{});
                                tmp_obj = method.invoke(bp, new Object[]{});
                                old_field_class = A01.class.getField(fieldName).getType();
                            } else {
                                method = bpc.getClass().getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), new Class[]{});
                                tmp_obj = method.invoke(bpc, new Object[]{});
                                old_field_class = bpc.getClass().getField(fieldName).getType();
                            }
                            fieldName = ce.getAppendix_field();
                            Class field_class = bpa.getClass().getField(fieldName).getType();
                            tmp_obj = PublicUtil.getDefaultValueForType(tmp_obj, field_class.getSimpleName(), old_field_class.getSimpleName());
                            method = bpa.getClass().getMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), new Class[]{field_class});
                            method.invoke(bpa, new Object[]{tmp_obj});
                        } catch (Exception ex) {
                            log.error(ex);
                        }
                    }
                    if (bpa == null) {
                        continue;
                    }
                    tmp_table.put(ce.getAppendix_name(), bpa);
                }
            }
            //触发校验规则
            for (BasePersonAppendix bpa : tmp_table.values()) {
                ValidateSQLResult v = CommUtil.entity_triger(bpa, true);
                if (v != null && v.getResult() != 0) {
                    throw new Exception(v.getMsg());
                }
            }
            for (String entity : updateSQLs.keySet()) {
                String sql = updateSQLs.get(entity);
                Integer a_id = a_id_key.get(entity);
                ex_sql += sql.substring(0, sql.length() - 1) + " where a01_key='" + bp.getA01_key() + "' and a_id=" + a_id + ";";
            }
            ImplUtil.exSQLs(tx, session, ex_sql, ";");
            for (BasePersonAppendix bpa : tmp_table.values()) {
                session.save(bpa);
            }
        }
        session.flush();
        String sql = "update BasePersonChange set chg_state='审批通过',action_date=" + now + " where basePersonChange_key in (" + bpcKey + ");";
        if (b_include_type) {
            String newPersonClass = changeScheme.getNewPersonClassName();
            List list = session.createSQLQuery("select ed.entityName,ed.entityCaption from TabName ed,EntityClass ec where ed.entityclass_key=ec.entityclass_key and ec.entityType_code='CLASS'  order by ed.order_no").list();
            String newClassName = "";
            for (Object obj : list) {
                Object[] objs = (Object[]) obj;
                sql += "delete from " + objs[0].toString() + " where a01_key in (" + a01Key + ");";
                if (newPersonClass.equals(objs[0].toString())) {
                    newClassName = objs[1].toString();
                }
            }
            sql += "update A01 set  a0191='" + newClassName + "' where a01_key in (" + a01Key + ");";
            sql += "insert into " + newPersonClass + "(a01_key) select a01_key from A01 where a01_key in (" + a01Key + ");";
            String s_select = "";
            String s_from = "BasePersonChange";
            String s_where = "BasePersonChange.a01_key=" + newPersonClass + ".a01_key";
            for (ChangeField cf : changeScheme.getChangeFields()) {
                if (cf.getAppendix_name() == null || !newPersonClass.equals(cf.getAppendix_name())) {
                    continue;
                }
                String fieldName = PersonUtil.getDbFieldName(cf.getAppendix_field(), false);
                String importField = fieldName;
                String importTable = changeTable;
                if (cf.isFrom_import()) {
                    importTable = cf.getImport_name();
                    if ("BasePersonChange".equals(importTable) && (importField.startsWith("new_") || importField.startsWith("old_"))) {// fields.contains(cf.getImport_field())) {
                        importTable = changeTable;
                    }
                    importField = PersonUtil.getDbFieldName(cf.getImport_field(), false);
                }
                if (!s_from.contains(importTable)) {
                    s_from += "," + importTable;
                    s_where += " and BasePersonChange.basePersonChange_key=" + importTable + ".basePersonChange_key";
                }
                s_select += "," + fieldName + "=" + importTable + "." + importField;
            }
            if (!s_select.equals("")) {
                s_select = s_select.substring(1);
                sql += "update " + newPersonClass + " set " + s_select + " from " + newPersonClass + "," + s_from + " where " + s_where
                        + " and BasePersonChange.basepersonchange_key in(" + bpcKey.toString() + ");";
            }
        }
        if ("借调".equals(changeScheme.getScheme_type())) {
            int mod = 0;//1：调回；2：调出；0：借调中的再次调动
            String str = (String) session.createSQLQuery("select sysparameter_value from sysparameter sp where sp.sysparameter_key='EmpJd.scheme'").uniqueResult();
            if (str != null && !str.trim().equals("")) {
                String[] strs = str.split(";");
                if (strs.length > 0 && strs[0].equals(changeScheme.getChangeScheme_key())) {
                    mod = 1;
                } else if (strs.length > 1 && strs[1].equals(changeScheme.getChangeScheme_key())) {
                    mod = 2;
                }
            }
            boolean contain_dept = changeScheme.contains("deptCode");
            if ((mod == 2 || mod == 1) && !contain_dept) {
                throw new Exception("调出模板中未包含部门字段");
            }
            if (mod == 2) {
                for (BasePersonChange bpc : set) {
                    String a01_key = "'" + bpc.getA01().getA01_key() + "'";
                    DeptCode old_dept = (DeptCode) PublicUtil.getProperty(bpc, "old_deptCode");
                    DeptCode new_dept = (DeptCode) PublicUtil.getProperty(bpc, "new_deptCode");
                    sql += "insert into A01jd(a01jd_key,a01_key,old_deptcode_key,new_deptcode_key)values(" + a01_key + "," + a01_key + ",'" + old_dept.getDeptCode_key() + "','" + new_dept.getDeptCode_key() + "');";
                    sql += "insert into A01jdLog(a01jdlog_key,a01_key,old_deptcode_key,new_deptcode_key,c_date,c_user,old_dept_code,old_dept_content,new_dept_code,new_dept_content,c_type)values(newid()," + a01_key + ",'" + old_dept.getDeptCode_key() + "','"
                            + new_dept.getDeptCode_key() + "'," + now + ",'" + model_log.getChg_user() + "','" + old_dept.getDept_code() + "','" + old_dept.getContent() + "','" + new_dept.getDept_code() + "','" + new_dept.getContent() + "','调出');";
                }
            } else if (mod == 1) {
                for (BasePersonChange bpc : set) {
                    String a01_key = "'" + bpc.getA01().getA01_key() + "'";
                    DeptCode old_dept = (DeptCode) PublicUtil.getProperty(bpc, "old_deptCode");
                    DeptCode new_dept = (DeptCode) PublicUtil.getProperty(bpc, "new_deptCode");
                    sql += "delete from  A01jd where a01_key=" + a01_key + ";";
                    sql += "insert into A01jdLog(a01jdlog_key,a01_key,old_deptcode_key,new_deptcode_key,c_date,c_user,old_dept_code,old_dept_content,new_dept_code,new_dept_content,c_type)values(newid()," + a01_key + ",'" + old_dept.getDeptCode_key() + "','"
                            + new_dept.getDeptCode_key() + "'," + now + ",'" + model_log.getChg_user() + "','" + old_dept.getDept_code() + "','" + old_dept.getContent() + "','" + new_dept.getDept_code() + "','" + new_dept.getContent() + "','调回');";
                }
            } else {
                if (contain_dept) {
                    for (BasePersonChange bpc : set) {
                        String a01_key = "'" + bpc.getA01().getA01_key() + "'";
                        DeptCode old_dept = (DeptCode) PublicUtil.getProperty(bpc, "old_deptCode");
                        DeptCode new_dept = (DeptCode) PublicUtil.getProperty(bpc, "new_deptCode");
                        sql += "update a01jd set new_deptcode_key='" + new_dept.getDeptCode_key() + "' where a01_key=" + a01_key + ";";
                        sql += "insert into A01jdLog(a01jdlog_key,a01_key,old_deptcode_key,new_deptcode_key,c_date,c_user,old_dept_code,old_dept_content,new_dept_code,new_dept_content,c_type)values(newid()," + a01_key + ",'" + old_dept.getDeptCode_key() + "','"
                                + new_dept.getDeptCode_key() + "'," + now + ",'" + model_log.getChg_user() + "','" + old_dept.getDept_code() + "','" + old_dept.getContent() + "','" + new_dept.getDept_code() + "','" + new_dept.getContent() + "','中转');";
                    }
                }
            }
        } else if ("临时".equals(changeScheme.getScheme_type())) {
            boolean contain_dept = changeScheme.contains("deptCode");
            if (!contain_dept) {
                throw new Exception("调出模板中未包含部门字段");
            }
            for (BasePersonChange bpc : set) {
                String a01_key = "'" + bpc.getA01().getA01_key() + "'";
                DeptCode old_dept = (DeptCode) PublicUtil.getProperty(bpc, "old_deptCode");
                DeptCode new_dept = (DeptCode) PublicUtil.getProperty(bpc, "new_deptCode");
                sql += "insert into A01tempchange(a01tempchange_key,a01_key,old_deptcode_key,old_dept_code,old_dept_content,new_deptcode_key,new_dept_code,new_dept_content,basePersonChange_key) "
                        + "values(" + a01_key + "," + a01_key + ",'" + old_dept.getDeptCode_key() + "','" + old_dept.getDept_code() + "','" + old_dept.getContent() + "'"
                        + ",'" + new_dept.getDeptCode_key() + "','" + new_dept.getDept_code() + "','" + new_dept.getContent() + "','" + bpc.getBasePersonChange_key() + "');";
                sql += "insert into A01tempchangelog(a01tempchangelog_key,a01_key,old_deptcode_key,new_deptcode_key,c_date,c_user,old_dept_code,old_dept_content,new_dept_code,new_dept_content,c_type)values(newid()," + a01_key + ",'" + old_dept.getDeptCode_key() + "','"
                        + new_dept.getDeptCode_key() + "'," + now + ",'" + model_log.getChg_user() + "','" + old_dept.getDept_code() + "','" + old_dept.getContent() + "','" + new_dept.getDept_code() + "','" + new_dept.getContent() + "','临时变动');";
            }
        }
        ImplUtil.exSQLs(tx, session, sql, ";");
    }

    @Override
    public ValidateSQLResult saveChangeScheme(ChangeScheme changeScheme, QueryScheme queryScheme, boolean tableUpdate, String roleKey) throws RemoteException {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        String db_type = HibernateUtil.getDb_type();
        int order = changeScheme.getChangeScheme_no();
        String entityName = "PersonChange_" + order;
        String schemeName = changeScheme.getChangeScheme_name();
        String schemeKey = changeScheme.getChangeScheme_key();
        try {
            //保存方案
            String sql = "delete from changemethod where changescheme_key='" + schemeKey + "' or changemethod_key like '" + schemeKey + "%';";
            sql += "delete from changeitem where changescheme_key='" + schemeKey + "';";
            sql += "delete from ChangeField where changescheme_key='" + schemeKey + "';";
            ImplUtil.exSQLs(tx, session, sql, ";");
            session.saveOrUpdate(changeScheme);
            session.flush();
            for (ChangeItem ci : changeScheme.getChangeItems()) {
                session.save(ci);
            }
            session.flush();
            for (ChangeField ci : changeScheme.getChangeFields()) {
                session.save(ci);
            }
            session.flush();
            for (ChangeMethod ci : changeScheme.getChangeMethods()) {
                session.save(ci);
            }
            session.flush();
            //保存条件
            sql = "delete from condition where queryscheme_key ='changeScheme_" + schemeKey + "';";
            sql += "delete from queryscheme where queryscheme_key='changeScheme_" + schemeKey + "';";
//            String group_name = changeScheme.getChangeScheme_type() == null ? "''" : "'" + changeScheme.getChangeScheme_type() + "'";
            
            ImplUtil.exSQLs(tx, session, sql, ";");
            if (queryScheme != null) {
                queryScheme.setScheme_type("调配模板（" + schemeName + "）");
                session.save(queryScheme);
                for (Condition c : queryScheme.getConditions()) {
                    session.save(c);
                }
            }
            session.flush();
            sql = "insert into TABNAME(entity_key,entityName,entitycaption,canmodify,order_no,success_build,limit_flag,init_flag)"
                    + "select top 1 '" + entityName + "','" + entityName + "','" + schemeName + "','其他业务'," + order + ",0,0,0 from changescheme where changeScheme_key='" + schemeKey + "'"
                    + " and not exists(select 1 from TabName where entityName='" + entityName + "');";
            sql += "update TabName set entityClass_key=ec.entityClass_key,entitycaption='" + schemeName + "' from EntityClass ec,TabName t where ec.entityType_code='RSDD' and t.entityName='" + entityName + "';";
            ImplUtil.exSQLs(tx, session, sql, ";");
            //保存功能菜单
            String key = EmpUtil.changeSchemeCode;
            FunUtil.rebuildFun(session, roleKey, key, key + ".scheme_" + schemeKey, schemeName);
            FunUtil.rebuildFun(session, roleKey, key + ".scheme_" + schemeKey, key + ".scheme_" + schemeKey + "_view", "查看");
            FunUtil.rebuildFun(session, roleKey, key + ".scheme_" + schemeKey, key + ".scheme_" + schemeKey + "_use", "执行");
            EntityDef ed = (EntityDef) session.createQuery("from EntityDef ed where ed.entityName='" + entityName + "'").uniqueResult();
            List list = session.createQuery("from FieldDef fd where fd.entityDef.entityName='A01' or fd.entityDef.entityClass.entityType_code='ANNEX'").list();
            Hashtable<String, FieldDef> fds = new Hashtable<String, FieldDef>();
            for (Object obj : list) {
                FieldDef fd = (FieldDef) obj;
                fds.put(fd.getField_name(), fd);
            }
            FieldDef fd_dept = new FieldDef();
            fd_dept.setField_name("deptCode");
            fd_dept.setField_caption("部门");
            fd_dept.setField_type("String");
            fd_dept.setField_width(255);
            fd_dept.setView_width(40);
            fds.put("deptCode", fd_dept);
            FieldDef fd_g10 = new FieldDef();
            fd_g10.setField_name("g10");
            fd_g10.setField_caption("岗位");
            fd_g10.setField_type("String");
            fd_g10.setField_width(255);
            fd_g10.setView_width(40);
            fds.put("g10", fd_g10);
            List<String> fields = EntityBuilder.getCommFieldNameListOf(FieldDef.class, EntityBuilder.COMM_FIELD_VISIBLE);
            int ind = 1;
            sql = "";
            for (ChangeItem ci : changeScheme.getChangeItems()) {
                String fieldName = SysUtil.tranField(ci.getFieldName());
                FieldDef copyfd = fds.get(fieldName);
                if (copyfd == null) {
                    continue;
                }
                String oldName = "old_" + fieldName;
                FieldDef fd = new FieldDef();
                PublicUtil.copyProperties(copyfd, fd, fields, fields);
                fd.setField_key(schemeKey + "_" + oldName);
                fd.setField_mark("自定义固定项");
                fd.setEntityDef(ed);
                fd.setOrder_no(ind);
                fd.setField_name(oldName);
                fd.setField_caption("变动前" + fd.getField_caption());
                sql += RebuildUtil.buildSQL(fd, db_type) + "|";
                ind++;
                String newName = "new_" + fieldName;
                fd = new FieldDef();
                PublicUtil.copyProperties(copyfd, fd, fields, fields);
                fd.setField_key(schemeKey + "_" + newName);
                fd.setField_mark("自定义固定项");
                fd.setEntityDef(ed);
                fd.setOrder_no(ind);
                fd.setField_name(newName);
                fd.setField_caption("变动后" + fd.getField_caption());
                sql += RebuildUtil.buildSQL(fd, db_type) + "|";
                ind++;
            }
            for (ChangeField ci : changeScheme.getChangeFields()) {
                if ("更新".equals(ci.getC_type())) {
                    continue;
                }
                String fieldName = SysUtil.tranField(ci.getAppendix_field());
                FieldDef copyfd = fds.get(fieldName);
                if (copyfd == null) {
                    continue;
                }
                FieldDef fd = new FieldDef();
                PublicUtil.copyProperties(copyfd, fd, fields, fields);
                fd.setField_key(schemeKey + "_" + fieldName);
                fd.setField_mark("自定义固定项");
                fd.setEntityDef(ed);
                fd.setOrder_no(ind);
                fd.setField_name(fieldName);
                sql += RebuildUtil.buildSQL(fd, db_type) + "|";
                ind++;
            }
            if (tableUpdate) {
                sql += "update SysParameter set sysparameter_value='1' where sysParameter_key='HRTableUpdate'";
            }
            ImplUtil.exSQLs(tx, session, sql, ";\\|");
            tx.commit();
        } catch (Exception e) {
            ImplUtil.rollBack(tx);
            log.error(e);
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult delNewPerson(List<String> a01_keys) throws RemoteException {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        String ex_str = "";
        try {
            List list = session.createSQLQuery("select entityName from tabname t,entityclass ec where t.entityclass_key=ec.entityclass_key and ec.entitytype_code in('CLASS','ANNEX')").list();
            list.add("RYCHGLOG");
            SysUtil.sortStrList(list);
            for (Object obj : list) {
                ex_str += DbUtil.getQueryForMID("delete from " + obj.toString() + " where a01_key in ", a01_keys);
            }
            ex_str += DbUtil.getQueryForMID("delete from A01 where a01_key in ", a01_keys);
        } catch (Exception e) {
            log.error(e);
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
        }
        ImplUtil.exSQLs(tx, session, validateSQLResult, ex_str, ";");
        HibernateUtil.closeSession();
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult createPersonPYM(int type, boolean isAll, String s_where, List<String> a01_keys) throws RemoteException {
        List list = null;
        String hql = "select a0101,pydm,a01_key from A01 a01,DeptCode d where a01.deptCode_key=d.deptCode_key ";
        if (type == 0) {
            list = HibernateUtil.selectSQL(hql + " and a01.a01_key in", a01_keys, false);
        } else {
            String db_type = HibernateUtil.getDb_type();
            hql += " and " + s_where;
            if (!isAll) {
                hql += "and rtrim(ltrim(" + DbUtil.getNull_strForDB(db_type) + "(a01.pydm,'')))=''";
            }
            list = HibernateUtil.selectSQL(hql, false, 0);
        }
        StringBuffer ex_str = new StringBuffer();
        for (Object obj : list) {
            Object[] objs = (Object[]) obj;
            if (objs[0] == null || objs[0].toString().trim().equals("")) {
                continue;
            }
            String str = PinYinMa.ctoE(objs[0].toString());
            if (objs[1] == null || !objs[1].toString().equals(str)) {
                ex_str.append("update A01 set pydm='");
                ex_str.append(str);
                ex_str.append("' where a01_key='");
                ex_str.append(objs[2].toString());
                ex_str.append("';");
            }
        }
        return HibernateUtil.excuteSQLs(ex_str.toString(), ";");
    }

    @Override
    public ValidateSQLResult delPersonFromLog(List<String> log_keys, RyChgLog rcl) throws RemoteException {
        List list = HibernateUtil.selectSQL("select a01_key from rychglog where rychglog_key in ", log_keys, false);
        List<String> a01_keys = new ArrayList<String>();
        for (Object obj : list) {
            if (!a01_keys.contains(obj.toString())) {
                a01_keys.add(obj.toString());
            }
        }
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        HashSet<String> del_tables = new HashSet<String>();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        try {
            List table_list = s.createSQLQuery("select entityname from tabname where entityClass_key in (select entityclass_key from EntityClass where entitytype_code in ('ANNEX','HT','CLASS'))").list();
            del_tables.addAll(table_list);
            del_tables.add("BasePersonChange");
            del_tables.add("k_kaoqin_a01");
            del_tables.add("A01PassWord");
            del_tables.add("HT01");
            del_tables.add("Htrelieve");
            del_tables.add("BaseContractAppendix");
            del_tables.add("PayA01");
            del_tables.add("in_info");
            del_tables.add("in_detail");
            del_tables.add("in_bill");
            del_tables.add("in_overduepay");
            del_tables.add("in_pay");
            del_tables.add("PersonDayRecord");
            del_tables.add("PersonMonthRecord");
            del_tables.add("A01");
            String ex_sqls = "";
            String insert_log = "insert into RYCHGLOG(rychglog_key,afterstate,beforestate,chg_date,chg_field,chg_ip,chg_mac,chg_type,chg_user,a01_key,a0101,a0190,dept_name) ";
            insert_log += "select " + DbUtil.getUIDForDb(HibernateUtil.getDb_type()) + ",'物理删除','逻辑删除'," + DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd hh:mm:ss") + ",null,'" + rcl.getChg_ip() + "','" + rcl.getChg_mac() + "','物理删除','" + rcl.getChg_user()
                    + "',null,a0101,a0190,content from a01,deptcode d WHERE a01.deptcode_key=d.deptcode_key and a01.a01_key in ";
            ex_sqls += DbUtil.getQueryForMID(insert_log, a01_keys);
            ex_sqls += DbUtil.getQueryForMID("delete from K_card where k_kaoqin_a01_key in(select k_kaoqin_a01_key from k_kaoqin_a01 where a01_key in ", a01_keys, ")");
            ex_sqls += DbUtil.getQueryForMID("delete from K_day where k_kaoqin_a01_key in(select k_kaoqin_a01_key from k_kaoqin_a01 where a01_key in ", a01_keys, ")");
            ex_sqls += DbUtil.getQueryForMID("delete from K_month where k_kaoqin_a01_key in(select k_kaoqin_a01_key from k_kaoqin_a01 where a01_key in ", a01_keys, ")");
            ex_sqls += DbUtil.getQueryForMID("delete from K_leave_leave where k_kaoqin_a01_key in(select k_kaoqin_a01_key from k_kaoqin_a01 where a01_key in ", a01_keys, ")");
            ex_sqls += DbUtil.getQueryForMID("delete from K_overtime where k_kaoqin_a01_key in(select k_kaoqin_a01_key from k_kaoqin_a01 where a01_key in ", a01_keys, ")");
//            List no_list = s.createSQLQuery("select changeScheme_no from changeScheme where changeScheme_key<>'EmpScheme_Del'").list();
            List no_list = s.createSQLQuery("select changeScheme_no from changeScheme ").list();
            for (Object obj : no_list) {
                String tmp_str = obj.toString().replace(" ", "");
                ex_sqls += DbUtil.getQueryForMID("delete from PERSONCHANGE_" + tmp_str + " where basePersonChange_key in (select basePersonChange_key from basePersonChange where a01_key in  ", a01_keys, ")");
            }
            ex_sqls += DbUtil.getQueryForMID("delete from a01chg where basePersonChange_key in (select basePersonChange_key from basePersonChange where a01_key in ", a01_keys, ")");
            ex_sqls += DbUtil.getQueryForMID("delete from RoleDept where rolea01_key in (select rolea01_key from A01PassWord apw,rolea01 ra where ra.a01password_key=apw.a01password_key and apw.a01_key in ", a01_keys, ")");
            ex_sqls += DbUtil.getQueryForMID("delete from WorkFlowA01 where a01password_key in (select a01password_key from A01PassWord where a01_key in ", a01_keys, ")");
            ex_sqls += DbUtil.getQueryForMID("delete from RoleA01 where a01password_key in (select a01password_key from A01PassWord where a01_key in ", a01_keys, ")");
            for (String tableName : del_tables) {
                if (tableName.equals("A01")) {
                    continue;
                }
                ex_sqls += DbUtil.getQueryForMID("delete from " + tableName + " where a01_key in ", a01_keys);
            }
            ex_sqls += DbUtil.getQueryForMID("delete from rychglog where a01_key in ", a01_keys);
            ex_sqls += DbUtil.getQueryForMID("delete from A01 where a01_key in ", a01_keys);
            ImplUtil.exSQLs(tx, s, ex_sqls, ";");
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
    public ValidateSQLResult saveRegisterDesign(List paras, List showSchemes, List depts) throws RemoteException {
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        try {
            for (Object obj : paras) {
                SysParameter sp = (SysParameter) obj;
                if (sp.getSysparameter_code().equals("Register.check_flag")) {
                    s.createSQLQuery("update ChangeScheme set check_flag = " + sp.getSysparameter_value() + " where changeScheme_key='EmpScheme_Add'").executeUpdate();
                    ChangeScheme cs = (ChangeScheme) s.createQuery("from ChangeScheme where changeScheme_key='EmpScheme_Add'").uniqueResult();

                }
                s.saveOrUpdate(obj);
            }
            s.flush();
            s.createSQLQuery("delete from ShowSchemeOrder where showScheme_key in(select showScheme_key from ShowScheme where entity_name like 'RegisterDesign.%')").executeUpdate();
            s.createSQLQuery("delete from ShowSchemeDetail where showScheme_key in(select showScheme_key from ShowScheme where entity_name like 'RegisterDesign.%')").executeUpdate();
            s.createSQLQuery("delete from ShowScheme where entity_name like 'RegisterDesign.%'").executeUpdate();
            s.createSQLQuery("delete from SysParameter where sysparameter_code='Register.Dept'").executeUpdate();
            s.flush();
            for (Object obj : showSchemes) {
                if (obj instanceof ShowScheme) {
                    ShowScheme ss = (ShowScheme) obj;
                    s.saveOrUpdate(obj);
                    s.flush();
                    for (Object ssd : ss.getShowSchemeDetails()) {
                        s.save(ssd);
                    }
                }
            }
            for (Object obj : depts) {
                SysParameter sp = (SysParameter) UtilTool.createUIDEntity(SysParameter.class);
                sp.setSysparameter_value(((DeptCode) obj).getDept_code());
                sp.setSysparameter_code("Register.Dept");
                s.save(sp);
            }
            tx.commit();
        } catch (Exception e) {
            ImplUtil.rollBack(tx);
            log.error(validateSQLResult.getMsg());
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult addPersonForNoCheck(List<String> list, RyChgLog rc) throws RemoteException {
        String ex_sql = DbUtil.getQueryForMID("update A01 set a0193=0 where a01_key in ", list);
        String ci_sql = "insert into RyChgLog(rychglog_key,a01_key,chg_ip,chg_mac,chg_user,chg_date,a0190,a0101,dept_name,chg_field,chg_type,old_dept_code,new_dept_code,beforestate,afterstate,changeScheme_key) ";
        ci_sql += "select newid(),rcl.a01_key,'" + rc.getChg_ip() + "','" + rc.getChg_mac() + "','" + rc.getChg_user() + "'," + DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd hh:mm:ss", "sqlserver") + ",rcl.a0190,rcl.a0101,rcl.dept_name,'a0193','新增'";
        ci_sql += ",rcl.old_dept_code,rcl.new_dept_code,2,0,rcl.changeScheme_key from RyChgLog rcl where  rcl.a01_key in";
        ex_sql += DbUtil.getQueryForMID(ci_sql, list);
        ex_sql += DbUtil.getQueryForMID("update Zp_resume set z_state='入职结束' where a01_key in ", list);
        ValidateSQLResult validateSQLResult = HibernateUtil.excuteSQLs(ex_sql, ";");
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult cancelRegisterFlow(List<String> a01Keys) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        Object changeNo = null;
        try {
            changeNo = s.createSQLQuery("select changeScheme_no from changescheme where changescheme_key='EmpScheme_Add'").uniqueResult();
            if (changeNo == null) {
                validateSQLResult.setResult(1);
                validateSQLResult.setMsg("人员入职模板丢失");
            } else {
                StringBuilder ex_sql = new StringBuilder();
                ex_sql.append(DbUtil.getQueryForMID("delete from WfInsLog where wfinstance_key in (select wfinstance_key from wfinstance where wf_no in(select distinct order_no from basepersonchange where changeScheme_key='EmpScheme_Add' and a01_key in ", a01Keys, "))"));
                ex_sql.append(DbUtil.getQueryForMID("delete from WfInstance where wf_no in(select distinct order_no from basepersonchange where changeScheme_key='EmpScheme_Add' and a01_key in", a01Keys, ")"));
                ex_sql.append(DbUtil.getQueryForMID("delete from A01Chg where basepersonchange_key in(select basepersonchange_key from basepersonchange where changescheme_key='EmpScheme_Add' and a01_key in ", a01Keys, ")"));
                ex_sql.append(DbUtil.getQueryForMID("delete from PersonChange_" + changeNo + " where basepersonchange_key in(select basepersonchange_key from basepersonchange where changescheme_key='EmpScheme_Add' and a01_key in", a01Keys, ")"));
                ex_sql.append(DbUtil.getQueryForMID("delete from BasePersonChange where changescheme_key='EmpScheme_Add' and a01_key in", a01Keys));
                ImplUtil.exSQLs(tx, s, validateSQLResult, ex_sql.toString(), ";", true);
            }
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
    public ValidateSQLResult setEmpDocuFilePath(String path, SysParameter sp) throws RemoteException {
        ValidateSQLResult vs = new ValidateSQLResult();
        vs.setResult(1);
        if (path == null || path.trim().equals("")) {
            vs.setMsg("路径不可为空");
        } else {
            try {
                path = path.replace("$", System.getProperty("user.dir") + "/" + "docu");
                if (!new File(path).exists()) {
                    new File(path).mkdir();
                }
                if (new File(path).isDirectory()) {
                    vs = HibernateUtil.saveOrUpdate(sp);
                } else {
                    vs.setMsg("文件目录不合法");
                }

                docu_dir = path;
            } catch (SecurityException e) {
                log.error(e);
                vs.setMsg("文件目录不合法");
            }
        }
        return vs;
    }

    @Override
    public ValidateSQLResult addAppendix(Object obj) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            BasePersonAppendix bpa = (BasePersonAppendix) obj;
            A01 a01 = (A01) session.createQuery("from A01 where a01_key='" + bpa.getA01().getA01_key() + "'").uniqueResult();
            if (a01 == null) {
                validateSQLResult.setResult(1);
                validateSQLResult.setMsg("对应人员不存在");
                return validateSQLResult;
            }
            session.createSQLQuery("update " + obj.getClass().getSimpleName() + " set last_flag='' where a01_key='" + a01.getA01_key() + "'").executeUpdate();
            session.flush();
            session.save(obj);
            session.flush();
            Hashtable<String, List<FieldDef>> triger_defs = new Hashtable<String, List<FieldDef>>();
            HibernateUtil.entity_triger(triger_defs, session, null, obj);
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
    public ValidateSQLResult outVirtualDept(List<String> a01Keys, VirtualDeptPersonLog modelLog) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            String db_type = HibernateUtil.getDb_type();
            String a01KeyStr = DbUtil.getQueryForMID("", a01Keys, "", "");
            String log_sql = "insert into VirtualDeptPersonLog(VirtualDeptPersonLog_key,remove_date,Remove_reason,deptcode_key,a01_key,person_code)"
                    + "select " + DbUtil.getUIDForDb(db_type) + "," + DateUtil.toStringForQuery(modelLog.getRemove_date(), "yyyy-MM-dd hh:mm:ss", db_type) + ",'" + modelLog.getRemove_reason() + "','" + modelLog.getDeptCode().getDeptCode_key() + "',a01_key,'" + modelLog.getPerson_code() + "' from A01 where a01_key in" + a01KeyStr;
            session.createSQLQuery(log_sql).executeUpdate();
            session.flush();
            String del_sql = "delete from VirtualDeptPerson where deptcode_key='" + modelLog.getDeptCode().getDeptCode_key() + "' and a01_key in" + a01KeyStr;
            session.createSQLQuery(del_sql).executeUpdate();
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

    @Override
    public ValidateSQLResult changePersonNo(List<String[]> nos, String no_field) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        String tableName = CountUtil.getCommTempTable();
        String db_type = HibernateUtil.getDb_type();
        String create_sql = "create table " + tableName + "(a01_key varchar(50) primary key,p_no int)";
        try {
            DbUtil.initTempTable(session, tableName, db_type);
            session.createSQLQuery(create_sql).executeUpdate();
            for (String[] data : nos) {
                session.createSQLQuery("insert into " + tableName + "(a01_key,p_no)values('" + data[0] + "'," + data[1] + ")").executeUpdate();
            }
            session.flush();
            create_sql = "update A01 set " + no_field + "=b.p_no from A01," + tableName + " b where A01.a01_key=b.a01_key";
            session.createSQLQuery(create_sql).executeUpdate();
            session.createSQLQuery("drop table " + tableName).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            log.error(validateSQLResult.getMsg());
            ImplUtil.rollBack(tx);
            try {
                session.createSQLQuery("drop table " + tableName).executeUpdate();
            } catch (Exception ex) {
            }
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public void changePersonG10(List<String[]> a01s) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            SysUtil.sortArrays(a01s);
            for (String[] data : a01s) {
                if ("".equals(data[1])) {
                    session.createSQLQuery("update A01 set g10_key = null where a01_key='" + data[0] + "'").executeUpdate();
                } else {
                    session.createSQLQuery("update A01 set g10_key='" + data[1] + "' where a01_key='" + data[0] + "'").executeUpdate();
                }
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
    }

    @Override
    public ValidateSQLResult saveEmpNoRule(AutoNoRule anr, String old_no) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            session.saveOrUpdate(anr);
            session.flush();
            if (old_no != null && !old_no.equals(anr.getAutoNoRule_id())) {
                session.createSQLQuery("delete from AutoNo where autoNo_key like '" + old_no + "%'").executeUpdate();
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
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult saveEmpReigster(Object data, List appendix, RyChgLog rcl, String entityName) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        if (!(data instanceof A01)) {
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg("传入非法数据");
            return validateSQLResult;
        }

        A01 a01 = (A01) data;
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            if (session.createSQLQuery("select 1 from A01 where a0190='" + a01.getA0190() + "' and a01_key<>'" + a01.getA01_key() + "'").list().size() > 0) {
                a01.setA0190(PersonUtil.getPersonNo(session, a01.getDeptCode(), entityName, 1));
            }
            Hashtable<String, List<FieldDef>> triger_defs = new Hashtable<String, List<FieldDef>>();
            HibernateUtil.entity_triger(triger_defs, session, null, a01);
            session.saveOrUpdate(a01);
            session.flush();
            Hashtable<String, List> appendixKeys = new Hashtable<String, List>();
            List<String> keys = new ArrayList<String>();
            for (Object obj : appendix) {
                String entity = obj.getClass().getSimpleName();
                ((BasePersonAppendix) obj).setA01(a01);
                if (!keys.contains(entity)) {
                    keys.add(entity);
                }
                List apps = appendixKeys.get(entity);
                if (apps == null) {
                    apps = new ArrayList();
                }
                apps.add(obj);
                appendixKeys.put(entity, apps);
            }
            SysUtil.sortStrList(keys);
            for (String entity : keys) {
                session.createSQLQuery("delete from " + entity + " where a01_key='" + a01.getA01_key() + "'").executeUpdate();
                List objs = appendixKeys.get(entity);
                for (Object obj : objs) {
                    session.save(obj);
                    HibernateUtil.entity_triger(triger_defs, session, null, obj);
                    session.flush();
                }
            }
            if (rcl != null) {
                session.save(rcl);
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
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult deleteEmpDocu(List<String> edKeys) throws RemoteException {
        ValidateSQLResult result = new ValidateSQLResult();
        String tableName = CountUtil.getCommTempTable();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            String db_type = HibernateUtil.getDb_type();
            String create_sql = "create table " + tableName + "(EmpDocu_key varchar(50))";
            DbUtil.initTempTable(session, tableName, db_type);
            session.createSQLQuery(create_sql).executeUpdate();
            for (String data : edKeys) {
                session.createSQLQuery("insert into " + tableName + "(EmpDocu_key)values('" + data + "')").executeUpdate();
            }
            List list = session.createSQLQuery("select File_path from EmpDocuFile where EmpDocu_key in (select EmpDocu_key from " + tableName + ")").list();
            for (Object obj : list) {
                if (obj == null || obj.toString().trim().equals("")) {
                    continue;
                }
                String path = obj.toString();
                if (path == null || path.equals("")) {
                    continue;
                }
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }
            session.createSQLQuery("delete from EmpDocuFile where EmpDocu_key in (select EmpDocu_key from " + tableName + ")").executeUpdate();
            session.createSQLQuery("delete from EmpDocu where EmpDocu_key in (select EmpDocu_key from " + tableName + ")").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            result.setResult(1);
            result.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
            try {
                session.createSQLQuery("drop table " + tableName).executeUpdate();
            } catch (Exception ex) {
            }
            log.error(e);
        } finally {
            HibernateUtil.closeSession();
        }
        return result;
    }

    @Override
    public ValidateSQLResult saveAnnexCheck(List entityList) throws RemoteException {
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        try {
            List annexList = s.createQuery("from EntityDef ed join fetch ed.entityClass "
                    + " where ed.entityClass.entityType_code = 'ANNEX' ").list();
            List noSelectedList = new ArrayList();
            for (Object obj : annexList) {
                EntityDef ed = (EntityDef) obj;
                if (!entityList.contains(ed)) {
                    noSelectedList.add(ed);
                }
            }

            s.createSQLQuery("delete system where entity_key in (select entity_key from tabname where entityClass_key in (select entityClass_key from entityClass where entitytype_code = 'ANNEX'))  and field_name = 'wf_state'").executeUpdate();
            String val = "";

            FuntionRight annexCheckFR = (FuntionRight) s.createQuery("from FuntionRight fr where fr.fun_module_flag='EmpMng.AnnexCheck'").uniqueResult();
            if (annexCheckFR == null) {
                FuntionRight fr = (FuntionRight) s.createQuery("from FuntionRight fr where fr.fun_module_flag='EmpMng'").uniqueResult();
                annexCheckFR = new FuntionRight();
                annexCheckFR.setFuntionRight_key("EmpMng.AnnexCheck");
                annexCheckFR.setFun_code(PersonUtil.getNewFuntionCode(fr.getFun_code()));
                annexCheckFR.setFun_parent_code(fr.getFun_code());
                annexCheckFR.setFun_module_flag("EmpMng.AnnexCheck");
                annexCheckFR.setFun_name("人员附表审批");
                annexCheckFR.setFun_level(fr.getFun_level());
                annexCheckFR.setGranted(true);
                s.save(annexCheckFR);
                s.flush();
            }

            for (Object obj : entityList) {
                EntityDef ed = (EntityDef) obj;
                String entity = ed.getEntityName();
                val += entity + ";";

//                //权限
//                FuntionRight fr = (FuntionRight) s.createQuery("from FuntionRight fr where fr.fun_module_flag='EmpMng.AnnexCheck_" + entity + "'").uniqueResult();
//                if (fr == null) {
//                    fr = (FuntionRight) UtilTool.createUIDEntity(FuntionRight.class);
//                    fr.setFun_code(getNewFuntionCode(annexCheckFR.getFun_code()));
//                    fr.setFun_parent_code(annexCheckFR.getFun_code());
//                    fr.setFun_module_flag("EmpMng.AnnexCheck_" + entity);
//                    fr.setFun_name(ed.getEntityCaption());
//                    fr.setFun_level(annexCheckFR.getFun_level() + 1);
//                    fr.setGranted(true);
//                    s.save(fr);
//                    s.flush();
//                }
//
//                //工作流
//                WorkFlowClass wfc = new WorkFlowClass();
//                wfc.setWorkFlowClass_key(ed.getEntity_key());
//                wfc.setWorkFlowClass_name(ed.getEntityCaption());
//                wfc.setEntity_class("org.jhrcore.entity." + entity);
//                wfc.setFuntion_key(annexCheckFR.getFuntionRight_key());
//                wfc.setForm_class("org.jhrcore.client.personnel.EmpAddAppendixPanel");
//                s.saveOrUpdate(wfc);
                //s.flush();
                String field = "wf_state";
                //重构审批状态字段
//                List list = s.createSQLQuery("select 1 from system s,tabname tn where tn.entity_key = s.entity_key "
//                        + " and tn.entityName = '" + entity + "' and s.field_name = 'check_flag' ").list();
                List list = s.createSQLQuery("select 1 from system s,tabname tn where tn.entity_key = s.entity_key "
                        + " and tn.entityName = '" + entity + "' and s.field_name = '" + field + "' ").list();
                if (list.size() > 0) {
                    continue;
                }
                String sql_check_flag = "insert into system(field_key,code_type_name,default_value,editable,editableedit,editablenew,field_caption,field_mark,field_name,field_scale,field_type,field_width,FORMAT,not_null,order_no,used_flag,view_width,visible,visibleedit,visiblenew,entity_key,PYM,field_align,save_flag,unique_flag,relation_flag,RELATION_TEXT,REGULA_MSG,regula_save_flag,REGULA_TEXT,regula_use_flag,relation_add_flag,relation_edit_flag,relation_save_flag,not_null_save_check,regula_save_check) ";
                String sql_person_code = sql_check_flag;
                sql_check_flag += " select top 1 '" + UtilTool.getUID() + "',null,'',1,0,1,'审核状态','自定义已选项','" + field + "',0,'String',20,'',0,0,1,'20',1,1,1,entity_key,'SHZT','左对齐',0,0,0,'null','null',0,null,0,0,0,0,0,0 from tabname where entityname = '"
                        + entity + "' and not exists(select 1 from system where field_name ='" + field + "' and entity_key in(select entity_key from tabname where entityname = '" + entity + "'))";
                sql_person_code += " select top 1 '" + UtilTool.getUID() + "',null,'',1,0,1,'新增人','自定义已选项','person_code',0,'String',20,'',0,1,1,'20',0,0,0,entity_key,'XZR','左对齐',0,0,0,'null','null',0,null,0,0,0,0,0,0 from tabname where entityname = '"
                        + entity + "' and not exists(select 1 from system where field_name ='person_code' and entity_key in(select entity_key from tabname where entityname = '" + entity + "'))";
                String sql2 = " if not exists(select * from syscolumns where id=object_id('" + entity + "') and name='" + field + "') "
                        + " alter table " + entity + " add " + field + " varchar(255);";
                sql2 += " if not exists(select * from syscolumns where id=object_id('" + entity + "') and name='person_code') "
                        + " alter table " + entity + " add person_code varchar(255);";
                s.createSQLQuery(sql2).executeUpdate();
                s.flush();
                sql2 = " if not exists(select * from syscolumns where id=object_id('" + entity + "') and name='person_code') "
                        + " alter table " + entity + " add person_code varchar(255);";
                s.createSQLQuery(sql2).executeUpdate();
                s.flush();
                s.createSQLQuery("update " + entity + " set " + field + "='未提交' where " + field + " is null").executeUpdate();
                s.createSQLQuery(sql_check_flag).executeUpdate();
                s.createSQLQuery(sql_person_code).executeUpdate();
            }
            List list = s.createSQLQuery("select 1 from sysParameter where sysParameter_key = 'Emp.annexCheck' ").list();
            if (list.isEmpty()) {
                s.createSQLQuery("insert into sysParameter(sysParameter_key,sysParameter_code,sysParameter_name,sysParameter_value) "
                        + " values('Emp.annexCheck','Emp.annexCheck','人员附表审批','" + val + "')  ").executeUpdate();
            } else {
                s.createSQLQuery("update sysParameter set sysParameter_value = '" + val + "' where sysParameter_key = 'Emp.annexCheck' ").executeUpdate();
            }

            for (Object obj : noSelectedList) {
                EntityDef ed = (EntityDef) obj;

                //删除权限
                s.createSQLQuery("delete roleFuntion where funtionRight_key in "
                        + " (select funtionRight_key from funtionRight where fun_module_flag = 'EmpMng.AnnexCheck_" + ed.getEntityName() + "')").executeUpdate();
                s.createSQLQuery("delete funtionRight where fun_module_flag = 'EmpMng.AnnexCheck_" + ed.getEntityName() + "'").executeUpdate();

                //删除工作流
                s.createSQLQuery("delete from wfinslog where wfinstance_key in(select wfinstance_key from wfinstance where workflowdef_key in (select workflowdef_key from workflowdef where workflowclass_key='" + ed.getEntity_key() + "'))").executeUpdate();
                s.createSQLQuery("delete from wfinstance where workflowdef_key in(select workflowdef_key from workflowdef where workflowclass_key='" + ed.getEntity_key() + "')").executeUpdate();
                s.createSQLQuery("delete from workflowa01 where workflowdef_key in (select workflowdef_key from workflowdef where workflowclass_key='" + ed.getEntity_key() + "')").executeUpdate();
                s.createSQLQuery("delete WorkFlowDef where WorkFlowClass_key = '" + ed.getEntity_key() + "'").executeUpdate();
                s.createSQLQuery("delete WorkFlowClass where WorkFlowClass_key = '" + ed.getEntity_key() + "'").executeUpdate();
                s.flush();
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
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult delAppendix(String appendixTable, List<String[]> aKeys) throws RemoteException {
        String tableName = CountUtil.getCommTempTable();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        String db_type = HibernateUtil.getDb_type();
        String create_sql = "create table " + tableName + "(t_key varchar(100),a01_key varchar(50),a_id varchar(50),last_flag varchar(10))";
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            s.createSQLQuery(create_sql).executeUpdate();
            s.flush();
            String sql = DbUtil.getInstrForMID("insert into " + tableName + "(t_key,a01_key)", aKeys, "", ";");
            sql += "delete from " + appendixTable + " where exists(select 1 from " + tableName + " b where b.t_key=" + appendixTable + ".basePersonAppendix_key);";
            ImplUtil.exSQLs(tx, s, sql, ";");
            List list = s.createSQLQuery("select basePersonAppendix_key,a01_key from " + appendixTable + " where exists(select 1 from " + tableName + " b where b.a01_key=" + appendixTable + ".a01_key) order by a01_key,a_id").list();
            int num = 1;
            List<String[]> data = new ArrayList<String[]>();
            if (list.size() > 0) {
                String a01Key = "";
                for (Object obj : list) {
                    Object[] o1 = (Object[]) obj;
                    if (!a01Key.equals(SysUtil.objToStr(o1[1]))) {
                        a01Key = SysUtil.objToStr(o1[1]);
                        num = 1;
                    } else {
                        ((String[]) data.get(data.size() - 1))[3] = "''";
                    }
                    data.add(new String[]{"'" + o1[0].toString() + "'", "'" + o1[1].toString() + "'", "'" + (num++) + "'", "'最新'"});
                }
            }
            if (!data.isEmpty()) {
                String ex_sql = "delete from " + tableName + ";";
                ex_sql += DbUtil.getInstrForMID("insert into " + tableName + "(t_key,a01_key,a_id,last_flag)", data, "", ";");
                ex_sql += "update " + appendixTable + " set a_id=b.a_id,last_flag=b.last_flag from " + tableName + " b where b.t_key=" + appendixTable + ".basePersonAppendix_key;";
                ImplUtil.exSQLs(tx, s, ex_sql, ";");
            }
            s.createSQLQuery("drop table " + tableName).executeUpdate();
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
    public ValidateSQLResult updateAppendix(String appendixTable, List<String[]> aKeys) throws RemoteException {
        String tableName = CountUtil.getCommTempTable();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        String db_type = HibernateUtil.getDb_type();
        String create_sql = "create table " + tableName + "(t_key varchar(100),a01_key varchar(50),a_id varchar(50),last_flag varchar(10))";
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            s.createSQLQuery(create_sql).executeUpdate();
            s.flush();
            String sql = DbUtil.getInstrForMID("insert into " + tableName + "(a01_key)", aKeys, "", ";");
            ImplUtil.exSQLs(tx, s, sql, ";");
            List list = s.createSQLQuery("select basePersonAppendix_key,a01_key from " + appendixTable + " where exists(select 1 from " + tableName + " b where b.a01_key=" + appendixTable + ".a01_key) order by a01_key,a_id").list();
            int num = 1;
            List<String[]> data = new ArrayList<String[]>();
            if (list.size() > 1) {
                for (int i = 0; i < list.size() - 1; i++) {
                    Object[] o1 = (Object[]) list.get(i);
                    Object[] o2 = (Object[]) list.get(i + 1);
                    if (!o1[1].toString().equals(o2[1].toString())) {
                        data.add(new String[]{"'" + o1[0].toString() + "'", "'" + o1[1].toString() + "'", "'" + num + "'", "'最新'"});
                        num = 1;
                    } else {
                        data.add(new String[]{"'" + o1[0].toString() + "'", "'" + o1[1].toString() + "'", "'" + (num++) + "'", "''"});
                    }
                }
            }
            Object[] o1 = (Object[]) list.get(list.size() - 1);
            data.add(new String[]{"'" + o1[0].toString() + "'", "'" + o1[1].toString() + "'", "'" + num + "'", "'最新'"});
            if (!data.isEmpty()) {
                String ex_sql = "delete from " + tableName + ";";
                ex_sql += DbUtil.getInstrForMID("insert into " + tableName + "(t_key,a01_key,a_id,last_flag)", data, "", ";");
                ex_sql += "update " + appendixTable + " set a_id=b.a_id,last_flag=b.last_flag from " + tableName + " b where b.t_key=" + appendixTable + ".basePersonAppendix_key;";
                ImplUtil.exSQLs(tx, s, ex_sql, ";");
            }
            s.createSQLQuery("drop table " + tableName).executeUpdate();
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
    public List getDocumentPerson(List<String> depts, Set<String> a0190s, String person_key) throws RemoteException {
        List result = new ArrayList();
        String tableName = CountUtil.getCommTempTable();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        String db_type = HibernateUtil.getDb_type();
        String create_sql = "create table " + tableName + "(a0190 varchar(255),a_id int,a01_key varchar(255),dept_code varchar(255));";
        String deptStr = "";
        for (String dept : depts) {
            deptStr += " or d.dept_code like '" + dept + "%'";
        }
        List<String[]> data = new ArrayList<String[]>();
        for (String a0190 : a0190s) {
            data.add(new String[]{"'" + a0190 + "'", "1"});
        }
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            String sql = create_sql;
            sql += DbUtil.getInstrForMID("insert into " + tableName + "(a0190,a_id)", data, "", ";");
            ImplUtil.exSQLs(tx, s, sql, ";");
            sql = "insert into " + tableName + "(a0190,a_id,a01_key,dept_code)select a.a0190,2,A01.a01_key,d.dept_code from " + tableName + " a,DeptCode d,A01 where A01.deptCode_key=d.deptCode_key"
                    + " and A01.a0190=a.a0190 and a.a_id=1 and (" + (deptStr.equals("") ? "1=1" : deptStr.substring(4)) + ")"
                    + " and (" + (person_key == null ? "1=1" : person_key + ")");
            s.createSQLQuery(sql).executeUpdate();
            s.createSQLQuery("delete from " + tableName + " where a_id=1").executeUpdate();
            sql = "select a.a0190,a.a01_key,a.dept_code,pd.a01_key + '_' + pd.docuClass_key + '_' + pd.file_name from " + tableName + " a left join personDocu pd on  pd.a01_key=a.a01_key";
            result.addAll(s.createSQLQuery(sql).list());
            s.createSQLQuery("drop table " + tableName).executeUpdate();
            tx.commit();
        } catch (Exception ex) {
            ImplUtil.rollBack(tx);
            log.error(ImplUtil.getSQLExceptionMsg(ex));
            try {
                s.createSQLQuery("drop table " + tableName).executeUpdate();
            } catch (Exception e) {
            }
        } finally {
            HibernateUtil.closeSession();
        }
        return result;
    }

    @Override
    public ValidateSQLResult uploadFile(byte[] p_byte, String file_path) throws RemoteException {
        ValidateSQLResult vs = new ValidateSQLResult();
        if (file_path == null || file_path.equals("")) {
            vs.setResult(1);
            vs.setMsg("文件路径不对");
            return vs;
        }
        file_path = file_path.replace("$", file_dir);
        File file2 = new File(file_path);
        try {
            if (!file2.getParentFile().exists()) {
                file2.getParentFile().mkdirs();
            }
            if (!file2.getParentFile().exists()) {
                vs.setResult(1);
                vs.setMsg("文件存放路径错误");
                return vs;
            }
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file2));
            output.write(p_byte);
            output.close();
        } catch (IOException ex) {
            log.error(ex);
            vs.setResult(1);
            vs.setMsg(ex.getMessage());
        }
        return vs;
    }

    @Override
    public ValidateSQLResult recoveryChange(Object change) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        if (change == null || !(change instanceof BasePersonChange)) {
            return validateSQLResult;
        }
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        BasePersonChange bpc = (BasePersonChange) change;
        String change_key = bpc.getBasePersonChange_key();
        String scheme_key = bpc.getChangescheme_key();
        try {
            ChangeScheme cs = (ChangeScheme) s.createQuery("from ChangeScheme cs left join fetch cs.changeItems left join fetch cs.changeFields where cs.changeScheme_key='" + scheme_key + "'").uniqueResult();
            if (cs == null) {
                validateSQLResult.setResult(1);
                validateSQLResult.setMsg("对应调配方案不存在");
                return validateSQLResult;
            }
            int order_no = cs.getChangeScheme_no();
            bpc = (BasePersonChange) s.createQuery("from PersonChange_" + order_no + " where basePersonChange_key='" + change_key + "'").uniqueResult();
            String sql = "update A01 set ";
            for (ChangeItem ci : cs.getChangeItems()) {
                String fieldName = ci.getFieldName();
                fieldName = SysUtil.tranField(fieldName);
                Object obj = PublicUtil.getProperty(bpc, "old_" + fieldName);
                if (fieldName.equals("deptCode")) {
                    sql += "deptCode_key=" + ((obj == null) ? null : "'" + ((DeptCode) obj).getDeptCode_key() + "'");
                } else {
                    Field field = A01.class.getField(fieldName);
                    if (field == null) {
                        continue;
                    }
                    Class field_class = field.getType();
                    String type = field_class.getSimpleName().toLowerCase();
                    if (type.equals("string")) {
                        sql += fieldName + "=" + (obj == null ? null : "'" + obj.toString().replace("'", "''") + "'");
                    } else if (type.equals("boolean")) {
                        sql += fieldName + "=" + ((obj == null) ? 0 : (((Boolean) obj) ? 1 : 0));
                    } else if (type.equals("int") || type.equals("integer") || type.equals("float") || type.equals("double") || type.equals("bigdecimal")) {
                        sql += fieldName + "=" + obj;
                    } else if (type.equals("date")) {
                        sql += fieldName + "=" + ((obj == null || !(obj instanceof Date)) ? null : DateUtil.toStringForQuery((Date) obj, "yyyy-MM-dd HH:mm:ss"));
                    }
                    if (fieldName.equals("a0191")) {
                        List list = s.createSQLQuery("select entityName,entityCaption from tabname t,entityClass ec where t.entityClass_key=ec.entityClass_key and t.entityCaption in('" + bpc.getA01().getA0191() + "','" + obj + "')").list();
                        for (Object data : list) {
                            Object[] objs = (Object[]) data;
                            if (objs[1].toString().equals(obj)) {
                                s.createSQLQuery("insert into " + objs[0] + "(a01_key)values('" + bpc.getA01().getA01_key() + "')").executeUpdate();
                            } else {
                                s.createSQLQuery("delete from " + objs[0] + " where a01_key='" + bpc.getA01().getA01_key() + "'").executeUpdate();
                            }
                        }
                    }
                }
                sql += ",";
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += " where a01_key='" + bpc.getA01().getA01_key() + "';";
            sql += "delete from A01Chg where basePersonChange_key='" + change_key + "';";
            sql += "delete from PersonChange_" + order_no + " where basePersonChange_key='" + change_key + "';";
            sql += "delete from BasePersonChange where basePersonChange_key='" + change_key + "'";
            ImplUtil.exSQLs(tx, s, sql, ";");
            tx.commit();
        } catch (Exception ex) {
            ImplUtil.rollBack(tx);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(ex));
            validateSQLResult.setResult(1);
            log.error(validateSQLResult.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    /*
     * change_style:变动类型（如临时变动:A01tempchange）
     */
    @Override
    public ValidateSQLResult recoveryChanges(List recList, String change_style) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        if (recList.isEmpty()) {
            return validateSQLResult;
        }
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            List dataList = new ArrayList();
            if ("A01tempchange".equals(change_style)) {
                StringBuilder sb_keys = new StringBuilder();
                sb_keys.append("'-1'");
                for (Object key_obj : recList) {
                    String a01_key = key_obj.toString();
                    sb_keys.append(",'").append(a01_key).append("'");
                }
                dataList = s.createQuery("from BasePersonChange bpc where bpc.basePersonChange_key in "
                        + "(select a.basePersonChange_key from A01tempchange a where a.a01_key in(" + sb_keys.toString() + "))").list();
            }
            if (dataList.isEmpty()) {
                return validateSQLResult;
            }
            HashMap<String, ChangeScheme> hm_scheme = new HashMap<String, ChangeScheme>();
            for (Object obj_bpc : dataList) {
                BasePersonChange bpc = (BasePersonChange) obj_bpc;
                String change_key = bpc.getBasePersonChange_key();
                String scheme_key = bpc.getChangescheme_key();
                ChangeScheme cs = null;
                if (hm_scheme.containsKey(scheme_key)) {
                    cs = hm_scheme.get(scheme_key);
                } else {
                    cs = (ChangeScheme) s.createQuery("from ChangeScheme cs left join fetch cs.changeItems left join fetch cs.changeFields where cs.changeScheme_key='" + scheme_key + "'").uniqueResult();
                    if (cs == null) {
                        validateSQLResult.setResult(1);
                        validateSQLResult.setMsg("对应调配方案不存在");
                        return validateSQLResult;
                    }
                    hm_scheme.put(cs.getChangeScheme_key(), cs);
                }
                int order_no = cs.getChangeScheme_no();
                bpc = (BasePersonChange) s.createQuery("from PersonChange_" + order_no + " where basePersonChange_key='" + change_key + "'").uniqueResult();
                String sql = "update A01 set ";
                for (ChangeItem ci : cs.getChangeItems()) {
                    String fieldName = ci.getFieldName();
                    fieldName = SysUtil.tranField(fieldName);
                    Object obj = PublicUtil.getProperty(bpc, "old_" + fieldName);
                    if (fieldName.equals("deptCode")) {
                        sql += "deptCode_key=" + ((obj == null) ? null : "'" + ((DeptCode) obj).getDeptCode_key() + "'");
                    } else {
                        Field field = A01.class.getField(fieldName);
                        if (field == null) {
                            continue;
                        }
                        Class field_class = field.getType();
                        String type = field_class.getSimpleName().toLowerCase();
                        if (type.equals("string")) {
                            sql += fieldName + "=" + (obj == null ? null : "'" + obj.toString().replace("'", "''") + "'");
                        } else if (type.equals("boolean")) {
                            sql += fieldName + "=" + ((obj == null) ? 0 : (((Boolean) obj) ? 1 : 0));
                        } else if (type.equals("int") || type.equals("integer") || type.equals("float") || type.equals("double") || type.equals("bigdecimal")) {
                            sql += fieldName + "=" + obj;
                        } else if (type.equals("date")) {
                            sql += fieldName + "=" + ((obj == null || !(obj instanceof Date)) ? null : DateUtil.toStringForQuery((Date) obj, "yyyy-MM-dd HH:mm:ss"));
                        }
                        if (fieldName.equals("a0191")) {
                            List list = s.createSQLQuery("select entityName,entityCaption from tabname t,entityClass ec where t.entityClass_key=ec.entityClass_key and t.entityCaption in('" + bpc.getA01().getA0191() + "','" + obj + "')").list();
                            for (Object data : list) {
                                Object[] objs = (Object[]) data;
                                if (objs[1].toString().equals(obj)) {
                                    s.createSQLQuery("insert into " + objs[0] + "(a01_key)values('" + bpc.getA01().getA01_key() + "')").executeUpdate();
                                } else {
                                    s.createSQLQuery("delete from " + objs[0] + " where a01_key='" + bpc.getA01().getA01_key() + "'").executeUpdate();
                                }
                            }
                        }
                    }
                    sql += ",";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += " where a01_key='" + bpc.getA01().getA01_key() + "';";
                sql += "delete from A01Chg where basePersonChange_key='" + change_key + "';";
                sql += "delete from PersonChange_" + order_no + " where basePersonChange_key='" + change_key + "';";
                sql += "delete from BasePersonChange where basePersonChange_key='" + change_key + "';";
                sql += "delete from A01tempchange where basePersonChange_key='" + change_key + "';";
                ImplUtil.exSQLs(tx, s, sql, ";");
                s.flush();
            }
            tx.commit();
        } catch (Exception ex) {
            ImplUtil.rollBack(tx);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(ex));
            validateSQLResult.setResult(1);
            log.error(validateSQLResult.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult delChangeScheme(List<ChangeScheme> schemes) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            String ex_sql = "";
            for (ChangeScheme cs : schemes) {
                String table = "PersonChange_" + cs.getChangeScheme_no();
                String remove_key = cs.getChangeScheme_key();
                ex_sql += "delete from changefield where changescheme_key='" + remove_key + "';";
                ex_sql += "delete from changeitem  where changescheme_key='" + remove_key + "';";
                ex_sql += "delete from changemethod  where changescheme_key='" + remove_key + "';";
                ex_sql += "delete from a01chg where basepersonchange_key in(select basepersonchange_key from basepersonchange where changescheme_key='" + remove_key + "');";
                ex_sql += "delete from basepersonchange where changescheme_key='" + remove_key + "';";
                ex_sql += FunUtil.getDelFunSQL("#" + EmpUtil.changeSchemeCode + ".scheme_" + remove_key);
                ex_sql += "delete from system where entity_key in(select entity_key from tabname where entityname='" + table + "');";
                ex_sql += "delete from roleentity where entity_key in(select entity_key from tabname where entityname='" + table + "');";
                ex_sql += "delete from tabname where entityname='" + table + "';";
                ex_sql += "delete from rolefield where field_name like '" + table + ".%';";
                if (cs.isCheck_flag()) {
                    ex_sql += "delete from wfinslog where wfinstance_key in(select wfinstance_key from wfinstance where workflowdef_key in (select workflowdef_key from workflowdef where workflowclass_key='" + remove_key + "'));";
                    ex_sql += "delete from wfinstance where workflowdef_key in(select workflowdef_key from workflowdef where workflowclass_key='" + remove_key + "');";
                    ex_sql += "delete from workflowa01 where workflowdef_key in (select workflowdef_key from workflowdef where workflowclass_key='" + remove_key + "');";
                    ex_sql += "delete from workflowdef where workflowclass_key='" + remove_key + "';";
                    ex_sql += "delete from workflowclass where workflowclass_key='" + remove_key + "';";
                }
                ex_sql += "delete from changescheme where changescheme_key='" + remove_key + "';";
            }
            ImplUtil.exSQLs(tx, s, ex_sql, ";");
            tx.commit();
            for (ChangeScheme cs : schemes) {
                try {
                    s.createSQLQuery("drop table PersonChange_" + cs.getChangeScheme_no()).executeUpdate();
                } catch (Exception ex) {
                    log.error(ex);
                    continue;
                }
            }
        } catch (Exception ex) {
            ImplUtil.rollBack(tx);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(ex));
            validateSQLResult.setResult(1);
            log.error(validateSQLResult.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult UpdateA01s(List a01s) throws RemoteException {
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        try {
            List<String> detailKeys = new ArrayList<String>();
            List<A01> fds = new ArrayList();
            for (Object obj : a01s) {
                detailKeys.add(((A01) obj).getA01_key());
                fds.add((A01) obj);
            }
            String detailKeyStr = DbUtil.getQueryForMID("", detailKeys, "", "");
            s.createSQLQuery("update A01 set a0101=a0101 where a01_key in" + detailKeyStr).executeUpdate();
            s.flush();
            SysUtil.sortListByStr(fds, "a01_key");
            for (A01 fd : fds) {
                s.update(fd);
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
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult addAppendixs(List apppendixs) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Hashtable<String, List> appendixKeys = new Hashtable<String, List>();
        Hashtable<String, List> appendixA01Keys = new Hashtable<String, List>();
        for (Object obj : apppendixs) {
            String entity = obj.getClass().getSimpleName();
            List list = appendixKeys.get(entity);
            List a01list = appendixA01Keys.get(entity);
            if (list == null) {
                a01list = new ArrayList();
                list = new ArrayList();
                appendixKeys.put(entity, list);
                appendixA01Keys.put(entity, a01list);
            }
            a01list.add(new String[]{"'" + ((BasePersonAppendix) obj).getA01().getA01_key() + "'"});
            list.add(obj);
        }
        List<String> entitys = new ArrayList();
        entitys.addAll(appendixKeys.keySet());
        SysUtil.sortStrList(entitys);
        String tableName = CountUtil.getCommTempTable();
        String db_type = HibernateUtil.getDb_type();
        String create_sql = "create table " + tableName + "(a01_key varchar(50) primary key)";
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            DbUtil.initTempTable(s, tableName, db_type);
            s.createSQLQuery(create_sql).executeUpdate();
            for (String entity : entitys) {
                String ex_sql = "delete from " + tableName + ";";
                ex_sql += DbUtil.getInstrForMID("insert into " + tableName + "(a01_key)", appendixA01Keys.get(entity), "", ";");
                ex_sql += "update " + entity + " set last_flag='' where exists(select 1 from " + tableName + " a where a.a01_key=" + entity + ".a01_key);";
                ImplUtil.exSQLs(tx, s, ex_sql, ";");
                List list = appendixKeys.get(entity);
                for (Object obj : list) {
                    s.save(obj);
                }
                s.flush();
            }
            ImplUtil.dropTempTable(s, tableName);
            tx.commit();
        } catch (Exception e) {
            ImplUtil.dropTempTable(s, tableName);
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
    public Hashtable<String, DeptCode> getEmpJdDept(String a01KeyStr) throws RemoteException {
        Hashtable<String, DeptCode> depts = new Hashtable<String, DeptCode>();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            String sql = "select a01_key,old_deptCode_key from A01jd j where j.a01_key in(" + a01KeyStr + ")";
            List list = s.createSQLQuery(sql).list();
            String deptKeyStr = "";
            Hashtable<String, String> deptKeys = new Hashtable<String, String>();
            for (Object obj : list) {
                Object[] objs = (Object[]) obj;
                if (objs[1] == null) {
                    continue;
                }
                deptKeyStr += ",'" + objs[1].toString() + "'";
                deptKeys.put(objs[1].toString(), objs[0].toString());
            }
            if (!deptKeyStr.equals("")) {
                sql = "from DeptCode d where d.deptCode_key in(" + deptKeyStr.substring(1) + ")";
                List data = s.createQuery(sql).list();
                for (Object obj : data) {
                    DeptCode dc = (DeptCode) obj;
                    String a01Key = deptKeys.get(dc.getDeptCode_key());
                    depts.put(a01Key, dc);
                }
            }
            tx.commit();
        } catch (Exception e) {
            log.error(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return depts;
    }

}
