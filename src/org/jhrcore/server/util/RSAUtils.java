/*     */ package org.jhrcore.server.util;
/*     */ 
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.security.Key;
/*     */ import java.security.KeyFactory;
/*     */ import java.security.KeyPair;
/*     */ import java.security.KeyPairGenerator;
/*     */ import java.security.PrivateKey;
/*     */ import java.security.PublicKey;
/*     */ import java.security.Signature;
/*     */ import java.security.interfaces.RSAPrivateKey;
/*     */ import java.security.interfaces.RSAPublicKey;
/*     */ import java.security.spec.PKCS8EncodedKeySpec;
/*     */ import java.security.spec.X509EncodedKeySpec;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import javax.crypto.Cipher;
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
/*     */ public class RSAUtils
/*     */ {
/*     */   public static final String KEY_ALGORITHM = "RSA";
/*     */   public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
/*     */   private static final String PUBLIC_KEY = "RSAPublicKey";
/*     */   private static final String PRIVATE_KEY = "RSAPrivateKey";
/*     */   private static final int MAX_ENCRYPT_BLOCK = 117;
/*     */   private static final int MAX_DECRYPT_BLOCK = 128;
/*     */   
/*     */   public static Map<String, Object> genKeyPair()
/*     */     throws Exception
/*     */   {
/*  87 */     KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
/*  88 */     keyPairGen.initialize(1024);
/*  89 */     KeyPair keyPair = keyPairGen.generateKeyPair();
/*  90 */     RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
/*  91 */     RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
/*  92 */     Map<String, Object> keyMap = new HashMap(2);
/*  93 */     keyMap.put("RSAPublicKey", publicKey);
/*  94 */     keyMap.put("RSAPrivateKey", privateKey);
/*  95 */     return keyMap;
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
/*     */ 
/*     */   public static String sign(byte[] data, String privateKey)
/*     */     throws Exception
/*     */   {
/* 110 */     byte[] keyBytes = Base64Utils.decode(privateKey);
/* 111 */     PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
/* 112 */     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
/* 113 */     PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
/* 114 */     Signature signature = Signature.getInstance("MD5withRSA");
/* 115 */     signature.initSign(privateK);
/* 116 */     signature.update(data);
/* 117 */     return Base64Utils.encode(signature.sign());
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static boolean verify(byte[] data, String publicKey, String sign)
/*     */     throws Exception
/*     */   {
/* 135 */     byte[] keyBytes = Base64Utils.decode(publicKey);
/* 136 */     X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
/* 137 */     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
/* 138 */     PublicKey publicK = keyFactory.generatePublic(keySpec);
/* 139 */     Signature signature = Signature.getInstance("MD5withRSA");
/* 140 */     signature.initVerify(publicK);
/* 141 */     signature.update(data);
/* 142 */     return signature.verify(Base64Utils.decode(sign));
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
/*     */ 
/*     */   public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
/*     */     throws Exception
/*     */   {
/* 157 */     byte[] keyBytes = Base64Utils.decode(privateKey);
/* 158 */     PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
/* 159 */     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
/* 160 */     Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
/* 161 */     Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
/* 162 */     cipher.init(2, privateK);
/* 163 */     int inputLen = encryptedData.length;
/* 164 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 165 */     int offSet = 0;
/*     */     
/* 167 */     int i = 0;
/*     */     
/* 169 */     while (inputLen - offSet > 0) { 
/* 170 */       byte[] cache; if (inputLen - offSet > 128) {
/* 171 */         cache = cipher.doFinal(encryptedData, offSet, 128);
/*     */       } else {
/* 173 */         cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
/*     */       }
/* 175 */       out.write(cache, 0, cache.length);
/* 176 */       i++;
/* 177 */       offSet = i * 128;
/*     */     }
/* 179 */     byte[] decryptedData = out.toByteArray();
/* 180 */     out.close();
/* 181 */     return decryptedData;
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
/*     */ 
/*     */   public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey)
/*     */     throws Exception
/*     */   {
/* 196 */     byte[] keyBytes = Base64Utils.decode(publicKey);
/* 197 */     X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
/* 198 */     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
/* 199 */     Key publicK = keyFactory.generatePublic(x509KeySpec);
/* 200 */     Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
/* 201 */     cipher.init(2, publicK);
/* 202 */     int inputLen = encryptedData.length;
/* 203 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 204 */     int offSet = 0;
/*     */     
/* 206 */     int i = 0;
/*     */     
/* 208 */     while (inputLen - offSet > 0) { 
/* 209 */       byte[] cache; if (inputLen - offSet > 128) {
/* 210 */         cache = cipher.doFinal(encryptedData, offSet, 128);
/*     */       } else {
/* 212 */         cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
/*     */       }
/* 214 */       out.write(cache, 0, cache.length);
/* 215 */       i++;
/* 216 */       offSet = i * 128;
/*     */     }
/* 218 */     byte[] decryptedData = out.toByteArray();
/* 219 */     out.close();
/* 220 */     return decryptedData;
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
/*     */ 
/*     */   public static byte[] encryptByPublicKey(byte[] data, String publicKey)
/*     */     throws Exception
/*     */   {
/* 235 */     byte[] keyBytes = Base64Utils.decode(publicKey);
/* 236 */     X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
/* 237 */     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
/* 238 */     Key publicK = keyFactory.generatePublic(x509KeySpec);
/*     */     
/* 240 */     Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
/* 241 */     cipher.init(1, publicK);
/* 242 */     int inputLen = data.length;
/* 243 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 244 */     int offSet = 0;
/*     */     
/* 246 */     int i = 0;
/*     */     
/* 248 */     while (inputLen - offSet > 0) {
/* 249 */       byte[] cache; if (inputLen - offSet > 117) {
/* 250 */         cache = cipher.doFinal(data, offSet, 117);
/*     */       } else {
/* 252 */         cache = cipher.doFinal(data, offSet, inputLen - offSet);
/*     */       }
/* 254 */       out.write(cache, 0, cache.length);
/* 255 */       i++;
/* 256 */       offSet = i * 117;
/*     */     }
/* 258 */     byte[] encryptedData = out.toByteArray();
/* 259 */     out.close();
/* 260 */     return encryptedData;
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
/*     */ 
/*     */   public static byte[] encryptByPrivateKey(byte[] data, String privateKey)
/*     */     throws Exception
/*     */   {
/* 275 */     byte[] keyBytes = Base64Utils.decode(privateKey);
/* 276 */     PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
/* 277 */     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
/* 278 */     Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
/* 279 */     Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
/* 280 */     cipher.init(1, privateK);
/* 281 */     int inputLen = data.length;
/* 282 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 283 */     int offSet = 0;
/*     */     
/* 285 */     int i = 0;
/*     */     
/* 287 */     while (inputLen - offSet > 0) {
/* 288 */       byte[] cache; if (inputLen - offSet > 117) {
/* 289 */         cache = cipher.doFinal(data, offSet, 117);
/*     */       } else {
/* 291 */         cache = cipher.doFinal(data, offSet, inputLen - offSet);
/*     */       }
/* 293 */       out.write(cache, 0, cache.length);
/* 294 */       i++;
/* 295 */       offSet = i * 117;
/*     */     }
/* 297 */     byte[] encryptedData = out.toByteArray();
/* 298 */     out.close();
/* 299 */     return encryptedData;
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
/*     */   public static String getPrivateKey(Map<String, Object> keyMap)
/*     */     throws Exception
/*     */   {
/* 313 */     Key key = (Key)keyMap.get("RSAPrivateKey");
/* 314 */     return Base64Utils.encode(key.getEncoded());
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
/*     */   public static String getPublicKey(Map<String, Object> keyMap)
/*     */     throws Exception
/*     */   {
/* 328 */     Key key = (Key)keyMap.get("RSAPublicKey");
/* 329 */     return Base64Utils.encode(key.getEncoded());
/*     */   }
/*     */ }
