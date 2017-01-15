/*    */ package org.jhrcore.server.util;
/*    */ 
/*    */ import java.util.Hashtable;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class CountUtil
/*    */ {
/* 15 */   public static int gz_import_num = 0;
/* 16 */   public static int user_view_num = 0;
/* 17 */   private static int comm_num = 0;
/* 18 */   private static int kcomm_num = 0;
/*    */   
/* 20 */   public static Hashtable<String, Integer> user_view_keys = new Hashtable();
/* 21 */   private static Hashtable<String, Integer> kqtables = new Hashtable();
/*    */   
/*    */   public static synchronized String getCommTempTable() {
/* 24 */     comm_num += 1;
/* 25 */     if (comm_num > 10000) {
/* 26 */       comm_num = 1;
/*    */     }
/* 28 */     return "HRTEMP_" + comm_num;
/*    */   }
/*    */   
/*    */   public static synchronized String getKQTempTable(String person_code) {
/* 32 */     Integer i = (Integer)kqtables.get(person_code);
/* 33 */     if (i == null) {
/* 34 */       kcomm_num += 1;
/* 35 */       i = Integer.valueOf(kcomm_num);
/* 36 */       kqtables.put(person_code, i);
/*    */     }
/* 38 */     return "KaA01_" + i;
/*    */   }
/*    */   
/*    */   public static synchronized void removeKQTempTable(Integer num) {
/* 42 */     kqtables.remove(num);
/*    */   }
/*    */   
/*    */   public static synchronized String getGzTempTable() {
/* 46 */     gz_import_num += 1;
/* 47 */     if (gz_import_num > 100000) {
/* 48 */       gz_import_num = 1;
/*    */     }
/* 50 */     return "HRGZ_" + gz_import_num;
/*    */   }
/*    */   
/*    */   public static synchronized int getUser_view_num(String a01_key) {
/* 54 */     Integer i = (Integer)user_view_keys.get(a01_key);
/* 55 */     if (i == null) {
/* 56 */       user_view_num += 1;
/* 57 */       i = Integer.valueOf(user_view_num);
/* 58 */       user_view_keys.put(a01_key, i);
/*    */     }
/* 60 */     return i.intValue();
/*    */   }
/*    */ }
