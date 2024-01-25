package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.util.KeyedDataStorage;
import java.io.IOException;
import java.util.Locale;
import jenkins.fingerprints.FingerprintStorage;

public final class FingerprintMap extends KeyedDataStorage<Fingerprint, FingerprintMap.FingerprintParams> {
  public boolean isReady() { return FingerprintStorage.get().isReady(); }
  
  @NonNull
  public Fingerprint getOrCreate(@CheckForNull AbstractBuild build, @NonNull String fileName, @NonNull byte[] md5sum) throws IOException { return getOrCreate(build, fileName, Util.toHexString(md5sum)); }
  
  @NonNull
  public Fingerprint getOrCreate(@CheckForNull AbstractBuild build, @NonNull String fileName, @NonNull String md5sum) throws IOException { return (Fingerprint)getOrCreate(md5sum, new FingerprintParams(build, fileName)); }
  
  @NonNull
  public Fingerprint getOrCreate(@CheckForNull Run build, @NonNull String fileName, @NonNull String md5sum) throws IOException { return (Fingerprint)getOrCreate(md5sum, new FingerprintParams(build, fileName)); }
  
  protected Fingerprint get(String md5sum, boolean createIfNotExist, FingerprintParams createParams) throws IOException {
    if (md5sum.length() != 32)
      return null; 
    md5sum = md5sum.toLowerCase(Locale.ENGLISH);
    return (Fingerprint)super.get(md5sum, createIfNotExist, createParams);
  }
  
  @NonNull
  protected Fingerprint create(@NonNull String md5sum, @NonNull FingerprintParams createParams) throws IOException { return new Fingerprint(createParams.build, createParams.fileName, Util.fromHexString(md5sum)); }
  
  @CheckForNull
  protected Fingerprint load(@NonNull String key) throws IOException { return Fingerprint.load(key); }
}
