/*      */ package org.jhrcore.server;
/*      */ 
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedOutputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.rmi.RemoteException;
/*      */ import java.text.DateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashSet;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Timer;
import java.util.TimerTask;
/*      */ import org.apache.log4j.Logger;
/*      */ import org.hibernate.MappingException;
/*      */ import org.hibernate.Query;
/*      */ import org.hibernate.SQLQuery;
/*      */ import org.hibernate.Session;
/*      */ import org.hibernate.Transaction;
/*      */ import org.hibernate.tool.hbm2ddl.SchemaUpdate;
/*      */ import org.jhrcore.comm.CodeManager;
/*      */ import org.jhrcore.comm.ConfigManager;
/*      */ import org.jhrcore.entity.AutoNo;
/*      */ import org.jhrcore.entity.AutoNoRule;
/*      */ import org.jhrcore.entity.Code;
/*      */ import org.jhrcore.entity.DeptCode;
/*      */ import org.jhrcore.entity.ExportScheme;
/*      */ import org.jhrcore.entity.FormulaDetail;
/*      */ import org.jhrcore.entity.base.EntityClass;
/*      */ import org.jhrcore.entity.base.EntityDef;
/*      */ import org.jhrcore.entity.base.FieldDef;
/*      */ import org.jhrcore.entity.base.LoginUser;
/*      */ import org.jhrcore.entity.base.ModuleInfo;
/*      */ import org.jhrcore.entity.change.ChangeItem;
/*      */ import org.jhrcore.entity.change.ChangeScheme;
/*      */ import org.jhrcore.entity.query.CommAnalyseScheme;
/*      */ import org.jhrcore.entity.query.QueryAnalysisScheme;
/*      */ import org.jhrcore.entity.query.QueryPart;
/*      */ import org.jhrcore.entity.query.QueryScheme;
/*      */ import org.jhrcore.entity.right.FuntionRight;
/*      */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*      */ import org.jhrcore.entity.showstyle.ShowFieldGroup;
/*      */ import org.jhrcore.entity.showstyle.ShowScheme;
/*      */ import org.jhrcore.entity.showstyle.ShowSchemeGroup;
/*      */ import org.jhrcore.iservice.CommService;
/*      */ import org.jhrcore.rebuild.EntityBuilder;
/*      */ import org.jhrcore.server.util.ImplUtil;
/*      */ import org.jhrcore.server.util.RebuildUtil;
/*      */ import org.jhrcore.util.DbUtil;
/*      */ import org.jhrcore.util.PublicUtil;
/*      */ import org.jhrcore.util.SysUtil;
/*      */ 
/*      */ @org.jboss.annotation.ejb.Clustered
/*      */ @javax.ejb.Stateless
/*      */ @javax.ejb.Remote({CommService.class})
/*      */ public class CommServiceImpl extends SuperImpl implements CommService
/*      */ {
/*   66 */   private static Logger log = Logger.getLogger(CommServiceImpl.class);
/*   67 */   public static Hashtable<String, List<LoginUser>> loginUserKeys = new Hashtable();
/*      */   
/*      */ 
/*      */ 
/*   71 */   private static long serverCheckTime = 60000L;
/*   72 */   private static long sessionTime = 3600000L;
/*   73 */   public static boolean u_flag = false;
/*   74 */   public static List codes = new ArrayList();
/*   75 */   public static Hashtable<String, Object> moduleKeys = new Hashtable();
/*   76 */   public static boolean moduleChange = true;
/*   77 */   public static String bbType = "";
/*      */   private static final long serialVersionUID = 1L;
/*      */   
static {
        Timer serverTimer = new Timer();
        serverTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                manageClient();
            }
        }, new Date(), serverCheckTime);
    }
/*      */ 
/*      */   public static void update_data(String fileName)
/*      */   {
/*   91 */     File f = new File(fileName);
/*   92 */     if (f.exists()) {
/*   93 */       String s = org.jhrcore.server.util.FileUtil.readTxt(fileName);
/*   94 */       ValidateSQLResult result = HibernateUtil.excuteSQLs(s, ";");
/*   95 */       if (result.getResult() == 0) {
/*   96 */         f.delete();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public static void rebuild_model() {
/*  102 */     HibernateUtil.getSessionFactory();
/*  103 */     SchemaUpdate schemaUpdate = new SchemaUpdate(HibernateUtil.getAnnotationConfiguration());
/*  104 */     schemaUpdate.execute(true, true);
/*      */     
/*  106 */     update_data("update.txt");
/*  107 */     List<ModuleInfo> list = HibernateUtil.fetchEntitiesBy("from ModuleInfo a left join fetch a.entityClasss b left join fetch b.entityDefs c left join fetch c.fieldDefs where a.used=1 order by a.order_no");
/*  108 */     EntityBuilder.buildEntities(list);
/*  109 */     HashSet<String> pay_field_keys = new HashSet();
/*      */     
/*  111 */     String entitycaption = "工资基本信息表";
/*  112 */     Object obj = HibernateUtil.fetchEntityBy("select entityCaption from EntityDef where entityName='C21'");
/*  113 */     if (obj != null) {
/*  114 */       entitycaption = obj.toString();
/*      */     }
/*      */     
/*  117 */     HibernateUtil.closeSessionFactory();
/*  118 */     for (ModuleInfo mi : list) {
/*  119 */       String packageName = EntityBuilder.getPackage(mi);
/*  120 */       for (int i = 0; i < mi.getEntityClasss().size(); i++) {
/*  121 */         EntityClass entityClass = (EntityClass)mi.getEntityClasss().toArray()[i];
/*  122 */         for (int j = 0; j < entityClass.getEntityDefs().size(); j++) {
/*  123 */           EntityDef ed = (EntityDef)entityClass.getEntityDefs().toArray()[j];
/*  124 */           if (ed.isSuccess_build())
/*      */           {
/*      */ 
/*  127 */             if (HibernateUtil.getAnnotationConfiguration().getClassMapping(packageName + ed.getEntityName()) == null)
/*      */             {
/*      */               try
/*      */               {
/*      */ 
/*  132 */                 Class<?> theClass = Class.forName(packageName + ed.getEntityName());
/*  133 */                 log.info("addAnnotatedClass:" + ed.getEntityName() + "-" + theClass);
/*  134 */                 HibernateUtil.addAnnotatedClass(theClass, true);
/*      */               } catch (Exception e) {
/*  136 */                 e.printStackTrace();
/*      */               } }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  142 */     Set<Class<?>> cs = SysUtil.getAllClasses("org.jhrcore.entity");
/*  143 */     for (Class c : cs) {
/*      */       try {
/*  145 */         HibernateUtil.addAnnotatedClass(c, false);
/*      */       } catch (MappingException e) {
/*  147 */         e.printStackTrace();
/*      */       }
/*      */     }
/*  150 */     Object updateTable = HibernateUtil.fetchEntityBy("select sysparameter_value from SysParameter where sysParameter_key='HRTableUpdate'");
/*  151 */     if ((updateTable == null) || ("1".equals(updateTable))) {
/*  152 */       u_flag = true;
/*  153 */       schemaUpdate = new SchemaUpdate(HibernateUtil.getAnnotationConfiguration());
/*  154 */       schemaUpdate.execute(true, true);
/*  155 */       HibernateUtil.excuteSQL("update SysParameter set sysparameter_value='0' where sysParameter_key='HRTableUpdate'");
/*  156 */       update_data("update_data.txt");
/*      */     } else {
/*  158 */       u_flag = false;
/*      */     }
/*  160 */     EntityBuilder.putInitClass();
/*  161 */     System.out.println("提示：数据服务器启动成功");
/*      */   }
/*      */   
/*      */   public static void rebuildCache() {
/*  165 */     rebuildModules();
/*  166 */     rebuildCodes(null);
/*      */   }
/*      */   
/*      */   private static synchronized void rebuildModules() {
/*  170 */     if (!moduleChange) {
/*  171 */       return;
/*      */     }
/*  173 */     List<ModuleInfo> list = HibernateUtil.fetchEntitiesBy("from ModuleInfo a left join fetch a.entityClasss b left join fetch b.entityDefs c left join fetch c.fieldDefs where a.used=1 order by a.order_no");
/*  174 */     for (Object obj : list) {
/*  175 */       ModuleInfo mi = (ModuleInfo)obj;
/*  176 */       moduleKeys.put("module" + mi.getModule_code(), obj);
/*  177 */       for (EntityClass ec : mi.getEntityClasss()) {
/*  178 */         moduleKeys.put("class" + ec.getEntityType_code(), ec);
/*  179 */         for (Iterator i$ = ec.getEntityDefs().iterator(); i$.hasNext();) {EntityDef ed = (EntityDef)i$.next();
/*  180 */           moduleKeys.put("entity" + ed.getEntityName(), ed);
/*  181 */           for (FieldDef fd : ed.getFieldDefs())
/*  182 */             moduleKeys.put(ed.getEntityName() + "_" + fd.getField_name(), fd);
/*      */         }
/*      */       } }
/*      */     Iterator i$;
/*      */     EntityDef ed;
/*  187 */     moduleChange = false;
/*      */   }
/*      */   
/*      */   public static synchronized void rebuildCodes(Code code) { int index;
/*  191 */     if (code == null) {
/*  192 */       codes.clear();
/*  193 */       codes.addAll(HibernateUtil.fetchEntitiesBy("from Code  where used=1 order by code_tag"));
/*      */     } else {
/*  195 */       String codeType = code.getCode_type();
/*  196 */       List list = HibernateUtil.fetchEntitiesBy("from Code  where used=1 and code_type='" + codeType + "' order by code_tag desc");
/*  197 */       List removes = new ArrayList();
/*  198 */       index = -1;
/*  199 */       int size = codes.size();
/*  200 */       for (int i = 0; i < size; i++) {
/*  201 */         Code c = (Code)codes.get(i);
/*  202 */         if (c.getCode_type().equals(codeType)) {
/*  203 */           if (index == -1) {
/*  204 */             index = i;
/*      */           }
/*  206 */           removes.add(c);
/*      */         }
/*      */       }
/*  209 */       codes.removeAll(removes);
/*  210 */       for (Object obj : list) {
/*  211 */         codes.add(index, obj);
/*      */       }
/*      */     }
/*  214 */     CodeManager.getCodeManager().fillCodes(codes);
/*      */   }
/*      */   
/*      */   public String getBbType() throws RemoteException
/*      */   {
/*  219 */     return bbType;
/*      */   }
/*      */   
/*      */   public List getSysModule(boolean fetchClass, boolean fetchEntity, boolean fetchField) throws RemoteException
/*      */   {
/*  224 */     if (moduleChange) {
/*  225 */       rebuildModules();
/*      */     }
/*  227 */     List result = new ArrayList();
/*  228 */     List<String> modulefields; List classfields; List entityfields; if ((fetchClass) && (fetchEntity) && (fetchField)) {
/*  229 */       for (String key : moduleKeys.keySet()) {
/*  230 */         if (key.startsWith("module")) {
/*  231 */           result.add(moduleKeys.get(key));
/*      */         }
/*      */       }
/*      */     } else {
/*  235 */       modulefields = EntityBuilder.getCommFieldNameListOf(ModuleInfo.class, EntityBuilder.COMM_FIELD_ALL);
/*  236 */       modulefields.remove("entityClasss");
/*  237 */       classfields = EntityBuilder.getCommFieldNameListOf(EntityClass.class, EntityBuilder.COMM_FIELD_ALL);
/*  238 */       classfields.remove("entityDefs");
/*  239 */       classfields.remove("moduleInfo");
/*  240 */       entityfields = EntityBuilder.getCommFieldNameListOf(EntityDef.class, EntityBuilder.COMM_FIELD_ALL);
/*  241 */       entityfields.remove("fieldDefs");
/*  242 */       entityfields.remove("entityClass");
/*  243 */       for (String key : moduleKeys.keySet()) {
/*  244 */         if (key.startsWith("module")) {
/*  245 */           ModuleInfo srcModule = (ModuleInfo)moduleKeys.get(key);
/*  246 */           ModuleInfo dstModule = new ModuleInfo();
/*  247 */           PublicUtil.copyProperties(srcModule, dstModule, modulefields, modulefields);
/*  248 */           result.add(dstModule);
/*  249 */           if (fetchClass)
/*  250 */             for (EntityClass srcEc : srcModule.getEntityClasss()) {
/*  251 */               EntityClass dstEc = new EntityClass();
/*  252 */               PublicUtil.copyProperties(srcEc, dstEc, classfields, classfields);
/*  253 */               dstEc.setModuleInfo(dstModule);
/*  254 */               dstModule.getEntityClasss().add(dstEc);
/*  255 */               if (fetchEntity)
/*  256 */                 for (EntityDef srcEd : srcEc.getEntityDefs()) {
/*  257 */                   EntityDef dstEd = new EntityDef();
/*  258 */                   PublicUtil.copyProperties(srcEd, dstEd, entityfields, entityfields);
/*  259 */                   dstEd.setEntityClass(dstEc);
/*  260 */                   dstEc.getEntityDefs().add(dstEd);
/*      */                 }
/*      */             }
/*      */         }
/*      */       }
/*      */     }
/*      */     ModuleInfo dstModule;
/*      */     EntityClass dstEc;
/*  268 */     SysUtil.sortListByInteger(result, "order_no");
/*  269 */     return result;
/*      */   }
/*      */   
/*      */   private static void manageClient() {
/*  273 */     if (loginUserKeys.keySet().isEmpty()) {
/*  274 */       return;
/*      */     }
/*  276 */     long testTime = System.currentTimeMillis();
/*  277 */     for (String key : loginUserKeys.keySet()) {
/*  278 */       List<LoginUser> lus = (List)loginUserKeys.get(key);
/*  279 */       for (LoginUser lu : lus) {
/*  280 */         if ((!lu.getUser_code().equals("-1")) && (testTime - lu.getLast_visit_time() > sessionTime)) {
/*  281 */           lu.setUser_state("超时");
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private static void buildChangeScheme()
/*      */   {
/*  293 */     List<ChangeScheme> list_ChangeSchemes = new ArrayList();
/*  294 */     List cslist = HibernateUtil.fetchEntitiesBy("from ChangeScheme cs left join fetch cs.changeItems where cs.changeScheme_key in('EmpScheme_Add','EmpScheme_Jd')");
/*  295 */     Hashtable<String, ChangeScheme> csKeys = new Hashtable();
/*  296 */     for (Object obj : cslist) {
/*  297 */       ChangeScheme cs = (ChangeScheme)obj;
/*  298 */       csKeys.put(cs.getChangeScheme_key(), cs);
/*      */     }
/*  300 */     list_ChangeSchemes.add(initScheme(csKeys, "EmpScheme_Add"));
/*  301 */     String db_type = HibernateUtil.getDb_type();
/*  302 */     Session session = HibernateUtil.currentSession();
/*  303 */     Transaction tx = session.beginTransaction();
/*  304 */     String sql = "";
/*      */     try {
/*  306 */       for (ChangeScheme changeScheme : list_ChangeSchemes) {
/*  307 */         int order = changeScheme.getChangeScheme_no();
/*  308 */         String entityName = "PersonChange_" + order;
/*  309 */         String schemeName = changeScheme.getChangeScheme_name();
/*  310 */         String schemeKey = changeScheme.getChangeScheme_key();
/*  311 */         if (db_type.equals("sqlserver")) {
/*  312 */           sql = sql + "insert into TABNAME(entity_key,entityName,entitycaption,canmodify,order_no,success_build,limit_flag,init_flag)select top 1 '" + entityName + "','" + entityName + "','" + schemeName + "','其他业务'," + order + ",0,0,0 from changescheme where changeScheme_key='" + schemeKey + "'" + " and not exists(select 1 from TabName where entityName='" + entityName + "');";
/*      */           
/*      */ 
/*  315 */           sql = sql + "update TabName set entityClass_key=ec.entityClass_key from EntityClass ec,TabName t where ec.entityType_code='RSDD' and t.entityName='" + entityName + "';";
/*      */         } else {
/*  317 */           sql = sql + "insert into TABNAME(entity_key,entityName,entitycaption,canmodify,order_no,success_build,limit_flag,init_flag)select  '" + entityName + "','" + entityName + "','" + schemeName + "','其他业务'," + order + ",0,0,0 from changescheme where changeScheme_key='" + schemeKey + "'" + " and not exists(select 1 from TabName where entityName='" + entityName + "') and rownum<2;";
/*      */           
/*      */ 
/*  320 */           sql = sql + "update TabName t set entityClass_key=(select ec.entityClass_key from EntityClass ec where ec.entityType_code='RSDD') where t.entityName='" + entityName + "';";
/*      */         }
/*  322 */         ImplUtil.exSQLs(tx, session, sql, ";");
/*  323 */         String fieldNames = "";
/*  324 */         for (ChangeItem ci : changeScheme.getChangeItems()) {
/*  325 */           fieldNames = fieldNames + ",'" + ci.getFieldName() + "'";
/*      */         }
/*  327 */         fieldNames = fieldNames.substring(1);
/*  328 */         EntityDef ed = (EntityDef)session.createQuery("from EntityDef ed where ed.entityName='" + entityName + "'").uniqueResult();
/*  329 */         List list = session.createQuery("from FieldDef fd where fd.entityDef.entityName='A01' or fd.entityDef.entityClass.entityType_code='ANNEX'").list();
/*  330 */         Hashtable<String, FieldDef> fds = new Hashtable();
/*  331 */         for (Object obj : list) {
/*  332 */           FieldDef fd = (FieldDef)obj;
/*  333 */           fds.put(fd.getField_name(), fd);
/*      */         }
/*  335 */         FieldDef fd_dept = new FieldDef();
/*  336 */         fd_dept.setField_name("deptCode");
/*  337 */         fd_dept.setField_caption("部门");
/*  338 */         fd_dept.setField_type("String");
/*  339 */         fd_dept.setField_width(255);
/*  340 */         fd_dept.setView_width(40);
/*  341 */         fds.put("deptCode", fd_dept);
/*  342 */         FieldDef fd_g10 = new FieldDef();
/*  343 */         fd_g10.setField_name("g10");
/*  344 */         fd_g10.setField_caption("岗位");
/*  345 */         fd_g10.setField_type("String");
/*  346 */         fd_g10.setField_width(255);
/*  347 */         fd_g10.setView_width(40);
/*  348 */         fds.put("g10", fd_g10);
/*  349 */         List<String> fields = EntityBuilder.getCommFieldNameListOf(FieldDef.class, EntityBuilder.COMM_FIELD_VISIBLE);
/*  350 */         int ind = 1;
/*  351 */         sql = "";
/*  352 */         for (ChangeItem ci : changeScheme.getChangeItems()) {
/*  353 */           String fieldName = SysUtil.tranField(ci.getFieldName());
/*  354 */           FieldDef copyfd = (FieldDef)fds.get(fieldName);
/*  355 */           if (copyfd != null)
/*      */           {
/*      */ 
/*  358 */             String oldName = "old_" + fieldName;
/*  359 */             FieldDef fd = new FieldDef();
/*  360 */             PublicUtil.copyProperties(copyfd, fd, fields, fields);
/*  361 */             fd.setField_key(entityName + "_" + oldName);
/*      */             
/*  363 */             fd.setField_mark("自定义固定项");
/*  364 */             fd.setEntityDef(ed);
/*  365 */             fd.setOrder_no(ind);
/*  366 */             fd.setField_name(oldName);
/*  367 */             fd.setField_caption("变动前" + fd.getField_caption());
/*  368 */             sql = sql + RebuildUtil.buildSQL(fd, db_type);
/*  369 */             ind++;
/*  370 */             String newName = "new_" + fieldName;
/*  371 */             fd = new FieldDef();
/*  372 */             PublicUtil.copyProperties(copyfd, fd, fields, fields);
/*  373 */             fd.setField_key(entityName + "_" + newName);
/*      */             
/*  375 */             fd.setField_mark("自定义固定项");
/*  376 */             fd.setEntityDef(ed);
/*  377 */             fd.setOrder_no(ind);
/*  378 */             fd.setField_name(newName);
/*  379 */             fd.setField_caption("变动后" + fd.getField_caption());
/*  380 */             sql = sql + RebuildUtil.buildSQL(fd, db_type);
/*  381 */             ind++;
/*      */           } }
/*  383 */         ImplUtil.exSQLs(tx, session, sql, ";");
/*      */       }
/*  385 */       tx.commit();
/*      */     } catch (Exception ex) {
/*  387 */       ex.printStackTrace();
/*  388 */       ImplUtil.rollBack(tx);
/*  389 */       log.error(ex);
/*      */     } finally {
/*  391 */       HibernateUtil.closeSession();
/*      */     }
/*      */   }
/*      */   
/*      */   private static ChangeScheme initScheme(Hashtable<String, ChangeScheme> csKeys, String schemeKey) {
/*  396 */     createNewNoForChangeScheme();
/*  397 */     String scheme_name = "入职登记";
/*  398 */     ChangeScheme cs = (ChangeScheme)csKeys.get(schemeKey);
/*  399 */     if (cs != null) {
/*  400 */       cs.setChangeScheme_name(scheme_name);
/*  401 */       HibernateUtil.saveOrUpdate(cs);
/*  402 */       return cs;
/*      */     }
/*  404 */     ChangeScheme ciAdd = new ChangeScheme();
/*  405 */     ciAdd.setChangeScheme_key(schemeKey);
/*  406 */     ciAdd.setChangeScheme_no(SysUtil.objToInt(HibernateUtil.fetchNewNoBy(schemeKey, 0, null)));
/*  407 */     String fieldName = "a0193";
/*  408 */     String fieldType = "Integer";
/*  409 */     String displayName = "人员状态标识";
/*  410 */     ciAdd.setChangeScheme_name(scheme_name);
/*  411 */     ciAdd.setCheck_flag(true);
/*  412 */     ChangeItem ci = new ChangeItem();
/*  413 */     ci.setChangeItem_key(schemeKey);
/*  414 */     ci.setFieldName(fieldName);
/*  415 */     ci.setChangeScheme(ciAdd);
/*  416 */     ci.setField_type(fieldType);
/*  417 */     ci.setDisplayName(displayName);
/*  418 */     HashSet<ChangeItem> cis = new HashSet();
/*  419 */     cis.add(ci);
/*  420 */     ciAdd.setChangeItems(cis);
/*  421 */     ciAdd.setNew_flag(0);
/*  422 */     HibernateUtil.saveOrUpdate(ciAdd);
/*  423 */     HibernateUtil.saveOrUpdate(ci);
/*  424 */     return ciAdd;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private static void createNewNoForChangeScheme()
/*      */   {
/*  435 */     Session s = HibernateUtil.currentSession();
/*  436 */     Transaction tx = s.beginTransaction();
/*      */     try {
/*  438 */       List list = s.createQuery("from AutoNoRule where autoNoRule_key in('PersonChangeNo','EmpScheme')").list();
/*  439 */       boolean change_rule_exists = false;
/*  440 */       String db_type = HibernateUtil.getDb_type();
/*  441 */       boolean cur_rule_exists = false;
/*  442 */       for (Object obj : list) {
/*  443 */         AutoNoRule anr = (AutoNoRule)obj;
/*  444 */         if (anr.getAutoNoRule_key().equals("EmpScheme")) {
/*  445 */           change_rule_exists = true;
/*  446 */         } else if (anr.getAutoNoRule_key().equals("PersonChangeNo")) {
/*  447 */           cur_rule_exists = true;
/*      */         }
/*      */       }
/*  450 */       if (!change_rule_exists) {
/*  451 */         AutoNoRule anr = new AutoNoRule();
/*  452 */         anr.setAutoNoRule_key("EmpScheme");
/*  453 */         anr.setAutoNoRule_name("调配模板序列号");
/*  454 */         anr.setAutoNoRule_id("EmpScheme");
/*  455 */         anr.setNo_lenth(4);
/*  456 */         AutoNo an = new AutoNo();
/*  457 */         an.setAutoNo_key("EmpScheme");
/*  458 */         an.setNew_no(SysUtil.objToInt(s.createSQLQuery("select " + DbUtil.getNull_strForDB(db_type) + "(max(changescheme_no),0) from changescheme").uniqueResult()) + 1);
/*  459 */         s.saveOrUpdate(anr);
/*  460 */         s.saveOrUpdate(an);
/*  461 */         s.flush();
/*      */       }
/*  463 */       if (!cur_rule_exists) {
/*  464 */         AutoNoRule anr = new AutoNoRule();
/*  465 */         anr.setAutoNoRule_key("PersonChangeNo");
/*  466 */         anr.setAutoNoRule_name("人员调配批次号");
/*  467 */         anr.setAutoNoRule_id("PersonChangeNo");
/*  468 */         anr.setNo_unit("按天");
/*  469 */         anr.setNo_lenth(4);
/*  470 */         s.saveOrUpdate(anr);
/*  471 */         AutoNo an = new AutoNo();
/*  472 */         an.setAutoNo_key("PersonChangeNo");
/*  473 */         an.setNew_no(1);
/*  474 */         s.saveOrUpdate(anr);
/*  475 */         s.saveOrUpdate(an);
/*      */       }
/*  477 */       tx.commit();
/*      */     } catch (Exception e) {
/*  479 */       e.printStackTrace();
/*  480 */       log.error(e);
/*  481 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  483 */       HibernateUtil.closeSession();
/*      */     }
/*      */   }
/*      */   
/*      */   public CommServiceImpl() throws RemoteException {
/*  488 */     super(ServerApp.rmiPort);
/*      */   }
/*      */   
/*      */   public CommServiceImpl(int port) throws RemoteException {
/*  492 */     super(port);
/*      */   }
/*      */   
/*      */   public String getSQL_dialect()
/*      */     throws RemoteException
/*      */   {
/*  498 */     return HibernateUtil.getDialect();
/*      */   }
/*      */   
/*      */   public Date getServerDate() throws RemoteException
/*      */   {
/*  503 */     return HibernateUtil.getServerDate();
/*      */   }
/*      */   
/*      */   public String fetchNewNoBy(String autoNoRule_key, int b_inc, Hashtable<String, String> params)
/*      */     throws RemoteException
/*      */   {
/*  509 */     return HibernateUtil.fetchNewNoBy(autoNoRule_key, b_inc, params);
/*      */   }
/*      */   
/*      */   public List fetchNewNoByAndAuto(String autoNoRule_key, int b_inc, Hashtable<String, String> params, Map<String, Integer> map) throws RemoteException
/*      */   {
/*  514 */     return HibernateUtil.fetchNewNoByAndAotu(autoNoRule_key, b_inc, params, map);
/*      */   }
/*      */   
/*      */ 
/*      */   public String fetchNewNoBy(AutoNoRule anr, Hashtable<String, String> params)
/*      */     throws RemoteException
/*      */   {
/*  521 */     String tmp_s = "";
/*  522 */     Date date = HibernateUtil.getServerDate();
/*  523 */     DateFormat df = new java.text.SimpleDateFormat("yyyyMMdd");
/*  524 */     String ymd = df.format(date);
/*  525 */     String prefix = anr.isAdd_perfix() ? anr.getPerfix() : (anr.getPerfix() == null) || (anr.getPerfix().trim().equals("")) ? "''" : "''";
/*  526 */     if (!anr.getNo_unit().equals("顺序编码")) {
/*  527 */       if (anr.getNo_unit().equals("按年")) {
/*  528 */         tmp_s = ymd.substring(0, 4);
/*  529 */       } else if (anr.getNo_unit().equals("按月")) {
/*  530 */         tmp_s = ymd.substring(0, 6);
/*  531 */       } else if (anr.getNo_unit().equals("按天"))
/*  532 */         tmp_s = ymd;
/*      */     }
/*  534 */     prefix = prefix.replace("@年份", "'" + ymd.substring(0, 4) + "'");
/*  535 */     prefix = prefix.replace("@月份", "'" + ymd.substring(4, 6) + "'");
/*  536 */     prefix = prefix.replace("@日期", "'" + ymd.substring(6) + "'");
/*  537 */     if (params != null) {
/*  538 */       for (String key : params.keySet()) {
/*  539 */         prefix = prefix.replace(key, (CharSequence)params.get(key));
/*      */       }
/*      */     }
/*  542 */     Session s = HibernateUtil.currentSession();
/*  543 */     Transaction tx = s.beginTransaction();
/*  544 */     int new_no = anr.getInit_no();
/*  545 */     String no = "";
/*      */     try {
/*  547 */       if (!prefix.equals("")) {
/*  548 */         s.createSQLQuery("update AutoNoRule set t_perfix=(" + prefix + ") where autoNoRule_key='" + anr.getAutoNoRule_key() + "'").executeUpdate();
/*  549 */         s.flush();
/*  550 */         SQLQuery qr = s.createSQLQuery("select t_perfix from AutoNoRule where autoNoRule_key='" + anr.getAutoNoRule_key() + "'");
/*  551 */         qr.setMaxResults(1);
/*  552 */         List list = qr.list();
/*  553 */         prefix = (list.isEmpty()) || (list.get(0) == null) ? "" : list.get(0).toString();
/*      */       }
/*  555 */       prefix = prefix + tmp_s;
/*  556 */       Object obj = s.createSQLQuery("select new_no from autono where autono_key='" + anr.getAutoNoRule_id() + tmp_s + "'").uniqueResult();
/*  557 */       if (obj != null) {
/*  558 */         int old_no = SysUtil.objToInt(obj);
/*  559 */         if (new_no < old_no) {
/*  560 */           if (anr.getNo_unit().equals("顺序编码")) {
/*  561 */             return "@@@" + old_no;
/*      */           }
/*  563 */           new_no = old_no;
/*      */         }
/*      */       }
/*  566 */       no = new_no + "";
/*  567 */       int i = no.length();
/*  568 */       while (i < anr.getNo_lenth()) {
/*  569 */         no = "0" + no;
/*  570 */         i++;
/*      */       }
/*  572 */       tx.rollback();
/*      */     } catch (Exception e) { int i;
/*  574 */       ImplUtil.rollBack(tx);
/*  575 */       log.error(ImplUtil.getSQLExceptionMsg(e));
/*  576 */       return null;
/*      */     } finally {
/*  578 */       HibernateUtil.closeSession();
/*      */     }
/*  580 */     return prefix + no;
/*      */   }
/*      */   
/*      */   public String getServerIp()
/*      */   {
/*  585 */     return org.jhrcore.util.ClientIpCheck.getIP();
/*      */   }
/*      */   
/*      */   public Properties getSysProperties() throws RemoteException
/*      */   {
/*  590 */     return ServerApp.getSys_properties();
/*      */   }
/*      */   
/*      */   public void uploadFileByStream(InputStream ips, String dest) throws RemoteException
/*      */   {
/*  595 */     if (ips == null) {
/*  596 */       return;
/*      */     }
/*  598 */     FileOutputStream fos = null;
/*      */     try {
/*  600 */       fos = new FileOutputStream(dest);
/*  601 */       byte[] buffer = new byte['Ѐ'];
/*  602 */       int len = 0;
/*  603 */       while ((len = ips.read(buffer)) != -1) {
/*  604 */         fos.write(buffer, 0, len);
/*      */       }
/*  606 */       fos.flush();
/*  607 */       fos.close();
/*  608 */       ips.close();
/*      */     } catch (FileNotFoundException ex) {
/*  610 */       log.error(ex);
/*      */     } catch (IOException ex) {
/*  612 */       log.error(ex);
/*      */     }
/*      */   }
/*      */   
/*      */   public void uploadFile(byte[] p_byte, String path) throws RemoteException
/*      */   {
/*      */     try {
/*  619 */       File file = new File(path);
/*  620 */       if (!file.getParentFile().exists()) {
/*  621 */         file.getParentFile().mkdirs();
/*      */       }
/*  623 */       BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
/*      */       
/*  625 */       output.write(p_byte);
/*  626 */       output.close();
/*      */     } catch (Exception e) {
/*  628 */       log.error(e);
/*      */     }
/*      */   }
/*      */   
/*      */   public byte[] downloadFile(String ab_path) throws RemoteException
/*      */   {
/*  634 */     if ((ab_path == null) || (ab_path.equals(""))) {
/*  635 */       return null;
/*      */     }
/*      */     try {
/*  638 */       File file = new File(ab_path);
/*  639 */       if ((file == null) || (!file.exists())) {
/*  640 */         log.error("file not exists:" + ab_path);
/*  641 */         return null;
/*      */       }
/*  643 */       byte[] buffer = new byte[(int)file.length()];
/*  644 */       BufferedInputStream input = new BufferedInputStream(new java.io.FileInputStream(file));
/*      */       
/*  646 */       input.read(buffer, 0, buffer.length);
/*  647 */       input.close();
/*  648 */       return buffer;
/*      */     } catch (Exception e) {
/*  650 */       log.error(e); }
/*  651 */     return null;
/*      */   }
/*      */   
/*      */   public boolean deleteFiles(List<String> list_paths)
/*      */     throws RemoteException
/*      */   {
/*  657 */     if ((list_paths == null) || (list_paths.size() < 0)) {
/*  658 */       return true;
/*      */     }
/*  660 */     File file = null;
/*  661 */     int error = 0;
/*  662 */     for (String docu_path : list_paths) {
/*  663 */       file = new File(docu_path);
/*  664 */       if ((file.exists()) && 
/*  665 */         (!file.delete())) {
/*  666 */         error++;
/*  667 */         break;
/*      */       }
/*      */     }
/*      */     
/*  671 */     if (error > 0) {
/*  672 */       return false;
/*      */     }
/*  674 */     return true;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveQueryScheme(QueryScheme qs) throws RemoteException
/*      */   {
/*  679 */     Session s = HibernateUtil.currentSession();
/*  680 */     Transaction tx = s.beginTransaction();
/*  681 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/*  683 */       boolean isNew = s.createQuery("select 1 from QueryScheme qs where qs.queryScheme_key='" + qs.getQueryScheme_key() + "'").list().isEmpty();
/*  684 */       List saveList = new ArrayList();
/*  685 */       if (isNew) {
/*  686 */         saveList.add(qs);
/*      */       } else {
/*  688 */         s.update(qs);
/*  689 */         s.createSQLQuery("delete from Condition where queryScheme_key='" + qs.getQueryScheme_key() + "'").executeUpdate();
/*  690 */         s.flush();
/*      */       }
/*  692 */       saveList.addAll(qs.getConditions());
/*  693 */       for (Object obj : saveList) {
/*  694 */         s.save(obj);
/*      */       }
/*  696 */       tx.commit();
/*      */     } catch (Exception e) {
/*  698 */       ImplUtil.rollBack(tx);
/*  699 */       validateSQLResult.setResult(1);
/*  700 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  701 */       log.error(ImplUtil.getSQLExceptionMsg(e));
/*      */     } finally {
/*  703 */       HibernateUtil.closeSession();
/*      */     }
/*  705 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult disconnect(List<Object> userCodes, String msg)
/*      */   {
/*  710 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  711 */     long server_start_time = ServerApp.getServerStartTime();
/*  712 */     if (server_start_time == 0L) {
/*  713 */       validateSQLResult.setResult(1);
/*  714 */       validateSQLResult.setMsg("HR服务尚未启动成功，请稍后登陆!");
/*  715 */       return validateSQLResult;
/*      */     }
/*  717 */     if (userCodes.size() <= 1) {
/*  718 */       return validateSQLResult;
/*      */     }
/*  720 */     LoginUser option_lu = (LoginUser)userCodes.get(0);
/*  721 */     userCodes.remove(option_lu);
/*  722 */     msg = "您被【" + option_lu.getUser_name() + "】强制下线，下线缘由：\n" + msg;
/*  723 */     for (Object user : userCodes) {
/*  724 */       if ((user instanceof LoginUser)) {
/*  725 */         LoginUser user_code = (LoginUser)user;
/*  726 */         List<LoginUser> ucs = (List)loginUserKeys.get(user_code.getUser_code());
/*  727 */         for (LoginUser lu : ucs) {
/*  728 */           lu.setUser_state("强制下线");
/*  729 */           lu.setMsg(msg);
/*      */         }
/*      */       }
/*      */     }
/*  733 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public ValidateSQLResult connectServer(String type, String user_code)
/*      */     throws RemoteException
/*      */   {
/*  747 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  748 */     long server_start_time = ServerApp.getServerStartTime();
/*  749 */     if (server_start_time == 0L) {
/*  750 */       validateSQLResult.setResult(1);
/*  751 */       validateSQLResult.setMsg("HR服务尚未启动成功，请稍后登陆!");
/*  752 */       return validateSQLResult;
/*      */     }
/*  754 */     if ((user_code == null) || (user_code.trim().equals(""))) {
/*  755 */       validateSQLResult.setResult(1);
/*  756 */       validateSQLResult.setMsg("非法登录!");
/*  757 */       return validateSQLResult;
/*      */     }
/*  759 */     long now = System.currentTimeMillis();
/*  760 */     String[] codes = user_code.split("\\|");
/*  761 */     if (codes.length < 5) {
/*  762 */       validateSQLResult.setResult(1);
/*  763 */       validateSQLResult.setMsg("非法登录!");
/*  764 */       return validateSQLResult;
/*      */     }
/*  766 */     List<LoginUser> ucs = (List)loginUserKeys.get(codes[0]);
/*  767 */     if (type.equals("login")) {
/*  768 */       if (ucs == null) {
/*  769 */         ucs = new ArrayList();
/*      */       } else {
/*  771 */         for (LoginUser lu : ucs) {
/*  772 */           lu.setUser_state("离线");
/*      */         }
/*      */       }
/*  775 */       LoginUser uc = new LoginUser();
/*  776 */       uc.setServer_time(ServerApp.getServerStartTime());
/*  777 */       uc.setLogin_time(new Date());
/*  778 */       uc.setLog_ip(codes[1]);
/*  779 */       uc.setLog_mac(codes[2]);
/*  780 */       uc.setUser_code(codes[0]);
/*  781 */       uc.setLast_visit_time(now);
/*  782 */       uc.setUser_state("在线");
/*  783 */       uc.setLoginUser_key(codes[4]);
/*  784 */       ucs.add(uc);
/*  785 */       loginUserKeys.put(codes[0], ucs);
/*  786 */       List list = HibernateUtil.selectSQL("select count(*) as num from MailInfo mi where mi.user_key='" + codes[0] + "' and mi.r_userKey='" + codes[0] + "' and mi.r_date is null", false, -1);
/*  787 */       int msgNo = 0;
/*  788 */       if (list.size() > 0) {
/*  789 */         msgNo = Integer.valueOf(list.get(0).toString()).intValue();
/*      */       }
/*  791 */       uc.setMsg_no(msgNo);
/*  792 */       validateSQLResult.setInsert_result(msgNo);
/*  793 */     } else if (type.equals("online")) {
/*  794 */       if ((ucs == null) || (ucs.isEmpty())) {
/*  795 */         validateSQLResult.setResult(1);
/*  796 */         validateSQLResult.setMsg("HR服务已经重新启动，为保持数据一致，请重新登陆!");
/*  797 */         return validateSQLResult;
/*      */       }
/*  799 */       LoginUser online_lu = null;
/*  800 */       for (LoginUser lu : ucs) {
/*  801 */         if (lu.getLoginUser_key().equals(codes[4])) {
/*  802 */           online_lu = lu;
/*  803 */           break;
/*      */         }
/*      */       }
/*  806 */       if (online_lu == null) {
/*  807 */         validateSQLResult.setResult(2);
/*  808 */         validateSQLResult.setMsg("连接中断，请重新登录!");
/*  809 */         return validateSQLResult;
/*      */       }
/*  811 */       if (online_lu.getServer_time() != server_start_time) {
/*  812 */         validateSQLResult.setResult(1);
/*  813 */         validateSQLResult.setMsg("HR服务已经重新启动，为保持数据一致，请重新登陆!");
/*  814 */         return validateSQLResult; }
/*  815 */       if ((!online_lu.getUser_code().equals("-1")) && (online_lu.getUser_state().equals("离线"))) {
/*  816 */         validateSQLResult.setResult(2);
/*  817 */         validateSQLResult.setMsg("当前账号在别的地方登录，您被强制退出!");
/*  818 */         ucs.remove(online_lu);
/*  819 */         loginUserKeys.put(codes[0], ucs);
/*  820 */       } else if ((!online_lu.getUser_code().equals("-1")) && (online_lu.getUser_state().equals("超时"))) {
/*  821 */         validateSQLResult.setResult(2);
/*  822 */         validateSQLResult.setMsg("连接超时!");
/*  823 */         ucs.remove(online_lu);
/*  824 */         loginUserKeys.put(codes[0], ucs);
/*  825 */       } else if ((!online_lu.getUser_code().equals("-1")) && (online_lu.getUser_state().equals("强制下线"))) {
/*  826 */         validateSQLResult.setResult(2);
/*  827 */         validateSQLResult.setMsg(online_lu.getMsg());
/*  828 */         ucs.remove(online_lu);
/*  829 */         loginUserKeys.put(codes[0], ucs);
/*      */       } else {
/*  831 */         online_lu.setLast_visit_time(now);
/*  832 */         validateSQLResult.setMsg((online_lu.getMsg() == null) || (online_lu.getMsg().trim().equals("")) ? "通信正常" : online_lu.getMsg());
/*  833 */         online_lu.setMsg(null);
/*  834 */         validateSQLResult.setInsert_result(online_lu.getMsg_no());
/*      */       }
/*      */       
/*      */     }
/*  838 */     else if (type.equals("quit")) {
/*  839 */       LoginUser online_lu = null;
/*  840 */       for (LoginUser lu : ucs) {
/*  841 */         if (lu.getLoginUser_key().equals(codes[4])) {
/*  842 */           online_lu = lu;
/*  843 */           break;
/*      */         }
/*      */       }
/*  846 */       if (online_lu != null) {
/*  847 */         ucs.remove(online_lu);
/*      */       }
/*      */     } else {
/*  850 */       validateSQLResult.setResult(1);
/*  851 */       validateSQLResult.setMsg("未知的消息类型!");
/*      */     }
/*  853 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult sendMsg(List<Object> userCodes, String msg) throws RemoteException
/*      */   {
/*  858 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  859 */     long server_start_time = ServerApp.getServerStartTime();
/*  860 */     if (server_start_time == 0L) {
/*  861 */       validateSQLResult.setResult(1);
/*  862 */       validateSQLResult.setMsg("HR服务尚未启动成功，请稍后登陆!");
/*  863 */       return validateSQLResult;
/*      */     }
/*  865 */     if (userCodes.size() <= 1) {
/*  866 */       return validateSQLResult;
/*      */     }
/*  868 */     LoginUser option_lu = (LoginUser)userCodes.get(0);
/*  869 */     userCodes.remove(option_lu);
/*  870 */     msg = "@MSG【" + option_lu.getUser_name() + "】于 " + org.jhrcore.util.DateUtil.DateToStr(new Date(), "yyyy-MM-dd HH:mm:ss 给您发送消息：\n") + msg;
/*  871 */     for (Object user : userCodes) {
/*  872 */       if ((user instanceof LoginUser)) {
/*  873 */         LoginUser user_code = (LoginUser)user;
/*  874 */         List<LoginUser> ucs = (List)loginUserKeys.get(user_code.getUser_code());
/*  875 */         for (LoginUser lu : ucs) {
/*  876 */           lu.setMsg(msg);
/*      */         }
/*      */       }
/*      */     }
/*  880 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public List<LoginUser> getLoginUsers(Object cur_dept, String dept_right_sql) throws RemoteException
/*      */   {
/*  885 */     List users = new ArrayList();
/*  886 */     if ((cur_dept != null) && ((cur_dept instanceof DeptCode))) {
/*  887 */       String sql = "select a01.a01_key,a01.a0101 from a01,a01password apw,deptcode d where a01.deptCode_key=d.deptCode_key and a01.a01_key=apw.a01_key";
/*  888 */       sql = sql + " and d.dept_code like '" + ((DeptCode)cur_dept).getDept_code() + "%' and (" + dept_right_sql + ") and exists(select 1 from RoleA01 ra where ra.a01password_key=apw.a01password_key and ra.role_key<>'&&&') order by a01.a0190";
/*  889 */       List list = HibernateUtil.selectSQL(sql, false, 50000);
/*  890 */       for (Object obj : list) {
/*  891 */         Object[] objs = (Object[])obj;
/*  892 */         LoginUser lu = new LoginUser();
/*  893 */         lu.setLoginUser_key(objs[0].toString());
/*  894 */         lu.setUser_code(objs[0].toString());
/*  895 */         lu.setUser_name(objs[1] == null ? "" : objs[1].toString());
/*  896 */         if (objs[1] != null) {
/*  897 */           List<LoginUser> lus = (List)loginUserKeys.get(objs[0].toString());
/*  898 */           if (lus != null) {
/*  899 */             for (LoginUser user : lus) {
/*  900 */               if (user.getUser_state().equals("在线")) {
/*  901 */                 lu.setUser_state("在线");
/*  902 */                 break;
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*  907 */         users.add(lu);
/*      */       }
/*      */     }
/*  910 */     return users;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveExportScheme(ExportScheme es) throws RemoteException
/*      */   {
/*  915 */     Session s = HibernateUtil.currentSession();
/*  916 */     Transaction tx = s.beginTransaction();
/*  917 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/*  919 */       boolean isNew = s.createSQLQuery("select 1 from ExportScheme qs where qs.exportScheme_key='" + es.getExportScheme_key() + "'").list().isEmpty();
/*  920 */       List saveList = new ArrayList();
/*  921 */       if (isNew) {
/*  922 */         saveList.add(es);
/*      */       } else {
/*  924 */         s.update(es);
/*  925 */         s.createSQLQuery("delete from ExportDetail where exportScheme_key='" + es.getExportScheme_key() + "'").executeUpdate();
/*      */       }
/*  927 */       s.flush();
/*  928 */       saveList.addAll(es.getExportDetails());
/*  929 */       for (Object obj : saveList) {
/*  930 */         s.save(obj);
/*      */       }
/*  932 */       tx.commit();
/*      */     } catch (Exception e) {
/*  934 */       log.error(e);
/*  935 */       ImplUtil.rollBack(tx);
/*  936 */       validateSQLResult.setResult(1);
/*  937 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*      */     }
/*  939 */     HibernateUtil.closeSession();
/*  940 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public List<String> getSysModules() throws RemoteException
/*      */   {
/*  945 */     List list = new ArrayList();
/*      */     
/*  947 */     return list;
/*      */   }
/*      */   
/*      */   public String[] getSA() throws RemoteException
/*      */   {
/*  952 */     Session s = HibernateUtil.currentSession();
/*  953 */     String pass = "";
/*  954 */     Transaction tx = s.beginTransaction();
/*      */     try {
/*  956 */       List list = s.createSQLQuery("select SysParameter_value from SysParameter where sysParameter_key='SysManPass'").list();
/*  957 */       if ((list == null) || (list.isEmpty())) {
/*  958 */         s.createSQLQuery("insert into SysParameter(SysParameter_key,SysParameter_name,SysParameter_value,SysParameter_code) values('SysManPass','SysManPass','','sa')").executeUpdate();
/*  959 */         tx.commit();
/*      */       } else {
/*  961 */         pass = list.get(0) == null ? "" : list.get(0).toString();
/*      */       }
/*      */     } catch (Exception e) {
/*  964 */       log.error(e);
/*  965 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  967 */       HibernateUtil.closeSession();
/*      */     }
/*  969 */     String[] sa = { "sa", pass };
/*  970 */     return sa;
/*      */   }
/*      */   
/*      */   public String getWebServerIp() throws RemoteException
/*      */   {
/*  975 */     String webServerIp = getServerIp();
/*  976 */     Properties properties = ConfigManager.getConfigManager().loadProperties("system.properties");
/*  977 */     if (properties != null) {
/*  978 */       String ip = properties.getProperty("webip");
/*  979 */       if ((ip != null) && (!"".equals(ip.trim()))) {
/*  980 */         return ip;
/*      */       }
/*      */     }
/*      */     
/*  984 */     return webServerIp;
/*      */   }
/*      */   
/*      */   public String getWebServerPort() throws RemoteException
/*      */   {
/*  989 */     String webServerPort = "8080";
/*  990 */     Properties properties = ConfigManager.getConfigManager().loadProperties("system.properties");
/*  991 */     if (properties != null) {
/*  992 */       String port = properties.getProperty("webport");
/*  993 */       if ((port != null) && (!"".equals(port.trim()))) {
/*  994 */         return port;
/*      */       }
/*      */     }
/*      */     
/*  998 */     return webServerPort;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveShowSchemeGroup(List new_groups, String user_code, String module_code) throws RemoteException
/*      */   {
/* 1003 */     Session s = HibernateUtil.currentSession();
/* 1004 */     Transaction tx = s.beginTransaction();
/* 1005 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1007 */       s.createSQLQuery("delete from ShowFieldGroup where entity_name='" + module_code + "' and person_code='" + user_code + "'").executeUpdate();
/* 1008 */       s.createSQLQuery("delete from ShowSchemeGroup where entity_name='" + module_code + "' and person_code='" + user_code + "'").executeUpdate();
/* 1009 */       for (Object obj : new_groups) {
/* 1010 */         if ((obj instanceof ShowFieldGroup)) {
/* 1011 */           ShowFieldGroup sfg = (ShowFieldGroup)obj;
/* 1012 */           if (sfg.getPerson_code().equals(user_code)) {
/* 1013 */             s.save(obj);
/*      */           } else {
/* 1015 */             s.update(obj);
/*      */           }
/* 1017 */         } else if ((obj instanceof ShowSchemeGroup)) {
/* 1018 */           ShowSchemeGroup ssg = (ShowSchemeGroup)obj;
/* 1019 */           if (ssg.getPerson_code().equals(user_code)) {
/* 1020 */             s.save(obj);
/*      */           } else {
/* 1022 */             s.update(obj);
/*      */           }
/*      */         }
/*      */       }
/* 1026 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1028 */       ImplUtil.rollBack(tx);
/* 1029 */       validateSQLResult.setResult(1);
/* 1030 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1031 */       log.error(validateSQLResult.getMsg());
/*      */     }
/* 1033 */     HibernateUtil.closeSession();
/* 1034 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveShowScheme(ShowScheme es, String code) throws RemoteException
/*      */   {
/* 1039 */     Session s = HibernateUtil.currentSession();
/* 1040 */     Transaction tx = s.beginTransaction();
/* 1041 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1043 */       boolean isNew = s.createSQLQuery("select 1 from ShowScheme qs where qs.showScheme_key='" + es.getShowScheme_key() + "'").list().isEmpty();
/* 1044 */       List saveList = new ArrayList();
/* 1045 */       if (isNew) {
/* 1046 */         saveList.add(es);
/*      */       } else {
/* 1048 */         s.update(es);
/* 1049 */         String tableName = "ShowSchemeDetail";
/* 1050 */         if (code.equals("order")) {
/* 1051 */           tableName = "ShowSchemeOrder";
/*      */         }
/* 1053 */         s.createSQLQuery("delete from " + tableName + " where showScheme_key='" + es.getShowScheme_key() + "'").executeUpdate();
/*      */       }
/* 1055 */       if (("1".equals(es.getDefault_flag())) && (!code.equals("order"))) {
/* 1056 */         s.createSQLQuery("update ShowScheme set default_flag = '0' where person_code='" + es.getPerson_code() + "' and entity_name='" + es.getEntity_name() + "'").executeUpdate();
/*      */       }
/* 1058 */       s.flush();
/* 1059 */       if (code.equals("order")) {
/* 1060 */         saveList.addAll(es.getShowSchemeOrders());
/* 1061 */       } else if (code.equals("detail")) {
/* 1062 */         saveList.addAll(es.getShowSchemeDetails());
/*      */       }
/* 1064 */       for (Object obj : saveList) {
/* 1065 */         s.save(obj);
/*      */       }
/* 1067 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1069 */       validateSQLResult.setResult(1);
/* 1070 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1071 */       log.error(validateSQLResult.getMsg());
/* 1072 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1074 */       HibernateUtil.closeSession();
/*      */     }
/* 1076 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult delShowScheme(ShowScheme es) throws RemoteException
/*      */   {
/* 1081 */     Session s = HibernateUtil.currentSession();
/* 1082 */     Transaction tx = s.beginTransaction();
/* 1083 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1085 */       s.createSQLQuery("update ShowScheme set default_flag = '0' where showScheme_key='" + es.getShowScheme_key() + "'").executeUpdate();
/* 1086 */       s.createSQLQuery("delete from ShowSchemeDetail where showScheme_key='" + es.getShowScheme_key() + "'").executeUpdate();
/* 1087 */       s.createSQLQuery("delete from ShowSchemeOrder where showScheme_key='" + es.getShowScheme_key() + "'").executeUpdate();
/* 1088 */       s.createSQLQuery("delete from ShowScheme where showScheme_key='" + es.getShowScheme_key() + "'").executeUpdate();
/* 1089 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1091 */       validateSQLResult.setResult(1);
/* 1092 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1093 */       log.error(validateSQLResult.getMsg());
/* 1094 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1096 */       HibernateUtil.closeSession();
/*      */     }
/* 1098 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveColumnSumScheme(String code, String user_code, List cols) throws RemoteException
/*      */   {
/* 1103 */     Session s = HibernateUtil.currentSession();
/* 1104 */     Transaction tx = s.beginTransaction();
/* 1105 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1107 */       s.createSQLQuery("update ColumnSum  set field_type=field_type where entity_name='" + code + "' and user_code='" + user_code + "'").executeUpdate();
/* 1108 */       s.createSQLQuery("delete from ColumnSum where entity_name='" + code + "' and user_code='" + user_code + "'").executeUpdate();
/* 1109 */       s.flush();
/* 1110 */       for (Object obj : cols) {
/* 1111 */         s.save(obj);
/*      */       }
/* 1113 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1115 */       validateSQLResult.setResult(1);
/* 1116 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1117 */       log.error(validateSQLResult.getMsg());
/* 1118 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1120 */       HibernateUtil.closeSession();
/*      */     }
/* 1122 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult delFormulaDetail(String formula_key, List<String> detail_keys) throws RemoteException
/*      */   {
/* 1127 */     Session s = HibernateUtil.currentSession();
/* 1128 */     Transaction tx = s.beginTransaction();
/* 1129 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1131 */       String del_keys = DbUtil.getQueryForMID("", detail_keys, "", "");
/* 1132 */       s.createSQLQuery("update FormulaDetail set use_flag=use_flag where formulascheme_key='" + formula_key + "'").executeUpdate();
/* 1133 */       s.flush();
/* 1134 */       s.createSQLQuery("delete from FormulaDetail where formulaDetail_key in" + del_keys).executeUpdate();
/* 1135 */       s.flush();
/* 1136 */       List list = s.createQuery("from FormulaDetail fd where fd.formulaScheme.formulaScheme_key='" + formula_key + "' order by order_no").list();
/* 1137 */       int i = 0;
/* 1138 */       for (Object obj : list) {
/* 1139 */         i++;
/* 1140 */         FormulaDetail fd = (FormulaDetail)obj;
/* 1141 */         fd.setOrder_no(Integer.valueOf(i));
/* 1142 */         s.update(fd);
/*      */       }
/* 1144 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1146 */       validateSQLResult.setResult(1);
/* 1147 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1148 */       log.error(validateSQLResult.getMsg());
/* 1149 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1151 */       HibernateUtil.closeSession();
/*      */     }
/* 1153 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveFormulaDetail(Set details) throws RemoteException
/*      */   {
/* 1158 */     Session s = HibernateUtil.currentSession();
/* 1159 */     Transaction tx = s.beginTransaction();
/* 1160 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1162 */       List<String> detailKeys = new ArrayList();
/* 1163 */       List<FormulaDetail> fds = new ArrayList();
/* 1164 */       for (Object obj : details) {
/* 1165 */         detailKeys.add(((FormulaDetail)obj).getFormulaDetail_key());
/* 1166 */         fds.add((FormulaDetail)obj);
/*      */       }
/* 1168 */       String detailKeyStr = DbUtil.getQueryForMID("", detailKeys, "", "");
/* 1169 */       s.createSQLQuery("update FormulaDetail set use_flag=use_flag where formulaDetail_key in" + detailKeyStr).executeUpdate();
/* 1170 */       s.flush();
/* 1171 */       SysUtil.sortListByStr(fds, "formulaDetail_key");
/* 1172 */       for (FormulaDetail fd : fds) {
/* 1173 */         s.saveOrUpdate(fd);
/*      */       }
/* 1175 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1177 */       validateSQLResult.setResult(1);
/* 1178 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1179 */       log.error(validateSQLResult.getMsg());
/* 1180 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1182 */       HibernateUtil.closeSession();
/*      */     }
/* 1184 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveAnalyseScheme(CommAnalyseScheme as, QueryScheme qs, QueryPart part) throws RemoteException
/*      */   {
/* 1189 */     Session s = HibernateUtil.currentSession();
/* 1190 */     Transaction tx = s.beginTransaction();
/* 1191 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1193 */       s.saveOrUpdate(as);
/* 1194 */       s.flush();
/* 1195 */       String entityname = "";
/* 1196 */       if ((as instanceof org.jhrcore.entity.query.EmpQueryAnalyseScheme)) {
/* 1197 */         entityname = "EmpQueryAnalyseField";
/*      */       }
/* 1199 */       if (!entityname.equals("")) {
/* 1200 */         s.createSQLQuery("update  " + entityname + " set stat_type = stat_type where commAnalyseScheme_key='" + as.getCommAnalyseScheme_key() + "'").executeUpdate();
/* 1201 */         s.createSQLQuery("delete from " + entityname + " where commAnalyseScheme_key='" + as.getCommAnalyseScheme_key() + "'").executeUpdate();
/*      */       }
/* 1203 */       for (org.jhrcore.entity.query.CommAnalyseField caf : as.getCommAnalyseFields()) {
/* 1204 */         s.save(caf);
/*      */       }
/* 1206 */       s.flush();
/* 1207 */       if (qs != null) {
/* 1208 */         s.createSQLQuery("update QueryScheme set query_type=query_type where queryScheme_key='" + qs.getQueryScheme_key() + "'").executeUpdate();
/* 1209 */         s.createSQLQuery("delete from Condition where queryScheme_key='" + qs.getQueryScheme_key() + "'").executeUpdate();
/* 1210 */         s.flush();
/* 1211 */         s.saveOrUpdate(qs);
/* 1212 */         s.flush();
/* 1213 */         for (org.jhrcore.entity.query.Condition c : qs.getConditions()) {
/* 1214 */           s.save(c);
/*      */         }
/* 1216 */         s.flush();
/*      */       }
/* 1218 */       if (part != null) {
/* 1219 */         s.createSQLQuery("delete from QueryPartPara where queryPart_key='" + part.getQueryPart_key() + "'").executeUpdate();
/* 1220 */         s.createSQLQuery("delete from QueryPart where queryPart_key='" + part.getQueryPart_key() + "'").executeUpdate();
/* 1221 */         s.flush();
/* 1222 */         s.saveOrUpdate(part);
/* 1223 */         s.flush();
/* 1224 */         for (org.jhrcore.entity.query.QueryPartPara c : part.getQueryPartParas()) {
/* 1225 */           s.save(c);
/*      */         }
/* 1227 */         s.flush();
/*      */       }
/* 1229 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1231 */       validateSQLResult.setResult(1);
/* 1232 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1233 */       log.error(validateSQLResult.getMsg());
/* 1234 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1236 */       HibernateUtil.closeSession();
/*      */     }
/* 1238 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveQueryAnalysisScheme(QueryAnalysisScheme as, QueryScheme qs) throws RemoteException
/*      */   {
/* 1243 */     Session s = HibernateUtil.currentSession();
/* 1244 */     Transaction tx = s.beginTransaction();
/* 1245 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1247 */       s.saveOrUpdate(as);
/* 1248 */       s.flush();
/* 1249 */       s.createSQLQuery("update QueryAnalysisField set stat_type = stat_type where QueryAnalysisScheme_key='" + as.getQueryAnalysisScheme_key() + "'").executeUpdate();
/* 1250 */       s.createSQLQuery("delete from QueryAnalysisField where QueryAnalysisScheme_key='" + as.getQueryAnalysisScheme_key() + "'").executeUpdate();
/* 1251 */       for (org.jhrcore.entity.query.QueryAnalysisField qaf : as.getQueryAnalysisFields()) {
/* 1252 */         s.save(qaf);
/*      */       }
/* 1254 */       s.flush();
/* 1255 */       if (qs != null) {
/* 1256 */         s.createSQLQuery("delete from Condition where queryScheme_key='" + qs.getQueryScheme_key() + "'").executeUpdate();
/* 1257 */         s.saveOrUpdate(qs);
/* 1258 */         for (org.jhrcore.entity.query.Condition c : qs.getConditions()) {
/* 1259 */           s.save(c);
/*      */         }
/*      */       }
/* 1262 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1264 */       validateSQLResult.setResult(1);
/* 1265 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1266 */       log.error(validateSQLResult.getMsg());
/* 1267 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1269 */       HibernateUtil.closeSession();
/*      */     }
/* 1271 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveQueryPart(QueryPart qp) throws RemoteException
/*      */   {
/* 1276 */     Session s = HibernateUtil.currentSession();
/* 1277 */     Transaction tx = s.beginTransaction();
/* 1278 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1280 */       s.saveOrUpdate(qp);
/* 1281 */       s.flush();
/* 1282 */       s.createSQLQuery("delete from QueryPartPara where QueryPart_key='" + qp.getQueryPart_key() + "'").executeUpdate();
/* 1283 */       s.flush();
/* 1284 */       for (org.jhrcore.entity.query.QueryPartPara qaf : qp.getQueryPartParas()) {
/* 1285 */         s.save(qaf);
/*      */       }
/* 1287 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1289 */       validateSQLResult.setResult(1);
/* 1290 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1291 */       log.error(validateSQLResult.getMsg());
/* 1292 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1294 */       HibernateUtil.closeSession();
/*      */     }
/* 1296 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveQueryExtraField(List objs, List existKeys) throws RemoteException
/*      */   {
/* 1301 */     Session s = HibernateUtil.currentSession();
/* 1302 */     Transaction tx = s.beginTransaction();
/* 1303 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/* 1305 */       String key = DbUtil.getQueryForMID("", existKeys, "", "");
/* 1306 */       s.createSQLQuery("delete from QueryExtraField where QueryExtraField_key in" + key).executeUpdate();
/* 1307 */       s.flush();
/* 1308 */       for (Object obj : objs) {
/* 1309 */         s.save(obj);
/*      */       }
/* 1311 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1313 */       validateSQLResult.setResult(1);
/* 1314 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1315 */       log.error(validateSQLResult.getMsg());
/* 1316 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1318 */       HibernateUtil.closeSession();
/*      */     }
/* 1320 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveExtraFieldOrder(List<String[]> orders)
/*      */     throws RemoteException
/*      */   {
/* 1326 */     Session s = HibernateUtil.currentSession();
/* 1327 */     Transaction tx = s.beginTransaction();
/* 1328 */     ValidateSQLResult vs = new ValidateSQLResult();
/* 1329 */     SysUtil.sortArrays(orders);
/*      */     try {
/* 1331 */       for (Object obj : orders) {
/* 1332 */         if (obj != null) {
/* 1333 */           String[] str = (String[])obj;
/* 1334 */           s.createSQLQuery("update QueryExtraField set order_no=" + SysUtil.objToInt(str[1]) + " where queryExtraField_key='" + str[0] + "'").executeUpdate();
/* 1335 */           s.flush();
/*      */         }
/*      */       }
/* 1338 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1340 */       vs.setResult(1);
/* 1341 */       vs.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1342 */       log.error(vs.getMsg());
/* 1343 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1345 */       HibernateUtil.closeSession();
/*      */     }
/* 1347 */     return vs;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult updateFunRights(List<FuntionRight> rights)
/*      */     throws RemoteException
/*      */   {
/* 1353 */     Session s = HibernateUtil.currentSession();
/* 1354 */     Transaction tx = s.beginTransaction();
/* 1355 */     ValidateSQLResult vs = new ValidateSQLResult();
/* 1356 */     SysUtil.sortListByStr(rights, "funtionright_key");
/*      */     try {
/* 1358 */       for (Object obj : rights) {
/* 1359 */         if (obj != null) {
/* 1360 */           s.update(obj);
/*      */         }
/*      */       }
/* 1363 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1365 */       vs.setResult(1);
/* 1366 */       vs.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1367 */       log.error(vs.getMsg());
/* 1368 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1370 */       HibernateUtil.closeSession();
/*      */     }
/* 1372 */     return vs;
/*      */   }
/*      */   
/*      */   public String getServer_file_path() throws RemoteException
/*      */   {
/* 1377 */     String path = System.getProperty("user.dir");
/* 1378 */     path = path + "/";
/* 1379 */     return path;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveParameters(List list) throws RemoteException
/*      */   {
/* 1384 */     ValidateSQLResult result = new ValidateSQLResult();
/* 1385 */     Session s = HibernateUtil.currentSession();
/* 1386 */     Transaction tx = s.beginTransaction();
/*      */     try {
/* 1388 */       for (Object obj : list) {
/* 1389 */         s.saveOrUpdate(obj);
/* 1390 */         s.flush();
/*      */       }
/* 1392 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1394 */       ImplUtil.rollBack(tx);
/* 1395 */       result.setResult(1);
/* 1396 */       result.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1397 */       log.error(result.getMsg());
/*      */     } finally {
/* 1399 */       HibernateUtil.closeSession();
/*      */     }
/* 1401 */     return result;
/*      */   }
/*      */   
/*      */   public Hashtable<String, String> getSecuryInfo(boolean fetchVer) throws RemoteException
/*      */   {
/* 1406 */     Hashtable<String, String> result = new Hashtable();
/* 1407 */     if (fetchVer) {
/* 1408 */       Object obj = HibernateUtil.fetchEntityBy("select sysparameter_value from SysParameter where sysParameter_key='HRVersion'");
/* 1409 */       result.put("version", (obj == null) || (obj.toString().trim().equals("")) ? "9.1" : obj.toString());
/*      */     }
/*      */     
/*      */ 
/*      */ 
/* 1414 */     return result;
/*      */   }
/*      */   
/*      */   public Object login(String person_code, String pswd) throws RemoteException
/*      */   {
/* 1419 */     Object obj = "-1";
/* 1420 */     Session s = HibernateUtil.currentSession();
/* 1421 */     Transaction tx = s.beginTransaction();
/* 1422 */     String pass = "";
/*      */     try {
/* 1424 */       pswd = SysUtil.objToStr(pswd);
/* 1425 */       String saname = "sa";
/* 1426 */       List list = s.createSQLQuery("select SysParameter_value from SysParameter where sysParameter_key='SysManPass'").list();
/* 1427 */       if ((list == null) || (list.isEmpty())) {
/* 1428 */         s.createSQLQuery("insert into SysParameter(SysParameter_key,SysParameter_name,SysParameter_value,SysParameter_code) values('SysManPass','SysManPass','','" + saname + "')").executeUpdate();
/* 1429 */         tx.commit();
/*      */       } else {
/* 1431 */         pass = list.get(0) == null ? "" : list.get(0).toString();
/*      */       }
/* 1433 */       if (saname.equals(person_code)) {
/* 1434 */         if (!pswd.equals(pass)) {
/* 1435 */           obj = "0";
/*      */         } else {
/* 1437 */           Object[] result = new Object[1];
/* 1438 */           result[0] = "1";
/* 1439 */           obj = result;
/*      */         }
/*      */       } else {
/* 1442 */         String sql = "select apw.a01password_key,apw.pswd,apw.a01_key,a01.a0101,a01.deptCode_key from A01PassWord apw,A01 a01 where apw.a01_key=a01.a01_key and rtrim(ltrim(a01.a0190))='" + person_code + "'";
/* 1443 */         list = s.createSQLQuery(sql).list();
/* 1444 */         if ((list == null) || (list.isEmpty())) {
/* 1445 */           obj = "-1";
/*      */         } else {
/* 1447 */           Object[] objs = (Object[])list.get(0);
/* 1448 */           pass = objs[1] == null ? "" : objs[1].toString();
/* 1449 */           if (!pass.equals(pswd)) {
/* 1450 */             obj = "0";
/*      */           } else {
/* 1452 */             Object[] result = new Object[6];
/* 1453 */             result[0] = "0";
/* 1454 */             result[1] = objs[0];
/* 1455 */             result[2] = objs[2];
/* 1456 */             result[3] = objs[3];
/* 1457 */             result[4] = objs[4];
/* 1458 */             List data = s.createSQLQuery("select content,dept_code from deptcode where deptcode_key='" + objs[4] + "'").list();
/* 1459 */             if (data.size() > 0) {
/* 1460 */               result[4] = ((Object[])(Object[])data.get(0))[0];
/* 1461 */               result[5] = ((Object[])(Object[])data.get(0))[1];
/*      */             }
/* 1463 */             result[4] = SysUtil.objToStr(result[4]);
/* 1464 */             obj = result;
/*      */           }
/*      */         }
/*      */       }
/*      */     } catch (Exception e) {
/* 1469 */       obj = "-1";
/* 1470 */       log.error(ImplUtil.getSQLExceptionMsg(e));
/*      */     } finally {
/* 1472 */       HibernateUtil.closeSession();
/*      */     }
/* 1474 */     return obj;
/*      */   }
/*      */   
/*      */   public void logEvent(int time, String logStr) throws RemoteException
/*      */   {
/* 1479 */     if (logStr != null) {
/* 1480 */       log.error("useTime:" + time + " event:" + logStr);
/*      */     }
/*      */   }
/*      */   
/*      */   public List getSysCodes() throws RemoteException
/*      */   {
/* 1486 */     return codes;
/*      */   }
/*      */   
/*      */   public List getSysTrigerField(String entityNames, boolean isTrigerManager) throws RemoteException
/*      */   {
/*      */     
/* 1492 */     if ((entityNames == null | entityNames.trim().equals(""))) {
/* 1493 */       return new ArrayList();
/*      */     }
/* 1495 */     List list = new ArrayList();
/* 1496 */     String[] entitys = entityNames.split(";");
/* 1497 */     for (String entity : entitys) {
/* 1498 */       EntityDef ed = (EntityDef)moduleKeys.get("entity" + entity);
/* 1499 */       if (ed != null)
/*      */       {
/*      */ 
/* 1502 */         for (FieldDef fd : ed.getFieldDefs()) {
/* 1503 */           if (isTrigerManager) {
/* 1504 */             if ((fd.isRelation_flag()) || (fd.isNot_null()) || (fd.isUnique_flag()) || (fd.isRegula_use_flag())) {
/* 1505 */               list.add(fd);
/*      */             }
/*      */           }
/* 1508 */           else if ((fd.isNot_null_save_check()) || (fd.isRegula_save_check())) {
/* 1509 */             list.add(fd);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1514 */     return list;
/*      */   }
/*      */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\CommServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */