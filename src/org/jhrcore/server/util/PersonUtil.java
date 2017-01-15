/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.hibernate.Query;
/*     */ import org.hibernate.SQLQuery;
/*     */ import org.hibernate.Session;
/*     */ import org.jhrcore.entity.AutoNo;
/*     */ import org.jhrcore.entity.AutoNoRule;
/*     */ import org.jhrcore.entity.BasePersonAppendix;
/*     */ import org.jhrcore.entity.DeptCode;
/*     */ import org.jhrcore.server.HibernateUtil;
/*     */ import org.jhrcore.util.SysUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class PersonUtil
/*     */ {
/*  25 */   private static Logger log = Logger.getLogger(PersonUtil.class);
/*     */   
/*     */   public static String getNewFuntionCode(String parent_code) {
/*  28 */     String tmp2 = "000000000000000000000000000000000000000000";
/*  29 */     Session s = HibernateUtil.currentSession();
/*     */     try {
/*  31 */       if ((parent_code == null) || (parent_code.equalsIgnoreCase("ROOT"))) {
/*  32 */         String tmp = (String)s.createSQLQuery("select max(fun_code) from FuntionRight ").uniqueResult();
/*  33 */         if (tmp == null) {
/*  34 */           tmp2 = tmp2 + "1";
/*     */         } else {
/*  36 */           int len = tmp.length();
/*  37 */           if (len > 2) {
/*  38 */             int tmp_num = Integer.valueOf(tmp.substring(len - 2)).intValue() + 1;
/*  39 */             tmp2 = tmp2 + tmp_num;
/*     */           } else {
/*  41 */             tmp2 = tmp2 + (Integer.valueOf(tmp).intValue() + 1);
/*     */           }
/*     */         }
/*     */       } else {
/*  45 */         String tmp = (String)s.createSQLQuery("select max(fun_code) from FuntionRight r where r.fun_parent_code='" + parent_code + "'").uniqueResult();
/*  46 */         if (tmp == null) {
/*  47 */           tmp2 = tmp2 + "1";
/*     */         } else {
/*  49 */           int len = tmp.length();
/*  50 */           if (len > 2) {
/*  51 */             int tmp_num = Integer.valueOf(tmp.substring(len - 2)).intValue() + 1;
/*  52 */             tmp2 = tmp2 + tmp_num;
/*     */           } else {
/*  54 */             tmp2 = tmp2 + (Integer.valueOf(tmp).intValue() + 1);
/*     */           }
/*     */         }
/*     */       }
/*  58 */       tmp2 = tmp2.substring(tmp2.length() - 2);
/*     */       
/*     */ 
/*     */ 
/*     */ 
/*  63 */       return parent_code + tmp2;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
                log.error(e.getMessage());
                e.printStackTrace();
                }finally {} 
                return parent_code + tmp2;
/*     */   }
/*     */   
/*     */   public static Object getFieldObject(String entity_name, BasePersonAppendix obj, String field_name)
/*     */   {
/*  68 */     Object object = null;
/*     */     try {
/*  70 */       Class aclass = Class.forName("org.jhrcore.entity." + entity_name);
/*  71 */       Method method = aclass.getMethod("get" + field_name.substring(0, 1).toUpperCase() + field_name.substring(1), new Class[0]);
/*  72 */       object = method.invoke(obj, new Object[0]);
/*     */     } catch (Exception ex) {
/*  74 */       log.error(ex);
/*     */     }
/*  76 */     return object;
/*     */   }
/*     */   
/*     */   public static String getDbFieldName(String fieldName, boolean isChange) {
/*  80 */     if (fieldName.equalsIgnoreCase("deptCode")) {
/*  81 */       fieldName = "deptCode_key";
/*  82 */     } else if (fieldName.equalsIgnoreCase("g10")) {
/*  83 */       fieldName = "g10_key";
/*     */     } else {
/*  85 */       fieldName = SysUtil.tranField(fieldName);
/*     */     }
/*  87 */     if (isChange)
/*     */     {
/*     */ 
/*     */ 
/*  91 */       fieldName = "new_" + fieldName;
/*     */     }
/*  93 */     return fieldName;
/*     */   }
/*     */   
/*     */   public static String getPersonNo(Session s, DeptCode dc, String entityName, int inc_no) throws Exception {
/*  97 */     List data = s.createQuery("select autoNoRule_id from AutoNoRule anr where anr.autoNoRule_id like 'PersonNo_%' and (anr.autoNoRule_id like '%_" + entityName + "' or anr.autoNoRule_id like '%_A01')").list();
/*  98 */     String rule_key = "";
/*  99 */     String rule_all_key = "";
/* 100 */     String start_name = dc.getDept_code();
/* 101 */     for (Object obj : data) {
/* 102 */       String[] row_data = obj.toString().split("_");
/* 103 */       if (start_name.startsWith(row_data[1])) {
/* 104 */         if (row_data[2].equals(entityName)) {
/* 105 */           rule_key = obj.toString();
/* 106 */           break; }
/* 107 */         if (row_data[2].equals("A01")) {
/* 108 */           rule_all_key = obj.toString();
/*     */         }
/*     */       }
/*     */     }
/* 112 */     if (rule_key.equals("")) {
/* 113 */       rule_key = rule_all_key;
/*     */     }
/* 115 */     if (rule_key.equals("")) {
/* 116 */       return "";
/*     */     }
/* 118 */     Hashtable<String, String> params = new Hashtable();
/* 119 */     params.put("@???????", "'" + dc.getDept_code() + "'");
/* 120 */     String autono = HibernateUtil.fetchNewNoBy(s, rule_key, inc_no, params);
/* 121 */     return autono;
/*     */   }
/*     */   
/*     */   public static String getNewChangeNo(AutoNoRule anr, AutoNo an, String prefix) {
/* 125 */     String new_no = String.valueOf(an.getNew_no());
/* 126 */     while (new_no.length() < anr.getNo_lenth()) {
/* 127 */       new_no = "0" + new_no;
/*     */     }
/* 129 */     new_no = prefix + new_no;
/* 130 */     an.setNew_no(an.getNew_no() + 1);
/* 131 */     if ((anr.getNo_unit().equals("??????")) && (anr.getInit_no() < an.getNew_no())) {
/* 132 */       anr.setInit_no(an.getNew_no());
/*     */     }
/* 134 */     return new_no;
/*     */   }
/*     */ }
