/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Random;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RandomUtil
/*     */ {
/*     */   public static int[] getRandomResult0(int sum, int fqs, int rqs, int sjs, int bs)
/*     */   {
/*  24 */     int[] fq_sz = new int[fqs];
/*  25 */     int[] rq_sz = new int[rqs];
/*  26 */     int tmp_s = 0;
/*  27 */     int min_value = sum + 10000;
/*  28 */     int min_index = -1;
/*  29 */     Random ram = new Random();
/*  30 */     for (int i = 0; i < fqs; i++) {
/*  31 */       int r = 0;
/*     */       for (;;) {
/*  33 */         r = ram.nextInt(sjs);
/*  34 */         if (tmp_s + r < sum) {
/*     */           break;
/*     */         }
/*     */       }
/*  38 */       tmp_s += r;
/*  39 */       fq_sz[i] = r;
/*  40 */       if (r < min_value) {
/*  41 */         min_value = r;
/*  42 */         min_index = i;
/*     */       }
/*     */     }
/*  45 */     int sx = sum - tmp_s;
/*  46 */     int ty = sx / fqs;
/*  47 */     int ys = sx % fqs;
/*  48 */     for (int i = 0; i < fqs; i++) {
/*  49 */       if (ty == 0) {
/*  50 */         if ((fq_sz[i] + 1 <= sjs) && (sx > 0)) {
/*  51 */           fq_sz[i] += 1;
/*  52 */           sx -= 1;
/*  53 */           ys -= 1;
/*     */         }
/*     */       }
/*  56 */       else if (fq_sz[i] + ty <= sjs) {
/*  57 */         fq_sz[i] += ty;
/*     */       } else {
/*  59 */         ys += ty;
/*     */       }
/*     */     }
/*     */     
/*  63 */     fq_sz[min_index] += ys;
/*     */     
/*  65 */     List list = new ArrayList();
/*  66 */     for (int i = 0; i < rqs; i++) {
/*  67 */       list.add(Integer.valueOf(i));
/*     */     }
/*  69 */     Collections.shuffle(list);
/*  70 */     for (int i = 0; i < fqs; i++) {
/*  71 */       int v = fq_sz[i];
/*  72 */       int j = ((Integer)list.get(i)).intValue();
/*  73 */       rq_sz[j] = (v * bs);
/*     */     }
/*  75 */     return rq_sz;
/*     */   }
/*     */   
/*     */   public static Integer[] getRandomRu(int zje, int fqs, int rqs, int flag) {
/*  79 */     int[] result = new int[fqs];
/*  80 */     int pjs = zje / fqs;
/*  81 */     int jbs = (int)(pjs * 0.7D);
/*  82 */     int tempSum = 0;
/*  83 */     int ds = fqs / 2;
/*  84 */     for (int i = 0; i < ds; i++) {
/*  85 */       Random r = new Random();
/*  86 */       int sjs = r.nextInt(pjs - jbs);
/*  87 */       int r1 = jbs + sjs;
/*  88 */       int r2 = pjs * 2 - r1;
/*  89 */       r1 -= r1 % flag;
/*  90 */       r2 -= r2 % flag;
/*  91 */       result[(i * 2)] = r1;
/*  92 */       result[(i * 2 + 1)] = r2;
/*  93 */       tempSum = tempSum + r1 + r2;
/*     */     }
/*  95 */     result[(fqs - 1)] = (result[(fqs - 1)] + zje - tempSum);
/*  96 */     List list = new ArrayList();
/*  97 */     for (int i = 0; i < rqs; i++) {
/*  98 */       list.add(Integer.valueOf(i));
/*     */     }
/* 100 */     Collections.shuffle(list);
/* 101 */     Integer[] re = new Integer[rqs];
/* 102 */     for (int i = 0; i < rqs; i++) {
/* 103 */       re[i] = Integer.valueOf(0);
/*     */     }
/* 105 */     for (int i = 0; i < fqs; i++) {
/* 106 */       int j = ((Integer)list.get(i)).intValue();
/* 107 */       re[j] = Integer.valueOf(result[i]);
/*     */     }
/* 109 */     return re;
/*     */   }
/*     */   
/*     */   public static int[] getRandomResult(int sum, int fqs, int rqs, int sjs, int sjs_xx, int bs) {
/* 113 */     int[] fq_sz = new int[fqs];
/* 114 */     int[] rq_sz = new int[rqs];
/* 115 */     int tmp_s = 0;
/* 116 */     sum -= sjs_xx * fqs;
/* 117 */     sjs -= sjs_xx;
/* 118 */     int min_value = sum + 10000;
/* 119 */     int min_index = -1;
/* 120 */     Random ram = new Random();
/* 121 */     for (int i = 0; i < fqs; i++) {
/* 122 */       int r = 0;
/* 123 */       while ((r == 0) || (r + tmp_s > sum - fqs)) {
/* 124 */         if (tmp_s >= sum - fqs - 1) {
/* 125 */           r = 0;
/* 126 */           break;
/*     */         }
/* 128 */         if (sum - fqs - tmp_s < sjs) {
/* 129 */           r = ram.nextInt(sum - fqs - tmp_s);
/*     */         } else {
/* 131 */           r = ram.nextInt(sjs);
/*     */         }
/*     */       }
/* 134 */       tmp_s += r;
/* 135 */       fq_sz[i] = r;
/* 136 */       if (r < min_value) {
/* 137 */         min_value = r;
/* 138 */         min_index = i;
/*     */       }
/*     */     }
/* 141 */     int sx = sum - tmp_s;
/* 142 */     int ty = sx / fqs;
/* 143 */     int ys = sx % fqs;
/* 144 */     for (int i = 0; i < fqs; i++) {
/* 145 */       if (fq_sz[i] + ty <= sjs) {
/* 146 */         fq_sz[i] += ty;
/*     */       } else {
/* 148 */         ys += ty;
/*     */       }
/*     */     }
/* 151 */     fq_sz[min_index] += ys;
/* 152 */     for (int i = 0; i < fqs; i++) {
/* 153 */       fq_sz[i] += sjs_xx;
/*     */     }
/*     */     
/* 156 */     List list = new ArrayList();
/* 157 */     for (int i = 0; i < rqs; i++) {
/* 158 */       list.add(Integer.valueOf(i));
/*     */     }
/* 160 */     Collections.shuffle(list);
/* 161 */     for (int i = 0; i < fqs; i++) {
/* 162 */       int v = fq_sz[i];
/* 163 */       int j = ((Integer)list.get(i)).intValue();
/* 164 */       rq_sz[j] = (v * bs);
/*     */     }
/* 166 */     return rq_sz;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static List getRandomResult_zc(int sum, int fqs, int rqs, int sjs, int bs, int f_78)
/*     */   {
/* 177 */     int[] fq_sz = new int[fqs];
/* 178 */     int[] rq_sz = new int[rqs];
/* 179 */     List f78List = new ArrayList();
/* 180 */     List f78List_rq = new ArrayList();
/* 181 */     int tmp_s = 0;
/* 182 */     int min_value = sum;
/* 183 */     int min_index = -1;
/* 184 */     Random ram = new Random();
/* 185 */     for (int i = 0; i < fqs; i++) {
/* 186 */       int r = 0;
/* 187 */       while ((r == 0) || (r + tmp_s > sum - fqs)) {
/* 188 */         if (tmp_s >= sum - fqs - 1) {
/* 189 */           r = 0;
/* 190 */           break;
/*     */         }
/* 192 */         if (sum - fqs - tmp_s < sjs) {
/* 193 */           r = ram.nextInt(sum - fqs - tmp_s);
/*     */         } else {
/* 195 */           r = ram.nextInt(sjs);
/*     */         }
/*     */       }
/* 198 */       tmp_s += r;
/* 199 */       fq_sz[i] = r;
/* 200 */       if (r < min_value) {
/* 201 */         min_value = r;
/* 202 */         min_index = i;
/*     */       }
/*     */     }
/* 205 */     int sx = sum - tmp_s;
/* 206 */     int ty = sx / fqs;
/* 207 */     int ys = sx % fqs;
/* 208 */     for (int i = 0; i < fqs; i++) {
/* 209 */       if (fq_sz[i] + ty <= sjs) {
/* 210 */         fq_sz[i] += ty;
/*     */       } else {
/* 212 */         ys += ty;
/*     */       }
/*     */     }
/* 215 */     fq_sz[min_index] += ys;
/*     */     
/* 217 */     tmp_s = 0;
/* 218 */     int tmp_index = 0;
/* 219 */     for (int i = 0; i < fqs; i++) {
/* 220 */       if (tmp_s > f_78) {
/*     */         break;
/*     */       }
/* 223 */       tmp_index = i;
/* 224 */       tmp_s += fq_sz[i];
/* 225 */       f78List.add(Integer.valueOf(i));
/*     */     }
/* 227 */     if (tmp_s - f_78 > 0) {
/* 228 */       ys = tmp_s - f_78;
/* 229 */       min_index = -1;
/* 230 */       min_value = sum;
/* 231 */       for (int i = tmp_index + 1; i < fqs; i++) {
/* 232 */         if (fq_sz[i] < min_value) {
/* 233 */           min_value = fq_sz[i];
/* 234 */           min_index = i;
/*     */         }
/*     */       }
/* 237 */       fq_sz[tmp_index] -= ys;
/* 238 */       fq_sz[min_index] += ys;
/*     */     }
/*     */     
/* 241 */     List list = new ArrayList();
/* 242 */     for (int i = 0; i < rqs; i++) {
/* 243 */       list.add(Integer.valueOf(i));
/*     */     }
/* 245 */     Collections.shuffle(list);
/* 246 */     for (int i = 0; i < fqs; i++) {
/* 247 */       int v = fq_sz[i];
/* 248 */       int j = ((Integer)list.get(i)).intValue();
/* 249 */       rq_sz[j] = (v * bs);
/* 250 */       if (f78List.contains(Integer.valueOf(i))) {
/* 251 */         f78List_rq.add(Integer.valueOf(j));
/*     */       }
/*     */     }
/* 254 */     List l = new ArrayList();
/* 255 */     l.add(rq_sz);
/* 256 */     l.add(f78List_rq);
/* 257 */     return l;
/*     */   }
/*     */   
/*     */   public static float[] getFloatSjs(List szList) {
/* 261 */     int[] sz = new int[szList.size()];
/* 262 */     for (int i = 0; i < szList.size(); i++) {
/* 263 */       Integer it = (Integer)szList.get(i);
/* 264 */       sz[i] = it.intValue();
/*     */     }
/* 266 */     int fps = 0;
/* 267 */     float[] result = new float[sz.length];
/* 268 */     for (int i = 0; i < sz.length; i++) {
/* 269 */       int v = sz[i];
/* 270 */       int r = 1;
/* 271 */       sz[i] = (v - r);
/* 272 */       fps += r;
/*     */     }
/*     */     
/* 275 */     int[] rqs = getRandomResult(fps * 10, sz.length, sz.length, fps * 6, 0, 1);
/* 276 */     Arrays.sort(rqs);
/*     */     
/* 278 */     int[] t = new int[sz.length];
/* 279 */     System.arraycopy(sz, 0, t, 0, sz.length);
/* 280 */     Arrays.sort(t);
/* 281 */     List xbList = new ArrayList();
/* 282 */     for (int i = 0; i < sz.length; i++) {
/* 283 */       for (int j = 0; j < t.length; j++) {
/* 284 */         if ((sz[j] == t[i]) && 
/* 285 */           (!xbList.contains(i + ""))) {
/* 286 */           xbList.add(i + "");
/* 287 */           break;
/*     */         }
/*     */       }
/*     */     }
/*     */     
/* 292 */     int xb = sz.length - 1;
/* 293 */     for (Object obj : xbList) {
/* 294 */       int i = Integer.parseInt(obj.toString());
/* 295 */       result[i] = (sz[i] + rqs[xb] / 10.0F);
/* 296 */       xb--;
/*     */     }
/* 298 */     return result;
/*     */   }
/*     */   
/*     */   public static List<int[]> getFl(float s) {
/* 302 */     List<int[]> result = new ArrayList();
/* 303 */     int minValue = 5;
/* 304 */     if (s < 0.46F) {
/* 305 */       minValue = 0;
/*     */     }
/* 307 */     for (int i = 1; i < 100; i++)
/* 308 */       if ((i <= 0) || (i >= minValue))
/*     */       {
/*     */ 
/* 311 */         for (int j = 1; j < 100; j++)
/* 312 */           if ((j <= 0) || (j >= minValue))
/*     */           {
/*     */ 
/* 315 */             if (j > 80) {
/*     */               break;
/*     */             }
/* 318 */             if (i + j <= 90)
/*     */             {
/*     */ 
/* 321 */               float t = (float)(i * 0.0125D + j * 0.0078D + (100 - i - j) * 0.0038D);
/* 322 */               if ((t > s) && (t < s + 0.001D)) {
/* 323 */                 int[] sz = new int[3];
/* 324 */                 sz[0] = (100 - i - j);
/* 325 */                 sz[1] = j;
/* 326 */                 sz[2] = i;
/* 327 */                 result.add(sz);
/*     */               }
/*     */             }
/*     */           } }
/* 331 */     List<int[]> hgResult = new ArrayList();
/* 332 */     for (int[] i : result) {
/* 333 */       if ((i[2] > 10) && (i[2] < 50) && (i[1] > 20) && (i[1] < 50)) {
/* 334 */         hgResult.add(i);
/*     */       }
/*     */     }
/* 337 */     if (!hgResult.isEmpty()) {
/* 338 */       return hgResult;
/*     */     }
/* 340 */     System.out.println("result_size=" + result.size());
/* 341 */     return result;
/*     */   }
/*     */   
/*     */   public static List<int[]> combination(int n, int r) {
/* 345 */     System.out.println("n=" + n + " r=" + r);
/* 346 */     List<int[]> result = new ArrayList();
/* 347 */     int i = 0;
/* 348 */     int[] a = new int[r];
/* 349 */     a[i] = 0;
/*     */     for (;;) {
/* 351 */       if (a[i] < n) {
/* 352 */         if (i == r - 1) {
/* 353 */           int[] aa = new int[r];
/* 354 */           System.arraycopy(a, 0, aa, 0, r);
/* 355 */           if (result.size() == 300000) {
/*     */             break;
/*     */           }
/* 358 */           result.add(aa);
/* 359 */           a[i] += 1;
/*     */         } else {
/* 361 */           i++;
/* 362 */           a[i] = (a[(i - 1)] + 1);
/*     */         }
/*     */       } else {
/* 365 */         if (i == 0) {
/*     */           break;
/*     */         }
/* 368 */         i--;
/* 369 */         a[i] += 1;
/*     */       }
/*     */     }
/* 372 */     return result;
/*     */   }
/*     */   
/*     */   public static void main(String[] args) throws ParseException
/*     */   {
/* 377 */     List<int[]> list = getFl(0.45F);
/* 378 */     for (int[] i : list) {
/* 379 */       for (int j : i) {
/* 380 */         System.out.print("  " + j);
/*     */       }
/* 382 */       System.out.println();
/*     */     }
/* 384 */     System.out.println(0.3851D);
/*     */     
/* 386 */     Integer[] r = getRandomRu(5000, 3, 5, 1);
/* 387 */     Integer[] arr$ = r;int len$ = arr$.length; for (int i$ = 0; i$ < len$; i$++) { int i = arr$[i$].intValue();
/* 388 */       System.out.print("  " + i);
/*     */     }
/*     */     
/* 391 */     SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
/* 392 */     Date d = f.parse("20160231");
/* 393 */     System.out.println();
/* 394 */     System.out.println(CommUtil.getCPUSerialId() + "  " + CommUtil.getMACAddress());
/* 395 */     System.out.println(f.format(d));
/*     */   }
/*     */ }
