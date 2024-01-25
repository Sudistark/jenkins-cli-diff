package hudson.security;

import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AccountCreationFailedException extends Exception {
  public AccountCreationFailedException(String message) { super(message); }
}
