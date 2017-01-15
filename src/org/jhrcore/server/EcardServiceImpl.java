/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.rmi.RemoteException;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Calendar;
/*     */ import java.util.Collections;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Random;
/*     */ import javax.ejb.Remote;
/*     */ import javax.ejb.Stateless;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.hibernate.Query;
/*     */ import org.hibernate.SQLQuery;
/*     */ import org.hibernate.Session;
/*     */ import org.hibernate.Transaction;
/*     */ import org.jboss.annotation.ejb.Clustered;
/*     */ import org.jhrcore.entity.SysParameter;
/*     */ import org.jhrcore.entity.ecard.Ecard;
/*     */ import org.jhrcore.entity.ecard.Ecard_chu;
/*     */ import org.jhrcore.entity.ecard.Ecard_leave;
/*     */ import org.jhrcore.entity.ecard.Ecard_ru;
/*     */ import org.jhrcore.entity.ecard.Epos;
/*     */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*     */ import org.jhrcore.iservice.EcardService;
/*     */ import org.jhrcore.server.util.ImplUtil;
/*     */ import org.jhrcore.server.util.RandomUtil;
/*     */ import org.jhrcore.util.DateUtil;
/*     */ import org.jhrcore.util.SysUtil;
/*     */ import org.jhrcore.util.UtilTool;
/*     */ 
/*     */ 
/*     */ 
/*     */ @Clustered
/*     */ @Stateless
/*     */ @Remote({EcardService.class})
/*     */ public class EcardServiceImpl
/*     */   extends SuperImpl
/*     */   implements EcardService
/*     */ {
/*  47 */   private static Logger log = Logger.getLogger(EcardServiceImpl.class);
/*  48 */   private List eposList = null;
/*     */   
/*     */   public EcardServiceImpl() throws RemoteException {
/*  51 */     super(ServerApp.rmiPort);
/*     */   }
/*     */   
/*     */   public EcardServiceImpl(int port) throws RemoteException {
/*  55 */     super(port);
/*     */   }
/*     */   
/*     */   public ValidateSQLResult calcHuiKuan(String ym, String state, List<String> keys) throws RemoteException
/*     */   {
/*  60 */     ValidateSQLResult result = new ValidateSQLResult();
/*  61 */     Session session = HibernateUtil.currentSession();
/*  62 */     Transaction tx = session.beginTransaction();
/*  63 */     List<String> typeList = new ArrayList();
/*  64 */     typeList.add("普养");
/*  65 */     typeList.add("中养");
/*  66 */     typeList.add("精养");
/*     */     try {
/*  68 */       List cardList = null;
/*  69 */       String where_sql = "";
/*  70 */       if ("指定卡".equals(state)) {
/*  71 */         StringBuilder sb = new StringBuilder();
/*  72 */         sb.append("'-1'");
/*  73 */         for (String s : keys) {
/*  74 */           sb.append(",'").append(s).append("'");
/*     */         }
/*  76 */         where_sql = " and ecard_key in(" + sb.toString() + ")";
/*  77 */       } else if (!"所有卡".equals(state)) {
/*  78 */         if (state.contains("-")) {
/*  79 */           String[] strs = state.split("-");
/*  80 */           if (!"".equals(strs[0])) {
/*  81 */             where_sql = where_sql + " and ecard_state='" + strs[0] + "'";
/*     */           }
/*  83 */           if (!"".equals(strs[1])) {
/*  84 */             if (typeList.contains(strs[1])) {
/*  85 */               where_sql = where_sql + " and ecard_type='" + strs[1] + "'";
/*     */             } else {
/*  87 */               where_sql = where_sql + " and ecard_manager='" + strs[1] + "'";
/*     */             }
/*     */           }
/*     */         } else {
/*  91 */           where_sql = " and ecard_state='" + state + "'";
/*     */         }
/*     */       }
/*  94 */       cardList = session.createQuery("from Ecard where 1=1 " + where_sql).list();
/*  95 */       Calendar c1 = Calendar.getInstance();
/*  96 */       Calendar c2 = Calendar.getInstance();
/*  97 */       SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
/*  98 */       c1.setTime(format.parse(ym + "01"));
/*  99 */       c2.setTime(format.parse(ym + "01"));
/* 100 */       c2.add(2, 2);
/* 101 */       String hql = "from Ecard_leave k where 1=1";
/* 102 */       String startTime = DateUtil.toStringForQuery(c1.getTime());
/* 103 */       String endTime = DateUtil.toStringForQuery(c2.getTime());
/* 104 */       hql = hql + " and k.ecard_leave_flag='leave' and k.ecard_leave_date>= " + startTime + " and k.ecard_leave_date<= " + endTime;
/* 105 */       List leave_list = session.createQuery(hql).list();
/* 106 */       List exist_dates = new ArrayList();
/* 107 */       for (Object obj : leave_list) {
/* 108 */         Ecard_leave ecard_leave = (Ecard_leave)obj;
/* 109 */         exist_dates.add(DateUtil.DateToStr(ecard_leave.getEcard_leave_date()));
/*     */       }
/*     */       
/* 112 */       String hql2 = "from Ecard_leave k where 1=1";
/* 113 */       hql2 = hql2 + " and k.ecard_leave_flag='holiday' and k.ecard_leave_date>= " + startTime + " and k.ecard_leave_date<= " + endTime;
/* 114 */       List leave_list2 = session.createQuery(hql2).list();
/* 115 */       List exist_dates_h = new ArrayList();
/* 116 */       for (Object obj : leave_list2) {
/* 117 */         Ecard_leave ecard_leave = (Ecard_leave)obj;
/* 118 */         exist_dates_h.add(DateUtil.DateToStr(ecard_leave.getEcard_leave_date()));
/*     */       }
/*     */       
/* 121 */       session.createSQLQuery("delete from Ecard_ru where ru_ym='" + ym + "' and ecard_key in(select ecard_key from Ecard where 1=1 " + where_sql + ")").executeUpdate();
/* 122 */       session.flush();
/* 123 */       for (Object obj : cardList) {
/* 124 */         Ecard ecard = (Ecard)obj;
/* 125 */         List<Ecard_ru> list = buildEcard_ru(ecard, ym, exist_dates, exist_dates_h);
/* 126 */         int cs = 0;
/* 127 */         while (list == null) {
/* 128 */           if (cs > 9) {
/* 129 */             result.setResult(-1);
/* 130 */             result.setMsg("生成汇款数据有错误");
/* 131 */             return result;
/*     */           }
/* 133 */           cs++;
/* 134 */           list = buildEcard_ru(ecard, ym, exist_dates, exist_dates_h);
/*     */         }
/* 136 */         if (list != null) {
/* 137 */           for (Ecard_ru ru : list) {
/* 138 */             session.save(ru);
/*     */           }
/*     */         }
/*     */       }
/* 142 */       session.flush();
/* 143 */       tx.commit();
/* 144 */       result.setResult(0);
/* 145 */       result.setMsg("生成汇款成功");
/* 146 */       ValidateSQLResult xfResult = calcXiaoFei(ym, state, keys);
/* 147 */       if (xfResult.getResult() == 0) {
/* 148 */         result.setMsg("生成数据成功");
/*     */       } else {
/* 150 */         result.setResult(1);
/* 151 */         result.setMsg("生成汇款成功，但消费数据需要重新生成！" + xfResult.getMsg());
/*     */       }
/*     */     } catch (Exception e) {
/* 154 */       e.printStackTrace();
/* 155 */       result.setResult(1);
/* 156 */       result.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 157 */       ImplUtil.rollBack(tx);
/* 158 */       log.error(e);
/*     */     } finally {
/* 160 */       HibernateUtil.closeSession();
/*     */     }
/* 162 */     return result;
/*     */   }
/*     */   
/*     */   private List<Ecard_ru> buildEcard_ru(Ecard e, String ym, List exist_dates, List exist_dates_h) throws ParseException {
/* 166 */     List<Ecard_ru> result = new ArrayList();
/* 167 */     int bs = e.getM_zonge().intValue() / 100;
/*     */     
/*     */ 
/* 170 */     List<Date> dateList = new ArrayList();
/* 171 */     Calendar c1 = Calendar.getInstance();
/* 172 */     Calendar c2 = Calendar.getInstance();
/* 173 */     SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
/* 174 */     if (e.getM_hkstart().intValue() < 10) {
/* 175 */       c1.setTime(format.parse(ym + "0" + e.getM_hkstart()));
/*     */     } else {
/* 177 */       c1.setTime(format.parse(ym + e.getM_hkstart()));
/*     */     }
/* 179 */     if (e.getM_hkend().intValue() < 10) {
/* 180 */       c2.setTime(format.parse(ym + "0" + e.getM_hkend()));
/*     */     } else {
/* 182 */       c2.setTime(format.parse(ym + e.getM_hkend()));
/*     */     }
/* 184 */     if (c2.getTime().before(c1.getTime())) {
/* 185 */       c2.add(2, 1);
/*     */     }
/* 187 */     System.out.println(DateUtil.DateToStr(c1.getTime()));
/* 188 */     System.out.println(DateUtil.DateToStr(c2.getTime()));
/* 189 */     Calendar c_temp = Calendar.getInstance();
/* 190 */     c_temp.setTime(c2.getTime());
/* 191 */     while (!c_temp.getTime().before(c1.getTime()))
/* 192 */       if (exist_dates.contains(DateUtil.DateToStr(c_temp.getTime()))) {
/* 193 */         c_temp.add(5, -1);
/*     */       }
/*     */       else {
/* 196 */         dateList.add(c_temp.getTime());
/* 197 */         c_temp.add(5, -1);
/*     */       }
/* 199 */     Date zdd = null;
/* 200 */     if (e.getEcard_jifen().intValue() > 0) {
/* 201 */       int tempIndex = 0;
/* 202 */       Random rr = new Random();
/* 203 */       int r = rr.nextInt(dateList.size() - 2) + 1;
/*     */       
/* 205 */       while (zdd == null) {
/* 206 */         Date tempDate = (Date)dateList.get(r);
/* 207 */         if (!exist_dates_h.contains(DateUtil.DateToStr(tempDate))) {
/* 208 */           zdd = tempDate;
/* 209 */           dateList.remove(r);
/* 210 */           break;
/*     */         }
/* 212 */         rr = new Random();
/* 213 */         r = rr.nextInt(dateList.size() - 2) + 1;
/* 214 */         tempIndex++;
/* 215 */         if (tempIndex == 100) {
/* 216 */           return null;
/*     */         }
/*     */       }
/*     */     }
/*     */     
/* 221 */     int rqs = dateList.size();
/* 222 */     int cishu = e.getM_cishu().intValue();
/* 223 */     if (e.getEcard_jifen().intValue() > 0) {
/* 224 */       cishu -= 1;
/*     */     }
/* 226 */     if (rqs < cishu) {
/* 227 */       cishu = rqs;
/*     */     }
/*     */     
/* 230 */     if ((e.getEcard_jifen().intValue() > 0) && (zdd != null)) {
/* 231 */       boolean zdFlag = false;
/* 232 */       Date tempZdd = null;
/* 233 */       Date tempZmm = null;
/* 234 */       int zdhk = e.getM_zonge().intValue() * e.getEcard_jifen().intValue() / 100;
/* 235 */       Integer[] rq_sz = RandomUtil.getRandomRu(e.getM_zonge().intValue() - zdhk, cishu, rqs, 10);
/* 236 */       for (int i = 0; i < rq_sz.length; i++) {
/* 237 */         Date d = (Date)dateList.get(i);
/* 238 */         if (rq_sz[i].intValue() > 0) {
/* 239 */           if (d.after(zdd)) {
/* 240 */             zdFlag = true;
/*     */           }
/* 242 */           if ((tempZdd == null) || (tempZdd.before(d))) {
/* 243 */             tempZdd = d;
/*     */           }
/* 245 */           if ((tempZmm == null) || (d.before(tempZmm))) {
/* 246 */             tempZmm = d;
/*     */           }
/* 248 */           Ecard_ru ru = (Ecard_ru)UtilTool.createUIDEntity(Ecard_ru.class);
/* 249 */           ru.setEcard_ru_key(e.getEcard_key() + "_" + format.format(d));
/* 250 */           ru.setEcard_bank(e.getEcard_bank());
/* 251 */           ru.setEcard_code(e.getEcard_code());
/* 252 */           ru.setEcard_key(e.getEcard_key());
/* 253 */           ru.setEcard_name(e.getEcard_name());
/* 254 */           ru.setRu_date(d);
/* 255 */           ru.setRu_je(rq_sz[i]);
/* 256 */           ru.setRu_ym(ym);
/* 257 */           result.add(ru);
/*     */         }
/*     */       }
/*     */       
/* 261 */       if (!zdFlag) {
/* 262 */         for (Ecard_ru ru : result) {
/* 263 */           if ((ru.getRu_date().after(tempZmm)) && (!exist_dates_h.contains(DateUtil.DateToStr(ru.getRu_date())))) {
/* 264 */             Date tempDate = ru.getRu_date();
/* 265 */             ru.setRu_date(zdd);
/* 266 */             ru.setEcard_ru_key(e.getEcard_key() + "_" + format.format(zdd));
/* 267 */             zdd = tempDate;
/* 268 */             break;
/*     */           }
/*     */         }
/*     */       }
/*     */       
/* 273 */       Ecard_ru ru = (Ecard_ru)UtilTool.createUIDEntity(Ecard_ru.class);
/* 274 */       ru.setEcard_ru_key(e.getEcard_key() + "_" + format.format(zdd));
/* 275 */       ru.setEcard_bank(e.getEcard_bank());
/* 276 */       ru.setEcard_code(e.getEcard_code());
/* 277 */       ru.setEcard_key(e.getEcard_key());
/* 278 */       ru.setEcard_name(e.getEcard_name());
/* 279 */       ru.setRu_date(zdd);
/* 280 */       ru.setRu_flag("是");
/* 281 */       ru.setRu_je(Integer.valueOf(zdhk));
/* 282 */       ru.setRu_ym(ym);
/* 283 */       result.add(ru);
/*     */     } else {
/* 285 */       Integer[] rq_sz = RandomUtil.getRandomRu(e.getM_zonge().intValue(), cishu, rqs, 10);
/* 286 */       for (int i = 0; i < rq_sz.length; i++) {
/* 287 */         Date d = (Date)dateList.get(i);
/* 288 */         if (rq_sz[i].intValue() > 0) {
/* 289 */           Ecard_ru ru = (Ecard_ru)UtilTool.createUIDEntity(Ecard_ru.class);
/* 290 */           ru.setEcard_ru_key(e.getEcard_key() + "_" + format.format(d));
/* 291 */           ru.setEcard_bank(e.getEcard_bank());
/* 292 */           ru.setEcard_code(e.getEcard_code());
/* 293 */           ru.setEcard_key(e.getEcard_key());
/* 294 */           ru.setEcard_name(e.getEcard_name());
/* 295 */           ru.setRu_date(d);
/* 296 */           ru.setRu_je(rq_sz[i]);
/* 297 */           ru.setRu_ym(ym);
/* 298 */           result.add(ru);
/*     */         }
/*     */       }
/*     */     }
/*     */     
/* 303 */     return result;
/*     */   }
/*     */   
/*     */   public ValidateSQLResult calcXiaoFei(String ym, String state, List<String> keys) throws RemoteException
/*     */   {
/* 308 */     ValidateSQLResult result = new ValidateSQLResult();
/* 309 */     Session session = HibernateUtil.currentSession();
/* 310 */     Transaction tx = session.beginTransaction();
/* 311 */     List<String> typeList = new ArrayList();
/* 312 */     typeList.add("普养");
/* 313 */     typeList.add("中养");
/* 314 */     typeList.add("精养");
/*     */     try {
/* 316 */       List cardList = null;
/* 317 */       String where_sql = "";
/* 318 */       if ("指定卡".equals(state)) {
/* 319 */         StringBuilder sb = new StringBuilder();
/* 320 */         sb.append("'-1'");
/* 321 */         for (String s : keys) {
/* 322 */           sb.append(",'").append(s).append("'");
/*     */         }
/* 324 */         where_sql = " and ecard_key in(" + sb.toString() + ")";
/* 325 */       } else if (!"所有卡".equals(state)) {
/* 326 */         if (state.contains("-")) {
/* 327 */           String[] strs = state.split("-");
/* 328 */           if (!"".equals(strs[0])) {
/* 329 */             where_sql = where_sql + " and ecard_state='" + strs[0] + "'";
/*     */           }
/* 331 */           if (!"".equals(strs[1])) {
/* 332 */             if (typeList.contains(strs[1])) {
/* 333 */               where_sql = where_sql + " and ecard_type='" + strs[1] + "'";
/*     */             } else {
/* 335 */               where_sql = where_sql + " and ecard_manager='" + strs[1] + "'";
/*     */             }
/*     */           }
/*     */         } else {
/* 339 */           where_sql = " and ecard_state='" + state + "'";
/*     */         }
/*     */       }
/* 342 */       cardList = session.createQuery("from Ecard where 1=1 " + where_sql).list();
/* 343 */       Calendar c1 = Calendar.getInstance();
/* 344 */       Calendar c2 = Calendar.getInstance();
/* 345 */       SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
/* 346 */       c1.setTime(format.parse(ym + "01"));
/* 347 */       c2.setTime(format.parse(ym + "01"));
/* 348 */       c2.add(2, 2);
/* 349 */       this.eposList = session.createQuery("from Epos where epos_qiyong='是' order by epos_num").list();
/* 350 */       Hashtable<String, ArrayList> flTable = new Hashtable();
/* 351 */       for (Object obj : this.eposList) {
/* 352 */         Epos epos = (Epos)obj;
/* 353 */         if (epos.getChu_down() == null) {
/* 354 */           epos.setChu_down(Float.valueOf(0.0F));
/*     */         }
/* 356 */         if (epos.getChu_up() == null) {
/* 357 */           epos.setChu_up(Float.valueOf(1000000.0F));
/*     */         }
/* 359 */         String f = epos.getEpos_fei();
/* 360 */         float ff = Float.parseFloat(f);
/* 361 */         String key = "38";
/* 362 */         if ((ff < 0.01D) && (ff > 0.005D)) {
/* 363 */           key = "78";
/* 364 */         } else if (ff > 0.01D) {
/* 365 */           key = "125";
/*     */         }
/* 367 */         ArrayList tempList = null;
/* 368 */         if (flTable.containsKey(key)) {
/* 369 */           tempList = (ArrayList)flTable.get(key);
/*     */         } else {
/* 371 */           tempList = new ArrayList();
/* 372 */           flTable.put(key, tempList);
/*     */         }
/* 374 */         tempList.add(epos);
/*     */       }
/* 376 */       Hashtable<String, Float> ykcpTable = new Hashtable();
/* 377 */       ykcpTable.put("普养", Float.valueOf(0.55F));
/* 378 */       ykcpTable.put("中养", Float.valueOf(0.6F));
/* 379 */       ykcpTable.put("精养", Float.valueOf(0.65F));
/* 380 */       List paramList = session.createQuery("from SysParameter where sysParameter_key in('ecard_cb_py','ecard_cb_zy','ecard_cb_jy')").list();
/* 381 */       for (Object obj : paramList) {
/* 382 */         SysParameter sp = (SysParameter)obj;
/* 383 */         if ("ecard_cb_py".equals(sp.getSysParameter_key())) {
/* 384 */           if (SysUtil.isFloat(sp.getSysparameter_value())) {
/* 385 */             ykcpTable.remove("普养");
/* 386 */             ykcpTable.put("普养", SysUtil.objToFloat(sp.getSysparameter_value()));
/*     */           }
/* 388 */         } else if ("ecard_cb_zy".equals(sp.getSysParameter_key())) {
/* 389 */           if (SysUtil.isFloat(sp.getSysparameter_value())) {
/* 390 */             ykcpTable.remove("中养");
/* 391 */             ykcpTable.put("中养", SysUtil.objToFloat(sp.getSysparameter_value()));
/*     */           }
/* 393 */         } else if (("ecard_cb_jy".equals(sp.getSysParameter_key())) && 
/* 394 */           (SysUtil.isFloat(sp.getSysparameter_value()))) {
/* 395 */           ykcpTable.remove("精养");
/* 396 */           ykcpTable.put("精养", SysUtil.objToFloat(sp.getSysparameter_value()));
/*     */         }
/*     */       }
/*     */       
/* 400 */       String hql = "from Ecard_leave k where 1=1";
/* 401 */       String startTime = DateUtil.toStringForQuery(c1.getTime());
/* 402 */       String endTime = DateUtil.toStringForQuery(c2.getTime());
/* 403 */       hql = hql + " and k.ecard_leave_flag='holiday' and k.ecard_leave_date>= " + startTime + " and k.ecard_leave_date<= " + endTime;
/* 404 */       List leave_list = session.createQuery(hql).list();
/* 405 */       List exist_dates = new ArrayList();
/* 406 */       for (Object obj : leave_list) {
/* 407 */         Ecard_leave ecard_leave = (Ecard_leave)obj;
/* 408 */         exist_dates.add(DateUtil.DateToStr(ecard_leave.getEcard_leave_date()));
/*     */       }
/* 410 */       List ruList = session.createQuery("from Ecard_ru r where r.ru_ym='" + ym + "' and r.ecard_key in(select ecard_key from Ecard where 1=1 " + where_sql + ")").list();
/* 411 */       Hashtable<String, List<Ecard_ru>> ruTable = new Hashtable();
/* 412 */       for (Object obj : ruList) {
/* 413 */         Ecard_ru r = (Ecard_ru)obj;
/* 414 */         List<Ecard_ru> l = new ArrayList();
/* 415 */         if (!ruTable.containsKey(r.getEcard_key())) {
/* 416 */           ruTable.put(r.getEcard_key(), l);
/*     */         } else {
/* 418 */           l = (List)ruTable.get(r.getEcard_key());
/*     */         }
/* 420 */         l.add(r);
/*     */       }
/* 422 */       session.createSQLQuery("delete from Ecard_chu where chu_ym='" + ym + "' and ecard_key in(select ecard_key from Ecard where 1=1 " + where_sql + ")").executeUpdate();
/* 423 */       session.flush();
/* 424 */       for (Object obj : cardList) {
/* 425 */         Ecard ecard = (Ecard)obj;
/* 426 */         if (ruTable.containsKey(ecard.getEcard_key())) {
/* 427 */           List<Ecard_chu> list = buildEcard_chu(ecard, ym, flTable, (List)ruTable.get(ecard.getEcard_key()), (Float)ykcpTable.get(ecard.getEcard_type()), exist_dates);
/* 428 */           int cs = 0;
/* 429 */           while (list == null) {
/* 430 */             if (cs > 9) {
/* 431 */               result.setResult(-1);
/* 432 */               result.setMsg("生成消费数据有错误，可以重新生成或者调整汇款数据后再生成。");
/* 433 */               return result;
/*     */             }
/* 435 */             cs++;
/* 436 */             list = buildEcard_chu(ecard, ym, flTable, (List)ruTable.get(ecard.getEcard_key()), (Float)ykcpTable.get(ecard.getEcard_type()), exist_dates);
/*     */           }
/* 438 */           for (Ecard_chu chu : list) {
/* 439 */             session.save(chu);
/*     */           }
/*     */         }
/*     */       }
/* 443 */       session.flush();
/* 444 */       tx.commit();
/* 445 */       result.setResult(0);
/* 446 */       result.setMsg("保存成功");
/*     */     } catch (Exception e) {
/* 448 */       e.printStackTrace();
/* 449 */       result.setResult(1);
/* 450 */       result.setMsg(ImplUtil.getSQLExceptionMsg(e));
/* 451 */       ImplUtil.rollBack(tx);
/* 452 */       log.error(e);
/*     */     } finally {
/* 454 */       HibernateUtil.closeSession();
/*     */     }
/* 456 */     return result;
/*     */   }
/*     */   
/*     */   private List<Ecard_chu> buildEcard_chu_zd(Ecard e, List<Ecard_ru> ruList, String ym, Hashtable<String, Integer> ecsTable) {
/* 460 */     List<Ecard_chu> result = new ArrayList();
/* 461 */     if (ruList.isEmpty()) {
/* 462 */       return result;
/*     */     }
/* 464 */     List<Epos> posList = new ArrayList();
/* 465 */     for (Object obj : this.eposList) {
/* 466 */       Epos epos = (Epos)obj;
/* 467 */       if ("是".equals(epos.getEpos_dae())) {
/* 468 */         posList.add(epos);
/*     */       }
/*     */     }
/* 471 */     for (Ecard_ru r : ruList) {
/* 472 */       Ecard_chu chu = (Ecard_chu)UtilTool.createUIDEntity(Ecard_chu.class);
/* 473 */       chu.setEcard_bank(e.getEcard_bank());
/* 474 */       chu.setEcard_code(e.getEcard_code());
/* 475 */       chu.setEcard_key(e.getEcard_key());
/* 476 */       chu.setEcard_name(e.getEcard_name());
/* 477 */       chu.setChu_date(r.getRu_date());
/* 478 */       chu.setChu_je(Float.valueOf(0.0F + r.getRu_je().intValue()));
/* 479 */       chu.setChu_ym(ym);
/* 480 */       if (posList.isEmpty()) {
/* 481 */         chu.setChu_fl("0.0038");
/*     */       } else {
/* 483 */         Random random = new Random();
/* 484 */         int i = random.nextInt(posList.size());
/* 485 */         Epos epos = (Epos)posList.get(i);
/* 486 */         chu.setChu_item(epos.getEpos_item());
/* 487 */         chu.setEpos_code(epos.getEpos_code());
/* 488 */         chu.setEpos_name(epos.getEpos_name());
/* 489 */         chu.setChu_fl(epos.getEpos_fei());
/* 490 */         chu.setEpos(epos);
/* 491 */         int temp_cishu = 0;
/* 492 */         if (ecsTable.containsKey(epos.getEpos_key())) {
/* 493 */           temp_cishu = ((Integer)ecsTable.get(epos.getEpos_key())).intValue();
/*     */         }
/* 495 */         temp_cishu += 1;
/* 496 */         ecsTable.remove(epos.getEpos_key());
/* 497 */         ecsTable.put(epos.getEpos_key(), Integer.valueOf(temp_cishu));
/*     */       }
/* 499 */       result.add(chu);
/*     */     }
/* 501 */     return result;
/*     */   }
/*     */   
/*     */   private List<Ecard_chu> buildEcard_chu(Ecard e, String ym, Hashtable<String, ArrayList> flTable, List<Ecard_ru> ruList, Float cb, List exist_dates) throws ParseException {
/* 505 */     List<Ecard_chu> result = new ArrayList();
/* 506 */     int csbs = e.getX_cishu().intValue() / ruList.size();
/* 507 */     int csyx = e.getX_cishu().intValue() % ruList.size();
/* 508 */     List<Integer> csyxList = new ArrayList();
/* 509 */     List<Ecard_chu> chuList = new ArrayList();
/* 510 */     int tempSum = 0;
/* 511 */     float holidayFl = 0.0F;
/* 512 */     for (int i = 0; i < ruList.size(); i++) {
/* 513 */       Ecard_ru r = (Ecard_ru)ruList.get(i);
/* 514 */       if (!"是".equals(r.getRu_flag()))
/*     */       {
/*     */ 
/* 517 */         if (tempSum < csyx) {
/* 518 */           csyxList.add(Integer.valueOf(1));
/* 519 */           tempSum++;
/*     */         } else {
/* 521 */           csyxList.add(Integer.valueOf(0));
/*     */         } }
/*     */     }
/* 524 */     Collections.shuffle(csyxList);
/* 525 */     int index = 0;
/* 526 */     int ye = 0;
/* 527 */     int zddze = 0;
/* 528 */     List<Ecard_ru> zdruList = new ArrayList();
/* 529 */     for (int k = 0; k < ruList.size(); k++) {
/* 530 */       Ecard_ru r = (Ecard_ru)ruList.get(k);
/* 531 */       if ("是".equals(r.getRu_flag())) {
/* 532 */         zdruList.add(r);
/* 533 */         zddze += r.getRu_je().intValue();
/*     */       }
/*     */       else {
/* 536 */         int xfje = r.getRu_je().intValue();
/* 537 */         if (k != ruList.size() - 1) {
/* 538 */           Random rd = new Random();
/* 539 */           int tempYe = 2 + rd.nextInt(9);
/* 540 */           ye = tempYe + ye;
/* 541 */           xfje -= tempYe;
/*     */         } else {
/* 543 */           xfje += ye;
/*     */         }
/* 545 */         int tempCs = csbs + ((Integer)csyxList.get(index)).intValue();
/*     */         
/* 547 */         index++;
/* 548 */         if (tempCs < 1) {
/* 549 */           return null;
/*     */         }
/*     */         Integer[] xfsz;
/* 552 */         if (tempCs == 1) {
/* 553 */           xfsz = new Integer[1];
/* 554 */           xfsz[0] = Integer.valueOf(xfje);
/*     */         } else {
/* 556 */           xfsz = RandomUtil.getRandomRu(xfje, tempCs, tempCs, 1);
/* 557 */           Arrays.sort(xfsz, Collections.reverseOrder());
/*     */         }
/* 559 */         for (int i = 0; i < xfsz.length; i++) {
/* 560 */           Ecard_chu chu = (Ecard_chu)UtilTool.createUIDEntity(Ecard_chu.class);
/* 561 */           chu.setEcard_bank(e.getEcard_bank());
/* 562 */           chu.setEcard_code(e.getEcard_code());
/* 563 */           chu.setEcard_key(e.getEcard_key());
/* 564 */           chu.setEcard_name(e.getEcard_name());
/* 565 */           chu.setChu_date(r.getRu_date());
/* 566 */           chu.setChu_je(Float.valueOf(xfsz[i].intValue()));
/* 567 */           chu.setChu_ym(ym);
/* 568 */           if (exist_dates.contains(DateUtil.DateToStr(r.getRu_date()))) {
/* 569 */             holidayFl += chu.getChu_je().floatValue();
/* 570 */             chu.setChu_fl("2");
/*     */           }
/* 572 */           chuList.add(chu);
/*     */         }
/*     */       } }
/* 575 */     Hashtable<String, Integer> ecsTable = new Hashtable();
/* 576 */     List<Ecard_chu> zdchuList = buildEcard_chu_zd(e, zdruList, ym, ecsTable);
/* 577 */     int zds = 0;
/* 578 */     for (Ecard_chu c : zdchuList) {
/* 579 */       int tempPecent = (int)(c.getChu_je().floatValue() / 100.0F);
/* 580 */       Random tempRan = new Random();
/* 581 */       int tempJe = tempPecent + tempRan.nextInt(tempPecent * 2);
/* 582 */       c.setChu_je(Float.valueOf(c.getChu_je().floatValue() - tempJe));
/* 583 */       zds += tempJe;
/*     */     }
/*     */     
/* 586 */     float fl = 0.55F;
/* 587 */     if ("中养".equals(e.getEcard_type())) {
/* 588 */       fl = 0.6F;
/* 589 */     } else if ("精养".equals(e.getEcard_type())) {
/* 590 */       fl = 0.65F;
/*     */     }
/* 592 */     if (cb != null) {
/* 593 */       fl = cb.floatValue();
/*     */     }
/* 595 */     fl = (float)(fl - holidayFl * 0.2D / e.getM_zonge().intValue());
/* 596 */     List<int[]> flSz = RandomUtil.getFl(fl);
/* 597 */     Collections.shuffle(flSz);
/* 598 */     int[] fls = (int[])flSz.get(0);
/* 599 */     int i_78 = e.getM_zonge().intValue() * fls[1] / 100;
/* 600 */     int i_125 = e.getM_zonge().intValue() * fls[2] / 100;
/* 601 */     System.out.println("i_78=" + i_78 + " i_125=" + i_125 + " z=" + (i_125 * 0.0125D + i_78 * 0.0078D + (e.getM_zonge().intValue() - i_125 - i_78) * 0.0038D));
/* 602 */     List<Ecard_chu> list38 = new ArrayList();
/* 603 */     list38.addAll(chuList);
/* 604 */     int cl_len = list38.size();
/* 605 */     List<Ecard_chu> list125 = new ArrayList();
/* 606 */     int geshu_125 = i_125 * cl_len / (e.getM_zonge().intValue() - zddze);
/* 607 */     float r = 0.0F;
/* 608 */     if (geshu_125 == 0) {
/* 609 */       r = i_125;
/*     */     } else {
/* 611 */       r = getFhflCon(list125, list38, (List)flTable.get("125"), i_125, geshu_125);
/*     */     }
/* 613 */     float temps = 0.0F;
/* 614 */     for (Ecard_chu c : list125) {
/* 615 */       temps += c.getChu_je().floatValue();
/*     */     }
/* 617 */     System.out.println("temps=" + temps);
/* 618 */     System.out.println("r=" + r);
/* 619 */     list38.removeAll(list125);
/* 620 */     i_78 += (int)r * 125 / 78;
/* 621 */     System.out.println("i_78=" + i_78 + " i_125=" + i_125 + " z=" + (i_125 * 0.0125D + i_78 * 0.0078D + (e.getM_zonge().intValue() - i_125 - i_78) * 0.0038D));
/* 622 */     List<Ecard_chu> list78 = new ArrayList();
/* 623 */     int geshu_78 = i_78 * cl_len / (e.getM_zonge().intValue() - zddze);
/* 624 */     if (geshu_78 == 0) {
/* 625 */       r = i_78;
/*     */     } else {
/* 627 */       r = getFhflCon(list78, list38, (List)flTable.get("78"), i_78, geshu_78);
/*     */     }
/* 629 */     System.out.println("last--r=" + r);
/* 630 */     if (Math.abs(r) > 40.0F) {
/* 631 */       return null;
/*     */     }
/* 633 */     list38.removeAll(list78);
/* 634 */     setChuFl(list125, (List)flTable.get("125"), 0.0125F, ecsTable);
/* 635 */     setChuFl(list78, (List)flTable.get("78"), 0.0078F, ecsTable);
/* 636 */     setChuFl(list38, (List)flTable.get("38"), 0.0038F, ecsTable);
/* 637 */     List<Ecard_chu> zbList = new ArrayList();
/* 638 */     List<Ecard_chu> sxList = new ArrayList();
/* 639 */     List<Ecard_chu> syList = new ArrayList();
/* 640 */     List<Integer> je_sz = new ArrayList();
/* 641 */     list38.addAll(list125);
/* 642 */     list38.addAll(list78);
/*     */     
/* 644 */     Ecard_chu lastChu = null;
/*     */     
/* 646 */     for (Ecard_chu c : list38)
/* 647 */       if ((c.getEpos() != null) && ("整百".equals(c.getEpos().getEpos_xiaoshu()))) {
/* 648 */         zbList.add(c);
/*     */       } else {
/* 650 */         if ((lastChu == null) || (lastChu.getChu_date().before(c.getChu_date()))) {
/* 651 */           lastChu = c;
/*     */         }
/* 653 */         if ((c.getEpos() != null) && ("小数".equals(c.getEpos().getEpos_xiaoshu()))) {
/* 654 */           sxList.add(c);
/* 655 */           je_sz.add(Integer.valueOf(Math.round(c.getChu_je().floatValue())));
/*     */         } else {
/* 657 */           syList.add(c);
/*     */         } }
/*     */     float[] f_sz;
/*     */     int j;
/* 661 */     if (je_sz.size() > 1) {
/* 662 */       f_sz = RandomUtil.getFloatSjs(je_sz);
/* 663 */       j = 0;
/* 664 */       for (Ecard_chu c : sxList) {
/* 665 */         c.setChu_je(Float.valueOf(f_sz[j]));
/* 666 */         j++;
/*     */       }
/*     */     }
/* 669 */     if ((lastChu == null) && (!zbList.isEmpty())) {
/* 670 */       return null;
/*     */     }
/* 672 */     float temp_ys = fixZhengBai(zbList);
/* 673 */     lastChu.setChu_je(Float.valueOf(lastChu.getChu_je().floatValue() + temp_ys + zds));
/* 674 */     syList.addAll(zdchuList);
/* 675 */     syList.addAll(sxList);
/* 676 */     syList.addAll(zbList);
/* 677 */     result.addAll(syList);
/* 678 */     return result;
/*     */   }
/*     */   
/*     */   private float fixZhengBai(List<Ecard_chu> chuList) {
/* 682 */     float ys = 0.0F;
/* 683 */     if (!chuList.isEmpty()) {
/* 684 */       float temp_ys = 0.0F;
/* 685 */       for (Ecard_chu c : chuList) {
/* 686 */         float f = c.getChu_je().floatValue() % 100.0F;
/* 687 */         c.setChu_je(Float.valueOf(c.getChu_je().floatValue() - f));
/* 688 */         temp_ys += f;
/*     */       }
/* 690 */       ys = temp_ys;
/*     */     }
/*     */     
/*     */ 
/* 694 */     return ys;
/*     */   }
/*     */   
/*     */   private List<Ecard_chu> setChuFl(List<Ecard_chu> chuList, List flList, float fl, Hashtable<String, Integer> ecsTable)
/*     */   {
/* 699 */     List<Ecard_chu> result = new ArrayList();
/* 700 */     int flLen = flList.size();
/* 701 */     int flIndex = 0;
/* 702 */     int flXunh = 0;
/* 703 */     boolean flFlag = false;
/* 704 */     for (Iterator i$ = chuList.iterator(); i$.hasNext();)
/*     */     {
/* 704 */       Ecard_chu c = (Ecard_chu)i$.next();
/*     */       
/* 706 */       if ("2".equals(c.getChu_fl())) {
/* 707 */         c.setChu_fl("" + SysUtil.round(fl + 0.002D, 4));
/*     */       } else {
/* 709 */         c.setChu_fl("" + fl);
/*     */       }
/* 711 */       flFlag = false;
/* 712 */       flXunh = 0;
/* 713 */       int tempI = 0;
/* 714 */       while ((!flFlag) && 
/* 715 */         (tempI != 4))
/*     */       {
/* 718 */         for (; 
/* 718 */             flIndex < flLen; flIndex++) {
/* 719 */           flXunh++;
/* 720 */           Epos epos = (Epos)flList.get(flIndex);
/* 721 */           if ((epos.getChu_down().floatValue() < 0.001D) && (epos.getChu_up().floatValue() < 0.001D)) {
/* 722 */             int temp_cishu = 0;
/* 723 */             if (ecsTable.containsKey(epos.getEpos_key())) {
/* 724 */               temp_cishu = ((Integer)ecsTable.get(epos.getEpos_key())).intValue();
/*     */             }
/* 726 */             temp_cishu += 1;
/* 727 */             ecsTable.remove(epos.getEpos_key());
/* 728 */             ecsTable.put(epos.getEpos_key(), Integer.valueOf(temp_cishu));
/* 729 */             if (temp_cishu <= epos.getEpos_cishu().intValue())
/*     */             {
/*     */ 
/* 732 */               c.setChu_item(epos.getEpos_item());
/* 733 */               c.setEpos_code(epos.getEpos_code());
/* 734 */               c.setEpos_name(epos.getEpos_name());
/* 735 */               c.setEpos(epos);
/* 736 */               flIndex++;
/* 737 */               flFlag = true;
/* 738 */               break;
/*     */             }
/* 740 */           } else if ((c.getChu_je().floatValue() >= epos.getChu_down().floatValue()) && (c.getChu_je().floatValue() <= epos.getChu_up().floatValue())) {
/* 741 */             int temp_cishu = 0;
/* 742 */             if (ecsTable.containsKey(epos.getEpos_key())) {
/* 743 */               temp_cishu = ((Integer)ecsTable.get(epos.getEpos_key())).intValue();
/*     */             }
/* 745 */             temp_cishu += 1;
/* 746 */             ecsTable.remove(epos.getEpos_key());
/* 747 */             ecsTable.put(epos.getEpos_key(), Integer.valueOf(temp_cishu));
/* 748 */             if (temp_cishu <= epos.getEpos_cishu().intValue())
/*     */             {
/*     */ 
/* 751 */               c.setChu_item(epos.getEpos_item());
/* 752 */               c.setEpos_code(epos.getEpos_code());
/* 753 */               c.setEpos_name(epos.getEpos_name());
/* 754 */               c.setEpos(epos);
/* 755 */               flIndex++;
/* 756 */               flFlag = true;
/* 757 */               break;
/*     */             }
/*     */           }
/* 760 */           else if (flXunh == flLen) {
/* 761 */             flFlag = true;
/* 762 */             break;
/*     */           }
/*     */         }
/* 765 */         if ((!flFlag) || (flIndex >= flLen)) {
/* 766 */           tempI++;
/* 767 */           flIndex = 0;
/*     */         }
/*     */       }
/*     */     }
/* 771 */     return result;
/*     */   }
/*     */   
/*     */   private float getFhflCon(List<Ecard_chu> result, List<Ecard_chu> chuList, List flList, float je, int geshu) {
/* 775 */     List<Ecard_chu> tempList = new ArrayList();
/* 776 */     for (Iterator i$ = chuList.iterator(); i$.hasNext();) { 
                Ecard_chu chu = (Ecard_chu)i$.next();
/* 777 */       for (Object f : flList) {
/* 778 */         Epos tempPos = (Epos)f;
/* 779 */         if ((chu.getChu_je().floatValue() >= tempPos.getChu_down().floatValue()) && (chu.getChu_je().floatValue() <= tempPos.getChu_up().floatValue())) {
/* 780 */           tempList.add(chu);
/* 781 */           break;
/*     */         }
/*     */       } }
/*     */     Ecard_chu chu;
/* 785 */     if (tempList.isEmpty()) {
/* 786 */       return -1000000.0F;
/*     */     }
/* 788 */     Collections.shuffle(tempList);
/* 789 */     int len = tempList.size();
/* 790 */     float minCy = 10000.0F;
/* 791 */     int[] indexSz = null;
/* 792 */     int[] minSz = null;
/* 793 */     boolean duoFlag = false;
/* 794 */     boolean shaoFlag = false;
/* 795 */     boolean hsFlag = false;
/* 796 */     float tje_min = 0.0F;
/* 797 */     System.out.println("len=" + len + " gs=" + geshu);
/* 798 */     List<int[]> list = RandomUtil.combination(len, geshu);
/* 799 */     for (int[] sz : list) {
/* 800 */       float tje = 0.0F;
/* 801 */       for (int j = 0; j < sz.length; j++) {
/* 802 */         tje += ((Ecard_chu)tempList.get(sz[j])).getChu_je().floatValue();
/*     */       }
/* 804 */       if (Math.abs(tje - je) < 20.0F) {
/* 805 */         tje_min = tje;
/* 806 */         hsFlag = true;
/* 807 */         indexSz = sz;
/* 808 */         break;
/*     */       }
/* 810 */       duoFlag = tje > je;
/* 811 */       shaoFlag = tje < je;
/* 812 */       if (Math.abs(tje - je) < minCy) {
/* 813 */         minCy = Math.abs(tje - je);
/* 814 */         minSz = sz;
/* 815 */         tje_min = tje;
/*     */       }
/*     */     }
/* 818 */     if (indexSz == null) {
/* 819 */       if ((duoFlag) && (geshu > 1)) {
/* 820 */         list = RandomUtil.combination(len, geshu - 1);
/* 821 */         for (int[] sz : list) {
/* 822 */           float tje = 0.0F;
/* 823 */           for (int j = 0; j < sz.length; j++) {
/* 824 */             tje += ((Ecard_chu)tempList.get(sz[j])).getChu_je().floatValue();
/*     */           }
/* 826 */           if (Math.abs(tje - je) < 20.0F) {
/* 827 */             tje_min = tje;
/* 828 */             indexSz = sz;
/* 829 */             hsFlag = true;
/* 830 */             break;
/*     */           }
/*     */         }
/*     */       }
/* 834 */       if ((!hsFlag) && (shaoFlag)) {
/* 835 */         list = RandomUtil.combination(len, geshu + 1);
/* 836 */         for (int[] sz : list) {
/* 837 */           float tje = 0.0F;
/* 838 */           for (int j = 0; j < sz.length; j++) {
/* 839 */             tje += ((Ecard_chu)tempList.get(sz[j])).getChu_je().floatValue();
/*     */           }
/* 841 */           if (Math.abs(tje - je) < 20.0F) {
/* 842 */             tje_min = tje;
/* 843 */             indexSz = sz;
/* 844 */             hsFlag = true;
/* 845 */             break;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 850 */     if (hsFlag) {
/* 851 */       if (indexSz != null) {
/* 852 */         for (int j = 0; j < indexSz.length; j++) {
/* 853 */           result.add(tempList.get(indexSz[j]));
/*     */         }
/*     */       }
/*     */     }
/* 857 */     else if (minSz != null) {
/* 858 */       for (int j = 0; j < minSz.length; j++) {
/* 859 */         result.add(tempList.get(minSz[j]));
/*     */       }
/*     */     }
/*     */     
/* 863 */     float ce = je - tje_min;
/* 864 */     return ce;
/*     */   }
/*     */ }
