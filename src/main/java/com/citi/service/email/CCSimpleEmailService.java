package com.citi.service.email;

import com.citi.model.PendingLog;

import java.util.List;

public interface CCSimpleEmailService {
  boolean processEMail(String recipient, String subject, String message);
}
