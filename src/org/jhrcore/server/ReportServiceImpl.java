/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jhrcore.server;

import com.fr.base.core.BaseCoreUtils;
import com.fr.view.core.DateUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jhrcore.entity.report.ReportGroup;
import org.jhrcore.entity.salary.ValidateSQLResult;
import org.jhrcore.iservice.ReportService;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jboss.annotation.ejb.Clustered;
import org.jhrcore.util.DbUtil;
import org.jhrcore.util.SysUtil;
import org.jhrcore.client.UserContext;
import org.jhrcore.entity.DeptCode;
import org.jhrcore.util.UtilTool;
import org.jhrcore.entity.report.ReportDef;
import org.jhrcore.entity.report.ReportDept;
import org.jhrcore.entity.report.ReportLog;
import org.jhrcore.entity.report.ReportModule;
import org.jhrcore.entity.report.ReportNo;
import org.jhrcore.entity.report.ReportRef;
import org.jhrcore.entity.report.ReportXlsCondition;
import org.jhrcore.entity.report.ReportXlsDetail;
import org.jhrcore.entity.report.ReportXlsScheme;
import org.jhrcore.server.jdbc.JdbcUtil;
import org.jhrcore.server.util.ImplUtil;
import org.jhrcore.util.ObjectToXMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Administrator
 */
@Clustered
@Stateless
@Remote(ReportService.class)
public class ReportServiceImpl extends SuperImpl implements ReportService {

    private static Logger log = Logger.getLogger(ReportServiceImpl.class.getSimpleName());
    private static String report_dir;

    public ReportServiceImpl() throws RemoteException {
        super(ServerApp.rmiPort);
        report_dir = System.getProperty("user.dir") + "/" + "report";
    }

    public ReportServiceImpl(int port) throws RemoteException {
        super(port);
        report_dir = System.getProperty("user.dir") + "/" + "report";
        initReportModel();
    }

    public void upLoadReport(String report_name, byte[] buffer) throws RemoteException {
        if (report_dir == null || report_dir.equals("")) {
            return;
        }
        try {
            File file = new File(report_dir + "/" + report_name);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            BufferedOutputStream output = new BufferedOutputStream(
                    new FileOutputStream(file));
            output.write(buffer);
            output.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public ValidateSQLResult delReport(ReportDef reportDef) throws RemoteException {
        ValidateSQLResult vs = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            String key = reportDef.getReportDef_key();
            String sql = "delete from RoleReport where reportDef_key='" + key + "';";
            sql += "delete from ReportDef where reportDef_key='" + key + "'";
            ImplUtil.exSQLs(tx, session, sql, ";");
            File file = new File(report_dir + "/" + reportDef.getReport_name() + ".cpt");
            if (file.exists()) {
                file.delete();
            }
            file = new File(report_dir + "/" + reportDef.getReport_name() + ".xml");
            if (file.exists()) {
                file.delete();
            }
            tx.commit();
        } catch (Exception ex) {
            ImplUtil.rollBack(tx);
            vs.setResult(1);
            vs.setMsg(ImplUtil.getSQLExceptionMsg(ex));
            log.error(vs.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return vs;
    }

    @Override
    public byte[] getReportForDocument(Document doc, String rmiIp) throws RemoteException {
        try {
            NodeList nl = doc.getElementsByTagName("JDBCDatabaseAttr");
            Node oldnode = nl.item(0);
            Node node = oldnode.getAttributes().getNamedItem("url");
            node.setNodeValue(getReportURL(rmiIp));
            node = oldnode.getAttributes().getNamedItem("driver");
            node.setNodeValue(HibernateUtil.getSQL_driver());
            node = oldnode.getAttributes().getNamedItem("user");
            node.setNodeValue(HibernateUtil.getSQL_user());
            node = oldnode.getAttributes().getNamedItem("password");
            node.setNodeValue(com.fr.report.io.xml.ReportXMLUtils.passwordString(HibernateUtil.getSQL_pass()));
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            t.transform(new DOMSource(doc), new StreamResult(bos));
            return bos.toByteArray();
        } catch (TransformerException ex) {
            log.error(ex);
        }
        return null;
    }

    @Override
    public byte[] getReport_cpt(String report_def_key) throws RemoteException {
        ReportDef reportDef = (ReportDef) HibernateUtil.fetchEntityBy("from ReportDef where reportDef_key='" + report_def_key + "'");
        if (reportDef == null) {
            return null;
        }
        if (reportDef.getReport_cpt_blob() != null) {
            try {
                InputStream is = reportDef.getReport_cpt_blob().getBinaryStream();
                byte buffer[] = new byte[(int) reportDef.getReport_cpt_blob().length()];
                BufferedInputStream input = new BufferedInputStream(is);
                input.read(buffer, 0, buffer.length);
                input.close();
                return (buffer);
            } catch (IOException ex) {
                log.error(ex);
            } catch (SQLException ex) {
                log.error(ex);
            }
            return null;
        }
        try {
            File file = new File(report_dir + "/" + reportDef.getReport_name() + ".cpt");
            if (file == null) {
                log.error("file not exists:" + report_dir + "/" + reportDef.getReport_name() + ".cpt");
                return null;
            }

            FileInputStream fis = new FileInputStream(file);
            reportDef.setReport_cpt_blob(Hibernate.createBlob(fis));
            HibernateUtil.update(reportDef);
            fis.close();
            fis = new FileInputStream(file);
            byte buffer[] = new byte[(int) file.length()];
            BufferedInputStream input = new BufferedInputStream(fis);
            input.read(buffer, 0, buffer.length);
            input.close();
            fis.close();
            return (buffer);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    @Override
    public byte[] getReport_datasource(String report_def_key, String rmiIp) throws RemoteException {
        ReportDef reportDef = (ReportDef) HibernateUtil.fetchEntityBy("from ReportDef where reportDef_key='" + report_def_key + "'");
        if (reportDef == null) {
            return null;
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = null;
            if (reportDef.getDatasource_blob() != null) {
                doc = db.parse(reportDef.getDatasource_blob().getBinaryStream());
            } else {
                File file = new File(System.getProperty("user.dir") + "/report/" + reportDef.getReport_name() + ".xml");
                if (!file.exists()) {
                    log.error("ReportService.getReport_datasource:" + file.getPath() + " not exists");
                    return null;
                }
                doc = db.parse(file);
                FileInputStream fis = new FileInputStream(file);
                reportDef.setDatasource_blob(Hibernate.createBlob(fis));
                HibernateUtil.update(reportDef);
                fis.close();
            }
            NodeList nl = doc.getElementsByTagName("JDBCDatabaseAttr");
            Node oldnode = nl.item(0);
            Node node = oldnode.getAttributes().getNamedItem("url");
//            node.setNodeValue("jdbc:vjdbc:servlet:http://" + rmiIp + ":" + ServerApp.webPort + "/webhr/VJdbcServlet,testdb");//node.setNodeValue(getReportURL(rmiIp));
            node.setNodeValue("jdbc:vjdbc:rmi://" + rmiIp + ":" + (rmiIp.equals(ServerApp.rmiIp) ? ServerApp.rmiOutPort : ServerApp.rmiInPort) + "/VJdbc,testdb");//node.setNodeValue(getReportURL(rmiIp));
            //node.setNodeValue(HibernateUtil.getSQL_url());
            node = oldnode.getAttributes().getNamedItem("driver");
            node.setNodeValue("de.simplicit.vjdbc.VirtualDriver");//node.setNodeValue(HibernateUtil.getSQL_driver());
            node = oldnode.getAttributes().getNamedItem("user");
            node.setNodeValue(HibernateUtil.getSQL_user());
            node = oldnode.getAttributes().getNamedItem("password");
            node.setNodeValue(com.fr.report.io.xml.ReportXMLUtils.passwordString(HibernateUtil.getSQL_pass()));
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            t.transform(new DOMSource(doc), new StreamResult(bos));
            //String xmlStr = bos.toString();
            return bos.toByteArray();//xmlStr.getBytes();
        } catch (TransformerException ex) {
            log.error(ex);
        } catch (SAXException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
        } catch (ParserConfigurationException ex) {
            log.error(ex);
        } catch (SQLException ex) {
            log.error(ex);
        }
        return null;
    }

    @Override
    public byte[] getBase_datasource(String rmiIp) throws RemoteException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(System.getProperty("user.dir") + "/report/" + "base_datasource.xml"));
            NodeList nl = doc.getElementsByTagName("JDBCDatabaseAttr");
            Node oldnode = nl.item(0);
            Node node = oldnode.getAttributes().getNamedItem("url");
//            node.setNodeValue("jdbc:vjdbc:servlet:http://" + rmiIp + ":" + ServerApp.webPort + "/webhr/VJdbcServlet,testdb");//node.setNodeValue(getReportURL(rmiIp));
            node.setNodeValue("jdbc:vjdbc:rmi://" + rmiIp + ":" + (rmiIp.equals(ServerApp.rmiIp) ? ServerApp.rmiOutPort : ServerApp.rmiInPort) + "/VJdbc,testdb");//node.setNodeValue(getReportURL(rmiIp));
            //node.setNodeValue(HibernateUtil.getSQL_url());
            node = oldnode.getAttributes().getNamedItem("driver");
            node.setNodeValue("de.simplicit.vjdbc.VirtualDriver");//node.setNodeValue(HibernateUtil.getSQL_driver());
            node = oldnode.getAttributes().getNamedItem("user");
            node.setNodeValue(HibernateUtil.getSQL_user());
            node = oldnode.getAttributes().getNamedItem("password");
            node.setNodeValue(com.fr.report.io.xml.ReportXMLUtils.passwordString(HibernateUtil.getSQL_pass()));
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            t.transform(new DOMSource(doc), new StreamResult(bos));
            //String xmlStr = bos.toString();
            return bos.toByteArray();//xmlStr.getBytes();
        } catch (TransformerException ex) {
            log.error(ex);
        } catch (SAXException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
        } catch (ParserConfigurationException ex) {
            log.error(ex);
        }
        return null;
    }

    @Override
    public ValidateSQLResult saveReport(ReportDef reportDef, byte[] db_source, byte[] cpt, boolean isNew, String roleKey) throws RemoteException {
        ValidateSQLResult vs = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(db_source);
            reportDef.setDatasource_blob(Hibernate.createBlob(input));
            input = new ByteArrayInputStream(cpt);
            reportDef.setReport_cpt_blob(Hibernate.createBlob(input));
            session.saveOrUpdate(reportDef);
            session.flush();
            if (isNew && roleKey != null && !roleKey.equals("-1")) {
                String key = reportDef.getReportDef_key();
                String sql = "insert into RoleReport(role_report_key,reportDef_key,role_key,fun_flag)select role_key"
                        + DbUtil.getPlusStr(HibernateUtil.getDb_type()) + "'_" + key + "','" + key + "',role_key,1 from Role r where role_key in('" + roleKey.replace(";", "','") + "')"
                        + " and not exists(select 1 from rolereport rf where rf.reportDef_key='" + key + "' and rf.role_key=r.role_key)";
                session.createSQLQuery(sql).executeUpdate();
            }
            tx.commit();
        } catch (IOException ex) {
            ImplUtil.rollBack(tx);
            vs.setResult(1);
            vs.setMsg(ImplUtil.getSQLExceptionMsg(ex));
            log.error(vs.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return vs;
    }

    @Override
    public ValidateSQLResult serRole_key(ReportDef reportDef, String roleKey) throws RemoteException {
        ValidateSQLResult vs = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            String str = "select role_code from Role where role_key ='" + roleKey + "'";
            List<String> lists = (List<String>) (session.createQuery(str).list());
            StringBuffer bf = new StringBuffer();
            for (Object obj : lists) {
                bf.append(" role_code like '").append(obj).append("%' ");
                if ("oracle".equalsIgnoreCase(UserContext.getSql_dialect())) {
                    bf.append(" or instr( '").append(obj).append("',role_code)=1");
                } else {
                    bf.append(" or charindex(role_code,'").append(obj).append("')=1");
                }
            }
            String sql = "select role_key from Role where (" + bf.toString() + ") order by role_code ";
            List<String> listH = (List<String>) (session.createQuery(sql).list());
            String sqllist = "";
            for (Object obj : listH) {
                String a = ((String) obj) + "_" + reportDef.getReportDef_key();
                String b = (String) obj;

                String c = reportDef.getReportDef_key();
                sqllist += "insert into RoleReport VALUES ('" + a + " ','2','" + b + "','" + c + "');";

            }
            sqllist += "delete from RoleReport where reportdef_key ='" + reportDef.getReportDef_key() + "' and role_key='" + roleKey + "' and fun_flag='2'";
            ImplUtil.exSQLs(tx, session, sqllist, ";");
            tx.commit();
        } catch (Exception ex) {
            ImplUtil.rollBack(tx);
            vs.setResult(1);
            vs.setMsg(ImplUtil.getSQLExceptionMsg(ex));
            log.error(vs.getMsg());
        } finally {
            HibernateUtil.closeSession();
        }
        return vs;
    }

    private String getReportURL(String rmiIp) {
        String url = HibernateUtil.getSQL_url();
        if (rmiIp != null && !rmiIp.equals("127.0.0.1") && !rmiIp.equals("localhost")) {
            Properties property = ServerApp.getSys_properties();
            if (property != null) {
                if (property.getProperty("dbIp") != null && !property.getProperty("dbIp").trim().equals("")
                        && property.getProperty("rmiIp") != null && rmiIp.equals(property.getProperty("rmiIp").trim())) {
                    url = url.replace(property.getProperty("ip"), property.getProperty("dbIp"));
                }
            }
        }
        return url;
    }

    private void initReportModel() {
        File file = new File(report_dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(report_dir + "/base_datasource.xml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                org.dom4j.Document document = org.dom4j.DocumentHelper.createDocument();
                org.dom4j.Element configuration = document.addElement("DatasourceManager");
                org.dom4j.Element ConnectionMap = configuration.addElement("ConnectionMap");
                org.dom4j.Element Connection = ConnectionMap.addElement("Connection");
                Connection.addAttribute("name", "HR");
                Connection.addAttribute("class", "com.fr.data.impl.JDBCDatabaseConnection");
                Connection.addElement("DatabaseAttr");
                org.dom4j.Element JDBCDatabaseAttr = Connection.addElement("JDBCDatabaseAttr");
                JDBCDatabaseAttr.addAttribute("url", "jdbc:sqlserver://127.0.0.1:1433;databaseName=jhr");
                JDBCDatabaseAttr.addAttribute("driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
                JDBCDatabaseAttr.addAttribute("user", "sa");
                JDBCDatabaseAttr.addAttribute("password", "___0060002f003b003d0057");
                JDBCDatabaseAttr.addAttribute("encryptPassword", "true");
                configuration.addElement("DictMap");
                configuration.addElement("Relations");
                org.dom4j.io.XMLWriter writer = null;
                //设置输出格式
                org.dom4j.io.OutputFormat format = new org.dom4j.io.OutputFormat();
                format.setEncoding("utf-8");
                format.setIndent(true);
                format.setLineSeparator("\n");
                format.setNewlines(true);
                writer = new org.dom4j.io.XMLWriter(format);
                writer.setOutputStream(new FileOutputStream(file.getAbsolutePath()));
                writer.write(document);
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    @Override
    public ValidateSQLResult addReportToModule(String module_flag, List<String> reportKeys) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
            for (String key : reportKeys) {
                ReportModule rm = new ReportModule();
                rm.setModule_flag(module_flag);
                rm.setReportDef_key(key);
                rm.setCode("comm");
                rm.setReportModule_key("comm_" + module_flag + "_" + key);
                session.saveOrUpdate(rm);
            }
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult setReportNoUser(List<String> noKeys, ReportLog modLog) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        String db_type = HibernateUtil.getDb_type();
        String uid = DbUtil.getUIDForDb(db_type);
        String now = DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd HH:mm:ss", db_type);
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            SysUtil.sortStrList(noKeys);
            String logStr = "insert into ReportLog(ReportLog_key,ym,cno,a0190,a0101,cdate,ctype,ctext,deptCode_key,reportGroup_key)select "
                    + uid + ",ym,cno,'" + modLog.getA0190() + "','" + modLog.getA0101() + "'," + now + ",'设置办事员用户','',deptCode_key,reportGroup_key from ReportNo rn where reportNo_Key in ";
            String sql = DbUtil.getQueryForMID(logStr, noKeys);
            sql += DbUtil.getQueryForMID("update ReportNo set buserNo='" + modLog.getA0190() + "',buserName='" + modLog.getA0101() + "' where reportNo_key in", noKeys);
            ImplUtil.exSQLs(tx, s, sql, ";");
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult copyRule(List<String> ruleKeys, List<String> reportKeys) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        String uid = DbUtil.getUIDForDb();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            for (String reportKey : reportKeys) {
                Object order = s.createSQLQuery("select max(order_no) from ReportRegula rr where rr.reportDef_key='" + reportKey + "'").uniqueResult();
                int order_no = 0;
                if (order != null) {
                    order_no = SysUtil.objToInt(order);
                }
                order_no++;
                for (String ruleKey : ruleKeys) {
                    String sql = "insert into ReportRegula(ReportRegula_key,reportDef_key,r_name,used,r_text,order_no)select " + uid
                            + ",'" + reportKey + "',r_name,used,r_text," + order_no + " from ReportRegula rr where rr.reportRegula_key='" + ruleKey + "'";
                    s.createSQLQuery(sql).executeUpdate();
                    order_no++;
                }
            }
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult addReportsToGroup(ReportGroup rgroup, List<String> keys) throws RemoteException {
        ValidateSQLResult vsr = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            String sql = "delete from ReportRef where reportGroup_key ='" + rgroup.getReportGroup_key() + "'";
            s.createSQLQuery(sql).executeUpdate();
            s.flush();
            int order_no = 1;
            for (String key : keys) {
                ReportRef rrf = (ReportRef) UtilTool.createUIDEntity(ReportRef.class);
                rrf.setOrder_no(order_no++);
                rrf.setReportGroup_key(rgroup.getReportGroup_key());
                rrf.setR_type(rgroup.getR_type());
                rrf.setReportDef_key(key);
                s.save(rrf);
            }
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            vsr.setResult(1);
            vsr.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return vsr;
    }

    @Override
    public ValidateSQLResult addDeptsToGroup(ReportGroup rgroup, List<String> deptKeys) throws RemoteException {
        ValidateSQLResult vsr = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            List<String> objs = s.createSQLQuery("select deptCode_key from ReportDept where reportGroup_key ='" + rgroup.getReportGroup_key() + "'").list();
            List<String> delKeys = new ArrayList();
            List<String> exists = new ArrayList();
            for (String obj : objs) {
                if (deptKeys.contains(obj)) {
                    exists.add(obj);
                } else {
                    delKeys.add(obj);
                }
            }
            deptKeys.removeAll(exists);
            deptKeys.removeAll(delKeys);
            if (!delKeys.isEmpty()) {
                String sql = "delete from ReportDept where reportGroup_key ='" + rgroup.getReportGroup_key() + "' and deptCode_key in";
                sql = DbUtil.getQueryForMID(sql, delKeys);
                sql = sql.substring(0, sql.length() - 1);
                s.createSQLQuery(sql).executeUpdate();
                s.flush();
            }
            for (String key : deptKeys) {
                ReportDept rdept = (ReportDept) UtilTool.createUIDEntity(ReportDept.class);
                rdept.setReportGroup_key(rgroup.getReportGroup_key());
                rdept.setDeptCode_key(key);
                s.save(rdept);
            }
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            vsr.setResult(1);
            vsr.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return vsr;
    }

    @Override
    public ValidateSQLResult createReportNo(DeptCode dc, ReportGroup rgroup, String ym, List<ReportDef> reports) throws RemoteException {
        ValidateSQLResult validateSQLResult = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            ReportDept reportDept = (ReportDept) s.createQuery("select rdept from ReportDept rdept where rdept.reportGroup_key='" + rgroup.getReportGroup_key() + "' and rdept.deptCode_key='" + dc.getDeptCode_key() + "'").uniqueResult();
            if (reportDept == null) {
                validateSQLResult.setResult(1);
                validateSQLResult.setMsg("部门不存在");
                return validateSQLResult;
            }
            List existsReport = s.createQuery("select reportDef_key from ReportNo where reportGroup_key='" + rgroup.getReportGroup_key() + "' and deptCode_key='" + dc.getDeptCode_key() + "' and ym='" + ym + "'").list();
            Date date = new Date();
            Hashtable<String, String> paras = new Hashtable<String, String>();
            for (ReportDef rdf : reports) {
                if (existsReport.contains(rdf.getReportDef_key())) {
                    continue;
                }
                ReportNo rn = (ReportNo) UtilTool.createUIDEntity(ReportNo.class);
                rn.setCdate(date);
                rn.setNoState("未生成");
                rn.setCnum(0);
                paras.put("@报表编码", "'" + rdf.getReport_class() + "'");
                String no = HibernateUtil.fetchNewNoBy(s, "ReportNo", 1, paras);
                rn.setCno(no);
                rn.setR_type(rgroup.getR_type());
                rn.setYm(ym);
                rn.setNeedCheck(reportDept.isNeedCheck());
                rn.setDept_code2(dc.getDept_code());
                rn.setDeptCode_key(dc.getDeptCode_key());
                rn.setReportDef_key(rdf.getReportDef_key());
                rn.setReportGroup_key(rgroup.getReportGroup_key());
                s.save(rn);
                s.flush();
                ReportLog rl = (ReportLog) UtilTool.createUIDEntity(ReportLog.class);
                rl.setYm(ym);
                rl.setA0101(reportDept.getTuserName());
                rl.setA0190(reportDept.getTuserNo());
                rl.setCdate(date);
                rl.setCtype("生成单号");
                rl.setCno(no);
                rl.setDeptCode_key(dc.getDeptCode_key());
                rl.setReportGroup_key(rgroup.getReportGroup_key());
                rl.setReportDef_key(rdf.getReportDef_key());
                s.save(rl);
                s.flush();
            }
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            validateSQLResult.setResult(1);
            validateSQLResult.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return validateSQLResult;
    }

    @Override
    public ValidateSQLResult delReportNo(List<String> noKeys, ReportLog modLog) throws RemoteException {
        ValidateSQLResult vsr = new ValidateSQLResult();
        String db_type = HibernateUtil.getDb_type();
        String uid = DbUtil.getUIDForDb(db_type);
        String now = DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd HH:mm:ss", db_type);
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            SysUtil.sortStrList(noKeys);
            String logStr = "insert into ReportLog(ReportLog_key,ym,cno,a0190,a0101,cdate,ctype,ctext,deptCode_key,reportGroup_key,reportDef_key)select "
                    + uid + ",ym,cno,'" + modLog.getA0190() + "','" + modLog.getA0101() + "'," + now + ",'删除单号','',deptCode_key,reportGroup_key,reportDef_key from ReportNo rn where reportNo_Key in ";
            String sql = DbUtil.getQueryForMID(logStr, noKeys) + ";";
            sql += DbUtil.getQueryForMID("delete from ReportNo where reportNo_key in", noKeys);
            ImplUtil.exSQLs(tx, s, sql, ";");
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            vsr.setResult(1);
            vsr.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return vsr;
    }

    @Override
    public ValidateSQLResult createReportData(List<String> noKeys, ReportLog modLog) throws RemoteException {
        ValidateSQLResult vsr = new ValidateSQLResult();
        String db_type = HibernateUtil.getDb_type();
        String uid = DbUtil.getUIDForDb(db_type);
        String now = DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd HH:mm:ss", db_type);
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            SysUtil.sortStrList(noKeys);
            String logStr = "insert into ReportLog(ReportLog_key,ym,cno,a0190,a0101,cdate,ctype,ctext,deptCode_key,reportGroup_key,reportDef_key)select "
                    + uid + ",ym,cno,'" + modLog.getA0190() + "','" + modLog.getA0101() + "'," + now + ",'生成数据','',deptCode_key,reportGroup_key,reportDef_key from ReportNo rn where reportNo_Key in ";
            String sql = DbUtil.getQueryForMID(logStr, noKeys) + ";";
            sql += DbUtil.getQueryForMID("update ReportNo set cnum=cnum+1,noState='未提交' where reportNo_key in", noKeys);
            ImplUtil.exSQLs(tx, s, sql, ";");
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            vsr.setResult(1);
            vsr.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return vsr;
    }

    @Override
    public ValidateSQLResult updateReportNoState(List<String> deptKeys, ReportLog modLog, String old_type, String new_type, String group_key, String ym) throws RemoteException {
        ValidateSQLResult vsr = new ValidateSQLResult();
        String db_type = HibernateUtil.getDb_type();
        String uid = DbUtil.getUIDForDb(db_type);
        String now = DateUtil.toStringForQuery(new Date(), "yyyy-MM-dd HH:mm:ss", db_type);
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            SysUtil.sortStrList(deptKeys);
            String logStr = "insert into ReportLog(ReportLog_key,ym,cno,a0190,a0101,cdate,ctype,ctext,deptCode_key,reportGroup_key,reportDef_key)select "
                    + uid + ",'" + ym + "',cno,'" + modLog.getA0190() + "','" + modLog.getA0101() + "'," + now + ",'" + modLog.getCtype() + "','" + modLog.getCtext().replace("'", "''") + "',deptCode_key,'" + group_key + "',reportDef_key from ReportNo rn where reportGroup_key='" + group_key + "' and ym='" + ym + "' and noState='" + old_type + "' and deptCode_Key in ";
            String sql = DbUtil.getQueryForMID(logStr, deptKeys);
            sql += DbUtil.getQueryForMID("update ReportNo set noState='" + new_type + "' where reportGroup_key='" + group_key + "' and ym='" + ym + "' and noState='" + old_type + "' and deptCode_Key in", deptKeys);
            ImplUtil.exSQLs(tx, s, sql, ";");
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            vsr.setResult(1);
            vsr.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return vsr;
    }

    @Override
    public byte[] getReportSetting(String id) throws RemoteException {
        byte[] b = null;
        try {
            File f = new File(report_dir + "/comm.report_set");
            if (!f.exists()) {
                return null;
            }
            List list = ObjectToXMLUtil.objectXmlDecoder(report_dir + "/comm.report_set");
            return ObjectToXMLUtil.objectXmlEncoder(list);
        } catch (Exception e) {
            log.error(e);
        }
        return b;
    }

    @Override
    public ValidateSQLResult saveReportSetting(String id, byte[] source) throws RemoteException {
        ValidateSQLResult result = new ValidateSQLResult();
        if (source == null) {
            return result;
        }
        try {
            Object obj = ObjectToXMLUtil.objectXmlDecoder(source);
            if (obj == null) {
                return result;
            }
            ObjectToXMLUtil.objectXmlEncoder(obj, report_dir + "/comm.report_set");
        } catch (Exception e) {
            result.setResult(1);
            result.setMsg(e.getMessage());
        }
        return result;
    }

    @Override
    public int getTabColumn(String mc) throws RemoteException {
        Connection conn = JdbcUtil.getConnection();
        try {
            mc = mc.toUpperCase();
            String hql = "delete from ReportXlsDetail where reportXlsScheme_key='" + mc + "';";
            hql += "delete from ReportXlsScheme where reportXlsScheme_key='" + mc + "';";
            HibernateUtil.excuteSQLs(hql, ";");
            Statement stmt = conn.createStatement();
            String sql = "select   *   from   " + mc + "  where rownum =1";  //只取一条记录
            if (HibernateUtil.getDb_type().equals("sqlserver")) {
                sql = "select top 1 *   from   " + mc;  //只取一条记录
            }
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rsmd.getColumnCount() == 0) {
                return 0;
            }
            ReportXlsScheme defaultExportScheme = new ReportXlsScheme();
            defaultExportScheme.setReportXlsScheme_key(mc);
            defaultExportScheme.setScheme_name("");
            defaultExportScheme.setEntity_name(mc);
            defaultExportScheme.setScheme_title("");
            defaultExportScheme.setScheme_type("0");  //0 是默认的表方案 名称为空，1是自建的表方案
            HibernateUtil.save(defaultExportScheme);
            //系统允许的字段类型
            List<String> sysTypes = Arrays.asList(new String[]{"CHAR", "VARCHAR2", "VARCHAR", "NUMBER", "INT", "DATE", "DATETIME", "TIMESTAMP"});
            for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
                String type = rsmd.getColumnTypeName(i).toUpperCase();
                if (sysTypes.contains(type)) {
                    ReportXlsDetail fill = new ReportXlsDetail();
                    fill.setReportXlsDetail_key(mc + "_" + rsmd.getColumnName(i));
                    fill.setCol(rsmd.getColumnName(i));
                    fill.setCol_type(type);
                    fill.setCol_len(rsmd.getPrecision(i));//i列类型的精确度(类型的长度):
                    fill.setCol_scale(rsmd.getScale(i));//i列小数点后的位数
                    fill.setReportXlsScheme(defaultExportScheme);
                    HibernateUtil.save(fill);
                }
            }
            return 1;
        } catch (Exception e) {
            log.error("表" + mc + "不存在！");
            return 2;
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                log.error(ex);
            }
        }

    }

    @Override
    public ValidateSQLResult saveXlsScheme(ReportXlsScheme scheme, boolean isNew) throws RemoteException {
        ValidateSQLResult vsr = new ValidateSQLResult();
        Session s = HibernateUtil.currentSession();
        Transaction tx = s.beginTransaction();
        try {
            String schemeKey = scheme.getReportXlsScheme_key();
            if (isNew) {
                s.save(scheme);
            } else {
                String sql = "delete from ReportXlsCondition where ReportXlsScheme_key='" + schemeKey + "';";
                sql += "delete from ReportXlsDetail where ReportXlsScheme_key='" + schemeKey + "'";
                ImplUtil.exSQLs(tx, s, sql, ";");
                s.update(scheme);
            }
            s.flush();
            for (ReportXlsDetail fsd : scheme.getReportXlsDetails()) {
                s.save(fsd);
            }
            s.flush();
            for (ReportXlsCondition rc : scheme.getReportXlsConditions()) {
                s.save(rc);
            }
            s.flush();
            String sql = "update reportxlsdetail rd set col_name=(select col_name from reportxlsdetail r where reportxlsscheme_key='"
                    + schemeKey + "' and r.col=rd.col) where rd.reportxlsscheme_key='" + scheme.getEntity_name().toUpperCase() + "'";
            if (HibernateUtil.getDb_type().equals("sqlserver")) {
                sql = "update rd set col_name=r.col_name from reportxlsdetail r,reportxlsdetail rd where r.reportxlsscheme_key='"
                        + schemeKey + "' and r.col=rd.col and rd.reportxlsscheme_key='" + scheme.getEntity_name().toUpperCase() + "'";
            }
            s.createSQLQuery(sql).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e);
            vsr.setResult(1);
            vsr.setMsg(ImplUtil.getSQLExceptionMsg(e));
            ImplUtil.rollBack(tx);
        } finally {
            HibernateUtil.closeSession();
        }
        return vsr;
    }

    @Override
    public boolean isSecuryed() throws RemoteException {
        return false;
    }

    @Override
    public synchronized ValidateSQLResult reportCert(String code) throws RemoteException {
        ValidateSQLResult vs = new ValidateSQLResult();
        try {
            vs.setResult(1);
            if (isSecuryed()) {
                vs.setMsg("报表系统已注册，请重新登录");
                return vs;
            }
            if (!BaseCoreUtils.checkDesingerActive(code)) {
                vs.setMsg("非法的注册码");
                return vs;
            }
           // Secury.writeReportCert(code);
            vs.setResult(0);
        } catch (Exception e) {
            log.error(e);
            vs.setResult(1);
            vs.setMsg(ImplUtil.getSQLExceptionMsg(e));
        }
        return vs;
    }
}
