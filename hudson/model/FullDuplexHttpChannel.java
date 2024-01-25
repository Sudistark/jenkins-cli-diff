package hudson.model;

import hudson.remoting.Channel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Logger;
import jenkins.util.FullDuplexHttpService;

@Deprecated
public abstract class FullDuplexHttpChannel extends FullDuplexHttpService {
  private Channel channel;
  
  private final boolean restricted;
  
  protected FullDuplexHttpChannel(UUID uuid, boolean restricted) throws IOException {
    super(uuid);
    this.restricted = restricted;
  }
  
  protected void run(InputStream upload, OutputStream download) throws IOException, InterruptedException {
    this.channel = new Channel("HTTP full-duplex channel " + this.uuid, Computer.threadPoolForRemoting, Channel.Mode.BINARY, upload, download, null, this.restricted);
    Object object = new Object(this, this.channel, upload);
    object.start();
    main(this.channel);
    this.channel.join();
    object.interrupt();
  }
  
  public Channel getChannel() { return this.channel; }
  
  private static final Logger LOGGER = Logger.getLogger(FullDuplexHttpChannel.class.getName());
  
  protected abstract void main(Channel paramChannel) throws IOException, InterruptedException;
}
