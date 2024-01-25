package hudson.model.labels;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

public class LabelExpressionLexer extends Lexer {
  protected static final DFA[] _decisionToDFA;
  
  protected static final PredictionContextCache _sharedContextCache;
  
  public static final int AND = 1;
  
  public static final int OR = 2;
  
  public static final int NOT = 3;
  
  public static final int IMPLIES = 4;
  
  public static final int IFF = 5;
  
  public static final int LPAREN = 6;
  
  public static final int RPAREN = 7;
  
  public static final int ATOM = 8;
  
  public static final int WS = 9;
  
  public static final int STRINGLITERAL = 10;
  
  public static String[] channelNames;
  
  public static String[] modeNames;
  
  public static final String[] ruleNames;
  
  private static final String[] _LITERAL_NAMES;
  
  private static final String[] _SYMBOLIC_NAMES;
  
  public static final Vocabulary VOCABULARY;
  
  @Deprecated
  public static final String[] tokenNames;
  
  public static final String _serializedATN = "\004\000\nE\006￿￿\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\002\004\007\004\002\005\007\005\002\006\007\006\002\007\007\007\002\b\007\b\002\t\007\t\002\n\007\n\001\000\001\000\001\000\001\001\001\001\001\001\001\002\001\002\001\003\001\003\001\003\001\004\001\004\001\004\001\004\001\005\001\005\001\006\001\006\001\007\001\007\001\b\001\b\001\b\004\b0\b\b\013\b\f\b1\001\t\004\t5\b\t\013\t\f\t6\001\t\001\t\001\n\001\n\001\n\001\n\005\n?\b\n\n\n\f\nB\t\n\001\n\001\n\000\000\013\001\001\003\002\005\003\007\004\t\005\013\006\r\007\017\000\021\b\023\t\025\n\001\000\004\007\000\t\t \"&)--<<>>||\002\000\t\t  \b\000\"\"''\\\\bbffnnrrtt\004\000\n\n\r\r\"\"\\\\H\000\001\001\000\000\000\000\003\001\000\000\000\000\005\001\000\000\000\000\007\001\000\000\000\000\t\001\000\000\000\000\013\001\000\000\000\000\r\001\000\000\000\000\021\001\000\000\000\000\023\001\000\000\000\000\025\001\000\000\000\001\027\001\000\000\000\003\032\001\000\000\000\005\035\001\000\000\000\007\037\001\000\000\000\t\"\001\000\000\000\013&\001\000\000\000\r(\001\000\000\000\017*\001\000\000\000\021/\001\000\000\000\0234\001\000\000\000\025:\001\000\000\000\027\030\005&\000\000\030\031\005&\000\000\031\002\001\000\000\000\032\033\005|\000\000\033\034\005|\000\000\034\004\001\000\000\000\035\036\005!\000\000\036\006\001\000\000\000\037 \005-\000\000 !\005>\000\000!\b\001\000\000\000\"#\005<\000\000#$\005-\000\000$%\005>\000\000%\n\001\000\000\000&'\005(\000\000'\f\001\000\000\000()\005)\000\000)\016\001\000\000\000*+\b\000\000\000+\020\001\000\000\000,-\004\b\000\000-0\005-\000\000.0\003\017\007\000/,\001\000\000\000/.\001\000\000\00001\001\000\000\0001/\001\000\000\00012\001\000\000\0002\022\001\000\000\00035\007\001\000\00043\001\000\000\00056\001\000\000\00064\001\000\000\00067\001\000\000\00078\001\000\000\00089\006\t\000\0009\024\001\000\000\000:@\005\"\000\000;<\005\\\000\000<?\007\002\000\000=?\b\003\000\000>;\001\000\000\000>=\001\000\000\000?B\001\000\000\000@>\001\000\000\000@A\001\000\000\000AC\001\000\000\000B@\001\000\000\000CD\005\"\000\000D\026\001\000\000\000\006\000/16>@\001\006\000\000";
  
  public static final ATN _ATN;
  
  static  {
    RuntimeMetaData.checkVersion("4.13.1", "4.13.1");
    _sharedContextCache = new PredictionContextCache();
    channelNames = new String[] { "DEFAULT_TOKEN_CHANNEL", "HIDDEN" };
    modeNames = new String[] { "DEFAULT_MODE" };
    ruleNames = makeRuleNames();
    _LITERAL_NAMES = makeLiteralNames();
    _SYMBOLIC_NAMES = makeSymbolicNames();
    VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);
    tokenNames = new String[_SYMBOLIC_NAMES.length];
    for (i = 0; i < tokenNames.length; i++) {
      tokenNames[i] = VOCABULARY.getLiteralName(i);
      if (tokenNames[i] == null)
        tokenNames[i] = VOCABULARY.getSymbolicName(i); 
      if (tokenNames[i] == null)
        tokenNames[i] = "<INVALID>"; 
    } 
    _ATN = (new ATNDeserializer()).deserialize("\004\000\nE\006￿￿\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\002\004\007\004\002\005\007\005\002\006\007\006\002\007\007\007\002\b\007\b\002\t\007\t\002\n\007\n\001\000\001\000\001\000\001\001\001\001\001\001\001\002\001\002\001\003\001\003\001\003\001\004\001\004\001\004\001\004\001\005\001\005\001\006\001\006\001\007\001\007\001\b\001\b\001\b\004\b0\b\b\013\b\f\b1\001\t\004\t5\b\t\013\t\f\t6\001\t\001\t\001\n\001\n\001\n\001\n\005\n?\b\n\n\n\f\nB\t\n\001\n\001\n\000\000\013\001\001\003\002\005\003\007\004\t\005\013\006\r\007\017\000\021\b\023\t\025\n\001\000\004\007\000\t\t \"&)--<<>>||\002\000\t\t  \b\000\"\"''\\\\bbffnnrrtt\004\000\n\n\r\r\"\"\\\\H\000\001\001\000\000\000\000\003\001\000\000\000\000\005\001\000\000\000\000\007\001\000\000\000\000\t\001\000\000\000\000\013\001\000\000\000\000\r\001\000\000\000\000\021\001\000\000\000\000\023\001\000\000\000\000\025\001\000\000\000\001\027\001\000\000\000\003\032\001\000\000\000\005\035\001\000\000\000\007\037\001\000\000\000\t\"\001\000\000\000\013&\001\000\000\000\r(\001\000\000\000\017*\001\000\000\000\021/\001\000\000\000\0234\001\000\000\000\025:\001\000\000\000\027\030\005&\000\000\030\031\005&\000\000\031\002\001\000\000\000\032\033\005|\000\000\033\034\005|\000\000\034\004\001\000\000\000\035\036\005!\000\000\036\006\001\000\000\000\037 \005-\000\000 !\005>\000\000!\b\001\000\000\000\"#\005<\000\000#$\005-\000\000$%\005>\000\000%\n\001\000\000\000&'\005(\000\000'\f\001\000\000\000()\005)\000\000)\016\001\000\000\000*+\b\000\000\000+\020\001\000\000\000,-\004\b\000\000-0\005-\000\000.0\003\017\007\000/,\001\000\000\000/.\001\000\000\00001\001\000\000\0001/\001\000\000\00012\001\000\000\0002\022\001\000\000\00035\007\001\000\00043\001\000\000\00056\001\000\000\00064\001\000\000\00067\001\000\000\00078\001\000\000\00089\006\t\000\0009\024\001\000\000\000:@\005\"\000\000;<\005\\\000\000<?\007\002\000\000=?\b\003\000\000>;\001\000\000\000>=\001\000\000\000?B\001\000\000\000@>\001\000\000\000@A\001\000\000\000AC\001\000\000\000B@\001\000\000\000CD\005\"\000\000D\026\001\000\000\000\006\000/16>@\001\006\000\000".toCharArray());
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (i = 0; i < _ATN.getNumberOfDecisions(); i++)
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i); 
  }
  
  private static String[] makeRuleNames() { return new String[] { 
        "AND", "OR", "NOT", "IMPLIES", "IFF", "LPAREN", "RPAREN", "IDENTIFIER_PART", "ATOM", "WS", 
        "STRINGLITERAL" }; }
  
  private static String[] makeLiteralNames() { return new String[] { null, "'&&'", "'||'", "'!'", "'->'", "'<->'", "'('", "')'" }; }
  
  private static String[] makeSymbolicNames() { return new String[] { 
        null, "AND", "OR", "NOT", "IMPLIES", "IFF", "LPAREN", "RPAREN", "ATOM", "WS", 
        "STRINGLITERAL" }; }
  
  @Deprecated
  public String[] getTokenNames() { return tokenNames; }
  
  public Vocabulary getVocabulary() { return VOCABULARY; }
  
  public LabelExpressionLexer(CharStream input) {
    super(input);
    this._interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }
  
  public String getGrammarFileName() { return "LabelExpressionLexer.g4"; }
  
  public String[] getRuleNames() { return ruleNames; }
  
  public String getSerializedATN() { return "\004\000\nE\006￿￿\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\002\004\007\004\002\005\007\005\002\006\007\006\002\007\007\007\002\b\007\b\002\t\007\t\002\n\007\n\001\000\001\000\001\000\001\001\001\001\001\001\001\002\001\002\001\003\001\003\001\003\001\004\001\004\001\004\001\004\001\005\001\005\001\006\001\006\001\007\001\007\001\b\001\b\001\b\004\b0\b\b\013\b\f\b1\001\t\004\t5\b\t\013\t\f\t6\001\t\001\t\001\n\001\n\001\n\001\n\005\n?\b\n\n\n\f\nB\t\n\001\n\001\n\000\000\013\001\001\003\002\005\003\007\004\t\005\013\006\r\007\017\000\021\b\023\t\025\n\001\000\004\007\000\t\t \"&)--<<>>||\002\000\t\t  \b\000\"\"''\\\\bbffnnrrtt\004\000\n\n\r\r\"\"\\\\H\000\001\001\000\000\000\000\003\001\000\000\000\000\005\001\000\000\000\000\007\001\000\000\000\000\t\001\000\000\000\000\013\001\000\000\000\000\r\001\000\000\000\000\021\001\000\000\000\000\023\001\000\000\000\000\025\001\000\000\000\001\027\001\000\000\000\003\032\001\000\000\000\005\035\001\000\000\000\007\037\001\000\000\000\t\"\001\000\000\000\013&\001\000\000\000\r(\001\000\000\000\017*\001\000\000\000\021/\001\000\000\000\0234\001\000\000\000\025:\001\000\000\000\027\030\005&\000\000\030\031\005&\000\000\031\002\001\000\000\000\032\033\005|\000\000\033\034\005|\000\000\034\004\001\000\000\000\035\036\005!\000\000\036\006\001\000\000\000\037 \005-\000\000 !\005>\000\000!\b\001\000\000\000\"#\005<\000\000#$\005-\000\000$%\005>\000\000%\n\001\000\000\000&'\005(\000\000'\f\001\000\000\000()\005)\000\000)\016\001\000\000\000*+\b\000\000\000+\020\001\000\000\000,-\004\b\000\000-0\005-\000\000.0\003\017\007\000/,\001\000\000\000/.\001\000\000\00001\001\000\000\0001/\001\000\000\00012\001\000\000\0002\022\001\000\000\00035\007\001\000\00043\001\000\000\00056\001\000\000\00064\001\000\000\00067\001\000\000\00078\001\000\000\00089\006\t\000\0009\024\001\000\000\000:@\005\"\000\000;<\005\\\000\000<?\007\002\000\000=?\b\003\000\000>;\001\000\000\000>=\001\000\000\000?B\001\000\000\000@>\001\000\000\000@A\001\000\000\000AC\001\000\000\000B@\001\000\000\000CD\005\"\000\000D\026\001\000\000\000\006\000/16>@\001\006\000\000"; }
  
  public String[] getChannelNames() { return channelNames; }
  
  public String[] getModeNames() { return modeNames; }
  
  public ATN getATN() { return _ATN; }
  
  public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
    switch (ruleIndex) {
      case 8:
        return ATOM_sempred(_localctx, predIndex);
    } 
    return true;
  }
  
  private boolean ATOM_sempred(RuleContext _localctx, int predIndex) {
    switch (predIndex) {
      case 0:
        return (this._input.LA(2) != 62);
    } 
    return true;
  }
}
