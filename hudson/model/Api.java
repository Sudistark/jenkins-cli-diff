package hudson.model;

import hudson.ExtensionList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.transform.stream.StreamResult;
import jenkins.model.Jenkins;
import jenkins.security.SecureRequester;
import jenkins.util.xml.FilteredFunctionContext;
import org.dom4j.CharacterData;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;
import org.kohsuke.stapler.export.NamedPathPruner;
import org.kohsuke.stapler.export.SchemaGenerator;
import org.kohsuke.stapler.export.TreePruner;

public class Api extends AbstractModelObject {
  public final Object bean;
  
  public Api(Object bean) { this.bean = bean; }
  
  public String getDisplayName() { return "API"; }
  
  public String getSearchUrl() { return "api"; }
  
  public void doXml(StaplerRequest req, StaplerResponse rsp, @QueryParameter String xpath, @QueryParameter String wrapper, @QueryParameter String tree, @QueryParameter int depth) throws IOException, ServletException {
    Object result;
    setHeaders(rsp);
    String[] excludes = req.getParameterValues("exclude");
    if (xpath == null && excludes == null) {
      rsp.serveExposedBean(req, this.bean, Flavor.XML);
      return;
    } 
    StringWriter sw = new StringWriter();
    Model p = MODEL_BUILDER.get(this.bean.getClass());
    NamedPathPruner namedPathPruner = (tree != null) ? new NamedPathPruner(tree) : new TreePruner.ByDepth(1 - depth);
    p.writeTo(this.bean, namedPathPruner, Flavor.XML.createDataWriter(this.bean, sw));
    FilteredFunctionContext functionContext = new FilteredFunctionContext();
    try {
      Document dom = (new SAXReader()).read(new StringReader(sw.toString()));
      if (excludes != null)
        for (String exclude : excludes) {
          XPath xExclude = dom.createXPath(exclude);
          xExclude.setFunctionContext(functionContext);
          List<Node> list = xExclude.selectNodes(dom);
          for (Node n : list) {
            Element parent = n.getParent();
            if (parent != null)
              parent.remove(n); 
          } 
        }  
      if (xpath == null) {
        result = dom;
      } else {
        XPath comp = dom.createXPath(xpath);
        comp.setFunctionContext(functionContext);
        List list = comp.selectNodes(dom);
        if (wrapper != null) {
          String validNameRE = "^[a-zA-Z_][\\w-\\.]*$";
          if (!wrapper.matches(validNameRE)) {
            rsp.setStatus(400);
            rsp.getWriter().print(Messages.Api_WrapperParamInvalid());
            return;
          } 
          Element root = DocumentFactory.getInstance().createElement(wrapper);
          for (Object o : list) {
            if (o instanceof String) {
              root.addText(o.toString());
              continue;
            } 
            root.add(((Node)o).detach());
          } 
          result = root;
        } else {
          if (list.isEmpty()) {
            rsp.setStatus(404);
            rsp.getWriter().print(Messages.Api_NoXPathMatch(xpath));
            return;
          } 
          if (list.size() > 1) {
            rsp.setStatus(500);
            rsp.getWriter().print(Messages.Api_MultipleMatch(xpath, Integer.valueOf(list.size())));
            return;
          } 
          result = list.get(0);
        } 
      } 
    } catch (DocumentException e) {
      LOGGER.log(Level.FINER, "Failed to do XPath/wrapper handling. XML is as follows:" + sw, e);
      throw new IOException("Failed to do XPath/wrapper handling. Turn on FINER logging to view XML.", e);
    } 
    if (isSimpleOutput(result) && !permit(req)) {
      rsp.sendError(403, "primitive XPath result sets forbidden; implement jenkins.security.SecureRequester");
      return;
    } 
    OutputStream o = rsp.getCompressedOutputStream(req);
    try {
      if (isSimpleOutput(result)) {
        rsp.setContentType("text/plain;charset=UTF-8");
        String text = (result instanceof CharacterData) ? ((CharacterData)result).getText() : result.toString();
        o.write(text.getBytes(StandardCharsets.UTF_8));
        if (o != null)
          o.close(); 
        return;
      } 
      rsp.setContentType("application/xml;charset=UTF-8");
      (new XMLWriter(o)).write(result);
      if (o != null)
        o.close(); 
    } catch (Throwable throwable) {
      if (o != null)
        try {
          o.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  private boolean isSimpleOutput(Object result) { return (result instanceof CharacterData || result instanceof String || result instanceof Number || result instanceof Boolean); }
  
  public void doSchema(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    setHeaders(rsp);
    rsp.setContentType("application/xml");
    StreamResult r = new StreamResult(rsp.getOutputStream());
    (new SchemaGenerator((new ModelBuilder()).get(this.bean.getClass()))).generateSchema(r);
    r.getOutputStream().close();
  }
  
  public void doJson(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    if (req.getParameter("jsonp") == null || permit(req)) {
      setHeaders(rsp);
      rsp.serveExposedBean(req, this.bean, (req.getParameter("jsonp") == null) ? Flavor.JSON : Flavor.JSONP);
    } else {
      rsp.sendError(403, "jsonp forbidden; implement jenkins.security.SecureRequester");
    } 
  }
  
  public void doPython(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    setHeaders(rsp);
    rsp.serveExposedBean(req, this.bean, Flavor.PYTHON);
  }
  
  private boolean permit(StaplerRequest req) {
    for (SecureRequester r : ExtensionList.lookup(SecureRequester.class)) {
      if (r.permit(req, this.bean))
        return true; 
    } 
    return false;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  protected void setHeaders(StaplerResponse rsp) {
    rsp.setHeader("X-Jenkins", Jenkins.VERSION);
    rsp.setHeader("X-Jenkins-Session", Jenkins.SESSION_HASH);
    rsp.setHeader("X-Content-Type-Options", "nosniff");
    rsp.setHeader("X-Frame-Options", "deny");
  }
  
  private static final Logger LOGGER = Logger.getLogger(Api.class.getName());
  
  private static final ModelBuilder MODEL_BUILDER = new ModelBuilder();
}
