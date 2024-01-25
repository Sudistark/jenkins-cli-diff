package jenkins.security;

import java.util.List;

@Deprecated
public class ApiTokenFilter extends BasicHeaderProcessor {
  protected List<? extends BasicHeaderAuthenticator> all() { return List.of(new BasicHeaderApiTokenAuthenticator()); }
}
