/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import com.fr.view.core.DateUtil;
/*     */ import java.io.File;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import org.jhrcore.comm.ConfigManager;
/*     */ import org.jhrcore.server.ResDialog;
/*     */ import org.jhrcore.ui.ContextManager;
/*     */ import org.jhrcore.util.SysUtil;
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
/*     */ public class SecuryUtil
/*     */ {
/*  26 */   private static String licName = "wflicense";
/*  27 */   private static String keyName = "wfkey";
/*     */   
/*     */   public static boolean isVilidate() {
/*  30 */     boolean result = true;
/*     */     try
/*     */     {
/*  33 */       File keyFile = new File(keyName);
/*  34 */       File licFile = new File(licName);
/*  35 */       if ((!keyFile.exists()) || (!licFile.exists())) {
/*  36 */         showDialog("");
/*  37 */         return false;
/*     */       }
/*  39 */       if (!isTrueDate()) {
/*  40 */         showDialog("");
/*  41 */         return false;
/*     */       }
/*  43 */       String key = FileUtil.readTxt(keyName);
/*  44 */       byte[] encodedData = FileUtil.readByteFile(licName);
/*  45 */       byte[] decodedData = RSAUtils.decryptByPublicKey(encodedData, key);
/*  46 */       String target = new String(decodedData);
/*  47 */       String[] strs = target.split("@@@");
/*  48 */       if (strs.length == 0) {
/*  49 */         showDialog("");
/*  50 */         return false;
/*     */       }
/*  52 */       if ("sy".equals(strs[0])) {
/*  53 */         org.jhrcore.server.CommServiceImpl.bbType = "试用版";
/*  54 */         if (!isPassDate(SysUtil.objToInt(strs[1]))) {
/*  55 */           showDialog("试用期已过，请注册软件，谢谢合作！");
/*  56 */           return false;
/*     */         }
/*     */       } else {
/*  59 */         if (strs.length != 4) {
/*  60 */           showDialog("");
/*  61 */           return false;
/*     */         }
/*  63 */         String cpumac = CommUtil.getCPUSerialId() + CommUtil.getMACAddress();
/*  64 */         if (!cpumac.equals(strs[1])) {
/*  65 */           showDialog("");
/*  66 */           return false;
/*     */         }
/*  68 */         if (!isPassDate(strs[2], SysUtil.objToInt(strs[3]))) {
/*  69 */           showDialog("有效使用期已过，请注册软件，谢谢合作！");
/*  70 */           return false;
/*     */         }
/*     */       }
/*  73 */       return result;
/*     */     } catch (Exception ex) {
/*  75 */       Logger.getLogger(SecuryUtil.class.getName()).log(Level.SEVERE, null, ex);
/*  76 */       result = false;
/*     */     }
/*  78 */     return result;
/*     */   }
/*     */   
/*     */   public static boolean isTrueDate() {
/*  82 */     boolean result = true;
/*  83 */     Date dqDate = new Date();
/*  84 */     String dqd = DesUtil.decryptBasedDes(ConfigManager.getConfigManager().getProperty("dqd"));
/*  85 */     if ((dqd == null) || ("".equals(dqd))) {
/*  86 */       ConfigManager.getConfigManager().setProperty("dqd", DesUtil.encryptBasedDes(DateUtil.DateToStr(dqDate, "yyyy-MM-dd")));
/*  87 */       ConfigManager.getConfigManager().save2();
/*  88 */       result = true;
/*     */     } else {
/*  90 */       ConfigManager.getConfigManager().setProperty("dqd", DesUtil.encryptBasedDes(DateUtil.DateToStr(dqDate, "yyyy-MM-dd")));
/*  91 */       ConfigManager.getConfigManager().save2();
/*  92 */       Date sdDate = DateUtil.StrToDate(dqd);
/*  93 */       if (dqDate.before(sdDate)) {
/*  94 */         return false;
/*     */       }
/*     */     }
/*  97 */     return result;
/*     */   }
/*     */   
/*     */   public static boolean isPassDate(int ts) {
/* 101 */     boolean result = true;
/* 102 */     Date dqDate = new Date();
/* 103 */     String syd = DesUtil.decryptBasedDes(ConfigManager.getConfigManager().getProperty("syd"));
/* 104 */     if ((syd == null) || ("".equals(syd))) {
/* 105 */       ConfigManager.getConfigManager().setProperty("syd", DesUtil.encryptBasedDes(DateUtil.DateToStr(dqDate, "yyyy-MM-dd")));
/* 106 */       ConfigManager.getConfigManager().save2();
/* 107 */       result = true;
/*     */     } else {
/* 109 */       Date stDate = DateUtil.StrToDate(syd);
/* 110 */       Calendar c = Calendar.getInstance();
/* 111 */       c.setTime(stDate);
/* 112 */       c.add(5, ts);
/* 113 */       if (c.getTime().before(dqDate)) {
/* 114 */         return false;
/*     */       }
/*     */     }
/* 117 */     return result;
/*     */   }
/*     */   
/*     */   public static boolean isPassDate(String qyd, int ts) {
/* 121 */     boolean result = true;
/* 122 */     Date dqDate = new Date();
/* 123 */     Date stDate = DateUtil.StrToDate(qyd);
/* 124 */     Calendar c = Calendar.getInstance();
/* 125 */     c.setTime(stDate);
/* 126 */     c.add(5, ts);
/* 127 */     if (c.getTime().before(dqDate)) {
/* 128 */       return false;
/*     */     }
/* 130 */     return result;
/*     */   }
/*     */   
/*     */   public static void showDialog(String string) {
/* 134 */     String t = "".equals(string) ? "请注册软件，谢谢合作！" : string;
/* 135 */     ResDialog dlg = new ResDialog();
/* 136 */     dlg.setTitle(t);
/* 137 */     ContextManager.locateOnMainScreenCenter(dlg);
/* 138 */     dlg.setVisible(true);
/*     */   }
/*     */ }
