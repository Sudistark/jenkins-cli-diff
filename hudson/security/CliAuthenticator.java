package hudson.security;

import java.io.IOException;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;

@Deprecated
public abstract class CliAuthenticator {
  public abstract Authentication authenticate() throws AuthenticationException, IOException, InterruptedException;
}
