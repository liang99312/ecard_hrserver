/*    */ package org.jhrcore.server.util;
/*    */ 
/*    */ import java.io.BufferedInputStream;
/*    */ import java.io.InputStream;
/*    */ import java.sql.Blob;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class BlobUtil
/*    */ {
/*    */   public static byte[] getBlobByte(Blob blob)
/*    */     throws Exception
/*    */   {
/* 17 */     InputStream is = null;
/* 18 */     byte[] buffer = null;
/*    */     try {
/* 20 */       is = blob.getBinaryStream();
/* 21 */       buffer = new byte[(int)blob.length()];
/* 22 */       BufferedInputStream input = new BufferedInputStream(is);
/* 23 */       input.read(buffer, 0, buffer.length);
/* 24 */       input.close();
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
/* 35 */       return buffer;
/*    */     }
/*    */     catch (Exception ex)
/*    */     {
/* 26 */       throw ex;
/*    */     } finally {
/*    */       try {
/* 29 */         if (is != null) {
/* 30 */           is.close();
/*    */         }
/*    */       }
/*    */       catch (Exception ex) {}
/*    */     }
/*    */   }
/*    */ }
