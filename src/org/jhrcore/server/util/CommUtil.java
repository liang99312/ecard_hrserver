/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.net.InetAddress;
/*     */ import java.net.NetworkInterface;
/*     */ import java.net.SocketException;
/*     */ import java.net.UnknownHostException;
/*     */ import java.util.Scanner;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class CommUtil
/*     */ {
/*     */   public static String setBRString(String string)
/*     */   {
/*  28 */     int len = string.length();
/*  29 */     if (len < 48) {
/*  30 */       return string;
/*     */     }
/*  32 */     int rowLength = 48;
/*  33 */     StringBuilder result = new StringBuilder();
/*  34 */     result.append(string);
/*  35 */     int rows = (len - 1) / rowLength + 1;
/*  36 */     if (rows > 1) {
/*  37 */       for (int i = rows - 1; i > 0; i--) {
/*  38 */         int index = i * rowLength;
/*  39 */         result.insert(index, "\n");
/*     */       }
/*     */     }
/*  42 */     return result.toString();
/*     */   }
/*     */   
/*     */   public static String getCpuSerial() {
/*  46 */     String serial = "-1";
/*     */     try {
/*  48 */       Process process = Runtime.getRuntime().exec(new String[] { "wmic", "cpu", "get", "ProcessorId" });
/*     */       
/*  50 */       process.getOutputStream().close();
/*  51 */       Scanner sc = new Scanner(process.getInputStream());
/*  52 */       return sc.next();
/*     */     }
/*     */     catch (IOException ex) {
/*  55 */       Logger.getLogger(CommUtil.class.getName()).log(Level.SEVERE, null, ex);
/*     */     }
/*  57 */     return serial;
/*     */   }
/*     */   
/*     */   public static String getSerialNumber(String drive) {
/*  61 */     String result = "-1";
/*     */     try {
/*  63 */       File file = File.createTempFile("realhowto", ".vbs");
/*  64 */       file.deleteOnExit();
/*  65 */       FileWriter fw = new FileWriter(file);
/*  66 */       String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\nSet colDrives = objFSO.Drives\nSet objDrive = colDrives.item(\"" + drive + "\")\n" + "Wscript.Echo objDrive.SerialNumber";
/*     */       
/*     */ 
/*     */ 
/*  70 */       fw.write(vbs);
/*  71 */       fw.close();
/*  72 */       Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
/*  73 */       BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
/*     */       
/*     */ 
/*  76 */       String sn = "";
/*  77 */       String line; while ((line = input.readLine()) != null) {
/*  78 */         sn = sn + line;
/*     */       }
/*  80 */       input.close();
/*  81 */       result = sn.trim();
/*     */     } catch (Exception e) {
/*  83 */       e.printStackTrace();
/*     */     }
/*  85 */     return result;
/*     */   }
/*     */   
/*     */   public static String getMACAddress()
/*     */   {
/*  90 */     String result = "-1";
/*     */     try {
/*  92 */       InetAddress ia = InetAddress.getLocalHost();
/*  93 */       byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
/*  94 */       StringBuilder sb = new StringBuilder();
/*     */       
/*  96 */       for (int i = 0; i < mac.length; i++) {
/*  97 */         if (i != 0) {
/*  98 */           sb.append("-");
/*     */         }
/* 100 */         String s = Integer.toHexString(mac[i] & 0xFF);
/* 101 */         sb.append(s.length() == 1 ? 0 + s : s);
/*     */       }
/* 103 */       result = sb.toString().toUpperCase();
/*     */     } catch (UnknownHostException ex) {
/* 105 */       Logger.getLogger(CommUtil.class.getName()).log(Level.SEVERE, null, ex);
/*     */     } catch (SocketException ex) {
/* 107 */       Logger.getLogger(CommUtil.class.getName()).log(Level.SEVERE, null, ex);
/*     */     } finally {
/* 109 */       if ((result == null) || ("-1".equals(result)) || (result.trim().length() < 1)) {
/* 110 */         result = "5C-52-4F-4E-8E-E9";
/* 111 */         return result;
/*     */       }
/*     */     }
/* 114 */     return result;
/*     */   }
/*     */   
/*     */   public static String getCPUSerialId() {
/* 118 */     String result = "";
/*     */     try {
/* 120 */       File file = File.createTempFile("tmp", ".vbs");
/* 121 */       file.deleteOnExit();
/* 122 */       FileWriter fw = new FileWriter(file);
/* 123 */       String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\nSet colItems = objWMIService.ExecQuery _ \n   (\"Select * from Win32_Processor\") \nFor Each objItem in colItems \n    Wscript.Echo objItem.ProcessorId \n    exit for  ' do the first cpu only! \nNext \n";
/*     */       
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 131 */       fw.write(vbs);
/* 132 */       fw.close();
/* 133 */       Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
/*     */       
/* 135 */       BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
/*     */       
/*     */       String line;
/* 138 */       while ((line = input.readLine()) != null) {
/* 139 */         result = result + line;
/*     */       }
/* 141 */       input.close();
/* 142 */       file.delete();
/*     */     } catch (Exception e) {
/* 144 */       e.fillInStackTrace();
/*     */     }
/* 146 */     if (result.trim().length() < 1) {
/* 147 */       result = "p11se7nu31ido0cl".toUpperCase();
/*     */     }
/* 149 */     String ss = result.trim();
/* 150 */     if (ss.length() % 2 == 1) {
/* 151 */       ss = ss + "f";
/*     */     }
/* 153 */     int len = ss.length() / 2;
/* 154 */     String res = "";
/* 155 */     for (int i = 0; i < len; i++) {
/* 156 */       res = res + ss.substring(i * 2, i * 2 + 2) + "-";
/*     */     }
/* 158 */     return res;
/*     */   }
/*     */ }
