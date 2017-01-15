/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import it.sauronsoftware.base64.Base64;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
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
/*     */ public class Base64Utils
/*     */ {
/*     */   private static final int CACHE_SIZE = 1024;
/*     */   
/*     */   public static byte[] decode(String base64)
/*     */     throws Exception
/*     */   {
/*  51 */     return Base64.decode(base64.getBytes());
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static String encode(byte[] bytes)
/*     */     throws Exception
/*     */   {
/*  64 */     return new String(Base64.encode(bytes));
/*     */   }
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
/*     */   public static String encodeFile(String filePath)
/*     */     throws Exception
/*     */   {
/*  80 */     byte[] bytes = fileToByte(filePath);
/*  81 */     return encode(bytes);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void decodeToFile(String filePath, String base64)
/*     */     throws Exception
/*     */   {
/*  94 */     byte[] bytes = decode(base64);
/*  95 */     byteArrayToFile(bytes, filePath);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static byte[] fileToByte(String filePath)
/*     */     throws Exception
/*     */   {
/* 108 */     byte[] data = new byte[0];
/* 109 */     File file = new File(filePath);
/* 110 */     if (file.exists()) {
/* 111 */       FileInputStream in = new FileInputStream(file);
/* 112 */       ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
/* 113 */       byte[] cache = new byte['Ѐ'];
/* 114 */       int nRead = 0;
/* 115 */       while ((nRead = in.read(cache)) != -1) {
/* 116 */         out.write(cache, 0, nRead);
/* 117 */         out.flush();
/*     */       }
/* 119 */       out.close();
/* 120 */       in.close();
/* 121 */       data = out.toByteArray();
/*     */     }
/* 123 */     return data;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void byteArrayToFile(byte[] bytes, String filePath)
/*     */     throws Exception
/*     */   {
/* 135 */     InputStream in = new ByteArrayInputStream(bytes);
/* 136 */     File destFile = new File(filePath);
/* 137 */     if (!destFile.getParentFile().exists()) {
/* 138 */       destFile.getParentFile().mkdirs();
/*     */     }
/* 140 */     destFile.createNewFile();
/* 141 */     OutputStream out = new FileOutputStream(destFile);
/* 142 */     byte[] cache = new byte['Ѐ'];
/* 143 */     int nRead = 0;
/* 144 */     while ((nRead = in.read(cache)) != -1) {
/* 145 */       out.write(cache, 0, nRead);
/* 146 */       out.flush();
/*     */     }
/* 148 */     out.close();
/* 149 */     in.close();
/*     */   }
/*     */ }
