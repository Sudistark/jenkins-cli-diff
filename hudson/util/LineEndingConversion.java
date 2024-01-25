package hudson.util;

public class LineEndingConversion {
  public static String convertEOL(String input, EOLType type) {
    if (null == input || input.isEmpty())
      return input; 
    input = input.replace("\r\n", "\n");
    input = input.replace('\r', '\n');
    switch (null.$SwitchMap$hudson$util$LineEndingConversion$EOLType[type.ordinal()]) {
      case 1:
      case 2:
        return input.replace('\n', '\r');
      case 3:
      case 4:
        return input.replace("\n", "\r\n");
      case 5:
        return input.replace("\n", "\n\r");
    } 
    return input;
  }
}
