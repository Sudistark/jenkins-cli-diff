package hudson.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class FileChannelWriter extends Writer implements Channel {
  private static final Logger LOGGER = Logger.getLogger(FileChannelWriter.class.getName());
  
  private final Charset charset;
  
  private final FileChannel channel;
  
  private final boolean forceOnFlush;
  
  private final boolean forceOnClose;
  
  FileChannelWriter(Path filePath, Charset charset, boolean forceOnFlush, boolean forceOnClose, OpenOption... options) throws IOException {
    this.charset = charset;
    this.forceOnFlush = forceOnFlush;
    this.forceOnClose = forceOnClose;
    this.channel = FileChannel.open(filePath, options);
  }
  
  public void write(char[] cbuf, int off, int len) throws IOException {
    CharBuffer charBuffer = CharBuffer.wrap(cbuf, off, len);
    ByteBuffer byteBuffer = this.charset.encode(charBuffer);
    this.channel.write(byteBuffer);
  }
  
  public void flush() throws IOException {
    if (this.forceOnFlush) {
      LOGGER.finest("Flush is forced");
      this.channel.force(true);
    } else {
      LOGGER.finest("Force disabled on flush(), no-op");
    } 
  }
  
  public boolean isOpen() { return this.channel.isOpen(); }
  
  public void close() throws IOException {
    if (this.channel.isOpen()) {
      if (this.forceOnClose)
        this.channel.force(true); 
      this.channel.close();
    } 
  }
}
