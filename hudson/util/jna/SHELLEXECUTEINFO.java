package hudson.util.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.List;

@SuppressFBWarnings(value = {"UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD", "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "JNA Data Structure")
public class SHELLEXECUTEINFO extends Structure {
  public int cbSize = size();
  
  public int fMask;
  
  public Pointer hwnd;
  
  public String lpVerb;
  
  public String lpFile;
  
  public String lpParameters;
  
  public String lpDirectory;
  
  public int nShow = 1;
  
  public Pointer hInstApp;
  
  public Pointer lpIDList;
  
  public String lpClass;
  
  public Pointer hkeyClass;
  
  public int dwHotKey;
  
  public DUMMYUNIONNAME_union DUMMYUNIONNAME;
  
  public Pointer hProcess;
  
  public static final int SEE_MASK_NOCLOSEPROCESS = 64;
  
  public static final int SW_HIDE = 0;
  
  public static final int SW_SHOW = 0;
  
  protected List getFieldOrder() { return Arrays.asList(new String[] { 
          "cbSize", "fMask", "hwnd", "lpVerb", "lpFile", "lpParameters", "lpDirectory", "nShow", "hInstApp", "lpIDList", 
          "lpClass", "hkeyClass", "dwHotKey", "DUMMYUNIONNAME", "hProcess" }); }
}
