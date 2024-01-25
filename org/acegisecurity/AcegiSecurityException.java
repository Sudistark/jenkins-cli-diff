package org.acegisecurity;

import org.springframework.core.NestedRuntimeException;

@Deprecated
public abstract class AcegiSecurityException extends NestedRuntimeException {
  protected AcegiSecurityException(String msg) { super(msg); }
  
  protected AcegiSecurityException(String msg, Throwable cause) { super(msg, cause); }
  
  public RuntimeException toSpring() { return new RuntimeException(toString(), this); }
}
