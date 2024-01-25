package jenkins.util.io;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Functions;
import hudson.Util;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class PathRemover {
  private final RetryStrategy retryStrategy;
  
  private final PathChecker pathChecker;
  
  public static PathRemover newSimpleRemover() { return new PathRemover(ignored -> false, PathChecker.ALLOW_ALL); }
  
  public static PathRemover newRemoverWithStrategy(@NonNull RetryStrategy retryStrategy) { return new PathRemover(retryStrategy, PathChecker.ALLOW_ALL); }
  
  public static PathRemover newFilteredRobustRemover(@NonNull PathChecker pathChecker, int maxRetries, boolean gcAfterFailedRemove, long waitBetweenRetries) { return new PathRemover(new PausingGCRetryStrategy(Math.max(maxRetries, 0), gcAfterFailedRemove, waitBetweenRetries), pathChecker); }
  
  private PathRemover(@NonNull RetryStrategy retryStrategy, @NonNull PathChecker pathChecker) {
    this.retryStrategy = retryStrategy;
    this.pathChecker = pathChecker;
  }
  
  public void forceRemoveFile(@NonNull Path path) throws IOException {
    int retryAttempts = 0;
    while (true) {
      Optional<IOException> maybeError = tryRemoveFile(path);
      if (maybeError.isEmpty())
        return; 
      if (this.retryStrategy.shouldRetry(retryAttempts)) {
        retryAttempts++;
        continue;
      } 
      IOException error = (IOException)maybeError.get();
      throw new IOException(this.retryStrategy.failureMessage(path, retryAttempts), error);
    } 
  }
  
  public void forceRemoveDirectoryContents(@NonNull Path path) throws IOException {
    int retryAttempt = 0;
    while (true) {
      List<IOException> errors = tryRemoveDirectoryContents(path);
      if (errors.isEmpty())
        return; 
      if (this.retryStrategy.shouldRetry(retryAttempt)) {
        retryAttempt++;
        continue;
      } 
      throw new CompositeIOException(this.retryStrategy.failureMessage(path, retryAttempt), errors);
    } 
  }
  
  public void forceRemoveRecursive(@NonNull Path path) throws IOException {
    int retryAttempt = 0;
    while (true) {
      List<IOException> errors = tryRemoveRecursive(path);
      if (errors.isEmpty())
        return; 
      if (this.retryStrategy.shouldRetry(retryAttempt)) {
        retryAttempt++;
        continue;
      } 
      throw new CompositeIOException(this.retryStrategy.failureMessage(path, retryAttempt), errors);
    } 
  }
  
  private Optional<IOException> tryRemoveFile(@NonNull Path path) {
    try {
      removeOrMakeRemovableThenRemove(path.normalize());
      return Optional.empty();
    } catch (IOException e) {
      return Optional.of(e);
    } 
  }
  
  private List<IOException> tryRemoveRecursive(@NonNull Path path) {
    Path normalized = path.normalize();
    List<IOException> accumulatedErrors = Util.isSymlink(normalized) ? new ArrayList() : tryRemoveDirectoryContents(normalized);
    Objects.requireNonNull(accumulatedErrors);
    tryRemoveFile(normalized).ifPresent(accumulatedErrors::add);
    return accumulatedErrors;
  }
  
  private List<IOException> tryRemoveDirectoryContents(@NonNull Path path) {
    Path normalized = path.normalize();
    List<IOException> accumulatedErrors = new ArrayList<IOException>();
    if (!Files.isDirectory(normalized, new LinkOption[0]))
      return accumulatedErrors; 
    try {
      DirectoryStream<Path> children = Files.newDirectoryStream(normalized);
      try {
        for (Path child : children)
          accumulatedErrors.addAll(tryRemoveRecursive(child)); 
        if (children != null)
          children.close(); 
      } catch (Throwable throwable) {
        if (children != null)
          try {
            children.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException e) {
      accumulatedErrors.add(e);
    } 
    return accumulatedErrors;
  }
  
  private void removeOrMakeRemovableThenRemove(@NonNull Path path) throws IOException {
    this.pathChecker.check(path);
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      makeRemovable(path);
      try {
        Files.deleteIfExists(path);
      } catch (IOException e2) {
        if (Files.isDirectory(path, new LinkOption[0])) {
          List<String> entries;
          Stream<Path> children = Files.list(path);
          try {
            entries = (List)children.map(Path::toString).collect(Collectors.toList());
            if (children != null)
              children.close(); 
          } catch (Throwable throwable) {
            if (children != null)
              try {
                children.close();
              } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
              }  
            throw throwable;
          } 
          throw new CompositeIOException("Unable to remove directory " + path + " with directory contents: " + entries, new IOException[] { e, e2 });
        } 
        throw new CompositeIOException("Unable to remove file " + path, new IOException[] { e, e2 });
      } 
    } 
  }
  
  private static void makeRemovable(@NonNull Path path) throws IOException {
    if (!Files.isWritable(path))
      makeWritable(path); 
    Optional<Path> maybeParent = Optional.ofNullable(path.getParent()).map(Path::normalize).filter(p -> !Files.isWritable(p));
    if (maybeParent.isPresent())
      makeWritable((Path)maybeParent.get()); 
  }
  
  private static void makeWritable(@NonNull Path path) throws IOException {
    if (!Functions.isWindows()) {
      try {
        PosixFileAttributes attrs = (PosixFileAttributes)Files.readAttributes(path, PosixFileAttributes.class, new LinkOption[0]);
        Set<PosixFilePermission> newPermissions = attrs.permissions();
        newPermissions.add(PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(path, newPermissions);
      } catch (NoSuchFileException ignored) {
        return;
      } catch (UnsupportedOperationException unsupportedOperationException) {}
    } else {
      DosFileAttributeView dos = (DosFileAttributeView)Files.getFileAttributeView(path, DosFileAttributeView.class, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
      if (dos != null)
        dos.setReadOnly(false); 
    } 
    path.toFile().setWritable(true);
  }
}
