package jenkins.org.apache.commons.validator.routines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class InetAddressValidator implements Serializable {
  private static final int IPV4_MAX_OCTET_VALUE = 255;
  
  private static final int MAX_UNSIGNED_SHORT = 65535;
  
  private static final int BASE_16 = 16;
  
  private static final long serialVersionUID = -919201640201914789L;
  
  private static final String IPV4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
  
  private static final int IPV6_MAX_HEX_GROUPS = 8;
  
  private static final int IPV6_MAX_HEX_DIGITS_PER_GROUP = 4;
  
  private static final InetAddressValidator VALIDATOR = new InetAddressValidator();
  
  private final RegexValidator ipv4Validator = new RegexValidator("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
  
  public static InetAddressValidator getInstance() { return VALIDATOR; }
  
  public boolean isValid(String inetAddress) { return (isValidInet4Address(inetAddress) || isValidInet6Address(inetAddress)); }
  
  public boolean isValidInet4Address(String inet4Address) {
    String[] groups = this.ipv4Validator.match(inet4Address);
    if (groups == null)
      return false; 
    for (String ipSegment : groups) {
      int iIpSegment;
      if (ipSegment == null || ipSegment.isEmpty())
        return false; 
      try {
        iIpSegment = Integer.parseInt(ipSegment);
      } catch (NumberFormatException e) {
        return false;
      } 
      if (iIpSegment > 255)
        return false; 
      if (ipSegment.length() > 1 && ipSegment.startsWith("0"))
        return false; 
    } 
    return true;
  }
  
  public boolean isValidInet6Address(String inet6Address) {
    String[] parts = inet6Address.split("/", -1);
    if (parts.length > 2)
      return false; 
    if (parts.length == 2)
      if (parts[1].matches("\\d{1,3}")) {
        int bits = Integer.parseInt(parts[1]);
        if (bits < 0 || bits > 128)
          return false; 
      } else {
        return false;
      }  
    parts = parts[0].split("%", -1);
    if (parts.length > 2)
      return false; 
    if (parts.length == 2)
      if (!parts[1].matches("[^\\s/%]+"))
        return false;  
    inet6Address = parts[0];
    boolean containsCompressedZeroes = inet6Address.contains("::");
    if (containsCompressedZeroes && inet6Address.indexOf("::") != inet6Address.lastIndexOf("::"))
      return false; 
    if ((inet6Address.startsWith(":") && !inet6Address.startsWith("::")) || (inet6Address
      .endsWith(":") && !inet6Address.endsWith("::")))
      return false; 
    String[] octets = inet6Address.split(":");
    if (containsCompressedZeroes) {
      List<String> octetList = new ArrayList<String>(Arrays.asList(octets));
      if (inet6Address.endsWith("::")) {
        octetList.add("");
      } else if (inet6Address.startsWith("::") && !octetList.isEmpty()) {
        octetList.remove(0);
      } 
      octets = (String[])octetList.toArray(new String[0]);
    } 
    if (octets.length > 8)
      return false; 
    int validOctets = 0;
    int emptyOctets = 0;
    int index = 0;
    while (true) {
      if (index < octets.length) {
        String octet = octets[index];
        if (octet.isEmpty()) {
          emptyOctets++;
          if (emptyOctets > 1)
            return false; 
        } else {
          emptyOctets = 0;
          if (index == octets.length - 1 && octet.contains(".")) {
            if (!isValidInet4Address(octet))
              return false; 
            validOctets += 2;
          } else {
            int octetInt;
            if (octet.length() > 4)
              return false; 
            try {
              octetInt = Integer.parseInt(octet, 16);
            } catch (NumberFormatException e) {
              return false;
            } 
            if (octetInt < 0 || octetInt > 65535)
              return false; 
            validOctets++;
          } 
          index++;
        } 
      } else {
        break;
      } 
      validOctets++;
    } 
    if (validOctets > 8 || (validOctets < 8 && !containsCompressedZeroes))
      return false; 
    return true;
  }
}
