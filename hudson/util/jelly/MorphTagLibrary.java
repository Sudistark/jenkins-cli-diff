package hudson.util.jelly;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.Attributes;

public class MorphTagLibrary extends TagLibrary {
  private static final String META_ATTRIBUTES = "ATTRIBUTES";
  
  private static final String EXCEPT_ATTRIBUTES = "EXCEPT";
  
  public Tag createTag(String name, Attributes attributes) throws JellyException { return null; }
  
  public TagScript createTagScript(String tagName, Attributes attributes) throws JellyException { return new Object(this, tagName); }
}
