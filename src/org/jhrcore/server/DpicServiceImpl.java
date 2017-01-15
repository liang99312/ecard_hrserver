/*    */ package org.jhrcore.server;
/*    */ 
/*    */ import java.io.BufferedInputStream;
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.rmi.RemoteException;
/*    */ import java.util.HashMap;
/*    */ import org.apache.log4j.Logger;
/*    */ import org.jhrcore.iservice.DpicService;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class DpicServiceImpl
/*    */   extends SuperImpl
/*    */   implements DpicService
/*    */ {
/* 21 */   private Logger log = Logger.getLogger(PersonServiceImpl.class.getName());
/*    */   
/* 23 */   private static String picture_dir = System.getProperty("user.dir") + "/" + "pic";
/*    */   
/*    */   public DpicServiceImpl() throws RemoteException {
/* 26 */     super(ServerApp.rmiPort);
/*    */   }
/*    */   
/* 29 */   public DpicServiceImpl(int port) throws RemoteException { super(port); }
/*    */   
/*    */   public byte[] downloadPicture(String pic_path)
/*    */     throws RemoteException
/*    */   {
/* 34 */     if ((pic_path == null) || (pic_path.equals(""))) {
/* 35 */       return null;
/*    */     }
/*    */     try {
/* 38 */       File file = new File(picture_dir + "/" + pic_path);
/* 39 */       if ((file == null) || (!file.exists())) {
/* 40 */         this.log.error("file not exists:" + picture_dir + "/" + pic_path);
/* 41 */         return null;
/*    */       }
/* 43 */       byte[] buffer = new byte[(int)file.length()];
/* 44 */       BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
/*    */       
/* 46 */       input.read(buffer, 0, buffer.length);
/* 47 */       input.close();
/* 48 */       return buffer;
/*    */     } catch (Exception e) {
/* 50 */       this.log.error(e); }
/* 51 */     return null;
/*    */   }
/*    */   
/*    */   public HashMap getDbConfig()
/*    */     throws RemoteException
/*    */   {
/* 57 */     HashMap dbCfg = new HashMap();
/* 58 */     String url = HibernateUtil.getSQL_url();
/* 59 */     String DBUrl = url.substring(url.indexOf("//") + 2);
/* 60 */     String uid = HibernateUtil.getSQL_user();
/* 61 */     String pwd = HibernateUtil.getSQL_pass();
/* 62 */     dbCfg.put("DBUid", uid);
/* 63 */     dbCfg.put("DBPwd", pwd);
/* 64 */     dbCfg.put("DBUrl", DBUrl);
/* 65 */     return dbCfg;
/*    */   }
/*    */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\DpicServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */