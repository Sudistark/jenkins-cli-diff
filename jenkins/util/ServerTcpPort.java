package jenkins.util;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

public class ServerTcpPort {
  private int value;
  
  private String type;
  
  @DataBoundConstructor
  public ServerTcpPort(int value, String type) {
    this.value = value;
    this.type = type;
  }
  
  public ServerTcpPort(JSONObject o) {
    this.type = o.getString("type");
    this.value = o.optInt("value");
  }
  
  public int getPort() {
    if (this.type.equals("fixed"))
      return this.value; 
    if (this.type.equals("random"))
      return 0; 
    return -1;
  }
}
