/*      */ package org.jhrcore.server;
/*      */ 
/*      */ import com.fr.view.core.DateUtil;
/*      */ import java.io.BufferedOutputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileOutputStream;
/*      */ import java.net.URL;
/*      */ import java.rmi.RemoteException;
/*      */ import java.sql.Connection;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Statement;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import javax.ejb.Remote;
/*      */ import javax.ejb.Stateless;
/*      */ import javax.xml.parsers.DocumentBuilder;
/*      */ import javax.xml.parsers.DocumentBuilderFactory;
/*      */ import org.apache.log4j.Logger;
/*      */ import org.hibernate.Query;
/*      */ import org.hibernate.SQLQuery;
/*      */ import org.hibernate.Session;
/*      */ import org.hibernate.Transaction;
/*      */ import org.jboss.annotation.ejb.Clustered;
/*      */ import org.jhrcore.client.UserContext;
/*      */ import org.jhrcore.comm.CommThreadPool;
/*      */ import org.jhrcore.entity.Code;
/*      */ import org.jhrcore.entity.CommMap;
/*      */ import org.jhrcore.entity.base.EntityClass;
/*      */ import org.jhrcore.entity.base.EntityDef;
/*      */ import org.jhrcore.entity.base.FieldDef;
/*      */ import org.jhrcore.entity.base.InternationBase;
/*      */ import org.jhrcore.entity.base.InternationRes;
/*      */ import org.jhrcore.entity.base.LogData;
/*      */ import org.jhrcore.entity.base.LogInfo;
/*      */ import org.jhrcore.entity.base.ModuleInfo;
/*      */ import org.jhrcore.entity.right.FuntionRight;
/*      */ import org.jhrcore.entity.right.RoleCode;
/*      */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*      */ import org.jhrcore.iservice.SysService;
/*      */ import org.jhrcore.server.jdbc.JdbcUtil;
/*      */ import org.jhrcore.server.util.CountUtil;
/*      */ import org.jhrcore.server.util.ImplUtil;
/*      */ import org.jhrcore.util.DbUtil;
/*      */ import org.jhrcore.util.FileUtil;
/*      */ import org.jhrcore.util.PinYinMa;
/*      */ import org.jhrcore.util.PublicUtil;
/*      */ import org.jhrcore.util.SysUtil;
/*      */ import org.jhrcore.util.UtilTool;
/*      */ import org.jhrcore.util.ZipUtil;
/*      */ import org.w3c.dom.Document;
/*      */ import org.w3c.dom.NamedNodeMap;
/*      */ import org.w3c.dom.Node;
/*      */ import org.w3c.dom.NodeList;
/*      */ 
/*      */ 
/*      */ @Clustered
/*      */ @Stateless
/*      */ @Remote({SysService.class})
/*      */ public class SysServiceImpl
/*      */   extends SuperImpl
/*      */   implements SysService
/*      */ {
/*   69 */   private static Logger log = Logger.getLogger(SysServiceImpl.class);
/*      */   private static String file_dir;
/*      */   private static String filepath;
/*      */   private static String ZIP_DIR;
/*      */   private static URL url;
/*      */   
/*      */   public SysServiceImpl() throws RemoteException {
/*   76 */     super(ServerApp.rmiPort);
/*   77 */     file_dir = System.getProperty("user.dir") + "/" + "update";
/*   78 */     ZIP_DIR = file_dir + "/temp/";
/*   79 */     url = FileUtil.getClassLocationURL(SysServiceImpl.class);
/*      */   }
/*      */   
/*      */   public SysServiceImpl(int port) throws RemoteException {
/*   83 */     super(port);
/*   84 */     file_dir = System.getProperty("user.dir") + "/" + "update";
/*   85 */     ZIP_DIR = file_dir + "/temp/";
/*   86 */     url = FileUtil.getClassLocationURL(SysServiceImpl.class);
/*      */   }
/*      */   
/*      */   public ValidateSQLResult updateFun(byte[] p_byte) throws RemoteException
/*      */   {
/*   91 */     ValidateSQLResult result = new ValidateSQLResult();
/*   92 */     String fileName = ZIP_DIR + "webhr.xml";
/*   93 */     File file2 = new File(fileName);
/*      */     try {
/*   95 */       if (!file2.getParentFile().exists()) {
/*   96 */         file2.getParentFile().mkdirs();
/*      */       }
/*   98 */       if (!file2.getParentFile().exists()) {
/*   99 */         result.setResult(1);
/*  100 */         result.setMsg("文件存放路径错误");
/*  101 */         return result;
/*      */       }
/*  103 */       BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file2));
/*  104 */       output.write(p_byte);
/*  105 */       output.close();
/*  106 */       updateFuntion(false);
/*      */     } catch (Exception ex) {
/*  108 */       log.error(ex);
/*  109 */       result.setResult(1);
/*  110 */       result.setMsg(ImplUtil.getSQLExceptionMsg(ex));
/*      */     }
/*  112 */     return result;
/*      */   }
/*      */   
/*      */   public synchronized ValidateSQLResult uploadFile(byte[] p_byte, String fileName) throws RemoteException
/*      */   {
/*  117 */     ValidateSQLResult result = new ValidateSQLResult();
/*  118 */     if ((fileName == null) || (fileName.equals(""))) {
/*  119 */       result.setResult(1);
/*  120 */       result.setMsg("文件路径不对");
/*  121 */       return result;
/*      */     }
/*  123 */     fileName = fileName.replace("$", file_dir);
/*  124 */     filepath = fileName;
/*  125 */     File file2 = new File(fileName);
/*      */     try {
/*  127 */       if (!file2.getParentFile().exists()) {
/*  128 */         file2.getParentFile().mkdirs();
/*      */       }
/*  130 */       if (!file2.getParentFile().exists()) {
/*  131 */         result.setResult(1);
/*  132 */         result.setMsg("文件存放路径错误");
/*  133 */         return result;
/*      */       }
/*  135 */       BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file2));
/*  136 */       output.write(p_byte);
/*  137 */       output.close();
/*      */     } catch (Exception ex) {
/*  139 */       log.error(ex);
/*  140 */       result.setResult(1);
/*  141 */       result.setMsg(ImplUtil.getSQLExceptionMsg(ex));
/*      */     }
/*  143 */     return result;
/*      */   }
/*      */   
/*      */   public synchronized ValidateSQLResult closeService() throws RemoteException
/*      */   {
/*  148 */     ValidateSQLResult result = new ValidateSQLResult();
/*      */     
/*      */ 
/*      */     try
/*      */     {
/*  153 */       HibernateUtil.closeConnect();
/*      */     }
/*      */     catch (Exception ex)
/*      */     {
/*  157 */       log.error(ex);
/*  158 */       result.setResult(1);
/*  159 */       result.setMsg(ImplUtil.getSQLExceptionMsg(ex));
/*      */     }
/*  161 */     return result;
/*      */   }
/*      */   
/*      */   public synchronized ValidateSQLResult updateVersion() throws RemoteException
/*      */   {
/*  166 */     ValidateSQLResult result = new ValidateSQLResult();
/*      */     try {
/*  168 */       ZipUtil.unZip(filepath, ZIP_DIR);
/*  169 */       updateFuntion(true);
/*  170 */       if (ServerApp.forHTTP) {
/*  171 */         updateFile();
/*      */       }
/*      */     } catch (Exception ex) {
/*  174 */       log.error(ex);
/*  175 */       result.setResult(1);
/*  176 */       result.setMsg(ImplUtil.getSQLExceptionMsg(ex));
/*      */     }
/*  178 */     return result;
/*      */   }
/*      */   
/*      */   public void updateFuntion(boolean excuteSQL) throws Exception {
/*  182 */     File file = new File(ZIP_DIR + "webhr.xml");
/*  183 */     if (!file.exists()) {
/*  184 */       return;
/*      */     }
/*  186 */     List<String[]> datas = new ArrayList();
/*  187 */     List<String> delFuns = new ArrayList();
/*  188 */     List<String[]> updateFuns = new ArrayList();
/*  189 */     List<String> sqls = new ArrayList();
/*  190 */     String db_type = UserContext.sql_dialect;
/*      */     try {
/*  192 */       List<FuntionRight> frs = new ArrayList();
/*  193 */       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
/*  194 */       DocumentBuilder db = dbf.newDocumentBuilder();
/*  195 */       Document doc = db.parse(file);
/*  196 */       NodeList list = doc.getElementsByTagName("Fun");
/*  197 */       int len = list.getLength();
/*  198 */       for (int i = 0; i < len; i++) {
/*  199 */         Node node = list.item(i);
/*  200 */         FuntionRight fr = new FuntionRight();
/*  201 */         fr.setFun_module_flag(node.getAttributes().getNamedItem("fun_module_flag").getNodeValue());
/*  202 */         NodeList childs = node.getChildNodes();
/*  203 */         int c_len = childs.getLength();
/*  204 */         for (int j = 0; j < c_len; j++) {
/*  205 */           Node cnode = childs.item(j);
/*  206 */           if (cnode.getNodeName().equals("Fproperty")) {
/*  207 */             Node pnode = cnode.getAttributes().getNamedItem("name");
/*  208 */             String property = pnode.getNodeValue();
/*  209 */             String text = cnode.getTextContent();
/*  210 */             PublicUtil.setValueBy2(fr, property, text);
/*      */           }
/*      */         }
/*  213 */         frs.add(fr);
/*      */       }
/*  215 */       for (FuntionRight fr : frs) {
/*  216 */         String[] data = new String[6];
/*  217 */         data[0] = ("'" + fr.getFun_code() + "'");
/*  218 */         data[1] = ("'" + fr.getFun_parent_code() + "'");
/*  219 */         data[2] = ("'" + fr.getFun_name() + "'");
/*  220 */         data[3] = ("'" + fr.getFun_module_flag() + "'");
/*  221 */         data[4] = (fr.getFun_level() + "");
/*  222 */         data[5] = (fr.isModule_flag() ? "1" : "0");
/*  223 */         datas.add(data);
/*      */       }
/*  225 */       list = doc.getElementsByTagName("Dproperty");
/*  226 */       len = list.getLength();
/*  227 */       for (int i = 0; i < len; i++) {
/*  228 */         Node node = list.item(i);
/*  229 */         delFuns.add(node.getTextContent());
/*      */       }
/*  231 */       list = doc.getElementsByTagName("Uproperty");
/*  232 */       len = list.getLength();
/*  233 */       for (int i = 0; i < len; i++) {
/*  234 */         Node node = list.item(i);
/*  235 */         updateFuns.add(new String[] { node.getAttributes().getNamedItem("name").getNodeValue(), node.getTextContent() });
/*      */       }
/*  237 */       list = doc.getElementsByTagName(db_type.equals("sqlserver") ? "SQLproperty" : "ORAproperty");
/*  238 */       len = list.getLength();
/*  239 */       for (int i = 0; i < len; i++) {
/*  240 */         Node node = list.item(i);
/*  241 */         sqls.add(node.getTextContent().trim().replace("\t", ""));
/*      */       }
/*      */     } catch (Exception ex) {
/*  244 */       ex.printStackTrace();
/*      */     }
/*  246 */     if ((datas.isEmpty()) && (delFuns.isEmpty()) && (updateFuns.isEmpty())) {
/*  247 */       return;
/*      */     }
/*  249 */     String tableName = CountUtil.getCommTempTable();
/*  250 */     String extra = db_type.equals("sqlserver") ? "" : " from dual";
/*  251 */     String val = db_type.equals("sqlserver") ? "varchar(255)" : "varchar2(255)";
/*  252 */     StringBuilder exSQL = new StringBuilder();
/*  253 */     if (delFuns.size() > 0) {
/*  254 */       List removes = new ArrayList();
/*  255 */       for (String fun : delFuns) {
/*  256 */         if (fun.startsWith("#")) {
/*  257 */           removes.add(fun);
/*  258 */           fun = fun.substring(1);
/*  259 */           exSQL.append("delete from rolefuntion where funtionright_key in(select funtionright_key from funtionright where fun_module_flag like '").append(fun).append("%');");
/*  260 */           exSQL.append("delete from funtionright where fun_module_flag like '").append(fun).append("%';");
/*      */         }
/*      */       }
/*  263 */       delFuns.removeAll(removes);
/*  264 */       exSQL.append(DbUtil.getQueryForMID("delete from rolefuntion where funtionright_key in(select funtionright_key from funtionright where fun_module_flag in", delFuns, ")"));
/*  265 */       exSQL.append(DbUtil.getQueryForMID("delete from funtionright where fun_module_flag in", delFuns));
/*      */     }
/*  267 */     if (updateFuns.size() > 0) {
/*  268 */       for (String[] row : updateFuns) {
/*  269 */         String s_where = "='" + row[0] + "'";
/*  270 */         row[0] = row[0].replace("#", "");
/*  271 */         exSQL.append("update funtionright set fun_module_flag=replace(fun_module_flag,'").append(row[0]).append("','").append(row[1]).append("') where fun_module_flag").append(s_where).append(";");
/*      */       }
/*      */     }
/*  274 */     Connection c = JdbcUtil.getConnection();
/*  275 */     Statement statement = null;
/*  276 */     ResultSet rs = null;
/*      */     try {
/*  278 */       statement = c.createStatement();
/*  279 */       if (datas.size() > 0) {
/*  280 */         String sql = DbUtil.isTableExistSQL(tableName, db_type);
/*  281 */         rs = statement.executeQuery(sql);
/*  282 */         while (rs.next()) {
/*  283 */           int size = rs.getInt(1);
/*  284 */           if (size > 0) {
/*  285 */             exSQL.append("drop table ").append(tableName).append(";");
/*      */           }
/*      */         }
/*  288 */         rs = null;
/*  289 */         exSQL.append("create table ").append(tableName).append("(fun_code ").append(val).append(",fun_parent_code ").append(val).append(",fun_name ").append(val).append(",fun_module_flag ").append(val).append(",fun_level int,module_flag int);");
/*  290 */         exSQL.append(DbUtil.getInstrForMID("insert into " + tableName + "(fun_code,fun_parent_code,fun_name,fun_module_flag,fun_level,module_flag)", datas, extra, ";"));
/*  291 */         String uid = DbUtil.getUIDForDb(db_type);
/*  292 */         exSQL.append("insert into FuntionRight(funtionright_key,fun_code,fun_parent_code,fun_name,fun_module_flag,fun_level,granted,module_flag,visible)select ").append(uid).append(",fun_code,fun_parent_code,fun_name,fun_module_flag,fun_level,0,module_flag,1 from ").append(tableName).append(" a where not exists(select 1 from funtionright fr where fr.fun_module_flag=a.fun_module_flag").append(");");
/*  293 */         if (db_type.equals("sqlserver")) {
/*  294 */           exSQL.append("update funtionright set fun_code=b.fun_code,fun_parent_code=b.fun_parent_code,fun_name=b.fun_name,module_flag=b.module_flag from funtionright fr,").append(tableName).append(" b where fr.fun_module_flag=b.fun_module_flag;");
/*      */         } else {
/*  296 */           exSQL.append("update funtionright fr set (fun_code,fun_parent_code,fun_name,module_flag)=(select b.fun_code,b.fun_parent_code,b.fun_name,b.module_flag from ").append(tableName).append(" b where fr.fun_module_flag=b.fun_module_flag) where exists(select 1 from ").append(tableName).append(" b where b.fun_module_flag=fr.fun_module_flag);");
/*      */         }
/*  298 */         exSQL.append("drop table ").append(tableName);
/*      */       }
/*  300 */       if (excuteSQL) {
/*  301 */         for (String sql : sqls) {
/*      */           try {
/*  303 */             statement.executeUpdate(sql);
/*      */           }
/*      */           catch (Exception ex) {
/*  306 */             ex.printStackTrace();
/*  307 */             log.error("sql执行错误:" + sql + "\n" + ImplUtil.getSQLExceptionMsg(ex));
/*  308 */             throw ex;
/*      */           }
/*      */         }
/*      */       }
/*  312 */       String[] ex_str = exSQL.toString().split(";");
/*  313 */       for (String tmp : ex_str) {
/*  314 */         if (!"".equals(tmp.trim()))
/*      */         {
/*      */           try
/*      */           {
/*  318 */             statement.executeUpdate(tmp);
/*      */           }
/*      */           catch (Exception ex) {
/*  321 */             ex.printStackTrace();
/*  322 */             log.error("sql执行错误:" + tmp + "\n" + ImplUtil.getSQLExceptionMsg(ex));
/*  323 */             throw ex;
/*      */           } }
/*      */       }
/*  326 */       c.commit();
/*      */     } catch (Exception ex) {
/*  328 */       ex.printStackTrace();
/*  329 */       log.error(ex);
/*      */     } finally {
/*  331 */       closeConnection(c);
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public static void updateFile()
/*      */     throws Exception
/*      */   {
/*  343 */     File file = new File(ZIP_DIR + "webhr.xml");
/*  344 */     if (!file.exists()) {
/*  345 */       log.error("webhr.xml not exists");
/*  346 */       return;
/*      */     }
/*  348 */     String strs = url.toString().split("/hrserver.jar")[0];
/*  349 */     String $server = strs.split("file:/")[1];
/*  350 */     String $client = System.getProperty("user.dir") + "/client";
/*  351 */     boolean isWindows = System.getProperty("os.name").toUpperCase().startsWith("WIN");
/*  352 */     if (!isWindows) {
/*  353 */       $server = "/" + $server;
/*      */     }
/*  355 */     log.error("$os:" + System.getProperty("os.name"));
/*  356 */     log.error("$server:" + $server);
/*  357 */     log.error("$client:" + $client);
/*  358 */     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
/*  359 */     DocumentBuilder db = dbf.newDocumentBuilder();
/*  360 */     Document doc = db.parse(file);
/*  361 */     NodeList list = doc.getElementsByTagName("JARproperty");
/*  362 */     int len = list.getLength();
/*  363 */     for (int i = 0; i < len; i++) {
/*  364 */       Node node = list.item(i);
/*  365 */       String fileName = node.getAttributes().getNamedItem("name").getNodeValue();
/*  366 */       boolean del_flag = SysUtil.objToBoolean(node.getAttributes().getNamedItem("del_flag").getNodeValue());
/*  367 */       String updatePath = node.getTextContent();
/*  368 */       updatePath = updatePath.replace("$server", $server);
/*  369 */       updatePath = updatePath.replace("$client", $client);
/*  370 */       if (del_flag) {
/*  371 */         log.error("del file:" + updatePath + "/" + fileName);
/*  372 */         FileUtil.deleteFile(updatePath + "/" + fileName);
/*      */       } else {
/*  374 */         String os = System.getProperty("os.arch");
/*  375 */         if ((!os.endsWith("64")) || (!fileName.startsWith("$32")))
/*      */         {
/*  377 */           if (!fileName.startsWith("$64"))
/*      */           {
/*      */ 
/*  380 */             File dstFile = new File(updatePath + "/" + fileName.replace("$32", "").replace("$64", ""));
/*  381 */             File srcFile = new File(ZIP_DIR + fileName);
/*  382 */             log.error("srcFile:" + srcFile.getPath());
/*  383 */             log.error("dstFile:" + dstFile.getPath());
/*  384 */             if (dstFile.exists()) {
/*      */               try {
/*  386 */                 FileUtil.CopyFile(srcFile, dstFile);
/*      */               } catch (Exception ex) {
/*  388 */                 log.error("copy error:" + dstFile + ";" + ex);
/*      */               }
/*      */             } else
/*      */               try {
/*  392 */                 FileUtil.writeFile(dstFile.getPath(), FileUtil.readFileToByte(srcFile));
/*      */               } catch (Exception ex) {
/*  394 */                 log.error("write error:" + dstFile + ";" + ex);
/*      */               }
/*      */           } }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public void closeConnection(Connection conn) {
/*  402 */     if (conn == null) {
/*  403 */       return;
/*      */     }
/*      */     try {
/*  406 */       if (!conn.isClosed()) {
/*  407 */         conn.close();
/*      */       }
/*      */     } catch (SQLException e) {
/*  410 */       log.error(e.getMessage());
/*      */     } finally {
/*  412 */       if (conn != null) {
/*  413 */         conn = null;
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public List fetchBackSchemes() throws RemoteException
/*      */   {
/*  420 */     String hql = "from SysBackScheme ss left join fetch ss.sysBackFields left join fetch ss.funtionRight order by ss.scheme_no";
/*  421 */     List list = HibernateUtil.fetchEntitiesBy(hql);
/*  422 */     Object obj; for (Iterator i$ = list.iterator(); i$.hasNext(); obj = i$.next()) {}
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  430 */     return list;
/*      */   }
/*      */   
/*      */   public List getBackVersionByCode(String moduleCode) throws RemoteException
/*      */   {
/*  435 */     String hql = "from SysBackVersion where ver_no like '" + moduleCode + "%'";
/*  436 */     return HibernateUtil.fetchEntitiesBy(hql);
/*      */   }
/*      */   
/*      */   public List getBackData(String verNo, String quickStr, String qsStr) throws RemoteException
/*      */   {
/*  441 */     String hql = "select sysBackData_key from SysBackData_" + verNo.substring(0, verNo.indexOf("_")) + " where ver_no='" + verNo + "'";
/*  442 */     quickStr = SysUtil.objToStr(quickStr);
/*  443 */     if ((quickStr != null) && (!quickStr.equals(""))) {
/*  444 */       hql = hql + " and (" + quickStr + ")";
/*      */     }
/*  446 */     if ((qsStr != null) && (!qsStr.equals(""))) {
/*  447 */       hql = hql + " and sysBackData_key in(" + qsStr + ")";
/*      */     }
/*  449 */     return HibernateUtil.selectSQL(hql, false, 0);
/*      */   }
/*      */   
/*      */   public ValidateSQLResult backData(String moduleCode, String where) throws RemoteException
/*      */   {
/*  454 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  455 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult importRes(List langList, List baseList, List resList) throws RemoteException
/*      */   {
/*  460 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  461 */     String tableName = CountUtil.getCommTempTable();
/*  462 */     String dbType = HibernateUtil.getDb_type();
/*  463 */     String uid = DbUtil.getUIDForDb(dbType);
/*  464 */     String val = dbType.equals("sqlserver") ? "varchar(255)" : "varchar2(255)";
/*  465 */     String numStr = dbType.equals("sqlserver") ? "tinyint" : "NUMBER(1)";
/*  466 */     Session session = HibernateUtil.currentSession();
/*  467 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  469 */       for (Object obj : langList) {
/*  470 */         session.saveOrUpdate(obj);
/*      */       }
/*  472 */       session.flush();
/*  473 */       ImplUtil.dropTempTable(session, tableName);
/*  474 */       String sql = "create table " + tableName + "(res_key " + val + ",res_text " + val + ",res_flag " + val + ",res_type " + val + ",used " + numStr + ");";
/*  475 */       List<String[]> data = new ArrayList();
/*  476 */       for (Object obj : baseList) {
/*  477 */         InternationBase ib = (InternationBase)obj;
/*  478 */         data.add(new String[] { "'" + ib.getRes_key() + "'", "'" + ib.getRes_text() + "'", "'" + ib.getRes_flag() + "'", "'" + ib.getRes_type() + "'", ib.isUsed() ? "1" : "0" });
/*      */       }
/*  480 */       sql = sql + DbUtil.getInstrForMID(new StringBuilder().append("insert into ").append(tableName).append("(res_key,res_text,res_flag,res_type,used)").toString(), data, "", ";");
/*  481 */       sql = sql + "insert into InternationBase(InternationBase_key,res_key,res_text,res_flag,res_type,used)select res_key,res_key,res_text,res_flag,res_type,used from " + tableName + " t where not exists(select 1 from InternationBase b where b.res_key=t.res_key);";
/*  482 */       if ("sqlserver".equals(dbType)) {
/*  483 */         sql = sql + "update InternationBase set res_text=t.res_text,res_flag=t.res_flag,res_type=t.res_type from " + tableName + " t where t.res_key=InternationBase.res_key and (InternationBase.res_text=null or InternationBase.res_text='');";
/*      */         
/*  485 */         sql = sql + "update InternationBase set used=t.used from " + tableName + " t where t.res_key=InternationBase.res_key ;";
/*      */       }
/*      */       else {
/*  488 */         sql = sql + "update InternationBase set (res_text,res_flag,res_type)=(select t.res_text,t.res_flag,t.res_type from " + tableName + " t where t.res_key=InternationBase.res_key) where exists(select 1 from " + tableName + " t where t.res_key=InternationBase.res_key) and trim(InternationBase.res_text) is null;";
/*      */         
/*  490 */         sql = sql + "update InternationBase set (used)=(select t.used from " + tableName + " t where t.res_key=InternationBase.res_key) where exists(select 1 from " + tableName + " t where t.res_key=InternationBase.res_key);";
/*      */       }
/*      */       
/*  493 */       sql = sql + "drop table " + tableName + ";";
/*  494 */       data.clear();
/*  495 */       sql = sql + "create table " + tableName + "(res_key " + val + ",res_value " + val + ",language " + val + ");";
/*  496 */       for (Object obj : resList) {
/*  497 */         InternationRes ib = (InternationRes)obj;
/*  498 */         data.add(new String[] { "'" + ib.getRes_key() + "'", "'" + ib.getRes_value() + "'", "'" + ib.getLanguage() + "'" });
/*      */       }
/*  500 */       sql = sql + DbUtil.getInstrForMID(new StringBuilder().append("insert into ").append(tableName).append("(res_key,res_value,language)").toString(), data, "", ";");
/*  501 */       sql = sql + "insert into InternationRes(InternationRes_key,res_key,res_value,language)select " + uid + ",res_key,res_value,language from " + tableName + " t where not exists(select 1 from InternationRes b where b.res_key=t.res_key and b.language=t.language);";
/*      */       
/*  503 */       if ("sqlserver".equals(dbType)) {
/*  504 */         sql = sql + "update InternationRes set res_value=t.res_value,language=t.language from " + tableName + " t where t.res_key=InternationRes.res_key and (InternationRes.res_value is null or InternationRes.res_value='');";
/*      */       }
/*      */       else {
/*  507 */         sql = sql + "update InternationRes set (res_text,language)=(select t.res_text,t.language from " + tableName + " t where t.res_key=InternationRes.res_key) where exists(select 1 from " + tableName + " t where t.res_key=InternationRes.res_key) and trim(InternationRes.res_text) is null;";
/*      */       }
/*      */       
/*      */ 
/*  511 */       sql = sql + "drop table " + tableName + ";";
/*  512 */       ImplUtil.exSQLs(tx, session, sql, ";");
/*  513 */       tx.commit();
/*      */     } catch (Exception e) {
/*  515 */       ImplUtil.dropTempTable(session, tableName);
/*  516 */       ImplUtil.rollBack(tx);
/*  517 */       validateSQLResult.setResult(1);
/*  518 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  519 */       log.error(validateSQLResult.getMsg());
/*      */     } finally {
/*  521 */       HibernateUtil.closeSession();
/*      */     }
/*  523 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult updateBaseToRes() throws RemoteException
/*      */   {
/*  528 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  529 */     Session session = HibernateUtil.currentSession();
/*  530 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  532 */       ImplUtil.exSQLs(tx, session, updateBaseToRes(session), ";");
/*  533 */       tx.commit();
/*      */     } catch (Exception e) {
/*  535 */       ImplUtil.rollBack(tx);
/*  536 */       validateSQLResult.setResult(1);
/*  537 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  538 */       log.error(validateSQLResult.getMsg());
/*      */     } finally {
/*  540 */       HibernateUtil.closeSession();
/*      */     }
/*  542 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   private String updateBaseToRes(Session session) {
/*  546 */     String dbType = HibernateUtil.getDb_type();
/*  547 */     String uid = DbUtil.getUIDForDb(dbType);
/*  548 */     List langs = session.createSQLQuery("select locale from InternationLang").list();
/*  549 */     String sql = "";
/*  550 */     for (Object obj : langs) {
/*  551 */       sql = sql + "insert into InternationRes(InternationRes_key,res_key,res_value,language)select " + uid + ",res_key,'','" + obj + "' from InternationBase ib where not exists(select 1 from InternationRes ir where ir.res_key=ib.res_key and ir.language='" + obj + "');";
/*      */     }
/*      */     
/*  554 */     return sql;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult updateLocalRes() throws RemoteException
/*      */   {
/*  559 */     List rebuilds = new CommServiceImpl().getSysModule(true, true, true);
/*  560 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  561 */     Session session = HibernateUtil.currentSession();
/*  562 */     Transaction tx = session.beginTransaction();
/*      */     try {
/*  564 */       List ress = session.createQuery("from InternationBase ib  where ib.res_type in('Module','EntityClass','Entity','Field')").list();
/*  565 */       Hashtable<String, InternationBase> ibKeys = new Hashtable();
/*  566 */       for (Object obj : ress) {
/*  567 */         InternationBase ib = (InternationBase)obj;
/*  568 */         ibKeys.put(ib.getRes_key(), ib);
/*      */       }
/*  570 */       List saveList = new ArrayList();
/*  571 */       List updateList = new ArrayList();
/*  572 */       for (Object obj : rebuilds) {
/*  573 */         ModuleInfo mi = (ModuleInfo)obj;
/*  574 */         String key = "Module." + mi.getModule_code();
/*  575 */         createInterBase(key, ibKeys, mi.getModule_name(), "Module", "重构模块", saveList, updateList);
/*  576 */         for (EntityClass ec : mi.getEntityClasss()) {
/*  577 */           key = "Class." + ec.getModuleInfo().getModule_code() + "." + ec.getEntityType_code();
/*  578 */           createInterBase(key, ibKeys, ec.getEntityType_name(), "Class", "重构业务", saveList, updateList);
/*  579 */           for (EntityDef ed : ec.getEntityDefs()) {
/*  580 */             key = "Entity." + ed.getEntityName();
/*  581 */             createInterBase(key, ibKeys, ed.getEntityCaption(), "Entity", "重构表", saveList, updateList);
/*  582 */             for (FieldDef fd : ed.getFieldDefs()) {
/*  583 */               key = "Field." + fd.getEntityDef().getEntityName() + "." + fd.getField_name();
/*  584 */               createInterBase(key, ibKeys, fd.getField_caption(), "Field", "重构字段", saveList, updateList);
/*      */             }
/*      */           }
/*      */         } }
/*      */       String key;
/*  589 */       for (Object obj : saveList) {
/*  590 */         session.save(obj);
/*      */       }
/*  592 */       session.flush();
/*  593 */       for (Object obj : updateList) {
/*  594 */         session.update(obj);
/*      */       }
/*  596 */       session.flush();
/*  597 */       ImplUtil.exSQLs(tx, session, updateBaseToRes(session), ";");
/*  598 */       tx.commit();
/*      */     } catch (Exception e) {
/*  600 */       ImplUtil.rollBack(tx);
/*  601 */       validateSQLResult.setResult(1);
/*  602 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  603 */       log.error(validateSQLResult.getMsg());
/*      */     } finally {
/*  605 */       HibernateUtil.closeSession();
/*      */     }
/*  607 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   private void createInterBase(String key, Hashtable<String, InternationBase> ibKeys, String text, String res_type, String res_flag, List saveList, List updateList) {
/*  611 */     InternationBase ib = (InternationBase)ibKeys.get(key);
/*  612 */     if (ib == null) {
/*  613 */       ib = new InternationBase();
/*  614 */       ib.setInternationBase_key(key);
/*  615 */       ib.setRes_key(key);
/*  616 */       ib.setRes_text(text);
/*  617 */       ib.setRes_type(res_type);
/*  618 */       ib.setRes_flag(res_flag);
/*  619 */       saveList.add(ib);
/*  620 */       ibKeys.put(key, ib);
/*  621 */     } else if (!ib.getRes_text().equals(text)) {
/*  622 */       ib.setRes_text(text);
/*  623 */       updateList.add(ib);
/*      */     }
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
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public ValidateSQLResult changeCodeGrade(List<String[]> list)
/*      */     throws RemoteException
/*      */   {
/*  682 */     ValidateSQLResult result = new ValidateSQLResult();
/*  683 */     String tableName = CountUtil.getCommTempTable();
/*  684 */     String dbType = HibernateUtil.getDb_type();
/*  685 */     String val = dbType.equals("sqlserver") ? "varchar(255)" : "varchar2(255)";
/*  686 */     String sql = "create table " + tableName + "(code_key " + val + ",grades int);";
/*  687 */     Session s = HibernateUtil.currentSession();
/*  688 */     Transaction tx = s.beginTransaction();
/*      */     try {
/*  690 */       ImplUtil.dropTempTable(s, tableName);
/*  691 */       sql = sql + DbUtil.getInstrForMID(new StringBuilder().append("insert into ").append(tableName).append("(code_key,grades)").toString(), list, "", ";");
/*  692 */       if (dbType.equals("sqlserver")) {
/*  693 */         sql = sql + "update Code set grades=t.grades from " + tableName + " t where t.code_key=Code.code_key;";
/*      */       } else {
/*  695 */         sql = sql + "update Code set grades=(select grades from " + tableName + " t where t.code_key=Code.code_key) where exists (" + "select 1 from " + tableName + " t where t.code_key=Code.code_key);";
/*      */       }
/*      */       
/*  698 */       sql = sql + "drop table " + tableName;
/*  699 */       ImplUtil.exSQLs(tx, s, sql, ";");
/*  700 */       tx.commit();
/*      */     } catch (Exception e) {
/*  702 */       ImplUtil.dropTempTable(s, tableName);
/*  703 */       ImplUtil.rollBack(tx);
/*  704 */       result.setResult(1);
/*  705 */       result.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  706 */       log.error(result.getMsg());
/*      */     } finally {
/*  708 */       HibernateUtil.closeSession();
/*      */     }
/*  710 */     CommThreadPool.getClientThreadPool().handleEvent(new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/*  714 */         CommServiceImpl.rebuildCodes(null);
/*      */       }
/*  716 */     });
/*  717 */     return result;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveCode(final Code code, RoleCode rc) throws RemoteException
/*      */   {
/*  722 */     Session s = HibernateUtil.currentSession();
/*  723 */     Transaction tx = s.beginTransaction();
/*  724 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/*  726 */       Object obj = s.createSQLQuery("select max(code_level) from code where code_type='" + code.getCode_type() + "'").uniqueResult();
/*  727 */       int grades = SysUtil.objToInt(obj);
/*  728 */       grades = grades >= code.getGrades().intValue() ? grades : code.getGrades().intValue();
/*  729 */       s.save(code);
/*  730 */       s.flush();
/*  731 */       String update_sql = "update Code  set grades=" + grades + " where code_type = '" + code.getCode_type() + "' and grades<>" + grades + ";";
/*  732 */       update_sql = update_sql + "update Code set end_flag=0 where code_id='" + code.getParent_id() + "' and code_type='" + code.getCode_type() + "'";
/*  733 */       ImplUtil.exSQLs(tx, s, update_sql, ";");
/*  734 */       if (rc != null) {
/*  735 */         s.save(rc);
/*      */       }
/*  737 */       tx.commit();
/*      */     } catch (Exception e) {
/*  739 */       validateSQLResult.setResult(1);
/*  740 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  741 */       log.error(validateSQLResult.getMsg());
/*  742 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  744 */       HibernateUtil.closeSession();
/*      */     }
/*  746 */     CommThreadPool.getClientThreadPool().handleEvent(new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/*  750 */         CommServiceImpl.rebuildCodes(code);
/*      */       }
/*  752 */     });
/*  753 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult updateCode(final Code code) throws RemoteException
/*      */   {
/*  758 */     Session s = HibernateUtil.currentSession();
/*  759 */     Transaction tx = s.beginTransaction();
/*  760 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/*  762 */       s.update(code);
/*  763 */       s.flush();
/*  764 */       if ((code.getCode_level().intValue() == 1) && (!code.getCode_name().equals(code.getCode_type()))) {
/*  765 */         String up_sql = "update system set code_type_name='" + code.getCode_name() + "' where code_type_name ='" + code.getCode_type() + "';";
/*  766 */         up_sql = up_sql + "update code set code_type ='" + code.getCode_name() + "' where code_type !='" + code.getCode_name() + "' and code_tag like '" + code.getCode_tag() + "%';";
/*  767 */         ImplUtil.exSQLs(tx, s, up_sql, ";");
/*      */       }
/*  769 */       tx.commit();
/*      */     } catch (Exception e) {
/*  771 */       validateSQLResult.setResult(1);
/*  772 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  773 */       log.error(validateSQLResult.getMsg());
/*  774 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  776 */       HibernateUtil.closeSession();
/*      */     }
/*  778 */     CommThreadPool.getClientThreadPool().handleEvent(new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/*  782 */         CommServiceImpl.rebuildCodes(code);
/*      */       }
/*  784 */     });
/*  785 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult delCode(final Code code, int grades) throws RemoteException
/*      */   {
/*  790 */     Session s = HibernateUtil.currentSession();
/*  791 */     Transaction tx = s.beginTransaction();
/*  792 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*      */     try {
/*  794 */       String sql = "delete from Code  where code_tag like'" + code.getCode_tag() + "%';";
/*  795 */       sql = sql + "update Code  set grades=" + grades + " where code_tag like '" + code.getCode_tag() + "%';";
/*  796 */       if (code.getParent_id().equals("ROOT")) {
/*  797 */         sql = sql + " update system set code_type_name = null where code_type_name='" + code.getCode_type() + "';";
/*      */       }
/*  799 */       sql = sql + "update Code set end_flag=1 where code_type='" + code.getCode_type() + "' and not exists(select 1 from Code c where c.parent_id='" + code.getParent_id() + "');";
/*      */       
/*  801 */       ImplUtil.exSQLs(tx, s, sql, ";");
/*  802 */       tx.commit();
/*      */     } catch (Exception e) {
/*  804 */       validateSQLResult.setResult(1);
/*  805 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  806 */       log.error(validateSQLResult.getMsg());
/*  807 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  809 */       HibernateUtil.closeSession();
/*      */     }
/*  811 */     CommThreadPool.getClientThreadPool().handleEvent(new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/*  815 */         CommServiceImpl.rebuildCodes(code);
/*      */       }
/*  817 */     });
/*  818 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult buildCodePYM(List<String[]> codes) throws RemoteException
/*      */   {
/*  823 */     Session s = HibernateUtil.currentSession();
/*  824 */     Transaction tx = s.beginTransaction();
/*  825 */     ValidateSQLResult vs = new ValidateSQLResult();
/*  826 */     SysUtil.sortArrays(codes);
/*      */     try {
/*  828 */       for (Object obj : codes) {
/*  829 */         if (obj != null) {
/*  830 */           String[] str = (String[])obj;
/*  831 */           s.createSQLQuery("update Code set pym='" + str[1] + "' where code_key='" + str[0] + "'").executeUpdate();
/*  832 */           s.flush();
/*      */         }
/*      */       }
/*  835 */       tx.commit();
/*      */     } catch (Exception e) {
/*  837 */       vs.setResult(1);
/*  838 */       vs.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  839 */       log.error(vs.getMsg());
/*  840 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  842 */       HibernateUtil.closeSession();
/*      */     }
/*  844 */     CommThreadPool.getClientThreadPool().handleEvent(new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/*  848 */         CommServiceImpl.rebuildCodes(null);
/*      */       }
/*  850 */     });
/*  851 */     return vs;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult buildFieldPYM(String entity_key) throws RemoteException
/*      */   {
/*  856 */     Session s = HibernateUtil.currentSession();
/*  857 */     Transaction tx = s.beginTransaction();
/*  858 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/*  859 */     String sql = "select field_key,field_caption,pym from system s,tabname t where s.entity_key=t.entity_key ";
/*  860 */     if (!"@".equals(entity_key)) {
/*  861 */       sql = sql + " and  t.entity_key='" + entity_key + "'";
/*      */     }
/*      */     try {
/*  864 */       List list = s.createSQLQuery(sql).list();
/*  865 */       StringBuilder ex_sql = new StringBuilder();
/*  866 */       for (Object obj : list) {
/*  867 */         Object[] objs = (Object[])obj;
/*  868 */         if ((objs[1] != null) && (!objs[1].toString().trim().equals("")))
/*      */         {
/*      */ 
/*  871 */           String pinYin = PinYinMa.ctoE(objs[1].toString());
/*  872 */           if ((objs[2] == null) || (!objs[2].equals(pinYin)))
/*  873 */             ex_sql.append("update system set pym='").append(pinYin).append("' where field_key='").append(objs[0]).append("';");
/*      */         }
/*      */       }
/*  876 */       ImplUtil.exSQLs(tx, s, ex_sql.toString(), ";");
/*  877 */       tx.commit();
/*  878 */       CommServiceImpl.moduleChange = true;
/*      */     } catch (Exception e) {
/*  880 */       validateSQLResult.setResult(1);
/*  881 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  882 */       log.error(validateSQLResult.getMsg());
/*      */     } finally {
/*  884 */       HibernateUtil.closeSession();
/*      */     }
/*  886 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveFieldFormat(List<FieldDef> fds) throws RemoteException
/*      */   {
/*  891 */     Session s = HibernateUtil.currentSession();
/*  892 */     Transaction tx = s.beginTransaction();
/*  893 */     ValidateSQLResult vs = new ValidateSQLResult();
/*  894 */     SysUtil.sortListByStr(fds, "field_key");
/*      */     try {
/*  896 */       for (Object obj : fds) {
/*  897 */         if (obj != null) {
/*  898 */           s.update(obj);
/*      */         }
/*      */       }
/*  901 */       tx.commit();
/*  902 */       CommServiceImpl.moduleChange = true;
/*      */     } catch (Exception e) {
/*  904 */       vs.setResult(1);
/*  905 */       vs.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  906 */       log.error(vs.getMsg());
/*  907 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  909 */       HibernateUtil.closeSession();
/*      */     }
/*  911 */     return vs;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult delSystemField(Object obj) throws RemoteException
/*      */   {
/*  916 */     ValidateSQLResult vs = new ValidateSQLResult();
/*  917 */     String tableName = CountUtil.getCommTempTable();
/*  918 */     String db_type = HibernateUtil.getDb_type();
/*  919 */     String connStr = DbUtil.getPlusStr(db_type);
/*  920 */     HashSet<String> viewTables = new HashSet();
/*  921 */     viewTables.add("A01");
/*  922 */     viewTables.add("DeptCode");
/*  923 */     viewTables.add("C21");
/*  924 */     boolean viewUpdate = false;
/*  925 */     Session s = HibernateUtil.currentSession();
/*  926 */     Transaction tx = s.beginTransaction();
/*      */     try {
/*  928 */       DbUtil.initTempTable(s, tableName, db_type);
/*  929 */       String create_sql = "";
/*  930 */       if (db_type.equals("sqlserver")) {
/*  931 */         create_sql = "create table " + tableName + " (f_key varchar(255),e_f_name varchar(150))";
/*      */       } else {
/*  933 */         create_sql = "create table " + tableName + " (f_key varchar2(255),e_f_name varchar2(150))";
/*      */       }
/*  935 */       s.createSQLQuery(create_sql).executeUpdate();
/*  936 */       s.flush();
/*  937 */       String f_sql = "";
/*  938 */       String ext_sql = "";
/*  939 */       if ((obj instanceof FieldDef)) {
/*  940 */         FieldDef fd = (FieldDef)obj;
/*  941 */         f_sql = "insert into " + tableName + "(f_key,e_f_name) select distinct field_key,entityname" + connStr + "'.'" + connStr + "field_name from tabname t,system s where t.entity_key=s.entity_key " + "and (s.field_key ='" + fd.getField_key() + "' or (t.entityname='PayBudgetDept' and s.field_name='" + fd.getField_name() + "'))";
/*      */         
/*  943 */         s.createSQLQuery(f_sql).executeUpdate();
/*  944 */         List list = s.createSQLQuery("select e_f_name from " + tableName).list();
/*  945 */         for (Object data : list) {
/*  946 */           String[] r_data = data.toString().split("\\.");
/*  947 */           ext_sql = DbUtil.isFieldExistSQL(r_data[0], r_data[1], db_type);
/*  948 */           if (Integer.valueOf(s.createSQLQuery(ext_sql).list().get(0).toString()).intValue() != 0) {
/*  949 */             s.createSQLQuery("alter table " + r_data[0] + " drop column " + r_data[1]).executeUpdate();
/*      */           }
/*      */         }
/*  952 */         s.flush();
/*  953 */         s.createSQLQuery("delete from rolefield where field_name in(select e_f_name from " + tableName + ")").executeUpdate();
/*  954 */         s.createSQLQuery("delete from system where field_key in(select f_key from " + tableName + ")").executeUpdate();
/*  955 */         if (fd.getEntityDef().getEntityClass().getEntityType_code().equals("XCDZ")) {
/*  956 */           s.createSQLQuery("delete from system where entity_key='" + fd.getEntityDef().getEntity_key() + "' and field_name='" + fd.getField_name() + "_c'").executeUpdate();
/*  957 */           s.createSQLQuery("delete from CompareFieldCode where entity_name='" + fd.getEntityDef().getEntityName() + "' and field_name='" + fd.getField_name() + "'").executeUpdate();
/*      */         }
/*  959 */         if (viewTables.contains(fd.getEntityDef().getEntityName())) {
/*  960 */           viewUpdate = true;
/*      */         }
/*  962 */       } else if ((obj instanceof EntityDef)) {
/*  963 */         EntityDef ed = (EntityDef)obj;
/*  964 */         DbUtil.initTempTable(s, ed.getEntityName(), db_type);
/*  965 */         f_sql = "insert into " + tableName + "(f_key,e_f_name) select t.entity_key,entityname" + connStr + "'.'" + connStr + "field_name from tabname t,system s where s.entity_key=t.entity_key and t.entity_key='" + ed.getEntity_key() + "'";
/*  966 */         s.createSQLQuery(f_sql).executeUpdate();
/*  967 */         s.createSQLQuery("delete from rolefield where field_name in (select e_f_name from " + tableName + ")").executeUpdate();
/*  968 */         s.createSQLQuery("delete from system where entity_key='" + ed.getEntity_key() + "'").executeUpdate();
/*  969 */         s.createSQLQuery("delete from roleentity where entity_key='" + ed.getEntity_key() + "'").executeUpdate();
/*  970 */         s.createSQLQuery("delete from tabname where entity_key='" + ed.getEntity_key() + "'").executeUpdate();
/*  971 */         s.createSQLQuery("delete from CompareFieldCode where entity_name='" + ed.getEntityName() + "'").executeUpdate();
/*      */       }
/*  973 */       s.flush();
/*  974 */       if (viewUpdate) {
/*  975 */         s.createSQLQuery("update SysParameter set sysparameter_value='1' where sysParameter_key='HRViewUpdate'").executeUpdate();
/*      */       }
/*  977 */       tx.commit();
/*  978 */       CommServiceImpl.moduleChange = true;
/*      */     } catch (Exception e) {
/*  980 */       vs.setResult(1);
/*  981 */       vs.setMsg(ImplUtil.getSQLExceptionMsg(e));
/*  982 */       ImplUtil.dropTempTable(s, tableName);
/*  983 */       log.error(vs.getMsg());
/*  984 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/*  986 */       HibernateUtil.closeSession();
/*      */     }
/*  988 */     return vs;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveSystemChange(List saveList, List updateList, List<String> delKeys) throws RemoteException
/*      */   {
/*  993 */     EntityDef ed = null;
/*  994 */     boolean tableUpdate = false;
/*  995 */     boolean viewUpdate = false;
/*  996 */     HashSet<String> viewTables = new HashSet();
/*  997 */     viewTables.add("A01");
/*  998 */     viewTables.add("DeptCode");
/*  999 */     viewTables.add("C21");
/* 1000 */     ValidateSQLResult validateSQLResult = new ValidateSQLResult();
/* 1001 */     Session s = HibernateUtil.currentSession();
/* 1002 */     Transaction tx = s.beginTransaction();
/*      */     try {
/* 1004 */       for (Object obj : saveList) {
/* 1005 */         if ((obj instanceof FieldDef)) {
/* 1006 */           FieldDef fd = (FieldDef)obj;
/* 1007 */           if ((fd.getEntityDef().getEntityName().equals("PayBudgetA01")) && (ed == null)) {
/* 1008 */             ed = (EntityDef)s.createQuery("from EntityDef ed where ed.entityName='PayBudgetDept'").uniqueResult();
/*      */           }
/*      */         }
/*      */       }
/* 1012 */       for (Object obj : updateList) {
/* 1013 */         if ((obj instanceof FieldDef)) {
/* 1014 */           FieldDef fd = (FieldDef)obj;
/* 1015 */           if ((fd.getEntityDef().getEntityName().equals("PayBudgetA01")) && (ed == null)) {
/* 1016 */             ed = (EntityDef)s.createQuery("from EntityDef ed where ed.entityName='PayBudgetDept'").uniqueResult();
/*      */           }
/*      */         }
/*      */       }
/* 1020 */       for (Object obj : saveList) {
/* 1021 */         if ((obj instanceof EntityDef)) {
/* 1022 */           s.save(obj);
/* 1023 */           tableUpdate = true;
/*      */         }
/*      */       }
/* 1026 */       s.flush();
/* 1027 */       for (Object obj : saveList) {
/* 1028 */         if ((obj instanceof FieldDef)) {
/* 1029 */           FieldDef fd = (FieldDef)obj;
/* 1030 */           tableUpdate = true;
/* 1031 */           if (viewTables.contains(fd.getEntityDef().getEntityName())) {
/* 1032 */             viewUpdate = true;
/*      */           }
/* 1034 */           s.save(fd);
/* 1035 */           if (fd.getEntityDef().getEntityClass().getEntityType_code().equals("XCDZ")) {
/* 1036 */             if (fd.getField_type().toLowerCase().equals("string"))
/*      */             {
/*      */ 
/* 1039 */               FieldDef new_fd = (FieldDef)UtilTool.createUIDEntity(FieldDef.class);
/* 1040 */               new_fd.setField_name(fd.getField_name() + "_c");
/* 1041 */               new_fd.setField_caption(fd.getField_caption());
/* 1042 */               new_fd.setVisible(false);
/* 1043 */               new_fd.setEntityDef(fd.getEntityDef());
/* 1044 */               new_fd.setField_align(fd.getField_align());
/* 1045 */               new_fd.setField_mark(fd.getField_mark());
/* 1046 */               new_fd.setField_scale(fd.getField_scale());
/* 1047 */               new_fd.setField_type(fd.getField_type());
/* 1048 */               new_fd.setField_width(fd.getField_width());
/* 1049 */               new_fd.setOrder_no(fd.getOrder_no() + 1);
/* 1050 */               new_fd.setView_width(fd.getView_width());
/* 1051 */               s.save(new_fd);
/* 1052 */             } } else if (fd.getEntityDef().getEntityName().equals("PayBudgetA01"))
/* 1053 */             if (ed != null)
/*      */             {
/*      */ 
/* 1056 */               s.evict(fd);
/* 1057 */               FieldDef new_fd = fd;
/* 1058 */               new_fd.setField_key(UtilTool.getUID());
/* 1059 */               new_fd.setEntityDef(ed);
/* 1060 */               s.save(new_fd);
/*      */             }
/*      */         }
/*      */       }
/* 1064 */       s.flush();
/* 1065 */       List u_entity = new ArrayList();
/* 1066 */       List u_field = new ArrayList();
/* 1067 */       for (Object obj : updateList) {
/* 1068 */         if ((obj instanceof EntityDef)) {
/* 1069 */           u_entity.add(obj);
/* 1070 */         } else if ((obj instanceof FieldDef)) {
/* 1071 */           u_field.add(obj);
/*      */         }
/*      */       }
/* 1074 */       SysUtil.sortListByStr(u_entity, "entity_key");
/* 1075 */       for (Object obj : u_entity) {
/* 1076 */         s.update(obj);
/*      */       }
/* 1078 */       s.flush();
/* 1079 */       SysUtil.sortListByStr(u_field, "field_key");
/* 1080 */       Hashtable<String, FieldDef> fieldKeys = new Hashtable();
/* 1081 */       for (Object obj : u_field) {
/* 1082 */         FieldDef fd = (FieldDef)obj;
/* 1083 */         s.update(fd);
/* 1084 */         fieldKeys.put(fd.getField_key(), fd);
/* 1085 */         if (fd.getEntityDef().getEntityClass().getEntityType_code().equals("XCDZ")) {
/* 1086 */           if (fd.getField_type().toLowerCase().equals("string"))
/*      */           {
/*      */ 
/* 1089 */             s.createSQLQuery("update system set field_caption ='" + fd.getField_caption() + "' where entity_key='" + fd.getEntityDef().getEntity_key() + "' and field_name='" + fd.getField_name() + "_c'").executeUpdate(); }
/* 1090 */         } else if (fd.getEntityDef().getEntityName().equals("PayBudgetA01"))
/* 1091 */           if (ed != null)
/*      */           {
/*      */ 
/* 1094 */             String ex_sql = "update system set field_caption='" + fd.getField_caption() + "',view_width=" + fd.getView_width() + ",format='" + fd.getFormat() + "'" + ",code_type_name='" + fd.getCode_type_name() + "',field_align='" + fd.getField_align() + "',field_mark='" + fd.getField_mark() + "' where entity_key='" + ed.getEntity_key() + "' and field_name='" + fd.getField_name() + "'";
/*      */             
/* 1096 */             s.createSQLQuery(ex_sql).executeUpdate();
/*      */           }
/*      */       }
/* 1099 */       if (delKeys.size() > 0) {
/* 1100 */         s.flush();
/* 1101 */         String keyStr = "";
/* 1102 */         for (String key : delKeys) {
/* 1103 */           keyStr = keyStr + ",'" + key + "'";
/*      */         }
/* 1105 */         keyStr = keyStr.substring(1);
/* 1106 */         s.createSQLQuery("update system set field_mark='自定义备选项',used_flag=0 where field_key in(" + keyStr + ")").executeUpdate();
/* 1107 */         for (String key : delKeys) {
/* 1108 */           FieldDef fd = (FieldDef)fieldKeys.get(key);
/* 1109 */           if (fd != null)
/*      */           {
/*      */ 
/* 1112 */             if (fd.getEntityDef().getEntityClass().getEntityType_code().equals("XCDZ")) {
/* 1113 */               if (fd.getField_type().toLowerCase().equals("string"))
/*      */               {
/*      */ 
/* 1116 */                 s.createSQLQuery("update system set field_mark='自定义备选项',used_flag=0 where entity_key='" + fd.getEntityDef().getEntity_key() + "' and field_name='" + fd.getField_name() + "_c'").executeUpdate(); }
/* 1117 */             } else if (fd.getEntityDef().getEntityName().equals("PayBudgetA01"))
/* 1118 */               if (ed != null)
/*      */               {
/*      */ 
/* 1121 */                 s.createSQLQuery("update system set field_mark='自定义备选项',used_flag=0 where entity_key='" + ed.getEntity_key() + "' and field_name='" + fd.getField_name() + "'").executeUpdate(); }
/*      */           }
/*      */         }
/*      */       }
/* 1125 */       if (tableUpdate) {
/* 1126 */         s.createSQLQuery("update SysParameter set sysparameter_value='1' where sysParameter_key='HRTableUpdate'").executeUpdate();
/*      */       }
/* 1128 */       if (viewUpdate) {
/* 1129 */         s.createSQLQuery("update SysParameter set sysparameter_value='1' where sysParameter_key='HRViewUpdate'").executeUpdate();
/*      */       }
/* 1131 */       CommServiceImpl.moduleChange = true;
/* 1132 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1134 */       validateSQLResult.setResult(1);
/* 1135 */       validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1136 */       log.error(validateSQLResult.getMsg());
/* 1137 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1139 */       HibernateUtil.closeSession();
/*      */     }
/* 1141 */     return validateSQLResult;
/*      */   }
/*      */   
/*      */   public HashMap getSchemeNum(List<String> list_scheme_table, String where_sql) throws RemoteException
/*      */   {
/* 1146 */     ValidateSQLResult result = new ValidateSQLResult();
/* 1147 */     Session s = HibernateUtil.currentSession();
/* 1148 */     HashMap<String, Integer> result_map = new HashMap();
/*      */     try {
/* 1150 */       for (String temp_table : list_scheme_table) {
/* 1151 */         String sql = "select count(1) from " + temp_table + " n where " + where_sql;
/* 1152 */         List num_list = s.createSQLQuery(sql).list();
/* 1153 */         int num = 0;
/* 1154 */         if ((num_list != null) && (num_list.get(0) != null) && (!"".equals(num_list.get(0).toString()))) {
/* 1155 */           num = Integer.parseInt(num_list.get(0).toString());
/*      */         }
/* 1157 */         result_map.put(temp_table, Integer.valueOf(num));
/*      */       }
/*      */     } catch (Exception e) {
/* 1160 */       result.setResult(1);
/* 1161 */       result.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1162 */       log.error(result.getMsg());
/*      */     } finally {
/* 1164 */       HibernateUtil.closeSession();
/*      */     }
/* 1166 */     return result_map;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveLogField(List list) throws RemoteException
/*      */   {
/* 1171 */     ValidateSQLResult result = new ValidateSQLResult();
/* 1172 */     Session s = HibernateUtil.currentSession();
/* 1173 */     Transaction tx = s.beginTransaction();
/*      */     try {
/* 1175 */       String sql = "delete from SysParameter where sysparameter_key like 'SysLogData%'";
/* 1176 */       s.createSQLQuery(sql).executeUpdate();
/* 1177 */       s.flush();
/* 1178 */       for (Object obj : list) {
/* 1179 */         s.save(obj);
/*      */       }
/* 1181 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1183 */       ImplUtil.rollBack(tx);
/* 1184 */       result.setResult(1);
/* 1185 */       result.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1186 */       log.error(result.getMsg());
/*      */     } finally {
/* 1188 */       HibernateUtil.closeSession();
/*      */     }
/* 1190 */     return result;
/*      */   }
/*      */   
/*      */   public void saveUserLog(Object li)
/*      */   {
/* 1195 */     if ((li instanceof LogInfo)) {
/* 1196 */       ((LogInfo)li).setLog_date(new Date());
/* 1197 */     } else if ((li instanceof LogData)) {
/* 1198 */       ((LogData)li).setLogDate(new Date());
/*      */     }
/* 1200 */     HibernateUtil.save(li);
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveFieldRegula(Object obj) throws RemoteException
/*      */   {
/* 1205 */     ValidateSQLResult vs = HibernateUtil.update(obj);
/* 1206 */     if (vs.getResult() == 0) {
/* 1207 */       CommServiceImpl.moduleChange = true;
/*      */     }
/* 1209 */     return vs;
/*      */   }
/*      */   
/*      */   public ValidateSQLResult saveUsers(List<String> keys, CommMap comm) throws RemoteException
/*      */   {
/* 1214 */     ValidateSQLResult result = new ValidateSQLResult();
/* 1215 */     String db_type = HibernateUtil.getDb_type();
/* 1216 */     String sql = "";
/* 1217 */     String mapKey = comm.getMap_key();
/* 1218 */     String mapType = comm.getMap_type();
/* 1219 */     String mapName = comm.getMap_name();
/* 1220 */     String mapUser = comm.getC_user();
/* 1221 */     String mapUserKey = comm.getC_user_key();
/* 1222 */     String mapDate = DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd", db_type);
/* 1223 */     String para = "'" + SysUtil.objToStr(comm.getMap_para()).replace("'", "''") + "'";
/* 1224 */     for (String key : keys) {
/* 1225 */       sql = sql + "insert into CommMap(commMap_key,user_key,map_type,map_key,map_name,c_date,c_user,c_user_key,map_para)values(";
/* 1226 */       if ("sqlserver".equals(db_type)) {
/* 1227 */         sql = sql + "newid(),'";
/*      */       } else {
/* 1229 */         sql = sql + "sys_guid(),'";
/*      */       }
/* 1231 */       sql = sql + key + "','" + mapType + "','" + mapKey + "','" + mapName + "'," + mapDate + ",'" + mapUser + "','" + mapUserKey + "'," + para + ")##";
/*      */     }
/*      */     
/* 1234 */     Session session = HibernateUtil.currentSession();
/* 1235 */     Transaction tx = session.beginTransaction();
/*      */     try {
/* 1237 */       ImplUtil.exSQLs(tx, session, sql, "##");
/* 1238 */       tx.commit();
/*      */     } catch (Exception e) {
/* 1240 */       result.setResult(1);
/* 1241 */       result.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 1242 */       log.error(result.getMsg());
/* 1243 */       ImplUtil.rollBack(tx);
/*      */     } finally {
/* 1245 */       HibernateUtil.closeSession();
/*      */     }
/* 1247 */     return result;
/*      */   }
/*      */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\SysServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */