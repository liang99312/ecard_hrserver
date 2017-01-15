/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import com.microsoft.sqlserver.jdbc.SQLServerException;
/*     */ import java.sql.BatchUpdateException;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.hibernate.SQLQuery;
/*     */ import org.hibernate.Session;
/*     */ import org.hibernate.Transaction;
/*     */ import org.hibernate.exception.GenericJDBCException;
/*     */ import org.hibernate.exception.SQLGrammarException;
/*     */ import org.jhrcore.entity.base.TempFieldInfo;
/*     */ import org.jhrcore.entity.salary.ValidateSQLResult;
/*     */ import org.jhrcore.server.HibernateUtil;
/*     */ import org.jhrcore.util.DbUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ImplUtil
/*     */ {
/*  26 */   private static Logger log = Logger.getLogger(ImplUtil.class);
/*     */   
/*     */   public static void exSQLs(Transaction tx, Session session, ValidateSQLResult validateSQLResult, String sqls, String split_char) {
/*  29 */     exSQLs(tx, session, validateSQLResult, sqls, split_char, true);
/*     */   }
/*     */   
/*     */   public static void exSQLs(Transaction tx, Session session, String sqls, String split_char) throws Exception {
/*  33 */     if (!sqls.equals("")) {
/*  34 */       String[] ex_str = sqls.split(split_char);
/*  35 */       for (String tmp : ex_str) {
/*  36 */         if (!"".equals(tmp.trim()))
/*     */         {
/*     */           try
/*     */           {
/*  40 */             session.createSQLQuery(tmp).executeUpdate();
/*  41 */             session.flush();
/*     */           } catch (Exception ex) {
/*  43 */             log.error("sql??д???:" + tmp + "\n" + getSQLExceptionMsg(ex));
/*  44 */             throw ex;
/*     */           } }
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   public static void exSQLs(Transaction tx, Session session, ValidateSQLResult validateSQLResult, String sqls, String split_char, boolean commit) {
/*  51 */     int result_records = 0;
/*  52 */     if ((!sqls.equals("")) && (validateSQLResult.getResult() == 0)) {
/*  53 */       String[] ex_str = sqls.split(split_char);
/*  54 */       for (String tmp : ex_str) {
/*  55 */         if (!"".equals(tmp.trim()))
/*     */         {
/*     */           try
/*     */           {
/*  59 */             result_records++;
/*  60 */             session.createSQLQuery(tmp).executeUpdate();
/*  61 */             session.flush();
/*     */           } catch (Exception e) {
/*  63 */             result_records--;
/*  64 */             validateSQLResult.setMsg(validateSQLResult.getMsg() + ";\n" + getSQLExceptionMsg(e));
/*  65 */             log.error("sql??д???:" + tmp + "\n" + validateSQLResult.getMsg());
/*  66 */             validateSQLResult.setResult(1);
/*  67 */             rollBack(tx);
/*  68 */             break;
/*     */           } }
/*     */       }
/*     */     }
/*     */     try {
/*  73 */       if (commit) {
/*  74 */         if (validateSQLResult.getResult() == 0) {
/*  75 */           tx.commit();
/*     */         } else {
/*  77 */           rollBack(tx);
/*     */         }
/*     */       }
/*     */     } catch (Exception e) {
/*  81 */       validateSQLResult.setMsg(validateSQLResult.getMsg() + ";\n" + getSQLExceptionMsg(e));
/*  82 */       log.error("sql:" + sqls + "\n" + validateSQLResult.getMsg());
/*  83 */       validateSQLResult.setResult(1);
/*  84 */       rollBack(tx);
/*     */     } finally {
/*  86 */       validateSQLResult.setUpdate_result(result_records);
/*     */     }
/*     */   }
/*     */   
/*     */   /* Error */
/*     */   public static void exSQLs_jdbc(Session session, ValidateSQLResult validateSQLResult, String ex_sql, String split_char, boolean allow_error)
/*     */   {
/*     */     // Byte code:
/*     */     //   0: aload_2
/*     */     //   1: ifnull +397 -> 398
/*     */     //   4: aload_2
/*     */     //   5: ldc 29
/*     */     //   7: ldc 3
/*     */     //   9: invokevirtual 30	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
/*     */     //   12: ldc 3
/*     */     //   14: invokevirtual 4	java/lang/String:equals	(Ljava/lang/Object;)Z
/*     */     //   17: ifne +381 -> 398
/*     */     //   20: aload_2
/*     */     //   21: aload_3
/*     */     //   22: invokevirtual 5	java/lang/String:split	(Ljava/lang/String;)[Ljava/lang/String;
/*     */     //   25: astore 5
/*     */     //   27: aload_0
/*     */     //   28: invokeinterface 31 1 0
/*     */     //   33: astore 6
/*     */     //   35: new 12	java/lang/StringBuilder
/*     */     //   38: dup
/*     */     //   39: invokespecial 13	java/lang/StringBuilder:<init>	()V
/*     */     //   42: astore 7
/*     */     //   44: aconst_null
/*     */     //   45: astore 8
/*     */     //   47: iconst_0
/*     */     //   48: istore 9
/*     */     //   50: aload 5
/*     */     //   52: astore 10
/*     */     //   54: aload 10
/*     */     //   56: arraylength
/*     */     //   57: istore 11
/*     */     //   59: iconst_0
/*     */     //   60: istore 12
/*     */     //   62: iload 12
/*     */     //   64: iload 11
/*     */     //   66: if_icmpge +255 -> 321
/*     */     //   69: aload 10
/*     */     //   71: iload 12
/*     */     //   73: aaload
/*     */     //   74: astore 13
/*     */     //   76: aload 13
/*     */     //   78: invokevirtual 6	java/lang/String:trim	()Ljava/lang/String;
/*     */     //   81: ldc 3
/*     */     //   83: invokevirtual 4	java/lang/String:equals	(Ljava/lang/Object;)Z
/*     */     //   86: ifeq +6 -> 92
/*     */     //   89: goto +226 -> 315
/*     */     //   92: aload 6
/*     */     //   94: invokeinterface 32 1 0
/*     */     //   99: astore 8
/*     */     //   101: aload 8
/*     */     //   103: aload 13
/*     */     //   105: invokeinterface 33 2 0
/*     */     //   110: pop
/*     */     //   111: aload_1
/*     */     //   112: aload_1
/*     */     //   113: invokevirtual 34	org/jhrcore/entity/salary/ValidateSQLResult:getUpdate_result	()I
/*     */     //   116: iconst_1
/*     */     //   117: iadd
/*     */     //   118: invokevirtual 27	org/jhrcore/entity/salary/ValidateSQLResult:setUpdate_result	(I)V
/*     */     //   121: aload 8
/*     */     //   123: ifnull +10 -> 133
/*     */     //   126: aload 8
/*     */     //   128: invokeinterface 35 1 0
/*     */     //   133: goto +182 -> 315
/*     */     //   136: astore 14
/*     */     //   138: getstatic 11	org/jhrcore/server/util/ImplUtil:log	Lorg/apache/log4j/Logger;
/*     */     //   141: aload 14
/*     */     //   143: invokevirtual 19	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
/*     */     //   146: goto +169 -> 315
/*     */     //   149: astore 14
/*     */     //   151: getstatic 11	org/jhrcore/server/util/ImplUtil:log	Lorg/apache/log4j/Logger;
/*     */     //   154: aload 14
/*     */     //   156: invokevirtual 19	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
/*     */     //   159: aload 7
/*     */     //   161: aload 13
/*     */     //   163: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   166: ldc 36
/*     */     //   168: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   171: pop
/*     */     //   172: aload 7
/*     */     //   174: aload 14
/*     */     //   176: invokestatic 17	org/jhrcore/server/util/ImplUtil:getSQLExceptionMsg	(Ljava/lang/Exception;)Ljava/lang/String;
/*     */     //   179: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   182: pop
/*     */     //   183: aload 7
/*     */     //   185: ldc 22
/*     */     //   187: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   190: pop
/*     */     //   191: aload_1
/*     */     //   192: aload_1
/*     */     //   193: invokevirtual 37	org/jhrcore/entity/salary/ValidateSQLResult:getError_result	()I
/*     */     //   196: iconst_1
/*     */     //   197: iadd
/*     */     //   198: invokevirtual 38	org/jhrcore/entity/salary/ValidateSQLResult:setError_result	(I)V
/*     */     //   201: aload 8
/*     */     //   203: invokeinterface 39 1 0
/*     */     //   208: goto +13 -> 221
/*     */     //   211: astore 15
/*     */     //   213: getstatic 11	org/jhrcore/server/util/ImplUtil:log	Lorg/apache/log4j/Logger;
/*     */     //   216: aload 15
/*     */     //   218: invokevirtual 19	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
/*     */     //   221: iload 4
/*     */     //   223: ifeq +31 -> 254
/*     */     //   226: aload 8
/*     */     //   228: ifnull +10 -> 238
/*     */     //   231: aload 8
/*     */     //   233: invokeinterface 35 1 0
/*     */     //   238: goto +77 -> 315
/*     */     //   241: astore 15
/*     */     //   243: getstatic 11	org/jhrcore/server/util/ImplUtil:log	Lorg/apache/log4j/Logger;
/*     */     //   246: aload 15
/*     */     //   248: invokevirtual 19	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
/*     */     //   251: goto +64 -> 315
/*     */     //   254: iconst_1
/*     */     //   255: istore 9
/*     */     //   257: aload 8
/*     */     //   259: ifnull +10 -> 269
/*     */     //   262: aload 8
/*     */     //   264: invokeinterface 35 1 0
/*     */     //   269: goto +52 -> 321
/*     */     //   272: astore 15
/*     */     //   274: getstatic 11	org/jhrcore/server/util/ImplUtil:log	Lorg/apache/log4j/Logger;
/*     */     //   277: aload 15
/*     */     //   279: invokevirtual 19	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
/*     */     //   282: goto +39 -> 321
/*     */     //   285: astore 16
/*     */     //   287: aload 8
/*     */     //   289: ifnull +10 -> 299
/*     */     //   292: aload 8
/*     */     //   294: invokeinterface 35 1 0
/*     */     //   299: goto +13 -> 312
/*     */     //   302: astore 17
/*     */     //   304: getstatic 11	org/jhrcore/server/util/ImplUtil:log	Lorg/apache/log4j/Logger;
/*     */     //   307: aload 17
/*     */     //   309: invokevirtual 19	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
/*     */     //   312: aload 16
/*     */     //   314: athrow
/*     */     //   315: iinc 12 1
/*     */     //   318: goto -256 -> 62
/*     */     //   321: aload_1
/*     */     //   322: new 12	java/lang/StringBuilder
/*     */     //   325: dup
/*     */     //   326: invokespecial 13	java/lang/StringBuilder:<init>	()V
/*     */     //   329: aload_1
/*     */     //   330: invokevirtual 21	org/jhrcore/entity/salary/ValidateSQLResult:getMsg	()Ljava/lang/String;
/*     */     //   333: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   336: aload 7
/*     */     //   338: invokevirtual 18	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */     //   341: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   344: invokevirtual 18	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */     //   347: invokevirtual 23	org/jhrcore/entity/salary/ValidateSQLResult:setMsg	(Ljava/lang/String;)V
/*     */     //   350: iload 9
/*     */     //   352: ifeq +18 -> 370
/*     */     //   355: iload 4
/*     */     //   357: ifne +13 -> 370
/*     */     //   360: aload 6
/*     */     //   362: invokeinterface 40 1 0
/*     */     //   367: goto +10 -> 377
/*     */     //   370: aload 6
/*     */     //   372: invokeinterface 41 1 0
/*     */     //   377: goto +21 -> 398
/*     */     //   380: astore 10
/*     */     //   382: getstatic 11	org/jhrcore/server/util/ImplUtil:log	Lorg/apache/log4j/Logger;
/*     */     //   385: aload 10
/*     */     //   387: invokevirtual 19	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
/*     */     //   390: goto +8 -> 398
/*     */     //   393: astore 18
/*     */     //   395: aload 18
/*     */     //   397: athrow
/*     */     //   398: return
/*     */     // Line number table:
/*     */     //   Java source line #91	-> byte code offset #0
/*     */     //   Java source line #92	-> byte code offset #20
/*     */     //   Java source line #93	-> byte code offset #27
/*     */     //   Java source line #94	-> byte code offset #35
/*     */     //   Java source line #95	-> byte code offset #44
/*     */     //   Java source line #96	-> byte code offset #47
/*     */     //   Java source line #97	-> byte code offset #50
/*     */     //   Java source line #98	-> byte code offset #76
/*     */     //   Java source line #99	-> byte code offset #89
/*     */     //   Java source line #102	-> byte code offset #92
/*     */     //   Java source line #103	-> byte code offset #101
/*     */     //   Java source line #104	-> byte code offset #111
/*     */     //   Java source line #123	-> byte code offset #121
/*     */     //   Java source line #124	-> byte code offset #126
/*     */     //   Java source line #128	-> byte code offset #133
/*     */     //   Java source line #126	-> byte code offset #136
/*     */     //   Java source line #127	-> byte code offset #138
/*     */     //   Java source line #129	-> byte code offset #146
/*     */     //   Java source line #105	-> byte code offset #149
/*     */     //   Java source line #106	-> byte code offset #151
/*     */     //   Java source line #107	-> byte code offset #159
/*     */     //   Java source line #108	-> byte code offset #172
/*     */     //   Java source line #109	-> byte code offset #183
/*     */     //   Java source line #110	-> byte code offset #191
/*     */     //   Java source line #112	-> byte code offset #201
/*     */     //   Java source line #115	-> byte code offset #208
/*     */     //   Java source line #113	-> byte code offset #211
/*     */     //   Java source line #114	-> byte code offset #213
/*     */     //   Java source line #116	-> byte code offset #221
/*     */     //   Java source line #123	-> byte code offset #226
/*     */     //   Java source line #124	-> byte code offset #231
/*     */     //   Java source line #128	-> byte code offset #238
/*     */     //   Java source line #126	-> byte code offset #241
/*     */     //   Java source line #127	-> byte code offset #243
/*     */     //   Java source line #128	-> byte code offset #251
/*     */     //   Java source line #119	-> byte code offset #254
/*     */     //   Java source line #123	-> byte code offset #257
/*     */     //   Java source line #124	-> byte code offset #262
/*     */     //   Java source line #128	-> byte code offset #269
/*     */     //   Java source line #126	-> byte code offset #272
/*     */     //   Java source line #127	-> byte code offset #274
/*     */     //   Java source line #128	-> byte code offset #282
/*     */     //   Java source line #122	-> byte code offset #285
/*     */     //   Java source line #123	-> byte code offset #287
/*     */     //   Java source line #124	-> byte code offset #292
/*     */     //   Java source line #128	-> byte code offset #299
/*     */     //   Java source line #126	-> byte code offset #302
/*     */     //   Java source line #127	-> byte code offset #304
/*     */     //   Java source line #128	-> byte code offset #312
/*     */     //   Java source line #97	-> byte code offset #315
/*     */     //   Java source line #131	-> byte code offset #321
/*     */     //   Java source line #133	-> byte code offset #350
/*     */     //   Java source line #134	-> byte code offset #360
/*     */     //   Java source line #136	-> byte code offset #370
/*     */     //   Java source line #141	-> byte code offset #377
/*     */     //   Java source line #138	-> byte code offset #380
/*     */     //   Java source line #139	-> byte code offset #382
/*     */     //   Java source line #141	-> byte code offset #390
/*     */     //   Java source line #140	-> byte code offset #393
/*     */     //   Java source line #143	-> byte code offset #398
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	signature
/*     */     //   0	399	0	session	Session
/*     */     //   0	399	1	validateSQLResult	ValidateSQLResult
/*     */     //   0	399	2	ex_sql	String
/*     */     //   0	399	3	split_char	String
/*     */     //   0	399	4	allow_error	boolean
/*     */     //   25	26	5	sqls	String[]
/*     */     //   33	338	6	c	java.sql.Connection
/*     */     //   42	295	7	msg	StringBuilder
/*     */     //   45	248	8	stmt	java.sql.Statement
/*     */     //   48	303	9	error	boolean
/*     */     //   52	18	10	arr$	String[]
/*     */     //   380	6	10	ex	Exception
/*     */     //   57	8	11	len$	int
/*     */     //   60	256	12	i$	int
/*     */     //   74	88	13	tmp	String
/*     */     //   136	6	14	e	Exception
/*     */     //   149	26	14	e	Exception
/*     */     //   211	6	15	e1	Exception
/*     */     //   241	6	15	e	Exception
/*     */     //   272	6	15	e	Exception
/*     */     //   285	28	16	localObject1	Object
/*     */     //   302	6	17	e	Exception
/*     */     //   393	3	18	localObject2	Object
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   121	133	136	java/lang/Exception
/*     */     //   92	121	149	java/lang/Exception
/*     */     //   201	208	211	java/lang/Exception
/*     */     //   226	238	241	java/lang/Exception
/*     */     //   257	269	272	java/lang/Exception
/*     */     //   92	121	285	finally
/*     */     //   149	226	285	finally
/*     */     //   254	257	285	finally
/*     */     //   285	287	285	finally
/*     */     //   287	299	302	java/lang/Exception
/*     */     //   350	377	380	java/lang/Exception
/*     */     //   350	377	393	finally
/*     */     //   380	390	393	finally
/*     */     //   393	395	393	finally
/*     */   }
/*     */   
/*     */   public static void rollBack(Transaction tx)
/*     */   {
/*     */     try
/*     */     {
/* 147 */       tx.rollback();
/*     */     } catch (Exception e) {
/* 149 */       log.error(e);
/*     */     }
/*     */   }
/*     */   
/*     */   public static String rebuildTable(String table_name, List<TempFieldInfo> fields)
/*     */   {
/* 155 */     if (fields.isEmpty()) {
/* 156 */       return "";
/*     */     }
/* 158 */     String build_t_sql = "";
/* 159 */     if (HibernateUtil.getDb_type().equalsIgnoreCase("sqlserver")) {
/* 160 */       for (Object obj : fields) {
/* 161 */         TempFieldInfo tfi = (TempFieldInfo)obj;
/* 162 */         tfi.setField_name(tfi.getField_name().replace("_code_", ""));
/* 163 */         build_t_sql = build_t_sql + "if not exists(select * from syscolumns where id=object_id('" + table_name + "') and name='" + tfi.getField_name() + "')\n";
/* 164 */         if (tfi.getField_type().equalsIgnoreCase("boolean")) {
/* 165 */           build_t_sql = build_t_sql + "    alter table " + table_name + " add " + tfi.getField_name() + " tinyint \n";
/* 166 */         } else if (tfi.getField_type().equalsIgnoreCase("date")) {
/* 167 */           build_t_sql = build_t_sql + "    alter table " + table_name + " add " + tfi.getField_name() + " datetime \n";
/* 168 */         } else if (tfi.getField_type().equalsIgnoreCase("Float")) {
/* 169 */           build_t_sql = build_t_sql + "    alter table " + table_name + " add " + tfi.getField_name() + " float \n";
/* 170 */         } else if (tfi.getField_type().equalsIgnoreCase("Integer")) {
/* 171 */           build_t_sql = build_t_sql + "    alter table " + table_name + " add " + tfi.getField_name() + " int \n";
/*     */         } else {
/* 173 */           build_t_sql = build_t_sql + "    alter table " + table_name + " add " + tfi.getField_name() + " varchar(255) \n";
/*     */         }
/*     */       }
/*     */     } else {
/* 177 */       build_t_sql = "declare \n cnt number;\n begin\n";
/*     */       
/*     */ 
/* 180 */       for (Object obj : fields) {
/* 181 */         TempFieldInfo tfi = (TempFieldInfo)obj;
/* 182 */         tfi.setField_name(tfi.getField_name().replace("_code_", ""));
/* 183 */         build_t_sql = build_t_sql + " SELECT count(1) into cnt from user_tab_columns where column_name = '" + tfi.getField_name().toUpperCase() + "' and table_name = '" + table_name.toUpperCase() + "';\n";
/* 184 */         build_t_sql = build_t_sql + " if cnt = 0 then begin \n";
/* 185 */         build_t_sql = build_t_sql + " execute immediate";
/* 186 */         if (tfi.getField_type().equalsIgnoreCase("boolean")) {
/* 187 */           build_t_sql = build_t_sql + " 'alter table " + table_name.toUpperCase() + " add " + tfi.getField_name().toUpperCase() + " NUMBER(10)'; \n";
/* 188 */         } else if (tfi.getField_type().equalsIgnoreCase("date")) {
/* 189 */           build_t_sql = build_t_sql + " 'alter table " + table_name.toUpperCase() + " add " + tfi.getField_name().toUpperCase() + " date'; \n";
/* 190 */         } else if (tfi.getField_type().equalsIgnoreCase("Float")) {
/* 191 */           build_t_sql = build_t_sql + " 'alter table " + table_name.toUpperCase() + " add " + tfi.getField_name().toUpperCase() + " float'; \n";
/* 192 */         } else if (tfi.getField_type().equalsIgnoreCase("Integer")) {
/* 193 */           build_t_sql = build_t_sql + " 'alter table " + table_name.toUpperCase() + " add " + tfi.getField_name().toUpperCase() + " NUMBER(10)'; \n";
/*     */         } else {
/* 195 */           build_t_sql = build_t_sql + " 'alter table " + table_name.toUpperCase() + " add " + tfi.getField_name().toUpperCase() + " varchar2(255)'; \n";
/*     */         }
/* 197 */         build_t_sql = build_t_sql + " commit;\n end; \n end if;\n";
/*     */       }
/* 199 */       build_t_sql = build_t_sql + " end;";
/*     */     }
/* 201 */     return build_t_sql;
/*     */   }
/*     */   
/*     */   public static String getSQLExceptionMsg(Exception e) {
/* 205 */     if (e == null) {
/* 206 */       return "";
/*     */     }
/* 208 */     if ((e instanceof SQLGrammarException)) {
/* 209 */       SQLGrammarException e1 = (SQLGrammarException)e;
/* 210 */       return e1.getSQLException().getMessage(); }
/* 211 */     if ((e instanceof GenericJDBCException)) {
/* 212 */       GenericJDBCException e1 = (GenericJDBCException)e;
/* 213 */       return e1.getSQLException().getMessage(); }
/* 214 */     if ((e.getCause() instanceof BatchUpdateException)) {
/* 215 */       BatchUpdateException e1 = (BatchUpdateException)e.getCause();
/* 216 */       return e1.getMessage(); }
/* 217 */     if ((e.getCause() instanceof SQLException))
/* 218 */       return ((SQLException)e.getCause()).getMessage();
/* 219 */     if ((e.getCause() instanceof SQLServerException)) {
/* 220 */       return ((SQLServerException)e.getCause()).getMessage();
/*     */     }
/* 222 */     return e.toString();
/*     */   }
/*     */   
/*     */   public static void dropTempTable(Session s, String tableName)
/*     */   {
/*     */     try {
/* 228 */       s.createSQLQuery("drop table " + tableName).executeUpdate();
/*     */     } catch (Exception ex) {
/* 230 */       log.error("sql??д???:drop table " + tableName + "\n" + getSQLExceptionMsg(ex));
/*     */     }
/*     */   }
/*     */   
/*     */   public static void dropTempTable(String tempTable) {
/* 235 */     HibernateUtil.excuteSQL("drop table " + tempTable);
/*     */   }
/*     */   
/*     */   public static void initTempTable(String tableName, String db_type) throws Exception {
/* 239 */     String sql = DbUtil.isTableExistSQL(tableName, db_type);
/* 240 */     int cal_table_exists = Integer.valueOf(HibernateUtil.selectSQL(sql, false, 0).get(0).toString()).intValue();
/* 241 */     if (cal_table_exists != 0) {
/* 242 */       HibernateUtil.excuteSQL("drop table " + tableName);
/*     */     }
/*     */   }
/*     */ }
