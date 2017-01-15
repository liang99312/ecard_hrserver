/*    */ package org.jhrcore.server.timer;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.Hashtable;
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ import org.jhrcore.comm.CommThreadPool;
/*    */ import org.jhrcore.server.HibernateUtil;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class HRTimer
/*    */ {
/*    */   private static HRTimer timer;
/* 20 */   private List<HRTask> tasks = new ArrayList();
/* 21 */   private Hashtable<String, HRTask> taskKeys = new Hashtable();
/*    */   
/*    */   public static HRTimer getTimer()
/*    */   {
/* 25 */     if (timer == null) {
/* 26 */       timer = new HRTimer();
/*    */     }
/* 28 */     return timer;
/*    */   }
/*    */   
/*    */   public void register() {
/* 32 */     List list = HibernateUtil.fetchEntitiesBy("from SysBackScheme ss join fetch ss.funtionRight left join fetch ss.sysBackFields order by ss.scheme_no");
/* 33 */     Object obj; for (Iterator i$ = list.iterator(); i$.hasNext(); obj = i$.next()) {}
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public void start()
/*    */   {
/* 45 */     register();
/* 46 */     for (final HRTask task : this.taskKeys.values()) {
/* 47 */       CommThreadPool.getClientThreadPool().handleEvent(new Runnable()
/*    */       {
/*    */         public void run()
/*    */         {
/*    */           try {
/* 52 */             HRTimer.this.startTask(task);
/*    */           } catch (Exception ex) {
/* 54 */             ex.printStackTrace();
/*    */           }
/*    */         }
/*    */       });
/*    */     }
/*    */   }
/*    */   
/*    */   public void startTask(String id) throws Exception {
/* 62 */     HRTask task = getTask(id);
/* 63 */     if (task != null) {
/* 64 */       startTask(task);
/* 65 */       return;
/*    */     }
/* 67 */     throw new Exception("未锟揭碉拷锟斤拷应锟斤拷锟斤拷,锟斤拷锟斤拷锟斤拷HR锟斤拷锟斤拷锟斤拷");
/*    */   }
/*    */   
/*    */   public void startTask(HRTask task) throws Exception {
/* 71 */     if ((task == null) || (!task.isEnable())) {
/* 72 */       return;
/*    */     }
/* 74 */     this.tasks.add(task);
/* 75 */     this.taskKeys.put(task.getId(), task);
/* 76 */     task.start();
/*    */   }
/*    */   
/*    */   public void stopTask(String id) throws Exception {
/* 80 */     HRTask task = getTask(id);
/* 81 */     if (task != null) {
/* 82 */       stopTask(task);
/* 83 */       return;
/*    */     }
/* 85 */     throw new Exception("未锟揭碉拷锟斤拷应锟斤拷锟斤拷,锟斤拷锟斤拷锟斤拷HR锟斤拷锟斤拷锟斤拷");
/*    */   }
/*    */   
/*    */   public void stopTask(HRTask task) throws Exception {
/* 89 */     task.stop();
/*    */   }
/*    */   
/*    */   public HRTask getTask(String id) {
/* 93 */     return (HRTask)this.taskKeys.get(id);
/*    */   }
/*    */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\timer\HRTimer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */