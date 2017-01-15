/*    */ package org.jhrcore.server.timer;
/*    */ 
/*    */ import java.util.Date;
/*    */ import java.util.Timer;
/*    */ import java.util.TimerTask;
/*    */ import org.jhrcore.server.EventServiceImpl;
/*    */ import org.jhrcore.util.UtilTool;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class HRSQLTask
/*    */   implements HRTask
/*    */ {
/* 19 */   private boolean started = false;
/*    */   private Timer timer;
/*    */   
/*    */   public long getPeriod()
/*    */   {
/* 24 */     return 200L;
/*    */   }
/*    */   
/*    */   public Date getStartDate()
/*    */   {
/* 29 */     return new Date();
/*    */   }
/*    */   
/*    */   public boolean isEnable()
/*    */   {
/* 34 */     return true;
/*    */   }
/*    */   
/*    */   public TimerTask getTask()
/*    */   {
/* 39 */     return new TimerTask()
/*    */     {
/*    */       public void run()
/*    */       {
/*    */         try {
/* 44 */           new EventServiceImpl();EventServiceImpl.doSEQWork();
/*    */         }
/*    */         catch (Exception ex) {}
/*    */       }
/*    */     };
/*    */   }
/*    */   
/*    */   public boolean isStart()
/*    */   {
/* 53 */     return this.started;
/*    */   }
/*    */   
/*    */   public String getId()
/*    */   {
/* 58 */     return "SQL";
/*    */   }
/*    */   
/*    */   public void start()
/*    */   {
/* 63 */     String sql = "update C21 with(rowlock) set C21.c2113=isnull(HRGZ_1.C21_c2113,0),C21.c210c=isnull(HRGZ_1.C21_c210c,0),C21.a0101=HRGZ_1.C21_a0101,C21.c210a=isnull(HRGZ_1.C21_c210a,0),C21.c2137=isnull(HRGZ_1.C21_c2137,0),C21.c211e=isnull(HRGZ_1.C21_c211e,0),C21.c2121=isnull(HRGZ_1.C21_c2121,0) from HRGZ_1 HRGZ_1,C21 C21 where C21.pay_key=HRGZ_1.pay_key ";
/* 64 */     String workId = "-a77aca2:14076caa114:-8000";
/* 65 */     int threads = 20;
/* 66 */     for (int i = 0; i < threads; i++) {
/* 67 */       EventServiceImpl.doSEQWork(UtilTool.getUID(), workId, sql);
/*    */     }
/* 69 */     this.timer = new Timer(true);
/* 70 */     this.timer.schedule(getTask(), getStartDate(), getPeriod());
/* 71 */     this.started = true;
/*    */   }
/*    */   
/*    */   public void stop() {}
/*    */ }
