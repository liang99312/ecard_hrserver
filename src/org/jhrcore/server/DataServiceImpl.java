/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import java.rmi.RemoteException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import javax.ejb.Remote;
/*     */ import javax.ejb.Stateless;
/*     */ import javax.script.ScriptEngine;
/*     */ import javax.script.ScriptEngineManager;
/*     */ import javax.script.ScriptException;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.hibernate.SQLQuery;
/*     */ import org.hibernate.Session;
/*     */ import org.hibernate.Transaction;
/*     */ import org.jboss.annotation.ejb.Clustered;
/*     */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*     */ import org.jhrcore.iservice.DataService;
/*     */ import org.jhrcore.server.util.ImplUtil;
/*     */ import org.jhrcore.util.DbUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @Clustered
/*     */ @Stateless
/*     */ @Remote({DataService.class})
/*     */ public class DataServiceImpl
/*     */   extends SuperImpl
/*     */   implements DataService
/*     */ {
/*  35 */   private static Logger log = Logger.getLogger(DataServiceImpl.class);
/*     */   
/*     */   public DataServiceImpl() throws RemoteException {
/*  38 */     super(ServerApp.rmiPort);
/*     */   }
/*     */   
/*     */   public DataServiceImpl(int port) throws RemoteException {
/*  42 */     super(port);
/*     */   }
/*     */   
/*     */   public ArrayList getEntitysBy(String hql, int pageInd, int count) throws RemoteException
/*     */   {
/*  47 */     return HibernateUtil.fetchEntitysBy(hql, pageInd, count);
/*     */   }
/*     */   
/*     */   public ArrayList getEntitysBy(String hql) throws RemoteException
/*     */   {
/*  52 */     return HibernateUtil.fetchEntitiesBy(hql);
/*     */   }
/*     */   
/*     */   public Object fetchEntityBy(String hql) throws RemoteException
/*     */   {
/*  57 */     return HibernateUtil.fetchEntityBy(hql);
/*     */   }
/*     */   
/*     */   public ArrayList selectSQL(String sql, Boolean fetch_head, Integer max_size) throws RemoteException
/*     */   {
/*  62 */     return HibernateUtil.selectSQL(sql, fetch_head.booleanValue(), max_size.intValue());
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteHQL(String hql) throws RemoteException
/*     */   {
/*  67 */     return HibernateUtil.excuteHQL(hql);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteHQLs(String hql, String split_char) throws RemoteException
/*     */   {
/*  72 */     return HibernateUtil.excuteHQLs(hql, split_char);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteHQLs(String sql, List keys, boolean isInt, String split_char, String extra_char) throws RemoteException
/*     */   {
/*  77 */     String ex_sql = DbUtil.getQueryForMID(sql, keys, split_char, extra_char, isInt);
/*  78 */     return HibernateUtil.excuteHQLs(ex_sql, split_char);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteSQL(String sql) throws RemoteException
/*     */   {
/*  83 */     return HibernateUtil.excuteSQL(sql);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteSQLs(String sql, List keys, boolean isInt, String split_char, String extra_char) throws RemoteException
/*     */   {
/*  88 */     String ex_sql = DbUtil.getQueryForMID(sql, keys, split_char, extra_char, isInt);
/*  89 */     return HibernateUtil.excuteSQLs(ex_sql, split_char);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteSQLs(String sql, String split_char) throws RemoteException
/*     */   {
/*  94 */     return HibernateUtil.excuteSQLs(sql, split_char);
/*     */   }
/*     */   
/*     */   public boolean exists(String hql) throws RemoteException
/*     */   {
/*  99 */     return HibernateUtil.exists(hql);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult deleteObj(Object o) throws RemoteException
/*     */   {
/* 104 */     return HibernateUtil.delete(o);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult deleteObjs(String tableName, String fieldName, List<String> keys) throws RemoteException
/*     */   {
/* 109 */     return HibernateUtil.deleteObjs(tableName, fieldName, keys);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult saveObj(Object o) throws RemoteException
/*     */   {
/* 114 */     return HibernateUtil.save(o);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult saveSet(HashSet set) throws RemoteException
/*     */   {
/* 119 */     return HibernateUtil.saveSet(set);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult saveList(List set) throws RemoteException
/*     */   {
/* 124 */     return HibernateUtil.saveList(set);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult update(Object obj) throws RemoteException
/*     */   {
/* 129 */     return HibernateUtil.update(obj);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult saveOrUpdate(Object obj) throws RemoteException
/*     */   {
/* 134 */     return HibernateUtil.saveOrUpdate(obj);
/*     */   }
/*     */   
/*     */   public ArrayList selectSQLByKey(String sql, String sql2, List<String> keys, boolean isInt) throws RemoteException
/*     */   {
/* 139 */     return HibernateUtil.selectSQL(sql, sql2, keys, isInt);
/*     */   }
/*     */   
/*     */   public ArrayList getEntitysByKey(String hql, List<String> keys, boolean isInt) throws RemoteException
/*     */   {
/* 144 */     return HibernateUtil.fetchEntitys(hql, keys, isInt);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteSQLs(List<String> sqls) throws RemoteException
/*     */   {
/* 149 */     return HibernateUtil.excuteSQLs(sqls);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult validateSQL(String sql, boolean update) throws RemoteException
/*     */   {
/* 154 */     return HibernateUtil.validateSQL(sql, update);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult validateHQL(String sql, boolean update) throws RemoteException
/*     */   {
/* 159 */     return HibernateUtil.validateHQL(sql, update);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteSQL_jdbc(String sql) throws RemoteException
/*     */   {
/* 164 */     return excuteSQLs_jdbc(sql, null);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult excuteSQLs_jdbc(String sql, String split_char) throws RemoteException
/*     */   {
/* 169 */     return HibernateUtil.excuteSQLs_jdbc(sql, split_char);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult validateTriger(String triger_text, Object bean, Object old_val, Object new_val) throws RemoteException
/*     */   {
/* 174 */     ValidateSQLResult validate_result = new ValidateSQLResult();
/* 175 */     validate_result.setMsg("错误信息如下：");
/* 176 */     if (triger_text == null) {
/* 177 */       validate_result.setResult(1);
/* 178 */       validate_result.setMsg(validate_result.getMsg() + "\n " + "脚本为空。");
/* 179 */       return validate_result;
/*     */     }
/* 181 */     Session s = HibernateUtil.currentSession();
/* 182 */     Transaction tx = s.beginTransaction();
/*     */     try {
/* 184 */       if (triger_text.startsWith("SQL")) {
/* 185 */         String s_tmp = triger_text.substring(3);
/* 186 */         s_tmp = s_tmp.replaceAll("@key", "'987654321'");
/* 187 */         s.createSQLQuery(s_tmp).executeUpdate();
/*     */       } else {
/* 189 */         triger_text = "importPackage(org.jhrcore.entity);\nimportPackage(org.jhrcore.client);\n" + triger_text;
/* 190 */         ScriptEngineManager manager = new ScriptEngineManager();
/* 191 */         ScriptEngine engine = manager.getEngineByName("js");
/* 192 */         engine.put("session", HibernateUtil.currentSession());
/* 193 */         engine.put("old_e", bean);
/* 194 */         engine.put("e", bean);
/* 195 */         engine.put("log", log);
/* 196 */         engine.put("old_val", old_val);
/* 197 */         engine.put("new_val", new_val);
/*     */         try {
/* 199 */           engine.eval(triger_text);
/*     */         } catch (ScriptException ex) {
/* 201 */           validate_result.setResult(1);
/* 202 */           validate_result.setMsg(validate_result.getMsg() + "\n " + ". 第" + ex.getLineNumber() + "行 " + ex.getMessage());
/* 203 */           log.error(ex);
/*     */         }
/*     */       }
/*     */     } catch (Exception e) {
/* 207 */       ImplUtil.rollBack(tx);
/* 208 */       validate_result.setResult(1);
/* 209 */       validate_result.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 210 */       log.error(validate_result.getMsg());
/*     */     } finally {
/* 212 */       HibernateUtil.closeSession();
/*     */     }
/* 214 */     return validate_result;
/*     */   }
/*     */   
/*     */   public HashSet<String> getNot_triger_packages() throws RemoteException
/*     */   {
/* 219 */     return HibernateUtil.getNot_triger_packages();
/*     */   }
/*     */   
/*     */   public ValidateSQLResult update(Object obj, String hql) throws RemoteException
/*     */   {
/* 224 */     return HibernateUtil.update(obj, hql);
/*     */   }
/*     */   
/*     */   public List getDb_tables(String t_str) throws RemoteException
/*     */   {
/* 229 */     return HibernateUtil.getDb_tables(t_str);
/*     */   }
/*     */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\DataServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */