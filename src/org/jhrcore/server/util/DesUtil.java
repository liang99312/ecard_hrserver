/*    */ package org.jhrcore.server.util;
/*    */ 
/*    */ import java.security.SecureRandom;
/*    */ import javax.crypto.Cipher;
/*    */ import javax.crypto.SecretKey;
/*    */ import javax.crypto.SecretKeyFactory;
/*    */ import javax.crypto.spec.DESKeySpec;
/*    */ import sun.misc.BASE64Decoder;
/*    */ import sun.misc.BASE64Encoder;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class DesUtil
/*    */ {
/* 22 */   private static final byte[] DES_KEY = { 21, 1, -110, 82, -32, -85, Byte.MIN_VALUE, -65 };
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static String encryptBasedDes(String data)
/*    */   {
/* 31 */     String encryptedData = null;
/*    */     try
/*    */     {
/* 34 */       SecureRandom sr = new SecureRandom();
/* 35 */       DESKeySpec deskey = new DESKeySpec(DES_KEY);
/*    */       
/* 37 */       SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
/* 38 */       SecretKey key = keyFactory.generateSecret(deskey);
/*    */       
/* 40 */       Cipher cipher = Cipher.getInstance("DES");
/* 41 */       cipher.init(1, key, sr);
/*    */       
/* 43 */       encryptedData = new BASE64Encoder().encode(cipher.doFinal(data.getBytes()));
/*    */     }
/*    */     catch (Exception e) {
/* 46 */       throw new RuntimeException("加密错误，错误信息：", e);
/*    */     }
/* 48 */     return encryptedData;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static String decryptBasedDes(String cryptData)
/*    */   {
/* 59 */     if ((cryptData == null) || ("".equals(cryptData))) {
/* 60 */       return "";
/*    */     }
/* 62 */     String decryptedData = null;
/*    */     try
/*    */     {
/* 65 */       SecureRandom sr = new SecureRandom();
/* 66 */       DESKeySpec deskey = new DESKeySpec(DES_KEY);
/*    */       
/* 68 */       SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
/* 69 */       SecretKey key = keyFactory.generateSecret(deskey);
/*    */       
/* 71 */       Cipher cipher = Cipher.getInstance("DES");
/* 72 */       cipher.init(2, key, sr);
/*    */       
/* 74 */       decryptedData = new String(cipher.doFinal(new BASE64Decoder().decodeBuffer(cryptData)));
/*    */     }
/*    */     catch (Exception e) {
/* 77 */       throw new RuntimeException("解密错误，错误信息：", e);
/*    */     }
/* 79 */     return decryptedData;
/*    */   }
/*    */ }
