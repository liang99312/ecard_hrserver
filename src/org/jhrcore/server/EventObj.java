/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import java.util.Date;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ class EventObj
/*     */ {
/*     */   private String id;
/*     */   private String workId;
/*     */   private String exSQL;
/* 177 */   private boolean started = false;
/* 178 */   private boolean finished = false;
/*     */   private Date start_date;
/*     */   private Date end_date;
/*     */   private String msg;
/*     */   
/*     */   public EventObj(String id, String workId, String exSQL) {
/* 184 */     this.id = id;
/* 185 */     this.workId = workId;
/* 186 */     this.exSQL = exSQL;
/*     */   }
/*     */   
/*     */   public String getId() {
/* 190 */     return this.id;
/*     */   }
/*     */   
/*     */   public void setId(String id) {
/* 194 */     this.id = id;
/*     */   }
/*     */   
/*     */   public String getExSQL() {
/* 198 */     return this.exSQL;
/*     */   }
/*     */   
/*     */   public void setExSQL(String exSQL) {
/* 202 */     this.exSQL = exSQL;
/*     */   }
/*     */   
/*     */   public String getWorkId() {
/* 206 */     return this.workId;
/*     */   }
/*     */   
/*     */   public void setWorkId(String workId) {
/* 210 */     this.workId = workId;
/*     */   }
/*     */   
/*     */   public boolean isFinished() {
/* 214 */     return this.finished;
/*     */   }
/*     */   
/*     */   public void setFinished(boolean finished) {
/* 218 */     this.finished = finished;
/*     */   }
/*     */   
/*     */   public boolean isStarted() {
/* 222 */     return this.started;
/*     */   }
/*     */   
/*     */   public void setStarted(boolean started) {
/* 226 */     this.started = started;
/*     */   }
/*     */   
/*     */   public String getMsg() {
/* 230 */     return this.msg;
/*     */   }
/*     */   
/*     */   public void setMsg(String msg) {
/* 234 */     this.msg = msg;
/*     */   }
/*     */   
/*     */   public Date getEnd_date() {
/* 238 */     return this.end_date;
/*     */   }
/*     */   
/*     */   public void setEnd_date(Date end_date) {
/* 242 */     this.end_date = end_date;
/*     */   }
/*     */   
/*     */   public Date getStart_date() {
/* 246 */     return this.start_date;
/*     */   }
/*     */   
/*     */   public void setStart_date(Date start_date) {
/* 250 */     this.start_date = start_date;
/*     */   }
/*     */   
/*     */   public boolean equals(Object obj)
/*     */   {
/* 255 */     if (obj == null) {
/* 256 */       return false;
/*     */     }
/* 258 */     if (getClass() != obj.getClass()) {
/* 259 */       return false;
/*     */     }
/* 261 */     EventObj other = (EventObj)obj;
/* 262 */     if (this.id == null ? other.id != null : !this.id.equals(other.id)) {
/* 263 */       return false;
/*     */     }
/* 265 */     return true;
/*     */   }
/*     */   
/*     */   public int hashCode()
/*     */   {
/* 270 */     int hash = 7;
/* 271 */     hash = 13 * hash + (this.id != null ? this.id.hashCode() : 0);
/* 272 */     return hash;
/*     */   }
/*     */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\EventObj.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */