package com.citi.service.email.impl;


import com.citi.Constants;
import com.citi.model.PendingLog;
import com.citi.service.email.CCSimpleEmailService;
import com.citi.util.CapString;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class CCSimpleEmailServiceImpl implements CCSimpleEmailService{

  protected static Logger logger = Logger.getLogger(CCSimpleEmailServiceImpl.class);

  private Properties sysProp;

  public CCSimpleEmailServiceImpl(Properties sysProp){
    this.sysProp = sysProp;
  }

  @Override
  public boolean processEMail(String recipient, String subject, String message) {
    try {
//      sysProp.remove("EDM_CHARSET");
//      sysProp.remove("EDM_SUBJECT");
      final String FROM_ADDRESS = sysProp.getProperty(Constants.EDM_FROM_ADDR);  //"gverdsii@gmail.com";
      final String FROM_PERSON = sysProp.getProperty(Constants.EDM_FROM_PERSON);  //"花旗（台灣）銀行";
      final String EDM_HOST = sysProp.getProperty(Constants.EDM_HOST);
      final String EDM_USR = sysProp.getProperty(Constants.EDM_USR);
      final String EDM_PWD = sysProp.getProperty(Constants.EDM_PWD);
      final String LOCAL_TEST = sysProp.getProperty(Constants.LOCAL_TEST);
      final boolean isLocalTest = "true".equalsIgnoreCase(LOCAL_TEST);

      // [COLA] Cycle Date Import Notification
      String EDM_SUBJECT = subject;

      String EDM_CHARSET = sysProp.getProperty("EDM_CHARSET");
      if (CapString.isEmpty(EDM_CHARSET)) {
        EDM_CHARSET = "utf-8";
      }

      Properties props = new Properties();
      // props.put("mail.smtp.host", "imta.citicorp.com");
      props.put("mail.smtp.host", EDM_HOST);
      logger.debug("[CCEmailServiceImpl] @ mail.smtp.host >> " + EDM_HOST);

      Session mailSession;
      if (isLocalTest) {
        /** TEST GMAIL */
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        mailSession = Session.getInstance(
          props,
          new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(EDM_USR, EDM_PWD);
            }
          }
        );
      } else {
        mailSession = Session.getDefaultInstance(props, null);
      }
      //mailSession.setDebug(true);
      mailSession.setDebug(Boolean.valueOf(sysProp.getProperty("mail.debug", "true")));

      // Email
      MimeMessage msg = new MimeMessage(mailSession);

      // 主旨
      msg.setSubject(EDM_SUBJECT, "BIG5");

      // 寄件人
      InternetAddress fromAddr = new InternetAddress(FROM_ADDRESS, FROM_PERSON, "BIG5");
      msg.setFrom(fromAddr);

      // 收件人
      msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient, false));

      // 如果 subtype = related 代表可以發送 HTML 格式
      MimeMultipart multipart = new MimeMultipart();
      // first part
      BodyPart messageBodyPart = new MimeBodyPart();
      messageBodyPart.setContent(message, "text/plain;charset=utf-8");
      // add it
      multipart.addBodyPart(messageBodyPart);
      logger.debug("[CCConfirmPageServiceImpl] @ emailing orgStr >>" + message);

      // put everything together
      msg.setContent(multipart);
      // msg.setContent(test, "text/html; charset=utf-8");
      msg.setSentDate(new Date());

      Transport.send(msg);
    } catch (MessagingException me) {
      me.printStackTrace();
      logger.error("sendEmailNotification:" + me.getMessage(), me);

      return false;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      logger.error("sendEmailNotification:" + e.getMessage(), e);

      return false;
    } catch (RuntimeException e) {
      e.printStackTrace();
      logger.error("sendEmailNotification:" + e.getMessage(), e);

      return false;
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("sendEmailNotification:" + e.getMessage(), e);

      return false;
    } catch (NoSuchMethodError e) {
      e.printStackTrace();
      logger.error("[CCEmailServiceImpl] @ sendEmailNotification: No SuchMethodError  ");

      return false;
    }

    return true;
  }

}
