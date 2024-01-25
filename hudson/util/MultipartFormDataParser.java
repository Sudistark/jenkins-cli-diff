package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileCountLimitExceededException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.ArrayUtils;
import org.kohsuke.accmod.Restricted;

public class MultipartFormDataParser implements AutoCloseable {
  private final Map<String, FileItem> byName = new HashMap();
  
  private static int FILEUPLOAD_MAX_FILES = Integer.getInteger(MultipartFormDataParser.class.getName() + ".FILEUPLOAD_MAX_FILES", 1000).intValue();
  
  private static long FILEUPLOAD_MAX_FILE_SIZE = Long.getLong(MultipartFormDataParser.class.getName() + ".FILEUPLOAD_MAX_FILE_SIZE", -1L).longValue();
  
  private static long FILEUPLOAD_MAX_SIZE = Long.getLong(MultipartFormDataParser.class.getName() + ".FILEUPLOAD_MAX_SIZE", -1L).longValue();
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public MultipartFormDataParser(HttpServletRequest request, int maxParts, long maxPartSize, long maxSize) throws ServletException {
    try {
      tmpDir = Files.createTempDirectory("jenkins-multipart-uploads", new java.nio.file.attribute.FileAttribute[0]).toFile();
    } catch (IOException e) {
      throw new ServletException("Error creating temporary directory", e);
    } 
    tmpDir.deleteOnExit();
    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory(10240, tmpDir));
    upload.setFileCountMax(maxParts);
    upload.setFileSizeMax(maxPartSize);
    upload.setSizeMax(maxSize);
    try {
      for (FileItem fi : upload.parseRequest(request))
        this.byName.put(fi.getFieldName(), fi); 
    } catch (FileCountLimitExceededException e) {
      throw new ServletException("File upload field count limit exceeded. Consider setting the Java system property " + MultipartFormDataParser.class
          .getName() + ".FILEUPLOAD_MAX_FILES to a value greater than " + FILEUPLOAD_MAX_FILES + ", or to -1 to disable this limit.", e);
    } catch (org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException e) {
      throw new ServletException("File upload field size limit exceeded. Consider setting the Java system property " + MultipartFormDataParser.class
          .getName() + ".FILEUPLOAD_MAX_FILE_SIZE to a value greater than " + FILEUPLOAD_MAX_FILE_SIZE + ", or to -1 to disable this limit.", e);
    } catch (org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException e) {
      throw new ServletException("File upload total size limit exceeded. Consider setting the Java system property " + MultipartFormDataParser.class
          .getName() + ".FILEUPLOAD_MAX_SIZE to a value greater than " + FILEUPLOAD_MAX_SIZE + ", or to -1 to disable this limit.", e);
    } catch (FileUploadException e) {
      throw new ServletException(e);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public MultipartFormDataParser(HttpServletRequest request, int maxParts) throws ServletException { this(request, maxParts, FILEUPLOAD_MAX_FILE_SIZE, FILEUPLOAD_MAX_SIZE); }
  
  public MultipartFormDataParser(HttpServletRequest request) throws ServletException { this(request, FILEUPLOAD_MAX_FILES, FILEUPLOAD_MAX_FILE_SIZE, FILEUPLOAD_MAX_SIZE); }
  
  public String get(String key) {
    FileItem fi = (FileItem)this.byName.get(key);
    if (fi == null)
      return null; 
    return fi.getString();
  }
  
  public FileItem getFileItem(String key) { return (FileItem)this.byName.get(key); }
  
  public void cleanUp() {
    for (FileItem item : this.byName.values())
      item.delete(); 
  }
  
  public void close() { cleanUp(); }
  
  public static boolean isMultiPartForm(@CheckForNull String contentType) {
    if (contentType == null)
      return false; 
    String[] parts = contentType.split(";");
    return ArrayUtils.contains(parts, "multipart/form-data");
  }
}
