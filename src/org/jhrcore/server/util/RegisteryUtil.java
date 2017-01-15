/*    */ package org.jhrcore.server.util;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import java.util.prefs.Preferences;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class RegisteryUtil
/*    */ {
/* 15 */   String[] keys = { "start_date", "end_date", "dq_date" };
/* 16 */   String[] values = { "2016-03-03", "2", "3" };
/*    */   
/*    */ 
/*    */ 
/*    */   public void writeValue()
/*    */   {
/* 22 */     Preferences pre = Preferences.systemRoot().node("/javaplayer");
/* 23 */     for (int i = 0; i < this.keys.length; i++)
/*    */     {
/* 25 */       System.out.println(pre.get(this.keys[i], null));
/*    */     }
/*    */   }
/*    */   
/*    */   public static void main(String[] args) {
/* 30 */     RegisteryUtil reg = new RegisteryUtil();
/* 31 */     reg.writeValue();
/*    */   }
/*    */ }

