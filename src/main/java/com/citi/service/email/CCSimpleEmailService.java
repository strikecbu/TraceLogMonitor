package com.citi.service.email;

public interface CCSimpleEmailService {
  boolean processEMail(String recipient, String subject, String message);
}
