/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import java.rmi.RemoteException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import javax.ejb.Remote;
/*     */ import javax.ejb.Stateless;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.hibernate.SQLQuery;
/*     */ import org.hibernate.Session;
/*     */ import org.hibernate.Transaction;
/*     */ import org.jboss.annotation.ejb.Clustered;
/*     */ import org.jhrcore.entity.A01;
/*     */ import org.jhrcore.entity.RyChgLog;
/*     */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*     */ import org.jhrcore.iservice.ImportService;
/*     */ import org.jhrcore.server.util.ImplUtil;
/*     */ import org.jhrcore.util.PublicUtil;
/*     */ import org.jhrcore.util.UtilTool;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @Clustered
/*     */ @Stateless
/*     */ @Remote({ImportService.class})
/*     */ public class ImportServiceImpl
/*     */   extends SuperImpl
/*     */   implements ImportService
/*     */ {
/*  35 */   private Logger log = Logger.getLogger(ImportServiceImpl.class);
/*     */   
/*     */   public ImportServiceImpl() throws RemoteException {
/*  38 */     super(ServerApp.rmiPort);
/*     */   }
/*     */   
/*     */   public ImportServiceImpl(int port) throws RemoteException {
/*  42 */     super(port);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult importData(String ex_sql, HashSet save_objs) throws RemoteException
/*     */   {
/*  47 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  48 */     Session session = HibernateUtil.currentSession();
/*  49 */     Transaction tx = session.beginTransaction();
/*  50 */     int error_result = 0;
/*  51 */     int insert_result = 0;
/*  52 */     int update_result = 0;
/*  53 */     StringBuffer msg = new StringBuffer();
/*  54 */     if (save_objs.size() > 0) {
/*  55 */       for (Object obj : save_objs) {
/*     */         try {
/*  57 */           insert_result++;
/*  58 */           tx = session.beginTransaction();
/*  59 */           session.save(obj);
/*  60 */           tx.commit();
/*     */         } catch (Exception e) {
/*  62 */           insert_result--;
/*  63 */           error_result++;
/*  64 */           if ((obj instanceof A01)) {
/*  65 */             msg.append("人员编号：");
/*  66 */             msg.append(((A01)obj).getA0190());
/*     */           }
/*  68 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/*  69 */           ImplUtil.rollBack(tx);
/*     */         } finally {
/*  71 */           tx = null;
/*  72 */           session.clear();
/*     */         }
/*     */       }
/*     */     }
/*  76 */     validateSQLResult.setError_result(error_result < 0 ? 0 : error_result);
/*  77 */     validateSQLResult.setInsert_result(insert_result < 0 ? 0 : insert_result);
/*  78 */     validateSQLResult.setUpdate_result(update_result < 0 ? 0 : update_result);
/*  79 */     validateSQLResult.setMsg(msg.toString());
/*  80 */     ImplUtil.exSQLs_jdbc(session, validateSQLResult, ex_sql, "\\;", true);
/*  81 */     HibernateUtil.closeSession();
/*  82 */     return validateSQLResult;
/*     */   }
/*     */   
/*     */   public ValidateSQLResult saveObjData(HashSet save_objs, HashSet update_objs) throws RemoteException
/*     */   {
/*  87 */     int error_result = 0;
/*  88 */     int insert_result = 0;
/*  89 */     int update_result = 0;
/*  90 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  91 */     Session session = HibernateUtil.currentSession();
/*  92 */     Transaction tx = session.beginTransaction();
/*  93 */     StringBuilder msg = new StringBuilder();
/*  94 */     if (save_objs.size() > 0) {
/*  95 */       for (Object obj : new ArrayList(save_objs)) {
/*     */         try {
/*  97 */           insert_result++;
/*  98 */           session.save(obj);
/*     */         } catch (Exception e) {
/* 100 */           insert_result--;
/* 101 */           error_result++;
/* 102 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/*     */         }
/*     */       }
/*     */     }
/* 106 */     if (update_objs.size() > 0) {
/* 107 */       for (Object obj : new ArrayList(update_objs)) {
/*     */         try {
/* 109 */           update_result++;
/* 110 */           session.update(obj);
/*     */         } catch (Exception e) {
/* 112 */           update_result--;
/* 113 */           error_result++;
/* 114 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/*     */         }
/*     */       }
/*     */     }
/* 118 */     validateSQLResult.setUpdate_result(update_result < 0 ? 0 : update_result);
/* 119 */     validateSQLResult.setError_result(error_result < 0 ? 0 : error_result);
/* 120 */     validateSQLResult.setInsert_result(insert_result < 0 ? 0 : insert_result);
/* 121 */     validateSQLResult.setMsg(msg.toString());
/* 122 */     this.log.error(validateSQLResult.getMsg());
/* 123 */     tx.commit();
/* 124 */     HibernateUtil.closeSession();
/* 125 */     return validateSQLResult;
/*     */   }
/*     */   
/*     */   public ValidateSQLResult saveObjData(HashSet save_objs, HashSet update_objs, String ex_sql) throws RemoteException
/*     */   {
/* 130 */     int error_result = 0;
/* 131 */     int insert_result = 0;
/* 132 */     int update_result = 0;
/* 133 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 134 */     Session session = HibernateUtil.currentSession();
/* 135 */     Transaction tx = session.beginTransaction();
/* 136 */     StringBuilder msg = new StringBuilder();
/* 137 */     if (save_objs.size() > 0) {
/* 138 */       for (Object obj : new ArrayList(save_objs)) {
/*     */         try {
/* 140 */           insert_result++;
/* 141 */           session.save(obj);
/*     */         } catch (Exception e) {
/* 143 */           insert_result--;
/* 144 */           error_result++;
/* 145 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/*     */         }
/*     */       }
/*     */     }
/* 149 */     if (update_objs.size() > 0) {
/* 150 */       for (Object obj : new ArrayList(update_objs)) {
/*     */         try {
/* 152 */           update_result++;
/* 153 */           session.update(obj);
/*     */         } catch (Exception e) {
/* 155 */           update_result--;
/* 156 */           error_result++;
/* 157 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/*     */         }
/*     */       }
/*     */     }
/* 161 */     session.flush();
/* 162 */     ImplUtil.exSQLs(tx, session, validateSQLResult, ex_sql, ";", false);
/* 163 */     validateSQLResult.setUpdate_result(update_result < 0 ? 0 : update_result);
/* 164 */     validateSQLResult.setError_result(error_result < 0 ? 0 : error_result);
/* 165 */     validateSQLResult.setInsert_result(insert_result < 0 ? 0 : insert_result);
/* 166 */     validateSQLResult.setMsg(msg.toString());
/* 167 */     this.log.error(validateSQLResult.getMsg());
/* 168 */     tx.commit();
/* 169 */     HibernateUtil.closeSession();
/* 170 */     return validateSQLResult;
/*     */   }
/*     */   
/*     */   public ValidateSQLResult saveData(String ex_sql, List save_objs) throws RemoteException
/*     */   {
/* 175 */     int error_result = 0;
/* 176 */     int insert_result = 0;
/* 177 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 178 */     Session session = HibernateUtil.currentSession();
/* 179 */     Transaction tx = session.beginTransaction();
/* 180 */     StringBuffer msg = new StringBuffer();
/* 181 */     ImplUtil.exSQLs(tx, session, validateSQLResult, ex_sql, ";", true);
/* 182 */     if ((validateSQLResult.getResult() == 0) && 
/* 183 */       (save_objs.size() > 0)) {
/* 184 */       for (Object obj : save_objs) {
/*     */         try {
/* 186 */           insert_result++;
/* 187 */           tx = session.beginTransaction();
/* 188 */           session.save(obj);
/* 189 */           tx.commit();
/*     */         } catch (Exception e) {
/* 191 */           insert_result--;
/* 192 */           error_result++;
/* 193 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/* 194 */           ImplUtil.rollBack(tx);
/*     */         } finally {
/* 196 */           tx = null;
/* 197 */           session.clear();
/*     */         }
/*     */       }
/*     */     }
/*     */     
/* 202 */     validateSQLResult.setError_result(error_result < 0 ? 0 : error_result);
/* 203 */     validateSQLResult.setInsert_result(insert_result < 0 ? 0 : insert_result);
/* 204 */     validateSQLResult.setMsg(msg.toString());
/* 205 */     HibernateUtil.closeSession();
/* 206 */     return validateSQLResult;
/*     */   }
/*     */   
/*     */   public ValidateSQLResult importA01Data(String ex_sql, HashSet save_objs, RyChgLog rc) throws RemoteException
/*     */   {
/* 211 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 212 */     Session session = HibernateUtil.currentSession();
/* 213 */     Transaction tx = null;
/* 214 */     int error_result = 0;
/* 215 */     int insert_result = 0;
/* 216 */     int update_result = 0;
/* 217 */     StringBuffer msg = new StringBuffer();
/* 218 */     Date date; if (save_objs.size() > 0) {
/* 219 */       date = new Date();
/* 220 */       for (Object obj : save_objs) {
/*     */         try {
/* 222 */           tx = session.beginTransaction();
/* 223 */           session.save(obj);
/* 224 */           if ((obj instanceof A01)) {
/* 225 */             A01 a01 = (A01)obj;
/* 226 */             RyChgLog rcl = (RyChgLog)UtilTool.createUIDEntity(RyChgLog.class);
/* 227 */             rcl.setChg_ip(rc.getChg_ip());
/* 228 */             rcl.setChg_mac(rc.getChg_mac());
/* 229 */             rcl.setChg_user(rc.getChg_user());
/* 230 */             rcl.setChg_date(date);
/* 231 */             rcl.setAfterstate("新增(未入库)");
/* 232 */             rcl.setChg_type("入职新增");
/* 233 */             rcl.setChg_field("a0193");
/* 234 */             rcl.setChangeScheme_key("EmpScheme_Add");
/* 235 */             rcl.setChg_date(date);
/* 236 */             rcl.setA01_key(a01.getA01_key());
/* 237 */             rcl.setA0101(a01.getA0101());
/* 238 */             rcl.setA0190(a01.getA0190());
/* 239 */             session.save(rcl);
/*     */           }
/* 241 */           tx.commit();
/* 242 */           insert_result++;
/*     */         } catch (Exception e) {
/* 244 */           if ((obj instanceof A01)) {
/* 245 */             msg.append("人员编号：");
/* 246 */             msg.append(((A01)obj).getA0190());
/*     */           }
/* 248 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/* 249 */           msg.append(";\n");
/* 250 */           error_result++;
/* 251 */           ImplUtil.rollBack(tx);
/*     */         } finally {
/* 253 */           tx = null;
/* 254 */           session.clear();
/*     */         }
/*     */       }
/*     */     }
/* 258 */     validateSQLResult.setMsg(msg.toString());
/* 259 */     validateSQLResult.setError_result(error_result < 0 ? 0 : error_result);
/* 260 */     validateSQLResult.setInsert_result(insert_result < 0 ? 0 : insert_result);
/* 261 */     validateSQLResult.setUpdate_result(update_result < 0 ? 0 : update_result);
/* 262 */     if ((ex_sql != null) && (ex_sql.endsWith("\n"))) {
/* 263 */       ex_sql = ex_sql.substring(0, ex_sql.length() - 1);
/*     */     }
/* 265 */     ImplUtil.exSQLs_jdbc(session, validateSQLResult, ex_sql, "\\;", true);
/* 266 */     HibernateUtil.closeSession();
/* 267 */     return validateSQLResult;
/*     */   }
/*     */   
/*     */   public ValidateSQLResult importCommData(HashSet save_objs, HashSet update_objs, String comm_code) throws RemoteException
/*     */   {
/* 272 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 273 */     Session session = HibernateUtil.currentSession();
/* 274 */     Transaction tx = null;
/* 275 */     int error_result = 0;
/* 276 */     int insert_result = 0;
/* 277 */     int update_result = 0;
/* 278 */     StringBuilder msg = new StringBuilder();
/* 279 */     List<String> err_comp_keys = new ArrayList();
/* 280 */     String entity_key = comm_code + "_key";
/* 281 */     List<String> n_tables = new ArrayList();
/* 282 */     n_tables.add("ht01");
/* 283 */     n_tables.add("k_dayrecord");
/* 284 */     n_tables.add("in_overduepay");
/* 285 */     n_tables.add("in_pay");
/* 286 */     n_tables.add("k_overtime");
/* 287 */     n_tables.add("k_leave_leave");
/* 288 */     n_tables.add("k_card");
/* 289 */     n_tables.add("bonus_detail");
/* 290 */     n_tables.add("k_shift");
/* 291 */     n_tables.add("k_leave_standard");
/* 292 */     if (!n_tables.contains(comm_code.toLowerCase())) {
/* 293 */       Object obj = null;
/* 294 */       if (update_objs.size() > 0) {
/* 295 */         obj = update_objs.toArray()[0];
/*     */       } else {
/* 297 */         obj = save_objs.toArray()[0];
/*     */       }
/* 299 */       entity_key = obj.getClass().getSuperclass().getSimpleName() + "_key";
/*     */     }
/* 301 */     if (save_objs.size() > 0) {
/* 302 */       for (Object obj : save_objs) {
/* 303 */         String key = "@";
/*     */         try {
/* 305 */           tx = session.beginTransaction();
/* 306 */           session.save(obj);
/* 307 */           session.flush();
/* 308 */           if (!n_tables.contains(comm_code.toLowerCase())) {
/* 309 */             key = getCompKey(obj);
/* 310 */             session.createSQLQuery("update " + comm_code + " set last_flag='' where a01_key in(select a01_key from a01 where a0190='" + key + "')").executeUpdate();
/* 311 */             String new_state_sql = "update " + comm_code + " set last_flag='最新' where " + entity_key + " in(select " + entity_key + " from " + comm_code + " a,(select max(c.a_id) as a_id,c.a01_key from " + comm_code + " c,A01 a01 where c.a01_key=a01.a01_key and a01.a0190='" + key + "' group by c.a01_key) b where a.a_id=b.a_id and a.a01_key=b.a01_key)";
/*     */             
/* 313 */             session.createSQLQuery(new_state_sql).executeUpdate();
/*     */           }
/* 315 */           tx.commit();
/* 316 */           insert_result++;
/*     */         } catch (Exception e) {
/* 318 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/* 319 */           msg.append(";\n");
/* 320 */           if (!err_comp_keys.contains(key)) {
/* 321 */             err_comp_keys.add(key);
/*     */           }
/* 323 */           error_result++;
/* 324 */           ImplUtil.rollBack(tx);
/*     */         } finally {
/* 326 */           tx = null;
/* 327 */           session.clear();
/*     */         }
/*     */       }
/*     */     }
/* 331 */     if (update_objs.size() > 0) {
/* 332 */       for (Object obj : update_objs) {
/*     */         try {
/* 334 */           tx = session.beginTransaction();
/* 335 */           session.update(obj);
/*     */           
/*     */ 
/* 338 */           tx.commit();
/* 339 */           update_result++;
/*     */         } catch (Exception e) {
/* 341 */           msg.append(ImplUtil.getSQLExceptionMsg(e));
/* 342 */           msg.append(";\n");
/* 343 */           String obj_key = "@";
/* 344 */           if (!n_tables.contains(comm_code.toLowerCase())) {
/* 345 */             obj_key = getCompKey(obj);
/*     */           }
/* 347 */           if (!err_comp_keys.contains(obj_key)) {
/* 348 */             err_comp_keys.add(obj_key);
/*     */           }
/* 350 */           error_result++;
/* 351 */           ImplUtil.rollBack(tx);
/*     */         } finally {
/* 353 */           tx = null;
/* 354 */           session.clear();
/*     */         }
/*     */       }
/*     */     }
/* 358 */     validateSQLResult.setError_comp_keys(err_comp_keys);
/* 359 */     validateSQLResult.setUpdate_result(update_result);
/* 360 */     validateSQLResult.setInsert_result(insert_result);
/* 361 */     validateSQLResult.setError_result(error_result);
/* 362 */     validateSQLResult.setResult(error_result);
/* 363 */     validateSQLResult.setMsg(msg.toString());
/* 364 */     HibernateUtil.closeSession();
/* 365 */     return validateSQLResult;
/*     */   }
/*     */   
/*     */   private String getCompKey(Object obj) {
/* 369 */     Object key = PublicUtil.getProperty(obj, "a01.a0190");
/* 370 */     return key == null ? "@" : key.toString();
/*     */   }
/*     */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\ImportServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */