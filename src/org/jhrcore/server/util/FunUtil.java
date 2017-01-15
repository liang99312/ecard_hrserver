/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import org.hibernate.Query;
/*     */ import org.hibernate.SQLQuery;
/*     */ import org.hibernate.Session;
/*     */ import org.hibernate.Transaction;
/*     */ import org.jhrcore.client.UserContext;
/*     */ import org.jhrcore.entity.SysParameter;
/*     */ import org.jhrcore.entity.change.ChangeScheme;
/*     */ import org.jhrcore.entity.right.FuntionRight;
/*     */ import org.jhrcore.mutil.EmpUtil;
/*     */ import org.jhrcore.server.HibernateUtil;
/*     */ import org.jhrcore.util.DbUtil;
/*     */ import org.jhrcore.util.SysUtil;
/*     */ import org.jhrcore.util.UtilTool;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class FunUtil
/*     */ {
/*     */   public static void rebuildFun()
/*     */   {
/*  29 */     Object updateTable = HibernateUtil.fetchEntityBy("select sysparameter_value from SysParameter where sysParameter_key='HRTableUpdate'");
/*  30 */     boolean updateable = "1".equals(updateTable);
/*  31 */     String db_type = HibernateUtil.getDb_type();
/*  32 */     String plus = DbUtil.getPlusStr(db_type);
/*  33 */     if (updateable) {
/*  34 */       Session s = HibernateUtil.currentSession();
/*  35 */       Transaction tx = s.beginTransaction();
/*     */       try {
/*  37 */         List list = s.createQuery("from ChangeScheme").list();
/*  38 */         for (Object obj : list) {
/*  39 */           ChangeScheme cs = (ChangeScheme)obj;
/*  40 */           String schemeKey = cs.getChangeScheme_key();
/*  41 */           String key = EmpUtil.changeSchemeCode;
/*  42 */           rebuildFun(s, "-1", key, key + ".scheme_" + schemeKey, cs.getChangeScheme_name());
/*  43 */           rebuildFun(s, "-1", key + ".scheme_" + schemeKey, key + ".scheme_" + schemeKey + "_view", "查看");
/*  44 */           rebuildFun(s, "-1", key + ".scheme_" + schemeKey, key + ".scheme_" + schemeKey + "_use", "执行");
/*     */         }
/*  46 */         tx.commit();
/*     */       } catch (Exception ex) {
/*  48 */         ImplUtil.rollBack(tx);
/*     */       } finally {
/*  50 */         HibernateUtil.closeSession();
/*     */       }
/*  52 */       String sql = getDelSQL(DbUtil.getNullSQL("fun_module_flag"));
/*     */       
/*  54 */       HibernateUtil.excuteSQLs(sql, ";");
/*     */     }
/*  56 */     List list = HibernateUtil.fetchEntitiesBy("select fun_module_flag from FuntionRight where fun_module_flag!='ROOT' and " + DbUtil.getLength_strForDB(db_type) + "(fun_code)<=3");
/*  57 */     List<String> modules = new ArrayList();
/*  58 */     StringBuilder ex_str = new StringBuilder();
/*  59 */     ex_str.append("update funtionright set granted=1;");
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*  73 */     for (String module : modules) {
/*  74 */       if ((!module.equalsIgnoreCase("ROOT")) && 
/*     */       
/*     */ 
/*  77 */         (!list.contains(module)))
/*     */       {
/*     */ 
/*  80 */         ex_str.append(getUpdateSQL(module));
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*  89 */     ex_str.append(getUpdateSQL("SysIndex"));
/*  90 */     ex_str.append(getUpdateSQL("Service"));
/*  91 */     list.add("Service");
/*  92 */     for (Object obj : list) {
/*  93 */       ex_str.append("update FuntionRight set granted=1 where fun_module_flag='").append(obj).append("' and exists(select 1 from FuntionRight where fun_module_flag like '").append(obj).append("%' and granted=1);");
/*     */     }
/*  95 */     ex_str.append("update moduleinfo set used=1;");
/*  96 */     ex_str.append("update moduleinfo set used=1 where exists(select 1 from FuntionRight fr where fr.fun_module_flag=moduleinfo.module_code and fr.granted=1);");
/*  97 */     ex_str.append("update moduleinfo set used=1 where module_code='ZHTJ' or module_code='XTYW' or module_code='GW'");
/*  98 */     HibernateUtil.excuteSQLs(ex_str.toString(), ";");
/*  99 */     buildAppendixWorkFlow();
/*     */   }
/*     */   
/*     */   private static void buildAppendixWorkFlow()
/*     */   {
/* 104 */     String val = "";
/* 105 */     SysParameter sp = (SysParameter)HibernateUtil.fetchEntityBy("from SysParameter sp where sp.sysParameter_key = 'Emp.annexCheck' ");
/* 106 */     if ((sp != null) && (sp.getSysparameter_value() != null)) {
/* 107 */       val = sp.getSysparameter_value();
/*     */     }
/* 109 */     if (val.trim().equals("")) {
/* 110 */       return;
/*     */     }
/* 112 */     String dbType = HibernateUtil.getDb_type();
/* 113 */     String var = dbType.equals("sqlserver") ? "varchar(255)" : "varchar2(255)";
/* 114 */     List entitys = Arrays.asList(val.split(";"));
/* 115 */     for (Object entity : entitys) {
/* 116 */       String sql = DbUtil.isFieldExistSQL(entity.toString(), "check_flag", HibernateUtil.getDb_type());
/* 117 */       List list = HibernateUtil.selectSQL(sql, false, 1);
/* 118 */       if ((!list.isEmpty()) && (!list.get(0).toString().equals("0")))
/*     */       {
/*     */ 
/* 121 */         sql = DbUtil.isFieldExistSQL(entity.toString(), "wf_state", dbType);
/* 122 */         list = HibernateUtil.selectSQL(sql, false, 1);
/* 123 */         sql = "";
/* 124 */         if ((list.isEmpty()) || (list.get(0).toString().equals("0"))) {
/* 125 */           sql = sql + " alter table " + entity.toString() + " add wf_state " + var + ";";
/*     */         }
/* 127 */         sql = sql + "update  " + entity.toString() + " set wf_state=check_flag;";
/* 128 */         sql = sql + "alter table " + entity.toString() + " drop column check_flag;";
/* 129 */         sql = sql + "update  " + entity.toString() + " set wf_state='未提交' where wf_state is null;";
/* 130 */         HibernateUtil.excuteSQLs(sql, ";");
/* 131 */         String field = "wf_state";
/* 132 */         String sql_check_flag = "insert into system(field_key,code_type_name,default_value,editable,editableedit,editablenew,field_caption,field_mark,field_name,field_scale,field_type,field_width,FORMAT,not_null,order_no,used_flag,view_width,visible,visibleedit,visiblenew,entity_key,PYM,field_align,save_flag,unique_flag,relation_flag,RELATION_TEXT,REGULA_MSG,regula_save_flag,REGULA_TEXT,regula_use_flag,relation_add_flag,relation_edit_flag,relation_save_flag,not_null_save_check,regula_save_check) ";
/* 133 */         if (dbType.equals("sqlserver")) {
/* 134 */           sql_check_flag = sql_check_flag + " select top 1 '" + UtilTool.getUID() + "',null,'',1,0,1,'审核状态','自定义已选项','" + field + "',0,'String',20,'',0,0,1,'20',1,1,1,entity_key,'SHZT','左对齐',0,0,0,'null','null',0,null,0,0,0,0,0,0 from tabname where entityname = '" + entity + "' and not exists(select 1 from system where field_name ='" + field + "' and entity_key in(select entity_key from tabname where entityname = '" + entity + "'));";
/*     */         }
/*     */         else {
/* 137 */           sql_check_flag = sql_check_flag + " select '" + UtilTool.getUID() + "',null,'',1,0,1,'审核状态','自定义已选项','" + field + "',0,'String',20,'',0,0,1,'20',1,1,1,entity_key,'SHZT','左对齐',0,0,0,'null','null',0,null,0,0,0,0,0,0 from tabname where entityname = '" + entity + "' and not exists(select 1 from system where field_name ='" + field + "' and entity_key in(select entity_key from tabname where entityname = '" + entity + "')) and rownum<2;";
/*     */         }
/*     */         
/* 140 */         sql_check_flag = sql_check_flag + "delete from system where field_name='check_flag' and entity_key in(select entity_key from tabname where entityname='" + entity.toString() + "');";
/* 141 */         HibernateUtil.excuteSQLs(sql_check_flag, ";");
/*     */       }
/*     */     }
/*     */   }
/*     */   
/* 146 */   public static void delGroupFun(Session session, String fun_module_flag, String group) { group = SysUtil.objToStr(group);
/* 147 */     if (!group.equals("")) {
/* 148 */       group = fun_module_flag + "." + group;
/* 149 */       Object obj = session.createSQLQuery("select count(*) from FuntionRight fr,FuntionRight fr1 where fr.fun_parent_code=fr1.fun_parent_code and fr1.fun_module_flag='" + group + "'").list();
/* 150 */       int count = SysUtil.objToInt(obj);
/* 151 */       if (count == 0) {
/* 152 */         session.createSQLQuery("delete RoleFuntion where funtionRight_key in(select funtionright_key from funtionright where fun_module_flag='" + group + "')").executeUpdate();
/* 153 */         session.createSQLQuery("delete FuntionRight where fun_module_flag='" + group + "'").executeUpdate();
/* 154 */         session.flush();
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   public static String getDelFunSQL(String code) {
/* 160 */     String s_where = "fun_module_flag='" + code + "'";
/* 161 */     if (code.startsWith("#")) {
/* 162 */       s_where = "fun_module_flag like '" + code.substring(1) + "%'";
/*     */     }
/* 164 */     String sql = "delete from rolefuntion where funtionright_key in(select funtionright_key from funtionright where " + s_where + ");";
/* 165 */     sql = sql + "delete from funtionright where " + s_where + ";";
/* 166 */     return sql;
/*     */   }
/*     */   
/*     */   private static String getDelSQL(String s_where) {
/* 170 */     String sql = "delete from rolefuntion where funtionright_key in(select funtionright_key from funtionright where " + s_where + ");";
/* 171 */     sql = sql + "delete from funtionright where " + s_where + ";";
/* 172 */     return sql;
/*     */   }
/*     */   
/*     */   private static String getUpdateSQL(String moduleCode)
/*     */   {
/* 177 */     return "update funtionright set granted=1 where fun_module_flag like '" + moduleCode + "%';";
/*     */   }
/*     */   
/*     */   public static void rebuildFun(Session s, String roleKey, String parentKey, String funKey, String funName) throws Exception {
/* 181 */     FuntionRight fr_parent = (FuntionRight)s.createQuery("from FuntionRight fr where fun_module_flag='" + parentKey + "'").uniqueResult();
/* 182 */     if (fr_parent == null) {
/* 183 */       throw new Exception("功能树错误，无法生成相应功能菜单，请联系系统管理员:" + parentKey);
/*     */     }
/* 185 */     String parent_code = fr_parent.getFun_code();
/* 186 */     FuntionRight fr_group = (FuntionRight)s.createQuery("from FuntionRight where fun_module_flag='" + funKey + "'").uniqueResult();
/* 187 */     boolean isNew = false;
/* 188 */     if (fr_group == null) {
/* 189 */       isNew = true;
/* 190 */       fr_group = new FuntionRight();
/* 191 */       fr_group.setFuntionRight_key(funKey);
/*     */     }
/* 193 */     if ((fr_group.getFun_code() == null) || (!fr_group.getFun_parent_code().startsWith(parent_code))) {
/* 194 */       String tmp = (String)s.createSQLQuery("select max(fun_code) from FuntionRight r where r.fun_parent_code='" + fr_parent.getFun_code() + "'").uniqueResult();
/* 195 */       fr_group.setFun_code(SysUtil.getNewFuntionCode(fr_parent.getFun_code(), tmp));
/*     */     }
/* 197 */     fr_group.setFun_parent_code(parent_code);
/* 198 */     fr_group.setFun_name(funName);
/* 199 */     fr_group.setFun_level(Integer.valueOf(fr_parent.getFun_level().intValue() + 1));
/* 200 */     fr_group.setGranted(true);
/* 201 */     fr_group.setFun_module_flag(funKey);
/* 202 */     fr_group.setModule_flag(false);
/* 203 */     s.saveOrUpdate(fr_group);
/* 204 */     s.flush();
/* 205 */     rebuildFun(s, roleKey, fr_group.getFuntionRight_key(), isNew);
/* 206 */     s.flush();
/*     */   }
/*     */   
/*     */   public static void rebuildFun(Session s, String roleKey, String funtionright_key, boolean isNew) throws Exception {
/* 210 */     if ((roleKey != null) && (!roleKey.equals("-1")) && (isNew)) {
/* 211 */       s.createSQLQuery(getRightSQL(roleKey, funtionright_key)).executeUpdate();
/*     */     }
/*     */   }
/*     */   
/*     */   public static String getRightSQL(String roleKey, String funtionright_key) {
/* 216 */     String key = "'" + roleKey + "_" + funtionright_key + "'";
/* 217 */     String last = UserContext.sql_dialect.equals("oracle") ? " from dual" : "";
/* 218 */     String sql = "insert into rolefuntion(role_funtion_key,role_key,funtionright_key,fun_flag)select " + key + ",'" + roleKey + "','" + funtionright_key + "',1" + last;
/*     */     
/* 220 */     return sql;
/*     */   }
/*     */ }

