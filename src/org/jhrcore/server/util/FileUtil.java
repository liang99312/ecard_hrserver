/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import javax.swing.JFileChooser;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class FileUtil
/*     */ {
/*     */   public static void saveFile(String nr)
/*     */   {
/*  28 */     JFileChooser jf = new JFileChooser();
/*  29 */     jf.setFileSelectionMode(1);
/*  30 */     jf.showDialog(null, null);
/*  31 */     File fi = jf.getSelectedFile();
/*  32 */     String f = fi.getAbsolutePath() + "\\wflicense";
/*  33 */     System.out.println("save: " + f);
/*     */     try {
/*  35 */       writeTxt(f, nr);
/*     */     }
/*     */     catch (Exception e) {}
/*     */   }
/*     */   
/*     */   public static void saveFile(byte[] nr, String key) {
/*  41 */     JFileChooser jf = new JFileChooser();
/*  42 */     jf.setFileSelectionMode(1);
/*  43 */     jf.showDialog(null, null);
/*  44 */     File fi = jf.getSelectedFile();
/*  45 */     String f = fi.getAbsolutePath() + "\\wflicense";
/*  46 */     String k = fi.getAbsolutePath() + "\\wfkey";
/*  47 */     System.out.println("save: " + f);
/*     */     try {
/*  49 */       writeByteFile(f, nr);
/*  50 */       writeTxt(k, key);
/*     */     }
/*     */     catch (Exception e) {}
/*     */   }
/*     */   
/*     */   public static String readTxt(String fileName) {
/*     */     try {
/*  57 */       String s = "";
/*  58 */       File file = new File(fileName);
/*  59 */       if ((file.isFile()) && (file.exists())) {
/*  60 */         InputStreamReader read = new InputStreamReader(new FileInputStream(file));
/*  61 */         BufferedReader br = new BufferedReader(read);
/*  62 */         String lineTXT = null;
/*  63 */         while ((lineTXT = br.readLine()) != null) {
/*  64 */           s = s + lineTXT;
/*     */         }
/*  66 */         read.close();
/*  67 */         return s;
/*     */       }
/*     */     } catch (Exception e) {
/*  70 */       e.printStackTrace();
/*     */     }
/*  72 */     return "";
/*     */   }
/*     */   
/*     */   public static void writeTxt(String fileName, String nr)
/*     */   {
/*     */     try {
/*  78 */       File file = new File(fileName);
/*  79 */       if (!file.exists()) {
/*  80 */         file.createNewFile();
/*     */       }
/*  82 */       FileWriter fw = new FileWriter(file);
/*  83 */       BufferedWriter bw = new BufferedWriter(fw);
/*  84 */       bw.write(nr);
/*  85 */       bw.flush();
/*  86 */       bw.close();
/*  87 */       fw.close();
/*     */     } catch (Exception e) {
/*  89 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static byte[] readByteFile(String filePath)
/*     */   {
/*  97 */     byte[] buffer = null;
/*     */     try {
/*  99 */       File file = new File(filePath);
/* 100 */       FileInputStream fis = new FileInputStream(file);
/* 101 */       ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
/* 102 */       byte[] b = new byte['Ϩ'];
/*     */       int n;
/* 104 */       while ((n = fis.read(b)) != -1) {
/* 105 */         bos.write(b, 0, n);
/*     */       }
/* 107 */       fis.close();
/* 108 */       bos.close();
/* 109 */       buffer = bos.toByteArray();
/*     */     } catch (FileNotFoundException e) {
/* 111 */       e.printStackTrace();
/*     */     } catch (IOException e) {
/* 113 */       e.printStackTrace();
/*     */     }
/* 115 */     return buffer;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void writeByteFile(String fileName, byte[] bfile)
/*     */   {
/* 122 */     BufferedOutputStream bos = null;
/* 123 */     FileOutputStream fos = null;
/* 124 */     File file = null;
/*     */     try {
/* 126 */       file = new File(fileName);
/* 127 */       fos = new FileOutputStream(file);
/* 128 */       bos = new BufferedOutputStream(fos);
/* 129 */       bos.write(bfile); return;
/*     */     } catch (Exception e) {
/* 131 */       e.printStackTrace();
/*     */     } finally {
/* 133 */       if (bos != null) {
/*     */         try {
/* 135 */           bos.close();
/*     */         } catch (IOException e1) {
/* 137 */           e1.printStackTrace();
/*     */         }
/*     */       }
/* 140 */       if (fos != null) {
/*     */         try {
/* 142 */           fos.close();
/*     */         } catch (IOException e1) {
/* 144 */           e1.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ }
