package hudson.util.jna;

import java.net.UnknownHostException;
import org.jinterop.dcom.common.IJIAuthInfo;
import org.jinterop.dcom.common.JIException;
import org.jinterop.winreg.IJIWinReg;
import org.jinterop.winreg.JIPolicyHandle;
import org.jinterop.winreg.JIWinRegFactory;

public class DotNet {
  private static final String PATH20 = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v2.0.50727";
  
  private static final String PATH30 = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v3.0\\Setup";
  
  private static final String PATH35 = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v3.5";
  
  private static final String PATH4 = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full";
  
  private static final String VALUE_INSTALL = "Install";
  
  private static final String VALUE_INSTALL_SUCCESS = "InstallSuccess";
  
  private static final String VALUE_RELEASE = "Release";
  
  public static boolean isInstalled(int major, int minor) {
    try {
      if (major == 4 && minor >= 5)
        return isV45PlusInstalled(minor); 
      if (major == 4 && minor == 0)
        return isV40Installed(); 
      if (major == 3 && minor == 5)
        return isV35Installed(); 
      if (major == 3 && minor == 0)
        return (isV35Installed() || isV30Installed()); 
      if (major == 2 && minor == 0)
        return (isV35Installed() || isV30Installed() || isV20Installed()); 
      return false;
    } catch (JnaException e) {
      if (e.getErrorCode() == 2)
        return false; 
      throw e;
    } 
  }
  
  private static boolean isV45PlusInstalled(int minor) {
    RegistryKey key = RegistryKey.LOCAL_MACHINE.openReadonly("SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full");
    try {
      boolean bool = (key.getIntValue("Release") >= GetV45PlusMinRelease(minor));
      if (key != null)
        key.close(); 
      return bool;
    } catch (Throwable throwable) {
      if (key != null)
        try {
          key.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  private static boolean isV40Installed() { key = RegistryKey.LOCAL_MACHINE.openReadonly("SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full");
    try {
      boolean bool = (key.getIntValue("Install") == 1);
      if (key != null)
        key.close(); 
      return bool;
    } catch (Throwable throwable) {
      if (key != null)
        try {
          key.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    }  }
  
  private static boolean isV35Installed() { key = RegistryKey.LOCAL_MACHINE.openReadonly("SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v3.5");
    try {
      boolean bool = (key.getIntValue("Install") == 1);
      if (key != null)
        key.close(); 
      return bool;
    } catch (Throwable throwable) {
      if (key != null)
        try {
          key.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    }  }
  
  private static boolean isV30Installed() { key = RegistryKey.LOCAL_MACHINE.openReadonly("SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v3.0\\Setup");
    try {
      boolean bool = (key.getIntValue("InstallSuccess") == 1);
      if (key != null)
        key.close(); 
      return bool;
    } catch (Throwable throwable) {
      if (key != null)
        try {
          key.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    }  }
  
  private static boolean isV20Installed() { key = RegistryKey.LOCAL_MACHINE.openReadonly("SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v2.0.50727");
    try {
      boolean bool = (key.getIntValue("Install") == 1);
      if (key != null)
        key.close(); 
      return bool;
    } catch (Throwable throwable) {
      if (key != null)
        try {
          key.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    }  }
  
  public static boolean isInstalled(int major, int minor, String targetMachine, IJIAuthInfo session) throws JIException, UnknownHostException {
    registry = JIWinRegFactory.getSingleTon().getWinreg(session, targetMachine, true);
    hklm = null;
    try {
      hklm = registry.winreg_OpenHKLM();
      if (major == 4 && minor >= 5)
        return isV45PlusInstalled(minor, registry, hklm); 
      if (major == 4 && minor == 0)
        return isV40Installed(registry, hklm); 
      if (major == 3 && minor == 5)
        return isV35Installed(registry, hklm); 
      if (major == 3 && minor == 0)
        return (isV35Installed(registry, hklm) || isV30Installed(registry, hklm)); 
      if (major == 2 && minor == 0)
        return (isV35Installed(registry, hklm) || isV30Installed(registry, hklm) || isV20Installed(registry, hklm)); 
      return false;
    } catch (JIException e) {
      if (e.getErrorCode() == 2)
        return false; 
      throw e;
    } finally {
      if (hklm != null)
        registry.winreg_CloseKey(hklm); 
      registry.closeConnection();
    } 
  }
  
  private static boolean isV45PlusInstalled(int minor, IJIWinReg registry, JIPolicyHandle hklm) throws JIException {
    key = null;
    try {
      key = registry.winreg_OpenKey(hklm, "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full", 131097);
      return (GetIntValue(registry, key, "Release") >= GetV45PlusMinRelease(minor));
    } finally {
      if (key != null)
        registry.winreg_CloseKey(key); 
    } 
  }
  
  private static boolean isV40Installed(IJIWinReg registry, JIPolicyHandle hklm) throws JIException {
    key = null;
    try {
      key = registry.winreg_OpenKey(hklm, "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full", 131097);
      return (GetIntValue(registry, key, "Install") == 1);
    } finally {
      if (key != null)
        registry.winreg_CloseKey(key); 
    } 
  }
  
  private static boolean isV35Installed(IJIWinReg registry, JIPolicyHandle hklm) throws JIException {
    key = null;
    try {
      key = registry.winreg_OpenKey(hklm, "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v3.5", 131097);
      return (GetIntValue(registry, key, "Install") == 1);
    } finally {
      if (key != null)
        registry.winreg_CloseKey(key); 
    } 
  }
  
  private static boolean isV30Installed(IJIWinReg registry, JIPolicyHandle hklm) throws JIException {
    key = null;
    try {
      key = registry.winreg_OpenKey(hklm, "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v3.0\\Setup", 131097);
      return (GetIntValue(registry, key, "InstallSuccess") == 1);
    } finally {
      if (key != null)
        registry.winreg_CloseKey(key); 
    } 
  }
  
  private static boolean isV20Installed(IJIWinReg registry, JIPolicyHandle hklm) throws JIException {
    key = null;
    try {
      key = registry.winreg_OpenKey(hklm, "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v2.0.50727", 131097);
      return (GetIntValue(registry, key, "Install") == 1);
    } finally {
      if (key != null)
        registry.winreg_CloseKey(key); 
    } 
  }
  
  private static int GetIntValue(IJIWinReg registry, JIPolicyHandle key, String name) throws JIException { return RegistryKey.convertBufferToInt((byte[])registry.winreg_QueryValue(key, name, 4)[1]); }
  
  private static int GetV45PlusMinRelease(int minor) {
    switch (minor) {
      case 5:
        return 378389;
      case 6:
        return 393295;
      case 7:
        return 460798;
      case 8:
        return 528040;
    } 
    return Integer.MAX_VALUE;
  }
}
