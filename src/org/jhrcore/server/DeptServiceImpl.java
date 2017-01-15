/*      */ package org.jhrcore.server;
/*      */ 
/*      */ import java.io.PrintStream;
/*      */ import java.rmi.RemoteException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashSet;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Timer;
/*      */ import javax.ejb.Remote;
/*      */ import javax.ejb.Stateless;
/*      */ import org.apache.log4j.Logger;
/*      */ import org.hibernate.Query;
/*      */ import org.hibernate.SQLQuery;
/*      */ import org.hibernate.Session;
/*      */ import org.hibernate.Transaction;
/*      */ import org.jboss.annotation.ejb.Clustered;
/*      */ import org.jhrcore.client.UserContext;
/*      */ import org.jhrcore.entity.A01;
/*      */ import org.jhrcore.entity.BaseDeptAppendix;
/*      */ import org.jhrcore.entity.DeptChgLog;
/*      */ import org.jhrcore.entity.DeptCode;
/*      */ import org.jhrcore.entity.RyChgLog;
/*      */ import org.jhrcore.entity.SysParameter;
/*      */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*      */ import org.jhrcore.iservice.DeptService;
/*      */ import org.jhrcore.server.util.CountUtil;
/*      */ import org.jhrcore.server.util.ImplUtil;
/*      */ import org.jhrcore.util.DateUtil;
/*      */ import org.jhrcore.util.DbUtil;
/*      */ import org.jhrcore.util.SysUtil;
/*      */ import org.jhrcore.util.UtilTool;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ @Clustered
/*      */ @Stateless
/*      */ @Remote({DeptService.class})
/*      */ public class DeptServiceImpl
/*      */   extends SuperImpl
/*      */   implements DeptService
/*      */ {
/*   55 */   private static Logger log = Logger.getLogger(DeptServiceImpl.class);
/*   56 */   private static Timer time_sendData = null;
/*      */   
/*      */   public DeptServiceImpl() throws RemoteException {
/*   59 */     super(ServerApp.rmiPort);
/*      */   }
/*      */   
/*      */   public DeptServiceImpl(int port) throws RemoteException {
/*   63 */     super(port);
/*      */   }
/*      */   
/*      */   public ValidateSQLResult transDept(List<DeptCode> depts, String src_code, String dst_code, DeptChgLog d_log, Hashtable<String, String> save_codes) throws RemoteException
/*      */   {
/*   68 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*   69 */     String db_type = HibernateUtil.getDb_type();
/*   70 */     String connStr = DbUtil.getPlusStr(db_type);
/*   71 */     String subStr = DbUtil.getSubStr(db_type);
/*   72 */     Session session = HibernateUtil.currentSession();
/*   73 */     Transaction tx = session.beginTransaction();
/*   74 */     StringBuilder ex_sql = new StringBuilder();
/*      */     try {
/*   76 */       SysUtil.sortListByStr(depts, "deptCode_key");
/*   77 */       int src_len = src_code.length();
/*   78 */       for (DeptCode obj : depts) {
/*   79 */         if (obj.getParent_code().equals(dst_code)) {
/*   80 */           src_code = obj.getDept_code();
/*   81 */           src_len = src_code.length();
/*      */         }
/*   83 */         session.update(obj);
/*   84 */         session.flush();
/*      */       }
/*   86 */       d_log.setChg_date(new Date());
/*   87 */       session.save(d_log);
/*   88 */       session.flush();
/*   89 */       Object obj = session.createSQLQuery("select max(px_code) from deptcode where parent_code='" + dst_code + "' and dept_code!='" + src_code + "'").uniqueResult();
/*   90 */       int i = 0;
/*   91 */       int dst_len = dst_code.length();
/*   92 */       if (obj != null) {
/*   93 */         i = SysUtil.objToInt(obj.toString().substring(dst_len));
/*      */       }
/*   95 */       i++;
/*   96 */       String parentPx = (String)session.createSQLQuery("select px_code from deptcode where dept_code='" + dst_code + "'").uniqueResult();
/*   97 */       parentPx = parentPx + SysUtil.getNewCode(i, src_len - dst_len);
/*   98 */       ex_sql.append("update deptcode set px_code='").append(parentPx).append("'").append(connStr).append(subStr).append("(dept_code,").append(src_len + 1).append(",99) where dept_code like '").append(src_code).append("%';");
/*   99 */       String sql = "update DeptCode set end_flag = 0 where dept_code in(select d.parent_code from DeptCode d where d.dept_code like '" + dst_code + "%') and dept_code like '" + dst_code + "%';";
/*  100 */       ex_sql.append(sql);
/*  101 */       String s_comm = "and exists(select 1 from basepersonchange bpc where a01chg.basepersonchange_key = bpc.basepersonchange_key and bpc.chg_state != '审批通过');";
/*  102 */       String d_sql = "";
/*  103 */       for (String d_code : save_codes.keySet()) {
/*  104 */         d_sql = "";
/*  105 */         String new_code = (String)save_codes.get(d_code);
/*  106 */         d_sql = d_sql + "update a01chg set a_dept_code='" + new_code + "' where a_dept_code ='" + d_code + "' " + s_comm;
/*  107 */         d_sql = d_sql + "update a01chg set b_dept_code='" + new_code + "' where b_dept_code ='" + d_code + "' " + s_comm;
/*  108 */         ex_sql.append(d_sql);
/*      */       }
/*  110 */       ImplUtil.exSQLs(tx, session, ex_sql.toString(), ";");
/*  111 */       tx.commit();
/*      */     } catch (Exception e) {
/*  113 */       validateSQLResult.setResult(1);
/*  114 */       validateSQLResult.setMsg(validateSQLResult.getMsg() + "\n" + ImplUtil.getSQLExceptionMsg(e));
/*  115 */       log.error(validateSQLResult.getMsg());
/*  116 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  118 */       HibernateUtil.closeSession();
/*      */     }
/*  120 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult unitDept(List<DeptCode> depts, DeptChgLog d_log) throws RemoteException
/*      */   {
/*  125 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  126 */     Session session = HibernateUtil.currentSession();
/*  127 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  129 */       session.createSQLQuery("update DeptCode set dept_code=dept_code where deptcode_key ='" + ((DeptCode)depts.get(0)).getDeptCode_key() + "' or deptcode_key='" + ((DeptCode)depts.get(1)).getDeptCode_key() + "'").executeUpdate();
/*  130 */       DeptCode src_dept = (DeptCode)depts.get(0);
/*  131 */       DeptCode dst_dept = (DeptCode)depts.get(1);
/*  132 */       src_dept.setDel_flag(true);
/*  133 */       session.update(src_dept);
/*  134 */       session.flush();
/*  135 */       session.createSQLQuery("update a01 set deptcode_key='" + dst_dept.getDeptCode_key() + "' where deptcode_key='" + src_dept.getDeptCode_key() + "'").executeUpdate();
/*  136 */       session.createSQLQuery("update c21 set deptcode_key='" + dst_dept.getDeptCode_key() + "' where deptcode_key='" + src_dept.getDeptCode_key() + "'").executeUpdate();
/*  137 */       d_log.setChg_date(new Date());
/*  138 */       session.save(d_log);
/*  139 */       tx.commit();
/*      */     } catch (Exception e) {
/*  141 */       validateSQLResult.setResult(1);
/*  142 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  143 */       log.error(validateSQLResult.getMsg());
/*  144 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  146 */       HibernateUtil.closeSession();
/*      */     }
/*  148 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public ValidateSQLResult delDeptPhysical(String deptKeyStr, String user_code)
/*      */     throws RemoteException
/*      */   {
/*  161 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  162 */     String conn_str = DbUtil.getPlusStr(HibernateUtil.getDb_type());
/*  163 */     List<String> del_app_tables = new ArrayList();
/*  164 */     del_app_tables.add("RoleDept");
/*  165 */     del_app_tables.add("VirtualDeptPersonLog");
/*  166 */     del_app_tables.add("VirtualDeptPerson");
/*  167 */     del_app_tables.add("DeptWeave");
/*  168 */     del_app_tables.add("DeptPositionHistory");
/*  169 */     del_app_tables.add("DeptPositionWeave");
/*  170 */     del_app_tables.add("SysNotice");
/*  171 */     del_app_tables.add("PayDept");
/*  172 */     del_app_tables.add("PayA01");
/*  173 */     del_app_tables.add("HT01");
/*  174 */     del_app_tables.add("RoleDept");
/*  175 */     del_app_tables.add("In_info");
/*  176 */     del_app_tables.add("In_detail");
/*  177 */     Session session = HibernateUtil.currentSession();
/*  178 */     Transaction tx = session.beginTransaction();
/*  179 */     StringBuilder ex_sql = new StringBuilder();
/*      */     try {
/*  181 */       List appdix_list = session.createQuery("select ed.entityName from EntityDef ed where ed.entityClass.entityType_code in('DEPT','GZZE','BXHS')").list();
/*  182 */       del_app_tables.addAll(appdix_list);
/*  183 */       appdix_list = session.createSQLQuery("select distinct changescheme_no from ChangeScheme where changeScheme_key in(select changeScheme_key from changeitem where fieldName='deptCode')").list();
/*  184 */       for (Object obj : appdix_list) {
/*  185 */         ex_sql.append("delete from PersonChange_");
/*  186 */         ex_sql.append(obj.toString());
/*  187 */         ex_sql.append(" where new_deptCode_key in(");
/*  188 */         ex_sql.append(deptKeyStr);
/*  189 */         ex_sql.append(") or old_deptCode_key in(");
/*  190 */         ex_sql.append(deptKeyStr);
/*  191 */         ex_sql.append(");");
/*      */       }
/*  193 */       List gz_list = session.createQuery("select ed.entityName from EntityDef ed where ed.entityClass.entityType_code='GZZJ'").list();
/*  194 */       for (Object tmp_obj : gz_list) {
/*  195 */         String gz_name = tmp_obj.toString();
/*  196 */         String del_gz_str = "delete from " + gz_name + " where pay_key in (select pay_key from c21 where payDeptBack_key is null and deptCode_key in (" + deptKeyStr + "));";
/*  197 */         ex_sql.append(del_gz_str);
/*      */       }
/*      */       
/*  200 */       List bx_list = session.createQuery("select ed.entityName from EntityDef ed where ed.entityClass.entityType_code='BXHS'").list();
/*  201 */       for (Object tmp_obj : bx_list) {
/*  202 */         String bx_table = tmp_obj.toString();
/*  203 */         String del_str = "delete from " + bx_table + " where deptCode_key in (" + deptKeyStr + ");";
/*  204 */         ex_sql.append(del_str);
/*      */       }
/*  206 */       String update_c21_str = "update c21 set deptCode_key =NULL where payDeptBack_key is not null and deptCode_key in (" + deptKeyStr + ");";
/*  207 */       ex_sql.append(update_c21_str);
/*  208 */       del_app_tables.add("DeptCode");
/*  209 */       ex_sql.append("insert into DeptChgLog (deptchglog_key,chg_date,chg_user,chg_type,chg_before,chg_after,chg_name,chg_caption,chg_ip,chg_mac) select ");
/*  210 */       ex_sql.append(DbUtil.getUIDForDb(HibernateUtil.getDb_type()));
/*  211 */       ex_sql.append(",");
/*  212 */       ex_sql.append(DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd HH:mm:ss", HibernateUtil.getDb_type()));
/*  213 */       ex_sql.append(",'");
/*  214 */       ex_sql.append(user_code);
/*  215 */       ex_sql.append("','物理删除',content");
/*  216 */       ex_sql.append(conn_str);
/*  217 */       ex_sql.append("'{'");
/*  218 */       ex_sql.append(conn_str);
/*  219 */       ex_sql.append("dept_code");
/*  220 */       ex_sql.append(conn_str);
/*  221 */       ex_sql.append("'}','','del_flag',content");
/*  222 */       ex_sql.append(conn_str);
/*  223 */       ex_sql.append("'{'");
/*  224 */       ex_sql.append(conn_str);
/*  225 */       ex_sql.append("dept_code");
/*  226 */       ex_sql.append(conn_str);
/*  227 */       ex_sql.append("'}','");
/*  228 */       ex_sql.append(UserContext.getPerson_ip());
/*  229 */       ex_sql.append("','");
/*  230 */       ex_sql.append(UserContext.getPerson_mac());
/*  231 */       ex_sql.append("' from DeptCode where deptCode_key in(");
/*  232 */       ex_sql.append(deptKeyStr);
/*  233 */       ex_sql.append(");");
/*  234 */       for (String tableName : del_app_tables) {
/*  235 */         ex_sql.append("delete from ");
/*  236 */         ex_sql.append(tableName);
/*  237 */         ex_sql.append(" where deptCode_key in (");
/*  238 */         ex_sql.append(deptKeyStr);
/*  239 */         ex_sql.append(");");
/*      */       }
/*  241 */       ex_sql.append("update DeptCode  set end_flag=1 where end_flag = 0 and dept_code not in (select distinct parent_code from deptcode);");
/*  242 */       ImplUtil.exSQLs(tx, session, validateSQLResult, ex_sql.toString(), ";");
/*      */     } catch (Exception e) {
/*  244 */       validateSQLResult.setResult(1);
/*  245 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  246 */       log.error(validateSQLResult.getMsg());
/*  247 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  249 */       HibernateUtil.closeSession();
/*      */     }
/*  251 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult delDeptChgA01s(List depta01s) throws RemoteException
/*      */   {
/*  256 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  257 */     Session session = HibernateUtil.currentSession();
/*  258 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  260 */       DeptCode src_dept = (DeptCode)depta01s.get(0);
/*  261 */       session.createSQLQuery("update DeptCode set dept_code=dept_code where dept_code like '" + src_dept.getDept_code() + "%'").executeUpdate();
/*  262 */       depta01s.remove(0);
/*  263 */       src_dept.setDel_flag(true);
/*  264 */       session.createSQLQuery("update deptcode set del_flag=1 where dept_code like '" + src_dept.getDept_code() + "%'").executeUpdate();
/*  265 */       Hashtable<String, String> a01_dept_keys = new Hashtable();
/*  266 */       List<A01> a01s = new ArrayList();
/*  267 */       for (Object obj : depta01s) {
/*  268 */         if ((obj instanceof A01)) {
/*  269 */           a01s.add((A01)obj);
/*      */         }
/*      */       }
/*      */       
/*  273 */       SysUtil.sortListByStr(a01s, "a01_key");
/*  274 */       for (A01 a01 : a01s) {
/*  275 */         String a01_keys = (String)a01_dept_keys.get(a01.getDeptCode().getDeptCode_key());
/*  276 */         if (a01_keys == null) {
/*  277 */           a01_keys = "'" + a01.getA01_key() + "'";
/*      */         } else {
/*  279 */           a01_keys = a01_keys + ",'" + a01.getA01_key() + "'";
/*      */         }
/*  281 */         a01_dept_keys.put(a01.getDeptCode().getDeptCode_key(), a01_keys);
/*      */       }
/*  283 */       String u_a01 = "";
/*  284 */       String u_c21 = "";
/*  285 */       for (String dept_key : a01_dept_keys.keySet()) {
/*  286 */         String a01_keys = (String)a01_dept_keys.get(dept_key);
/*  287 */         u_a01 = u_a01 + "update a01 set deptcode_key='" + dept_key + "' where a01_key in(" + a01_keys + ");";
/*  288 */         u_c21 = u_c21 + "update c21 set deptcode_key='" + dept_key + "' where a01_key in(" + a01_keys + ");";
/*      */       }
/*  290 */       ImplUtil.exSQLs(tx, session, validateSQLResult, u_a01 + u_c21, ";");
/*      */     } catch (Exception e) {
/*  292 */       validateSQLResult.setResult(1);
/*  293 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  294 */       log.error(validateSQLResult.getMsg());
/*  295 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  297 */       HibernateUtil.closeSession();
/*      */     }
/*  299 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult setGrade(int grade, int old_len, int new_len, DeptChgLog dept_log) throws RemoteException
/*      */   {
/*  304 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  305 */     Session session = HibernateUtil.currentSession();
/*  306 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  308 */       SysParameter sp = (SysParameter)session.createQuery("from SysParameter sp where sp.sysParameter_key='Dept.deptgrade'").uniqueResult();
/*  309 */       String old_value = sp.getSysparameter_value();
/*  310 */       String new_value = old_value.substring(0, (grade - 1) * 2) + new_len + old_value.substring(grade * 2 - 1);
/*  311 */       String db_type = HibernateUtil.getDb_type();
/*  312 */       if (dept_log != null) {
/*  313 */         DeptChgLog dcl = dept_log;
/*  314 */         dcl.setChg_before(old_value);
/*  315 */         dcl.setChg_after(new_value);
/*  316 */         session.save(dcl);
/*  317 */         sp.setSysparameter_value(new_value);
/*  318 */         session.saveOrUpdate(sp);
/*      */       }
/*  320 */       int left_ind = 0;
/*  321 */       String[] lens = sp.getSysparameter_value().split(";");
/*  322 */       for (int i = 1; i < grade; i++) {
/*  323 */         left_ind += Integer.valueOf(lens[(i - 1)]).intValue();
/*      */       }
/*  325 */       int right_ind = left_ind + (new_len < old_len ? old_len - new_len + 1 : 1);
/*  326 */       String extra_str = "'";
/*  327 */       int tmp_len = old_len;
/*  328 */       while (tmp_len < new_len) {
/*  329 */         extra_str = extra_str + "0";
/*  330 */         tmp_len++;
/*      */       }
/*  332 */       extra_str = extra_str + "'";
/*  333 */       String sub_str = DbUtil.getSubStr(db_type);
/*  334 */       String len_str = DbUtil.getLength_strForDB(db_type);
/*  335 */       String plus_str = DbUtil.getPlusStr(db_type);
/*  336 */       String sql = "update DeptCode set parent_code=" + sub_str + "(parent_code,1," + left_ind + ")" + plus_str + extra_str + plus_str + sub_str + "(parent_code," + right_ind + "," + len_str + "(parent_code)) where grade>" + grade + ";";
/*  337 */       sql = sql + "update DeptCode set dept_code=" + sub_str + "(dept_code,1," + left_ind + ")" + plus_str + extra_str + plus_str + sub_str + "(dept_code," + right_ind + "," + len_str + "(dept_code)) where grade>=" + grade + ";";
/*  338 */       sql = sql + "update DeptCode set px_code=" + sub_str + "(px_code,1," + left_ind + ")" + plus_str + extra_str + plus_str + sub_str + "(px_code," + right_ind + "," + len_str + "(px_code)) where grade>=" + grade;
/*  339 */       ImplUtil.exSQLs(tx, session, sql, ";");
/*  340 */       tx.commit();
/*      */     } catch (Exception e) {
/*  342 */       validateSQLResult.setResult(1);
/*  343 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  344 */       log.error(e);
/*  345 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  347 */       HibernateUtil.closeSession();
/*      */     }
/*  349 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult delDept(String deptKeyStr, RyChgLog rcl) throws RemoteException
/*      */   {
/*  354 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  355 */     Session session = HibernateUtil.currentSession();
/*  356 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  358 */       Object obj = session.createQuery("from DeptCode d where d.deptCode_key='" + deptKeyStr + "'").uniqueResult();
/*  359 */       if (obj == null) {
/*  360 */         validateSQLResult.setResult(1);
/*  361 */         validateSQLResult.setMsg("目标部门不存在！");
/*  362 */         return validateSQLResult;
/*      */       }
/*  364 */       DeptCode dc = (DeptCode)obj;
/*      */       
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  370 */       String db_type = HibernateUtil.getDb_type();
/*  371 */       String log_sql = "insert into DeptChgLog(deptchglog_key,chg_date,chg_user,chg_type,chg_before,chg_after,chg_name,chg_caption,chg_ip,chg_mac)select " + DbUtil.getUIDForDb(db_type) + "," + DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd hh:mm:ss") + ",'" + rcl.getChg_user() + "'," + "'逻辑删除','0','1','del_flag',content" + DbUtil.getPlusStr(db_type) + "'{'" + DbUtil.getPlusStr(db_type) + "dept_code" + DbUtil.getPlusStr(db_type) + "'}','" + rcl.getChg_ip() + "','" + rcl.getChg_mac() + "' from DeptCode where del_flag=0 and dept_code like '" + dc.getDept_code() + "%'";
/*      */       
/*      */ 
/*  374 */       session.createSQLQuery(log_sql).executeUpdate();
/*  375 */       session.createSQLQuery("update DeptCode set del_flag=1 where dept_code like '" + dc.getDept_code() + "%'").executeUpdate();
/*  376 */       tx.commit();
/*      */     } catch (Exception e) {
/*  378 */       validateSQLResult.setResult(1);
/*  379 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  380 */       log.error(e);
/*  381 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  383 */       HibernateUtil.closeSession();
/*      */     }
/*  385 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult AddDept(DeptCode dept, RyChgLog rcl)
/*      */     throws RemoteException
/*      */   {
/*  391 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  392 */     String db_type = HibernateUtil.getDb_type();
/*  393 */     String connStr = DbUtil.getPlusStr(db_type);
/*  394 */     String subStr = DbUtil.getSubStr(db_type);
/*  395 */     Session session = HibernateUtil.currentSession();
/*  396 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  398 */       DeptCode dc = dept;
/*  399 */       String parentCode = dc.getParent_code();
/*  400 */       session.createQuery("update DeptCode set content=content where dept_code='" + parentCode + "'").executeUpdate();
/*  401 */       Object obj = null;
/*  402 */       if (!dc.getParent_code().equalsIgnoreCase("ROOT")) {
/*  403 */         obj = session.createQuery("from DeptCode d where d.dept_code='" + parentCode + "'").uniqueResult();
/*  404 */         if (obj == null) {
/*  405 */           ImplUtil.rollBack(tx);
/*  406 */           validateSQLResult.setResult(1);
/*  407 */           validateSQLResult.setMsg("目标父部门不存在！");
/*  408 */           return validateSQLResult;
/*      */         }
/*      */       }
/*  411 */       List list = session.createSQLQuery("select dept_code,px_code from deptcode where parent_code='" + parentCode + "' order by px_code").list();
/*  412 */       boolean exists = false;
/*  413 */       for (Object row : list) {
/*  414 */         Object[] objs = (Object[])row;
/*  415 */         if (dc.getPx_code().equals(objs[1].toString())) {
/*  416 */           exists = true;
/*  417 */           break;
/*      */         }
/*      */       }
/*  420 */       StringBuilder exSQL = new StringBuilder();
/*  421 */       int pLen; int nLen; int levelLen; String prePx; int num; if (exists) {
/*  422 */         pLen = parentCode.length();
/*  423 */         nLen = dc.getDept_code().length();
/*  424 */         levelLen = nLen - pLen;
/*  425 */         prePx = dc.getPx_code().substring(0, pLen);
/*  426 */         nLen++;
/*  427 */         num = SysUtil.objToInt(dc.getPx_code().substring(pLen));
/*  428 */         for (Object row : list) {
/*  429 */           Object[] objs = (Object[])row;
/*  430 */           int num1 = SysUtil.objToInt(objs[1].toString().substring(pLen));
/*  431 */           if (num1 >= num)
/*      */           {
/*      */ 
/*  434 */             num++;
/*  435 */             String newCode = SysUtil.getNewCode(num, levelLen);
/*  436 */             if (newCode.length() > levelLen) {
/*  437 */               throw new Exception("无法分配排序码");
/*      */             }
/*  439 */             exSQL.append("update deptcode set px_code='").append(prePx).append(newCode).append("'").append(connStr).append(subStr).append("(px_code,").append(nLen).append(",99) where dept_code like '").append(objs[0]).append("%';");
/*      */           }
/*      */         } }
/*  442 */       if (obj != null) {
/*  443 */         DeptCode p_dept = (DeptCode)obj;
/*  444 */         session.createSQLQuery("update DeptCode set end_flag=0 where deptcode_key='" + p_dept.getDeptCode_key() + "'").executeUpdate();
/*      */       } else {
/*  446 */         dept.setPx_code(dept.getDept_code());
/*      */       }
/*  448 */       session.save(dept);
/*  449 */       session.flush();
/*  450 */       ImplUtil.exSQLs(tx, session, exSQL.toString(), ";");
/*  451 */       DeptChgLog dcl = (DeptChgLog)UtilTool.createUIDEntity(DeptChgLog.class);
/*  452 */       dcl.setChg_user(UserContext.person_code);
/*  453 */       dcl.setChg_type("新增");
/*  454 */       dcl.setChg_date(new Date());
/*  455 */       dcl.setChg_name("dept_code");
/*  456 */       dcl.setChg_caption("部门");
/*  457 */       dcl.setChg_ip(UserContext.getPerson_ip());
/*  458 */       dcl.setChg_mac(UserContext.getPerson_mac());
/*  459 */       dcl.setChg_after(dc.getContent() + "{" + dc.getDept_code() + "}");
/*  460 */       session.save(dcl);
/*  461 */       tx.commit();
/*      */     } catch (Exception e) {
/*  463 */       validateSQLResult.setResult(1);
/*  464 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  465 */       log.error(validateSQLResult.getMsg());
/*  466 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  468 */       HibernateUtil.closeSession();
/*      */     }
/*  470 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult resumeDept(List<String> deptKeys, RyChgLog rcl)
/*      */     throws RemoteException
/*      */   {
/*  476 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  477 */     String tableName = CountUtil.getCommTempTable();
/*  478 */     Session session = HibernateUtil.currentSession();
/*  479 */     Transaction tx = session.beginTransaction();
/*  480 */     String db_type = HibernateUtil.getDb_type();
/*      */     try {
/*  482 */       DbUtil.initTempTable(session, tableName, db_type);
/*  483 */       String sql = "";
/*  484 */       if (db_type.equals("oracle")) {
/*  485 */         sql = "create table " + tableName + "(deptcode_key varchar2(50))";
/*      */       } else {
/*  487 */         sql = "create table " + tableName + "(deptcode_key varchar(50))";
/*      */       }
/*  489 */       session.createSQLQuery(sql).executeUpdate();
/*  490 */       for (String key : deptKeys) {
/*  491 */         session.createSQLQuery("insert into " + tableName + "(deptcode_key)values('" + key + "')").executeUpdate();
/*      */       }
/*  493 */       session.flush();
/*  494 */       String s_where = "deptcode_key in(select deptcode_key from " + tableName + ")";
/*  495 */       session.createSQLQuery("update DeptCode set del_flag=0 where " + s_where).executeUpdate();
/*  496 */       String log_sql = "insert into DeptChgLog(deptchglog_key,chg_date,chg_user,chg_type,chg_before,chg_after,chg_name,chg_caption,chg_ip,chg_mac)select " + DbUtil.getUIDForDb(db_type) + "," + DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd hh:mm:ss") + ",'" + rcl.getChg_user() + "'," + "'逻辑恢复','1','0','del_flag',content" + DbUtil.getPlusStr(db_type) + "'{'" + DbUtil.getPlusStr(db_type) + "dept_code" + DbUtil.getPlusStr(db_type) + "'}','" + rcl.getChg_ip() + "','" + rcl.getChg_mac() + "' from DeptCode where " + s_where;
/*      */       
/*      */ 
/*      */ 
/*  500 */       session.createSQLQuery(log_sql).executeUpdate();
/*  501 */       session.createSQLQuery("drop table " + tableName).executeUpdate();
/*  502 */       tx.commit();
/*      */     } catch (Exception e) {
/*  504 */       validateSQLResult.setResult(1);
/*  505 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  506 */       log.error(e);
/*  507 */       ImplUtil.rollBack(tx);
/*  508 */       ImplUtil.dropTempTable(session, tableName);
/*      */     } finally {
/*  510 */       HibernateUtil.closeSession();
/*      */     }
/*  512 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult editDept(Object dept, String edit_code) throws RemoteException
/*      */   {
/*  517 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  518 */     Session session = HibernateUtil.currentSession();
/*  519 */     Transaction tx = session.beginTransaction();
/*  520 */     long time1 = System.currentTimeMillis();
/*      */     try {
/*  522 */       DeptCode dc = (DeptCode)dept;
/*  523 */       Object[] old_dept = (Object[])session.createSQLQuery("select dc.dept_code,dc.dept_full_name from DeptCode dc where dc.deptCode_key='" + dc.getDeptCode_key() + "'").uniqueResult();
/*  524 */       String old_code = old_dept[0].toString();
/*  525 */       String new_code = dc.getDept_code();
/*  526 */       String old_full_name = old_dept[1].toString();
/*  527 */       String new_full_name = dc.getDept_full_name();
/*  528 */       session.update(dept);
/*  529 */       session.flush();
/*  530 */       HibernateUtil.entity_triger(new Hashtable(), session, null, dept);
/*  531 */       String sql = "";
/*  532 */       String db_type = HibernateUtil.getDb_type();
/*  533 */       String subStr = DbUtil.getSubStr(db_type);
/*  534 */       String connStr = DbUtil.getPlusStr(db_type);
/*  535 */       String lenStr = DbUtil.getLength_strForDB(db_type);
/*  536 */       if ((edit_code.contains("content;")) && 
/*  537 */         (new_full_name != null) && (old_full_name != null)) {
/*  538 */         int full_name_len = old_full_name.length() + 1;
/*  539 */         String code = "'" + new_full_name + "'" + connStr + subStr + "(dept_full_name," + full_name_len + "," + lenStr + "(dept_full_name))";
/*  540 */         sql = sql + "update deptcode set dept_full_name=" + code + " where dept_code like '" + old_code + "%' and dept_code!='" + old_code + "' and " + lenStr + "(dept_full_name)>" + full_name_len + ";";
/*      */       }
/*      */       
/*      */ 
/*  544 */       if (edit_code.contains("dept_code;")) {
/*  545 */         int code_len = old_code.length() + 1;
/*  546 */         String code = "'" + new_code + "'" + connStr + subStr + "(dept_code," + code_len + "," + lenStr + "(dept_code))";
/*  547 */         sql = sql + "update deptcode set dept_code=" + code + ",parent_code=" + code.replace("dept_code", "parent_code") + " where dept_code like '" + old_code + "%' and dept_code!='" + old_code + "';";
/*      */       }
/*      */       
/*  550 */       ImplUtil.exSQLs(tx, session, sql, ";");
/*  551 */       tx.commit();
/*      */     } catch (Exception e) {
/*  553 */       validateSQLResult.setResult(1);
/*  554 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  555 */       log.error(e);
/*  556 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  558 */       HibernateUtil.closeSession();
/*      */     }
/*  560 */     long time2 = System.currentTimeMillis();
/*  561 */     System.out.println(time2 - time1 + ";" + time1 + ";" + time2);
/*  562 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult changeDeptFullName(List<String[]> save_dept) throws RemoteException
/*      */   {
/*  567 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  568 */     String tableName = CountUtil.getCommTempTable();
/*  569 */     Session session = HibernateUtil.currentSession();
/*  570 */     Transaction tx = session.beginTransaction();
/*  571 */     String db_type = HibernateUtil.getDb_type();
/*      */     try {
/*  573 */       String ext_sql = DbUtil.isTableExistSQL(tableName, db_type);
/*  574 */       if (Integer.valueOf(session.createSQLQuery(ext_sql).list().get(0).toString()).intValue() != 0) {
/*  575 */         session.createSQLQuery("drop table " + tableName).executeUpdate();
/*      */       }
/*  577 */       String sql = "";
/*  578 */       if (db_type.equals("oracle")) {
/*  579 */         sql = "create table " + tableName + "(deptcode_key varchar2(50) primary key,full_name varchar2(4000))";
/*      */       } else {
/*  581 */         sql = "create table " + tableName + "(deptcode_key varchar(50) primary key,full_name varchar(4000))";
/*      */       }
/*  583 */       session.createSQLQuery(sql).executeUpdate();
/*  584 */       for (String[] key : save_dept) {
/*  585 */         session.createSQLQuery("insert into " + tableName + "(deptcode_key,full_name)values('" + key[0] + "','" + key[1] + "')").executeUpdate();
/*      */       }
/*  587 */       session.flush();
/*  588 */       if (db_type.equals("sqlserver")) {
/*  589 */         sql = "update deptcode set dept_full_name=b.full_name from deptcode d," + tableName + " b where d.deptcode_key=b.deptcode_key";
/*      */       } else {
/*  591 */         sql = "update deptcode d set dept_full_name=(select full_name from " + tableName + " b where b.deptcode_key=d.deptcode_key) where exists(select 1 from " + tableName + " b where b.deptcode_key=d.deptcode_key)";
/*      */       }
/*  593 */       session.createSQLQuery(sql).executeUpdate();
/*  594 */       session.createSQLQuery("drop table " + tableName).executeUpdate();
/*  595 */       tx.commit();
/*      */     } catch (Exception ex) {
/*  597 */       validateSQLResult.setResult(1);
/*  598 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(ex));
/*  599 */       log.error(validateSQLResult.getMsg());
/*  600 */       ImplUtil.rollBack(tx);
/*      */       try {
/*  602 */         session.createSQLQuery("drop table " + tableName).executeUpdate();
/*      */       }
/*      */       catch (Exception e) {}
/*      */     } finally {
/*  606 */       HibernateUtil.closeSession();
/*      */     }
/*  608 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult addDeptAppendix(Object obj) throws RemoteException
/*      */   {
/*  613 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  614 */     Session session = HibernateUtil.currentSession();
/*  615 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  617 */       String tableName = obj.getClass().getSimpleName();
/*  618 */       BaseDeptAppendix bda = (BaseDeptAppendix)obj;
/*  619 */       session.createSQLQuery("update " + tableName + " set last_flag='' where deptCode_key='" + bda.getDeptCode().getDeptCode_key() + "'").executeUpdate();
/*  620 */       session.flush();
/*  621 */       session.saveOrUpdate(obj);
/*  622 */       tx.commit();
/*      */     } catch (Exception ex) {
/*  624 */       validateSQLResult.setResult(1);
/*  625 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(ex));
/*  626 */       log.error(validateSQLResult.getMsg());
/*  627 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  629 */       HibernateUtil.closeSession();
/*      */     }
/*  631 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public ValidateSQLResult copyG10(Hashtable hash, int copy)
/*      */     throws RemoteException
/*      */   {
/*  729 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  730 */     Session session = HibernateUtil.currentSession();
/*  731 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  733 */       List g10list = new ArrayList();
/*  734 */       Enumeration en = hash.keys();
/*      */       
/*  736 */       System.out.println("copy:" + copy);
/*  737 */       if (copy == 0) {
/*  738 */         List dest_app_list = new ArrayList();
/*  739 */         String sql = " select b.entityname ";
/*  740 */         sql = sql + " from entityclass a,tabname b ";
/*  741 */         sql = sql + " where a.entityclass_key = b.entityclass_key and a.entitytype_code = 'GWZJ' ";
/*  742 */         List list = session.createSQLQuery(sql).list();
/*  743 */         session.flush();
/*  744 */         en = hash.keys();
/*  745 */         Object srcg_key; Object obj2; while (en.hasMoreElements()) {
/*  746 */           srcg_key = en.nextElement();
/*  747 */           for (Object obj : list) {
/*  748 */             System.out.println("obj:" + obj);
/*  749 */             if (obj != null)
/*      */             {
/*      */ 
/*  752 */               String g10_ap = SysUtil.objToStr(obj, "");
/*  753 */               if (g10_ap.trim().length() != 0)
/*      */               {
/*      */ 
/*  756 */                 List p_a_list = session.createQuery("from " + g10_ap + " g join fetch g.g10 where g.g10.g10_key = '" + srcg_key + "' ").list();
/*  757 */                 for (Iterator i$ = p_a_list.iterator(); i$.hasNext(); obj2 = i$.next()) {}
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*  762 */         for (Object obj : dest_app_list) {
/*  763 */           session.save(obj);
/*  764 */           session.flush();
/*      */         }
/*      */       }
/*  767 */       tx.commit();
/*      */     } catch (Exception e) {
/*  769 */       validateSQLResult.setResult(1);
/*  770 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  771 */       log.error(validateSQLResult.getMsg());
/*  772 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  774 */       HibernateUtil.closeSession();
/*      */     }
/*  776 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult delG10(String key, String type) throws RemoteException
/*      */   {
/*  781 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  782 */     Session session = HibernateUtil.currentSession();
/*  783 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  785 */       String s_where = "g.g10_key in(select g10_key from g10 where deptcode_key='" + key + "')";
/*  786 */       SQLQuery query = session.createSQLQuery("select 1 from A01,g10 g where A01.g10_key=g.g10_key and " + s_where);
/*  787 */       query.setMaxResults(1);
/*  788 */       if (query.list().size() > 0) {
/*  789 */         throw new Exception("当前岗位下有人员，不允许删除");
/*      */       }
/*  791 */       String del_sql = "update A01 set g10_key=null where " + s_where.replace("g.", "") + ";";
/*  792 */       List list = session.createSQLQuery("select cs.changescheme_no from changescheme cs,changeitem ci where ci.changescheme_key=cs.changescheme_key and ci.fieldname='g10'").list();
/*  793 */       for (Object obj : list) {
/*  794 */         del_sql = del_sql + "update PersonChange_" + obj + " set old_g10_key=null where " + s_where.replace("g.g10_key", "old_g10_key");
/*  795 */         del_sql = del_sql + "update PersonChange_" + obj + " set new_g10_key=null where " + s_where.replace("g.g10_key", "new_g10_key");
/*      */       }
/*  797 */       List appendixs = session.createSQLQuery("select entityname from tabname t,entityclass ec where t.entityclass_key=ec.entityclass_key and ec.entitytype_code='GWZJ'").list();
/*  798 */       appendixs.add("DeptPositionHistory");
/*  799 */       appendixs.add("DeptPositionWeave");
/*  800 */       appendixs.add("G10");
/*  801 */       for (Object obj : appendixs) {
/*  802 */         del_sql = del_sql + "delete from " + obj + " where " + s_where.replace("g.", "") + ";";
/*      */       }
/*  804 */       ImplUtil.exSQLs(tx, session, del_sql, ";");
/*  805 */       tx.commit();
/*      */     } catch (Exception e) {
/*  807 */       ImplUtil.rollBack(tx);
/*  808 */       validateSQLResult.setResult(1);
/*  809 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  810 */       log.error(validateSQLResult.getMsg());
/*      */     } finally {
/*  812 */       HibernateUtil.closeSession();
/*      */     }
/*  814 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveDeptSort(String parent_code, List<String[]> childdepts) throws RemoteException
/*      */   {
/*  819 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  820 */     String db_type = HibernateUtil.getDb_type();
/*  821 */     String connStr = DbUtil.getPlusStr(db_type);
/*  822 */     String subStr = DbUtil.getSubStr(db_type);
/*  823 */     Session session = HibernateUtil.currentSession();
/*  824 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  826 */       if ((parent_code == null) || (parent_code.trim().equals(""))) {
/*  827 */         throw new Exception("非法数据传递");
/*      */       }
/*  829 */       List list = session.createSQLQuery("select dept_code,del_flag from deptcode where parent_code='" + parent_code + "' and dept_code !='" + parent_code + "' order by del_flag,dept_code").list();
/*  830 */       String child_code = SysUtil.objToStr(((Object[])(Object[])list.get(0))[0]);
/*  831 */       int levelLen = child_code.length();
/*  832 */       int parentLen = parent_code.length();
/*  833 */       if (levelLen <= parentLen) {
/*  834 */         throw new Exception("数据存在异常，子级代码[" + child_code + "]长度小于父代码[" + parent_code + "]");
/*      */       }
/*  836 */       int childdeptsize = 0;
/*  837 */       List<String> deptCodes = new ArrayList();
/*  838 */       for (Object obj : list) {
/*  839 */         Object[] objs = (Object[])obj;
/*  840 */         if (!objs[1].toString().equals("1"))
/*      */         {
/*      */ 
/*  843 */           deptCodes.add(objs[0].toString());
/*  844 */           childdeptsize++;
/*      */         } }
/*  846 */       if (childdeptsize != childdepts.size()) {
/*  847 */         throw new Exception("您可能没有当前部门的所有权限，不允许保存排序");
/*      */       }
/*  849 */       int i = 1;
/*  850 */       int childLen = levelLen - parentLen;
/*  851 */       Hashtable<String, String> pxCodeKeys = new Hashtable();
/*  852 */       list = session.createSQLQuery("select px_code from deptcode where dept_code='" + parent_code + "'").list();
/*  853 */       if (list.isEmpty()) {
/*  854 */         throw new Exception("当前部门数据丢失");
/*      */       }
/*  856 */       if (list.get(0) == null) {
/*  857 */         throw new Exception("当前部门排序数据异常，建议先使用还原所有部门排序功能");
/*      */       }
/*  859 */       parent_code = list.get(0).toString();
/*  860 */       if (((String[])childdepts.get(0))[1].equals("")) {
/*  861 */         for (String[] dept_code : childdepts) {
/*  862 */           pxCodeKeys.put(dept_code[0], parent_code + SysUtil.getNewCode(i, childLen));
/*  863 */           deptCodes.remove(dept_code[0]);
/*  864 */           i++;
/*      */         }
/*      */       } else {
/*  867 */         for (String[] dept_code : childdepts) {
/*  868 */           pxCodeKeys.put(dept_code[0], dept_code[1]);
/*  869 */           deptCodes.remove(dept_code[0]);
/*      */         }
/*      */         
/*  872 */         i = SysUtil.objToInt(((String[])childdepts.get(childdeptsize - 1))[1].substring(parentLen));
/*  873 */         i++;
/*      */       }
/*  875 */       for (String dept_code : deptCodes) {
/*  876 */         pxCodeKeys.put(dept_code, parent_code + SysUtil.getNewCode(i, childLen));
/*  877 */         i++;
/*      */       }
/*  879 */       StringBuilder exSQL = new StringBuilder();
/*  880 */       levelLen++;
/*  881 */       for (String dept_code : pxCodeKeys.keySet()) {
/*  882 */         String pxCode = (String)pxCodeKeys.get(dept_code);
/*  883 */         exSQL.append("update deptcode set px_code=dept_code where dept_code like '").append(dept_code).append("%' and px_code is null;");
/*  884 */         exSQL.append("update deptcode set px_code='").append(pxCode).append("'").append(connStr).append(subStr).append("(px_code,").append(levelLen).append(",99) where dept_code like '").append(dept_code).append("%';");
/*      */       }
/*  886 */       ImplUtil.exSQLs(tx, session, exSQL.toString(), ";");
/*  887 */       tx.commit();
/*      */     } catch (Exception e) {
/*  889 */       ImplUtil.rollBack(tx);
/*  890 */       validateSQLResult.setResult(1);
/*  891 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  892 */       log.error(validateSQLResult.getMsg());
/*      */     } finally {
/*  894 */       HibernateUtil.closeSession();
/*      */     }
/*  896 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult recoveryDeptSort(String parent_code, String method) throws RemoteException
/*      */   {
/*  901 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  902 */     String db_type = HibernateUtil.getDb_type();
/*  903 */     String connStr = DbUtil.getPlusStr(db_type);
/*  904 */     String subStr = DbUtil.getSubStr(db_type);
/*  905 */     Session session = HibernateUtil.currentSession();
/*  906 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  908 */       if (parent_code == null) {
/*  909 */         throw new Exception("非法数据传递");
/*      */       }
/*  911 */       StringBuilder exSQL = new StringBuilder();
/*  912 */       if ("all".equals(method)) {
/*  913 */         exSQL.append("update deptcode set px_code=dept_code");
/*      */       } else {
/*  915 */         List list = session.createSQLQuery("select px_code from deptcode where dept_code='" + parent_code + "'").list();
/*  916 */         if (list.isEmpty()) {
/*  917 */           throw new Exception("当前部门数据丢失");
/*      */         }
/*  919 */         if (list.get(0) == null) {
/*  920 */           exSQL.append("update deptcode set px_code=dept_code where dept_code like '").append(parent_code).append("%'");
/*      */         } else {
/*  922 */           String parentPx = list.get(0).toString();
/*  923 */           int pxLen = parentPx.length();
/*  924 */           if (pxLen != parent_code.length()) {
/*  925 */             throw new Exception("当前部门排序码异常，请还原所有部门默认排序");
/*      */           }
/*  927 */           pxLen++;
/*  928 */           if ("curAll".equals(method)) {
/*  929 */             exSQL.append("update deptcode set px_code='").append(parentPx).append("'").append(connStr).append(subStr).append("(dept_code,").append(pxLen).append(",99) where parent_code like '").append(parent_code).append("%';");
/*      */           } else {
/*  931 */             list = session.createSQLQuery("select dept_code from deptcode where parent_code='" + parent_code + "'").list();
/*  932 */             if (list.isEmpty()) {
/*  933 */               throw new Exception("当前部门下无子部门");
/*      */             }
/*  935 */             exSQL.append("update deptcode set px_code='").append(parentPx).append("'").append(connStr).append(subStr).append("(dept_code,").append(pxLen).append(",99) where parent_code='").append(parent_code).append("';");
/*  936 */             int cLen = list.get(0).toString().length() + 1;
/*  937 */             exSQL.append("update deptcode set px_code='").append(parentPx).append("'").append(connStr).append(subStr).append("(dept_code,").append(pxLen).append(",").append(cLen - pxLen).append(")").append(connStr).append(subStr).append("(px_code,").append(cLen).append(",99) where parent_code like '").append(parent_code).append("%' and parent_code!='").append(parent_code).append("';");
/*      */           }
/*      */         }
/*      */       }
/*      */       
/*  942 */       ImplUtil.exSQLs(tx, session, exSQL.toString(), ";");
/*  943 */       tx.commit();
/*      */     } catch (Exception e) {
/*  945 */       ImplUtil.rollBack(tx);
/*  946 */       validateSQLResult.setResult(1);
/*  947 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  948 */       log.error(validateSQLResult.getMsg());
/*      */     } finally {
/*  950 */       HibernateUtil.closeSession();
/*      */     }
/*  952 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult importDept(List saveDepts, String sqls) throws RemoteException
/*      */   {
/*  957 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  958 */     String db_type = HibernateUtil.getDb_type();
/*  959 */     String val = db_type.equals("oracle") ? "varchar2(255)" : "varchar(255)";
/*  960 */     String tableName = CountUtil.getCommTempTable();
/*  961 */     Session session = HibernateUtil.currentSession();
/*  962 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  964 */       ImplUtil.exSQLs(tx, session, sqls, ";");
/*  965 */       HashSet<String> parentCodes = new HashSet();
/*  966 */       for (Object obj : saveDepts) {
/*  967 */         DeptCode dc = (DeptCode)obj;
/*  968 */         parentCodes.add(dc.getParent_code()); }
/*      */       Hashtable<String, String> pxCodeKeys;
/*  970 */       Hashtable<String, Integer> pxInitKeys; if (parentCodes.size() > 0) {
/*  971 */         DbUtil.initTempTable(session, tableName, db_type);
/*  972 */         List<String[]> parentData = new ArrayList();
/*  973 */         for (String p : parentCodes) {
/*  974 */           parentData.add(new String[] { "'" + p + "'" });
/*      */         }
/*  976 */         String sql = "create table " + tableName + "(t_id " + val + ");";
/*  977 */         sql = sql + DbUtil.getInstrForMID(new StringBuilder().append("insert into ").append(tableName).append("(t_id)").toString(), parentData, "", ";");
/*  978 */         ImplUtil.exSQLs(tx, session, sql, ";");
/*  979 */         List list = session.createSQLQuery("select t_id,max(px_code) from deptcode d," + tableName + " t where d.parent_code=t.t_id group by t.t_id").list();
/*  980 */         pxCodeKeys = new Hashtable();
/*  981 */         pxInitKeys = new Hashtable();
/*  982 */         for (String key : parentCodes) {
/*  983 */           String pxCode = key;
/*  984 */           int i = 0;
/*  985 */           for (Object obj : list) {
/*  986 */             Object[] objs = (Object[])obj;
/*  987 */             if (key.equals(objs[0].toString())) {
/*  988 */               pxCode = objs[1].toString().substring(0, key.length());
/*  989 */               i = SysUtil.objToInt(objs[1].toString().substring(key.length()));
/*  990 */               break;
/*      */             }
/*      */           }
/*  993 */           i++;
/*  994 */           pxInitKeys.put(key, Integer.valueOf(i));
/*  995 */           pxCodeKeys.put(key, pxCode);
/*      */         }
/*  997 */         SysUtil.sortListByStr(saveDepts, "dept_code");
/*  998 */         for (Object obj : saveDepts) {
/*  999 */           DeptCode dc = (DeptCode)obj;
/* 1000 */           String parentCode = dc.getParent_code();
/* 1001 */           int initNum = ((Integer)pxInitKeys.get(parentCode)).intValue();
/* 1002 */           String parentPx = (String)pxCodeKeys.get(parentCode);
/* 1003 */           parentPx = parentPx + SysUtil.getNewCode(initNum, dc.getDept_code().length() - parentCode.length());
/* 1004 */           pxInitKeys.put(parentCode, Integer.valueOf(initNum + 1));
/* 1005 */           dc.setPx_code(parentPx);
/* 1006 */           session.save(obj);
/* 1007 */           session.flush();
/*      */         }
/*      */       }
/* 1010 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1012 */       ImplUtil.rollBack(tx);
/* 1013 */       validateSQLResult.setResult(1);
/* 1014 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1015 */       log.error(validateSQLResult.getMsg());
/*      */     } finally {
/* 1017 */       HibernateUtil.closeSession();
/*      */     }
/* 1019 */     return validateSQLResult;
/*      */   }
/*      */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\DeptServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */