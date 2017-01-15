/*    */ package org.jhrcore.server.italk;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.net.ServerSocket;
/*    */ import java.net.Socket;
/*    */ 
/*    */ public class SMRMISocket extends java.rmi.server.RMISocketFactory
/*    */ {
/*  9 */   private int port = 1099;
/*    */   
/*    */   public SMRMISocket() {}
/*    */   
/* 13 */   public SMRMISocket(int port) { this.port = port; }
/*    */   
/*    */ 
/*    */   public Socket createSocket(String host, int port)
/*    */     throws IOException
/*    */   {
/* 19 */     return new Socket(host, port);
/*    */   }
/*    */   
/*    */   public ServerSocket createServerSocket(int port)
/*    */     throws IOException
/*    */   {
/* 25 */     if (port == 0) {
/* 26 */       port = this.port;
/*    */     }
/* 28 */     return new ServerSocket(port);
/*    */   }
/*    */ }
