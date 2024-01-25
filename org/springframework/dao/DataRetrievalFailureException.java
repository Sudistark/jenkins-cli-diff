package org.springframework.dao;

@Deprecated
public class DataRetrievalFailureException extends DataAccessException {
  public DataRetrievalFailureException(String msg) { super(msg); }
  
  public DataRetrievalFailureException(String msg, Throwable cause) { super(msg, cause); }
}
