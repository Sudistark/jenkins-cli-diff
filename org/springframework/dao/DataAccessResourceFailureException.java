package org.springframework.dao;

@Deprecated
public class DataAccessResourceFailureException extends DataAccessException {
  public DataAccessResourceFailureException(String msg) { super(msg); }
  
  public DataAccessResourceFailureException(String msg, Throwable cause) { super(msg, cause); }
}
