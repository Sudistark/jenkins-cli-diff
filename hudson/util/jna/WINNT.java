package hudson.util.jna;

public interface WINNT {
  public static final int DELETE = 65536;
  
  public static final int READ_CONTROL = 131072;
  
  public static final int WRITE_DAC = 262144;
  
  public static final int WRITE_OWNER = 524288;
  
  public static final int SYNCHRONIZE = 1048576;
  
  public static final int STANDARD_RIGHTS_REQUIRED = 983040;
  
  public static final int STANDARD_RIGHTS_READ = 131072;
  
  public static final int STANDARD_RIGHTS_WRITE = 131072;
  
  public static final int STANDARD_RIGHTS_EXECUTE = 131072;
  
  public static final int STANDARD_RIGHTS_ALL = 2031616;
  
  public static final int SPECIFIC_RIGHTS_ALL = 65535;
  
  public static final int GENERIC_EXECUTE = 536870912;
  
  public static final int SERVICE_WIN32_OWN_PROCESS = 16;
  
  public static final int KEY_QUERY_VALUE = 1;
  
  public static final int KEY_SET_VALUE = 2;
  
  public static final int KEY_CREATE_SUB_KEY = 4;
  
  public static final int KEY_ENUMERATE_SUB_KEYS = 8;
  
  public static final int KEY_NOTIFY = 16;
  
  public static final int KEY_CREATE_LINK = 32;
  
  public static final int KEY_READ = 131097;
  
  public static final int KEY_WRITE = 131078;
  
  public static final int REG_NONE = 0;
  
  public static final int REG_SZ = 1;
  
  public static final int REG_EXPAND_SZ = 2;
  
  public static final int REG_BINARY = 3;
  
  public static final int REG_DWORD = 4;
  
  public static final int REG_DWORD_LITTLE_ENDIAN = 4;
  
  public static final int REG_DWORD_BIG_ENDIAN = 5;
  
  public static final int REG_LINK = 6;
  
  public static final int REG_MULTI_SZ = 7;
  
  public static final int REG_RESOURCE_LIST = 8;
  
  public static final int REG_FULL_RESOURCE_DESCRIPTOR = 9;
  
  public static final int REG_RESOURCE_REQUIREMENTS_LIST = 10;
  
  public static final int REG_OPTION_RESERVED = 0;
  
  public static final int REG_OPTION_NON_VOLATILE = 0;
  
  public static final int REG_OPTION_VOLATILE = 1;
  
  public static final int REG_OPTION_CREATE_LINK = 2;
  
  public static final int REG_OPTION_BACKUP_RESTORE = 4;
  
  public static final int REG_OPTION_OPEN_LINK = 8;
}
