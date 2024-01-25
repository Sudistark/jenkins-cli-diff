package hudson.util.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = {"MS_OOI_PKGPROTECT"}, justification = "for backward compatibility")
public interface Advapi32 extends StdCallLibrary {
  public static final Advapi32 INSTANCE = (Advapi32)Native.load("Advapi32", Advapi32.class, Options.UNICODE_OPTIONS);
  
  boolean GetUserName(char[] paramArrayOfChar, IntByReference paramIntByReference);
  
  boolean LookupAccountName(String paramString1, String paramString2, byte[] paramArrayOfByte, IntByReference paramIntByReference1, char[] paramArrayOfChar, IntByReference paramIntByReference2, PointerByReference paramPointerByReference);
  
  boolean LookupAccountSid(String paramString, byte[] paramArrayOfByte, char[] paramArrayOfChar1, IntByReference paramIntByReference1, char[] paramArrayOfChar2, IntByReference paramIntByReference2, PointerByReference paramPointerByReference);
  
  boolean ConvertSidToStringSid(byte[] paramArrayOfByte, PointerByReference paramPointerByReference);
  
  boolean ConvertStringSidToSid(String paramString, PointerByReference paramPointerByReference);
  
  Pointer OpenSCManager(String paramString, WString paramWString, int paramInt);
  
  boolean CloseServiceHandle(Pointer paramPointer);
  
  Pointer OpenService(Pointer paramPointer, String paramString, int paramInt);
  
  boolean StartService(Pointer paramPointer, int paramInt, char[] paramArrayOfChar);
  
  boolean ControlService(Pointer paramPointer, int paramInt, SERVICE_STATUS paramSERVICE_STATUS);
  
  boolean StartServiceCtrlDispatcher(Structure[] paramArrayOfStructure);
  
  Pointer RegisterServiceCtrlHandler(String paramString, Handler paramHandler);
  
  Pointer RegisterServiceCtrlHandlerEx(String paramString, HandlerEx paramHandlerEx, Pointer paramPointer);
  
  boolean SetServiceStatus(Pointer paramPointer, SERVICE_STATUS paramSERVICE_STATUS);
  
  Pointer CreateService(Pointer paramPointer, String paramString1, String paramString2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, String paramString3, String paramString4, IntByReference paramIntByReference, String paramString5, String paramString6, String paramString7);
  
  boolean DeleteService(Pointer paramPointer);
  
  boolean ChangeServiceConfig2(Pointer paramPointer, int paramInt, ChangeServiceConfig2Info paramChangeServiceConfig2Info);
  
  int RegOpenKeyEx(int paramInt1, String paramString, int paramInt2, int paramInt3, IntByReference paramIntByReference);
  
  int RegQueryValueEx(int paramInt, String paramString, IntByReference paramIntByReference1, IntByReference paramIntByReference2, byte[] paramArrayOfByte, IntByReference paramIntByReference3);
  
  int RegCloseKey(int paramInt);
  
  int RegDeleteValue(int paramInt, String paramString);
  
  int RegSetValueEx(int paramInt1, String paramString, int paramInt2, int paramInt3, byte[] paramArrayOfByte, int paramInt4);
  
  int RegCreateKeyEx(int paramInt1, String paramString1, int paramInt2, String paramString2, int paramInt3, int paramInt4, WINBASE.SECURITY_ATTRIBUTES paramSECURITY_ATTRIBUTES, IntByReference paramIntByReference1, IntByReference paramIntByReference2);
  
  int RegDeleteKey(int paramInt, String paramString);
  
  int RegEnumKeyEx(int paramInt1, int paramInt2, char[] paramArrayOfChar1, IntByReference paramIntByReference1, IntByReference paramIntByReference2, char[] paramArrayOfChar2, IntByReference paramIntByReference3, WINBASE.FILETIME paramFILETIME);
  
  int RegEnumValue(int paramInt1, int paramInt2, char[] paramArrayOfChar, IntByReference paramIntByReference1, IntByReference paramIntByReference2, IntByReference paramIntByReference3, byte[] paramArrayOfByte, IntByReference paramIntByReference4);
}
