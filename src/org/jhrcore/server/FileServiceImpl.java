/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.rmi.RemoteException;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import javax.ejb.Remote;
/*     */ import javax.ejb.Stateless;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.jboss.annotation.ejb.Clustered;
/*     */ import org.jhrcore.entity.FileRecord;
/*     */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*     */ import org.jhrcore.iservice.FileService;
/*     */ import org.jhrcore.util.FileUtil;
/*     */ import org.jhrcore.util.SysUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @Clustered
/*     */ @Stateless
/*     */ @Remote({FileService.class})
/*     */ public class FileServiceImpl
/*     */   extends SuperImpl
/*     */   implements FileService
/*     */ {
/*  33 */   private static Logger log = Logger.getLogger(FileServiceImpl.class);
/*     */   public static String file_dir;
/*     */   
/*     */   public FileServiceImpl() throws RemoteException {
/*  37 */     super(ServerApp.rmiPort);
/*  38 */     file_dir = System.getProperty("user.dir") + "/" + "filerecord";
/*     */   }
/*     */   
/*     */   public FileServiceImpl(int port) throws RemoteException {
/*  42 */     super(port);
/*  43 */     file_dir = System.getProperty("user.dir") + "/" + "filerecord";
/*     */   }
/*     */   
/*     */   public ValidateSQLResult uploadNewFile(byte[] buffer, FileRecord fr) throws RemoteException
/*     */   {
/*     */     try {
/*  49 */       String fileName = SysUtil.objToStr(fr.getFile_type());
/*  50 */       fileName = "." + fileName;
/*  51 */       File file = new File(file_dir + File.separator + fr.getFile_path());
/*  52 */       if (!file.getParentFile().exists()) {
/*  53 */         file.getParentFile().mkdirs();
/*     */       }
/*  55 */       BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
/*     */       
/*  57 */       output.write(buffer);
/*  58 */       output.close();
/*     */     } catch (Exception e) {
/*  60 */       ValidateSQLResult result = new ValidateSQLResult();
/*  61 */       result.setResult(1);
/*  62 */       result.setMsg("文件写入失败");
/*  63 */       log.error(e);
/*  64 */       return result;
/*     */     }
/*  66 */     fr.setC_date(new Date());
/*  67 */     return HibernateUtil.save(fr);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult uploadOverFile(byte[] buffer, FileRecord fr) throws RemoteException
/*     */   {
/*  72 */     throw new UnsupportedOperationException("Not supported yet.");
/*     */   }
/*     */   
/*     */   public ValidateSQLResult uploadFiles(List<byte[]> buffer, FileRecord fr) throws RemoteException
/*     */   {
/*  77 */     throw new UnsupportedOperationException("Not supported yet.");
/*     */   }
/*     */   
/*     */   public byte[] downloadFileByID(String recoreId) throws RemoteException
/*     */   {
/*  82 */     List list = HibernateUtil.selectSQL("select file_path from FileRecord where fileRecord_key='" + recoreId + "'", false, 1);
/*  83 */     if ((list.isEmpty()) || (list.get(0) == null) || (list.get(0).toString().trim().equals(""))) {
/*  84 */       return null;
/*     */     }
/*  86 */     File file = new File(file_dir + File.separator + list.get(0).toString());
/*  87 */     if (file.exists()) {
/*  88 */       return FileUtil.readFileToByte(file);
/*     */     }
/*  90 */     return null;
/*     */   }
/*     */   
/*     */   public HashMap<String, byte[]> downloadFileBySrcID(String srcId) throws RemoteException
/*     */   {
/*  95 */     throw new UnsupportedOperationException("Not supported yet.");
/*     */   }
/*     */   
/*     */   public FileRecord getRecordByID(String recoreId) throws RemoteException
/*     */   {
/* 100 */     throw new UnsupportedOperationException("Not supported yet.");
/*     */   }
/*     */   
/*     */   public List<FileRecord> getRecordBySrcID(String srcId) throws RemoteException
/*     */   {
/* 105 */     throw new UnsupportedOperationException("Not supported yet.");
/*     */   }
/*     */   
/*     */   public ValidateSQLResult deleteFileByID(String recoreId) throws RemoteException
/*     */   {
/* 110 */     ValidateSQLResult result = new ValidateSQLResult();
/* 111 */     List list = HibernateUtil.selectSQL("select file_path from FileRecord where fileRecord_key='" + recoreId + "'", false, 1);
/* 112 */     if ((list.isEmpty()) || (list.get(0) == null) || (list.get(0).toString().trim().equals(""))) {
/* 113 */       return result;
/*     */     }
/*     */     try {
/* 116 */       String path = (String)list.get(0);
/* 117 */       File file = new File(file_dir + File.separator + path);
/* 118 */       if (file.exists()) {
/* 119 */         file.delete();
/*     */       }
/*     */     } catch (Exception ex) {
/* 122 */       log.error(ex);
/* 123 */       result.setResult(1);
/* 124 */       result.setMsg("删除失败");
/* 125 */       return result;
/*     */     }
/* 127 */     String sql = "delete from FileRecord where fileRecord_key='" + recoreId + "'";
/* 128 */     return HibernateUtil.excuteSQL(sql);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult deleteFileBySrcID(String srcId) throws RemoteException
/*     */   {
/* 133 */     throw new UnsupportedOperationException("Not supported yet.");
/*     */   }
/*     */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\FileServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */