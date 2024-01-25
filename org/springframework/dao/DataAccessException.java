package org.springframework.dao;

import hudson.security.UserMayOrMayNotExistException2;
import org.springframework.core.NestedRuntimeException;

@Deprecated
public class DataAccessException extends NestedRuntimeException {
  public DataAccessException(String msg) { super(msg); }
  
  public DataAccessException(String msg, Throwable cause) { super(msg, cause); }
  
  public UserMayOrMayNotExistException2 toSpring() { return new UserMayOrMayNotExistException2(toString(), this); }
}
