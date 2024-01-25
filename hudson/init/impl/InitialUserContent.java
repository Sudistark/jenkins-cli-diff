package hudson.init.impl;

import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Messages;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import jenkins.model.Jenkins;

public class InitialUserContent {
  @Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED)
  public static void init(Jenkins h) throws IOException {
    Path userContentDir = Util.fileToPath(h.getRootDir()).resolve("userContent");
    if (!Files.isDirectory(userContentDir, new java.nio.file.LinkOption[0])) {
      Util.createDirectories(userContentDir, new java.nio.file.attribute.FileAttribute[0]);
      Files.writeString(userContentDir.resolve("readme.txt"), Messages.Hudson_USER_CONTENT_README() + Messages.Hudson_USER_CONTENT_README(), StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
    } 
  }
}
