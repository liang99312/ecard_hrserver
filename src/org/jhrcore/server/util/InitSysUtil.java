/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import de.simplicit.vjdbc.server.config.ConfigurationException;
/*     */ import de.simplicit.vjdbc.server.config.VJdbcConfiguration;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.rmi.Remote;
/*     */ import java.util.HashMap;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.dom4j.Document;
/*     */ import org.dom4j.DocumentHelper;
/*     */ import org.dom4j.Element;
/*     */ import org.dom4j.io.OutputFormat;
/*     */ import org.dom4j.io.XMLWriter;
/*     */ import org.jhrcore.client.UserContext;
/*     */ import org.jhrcore.entity.SysParameter;
/*     */ import org.jhrcore.entity.right.Role;
/*     */ import org.jhrcore.server.CommServiceImpl;
/*     */ import org.jhrcore.server.ConfigServiceImpl;
/*     */ import org.jhrcore.server.DataServiceImpl;
/*     */ import org.jhrcore.server.DeptServiceImpl;
/*     */ import org.jhrcore.server.DpicServiceImpl;
/*     */ import org.jhrcore.server.EcardServiceImpl;
/*     */ import org.jhrcore.server.EventServiceImpl;
/*     */ import org.jhrcore.server.FileServiceImpl;
/*     */ import org.jhrcore.server.HibernateUtil;
/*     */ import org.jhrcore.server.IDeplymentImpl;
/*     */ import org.jhrcore.server.ImportServiceImpl;
/*     */ import org.jhrcore.server.PersonServiceImpl;
/*     */ import org.jhrcore.server.PersonServiceImplForSQL;
/*     */ import org.jhrcore.server.ReportServiceImpl;
/*     */ import org.jhrcore.server.RightServiceImpl;
/*     */ import org.jhrcore.server.SysServiceImpl;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class InitSysUtil
/*     */ {
/*  47 */   private static Logger log = Logger.getLogger(InitSysUtil.class.getSimpleName());
/*  48 */   private static HashMap<String, Class> services = new HashMap();
/*     */   
/*     */   public static void initSystem() {
/*  51 */     initVJdbcConfiguration();
/*  52 */     initServices();
/*  53 */     initSysMan();
/*  54 */     initSysRole();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   private static void initVJdbcConfiguration()
/*     */   {
/*  62 */     Document document = DocumentHelper.createDocument();
/*     */     
/*  64 */     Element configuration = document.addElement("vjdbc-configuration");
/*     */     
/*  66 */     Element sessionfactory = configuration.addElement("connection");
/*  67 */     sessionfactory.addAttribute("id", "testdb");
/*  68 */     sessionfactory.addAttribute("driver", HibernateUtil.getSQL_driver());
/*  69 */     sessionfactory.addAttribute("url", HibernateUtil.getSQL_url());
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*  78 */     XMLWriter writer = null;
/*     */     
/*  80 */     OutputFormat format = new OutputFormat();
/*  81 */     format.setEncoding("utf-8");
/*  82 */     format.setIndent(true);
/*  83 */     format.setLineSeparator("\n");
/*  84 */     format.setNewlines(true);
/*     */     try {
/*  86 */       writer = new XMLWriter(format);
/*  87 */       ByteArrayOutputStream bo = new ByteArrayOutputStream();
/*  88 */       writer.setOutputStream(bo);
/*  89 */       writer.write(document);
/*  90 */       writer.flush();
/*  91 */       ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
/*  92 */       VJdbcConfiguration.init(bi, null);
/*  93 */       bi.close();
/*  94 */       bo.close(); return;
/*     */     } catch (ConfigurationException e) {
/*  96 */       log.error(e);
/*     */     } catch (UnsupportedEncodingException e) {
/*  98 */       log.error(e);
/*     */     } catch (FileNotFoundException e) {
/* 100 */       log.error(e);
/*     */     } catch (IOException e) {
/* 102 */       log.error(e);
/*     */     } finally {
/* 104 */       if (writer != null) {
/*     */         try {
/* 106 */           writer.close();
/*     */         } catch (IOException e) {
/* 108 */           log.error(e);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   private static void initServices()
/*     */   {
/* 118 */     addService(IDeplymentImpl.class);
/* 119 */     addService(DataServiceImpl.class);
/* 120 */     addService(CommServiceImpl.class);
/* 121 */     addService(ReportServiceImpl.class);
/* 122 */     addService(DeptServiceImpl.class);
/* 123 */     addService(PersonServiceImplForSQL.class);
/* 124 */     addService(PersonServiceImpl.class);
/* 125 */     addService(ConfigServiceImpl.class);
/* 126 */     addService(ImportServiceImpl.class);
/* 127 */     addService(RightServiceImpl.class);
/* 128 */     addService(SysServiceImpl.class);
/* 129 */     addService(DpicServiceImpl.class);
/* 130 */     addService(FileServiceImpl.class);
/* 131 */     addService(EcardServiceImpl.class);
/* 132 */     addService(EventServiceImpl.class);
/*     */   }
/*     */   
/*     */   private static void addService(Class c) {
/* 136 */     String serviceTag = "";
/* 137 */     for (Class cs : c.getInterfaces()) {
/* 138 */       if (Remote.class.isAssignableFrom(cs)) {
/* 139 */         serviceTag = cs.getSimpleName();
/* 140 */         break;
/*     */       }
/*     */     }
/* 143 */     if (serviceTag.equals("")) {
/* 144 */       return;
/*     */     }
/* 146 */     if (UserContext.sql_dialect.endsWith("sqlserver")) {
/* 147 */       if (c.getSimpleName().endsWith("ForSQL")) {
/* 148 */         services.put(serviceTag, c);
/* 149 */       } else { if (services.get(serviceTag) != null) {
/* 150 */           return;
/*     */         }
/* 152 */         services.put(serviceTag, c);
/*     */       }
/* 154 */     } else { if (c.getSimpleName().endsWith("ForSQL")) {
/* 155 */         return;
/*     */       }
/* 157 */       services.put(serviceTag, c);
/*     */     }
/*     */   }
/*     */   
/*     */   private static void initSysMan() {
/* 162 */     SysParameter sp = (SysParameter)HibernateUtil.fetchEntityBy("from SysParameter sp where sp.sysParameter_key='SysManPass'");
/* 163 */     if (sp == null) {
/* 164 */       sp = new SysParameter();
/* 165 */       sp.setSysParameter_key("SysManPass");
/* 166 */       sp.setSysparameter_name("SysManPass");
/* 167 */       sp.setSysparameter_value("");
/* 168 */       sp.setSysparameter_code("sa");
/* 169 */       HibernateUtil.saveOrUpdate(sp);
/*     */     }
/* 171 */     SysParameter sysParameter = (SysParameter)HibernateUtil.fetchEntityBy("from SysParameter sp where sp.sysParameter_key='HRTableUpdate'");
/* 172 */     if (sysParameter == null) {
/* 173 */       HibernateUtil.excuteSQL("insert into SysParameter(SysParameter_key,Sysparameter_code,Sysparameter_name,Sysparameter_value) VALUES('HRTableUpdate','HRTableUpdate','HRTableUpdate','0')");
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   private static void initSysRole()
/*     */   {
/* 182 */     Role roleSelf = (Role)HibernateUtil.fetchEntityBy("from Role r where r.role_key = '&&&' ");
/* 183 */     if (roleSelf == null) {
/* 184 */       roleSelf = new Role();
/* 185 */       roleSelf.setRole_key("&&&");
/* 186 */       roleSelf.setRole_code("01");
/* 187 */       roleSelf.setParent_code("ROOT");
/* 188 */       roleSelf.setRole_name("员工自助");
/*     */       
/* 190 */       HibernateUtil.saveOrUpdate(roleSelf);
/*     */     }
/*     */     
/* 193 */     Role roleOutUser = (Role)HibernateUtil.fetchEntityBy("from Role r where r.role_key = 'OutUser' ");
/* 194 */     if (roleOutUser == null) {
/* 195 */       roleOutUser = new Role();
/* 196 */       roleOutUser.setRole_key("OutUser");
/* 197 */       roleOutUser.setRole_code("02");
/* 198 */       roleOutUser.setParent_code("ROOT");
/* 199 */       roleOutUser.setRole_name("外部用户");
/*     */       
/* 201 */       HibernateUtil.saveOrUpdate(roleOutUser);
/*     */     }
/*     */   }
/*     */   
/*     */   public static HashMap<String, Class> getServices() {
/* 206 */     return services;
/*     */   }
/*     */ }
