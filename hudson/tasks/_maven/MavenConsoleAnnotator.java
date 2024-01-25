package hudson.tasks._maven;

import hudson.console.LineTransformationOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;

public class MavenConsoleAnnotator extends LineTransformationOutputStream.Delegating {
  private final Charset charset;
  
  public MavenConsoleAnnotator(OutputStream out, Charset charset) {
    super(out);
    this.charset = charset;
  }
  
  protected void eol(byte[] b, int len) throws IOException {
    String line = this.charset.decode(ByteBuffer.wrap(b, 0, len)).toString();
    line = trimEOL(line);
    Matcher m = MavenMojoNote.PATTERN.matcher(line);
    if (m.matches())
      (new MavenMojoNote()).encodeTo(this.out); 
    m = Maven3MojoNote.PATTERN.matcher(line);
    if (m.matches())
      (new Maven3MojoNote()).encodeTo(this.out); 
    m = MavenWarningNote.PATTERN.matcher(line);
    if (m.find())
      (new MavenWarningNote()).encodeTo(this.out); 
    m = MavenErrorNote.PATTERN.matcher(line);
    if (m.find())
      (new MavenErrorNote()).encodeTo(this.out); 
    this.out.write(b, 0, len);
  }
}
