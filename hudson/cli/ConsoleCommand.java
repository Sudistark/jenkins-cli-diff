package hudson.cli;

import hudson.Extension;
import hudson.console.AnnotatedLargeText;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.PermalinkProjectAction;
import hudson.model.Run;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Extension
public class ConsoleCommand extends CLICommand {
  @Argument(metaVar = "JOB", usage = "Name of the job", required = true)
  public Job<?, ?> job;
  
  public String getShortDescription() { return Messages.ConsoleCommand_ShortDescription(); }
  
  @Argument(metaVar = "BUILD", usage = "Build number or permalink to point to the build. Defaults to the last build", required = false, index = 1)
  public String build = "lastBuild";
  
  @Option(name = "-f", usage = "If the build is in progress, stay around and append console output as it comes, like 'tail -f'")
  public boolean follow = false;
  
  @Option(name = "-n", metaVar = "N", usage = "Display the last N lines")
  public int n = -1;
  
  protected int run() throws Exception {
    Run<?, ?> run;
    this.job.checkPermission(Item.READ);
    try {
      int n = Integer.parseInt(this.build);
      run = this.job.getBuildByNumber(n);
      if (run == null)
        throw new IllegalArgumentException("No such build #" + n); 
    } catch (NumberFormatException e) {
      PermalinkProjectAction.Permalink p = this.job.getPermalinks().get(this.build);
      if (p != null) {
        run = p.resolve(this.job);
        if (run == null)
          throw new IllegalStateException("Permalink " + this.build + " produced no build", e); 
      } else {
        PermalinkProjectAction.Permalink nearest = this.job.getPermalinks().findNearest(this.build);
        throw new IllegalArgumentException((nearest == null) ? 
            String.format("Not sure what you meant by \"%s\".", new Object[] { this.build }) : String.format("Not sure what you meant by \"%s\". Did you mean \"%s\"?", new Object[] { this.build, nearest
                .getId() }), e);
      } 
    } 
    w = new OutputStreamWriter(this.stdout, getClientCharset());
    try {
      long pos = (this.n >= 0) ? seek(run) : 0L;
      if (this.follow) {
        AnnotatedLargeText logText;
        do {
          logText = run.getLogText();
          pos = logText.writeLogTo(pos, w);
        } while (!logText.isComplete());
      } else {
        InputStream logInputStream = run.getLogInputStream();
        try {
          IOUtils.skip(logInputStream, pos);
          IOUtils.copy(new InputStreamReader(logInputStream, run.getCharset()), w);
          if (logInputStream != null)
            logInputStream.close(); 
        } catch (Throwable throwable) {
          if (logInputStream != null)
            try {
              logInputStream.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } 
    } finally {
      w.flush();
      w.close();
    } 
    return 0;
  }
  
  private long seek(Run<?, ?> run) throws IOException {
    RingBuffer rb = new RingBuffer(this);
    InputStream in = run.getLogInputStream();
    try {
      byte[] buf = new byte[4096];
      byte prev = 0;
      long pos = 0L;
      boolean prevIsNL = false;
      int len;
      while ((len = in.read(buf)) >= 0) {
        for (int i = 0; i < len; i++) {
          byte ch = buf[i];
          boolean isNL = (ch == 13 || ch == 10);
          if (!isNL && prevIsNL)
            rb.add(pos); 
          if (isNL && prevIsNL && (prev != 13 || ch != 10))
            rb.add(pos); 
          pos++;
          prev = ch;
          prevIsNL = isNL;
        } 
      } 
      long l = rb.get();
      if (in != null)
        in.close(); 
      return l;
    } catch (Throwable throwable) {
      if (in != null)
        try {
          in.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  protected void printUsageSummary(PrintStream stderr) { stderr.println("Produces the console output of a specific build to stdout, as if you are doing 'cat build.log'"); }
}
