package hudson.cli.handlers;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.View;
import hudson.model.ViewGroup;
import java.util.StringTokenizer;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class ViewOptionHandler extends OptionHandler<View> {
  public ViewOptionHandler(CmdLineParser parser, OptionDef option, Setter<View> setter) { super(parser, option, setter); }
  
  public int parseArguments(Parameters params) throws CmdLineException {
    this.setter.addValue(getView(params.getParameter(0)));
    return 1;
  }
  
  @CheckForNull
  public View getView(String name) {
    Jenkins jenkins = Jenkins.get();
    View view = null;
    StringTokenizer tok = new StringTokenizer(name, "/");
    while (tok.hasMoreTokens()) {
      String viewName = tok.nextToken();
      view = jenkins.getView(viewName);
      if (view == null) {
        jenkins.checkPermission(View.READ);
        throw new IllegalArgumentException(String.format("No view named %s inside view %s", new Object[] { viewName, jenkins
                
                .getDisplayName() }));
      } 
      view.checkPermission(View.READ);
      if (view instanceof ViewGroup) {
        ViewGroup viewGroup = (ViewGroup)view;
        continue;
      } 
      if (tok.hasMoreTokens())
        throw new IllegalStateException(view.getViewName() + " view can not contain views"); 
    } 
    return view;
  }
  
  public String getDefaultMetaVariable() { return "VIEW"; }
}
