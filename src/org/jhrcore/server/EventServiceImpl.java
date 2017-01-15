/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import java.rmi.RemoteException;
/*     */ import java.sql.Connection;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import javax.ejb.Remote;
/*     */ import javax.ejb.Stateless;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.jboss.annotation.ejb.Clustered;
/*     */ import org.jhrcore.comm.CommThreadPool;
/*     */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*     */ import org.jhrcore.iservice.EventService;
/*     */ import org.jhrcore.server.jdbc.JdbcUtil;
/*     */ import org.jhrcore.server.util.ImplUtil;
/*     */ import org.jhrcore.util.DbUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @Clustered
/*     */ @Stateless
/*     */ @Remote({EventService.class})
/*     */ public class EventServiceImpl
/*     */   extends SuperImpl
/*     */   implements EventService
/*     */ {
/*  34 */   public static int maxThreads = 10;
/*  35 */   private static Hashtable<String, EventObj> activeWorks = new Hashtable();
/*  36 */   private static Hashtable<String, EventObj> finishWorks = new Hashtable();
/*  37 */   private static List<EventObj> allEvents = new ArrayList();
/*  38 */   private static Logger log = Logger.getLogger(EventServiceImpl.class);
/*     */   
/*     */   public EventServiceImpl() throws RemoteException {
/*  41 */     super(ServerApp.rmiPort);
/*     */   }
/*     */   
/*     */   public EventServiceImpl(int port) throws RemoteException {
/*  45 */     super(port);
/*     */   }
/*     */   
/*     */   public static void doSEQWork(String id, String workId, String ex_sql) {
/*  49 */     EventObj event = new EventObj(id, workId, ex_sql);
/*  50 */     allEvents.add(event);
/*  51 */     doSEQWork();
/*     */   }
/*     */   
/*     */   public static synchronized void doSEQWork() {
/*  55 */     if ((activeWorks.isEmpty()) && (allEvents.isEmpty())) {
/*  56 */       return;
/*     */     }
/*  58 */     List<String> removes = new ArrayList();
/*  59 */     for (EventObj event : activeWorks.values()) {
/*  60 */       if (event.isFinished()) {
/*  61 */         finishWorks.put(event.getId(), event);
/*  62 */         removes.add(event.getWorkId());
/*  63 */         allEvents.remove(event);
/*     */       }
/*     */     }
/*  66 */     for (String workId : removes) {
/*  67 */       activeWorks.remove(workId);
/*     */     }
/*  69 */     int currenThreads = activeWorks.size();
/*  70 */     for (int i = 0; i < allEvents.size(); i++) {
/*  71 */       if (currenThreads == maxThreads) {
/*     */         break;
/*     */       }
/*  74 */       EventObj event = (EventObj)allEvents.get(i);
/*  75 */       if (!event.isStarted())
/*     */       {
/*     */ 
/*  78 */         if (!activeWorks.containsKey(event.getWorkId()))
/*     */         {
/*     */ 
/*  81 */           activeWorks.put(event.getWorkId(), event);
/*  82 */           currenThreads++;
/*     */         } } }
/*  84 */     for (final EventObj event : activeWorks.values())
/*  85 */       if (!event.isStarted())
/*     */       {
/*     */ 
/*  88 */         event.setStarted(true);
/*  89 */         event.setStart_date(new Date());
/*  90 */         event.setMsg(null);
/*  91 */         CommThreadPool.getClientThreadPool().handleEvent(new Runnable()
/*     */         {
/*     */           public void run()
/*     */           {
/*     */             try {
/*  96 */               EventServiceImpl.doSQLWork(event);
/*     */             } catch (Exception ex) {
/*  98 */               ex.printStackTrace();
/*     */             }
/*     */           }
/*     */         });
/*     */       }
/*     */   }
/*     */   
/*     */   public static void doSQLWork(EventObj event) {
/* 106 */     Connection c = JdbcUtil.getConnection();
/* 107 */     Statement stmt = null;
/*     */     try {
/* 109 */       c.setAutoCommit(false);
/* 110 */       String[] sqls = event.getExSQL().split(";");
/* 111 */       for (String tmp : sqls) {
/* 112 */         if (!tmp.trim().equals(""))
/*     */         {
/*     */           try
/*     */           {
/* 116 */             stmt = c.createStatement();
/* 117 */             stmt.executeUpdate(tmp);
/*     */           } catch (Exception ex) {
/* 119 */             log.error("ErrorSQL:" + tmp);
/*     */             try {
/* 121 */               if (stmt != null) {
/* 122 */                 stmt.close();
/*     */               }
/*     */             }
/*     */             catch (Exception e) {}
/* 126 */             throw ex;
/*     */           } }
/*     */       }
/* 129 */       c.commit();
/*     */     } catch (Exception ex) {
/* 131 */       event.setMsg(ImplUtil.getSQLExceptionMsg(ex));
/*     */       try {
/* 133 */         c.rollback();
/*     */       }
/*     */       catch (Exception e) {}
/*     */     } finally {
/* 137 */       event.setEnd_date(new Date());
/* 138 */       event.setFinished(true);
/* 139 */       DbUtil.closeConnection(c);
/*     */     }
/* 141 */     doSEQWork();
/*     */   }
/*     */   
/*     */   public ValidateSQLResult getEventState(String workId) throws RemoteException
/*     */   {
/* 146 */     ValidateSQLResult vs = new ValidateSQLResult();
/* 147 */     vs.setResult(3);
/* 148 */     EventObj event = (EventObj)finishWorks.get(workId);
/* 149 */     if (event != null) {
/* 150 */       finishWorks.remove(event.getId());
/* 151 */       vs.setMsg(event.getMsg());
/* 152 */       vs.setEnd_date(event.getEnd_date());
/* 153 */       vs.setStart_date(event.getStart_date());
/* 154 */       vs.setResult(vs.getMsg() != null ? 1 : 0);
/*     */     } else {
/* 156 */       for (EventObj e : activeWorks.values()) {
/* 157 */         if (e.getId().equals(workId)) {
/* 158 */           event = e;
/* 159 */           break;
/*     */         }
/*     */       }
/* 162 */       if (event != null) {
/* 163 */         vs.setEnd_date(event.getEnd_date());
/* 164 */         vs.setStart_date(event.getStart_date());
/* 165 */         vs.setResult(2);
/*     */       }
/*     */     }
/* 168 */     return vs;
/*     */   }
/*     */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\EventServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */