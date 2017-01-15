package org.jhrcore.server.timer;

import java.util.Date;
import java.util.TimerTask;

public abstract interface HRTask
{
  public abstract long getPeriod();
  
  public abstract Date getStartDate();
  
  public abstract boolean isEnable();
  
  public abstract TimerTask getTask();
  
  public abstract boolean isStart();
  
  public abstract String getId();
  
  public abstract void start();
  
  public abstract void stop();
}


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\timer\HRTask.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */