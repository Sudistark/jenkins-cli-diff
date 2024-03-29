package jenkins.management;

import hudson.Extension;
import hudson.model.RootAction;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.GET;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AdministrativeMonitorsApi implements RootAction {
  @GET
  public void doNonSecurityPopupContent(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
    AdministrativeMonitorsApiData viewData = new AdministrativeMonitorsApiData(getDecorator().getNonSecurityAdministrativeMonitors());
    req.getView(viewData, "monitorsList.jelly").forward(req, resp);
  }
  
  @GET
  public void doSecurityPopupContent(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
    AdministrativeMonitorsApiData viewData = new AdministrativeMonitorsApiData(getDecorator().getSecurityAdministrativeMonitors());
    req.getView(viewData, "monitorsList.jelly").forward(req, resp);
  }
  
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return null; }
  
  public String getUrlName() { return "administrativeMonitorsApi"; }
  
  private AdministrativeMonitorsDecorator getDecorator() {
    return (AdministrativeMonitorsDecorator)Jenkins.get()
      .getExtensionList(hudson.model.PageDecorator.class)
      .get(AdministrativeMonitorsDecorator.class);
  }
}
