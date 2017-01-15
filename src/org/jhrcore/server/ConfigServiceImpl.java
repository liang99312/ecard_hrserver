/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.rmi.RemoteException;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import javax.ejb.Remote;
/*     */ import javax.ejb.Stateless;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.jboss.annotation.ejb.Clustered;
/*     */ import org.jhrcore.iservice.ConfigService;
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
/*     */ @Clustered
/*     */ @Stateless
/*     */ @Remote({ConfigService.class})
/*     */ public class ConfigServiceImpl
/*     */   extends SuperImpl
/*     */   implements ConfigService
/*     */ {
/*  34 */   protected static final Logger log = Logger.getLogger(ConfigServiceImpl.class.getSimpleName());
/*  35 */   private static Properties props = null;
/*     */   
/*  37 */   public ConfigServiceImpl() throws RemoteException { super(ServerApp.rmiPort);
/*  38 */     props = loadPropertiesFrom(System.getProperty("user.dir") + "/kaoqin" + ".properties");
/*     */   }
/*     */   
/*     */   public ConfigServiceImpl(int port) throws RemoteException
/*     */   {
/*  43 */     super(port);
/*  44 */     props = loadPropertiesFrom(System.getProperty("user.dir") + "/kaoqin" + ".properties");
/*     */   }
/*     */   
/*     */   public static void save()
/*     */   {
/*  49 */     File file = new File(System.getProperty("user.dir") + "/kaoqin" + ".properties");
/*     */     try
/*     */     {
/*  52 */       props.store(new FileOutputStream(file), "");
/*     */     } catch (IOException e) {
/*  54 */       log.log(log.getPriority(), "Can not save properties file");
/*     */     }
/*     */   }
/*     */   
/*     */   private static Properties loadPropertiesFrom(String cfgFileName) {
/*  59 */     Properties properties = new Properties();
/*  60 */     File file = new File(cfgFileName);
/*  61 */     if (!file.exists()) {
/*     */       try {
/*  63 */         file.createNewFile();
/*     */       } catch (IOException e) {
/*  65 */         e.printStackTrace();
/*     */       } catch (Exception e) {
/*  67 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/*  72 */       properties.load(new BufferedInputStream(new FileInputStream(file)));
/*     */     } catch (FileNotFoundException e) {
/*  74 */       e.printStackTrace();
/*     */     } catch (IOException e) {
/*  76 */       e.printStackTrace();
/*     */     }
/*  78 */     return properties;
/*     */   }
/*     */   
/*     */   public Properties getProperties() throws RemoteException
/*     */   {
/*  83 */     if (props == null) {
/*  84 */       props = loadPropertiesFrom(System.getProperty("user.dir") + "/kaoqin" + ".properties");
/*     */     }
/*  86 */     return props;
/*     */   }
/*     */   
/*     */   public static Properties getKProperties() {
/*  90 */     Properties props2 = loadPropertiesFrom(System.getProperty("user.dir") + "/kaoqin" + ".properties");
/*     */     
/*  92 */     return props2;
/*     */   }
/*     */   
/*     */   public void saveProperties(Properties properties) throws RemoteException
/*     */   {
/*  97 */     if (props == null) {
/*  98 */       props = loadPropertiesFrom(System.getProperty("user.dir") + "/kaoqin" + ".properties");
/*     */     }
/* 100 */     Set<String> set = properties.stringPropertyNames();
/* 101 */     for (String key : set) {
/* 102 */       props.setProperty(key, properties.getProperty(key));
/*     */     }
/* 104 */     save();
/*     */   }
/*     */   
/*     */   public static void saveKQProperties(Properties properties) throws RemoteException {
/* 108 */     if (props == null) {
/* 109 */       props = loadPropertiesFrom(System.getProperty("user.dir") + "/kaoqin" + ".properties");
/*     */     }
/* 111 */     Set<String> set = properties.stringPropertyNames();
/* 112 */     for (String key : set) {
/* 113 */       props.setProperty(key, properties.getProperty(key));
/*     */     }
/* 115 */     save();
/*     */   }
/*     */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\ConfigServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */