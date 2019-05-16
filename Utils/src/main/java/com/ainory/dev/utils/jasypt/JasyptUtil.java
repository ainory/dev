package com.ainory.dev.utils.jasypt;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ainory on 2019. 5. 16..
 */
public class JasyptUtil {

  private static final Logger logger = LoggerFactory.getLogger(JasyptUtil.class);

  private static final String KEY = "t-core-2019";
  private static StandardPBEStringEncryptor pbeEnc = new StandardPBEStringEncryptor();

  static{
    pbeEnc.setAlgorithm("PBEWithMD5AndDES");
    pbeEnc.setPassword(KEY);
  }

  public static String Encryption(String value){
    try {
      return pbeEnc.encrypt(value);
    } catch (Exception e) {
      logger.error(ExceptionUtils.getStackTrace(e));
      return null;
    }
  }

  public static String Encryption(String key, String value){
    try {
      if(pbeEnc.isInitialized()){
        pbeEnc = new StandardPBEStringEncryptor();
        pbeEnc.setAlgorithm("PBEWithMD5AndDES");
      }
      pbeEnc.setPassword(key);
      return pbeEnc.encrypt(value);
    } catch (Exception e) {
      logger.error(ExceptionUtils.getStackTrace(e));
      return null;
    }
  }

  public static String Decryption(String encryptionValue){
    try {
      return pbeEnc.decrypt(encryptionValue);
    } catch (Exception e) {
      logger.error(ExceptionUtils.getStackTrace(e));
      return null;
    }
  }

  public static String Decryption(String key, String encryptionValue){
    try {
      if(pbeEnc.isInitialized()){
        pbeEnc = new StandardPBEStringEncryptor();
        pbeEnc.setAlgorithm("PBEWithMD5AndDES");
      }
      pbeEnc.setPassword(key);
      return pbeEnc.decrypt(encryptionValue);
    } catch (Exception e) {
      logger.error(ExceptionUtils.getStackTrace(e));
      return null;
    }
  }

  public static void main(String[] args) {
    String enc = JasyptUtil.Encryption("aaa", "tcore_collector");
    enc = JasyptUtil.Encryption("aaa", "tcore_collector");
    System.out.println(JasyptUtil.Decryption("aaa", "QGGtv51gO7FBMNlHu1eGZ5/UIMoEFmo+"));
    System.out.println(JasyptUtil.Decryption("aaa", "QGGtv51gO7FBMNlHu1eGZ5/UIMoEFmo+"));
  }
}
