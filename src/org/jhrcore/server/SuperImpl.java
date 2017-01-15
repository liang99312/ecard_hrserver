/*    */ package org.jhrcore.server;
/*    */ 
/*    */ import java.rmi.RemoteException;
/*    */ import java.rmi.server.UnicastRemoteObject;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SuperImpl
/*    */   extends UnicastRemoteObject
/*    */ {
/*    */   protected SuperImpl()
/*    */     throws RemoteException
/*    */   {
/* 17 */     super(ServerApp.rmiPort);
/*    */   }
/*    */   
/*    */   protected SuperImpl(int port) throws RemoteException {
/* 21 */     super(port);
/*    */   }
/*    */   
/*    */   public long getServerStartTime() throws RemoteException {
/* 25 */     return ServerApp.getServerStartTime();
/*    */   }
/*    */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\SuperImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */