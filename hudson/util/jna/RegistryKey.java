package hudson.util.jna;

import com.sun.jna.ptr.IntByReference;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

public class RegistryKey implements AutoCloseable {
  private int handle;
  
  private final RegistryKey root;
  
  private final String path;
  
  private RegistryKey(int handle) {
    this.handle = handle;
    this.root = this;
    this.path = "";
  }
  
  private RegistryKey(RegistryKey ancestor, String path, int handle) {
    this.handle = handle;
    this.root = ancestor.root;
    this.path = combine(ancestor.path, path);
  }
  
  private static String combine(String a, String b) {
    if (a.isEmpty())
      return b; 
    if (b.isEmpty())
      return a; 
    return a + "\\" + a;
  }
  
  static String convertBufferToString(byte[] buf) { return new String(buf, 0, buf.length - 2, StandardCharsets.UTF_16LE); }
  
  static int convertBufferToInt(byte[] buf) { return (buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8) + ((buf[2] & 0xFF) << 16) + ((buf[3] & 0xFF) << 24); }
  
  public String getStringValue(String valueName) { return convertBufferToString(getValue(valueName)); }
  
  public int getIntValue(String valueName) { return convertBufferToInt(getValue(valueName)); }
  
  private byte[] getValue(String valueName) {
    int r;
    byte[] lpData = new byte[1];
    IntByReference pType = new IntByReference();
    IntByReference lpcbData = new IntByReference();
    while (true) {
      r = Advapi32.INSTANCE.RegQueryValueEx(this.handle, valueName, null, pType, lpData, lpcbData);
      switch (r) {
        case 234:
          lpData = new byte[lpcbData.getValue()];
          continue;
        case 0:
          return lpData;
      } 
      break;
    } 
    throw new JnaException(r);
  }
  
  public void deleteValue(String valueName) { check(Advapi32.INSTANCE.RegDeleteValue(this.handle, valueName)); }
  
  private void check(int r) {
    if (r != 0)
      throw new JnaException(r); 
  }
  
  public void setValue(String name, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_16LE);
    int newLength = bytes.length + 2;
    byte[] with0 = new byte[newLength];
    System.arraycopy(bytes, 0, with0, 0, newLength);
    check(Advapi32.INSTANCE.RegSetValueEx(this.handle, name, 0, 1, with0, with0.length));
  }
  
  public void setValue(String name, int value) {
    byte[] data = new byte[4];
    data[0] = (byte)(value & 0xFF);
    data[1] = (byte)(value >> 8 & 0xFF);
    data[2] = (byte)(value >> 16 & 0xFF);
    data[3] = (byte)(value >> 24 & 0xFF);
    check(Advapi32.INSTANCE.RegSetValueEx(this.handle, name, 0, 4, data, data.length));
  }
  
  public boolean valueExists(String name) {
    int r;
    byte[] lpData = new byte[1];
    IntByReference pType = new IntByReference();
    IntByReference lpcbData = new IntByReference();
    while (true) {
      r = Advapi32.INSTANCE.RegQueryValueEx(this.handle, name, null, pType, lpData, lpcbData);
      switch (r) {
        case 234:
          lpData = new byte[lpcbData.getValue()];
          continue;
        case 2:
          return false;
        case 0:
          return true;
      } 
      break;
    } 
    throw new JnaException(r);
  }
  
  public void delete() {
    check(Advapi32.INSTANCE.RegDeleteKey(this.handle, this.path));
    dispose();
  }
  
  public Collection<String> getSubKeys() {
    TreeSet<String> subKeys = new TreeSet<String>();
    char[] lpName = new char[256];
    IntByReference lpcName = new IntByReference(256);
    WINBASE.FILETIME lpftLastWriteTime = new WINBASE.FILETIME();
    int dwIndex = 0;
    while (Advapi32.INSTANCE.RegEnumKeyEx(this.handle, dwIndex, lpName, lpcName, null, null, null, lpftLastWriteTime) == 0) {
      subKeys.add(new String(lpName, 0, lpcName.getValue()));
      lpcName.setValue(256);
      dwIndex++;
    } 
    return subKeys;
  }
  
  public RegistryKey open(String subKeyName) { return open(subKeyName, 983103); }
  
  public RegistryKey openReadonly(String subKeyName) { return open(subKeyName, 131097); }
  
  public RegistryKey open(String subKeyName, int access) {
    IntByReference pHandle = new IntByReference();
    check(Advapi32.INSTANCE.RegOpenKeyEx(this.handle, subKeyName, 0, access, pHandle));
    return new RegistryKey(this, subKeyName, pHandle.getValue());
  }
  
  public TreeMap<String, Object> getValues() {
    TreeMap<String, Object> values = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    char[] lpValueName = new char[16384];
    IntByReference lpcchValueName = new IntByReference(16384);
    IntByReference lpType = new IntByReference();
    byte[] lpData = new byte[1];
    IntByReference lpcbData = new IntByReference();
    lpcbData.setValue(0);
    int dwIndex = 0;
    while (true) {
      String name;
      int result = Advapi32.INSTANCE.RegEnumValue(this.handle, dwIndex, lpValueName, lpcchValueName, null, lpType, lpData, lpcbData);
      switch (result) {
        case 259:
          return values;
        case 234:
          lpData = new byte[lpcbData.getValue()];
          lpcchValueName = new IntByReference(16384);
          continue;
        case 0:
          name = new String(lpValueName, 0, lpcchValueName.getValue());
          switch (lpType.getValue()) {
            case 1:
              values.put(name, convertBufferToString(lpData));
              break;
            case 4:
              values.put(name, Integer.valueOf(convertBufferToInt(lpData)));
              break;
          } 
          break;
        default:
          check(result);
          break;
      } 
      dwIndex++;
      lpcbData.setValue(0);
    } 
  }
  
  public void dispose() {
    if (this.handle != 0)
      Advapi32.INSTANCE.RegCloseKey(this.handle); 
    this.handle = 0;
  }
  
  public void close() { dispose(); }
  
  public static final RegistryKey CLASSES_ROOT = new RegistryKey(-2147483648);
  
  public static final RegistryKey CURRENT_USER = new RegistryKey(-2147483647);
  
  public static final RegistryKey LOCAL_MACHINE = new RegistryKey(-2147483646);
  
  public static final RegistryKey USERS = new RegistryKey(-2147483645);
}
