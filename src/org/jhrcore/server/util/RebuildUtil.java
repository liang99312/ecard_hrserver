/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Field;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.tree.DefaultMutableTreeNode;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.jhrcore.entity.base.EntityClass;
/*     */ import org.jhrcore.entity.base.EntityDef;
/*     */ import org.jhrcore.entity.base.FieldDef;
/*     */ import org.jhrcore.entity.base.ModuleInfo;
/*     */ import org.jhrcore.util.DateUtil;
/*     */ import org.jhrcore.util.PublicUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RebuildUtil
/*     */ {
/*  31 */   private static Logger log = Logger.getLogger(RebuildUtil.class.getName());
/*     */   
/*     */   public static void generateSourceSQL(DefaultMutableTreeNode node, String path) {
/*  34 */     Object oo = node.getUserObject();
/*  35 */     List list = new ArrayList();
/*  36 */     if ((oo instanceof ModuleInfo)) {
/*  37 */       ModuleInfo mi = (ModuleInfo)oo;
/*  38 */       list.add(mi);
/*  39 */       for (int k = 0; k < mi.getEntityClasss().size(); k++) {
/*  40 */         EntityClass et = (EntityClass)mi.getEntityClasss().toArray()[k];
/*  41 */         list.add(et);
/*  42 */         for (int i = 0; i < et.getEntityDefs().size(); i++) {
/*  43 */           EntityDef ed = (EntityDef)et.getEntityDefs().toArray()[i];
/*  44 */           list.add(ed);
/*  45 */           if (ed.getFieldDefs() != null) {
/*  46 */             for (int j = 0; j < ed.getFieldDefs().size(); j++) {
/*  47 */               FieldDef fd = (FieldDef)ed.getFieldDefs().toArray()[j];
/*  48 */               list.add(fd);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*  53 */     } else if ((oo instanceof EntityClass)) {
/*  54 */       EntityClass et = (EntityClass)oo;
/*  55 */       list.add(et);
/*  56 */       for (int i = 0; i < et.getEntityDefs().size(); i++) {
/*  57 */         EntityDef ed = (EntityDef)et.getEntityDefs().toArray()[i];
/*  58 */         list.add(ed);
/*  59 */         if (ed.getFieldDefs() != null) {
/*  60 */           for (int j = 0; j < ed.getFieldDefs().size(); j++) {
/*  61 */             FieldDef fd = (FieldDef)ed.getFieldDefs().toArray()[j];
/*  62 */             list.add(fd);
/*     */           }
/*     */         }
/*     */       }
/*  66 */     } else if ((oo instanceof EntityDef)) {
/*  67 */       EntityDef ed = (EntityDef)oo;
/*  68 */       list.add(ed);
/*  69 */       if (ed.getFieldDefs() != null) {
/*  70 */         for (int j = 0; j < ed.getFieldDefs().size(); j++) {
/*  71 */           FieldDef fd = (FieldDef)ed.getFieldDefs().toArray()[j];
/*  72 */           list.add(fd);
/*     */         }
/*     */       }
/*  75 */     } else if ((oo instanceof FieldDef)) {
/*  76 */       list.add(oo);
/*     */     }
/*  78 */     StringBuilder str_sql = new StringBuilder("");
/*  79 */     StringBuilder str_ora = new StringBuilder("");
/*  80 */     for (Object obj : list) {
/*  81 */       str_sql.append(buildSQL(obj, "sqlserver"));
/*  82 */       str_ora.append(buildSQL(obj, "oracle"));
/*     */     }
/*  84 */     File dir = new File(path);
/*  85 */     if (!dir.exists()) {
/*  86 */       dir.mkdirs();
/*     */     }
/*  88 */     String fileName = "/sql.txt";
/*  89 */     File file = new File(path + fileName);
/*  90 */     if ((!file.exists()) || (!file.isFile())) {
/*     */       try {
/*  92 */         file.createNewFile();
/*     */       } catch (IOException ex) {
/*  94 */         log.error(ex);
/*     */       }
/*     */     }
/*     */     try {
/*  98 */       BufferedWriter bw = new BufferedWriter(new FileWriter(file));
/*  99 */       String[] str = str_sql.toString().split("\\;");
/* 100 */       for (String str2 : str) {
/* 101 */         bw.write(str2);
/* 102 */         bw.write(";");
/* 103 */         bw.newLine();
/*     */       }
/* 105 */       bw.flush();
/* 106 */       bw.close();
/*     */     } catch (FileNotFoundException ex) {
/* 108 */       log.error(ex);
/*     */     } catch (IOException ex) {
/* 110 */       log.error(ex);
/*     */     }
/* 112 */     fileName = "/oracle.txt";
/* 113 */     file = new File(path + fileName);
/* 114 */     if ((!file.exists()) || (!file.isFile())) {
/*     */       try {
/* 116 */         file.createNewFile();
/*     */       } catch (IOException ex) {
/* 118 */         log.error(ex);
/*     */       }
/*     */     }
/*     */     try {
/* 122 */       BufferedWriter bw = new BufferedWriter(new FileWriter(file));
/* 123 */       String[] str = str_ora.toString().split("\\;");
/* 124 */       for (String str2 : str) {
/* 125 */         bw.write(str2);
/* 126 */         bw.write(";");
/* 127 */         bw.newLine();
/*     */       }
/* 129 */       bw.flush();
/* 130 */       bw.close();
/*     */     } catch (FileNotFoundException ex) {
/* 132 */       log.error(ex);
/*     */     } catch (IOException ex) {
/* 134 */       log.error(ex);
/*     */     }
/* 136 */     JOptionPane.showMessageDialog(null, "???????" + file.getAbsolutePath());
/*     */   }
/*     */   
/*     */   public static String buildSQL(Object obj, String db_type) {
/* 140 */     StringBuilder str = new StringBuilder();
/* 141 */     String[] fields = null;
/* 142 */     if ((obj instanceof ModuleInfo)) {
/* 143 */       fields = new String[] { "module_key", "module_code", "module_desc", "module_name", "order_no", "query_entity_name" };
/* 144 */       str.append("insert into moduleinfo(module_key,module_code,module_desc,module_name,order_no,query_entity_name)");
/* 145 */     } else if ((obj instanceof EntityClass)) {
/* 146 */       fields = new String[] { "entityClass_key", "entityType_code", "entityType_name", "modify_flag", "order_no", "preEntityName", "start_num", "super_class" };
/* 147 */       str.append("insert into entityclass(entityclass_key,entitytype_code,entitytype_name,modify_flag,order_no,preentityname,start_num,super_class,module_key) ");
/* 148 */     } else if ((obj instanceof EntityDef)) {
/* 149 */       fields = new String[] { "entity_key", "canmodify", "entityCaption", "entityName", "order_no", "success_build", "limit_flag", "init_flag" };
/* 150 */       str.append("insert into tabname (entity_key,canmodify,entitycaption,entityname,order_no,success_build,limit_flag,init_flag,entityclass_key)");
/* 151 */     } else if ((obj instanceof FieldDef)) {
/* 152 */       fields = new String[] { "field_key", "code_type_name", "default_value", "editable", "editableedit", "editablenew", "field_caption", "field_mark", "field_name", "field_scale", "field_type", "field_width", "format", "not_null", "order_no", "used_flag", "view_width", "visible", "visibleedit", "visiblenew", "pym", "field_align", "save_flag", "unique_flag", "relation_flag", "regula_save_flag", "regula_use_flag", "relation_add_flag", "relation_edit_flag", "relation_save_flag", "not_null_save_check", "regula_save_check" };
/* 153 */       str.append("insert into system(field_key,code_type_name,default_value,editable,editableedit,editablenew,field_caption,field_mark,field_name,field_scale,field_type,field_width,FORMAT,not_null,order_no,used_flag,view_width,visible,visibleedit,visiblenew,PYM,field_align,save_flag,unique_flag,relation_flag,regula_save_flag,regula_use_flag,relation_add_flag,relation_edit_flag,relation_save_flag,not_null_save_check,regula_save_check,entity_key) ");
/*     */     }
/* 155 */     if (db_type.equals("sqlserver")) {
/* 156 */       str.append(" select top 1 ");
/* 157 */       for (String field : fields) {
/* 158 */         str.append(getValue(obj, field));
/* 159 */         str.append(",");
/*     */       }
/* 161 */       str.deleteCharAt(str.length() - 1);
/* 162 */       if ((obj instanceof ModuleInfo)) {
/* 163 */         ModuleInfo mi = (ModuleInfo)obj;
/* 164 */         str.append(" from moduleinfo where not exists(select 1 from moduleinfo where module_key='").append(mi.getModule_key()).append("');");
/* 165 */       } else if ((obj instanceof EntityClass)) {
/* 166 */         EntityClass et = (EntityClass)obj;
/* 167 */         str.append(",'").append(et.getModuleInfo().getModule_key()).append("' from entityclass where not exists (select 1 from entityclass where entitytype_code='").append(et.getEntityType_code()).append("');");
/* 168 */       } else if ((obj instanceof EntityDef)) {
/* 169 */         EntityDef ed = (EntityDef)obj;
/* 170 */         EntityClass et = ed.getEntityClass();
/* 171 */         ModuleInfo mi = et.getModuleInfo();
/* 172 */         str.append(",entityclass_key from entityclass where module_key='").append(mi.getModule_key()).append("' and entityclass_key='").append(et.getEntityClass_key()).append("' and not exists(select 1 from tabname where entityname='").append(ed.getEntityName()).append("' and entityclass_key in(select entityclass_key from entityclass ec,moduleinfo mi where ec.module_key=mi.module_key and mi.module_code='").append(mi.getModule_code()).append("' and ec.entitytype_code ='").append(et.getEntityType_code()).append("'));");
/* 173 */       } else if ((obj instanceof FieldDef)) {
/* 174 */         FieldDef fd = (FieldDef)obj;
/* 175 */         EntityDef ed = fd.getEntityDef();
/* 176 */         str.append(",entity_key from tabname where entityname = '").append(ed.getEntityName()).append("' and not exists(select 1 from system where field_name ='").append(fd.getField_name()).append("' and entity_key in(select entity_key from tabname where entityname = '").append(ed.getEntityName()).append("'));");
/*     */       }
/*     */     } else {
/* 179 */       str.append(" select  ");
/* 180 */       for (String field : fields) {
/* 181 */         str.append(getValue(obj, field));
/* 182 */         str.append(",");
/*     */       }
/* 184 */       str.deleteCharAt(str.length() - 1);
/* 185 */       if ((obj instanceof ModuleInfo)) {
/* 186 */         ModuleInfo mi = (ModuleInfo)obj;
/* 187 */         str.append(" from moduleinfo where not exists(select 1 from moduleinfo where module_key='").append(mi.getModule_key()).append("') and rownum<2;");
/* 188 */       } else if ((obj instanceof EntityClass)) {
/* 189 */         EntityClass et = (EntityClass)obj;
/* 190 */         str.append(",'").append(et.getModuleInfo().getModule_key()).append("' from entityclass where not exists (select 1 from entityclass where entitytype_code='").append(et.getEntityType_code()).append("') and rownum<2;");
/* 191 */       } else if ((obj instanceof EntityDef)) {
/* 192 */         EntityDef ed = (EntityDef)obj;
/* 193 */         EntityClass et = ed.getEntityClass();
/* 194 */         ModuleInfo mi = et.getModuleInfo();
/* 195 */         str.append(",entityclass_key from entityclass where module_key='").append(mi.getModule_key()).append("' and entityclass_key='").append(et.getEntityClass_key()).append("' and not exists(select 1 from tabname where entityname='").append(ed.getEntityName()).append("' and entityclass_key in(select entityclass_key from entityclass ec,moduleinfo mi where ec.module_key=mi.module_key and mi.module_code='").append(mi.getModule_code()).append("' and ec.entitytype_code ='").append(et.getEntityType_code()).append("')) and rownum<2;");
/* 196 */       } else if ((obj instanceof FieldDef)) {
/* 197 */         FieldDef fd = (FieldDef)obj;
/* 198 */         EntityDef ed = fd.getEntityDef();
/* 199 */         str.append(",entity_key from tabname where entityname = '").append(ed.getEntityName()).append("' and not exists(select 1 from system where field_name ='").append(fd.getField_name()).append("' and entity_key in(select entity_key from tabname where entityname = '").append(ed.getEntityName()).append("')) and rownum<2;");
/*     */       }
/*     */     }
/* 202 */     return str.toString();
/*     */   }
/*     */   
/*     */   private static Object getValue(Object obj, String field) {
/* 206 */     Object data = PublicUtil.getProperty(obj, field);
/*     */     try {
/* 208 */       Field f = obj.getClass().getField(field);
/* 209 */       String name = f.getType().getSimpleName().toLowerCase();
/* 210 */       if (name.equals("boolean"))
/* 211 */         return Integer.valueOf(((Boolean)data).booleanValue() ? 1 : 0);
/* 212 */       if (name.equals("string"))
/* 213 */         return "'" + data.toString().replace("'", "''") + "'";
/* 214 */       if (name.equals("date")) {
/* 215 */         return DateUtil.toStringForQuery((Date)data, "yyyy-MM-dd HH:mm:ss");
/*     */       }
/* 217 */       return data.toString();
/*     */     }
/*     */     catch (Exception ex) {}
/*     */     
/* 221 */     return null;
/*     */   }
/*     */ }
