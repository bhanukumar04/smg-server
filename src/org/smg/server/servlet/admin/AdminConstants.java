package org.smg.server.servlet.admin;

/**
 * Constant used for admin component including 1. Admin Properties
 * 2. Error/Success Msg 3. Authorize Attributes 4. Email Expressions
 * @author kanghuan
 *
 */
public class AdminConstants {
  //Admin Properties
  public static final String USER_ID = "userId";
  public static final String ACCESS_SIGNATURE = "accessSignature";
  public static final String SUPER_ADMIN = "SuperAdmin@smg.com";
  public static final String ADMIN = "admin";	
  public static final String ADMIN_ID = "adminId";
  public static final String PASS_WORD = "password";
  public static final String UNIQUE_ADMIN = "SuperAdmin";
  public static final String UNIQUE_PASSWORD = "SuperAdminPassword3938";
  public static final String ADMIN_EMAIL = "lisa.j.luo@gmail.com";
  public static final String ADMIN_NAME = "smg-server";
  public static final String ADMIN_FINISHED = "ADMIN_FINISHED";
  
  //Error/success messages
  public static final String WRONG_USER_ID = "WRONG_USER_ID";
  public static final String WRONG_ADMIN_INFO = "WRONG_ADMIN_INFO";  
  public static final String ERROR = "error";
  public static final String SUCCESS = "success";
  public static final String MISSING_INFO = "MISSING_INFO";
  public static final String UPDATED = "updated";
  public static final String INVALID_FORMAT = "INVALID_FORMAT";
  public static final String WRONG_GAME_ID = "WRONG_GAME_ID";
  public static final String WRONG_INFO  = "WRONG_INFO";
  public static final String WRONG_EMAIL = "WRONG_EMAIL";
  //Authorize attributes
  public static final String PASSED_LIST = "PASSED_LIST";
  public static final String BLOCKED_LIST = "BLOCKED_LIST";
  public static final String NO_RECORD = "NO_RECORD";
  public static final String AUTHORIZED = "authorized";
  public static final String GAME_ID = "gameId";
  //Email expressions
  public static final String TEXT = "TEXT";
  public static final String FIRST_NAME = "firstName";
  public static final String EMAIL = "email";
  public static final String MAIL_SUBJECT = "Notification on your game approval";
  
  public static final String promote ()
  {
	  return "We are happy to inform you that you have been promoted to be part of our admin family\n";
  }
  public static final String degrade()
  {
	  return "We've decided to degrade you to a normal user\n";
  }
  public static final String approve(String gameName) {
    return "We are happy to inform you that your game " + gameName
        + " has been approved by our admin.\n";
  }

  public static final String disapprove(String gameName) {
    return "Your game " + gameName
            + " has not passed our authorization process.  Please update your game and resubmit.\n";
  }
}
