/*    */ package org.jhrcore.server.jdbc;
/*    */ 
/*    */ import com.mchange.v2.c3p0.ComboPooledDataSource;
/*    */ import java.sql.Connection;
/*    */ import java.sql.PreparedStatement;
/*    */ import org.apache.log4j.Logger;
/*    */ import org.jhrcore.server.HibernateUtil;
/*    */ import org.jhrcore.util.DbUtil;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class JdbcUtil
/*    */ {
/* 20 */   private static Logger log = Logger.getLogger(JdbcUtil.class);
/* 21 */   private static ComboPooledDataSource cpds = null;
/*    */   
/*    */   public static synchronized Connection getConnection() {
/* 24 */     if (cpds == null) {
/* 25 */       cpds = new ComboPooledDataSource();
/*    */       try {
/* 27 */         cpds.setDriverClass(HibernateUtil.getSQL_driver());
/*    */       } catch (Exception e1) {
/* 29 */         e1.printStackTrace();
/*    */       }
/* 31 */       cpds.setJdbcUrl(HibernateUtil.getSQL_url());
/* 32 */       cpds.setUser(HibernateUtil.getSQL_user());
/* 33 */       cpds.setPassword(HibernateUtil.getSQL_pass());
/*    */       
/* 35 */       cpds.setMaxPoolSize(50);
/* 36 */       cpds.setAcquireIncrement(1);
/* 37 */       cpds.setMaxIdleTime(600);
/* 38 */       cpds.setMaxStatements(0);
/* 39 */       cpds.setMaxStatementsPerConnection(0);
/* 40 */       cpds.setIdleConnectionTestPeriod(300);
/* 41 */       cpds.setInitialPoolSize(1);
/* 42 */       cpds.setTestConnectionOnCheckin(false);
/* 43 */       cpds.setTestConnectionOnCheckout(false);
/*    */     }
/* 45 */     Connection c = null;
/*    */     try {
/* 47 */       c = cpds.getConnection();
/*    */     }
/*    */     catch (Exception ex) {}
/* 50 */     return c;
/*    */   }
/*    */   
/*    */   public static void doNOWaitWork(String ex_sql) throws Exception {
/* 54 */     Connection c = getConnection();
/*    */     try {
/* 56 */       c.setAutoCommit(true);
/* 57 */       String[] sqls = ex_sql.split(";");
/* 58 */       for (String tmp : sqls)
/* 59 */         if (!tmp.trim().equals(""))
/*    */         {
/*    */ 
/* 62 */           PreparedStatement stmt = null;
/*    */           try {
/* 64 */             stmt = c.prepareStatement(tmp);
/* 65 */             stmt.executeUpdate();
/*    */           } catch (Exception ex) {
/* 67 */             log.error("ErrorSQL:" + tmp);
/* 68 */             if (stmt != null) {
/*    */               try {
/* 70 */                 stmt.close();
/*    */               }
/*    */               catch (Exception e) {}
/*    */             }
/* 74 */             throw ex;
/*    */           }
/*    */         }
/*    */     } catch (Exception ex) {
/* 78 */       throw ex;
/*    */     } finally {
/* 80 */       DbUtil.closeConnection(c);
/*    */     }
/*    */   }
/*    */ }
