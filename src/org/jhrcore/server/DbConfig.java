/*    */ package org.jhrcore.server;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class DbConfig
/*    */   implements Serializable
/*    */ {
/*    */   private String url;
/*    */   private String driver;
/*    */   private String user;
/*    */   private String pass;
/*    */   private String dialect;
/*    */   
/*    */   public String getDialect()
/*    */   {
/* 22 */     return this.dialect;
/*    */   }
/*    */   
/*    */   public void setDialect(String dialect) {
/* 26 */     this.dialect = dialect;
/*    */   }
/*    */   
/*    */   public String getDriver() {
/* 30 */     return this.driver;
/*    */   }
/*    */   
/*    */   public void setDriver(String driver) {
/* 34 */     this.driver = driver;
/*    */   }
/*    */   
/*    */   public String getPass() {
/* 38 */     return this.pass;
/*    */   }
/*    */   
/*    */   public void setPass(String pass) {
/* 42 */     this.pass = pass;
/*    */   }
/*    */   
/*    */   public String getUrl() {
/* 46 */     return this.url;
/*    */   }
/*    */   
/*    */   public void setUrl(String url) {
/* 50 */     this.url = url;
/*    */   }
/*    */   
/*    */   public String getUser() {
/* 54 */     return this.user;
/*    */   }
/*    */   
/*    */   public void setUser(String user) {
/* 58 */     this.user = user;
/*    */   }
/*    */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\DbConfig.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */