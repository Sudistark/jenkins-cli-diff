package hudson.cli;

import hudson.Extension;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.util.QuotedStringTokenizer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;
import jenkins.scm.RunWithSCM;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.Option;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class ListChangesCommand extends RunRangeCommand {
  public String getShortDescription() { return Messages.ListChangesCommand_ShortDescription(); }
  
  @Option(name = "-format", usage = "Controls how the output from this command is printed.")
  public Format format = Format.PLAIN;
  
  protected int act(List<Run<?, ?>> builds) throws IOException {
    PrintWriter w;
    Charset charset;
    switch (null.$SwitchMap$hudson$cli$ListChangesCommand$Format[this.format.ordinal()]) {
      case 1:
        try {
          charset = getClientCharset();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } 
        w = new PrintWriter(new OutputStreamWriter(this.stdout, charset));
        w.println("<changes>");
        for (Run<?, ?> build : builds) {
          if (build instanceof RunWithSCM) {
            w.println("<build number='" + build.getNumber() + "'>");
            for (ChangeLogSet<?> cs : ((RunWithSCM)build).getChangeSets()) {
              Model p = (new ModelBuilder()).get(cs.getClass());
              p.writeTo(cs, Flavor.XML.createDataWriter(cs, w));
            } 
            w.println("</build>");
          } 
        } 
        w.println("</changes>");
        w.flush();
        return 0;
      case 2:
        for (Run<?, ?> build : builds) {
          if (build instanceof RunWithSCM)
            for (ChangeLogSet<?> cs : ((RunWithSCM)build).getChangeSets()) {
              for (ChangeLogSet.Entry e : cs) {
                this.stdout.printf("%s,%s%n", new Object[] { QuotedStringTokenizer.quote(e.getAuthor().getId()), QuotedStringTokenizer.quote(e.getMsg()) });
              } 
            }  
        } 
        return 0;
      case 3:
        for (Run<?, ?> build : builds) {
          if (build instanceof RunWithSCM)
            for (ChangeLogSet<?> cs : ((RunWithSCM)build).getChangeSets()) {
              for (ChangeLogSet.Entry e : cs) {
                this.stdout.printf("%s\t%s%n", new Object[] { e.getAuthor(), e.getMsg() });
                for (String p : e.getAffectedPaths())
                  this.stdout.println("  " + p); 
              } 
            }  
        } 
        return 0;
    } 
    throw new AssertionError("Unknown format: " + this.format);
  }
}
