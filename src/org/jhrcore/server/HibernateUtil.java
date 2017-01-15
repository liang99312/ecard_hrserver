/*      */ package org.jhrcore.server;
/*      */ 
/*      */ import java.io.File;
/*      */ import java.io.PrintStream;
/*      */ import java.io.Serializable;
/*      */ import java.sql.Connection;
/*      */ import java.sql.PreparedStatement;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.ResultSetMetaData;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Statement;
/*      */ import java.text.DateFormat;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashSet;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import javax.script.ScriptEngine;
/*      */ import javax.script.ScriptEngineManager;
/*      */ import org.apache.log4j.Logger;
/*      */ import org.hibernate.Hibernate;
/*      */ import org.hibernate.HibernateException;
/*      */ import org.hibernate.Query;
/*      */ import org.hibernate.SQLQuery;
/*      */ import org.hibernate.Session;
/*      */ import org.hibernate.SessionFactory;
/*      */ import org.hibernate.Transaction;
/*      */ import org.hibernate.cfg.AnnotationConfiguration;
/*      */ import org.hibernate.cfg.Configuration;
/*      */ import org.jhrcore.comm.BeanManager;
/*      */ import org.jhrcore.entity.AutoNo;
/*      */ import org.jhrcore.entity.AutoNoRule;
/*      */ import org.jhrcore.entity.base.FieldDef;
/*      */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*      */ import org.jhrcore.rebuild.EntityBuilder;
/*      */ import org.jhrcore.server.util.ImplUtil;
/*      */ import org.jhrcore.util.DbUtil;
/*      */ import org.jhrcore.util.PublicUtil;
/*      */ 
/*      */ public class HibernateUtil
/*      */ {
/*   44 */   private static Logger log = Logger.getLogger(HibernateUtil.class.getName());
/*   45 */   private static SessionFactory sessionFactory = null;
/*   46 */   private static AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration();
/*   47 */   private static HashSet<String> not_triger_packages = new HashSet();
/*   48 */   private static int squence_len = 2000;
/*   49 */   private static DbConfig dbConfig = null;
/*   50 */   private static String db_type = null;
/*   51 */   private static int closed = 0;
/*   52 */   private static Map<String, Integer> sta_map = new java.util.HashMap();
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public static final ThreadLocal<Session> session;
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private static ScriptEngine engine;
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private static DbConfig getDbConfig()
/*      */   {
/*   76 */     if (dbConfig == null) {
/*   77 */       dbConfig = new DbConfig();
/*   78 */       getSessionFactory();
/*      */       try {
/*   80 */         dbConfig.setUrl(annotationConfiguration.getProperty("connection.url"));
/*   81 */         dbConfig.setDialect(annotationConfiguration.getProperty("dialect"));
/*   82 */         dbConfig.setDriver(annotationConfiguration.getProperty("connection.driver_class"));
/*   83 */         dbConfig.setUser(annotationConfiguration.getProperty("connection.username"));
/*   84 */         dbConfig.setPass(annotationConfiguration.getProperty("connection.password"));
/*      */       } catch (Throwable ex) {
/*   86 */         log.error(ex);
/*      */       }
/*      */     }
/*   89 */     return dbConfig;
/*      */   }
/*      */   
/*      */   public static String getSQL_url() {
/*      */     try {
/*   94 */       return getDbConfig().getUrl();
/*      */     } catch (Throwable ex) {
/*   96 */       log.error(ex);
/*   97 */       throw new ExceptionInInitializerError(ex);
/*      */     }
/*      */   }
/*      */   
/*      */   public static String getSQL_driver() {
/*      */     try {
/*  103 */       return getDbConfig().getDriver();
/*      */     } catch (Throwable ex) {
/*  105 */       log.error(ex);
/*  106 */       throw new ExceptionInInitializerError(ex);
/*      */     }
/*      */   }
/*      */   
/*      */   public static String getSQL_user() {
/*      */     try {
/*  112 */       return getDbConfig().getUser();
/*      */     } catch (Throwable ex) {
/*  114 */       log.error(ex);
/*  115 */       throw new ExceptionInInitializerError(ex);
/*      */     }
/*      */   }
/*      */   
/*      */   public static String getSQL_pass() {
/*      */     try {
/*  121 */       return getDbConfig().getPass();
/*      */     } catch (Throwable ex) {
/*  123 */       log.error(ex);
/*  124 */       throw new ExceptionInInitializerError(ex);
/*      */     }
/*      */   }
/*      */   
/*      */   public static String getDb_type() {
/*  129 */     if (db_type == null) {
/*  130 */       db_type = DbUtil.SQL_dialect_check(getDialect());
/*      */     }
/*  132 */     return db_type;
/*      */   }
/*      */   
/*      */   public static String getDialect() {
/*      */     try {
/*  137 */       return getDbConfig().getDialect();
/*      */     } catch (Throwable ex) {
/*  139 */       log.error(ex);
/*  140 */       throw new ExceptionInInitializerError(ex);
/*      */     }
/*      */   }
/*      */   
/*      */   public static Date getServerDate() {
/*  145 */     return new Date();
/*      */   }
/*      */   
/*      */   public static SessionFactory getSessionFactory() {
/*  149 */     if (closed == 1) {
/*  150 */       return null;
/*      */     }
/*  152 */     if (sessionFactory == null) {
/*      */       try {
/*  154 */         File file = new File(System.getProperty("user.dir") + "/hibernate.cfg.xml");
/*  155 */         Configuration configure = annotationConfiguration.configure(file);
/*  156 */         sessionFactory = configure.buildSessionFactory();
/*      */       } catch (Throwable ex) {
/*  158 */         log.error(ex);
/*  159 */         throw new ExceptionInInitializerError(ex);
/*      */       }
/*      */     }
/*  162 */     return sessionFactory;
/*      */   }
/*      */   
/*      */   public static void closeConnect() {
/*  166 */     getDbConfig();
/*  167 */     closeSessionFactory();
/*  168 */     closed = 1;
/*      */   }
/*      */   
/*      */   public static void closeSessionFactory() {
/*  172 */     if (sessionFactory != null) {
/*  173 */       sessionFactory.close();
/*      */     }
/*  175 */     sessionFactory = null;
/*      */   }
/*      */   
/*      */   public static Session currentSession() throws HibernateException
/*      */   {
/*  180 */     Session s = (Session)session.get();
/*      */     
/*  182 */     if (s == null) {
/*  183 */       s = getSessionFactory().openSession();
/*      */       
/*  185 */       session.set(s);
/*      */     }
/*  187 */     return s;
/*      */   }
/*      */   
/*      */   public static void closeSession() throws HibernateException {
/*  191 */     Session s = (Session)session.get();
/*  192 */     if (s != null) {
/*  193 */       s.close();
/*      */     }
/*  195 */     session.set(null);
/*      */   }
/*      */   
/*      */   public static ArrayList fetchEntitiesBy(String hql)
/*      */   {
/*  200 */     long time1 = System.currentTimeMillis();
/*  201 */     Session cur_session = currentSession();
/*      */     try {
/*  203 */       Query qr = cur_session.createQuery(hql);
/*  204 */       qr.setMaxResults(500000);
/*  205 */       List list = qr.list();
/*  206 */       long time2; return (ArrayList)list;
/*      */     } catch (Exception e) {
/*  208 */       log.error(ImplUtil.getSQLExceptionMsg(e) + ":" + hql);
/*      */     } finally {
/*  210 */       long time2 = System.currentTimeMillis();
/*  211 */       if (time2 - time1 > 100000L) {
/*  212 */         log.error(time2 - time1 + " SQL:" + hql);
/*      */       }
/*  214 */       closeSession();
/*      */     }
/*  216 */     return new ArrayList();
/*      */   }
/*      */   
/*      */   public static Object fetchEntityBy(String hql)
/*      */   {
/*  221 */     Session cur_session = currentSession();
/*  222 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  224 */       Object o = cur_session.createQuery(hql).setCacheable(false).uniqueResult();
/*      */       
/*  226 */       return o;
/*      */     } catch (Exception e) {
/*  228 */       e.printStackTrace();
/*  229 */       log.error(ImplUtil.getSQLExceptionMsg(e) + ":" + hql);
/*  230 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  232 */       closeSession();
/*      */     }
/*  234 */     return null;
/*      */   }
/*      */   
/*      */   public static ArrayList fetchEntitysBy(String hql, int pageInd, int count)
/*      */   {
/*  239 */     ArrayList alist = new ArrayList();
/*  240 */     Session cur_session = currentSession();
/*  241 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  243 */       Query qr = cur_session.createQuery(hql);
/*  244 */       qr.setFirstResult((pageInd - 1) * count);
/*  245 */       qr.setMaxResults(count);
/*  246 */       alist.addAll(qr.list());
/*  247 */       tx.commit();
/*      */     } catch (Exception e) {
/*  249 */       log.error(ImplUtil.getSQLExceptionMsg(e) + ":" + hql);
/*  250 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  252 */       closeSession();
/*      */     }
/*  254 */     return alist;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public static ArrayList selectSQL(String sql, boolean fetch_head, int max_size)
/*      */   {
/*  265 */     Session cur_session = currentSession();
/*  266 */     ArrayList alist = new ArrayList();
/*  267 */     long time1 = System.currentTimeMillis();
/*      */     try {
/*  269 */       if (fetch_head) {
/*  270 */         Connection c = cur_session.connection();
/*  271 */         Statement sttm = c.createStatement();
/*  272 */         sttm.setMaxRows(0);
/*  273 */         ResultSet rs = null;
/*      */         try {
/*  275 */           rs = sttm.executeQuery(sql);
/*  276 */           ResultSetMetaData rsmd = rs.getMetaData();
/*  277 */           int cols = rsmd.getColumnCount();
/*  278 */           String[] heads = new String[cols];
/*  279 */           for (int j = 1; j <= cols; j++) {
/*  280 */             heads[(j - 1)] = rsmd.getColumnName(j);
/*      */           }
/*  282 */           alist.add(0, heads);
/*  283 */           rs.close();
/*      */         } catch (Exception e) {
/*  285 */           log.error(e);
/*      */         } finally {
/*  287 */           rs.close();
/*  288 */           sttm.close();
/*      */         }
/*      */       }
/*  291 */       SQLQuery qr = cur_session.createSQLQuery(sql);
/*  292 */       if (max_size > 0) {
/*  293 */         qr.setMaxResults(max_size);
/*      */       }
/*  295 */       List i = qr.list();
/*  296 */       alist.addAll(i);
/*      */     } catch (Exception e) {
/*  298 */       log.error(e + ":" + sql);
/*      */     } finally {
/*  300 */       closeSession();
/*      */     }
/*  302 */     long time2 = System.currentTimeMillis();
/*  303 */     if (time2 - time1 > 100000L) {
/*  304 */       log.error("耗时：" + (time2 - time1) + " SQL:" + sql);
/*      */     }
/*  306 */     return alist;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult excuteSQL(String sql)
/*      */   {
/*  311 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  312 */     Session cur_session = currentSession();
/*  313 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  315 */       cur_session.createSQLQuery(sql).executeUpdate();
/*  316 */       tx.commit();
/*      */     } catch (Exception e) {
/*  318 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  319 */       log.error(validateSQLResult.getMsg() + ":" + sql);
/*  320 */       ImplUtil.rollBack(tx);
/*  321 */       validateSQLResult.setResult(1);
/*      */     } finally {
/*  323 */       closeSession();
/*      */     }
/*  325 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult excuteSQL(String[] sqls) {
/*  329 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  330 */     Session cur_session = currentSession();
/*  331 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  333 */       if ((sqls != null) && (sqls.length > 0)) {
/*  334 */         int len = sqls.length;
/*  335 */         for (int i = 0; i < len; i++) {
/*  336 */           String sql = sqls[i];
/*  337 */           if ((sql != null) && (!sql.isEmpty()))
/*      */           {
/*      */ 
/*  340 */             cur_session.createSQLQuery(sql).executeUpdate();
/*  341 */             cur_session.flush();
/*  342 */             cur_session.clear();
/*      */           }
/*      */         } }
/*  345 */       tx.commit();
/*      */     } catch (Exception e) {
/*  347 */       e.printStackTrace();
/*  348 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  349 */       validateSQLResult.setResult(1);
/*  350 */       log.error(validateSQLResult.getMsg());
/*  351 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  353 */       closeSession();
/*      */     }
/*  355 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult excuteSQLs(List<String> sqls) {
/*  359 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  360 */     Session cur_session = currentSession();
/*  361 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  363 */       if ((sqls != null) && (sqls.size() > 0)) {
/*  364 */         for (String sql : sqls)
/*  365 */           if ((sql != null) && (!sql.isEmpty()))
/*      */           {
/*      */ 
/*  368 */             cur_session.createSQLQuery(sql).executeUpdate();
/*  369 */             cur_session.flush();
/*  370 */             cur_session.clear();
/*      */           }
/*      */       }
/*  373 */       tx.commit();
/*      */     } catch (Exception e) {
/*  375 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  376 */       log.error(validateSQLResult.getMsg() + ";" + sqls);
/*  377 */       validateSQLResult.setResult(1);
/*  378 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  380 */       closeSession();
/*      */     }
/*  382 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult excuteSQLs(String sql, String split_char) {
/*  386 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  387 */     String[] sqls = sql.split(split_char);
/*  388 */     if ((sqls != null) && (sqls.length > 0)) {
/*  389 */       int len = sqls.length;
/*  390 */       int a = len / squence_len;
/*  391 */       int s = len % squence_len;
/*  392 */       if (a > 0) {
/*  393 */         for (int i = 0; i < a; i++) {
/*  394 */           int start = i * squence_len;
/*  395 */           String[] ex_sqls = new String[squence_len];
/*  396 */           for (int j = 0; j < squence_len; j++) {
/*  397 */             ex_sqls[j] = sqls[(start + j)];
/*      */           }
/*  399 */           validateSQLResult = excuteSQL(ex_sqls);
/*  400 */           if (validateSQLResult.getResult() != 0) {
/*  401 */             return validateSQLResult;
/*      */           }
/*      */         }
/*      */       }
/*  405 */       if (s > 0) {
/*  406 */         int start = a * squence_len;
/*  407 */         String[] ex_sqls = new String[len - start];
/*  408 */         for (int i = start; i < len; i++) {
/*  409 */           ex_sqls[(i - start)] = sqls[i];
/*      */         }
/*  411 */         validateSQLResult = excuteSQL(ex_sqls);
/*      */       }
/*      */     }
/*  414 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult excuteHQL(String hql)
/*      */   {
/*  419 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  420 */     Session cur_session = currentSession();
/*  421 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  423 */       cur_session.createQuery(hql).executeUpdate();
/*  424 */       tx.commit();
/*      */     } catch (Exception e) {
/*  426 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  427 */       log.error(validateSQLResult.getMsg() + "\n" + hql);
/*  428 */       ImplUtil.rollBack(tx);
/*  429 */       validateSQLResult.setResult(1);
/*      */     } finally {
/*  431 */       closeSession();
/*      */     }
/*  433 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult excuteHQL(String[] sqls)
/*      */   {
/*  438 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  439 */     Session cur_session = currentSession();
/*  440 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  442 */       if ((sqls != null) && (sqls.length > 0)) {
/*  443 */         int len = sqls.length;
/*  444 */         for (int i = 0; i < len; i++) {
/*  445 */           cur_session.createQuery(sqls[i]).executeUpdate();
/*  446 */           cur_session.flush();
/*  447 */           cur_session.clear();
/*      */         }
/*      */       }
/*  450 */       tx.commit();
/*      */     } catch (Exception e) {
/*  452 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  453 */       log.error(validateSQLResult.getMsg());
/*  454 */       validateSQLResult.setResult(1);
/*  455 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  457 */       closeSession();
/*      */     }
/*  459 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult excuteHQLs(String hql, String split_char)
/*      */   {
/*  464 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  465 */     String[] sqls = hql.split(split_char);
/*  466 */     if ((sqls != null) && (sqls.length > 0)) {
/*  467 */       int len = sqls.length;
/*  468 */       int a = len / squence_len;
/*  469 */       int s = len % squence_len;
/*  470 */       if (a > 0) {
/*  471 */         for (int i = 0; i < a; i++) {
/*  472 */           int start = i * squence_len;
/*  473 */           String[] ex_sqls = new String[squence_len];
/*  474 */           for (int j = 0; j < squence_len; j++) {
/*  475 */             ex_sqls[j] = sqls[(start + j)];
/*      */           }
/*  477 */           validateSQLResult = excuteHQL(ex_sqls);
/*  478 */           if (validateSQLResult.getResult() != 0) {
/*  479 */             return validateSQLResult;
/*      */           }
/*      */         }
/*      */       }
/*  483 */       if (s > 0) {
/*  484 */         int start = a * squence_len;
/*  485 */         String[] ex_sqls = new String[len - start];
/*  486 */         for (int i = start; i < len; i++) {
/*  487 */           ex_sqls[(i - start)] = sqls[i];
/*      */         }
/*  489 */         validateSQLResult = excuteSQL(ex_sqls);
/*      */       }
/*      */     }
/*  492 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult delete(Object o)
/*      */   {
/*  497 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  498 */     Session cur_session = currentSession();
/*  499 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  501 */       cur_session.delete(o);
/*  502 */       tx.commit();
/*      */     } catch (Exception e) {
/*  504 */       validateSQLResult.setResult(1);
/*  505 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  506 */       log.error(validateSQLResult.getMsg() + ":" + o.getClass().getName());
/*  507 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  509 */       closeSession();
/*      */     }
/*  511 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult deleteObjs(String tableName, String fieldName, List<String> keys) {
/*  515 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  516 */     if ((keys == null) || (keys.isEmpty())) {
/*  517 */       return validateSQLResult;
/*      */     }
/*  519 */     if (keys.size() < 200) {
/*  520 */       StringBuffer str = new StringBuffer();
/*  521 */       for (String key : keys) {
/*  522 */         str.append(",'").append(key).append("'");
/*      */       }
/*  524 */       Session cur_session = currentSession();
/*  525 */       Transaction tx = cur_session.beginTransaction();
/*      */       try {
/*  527 */         String ex_sql = "delete from " + tableName + " where " + fieldName + " in (" + str.substring(1) + ")";
/*  528 */         cur_session.createSQLQuery(ex_sql).executeUpdate();
/*  529 */         tx.commit();
/*      */       } catch (Exception e) {
/*  531 */         validateSQLResult.setResult(1);
/*  532 */         validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  533 */         log.error(validateSQLResult.getMsg());
/*  534 */         ImplUtil.rollBack(tx);
/*      */       } finally {
/*  536 */         closeSession();
/*      */       }
/*      */     } else {
/*  539 */       String tmpTable = org.jhrcore.server.util.CountUtil.getCommTempTable();
/*  540 */       Session cur_session = currentSession();
/*  541 */       Transaction tx = cur_session.beginTransaction();
/*  542 */       String create_sql = "";
/*  543 */       if ((db_type.equals("sqlserver")) || (db_type.equals("mysql"))) {
/*  544 */         create_sql = "create table " + tmpTable + "(t_id varchar(255) primary key)";
/*      */       } else {
/*  546 */         create_sql = "create table " + tmpTable + "(t_id varchar2(255) primary key)";
/*      */       }
/*      */       try {
/*  549 */         DbUtil.initTempTable(cur_session, tmpTable, db_type);
/*  550 */         cur_session.createSQLQuery(create_sql).executeUpdate();
/*  551 */         for (String data : keys) {
/*  552 */           cur_session.createSQLQuery("insert into " + tmpTable + "(t_id)values('" + data + "')").executeUpdate();
/*      */         }
/*  554 */         cur_session.flush();
/*  555 */         String ex_sql = "delete from " + tableName + " where exists(select 1 from " + tmpTable + " t where t.t_id=" + fieldName + ")";
/*  556 */         cur_session.createSQLQuery(ex_sql).executeUpdate();
/*  557 */         cur_session.createSQLQuery("drop table " + tmpTable).executeUpdate();
/*  558 */         tx.commit();
/*      */       } catch (Exception e) {
/*  560 */         validateSQLResult.setResult(1);
/*  561 */         validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  562 */         log.error(validateSQLResult.getMsg());
/*  563 */         ImplUtil.rollBack(tx);
/*      */         try {
/*  565 */           cur_session.createSQLQuery("drop table " + tmpTable).executeUpdate();
/*      */         }
/*      */         catch (Exception ex) {}
/*      */       } finally {
/*  569 */         closeSession();
/*      */       }
/*      */     }
/*      */     
/*  573 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult saveList(List objs)
/*      */   {
/*  578 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  579 */     Session cur_session = currentSession();
/*  580 */     Transaction tx = cur_session.beginTransaction();
/*  581 */     Hashtable<String, List<FieldDef>> triger_defs = new Hashtable();
/*      */     try {
/*  583 */       for (Object obj : objs) {
/*  584 */         cur_session.save(obj);
/*  585 */         cur_session.flush();
/*  586 */         entity_triger(triger_defs, cur_session, null, obj);
/*      */       }
/*  588 */       tx.commit();
/*      */     } catch (Exception e) {
/*  590 */       validateSQLResult.setResult(1);
/*  591 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  592 */       log.error(validateSQLResult.getMsg());
/*  593 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  595 */       closeSession();
/*      */     }
/*  597 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult saveSet(HashSet objs)
/*      */   {
/*  602 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  603 */     Session cur_session = currentSession();
/*  604 */     Transaction tx = cur_session.beginTransaction();
/*  605 */     Hashtable<String, List<FieldDef>> triger_defs = new Hashtable();
/*      */     try {
/*  607 */       for (Object obj : objs) {
/*  608 */         cur_session.save(obj);
/*  609 */         cur_session.flush();
/*  610 */         entity_triger(triger_defs, cur_session, null, obj);
/*      */       }
/*  612 */       tx.commit();
/*      */     } catch (Exception e) {
/*  614 */       validateSQLResult.setResult(1);
/*  615 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  616 */       log.error(validateSQLResult.getMsg());
/*  617 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  619 */       closeSession();
/*      */     }
/*  621 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult save(Object o)
/*      */   {
/*  626 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  627 */     Session cur_session = currentSession();
/*  628 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  630 */       cur_session.save(o);
/*  631 */       cur_session.flush();
/*  632 */       entity_triger(new Hashtable(), cur_session, null, o);
/*  633 */       tx.commit();
/*      */     } catch (Exception e) {
/*  635 */       validateSQLResult.setResult(1);
/*  636 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  637 */       log.error("插入数据失败,时间：" + new Date() + " 数据：" + o.toString());
/*  638 */       log.error(validateSQLResult.getMsg());
/*  639 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  641 */       closeSession();
/*      */     }
/*  643 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult update(Object o)
/*      */   {
/*  648 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  649 */     Session cur_session = currentSession();
/*  650 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  652 */       cur_session.update(o);
/*  653 */       cur_session.flush();
/*  654 */       entity_triger(new Hashtable(), cur_session, null, o);
/*  655 */       tx.commit();
/*      */     } catch (Exception e) {
/*  657 */       validateSQLResult.setResult(1);
/*  658 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  659 */       log.error(validateSQLResult.getMsg());
/*  660 */       log.info("更新数据失败,时间：" + new Date() + " 数据：" + o.toString());
/*  661 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  663 */       closeSession();
/*      */     }
/*  665 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult saveOrUpdate(Object obj) {
/*  669 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  670 */     Session cur_session = currentSession();
/*  671 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  673 */       cur_session.saveOrUpdate(obj);
/*  674 */       cur_session.flush();
/*  675 */       entity_triger(new Hashtable(), cur_session, null, obj);
/*  676 */       tx.commit();
/*      */     } catch (Exception e) {
/*  678 */       validateSQLResult.setResult(1);
/*  679 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  680 */       log.error(validateSQLResult.getMsg());
/*  681 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  683 */       closeSession();
/*      */     }
/*  685 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ArrayList selectSQL(String sql, List<String> keys, boolean isInt) {
/*  689 */     int len = keys.size();
/*  690 */     int mod_len = len / 800;
/*  691 */     int re_len = mod_len + (len % 800 == 0 ? 0 : 1);
/*  692 */     ArrayList list = new ArrayList();
/*  693 */     Session cur_session = currentSession();
/*  694 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  696 */       if (isInt) {
/*  697 */         for (int i = 0; i < re_len; i++) {
/*  698 */           StringBuffer str = new StringBuffer();
/*      */           
/*  700 */           if (i < mod_len) {
/*  701 */             for (int j = 0; j < 800; j++) {
/*  702 */               str.append(",");
/*  703 */               str.append((String)keys.get(i * 800 + j));
/*      */             }
/*      */           } else {
/*  706 */             for (int j = 0; j < 800; j++) {
/*  707 */               int ind = i * 800 + j;
/*  708 */               if (ind >= len) {
/*      */                 break;
/*      */               }
/*  711 */               str.append(",");
/*  712 */               str.append((String)keys.get(ind));
/*      */             }
/*      */           }
/*      */           try {
/*  716 */             if (!str.toString().equals(""))
/*      */             {
/*      */ 
/*  719 */               List data = cur_session.createSQLQuery(sql + "(" + str.toString().substring(1) + ")").list();
/*  720 */               list.addAll(data);
/*      */             }
/*  722 */           } catch (Exception e) { log.error(e + ":" + sql);
/*  723 */             ImplUtil.rollBack(tx);
/*  724 */             break;
/*      */           }
/*      */         }
/*      */       } else {
/*  728 */         for (int i = 0; i < re_len; i++) {
/*  729 */           StringBuffer str = new StringBuffer();
/*      */           
/*  731 */           if (i < mod_len) {
/*  732 */             for (int j = 0; j < 800; j++) {
/*  733 */               str.append(",'");
/*  734 */               str.append((String)keys.get(i * 800 + j));
/*  735 */               str.append("'");
/*      */             }
/*      */           } else {
/*  738 */             for (int j = 0; j < 800; j++) {
/*  739 */               int ind = i * 800 + j;
/*  740 */               if (ind >= len) {
/*      */                 break;
/*      */               }
/*  743 */               str.append(",'");
/*  744 */               str.append((String)keys.get(ind));
/*  745 */               str.append("'");
/*      */             }
/*      */           }
/*      */           try {
/*  749 */             if (!str.toString().equals(""))
/*      */             {
/*      */ 
/*  752 */               List data = cur_session.createSQLQuery(sql + "(" + str.toString().substring(1) + ")").list();
/*  753 */               list.addAll(data);
/*      */             }
/*  755 */           } catch (Exception e) { log.error(e + ":" + sql);
/*  756 */             ImplUtil.rollBack(tx);
/*  757 */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception ex) {}finally {
/*  763 */       closeSession();
/*      */     }
/*  765 */     return list;
/*      */   }
/*      */   
/*      */   public static ArrayList selectSQL(String sql, String sql2, List<String> keys, boolean isInt) {
/*  769 */     int len = keys.size();
/*  770 */     int mod_len = len / 800;
/*  771 */     int re_len = mod_len + (len % 800 == 0 ? 0 : 1);
/*  772 */     ArrayList list = new ArrayList();
/*  773 */     Session cur_session = currentSession();
/*  774 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  776 */       if (isInt) {
/*  777 */         for (int i = 0; i < re_len; i++) {
/*  778 */           StringBuffer str = new StringBuffer();
/*      */           
/*  780 */           if (i < mod_len) {
/*  781 */             for (int j = 0; j < 800; j++) {
/*  782 */               str.append(",");
/*  783 */               str.append((String)keys.get(i * 800 + j));
/*      */             }
/*      */           } else {
/*  786 */             for (int j = 0; j < 800; j++) {
/*  787 */               int ind = i * 800 + j;
/*  788 */               if (ind >= len) {
/*      */                 break;
/*      */               }
/*  791 */               str.append(",");
/*  792 */               str.append((String)keys.get(ind));
/*      */             }
/*      */           }
/*      */           try {
/*  796 */             if (!str.toString().equals(""))
/*      */             {
/*      */ 
/*  799 */               List data = cur_session.createSQLQuery(sql + "(" + str.toString().substring(1) + ")" + sql2).list();
/*  800 */               list.addAll(data);
/*      */             }
/*  802 */           } catch (Exception e) { log.error(e + ":" + sql + "(" + str.toString() + ")" + sql2);
/*  803 */             ImplUtil.rollBack(tx);
/*  804 */             break;
/*      */           }
/*      */         }
/*      */       } else {
/*  808 */         for (int i = 0; i < re_len; i++) {
/*  809 */           StringBuffer str = new StringBuffer();
/*      */           
/*  811 */           if (i < mod_len) {
/*  812 */             for (int j = 0; j < 800; j++) {
/*  813 */               str.append(",'");
/*  814 */               str.append((String)keys.get(i * 800 + j));
/*  815 */               str.append("'");
/*      */             }
/*      */           } else {
/*  818 */             for (int j = 0; j < 800; j++) {
/*  819 */               int ind = i * 800 + j;
/*  820 */               if (ind >= len) {
/*      */                 break;
/*      */               }
/*  823 */               str.append(",'");
/*  824 */               str.append((String)keys.get(ind));
/*  825 */               str.append("'");
/*      */             }
/*      */           }
/*      */           try {
/*  829 */             if (!str.toString().equals(""))
/*      */             {
/*      */ 
/*  832 */               List data = cur_session.createSQLQuery(sql + "(" + str.toString().substring(1) + ")" + sql2).list();
/*  833 */               list.addAll(data);
/*      */             }
/*  835 */           } catch (Exception e) { log.error(e + ":" + sql + "(" + str.toString() + ")" + sql2);
/*  836 */             ImplUtil.rollBack(tx);
/*  837 */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception ex) {}finally {
/*  843 */       closeSession();
/*      */     }
/*  845 */     return list;
/*      */   }
/*      */   
/*      */   public static ArrayList fetchEntitys(String hql, List<String> keys, boolean isInt) {
/*  849 */     int len = keys.size();
/*  850 */     int mod_len = len / 800;
/*  851 */     int re_len = mod_len + (len % 800 == 0 ? 0 : 1);
/*  852 */     ArrayList list = new ArrayList();
/*  853 */     Session cur_session = currentSession();
/*  854 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/*  856 */       if (isInt) {
/*  857 */         for (int i = 0; i < re_len; i++) {
/*  858 */           StringBuffer str = new StringBuffer();
/*      */           
/*  860 */           if (i < mod_len) {
/*  861 */             for (int j = 0; j < 800; j++) {
/*  862 */               str.append(",");
/*  863 */               str.append((String)keys.get(i * 800 + j));
/*      */             }
/*      */           } else {
/*  866 */             for (int j = 0; j < 800; j++) {
/*  867 */               int ind = i * 800 + j;
/*  868 */               if (ind >= len) {
/*      */                 break;
/*      */               }
/*  871 */               str.append(",");
/*  872 */               str.append((String)keys.get(ind));
/*      */             }
/*      */           }
/*  875 */           if (!str.toString().equals(""))
/*      */           {
/*      */             try
/*      */             {
/*  879 */               List data = cur_session.createQuery(hql + "(" + str.toString().substring(1) + ")").list();
/*  880 */               list.addAll(data);
/*      */             } catch (Exception e) {
/*  882 */               log.error(e + ":" + hql);
/*  883 */               ImplUtil.rollBack(tx);
/*  884 */               break;
/*      */             } }
/*      */         }
/*      */       } else {
/*  888 */         for (int i = 0; i < re_len; i++) {
/*  889 */           StringBuffer str = new StringBuffer();
/*      */           
/*  891 */           if (i < mod_len) {
/*  892 */             for (int j = 0; j < 800; j++) {
/*  893 */               str.append(",'");
/*  894 */               str.append((String)keys.get(i * 800 + j));
/*  895 */               str.append("'");
/*      */             }
/*      */           } else {
/*  898 */             for (int j = 0; j < 800; j++) {
/*  899 */               int ind = i * 800 + j;
/*  900 */               if (ind >= len) {
/*      */                 break;
/*      */               }
/*  903 */               str.append(",'");
/*  904 */               str.append((String)keys.get(ind));
/*  905 */               str.append("'");
/*      */             }
/*      */           }
/*  908 */           if (!str.toString().equals(""))
/*      */           {
/*      */             try
/*      */             {
/*  912 */               List data = cur_session.createQuery(hql + "(" + str.toString().substring(1) + ")").list();
/*  913 */               list.addAll(data);
/*      */             } catch (Exception e) {
/*  915 */               log.error(e + ":" + hql);
/*  916 */               ImplUtil.rollBack(tx);
/*  917 */               break;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     } catch (Exception ex) {}finally {
/*  923 */       closeSession();
/*      */     }
/*  925 */     return list;
/*      */   }
/*      */   
/*      */   static
/*      */   {
/*   54 */     addAnnotatedClass(org.jhrcore.entity.base.EntityDef.class, true);
/*   55 */     addAnnotatedClass(org.jhrcore.entity.base.EntityClass.class, true);
/*   56 */     addAnnotatedClass(FieldDef.class, true);
/*   57 */     addAnnotatedClass(org.jhrcore.entity.base.ModuleInfo.class, true);
/*   58 */     addAnnotatedClass(org.jhrcore.entity.change.ChangeScheme.class, true);
/*   59 */     addAnnotatedClass(org.jhrcore.entity.change.ChangeField.class, true);
/*   60 */     addAnnotatedClass(org.jhrcore.entity.change.ChangeItem.class, true);
/*   61 */     addAnnotatedClass(org.jhrcore.entity.change.ChangeMethod.class, true);
/*   62 */     addAnnotatedClass(AutoNoRule.class, true);
/*   63 */     addAnnotatedClass(AutoNo.class, true);
/*   64 */     addAnnotatedClass(org.jhrcore.entity.SysParameter.class, true);
/*   65 */     not_triger_packages.add("org.jhrcore.entity.analysis");
/*   66 */     not_triger_packages.add("org.jhrcore.entity.base");
/*   67 */     not_triger_packages.add("org.jhrcore.entity.change");
/*   68 */     not_triger_packages.add("org.jhrcore.entity.query");
/*   69 */     not_triger_packages.add("org.jhrcore.entity.report");
/*   70 */     not_triger_packages.add("org.jhrcore.entity.right");
/*   71 */     not_triger_packages.add("org.jhrcore.entity.showstyle");
/*   72 */     not_triger_packages.add("org.jhrcore.entity.email");
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
/*  177 */     session = new ThreadLocal();
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
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  927 */     engine = null;
/*      */     
/*      */ 
/*  930 */     ScriptEngineManager manager = new ScriptEngineManager();
/*  931 */     engine = manager.getEngineByName("js");
/*      */   }
/*      */   
/*      */   public static HashSet<String> getNot_triger_packages() {
/*  935 */     return not_triger_packages;
/*      */   }
/*      */   
/*      */   public static void entity_triger(Hashtable<String, List<FieldDef>> triger_keys, Session cur_session, Object old_e, Object e) throws Exception {
/*  939 */     if ((old_e == null) && (e == null)) {
/*  940 */       return;
/*      */     }
/*  942 */     Class entity_class = old_e == null ? e.getClass() : old_e.getClass();
/*  943 */     String entityName = entity_class.getName();
/*  944 */     if (!entityName.startsWith("org.jhrcore.entity")) {
/*  945 */       return;
/*      */     }
/*  947 */     for (String packageName : not_triger_packages) {
/*  948 */       if (entityName.startsWith(packageName)) {
/*  949 */         return;
/*      */       }
/*      */     }
/*  952 */     String cur_entity_name = entity_class.getSimpleName();
/*  953 */     List<FieldDef> listFieldDef = (List)triger_keys.get(cur_entity_name);
/*  954 */     if (listFieldDef == null) {
/*  955 */       listFieldDef = new ArrayList();
/*      */       
/*  957 */       String ent_names = cur_entity_name;
/*  958 */       entity_class = entity_class.getSuperclass();
/*  959 */       while ((!entity_class.getName().endsWith(".Model")) && (!entity_class.getName().endsWith(".Object")))
/*      */       {
/*  961 */         ent_names = ent_names + ";" + entity_class.getSimpleName();
/*  962 */         entity_class = entity_class.getSuperclass();
/*      */       }
/*  964 */       List list = new CommServiceImpl().getSysTrigerField(ent_names, true);
/*  965 */       for (Object obj : list) {
/*  966 */         FieldDef fd = (FieldDef)obj;
/*  967 */         if (fd.isRelation_save_flag()) {
/*  968 */           listFieldDef.add(fd);
/*      */         }
/*      */       }
/*      */       
/*      */ 
/*  973 */       triger_keys.put(cur_entity_name, listFieldDef);
/*      */     }
/*  975 */     if (listFieldDef.isEmpty()) {
/*  976 */       return;
/*      */     }
/*      */     
/*  979 */     if (old_e == null) {
/*  980 */       String key_field = EntityBuilder.getEntityKey(e.getClass());
/*  981 */       Object key_obj = PublicUtil.getProperty(e, key_field);
/*  982 */       if ((key_obj == null) || (!(key_obj instanceof Serializable))) {
/*  983 */         return;
/*      */       }
/*  985 */       old_e = cur_session.load(e.getClass(), (Serializable)key_obj);
/*      */     }
/*  987 */     for (int i = 0; i < listFieldDef.size(); i++) {
/*  988 */       FieldDef fd2 = (FieldDef)listFieldDef.get(i);
/*      */       
/*  990 */       if (fd2.getRelation_text() == null) {
/*      */         break;
/*      */       }
/*  993 */       if (fd2.getRelation_text().startsWith("SQL")) {
/*  994 */         String s_tmp = fd2.getRelation_text().substring(3);
/*  995 */         Object tmp_e = e == null ? old_e : e;
/*  996 */         String key_field = EntityBuilder.getEntityKey(tmp_e.getClass());
/*  997 */         Object key_val = PublicUtil.getProperty(tmp_e, key_field);
/*  998 */         key_val = "'" + key_val + "'";
/*  999 */         s_tmp = s_tmp.replaceAll("@key", "" + key_val);
/* 1000 */         cur_session.createSQLQuery(s_tmp).executeUpdate();
/*      */       } else {
/* 1002 */         engine.put("e", e);
/* 1003 */         engine.put("old_e", old_e);
/* 1004 */         engine.put("session", cur_session);
/* 1005 */         engine.put("log", log);
/* 1006 */         engine.eval(fd2.getRelation_text());
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult validateHQL(String sql, boolean update) {
/* 1012 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 1013 */     Session cur_session = currentSession();
/* 1014 */     Transaction tx = cur_session.beginTransaction();
/* 1015 */     if ((sql == null) || (sql.equals(""))) {
/* 1016 */       validateSQLResult.setMsg("SQL语句为空");
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/*      */ 
/* 1024 */         if (update) {
/* 1025 */           String[] sqls = sql.split(";");
/* 1026 */           for (String t_sql : sqls) {
/* 1027 */             cur_session.createQuery(t_sql).executeUpdate();
/*      */           }
/*      */         } else {
/* 1030 */           cur_session.createQuery(sql).list();
/*      */         }
/*      */       }
/*      */       catch (Exception e) {
/* 1034 */         validateSQLResult.setResult(1);
/* 1035 */         validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1036 */         log.error(validateSQLResult.getMsg() + "\n" + sql);
/* 1037 */         ImplUtil.rollBack(tx);
/*      */       }
/*      */     }
/* 1040 */     closeSession();
/* 1041 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult validateSQL(String sql, boolean update) {
/* 1045 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 1046 */     if ((sql == null) || (sql.equals(""))) {
/* 1047 */       validateSQLResult.setMsg("SQL语句为空");
/* 1048 */       validateSQLResult.setResult(1);
/* 1049 */       return validateSQLResult;
/*      */     }
/* 1051 */     Session cur_session = currentSession();
/* 1052 */     Transaction tx = cur_session.beginTransaction();
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */     try
/*      */     {
/* 1059 */       if (update) {
/* 1060 */         String[] sqls = sql.split(";");
/* 1061 */         for (String t_sql : sqls) {
/* 1062 */           cur_session.createSQLQuery(t_sql).executeUpdate();
/*      */         }
/*      */       } else {
/* 1065 */         cur_session.createSQLQuery(sql).list();
/*      */       }
/* 1067 */       tx.rollback();
/*      */     } catch (Exception e) {
/* 1069 */       validateSQLResult.setResult(1);
/* 1070 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1071 */       log.error(validateSQLResult.getMsg());
/* 1072 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1074 */       closeSession();
/*      */     }
/* 1076 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static ValidateSQLResult update(Object o, String hql) {
/* 1080 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 1081 */     Session cur_session = currentSession();
/* 1082 */     Transaction tx = cur_session.beginTransaction();
/*      */     try {
/* 1084 */       cur_session.createSQLQuery(hql).executeUpdate();
/* 1085 */       cur_session.flush();
/* 1086 */       BeanManager.manager(o);
/* 1087 */       entity_triger(new Hashtable(), cur_session, null, o);
/* 1088 */       hql = BeanManager.getChangeHQL(o);
/* 1089 */       if (!hql.equals("")) {
/* 1090 */         cur_session.createSQLQuery(hql).executeUpdate();
/*      */       }
/* 1092 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1094 */       validateSQLResult.setResult(1);
/* 1095 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1096 */       log.error(validateSQLResult.getMsg());
/* 1097 */       log.info("更新数据失败,时间：" + new Date() + " 数据：" + o.toString());
/* 1098 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1100 */       closeSession();
/*      */     }
/* 1102 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static String fetchNewNoBy(Session s, String autoNoRule_key, int b_inc, Hashtable<String, String> params) throws Exception
/*      */   {
/* 1107 */     String new_no = "";
/*      */     
/*      */ 
/* 1110 */     AutoNoRule anr = (AutoNoRule)s.createQuery("from AutoNoRule anr where anr.autoNoRule_id='" + autoNoRule_key + "'").uniqueResult();
/* 1111 */     if (anr != null) {
/* 1112 */       Date date = new Date();
/* 1113 */       DateFormat df = new SimpleDateFormat("yyyyMMdd");
/* 1114 */       String ymd = df.format(date);
/* 1115 */       String autoNo_key = autoNoRule_key;
/* 1116 */       String prefix = anr.isAdd_perfix() ? anr.getPerfix() : (anr.getPerfix() == null) || (anr.getPerfix().trim().equals("")) ? "''" : "''";
/* 1117 */       String tmp_s = "";
/* 1118 */       if (!anr.getNo_unit().equals("顺序编码")) {
/* 1119 */         if (anr.getNo_unit().equals("按年")) {
/* 1120 */           tmp_s = ymd.substring(0, 4);
/* 1121 */         } else if (anr.getNo_unit().equals("按月")) {
/* 1122 */           tmp_s = ymd.substring(0, 6);
/* 1123 */         } else if (anr.getNo_unit().equals("按天"))
/* 1124 */           tmp_s = ymd;
/*      */       }
/* 1126 */       prefix = prefix.replace("@年份", "'" + ymd.substring(0, 4) + "'");
/* 1127 */       prefix = prefix.replace("@月份", "'" + ymd.substring(4, 6) + "'");
/* 1128 */       prefix = prefix.replace("@日期", "'" + ymd.substring(6) + "'");
/* 1129 */       if (params != null) {
/* 1130 */         for (String key : params.keySet()) {
/* 1131 */           prefix = prefix.replace(key, (CharSequence)params.get(key));
/*      */         }
/*      */       }
/* 1134 */       if (!prefix.equals("")) {
/* 1135 */         s.createSQLQuery("update AutoNoRule set t_perfix=(" + prefix + ") where autoNoRule_id='" + anr.getAutoNoRule_id() + "'").executeUpdate();
/* 1136 */         s.flush();
/* 1137 */         SQLQuery qr = s.createSQLQuery("select t_perfix from AutoNoRule where autoNoRule_id='" + anr.getAutoNoRule_id() + "'");
/* 1138 */         qr.setMaxResults(1);
/* 1139 */         List list = qr.list();
/* 1140 */         prefix = list.get(0) == null ? "" : list.get(0).toString();
/*      */       }
/* 1142 */       prefix = prefix + tmp_s;
/* 1143 */       autoNo_key = autoNo_key + tmp_s;
/* 1144 */       AutoNo an = (AutoNo)s.createQuery("from AutoNo an where an.autoNo_key='" + autoNo_key + "'").uniqueResult();
/* 1145 */       if (an == null) {
/* 1146 */         an = new AutoNo();
/* 1147 */         an.setNew_no(anr.getInit_no());
/* 1148 */         an.setAutoNo_key(autoNo_key);
/* 1149 */       } else if (anr.getInit_no() > an.getNew_no()) {
/* 1150 */         an.setNew_no(anr.getInit_no());
/*      */       }
/* 1152 */       new_no = String.valueOf(an.getNew_no());
/* 1153 */       while (new_no.length() < anr.getNo_lenth()) {
/* 1154 */         new_no = "0" + new_no;
/*      */       }
/* 1156 */       new_no = prefix + new_no;
/* 1157 */       if (b_inc == -1) {
/* 1158 */         an.setNew_no(an.getNew_no() + anr.getInc_no());
/*      */       } else {
/* 1160 */         an.setNew_no(an.getNew_no() + b_inc);
/*      */       }
/* 1162 */       if ((anr.getNo_unit().equals("顺序编码")) && (anr.getInit_no() < an.getNew_no())) {
/* 1163 */         s.createSQLQuery("update AutoNoRule set Init_no=" + an.getNew_no() + " where autoNoRule_id='" + anr.getAutoNoRule_id() + "'").executeUpdate();
/*      */       }
/* 1165 */       s.flush();
/* 1166 */       s.saveOrUpdate(an);
/*      */     }
/* 1168 */     return new_no;
/*      */   }
/*      */   
/*      */   public static List fetchNewNoByAndAuto(Session s, String autoNoRule_key, int b_inc, Hashtable<String, String> params, Map<String, Integer> map) throws Exception {
/* 1172 */     String new_no = "";
/* 1173 */     List list_autos = new ArrayList();
/* 1174 */     AutoNoRule anr = (AutoNoRule)s.createQuery("from AutoNoRule anr where anr.autoNoRule_id='" + autoNoRule_key + "'").uniqueResult();
/* 1175 */     if (anr != null) {
/* 1176 */       Date date = new Date();
/* 1177 */       DateFormat df = new SimpleDateFormat("yyyyMMdd");
/* 1178 */       String ymd = df.format(date);
/* 1179 */       String autoNo_key = autoNoRule_key;
/* 1180 */       String prefix = anr.isAdd_perfix() ? anr.getPerfix() : (anr.getPerfix() == null) || (anr.getPerfix().trim().equals("")) ? "''" : "''";
/* 1181 */       String tmp_s = "";
/* 1182 */       if (!anr.getNo_unit().equals("顺序编码")) {
/* 1183 */         if (anr.getNo_unit().equals("按年")) {
/* 1184 */           tmp_s = ymd.substring(0, 4);
/* 1185 */         } else if (anr.getNo_unit().equals("按月")) {
/* 1186 */           tmp_s = ymd.substring(0, 6);
/* 1187 */         } else if (anr.getNo_unit().equals("按天"))
/* 1188 */           tmp_s = ymd;
/*      */       }
/* 1190 */       prefix = prefix.replace("@年份", "'" + ymd.substring(0, 4) + "'");
/* 1191 */       prefix = prefix.replace("@月份", "'" + ymd.substring(4, 6) + "'");
/* 1192 */       prefix = prefix.replace("@日期", "'" + ymd.substring(6) + "'");
/* 1193 */       if (params != null) {
/* 1194 */         for (String key : params.keySet()) {
/* 1195 */           prefix = prefix.replace(key, (CharSequence)params.get(key));
/*      */         }
/*      */       }
/* 1198 */       if (!prefix.equals("")) {
/* 1199 */         s.createSQLQuery("update AutoNoRule set t_perfix=(" + prefix + ") where autoNoRule_id='" + anr.getAutoNoRule_id() + "'").executeUpdate();
/* 1200 */         s.flush();
/* 1201 */         SQLQuery qr = s.createSQLQuery("select t_perfix from AutoNoRule where autoNoRule_id='" + anr.getAutoNoRule_id() + "'");
/* 1202 */         qr.setMaxResults(1);
/* 1203 */         List list = qr.list();
/* 1204 */         prefix = list.get(0) == null ? "" : list.get(0).toString();
/*      */       }
/* 1206 */       prefix = prefix + tmp_s;
/* 1207 */       autoNo_key = autoNo_key + tmp_s;
/* 1208 */       list_autos.add(tmp_s);
/* 1209 */       AutoNo an = (AutoNo)s.createQuery("from AutoNo an where an.autoNo_key='" + autoNo_key + "'").uniqueResult();
/* 1210 */       if (an == null) {
/* 1211 */         an = new AutoNo();
/* 1212 */         an.setNew_no(anr.getInit_no());
/* 1213 */         an.setAutoNo_key(autoNo_key);
/* 1214 */       } else if (anr.getInit_no() > an.getNew_no()) {
/* 1215 */         an.setNew_no(anr.getInit_no());
/*      */       }
/* 1217 */       if (null != map.get(tmp_s)) {
/* 1218 */         an.setNew_no(((Integer)map.get(tmp_s)).intValue());
/*      */       }
/* 1220 */       if (null != sta_map.get(tmp_s)) {
/* 1221 */         an.setNew_no(((Integer)sta_map.get(tmp_s)).intValue());
/*      */       }
/* 1223 */       new_no = String.valueOf(an.getNew_no());
/* 1224 */       while (new_no.length() < anr.getNo_lenth()) {
/* 1225 */         new_no = "0" + new_no;
/*      */       }
/* 1227 */       new_no = prefix + new_no;
/* 1228 */       if (b_inc == -1) {
/* 1229 */         an.setNew_no(an.getNew_no() + anr.getInc_no());
/*      */       } else {
/* 1231 */         an.setNew_no(an.getNew_no() + b_inc);
/*      */       }
/* 1233 */       sta_map.put(tmp_s, Integer.valueOf(b_inc));
/* 1234 */       String sql = "";
/* 1235 */       if ((anr.getNo_unit().equals("顺序编码")) && (anr.getInit_no() < an.getNew_no())) {
/* 1236 */         sql = "update AutoNoRule set Init_no=" + an.getNew_no() + " where autoNoRule_id='" + anr.getAutoNoRule_id() + "'";
/*      */       }
/* 1238 */       list_autos.add(sql);
/* 1239 */       list_autos.add(an);
/*      */     }
/* 1241 */     list_autos.add(new_no);
/* 1242 */     return list_autos;
/*      */   }
/*      */   
/*      */   public static List fetchNewNoByAndAotu(String autoNoRule_key, int b_inc, Hashtable<String, String> params, Map<String, Integer> map)
/*      */   {
/* 1247 */     Session s = currentSession();
/*      */     
/* 1249 */     List list = null;
/*      */     try {
/* 1251 */       list = fetchNewNoByAndAuto(s, autoNoRule_key, b_inc, params, map);
/*      */     }
/*      */     catch (Exception e) {
/* 1254 */       log.error(ImplUtil.getSQLExceptionMsg(e));
/*      */     } finally {
/* 1256 */       closeSession();
/*      */     }
/* 1258 */     return list;
/*      */   }
/*      */   
/*      */   public static String fetchNewNoBy(String autoNoRule_key, int b_inc, Hashtable<String, String> params)
/*      */   {
/* 1263 */     Session s = currentSession();
/* 1264 */     Transaction tx = s.beginTransaction();
/* 1265 */     String new_no = "";
/*      */     try {
/* 1267 */       new_no = fetchNewNoBy(s, autoNoRule_key, b_inc, params);
/* 1268 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1270 */       ImplUtil.rollBack(tx);
/* 1271 */       log.error(ImplUtil.getSQLExceptionMsg(e));
/*      */     } finally {
/* 1273 */       closeSession();
/*      */     }
/* 1275 */     return new_no;
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
/*      */   public static AnnotationConfiguration getAnnotationConfiguration()
/*      */   {
/* 1406 */     return annotationConfiguration;
/*      */   }
/*      */   
/*      */   public static Connection buildJDBC(String driverName, String url, String username, String password)
/*      */   {
/* 1411 */     Connection conn = null;
/*      */     try {
/* 1413 */       Class.forName(driverName).newInstance();
/* 1414 */       conn = java.sql.DriverManager.getConnection(url, username, password);
/*      */       
/* 1416 */       conn.setAutoCommit(false);
/* 1417 */       conn.setTransactionIsolation(2);
/*      */     } catch (Exception e) {
/* 1419 */       e.printStackTrace();
/* 1420 */       log.error(e.getMessage());
/*      */     }
/* 1422 */     return conn;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   public static ValidateSQLResult excuteSQLs_jdbc(String sqls, String split_char)
/*      */   {
/* 1429 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 1430 */     Connection conn = org.jhrcore.server.jdbc.JdbcUtil.getConnection();
/* 1431 */     if (conn == null) {
/* 1432 */       validateSQLResult.setResult(1);
/* 1433 */       validateSQLResult.setMsg("数据库连接失败");
/* 1434 */       return validateSQLResult;
/*      */     }
/* 1436 */     PreparedStatement ps = null;
/* 1437 */     if ((sqls != null) && (!sqls.trim().equals(""))) {
/* 1438 */       String[] ex_str = null;
/* 1439 */       if (split_char == null) {
/* 1440 */         ex_str = new String[] { sqls };
/*      */       } else {
/* 1442 */         ex_str = sqls.split(split_char);
/*      */       }
/* 1444 */       for (String tmp : ex_str) {
/* 1445 */         if (!"".equals(tmp.replace(" ", "")))
/*      */         {
/*      */           try
/*      */           {
/* 1449 */             ps = conn.prepareStatement(tmp);
/* 1450 */             ps.executeUpdate();
/*      */           } catch (SQLException e) {
/* 1452 */             log.error(e + ":" + tmp);
/* 1453 */             validateSQLResult.setMsg(validateSQLResult.getMsg() + ";\n" + tmp + ";\n" + e.toString());
/* 1454 */             validateSQLResult.setResult(1);
/*      */           }
/*      */         }
/*      */       }
/*      */       try {
/* 1459 */         if (validateSQLResult.getResult() == 0) {
/* 1460 */           conn.commit();
/*      */         } else {
/* 1462 */           conn.rollback();
/*      */         }
/*      */       } catch (SQLException ex) {
/* 1465 */         validateSQLResult.setMsg(validateSQLResult.getMsg() + ";\n commit or rollback happened error!:\n" + ex.toString());
/*      */       }
/*      */     } else {
/* 1468 */       validateSQLResult.setResult(1);
/* 1469 */       validateSQLResult.setMsg("传入空语句！");
/*      */     }
/*      */     try {
/* 1472 */       if (ps != null) {
/* 1473 */         ps.close();
/* 1474 */         ps = null;
/*      */       }
/* 1476 */       conn.close();
/*      */     } catch (SQLException ex) {
/* 1478 */       log.error(ex);
/*      */     }
/* 1480 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public static List getQueryListForMID(Session session, String[] sqls) {
/* 1484 */     List data = new ArrayList();
/* 1485 */     if ((sqls != null) && (sqls.length > 0)) {
/* 1486 */       for (String sql : sqls) {
/* 1487 */         if ((sql != null) && (!sql.trim().equals("")))
/*      */         {
/*      */           try
/*      */           {
/* 1491 */             List list = session.createSQLQuery(sql).list();
/* 1492 */             if (list != null) {
/* 1493 */               data.addAll(list);
/*      */             }
/*      */           } catch (Exception e) {
/* 1496 */             log.error(e);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1501 */     return data;
/*      */   }
/*      */   
/*      */   public static boolean exists(String hql) {
/* 1505 */     Session s = currentSession();
/* 1506 */     Transaction tx = s.beginTransaction();
/*      */     try {
/* 1508 */       hql = hql.trim();
/* 1509 */       if (hql.startsWith("from")) {
/* 1510 */         hql = "select 1 " + hql;
/*      */       }
/* 1512 */       Query query = s.createQuery(hql);
/* 1513 */       query.setMaxResults(1);
/* 1514 */       return query.list().size() > 0;
/*      */     } catch (Exception ex) {
/* 1516 */       ImplUtil.rollBack(tx);
/* 1517 */       log.error(ex);
/*      */     } finally {
/* 1519 */       closeSession();
/*      */     }
/* 1521 */     return false;
/*      */   }
/*      */   
/*      */   public static List getDb_tables(String t_str) {
/* 1525 */     Session session = currentSession();
/* 1526 */     List table_list = new ArrayList();
/*      */     try {
/* 1528 */       if ("sqlserver".equalsIgnoreCase(getDb_type())) {
/* 1529 */         table_list = session.createSQLQuery("select so.name from Sysobjects so where so.name like '" + t_str + "%'").addScalar("name", Hibernate.STRING).list();
/* 1530 */       } else if ("mysql".equalsIgnoreCase(getDb_type())) {
/* 1531 */         table_list = session.createSQLQuery("select table_name from information_schema.tables where table_type='base table' and table_name like '" + t_str + "%'").addScalar("name", Hibernate.STRING).list();
/*      */       } else {
/* 1533 */         table_list = session.createSQLQuery("select table_name from all_tab_comments where table_name like '" + t_str.toUpperCase() + "%' and table_type ='TABLE' and owner ='" + getSQL_user().toUpperCase() + "'").list();
/*      */       }
/*      */     } catch (Exception e) {
/* 1536 */       log.error(e);
/*      */     } finally {
/* 1538 */       closeSession();
/*      */     }
/* 1540 */     return table_list;
/*      */   }
/*      */   
/*      */   public static void addAnnotatedClass(Class c, boolean force) {
/* 1544 */     if ((!force) && ((c == null) || (EntityBuilder.getHt_entity_classes().containsKey(c.getSimpleName())))) {
/* 1545 */       return;
/*      */     }
/* 1547 */     if (c.getAnnotation(javax.persistence.Entity.class) == null) {
/* 1548 */       System.out.println("NotEntity:" + c);
/* 1549 */       return;
/*      */     }
/* 1551 */     System.out.println("addAnnotatedClass:" + c);
/* 1552 */     log.info("addAnnotatedClass:" + c);
/* 1553 */     getAnnotationConfiguration().addAnnotatedClass(c);
/* 1554 */     EntityBuilder.getHt_entity_classes().put(c.getSimpleName(), c.getName());
/*      */   }
/*      */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\HibernateUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */