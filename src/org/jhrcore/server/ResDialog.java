/*     */ package org.jhrcore.server;
/*     */ 
/*     */ import javax.swing.GroupLayout;
/*     */ import javax.swing.GroupLayout.Alignment;
/*     */ import javax.swing.GroupLayout.ParallelGroup;
/*     */ import javax.swing.GroupLayout.SequentialGroup;
/*     */ import javax.swing.JLabel;
/*     */ 
/*     */ public class ResDialog extends javax.swing.JDialog
/*     */ {
/*     */   private javax.swing.JButton btnCancel;
/*     */   private JLabel jLabel1;
/*     */   private JLabel jLabel2;
/*     */   private JLabel jLabel5;
/*     */   private JLabel jLabel8;
/*     */   private javax.swing.JScrollPane jScrollPane1;
/*     */   private javax.swing.JSeparator jSeparator1;
/*     */   private javax.swing.JTextArea ta_ma;
/*     */   
/*     */   public ResDialog(java.awt.Frame parent, boolean modal)
/*     */   {
/*  22 */     super(parent, modal);
/*  23 */     initComponents();
/*     */   }
/*     */   
/*     */   public ResDialog() {
/*  27 */     initComponents();
/*  28 */     this.ta_ma.setText(org.jhrcore.server.util.CommUtil.getCPUSerialId() + org.jhrcore.server.util.CommUtil.getMACAddress());
/*  29 */     this.btnCancel.addActionListener(new java.awt.event.ActionListener()
/*     */     {
/*     */       public void actionPerformed(java.awt.event.ActionEvent e)
/*     */       {
/*  33 */         ResDialog.this.dispose();
/*  34 */         System.exit(0);
/*     */       }
/*     */     });
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   private void initComponents()
/*     */   {
/*  49 */     this.jLabel1 = new JLabel();
/*  50 */     this.jScrollPane1 = new javax.swing.JScrollPane();
/*  51 */     this.ta_ma = new javax.swing.JTextArea();
/*  52 */     this.jLabel2 = new JLabel();
/*  53 */     this.btnCancel = new javax.swing.JButton();
/*  54 */     this.jSeparator1 = new javax.swing.JSeparator();
/*  55 */     this.jLabel5 = new JLabel();
/*  56 */     this.jLabel8 = new JLabel();
/*     */     
/*  58 */     setDefaultCloseOperation(2);
/*  59 */     setModal(true);
/*     */     
/*  61 */     this.jLabel1.setText("注 册 码：");
/*     */     
/*  63 */     this.ta_ma.setColumns(20);
/*  64 */     this.ta_ma.setRows(5);
/*  65 */     this.jScrollPane1.setViewportView(this.ta_ma);
/*     */     
/*  67 */     this.jLabel2.setText("联系方式：");
/*     */     
/*  69 */     this.btnCancel.setText("关闭");
/*     */     
/*  71 */     this.jLabel5.setText("注：用时间到期后请复制注册码并发给销售人员进行注册使用。 ");
/*     */     
/*  73 */     this.jLabel8.setText("电话：18954682131 微信同步。");
/*     */     
/*  75 */     GroupLayout layout = new GroupLayout(getContentPane());
/*  76 */     getContentPane().setLayout(layout);
/*  77 */     layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jSeparator1).addGroup(layout.createSequentialGroup().addGap(35, 35, 35).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addGroup(layout.createSequentialGroup().addComponent(this.jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jScrollPane1, -2, 391, -2)).addGroup(layout.createSequentialGroup().addComponent(this.jLabel2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jLabel8)).addComponent(this.jLabel5, GroupLayout.Alignment.TRAILING, -1, -1, 32767)).addContainerGap(85, 32767)).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(-1, 32767).addComponent(this.btnCancel).addGap(201, 201, 201)));
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
/*  98 */     layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(43, 43, 43).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jScrollPane1, -2, -1, -2).addComponent(this.jLabel1)).addGap(46, 46, 46).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jLabel2).addComponent(this.jLabel8)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, 32767).addComponent(this.jLabel5).addGap(18, 18, 18).addComponent(this.jSeparator1, -2, -1, -2).addGap(18, 18, 18).addComponent(this.btnCancel).addGap(32, 32, 32)));
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
/* 118 */     pack();
/*     */   }
/*     */ }


/* Location:              E:\cspros\weifu\ecard_backup\hrserver\dist\hrserver.jar!\org\jhrcore\server\ResDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */