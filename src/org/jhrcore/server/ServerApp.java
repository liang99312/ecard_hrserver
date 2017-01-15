/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import de.simplicit.vjdbc.rmi.ConnectionBrokerRmi;
/*     */ import de.simplicit.vjdbc.server.rmi.ConnectionBrokerRmiImpl;
/*     */ import java.awt.AWTException;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Image;
/*     */ import java.awt.MenuItem;
/*     */ import java.awt.PopupMenu;
/*     */ import java.awt.SystemTray;
/*     */ import java.awt.TrayIcon;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.WindowAdapter;
/*     */ import java.awt.event.WindowEvent;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.net.InetAddress;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.UnknownHostException;
/*     */ import java.rmi.Naming;
/*     */ import java.rmi.Remote;
/*     */ import java.rmi.RemoteException;
/*     */ import java.rmi.registry.LocateRegistry;
/*     */ import java.rmi.server.RMISocketFactory;
/*     */ import java.util.HashMap;
/*     */ import java.util.Properties;
/*     */ import java.util.TimeZone;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JToolBar;
/*     */ import org.apache.log4j.Level;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.apache.log4j.PatternLayout;
/*     */ import org.apache.log4j.RollingFileAppender;
/*     */ import org.jhrcore.client.AppHrClient;
/*     */ import org.jhrcore.client.UserContext;
/*     */ import org.jhrcore.comm.ConfigManager;
/*     */ import org.jhrcore.server.italk.SMRMISocket;
/*     */ import org.jhrcore.server.timer.HRTimer;
/*     */ import org.jhrcore.server.util.InitSysUtil;
/*     */ import org.jhrcore.server.util.SecuryUtil;
/*     */ import org.jhrcore.ui.ContextManager;
/*     */ import org.jhrcore.util.ImageUtil;
/*     */ import org.jhrcore.util.SysUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ServerApp
/*     */ {
/*  58 */   private static Logger log = Logger.getLogger(ServerApp.class.getSimpleName());
/*     */   private static ConnectionBrokerRmi connectionBrokerRmi;
/*     */   private static ConnectionBrokerRmi connectionBrokerRmi2;
/*     */   private static Properties sys_properties;
/*  62 */   private static String rmiInIp = "";
/*  63 */   public static String rmiIp = "";
/*  64 */   public static String webPort = "8080";
/*  65 */   public static int rmiPort = 1299;
/*  66 */   public static int rmiInPort = 1299;
/*  67 */   public static int rmiOutPort = 1399;
/*  68 */   private static long serverStartTime = 0L;
/*  69 */   public static boolean forHTTP = false;
/*     */   private static JFrame mainFrame;
/*  71 */   private static JToolBar toolBar = new JToolBar();
/*  72 */   private static JButton btnStart = new JButton("启动服务");
/*  73 */   private static JButton btnStop = new JButton("停止服务");
/*  74 */   private static JButton btnRbuild = new JButton("重启服务");
/*  75 */   private static JPanel statusBar = new JPanel(new FlowLayout(0));
/*  76 */   private static JLabel statusText = new JLabel("  ");
/*  77 */   private static boolean isActive = false;
/*  78 */   private static TrayIcon trayIcon = null;
/*     */   
/*     */   public static void main(String[] args) {

/*  92 */     RollingFileAppender tmp = new RollingFileAppender();
/*     */     try {
/*  94 */       tmp.setFile(System.getProperty("user.dir") + "/ServerApp.log", true, true, 1000);
/*     */     }
/*     */     catch (IOException e) {
/*  97 */       e.printStackTrace();
/*     */     }
/*  99 */     tmp.setName("longshine");
/* 100 */     tmp.setMaxFileSize("1000KB");
/* 101 */     tmp.setMaxBackupIndex(20);
/* 102 */     PatternLayout lo = new PatternLayout();
/* 103 */     lo.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %5p %c{1}:%L - %m%n");
/* 104 */     tmp.setLayout(lo);
/* 105 */     Logger.getRootLogger().addAppender(tmp);
/* 106 */     log.info("Loading");
/* 107 */     Logger.getRootLogger().setLevel(Level.ERROR);
/* 108 */     if (SecuryUtil.isVilidate()) {
/* 109 */       startServer();
/* 111 */       new AppHrClient();
/*     */     }
/*     */   }
/*     */   
/*     */   public static void startServer()
/*     */   {
/*     */     try {
/* 118 */       UserContext.sql_dialect = HibernateUtil.getDb_type();
/* 119 */       UserContext.person_code = UserContext.sysManName;
/* 120 */       sys_properties = ConfigManager.getConfigManager().loadProperties("system.properties");
/*     */       
/*     */ 
/*     */ 
/* 124 */       long time1 = System.currentTimeMillis();
/* 125 */       CommServiceImpl.rebuild_model();
/* 126 */       long time2 = System.currentTimeMillis();
/* 127 */       System.out.println("kkkkkkkkk:" + (time2 - time1));
/* 128 */       createTableView();
/* 129 */       log.info("数据服务器启动成功");
/* 130 */       TimeZone tz = TimeZone.getTimeZone("GMT+8:00");
/* 131 */       TimeZone.setDefault(tz);
/* 132 */       InitSysUtil.initSystem();
/*     */       
/* 134 */       log.info("功能权限更新成功");
/* 135 */       System.out.println("功能权限更新成功");
/* 136 */       UserContext.createUserFolder();
/* 137 */       CommServiceImpl.rebuildCache();
/* 138 */       if (sys_properties != null) {
/* 139 */         String inPort = sys_properties.getProperty("rmiInPort");
/* 140 */         String outPort = sys_properties.getProperty("rmiOutPort");
/* 141 */         String rmiFactoryPort = sys_properties.getProperty("rmiFactoryPort");
/* 142 */         rmiInIp = sys_properties.getProperty("rmiInIp");
/* 143 */         rmiIp = sys_properties.getProperty("rmiIp");
/* 144 */         webPort = sys_properties.getProperty("webport");
/* 145 */         SMRMISocket smr = new SMRMISocket((rmiFactoryPort == null) || (rmiFactoryPort.trim().equals("")) ? 1099 : SysUtil.objToInt(rmiFactoryPort));
/* 146 */         RMISocketFactory.setSocketFactory(smr);
/* 147 */         if ((webPort == null) || (webPort.trim().equals(""))) {
/* 148 */           webPort = "8080";
/*     */         }
/* 150 */         if ((rmiInIp == null) || (rmiInIp.trim().length() == 0)) {
/* 151 */           rmiInIp = InetAddress.getLocalHost().getHostAddress();
/*     */         }
/* 153 */         if ((inPort != null) && (!inPort.trim().equals(""))) {
/* 154 */           rmiPort = Integer.valueOf(inPort).intValue();
/*     */         }
/* 156 */         rmiInPort = rmiPort;
/* 157 */         startServer(null, rmiInIp);
/* 158 */         if ((rmiIp != null) && (rmiIp.trim().length() != 0) && (!rmiIp.equals(rmiInIp)) && (!rmiIp.equals("127.0.0.1")) && (!rmiIp.equals("localhost"))) {
/* 159 */           if ((outPort != null) && (!outPort.trim().equals(""))) {
/* 160 */             rmiPort = Integer.valueOf(outPort).intValue();
/*     */           } else {
/* 162 */             rmiPort = 1399;
/*     */           }
/* 164 */           rmiOutPort = rmiPort;
/* 165 */           startServer(rmiIp, rmiInIp);
/*     */         }
/*     */       } else {
/* 168 */         SMRMISocket smr = new SMRMISocket();
/* 169 */         RMISocketFactory.setSocketFactory(smr);
/* 170 */         startServer(null, "localhost");
/*     */       }
/* 172 */       serverStartTime = System.currentTimeMillis();
/*     */       
/* 174 */       HRTimer.getTimer().start();
/* 175 */       System.out.println("服务启动成功！");
/*     */       
/*     */ 
/* 178 */       isActive = true;
/*     */     } catch (Exception e) {
/* 180 */       e.printStackTrace();log.error(e);
/*     */     }
/*     */     finally {}
/*     */   }
/*     */   
/*     */   public static void startServer(String ip, String inIp)
/*     */     throws RemoteException, MalformedURLException, UnknownHostException
/*     */   {
/* 195 */     if ((inIp == null) || (inIp.trim().length() == 0)) {
/* 196 */       return;
/*     */     }
/*     */     try {
/* 199 */       boolean isNetIp = (ip != null) && (ip.trim().length() != 0);
/* 200 */       if (isNetIp) {
/* 201 */         rmiIp = ip;
/* 202 */         System.setProperty("java.rmi.server.hostname", ip);
/*     */       } else {
/* 204 */         rmiInIp = inIp;
/* 205 */         System.setProperty("java.rmi.server.hostname", inIp);
/*     */       }
/* 207 */       LocateRegistry.createRegistry(rmiPort);
/* 208 */       String bindStart = "//" + inIp + ":" + rmiPort;
/* 209 */       if (isNetIp) {
/* 210 */         connectionBrokerRmi = new ConnectionBrokerRmiImpl(rmiPort);
/* 211 */         Naming.rebind(bindStart + "/VJdbc", connectionBrokerRmi);
/* 212 */         if (!forHTTP) {}
/*     */       }
/*     */       else
/*     */       {
/* 216 */         connectionBrokerRmi2 = new ConnectionBrokerRmiImpl(rmiPort);
/* 217 */         Naming.rebind(bindStart + "/VJdbc", connectionBrokerRmi2);
/* 218 */         if (forHTTP) {
/* 219 */           return;
/*     */         }
/*     */       }
/* 222 */       bindService(bindStart);
/*     */     } catch (Exception ex) {
/* 224 */       System.out.println("服务器端口绑定时发生错误:" + ex.getMessage());
/* 225 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */   
/*     */   private static void bindService(String bindStart)
/*     */   {
/* 231 */     for (String serviceTag : InitSysUtil.getServices().keySet()) {
/* 232 */       Class c = (Class)InitSysUtil.getServices().get(serviceTag);
/*     */       try {
/* 234 */         Remote service1 = (Remote)c.newInstance();
/* 235 */         log.error("serviceTag:" + serviceTag + ";" + bindStart);
/* 236 */         Naming.bind(bindStart + "/" + serviceTag, service1);
/*     */       } catch (Exception ex) {
/* 238 */         ex.printStackTrace();
/* 239 */         log.error(ex);
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   private static void createTray() {
/* 245 */     if (SystemTray.isSupported()) {
/* 246 */       SystemTray tray = SystemTray.getSystemTray();
/* 247 */       Image image = ImageUtil.getImage("moduleInfoIcon.png");
/* 248 */       ActionListener listener = new ActionListener()
/*     */       {
/*     */ 
/*     */ 
/*     */         public void actionPerformed(ActionEvent e) {}
/*     */ 
/*     */ 
/* 255 */       };
/* 256 */       PopupMenu popup = new PopupMenu();
/* 257 */       MenuItem defaultItem = new MenuItem("显示");
/* 258 */       defaultItem.addActionListener(listener);
/* 259 */       MenuItem exitItem = new MenuItem("退出");
/* 260 */       exitItem.addActionListener(new ActionListener()
/*     */       {
/*     */ 
/*     */         public void actionPerformed(ActionEvent e) {}
/*     */ 
/*     */ 
/* 266 */       });
/* 267 */       popup.add(defaultItem);
/* 268 */       popup.add(exitItem);
/* 269 */       trayIcon = new TrayIcon(image, "WEBHR服务器", popup);
/* 270 */       trayIcon.addActionListener(listener);
/*     */       try {
/* 272 */         tray.add(trayIcon);
/*     */       } catch (AWTException e1) {
/* 274 */         e1.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   private static void exit()
/*     */   {
/* 281 */     if (JOptionPane.showConfirmDialog(mainFrame, "确定要停止WEBHR服务吗?", "提示", 2, 3) != 0)
/*     */     {
/*     */ 
/* 284 */       mainFrame.setVisible(false);
/* 285 */       return;
/*     */     }
/*     */     try {
/* 288 */       stopServer();
/*     */     } catch (Exception ex) {
/* 290 */       ex.printStackTrace();
/* 291 */       log.error(ex);
/*     */     }
/* 293 */     removeTray();
/* 294 */     System.exit(0);
/*     */   }
/*     */   
/*     */   private static void removeTray() {
/* 298 */     if (SystemTray.isSupported()) {
/* 299 */       SystemTray tray = SystemTray.getSystemTray();
/* 300 */       if (trayIcon != null) {
/* 301 */         tray.remove(trayIcon);
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   private static void createTableView() {
/* 307 */     System.out.println("开始更新表权限");
/* 308 */     log.info("开始更新表权限");
/* 309 */     long time1 = System.currentTimeMillis();
/*     */     
/* 311 */     Object viewUpdate = HibernateUtil.fetchEntityBy("select sysparameter_value from SysParameter where sysParameter_key='HRViewUpdate'");
/* 312 */     RightServiceImpl.createTableView("1=1", !"1".equals(viewUpdate));
/* 313 */     HibernateUtil.excuteSQL("update SysParameter set sysparameter_value='0' where sysParameter_key='HRViewUpdate'");
/* 314 */     System.out.println("表权限更新完成");
/* 315 */     log.info("表权限更新完成");
/* 316 */     long time2 = System.currentTimeMillis();
/* 317 */     System.out.println("t:" + (time2 - time1));
/*     */   }
/*     */   
/*     */   public static void save() {
/* 321 */     File file = new File(System.getProperty("user.dir") + "/system" + ".properties");
/*     */     try
/*     */     {
/* 324 */       sys_properties.store(new FileOutputStream(file), "");
/*     */     } catch (IOException e) {
/* 326 */       log.log(log.getPriority(), "Can not save properties file");
/*     */     }
/*     */   }
/*     */   
/*     */   public static void stopServer() throws Exception {
/* 331 */     if (!isActive) {
/* 332 */       return;
/*     */     }
/* 334 */     if ((rmiIp != null) && (!rmiIp.trim().equals(""))) {
/* 335 */       stopServer(rmiIp, 1399);
/*     */     }
/* 337 */     if ((rmiInIp != null) && (!rmiInIp.trim().equals(""))) {
/* 338 */       stopServer(rmiInIp, 1299);
/*     */     }
/* 340 */     isActive = false;
/*     */   }
/*     */   
/*     */   private static void stopServer(String ip, int port) throws Exception {
/* 344 */     for (String key : InitSysUtil.getServices().keySet()) {
/* 345 */       if (!key.equals("UpdateService"))
/*     */       {
/*     */         try
/*     */         {
/* 349 */           Naming.unbind("//" + ip + ":" + port + "/" + key);
/*     */         } catch (Exception ex) {
/* 351 */           log.error("stoperror:" + ex);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   public static long getServerStartTime() {
/* 358 */     return serverStartTime;
/*     */   }
/*     */   
/*     */   public static Properties getSys_properties() {
/* 362 */     if (sys_properties == null) {
/* 363 */       sys_properties = new Properties();
/* 364 */       save();
/*     */     }
/* 366 */     return sys_properties;
/*     */   }
/*     */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\ServerApp.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */