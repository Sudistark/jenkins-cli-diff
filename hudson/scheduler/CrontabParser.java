package hudson.scheduler;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

public class CrontabParser extends BaseParser {
  protected static final DFA[] _decisionToDFA;
  
  protected static final PredictionContextCache _sharedContextCache;
  
  public static final int TOKEN = 1;
  
  public static final int WS = 2;
  
  public static final int MINUS = 3;
  
  public static final int STAR = 4;
  
  public static final int DIV = 5;
  
  public static final int OR = 6;
  
  public static final int AT = 7;
  
  public static final int H = 8;
  
  public static final int LPAREN = 9;
  
  public static final int RPAREN = 10;
  
  public static final int YEARLY = 11;
  
  public static final int ANNUALLY = 12;
  
  public static final int MONTHLY = 13;
  
  public static final int WEEKLY = 14;
  
  public static final int DAILY = 15;
  
  public static final int MIDNIGHT = 16;
  
  public static final int HOURLY = 17;
  
  public static final int RULE_startRule = 0;
  
  public static final int RULE_expr = 1;
  
  public static final int RULE_term = 2;
  
  public static final int RULE_token = 3;
  
  public static final String[] ruleNames;
  
  private static final String[] _LITERAL_NAMES;
  
  private static final String[] _SYMBOLIC_NAMES;
  
  public static final Vocabulary VOCABULARY;
  
  @Deprecated
  public static final String[] tokenNames;
  
  public static final String _serializedATN = "\004\001\021k\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\003\000(\b\000\003\000*\b\000\001\001\001\001\001\001\001\001\001\001\001\001\003\0012\b\001\001\001\001\001\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002?\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002K\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002Z\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002c\b\002\001\002\003\002f\b\002\001\003\001\003\001\003\001\003\000\000\004\000\002\004\006\000\000v\000)\001\000\000\000\002+\001\000\000\000\004e\001\000\000\000\006g\001\000\000\000\b\t\003\002\001\000\t\n\006\000￿￿\000\n\013\005\002\000\000\013\f\003\002\001\000\f\r\006\000￿￿\000\r\016\005\002\000\000\016\017\003\002\001\000\017\020\006\000￿￿\000\020\021\005\002\000\000\021\022\003\002\001\000\022\023\006\000￿￿\000\023\024\005\002\000\000\024\025\003\002\001\000\025\026\006\000￿￿\000\026\027\005\000\000\001\027*\001\000\000\000\030'\005\007\000\000\031\032\005\013\000\000\032(\006\000￿￿\000\033\034\005\f\000\000\034(\006\000￿￿\000\035\036\005\r\000\000\036(\006\000￿￿\000\037 \005\016\000\000 (\006\000￿￿\000!\"\005\017\000\000\"(\006\000￿￿\000#$\005\020\000\000$(\006\000￿￿\000%&\005\021\000\000&(\006\000￿￿\000'\031\001\000\000\000'\033\001\000\000\000'\035\001\000\000\000'\037\001\000\000\000'!\001\000\000\000'#\001\000\000\000'%\001\000\000\000(*\001\000\000\000)\b\001\000\000\000)\030\001\000\000\000*\001\001\000\000\000+,\003\004\002\000,1\006\001￿￿\000-.\005\006\000\000./\003\002\001\000/0\006\001￿￿\00002\001\000\000\0001-\001\000\000\00012\001\000\000\00023\001\000\000\00034\006\001￿￿\0004\003\001\000\000\00056\003\006\003\00067\006\002￿￿\00078\005\003\000\00089\003\006\003\0009>\006\002￿￿\000:;\005\005\000\000;<\003\006\003\000<=\006\002￿￿\000=?\001\000\000\000>:\001\000\000\000>?\001\000\000\000?@\001\000\000\000@A\006\002￿￿\000Af\001\000\000\000BC\003\006\003\000CD\006\002￿￿\000Df\001\000\000\000EJ\005\004\000\000FG\005\005\000\000GH\003\006\003\000HI\006\002￿￿\000IK\001\000\000\000JF\001\000\000\000JK\001\000\000\000KL\001\000\000\000Lf\006\002￿￿\000MN\005\b\000\000NO\005\t\000\000OP\003\006\003\000PQ\006\002￿￿\000QR\005\003\000\000RS\003\006\003\000ST\006\002￿￿\000TY\005\n\000\000UV\005\005\000\000VW\003\006\003\000WX\006\002￿￿\000XZ\001\000\000\000YU\001\000\000\000YZ\001\000\000\000Z[\001\000\000\000[\\\006\002￿￿\000\\f\001\000\000\000]b\005\b\000\000^_\005\005\000\000_`\003\006\003\000`a\006\002￿￿\000ac\001\000\000\000b^\001\000\000\000bc\001\000\000\000cd\001\000\000\000df\006\002￿￿\000e5\001\000\000\000eB\001\000\000\000eE\001\000\000\000eM\001\000\000\000e]\001\000\000\000f\005\001\000\000\000gh\005\001\000\000hi\006\003￿￿\000i\007\001\000\000\000\b')1>JYbe";
  
  public static final ATN _ATN;
  
  static  {
    RuntimeMetaData.checkVersion("4.13.1", "4.13.1");
    _sharedContextCache = new PredictionContextCache();
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
    _ATN = (new ATNDeserializer()).deserialize("\004\001\021k\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\003\000(\b\000\003\000*\b\000\001\001\001\001\001\001\001\001\001\001\001\001\003\0012\b\001\001\001\001\001\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002?\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002K\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002Z\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002c\b\002\001\002\003\002f\b\002\001\003\001\003\001\003\001\003\000\000\004\000\002\004\006\000\000v\000)\001\000\000\000\002+\001\000\000\000\004e\001\000\000\000\006g\001\000\000\000\b\t\003\002\001\000\t\n\006\000￿￿\000\n\013\005\002\000\000\013\f\003\002\001\000\f\r\006\000￿￿\000\r\016\005\002\000\000\016\017\003\002\001\000\017\020\006\000￿￿\000\020\021\005\002\000\000\021\022\003\002\001\000\022\023\006\000￿￿\000\023\024\005\002\000\000\024\025\003\002\001\000\025\026\006\000￿￿\000\026\027\005\000\000\001\027*\001\000\000\000\030'\005\007\000\000\031\032\005\013\000\000\032(\006\000￿￿\000\033\034\005\f\000\000\034(\006\000￿￿\000\035\036\005\r\000\000\036(\006\000￿￿\000\037 \005\016\000\000 (\006\000￿￿\000!\"\005\017\000\000\"(\006\000￿￿\000#$\005\020\000\000$(\006\000￿￿\000%&\005\021\000\000&(\006\000￿￿\000'\031\001\000\000\000'\033\001\000\000\000'\035\001\000\000\000'\037\001\000\000\000'!\001\000\000\000'#\001\000\000\000'%\001\000\000\000(*\001\000\000\000)\b\001\000\000\000)\030\001\000\000\000*\001\001\000\000\000+,\003\004\002\000,1\006\001￿￿\000-.\005\006\000\000./\003\002\001\000/0\006\001￿￿\00002\001\000\000\0001-\001\000\000\00012\001\000\000\00023\001\000\000\00034\006\001￿￿\0004\003\001\000\000\00056\003\006\003\00067\006\002￿￿\00078\005\003\000\00089\003\006\003\0009>\006\002￿￿\000:;\005\005\000\000;<\003\006\003\000<=\006\002￿￿\000=?\001\000\000\000>:\001\000\000\000>?\001\000\000\000?@\001\000\000\000@A\006\002￿￿\000Af\001\000\000\000BC\003\006\003\000CD\006\002￿￿\000Df\001\000\000\000EJ\005\004\000\000FG\005\005\000\000GH\003\006\003\000HI\006\002￿￿\000IK\001\000\000\000JF\001\000\000\000JK\001\000\000\000KL\001\000\000\000Lf\006\002￿￿\000MN\005\b\000\000NO\005\t\000\000OP\003\006\003\000PQ\006\002￿￿\000QR\005\003\000\000RS\003\006\003\000ST\006\002￿￿\000TY\005\n\000\000UV\005\005\000\000VW\003\006\003\000WX\006\002￿￿\000XZ\001\000\000\000YU\001\000\000\000YZ\001\000\000\000Z[\001\000\000\000[\\\006\002￿￿\000\\f\001\000\000\000]b\005\b\000\000^_\005\005\000\000_`\003\006\003\000`a\006\002￿￿\000ac\001\000\000\000b^\001\000\000\000bc\001\000\000\000cd\001\000\000\000df\006\002￿￿\000e5\001\000\000\000eB\001\000\000\000eE\001\000\000\000eM\001\000\000\000e]\001\000\000\000f\005\001\000\000\000gh\005\001\000\000hi\006\003￿￿\000i\007\001\000\000\000\b')1>JYbe".toCharArray());
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (i = 0; i < _ATN.getNumberOfDecisions(); i++)
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i); 
  }
  
  private static String[] makeRuleNames() { return new String[] { "startRule", "expr", "term", "token" }; }
  
  private static String[] makeLiteralNames() { return new String[] { 
        null, null, null, "'-'", "'*'", "'/'", "','", "'@'", "'H'", "'('", 
        "')'", "'yearly'", "'annually'", "'monthly'", "'weekly'", "'daily'", "'midnight'", "'hourly'" }; }
  
  private static String[] makeSymbolicNames() { return new String[] { 
        null, "TOKEN", "WS", "MINUS", "STAR", "DIV", "OR", "AT", "H", "LPAREN", 
        "RPAREN", "YEARLY", "ANNUALLY", "MONTHLY", "WEEKLY", "DAILY", "MIDNIGHT", "HOURLY" }; }
  
  @Deprecated
  public String[] getTokenNames() { return tokenNames; }
  
  public Vocabulary getVocabulary() { return VOCABULARY; }
  
  public String getGrammarFileName() { return "CrontabParser.g4"; }
  
  public String[] getRuleNames() { return ruleNames; }
  
  public String getSerializedATN() { return "\004\001\021k\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\001\000\003\000(\b\000\003\000*\b\000\001\001\001\001\001\001\001\001\001\001\001\001\003\0012\b\001\001\001\001\001\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002?\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002K\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002Z\b\002\001\002\001\002\001\002\001\002\001\002\001\002\001\002\003\002c\b\002\001\002\003\002f\b\002\001\003\001\003\001\003\001\003\000\000\004\000\002\004\006\000\000v\000)\001\000\000\000\002+\001\000\000\000\004e\001\000\000\000\006g\001\000\000\000\b\t\003\002\001\000\t\n\006\000￿￿\000\n\013\005\002\000\000\013\f\003\002\001\000\f\r\006\000￿￿\000\r\016\005\002\000\000\016\017\003\002\001\000\017\020\006\000￿￿\000\020\021\005\002\000\000\021\022\003\002\001\000\022\023\006\000￿￿\000\023\024\005\002\000\000\024\025\003\002\001\000\025\026\006\000￿￿\000\026\027\005\000\000\001\027*\001\000\000\000\030'\005\007\000\000\031\032\005\013\000\000\032(\006\000￿￿\000\033\034\005\f\000\000\034(\006\000￿￿\000\035\036\005\r\000\000\036(\006\000￿￿\000\037 \005\016\000\000 (\006\000￿￿\000!\"\005\017\000\000\"(\006\000￿￿\000#$\005\020\000\000$(\006\000￿￿\000%&\005\021\000\000&(\006\000￿￿\000'\031\001\000\000\000'\033\001\000\000\000'\035\001\000\000\000'\037\001\000\000\000'!\001\000\000\000'#\001\000\000\000'%\001\000\000\000(*\001\000\000\000)\b\001\000\000\000)\030\001\000\000\000*\001\001\000\000\000+,\003\004\002\000,1\006\001￿￿\000-.\005\006\000\000./\003\002\001\000/0\006\001￿￿\00002\001\000\000\0001-\001\000\000\00012\001\000\000\00023\001\000\000\00034\006\001￿￿\0004\003\001\000\000\00056\003\006\003\00067\006\002￿￿\00078\005\003\000\00089\003\006\003\0009>\006\002￿￿\000:;\005\005\000\000;<\003\006\003\000<=\006\002￿￿\000=?\001\000\000\000>:\001\000\000\000>?\001\000\000\000?@\001\000\000\000@A\006\002￿￿\000Af\001\000\000\000BC\003\006\003\000CD\006\002￿￿\000Df\001\000\000\000EJ\005\004\000\000FG\005\005\000\000GH\003\006\003\000HI\006\002￿￿\000IK\001\000\000\000JF\001\000\000\000JK\001\000\000\000KL\001\000\000\000Lf\006\002￿￿\000MN\005\b\000\000NO\005\t\000\000OP\003\006\003\000PQ\006\002￿￿\000QR\005\003\000\000RS\003\006\003\000ST\006\002￿￿\000TY\005\n\000\000UV\005\005\000\000VW\003\006\003\000WX\006\002￿￿\000XZ\001\000\000\000YU\001\000\000\000YZ\001\000\000\000Z[\001\000\000\000[\\\006\002￿￿\000\\f\001\000\000\000]b\005\b\000\000^_\005\005\000\000_`\003\006\003\000`a\006\002￿￿\000ac\001\000\000\000b^\001\000\000\000bc\001\000\000\000cd\001\000\000\000df\006\002￿￿\000e5\001\000\000\000eB\001\000\000\000eE\001\000\000\000eM\001\000\000\000e]\001\000\000\000f\005\001\000\000\000gh\005\001\000\000hi\006\003￿￿\000i\007\001\000\000\000\b')1>JYbe"; }
  
  public ATN getATN() { return _ATN; }
  
  public CrontabParser(TokenStream input) {
    super(input);
    this._interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }
  
  public final StartRuleContext startRule(CronTab table) throws RecognitionException {
    StartRuleContext _localctx = new StartRuleContext(this._ctx, getState(), table);
    enterRule(_localctx, 0, 0);
    try {
      setState(41);
      this._errHandler.sync(this);
      switch (this._input.LA(1)) {
        case 1:
        case 4:
        case 8:
          enterOuterAlt(_localctx, 1);
          setState(8);
          _localctx.expr = expr(0);
          _localctx.table.bits[0] = _localctx.expr.bits;
          setState(10);
          match(2);
          setState(11);
          _localctx.expr = expr(1);
          _localctx.table.bits[1] = _localctx.expr.bits;
          setState(13);
          match(2);
          setState(14);
          _localctx.expr = expr(2);
          _localctx.table.bits[2] = _localctx.expr.bits;
          setState(16);
          match(2);
          setState(17);
          _localctx.expr = expr(3);
          _localctx.table.bits[3] = _localctx.expr.bits;
          setState(19);
          match(2);
          setState(20);
          _localctx.expr = expr(4);
          _localctx.table.dayOfWeek = (int)_localctx.expr.bits;
          setState(22);
          match(-1);
          break;
        case 7:
          enterOuterAlt(_localctx, 2);
          setState(24);
          match(7);
          setState(39);
          this._errHandler.sync(this);
          switch (this._input.LA(1)) {
            case 11:
              setState(25);
              match(11);
              _localctx.table.set("H H H H *", getHashForTokens());
              break;
            case 12:
              setState(27);
              match(12);
              _localctx.table.set("H H H H *", getHashForTokens());
              break;
            case 13:
              setState(29);
              match(13);
              _localctx.table.set("H H H * *", getHashForTokens());
              break;
            case 14:
              setState(31);
              match(14);
              _localctx.table.set("H H * * H", getHashForTokens());
              break;
            case 15:
              setState(33);
              match(15);
              _localctx.table.set("H H * * *", getHashForTokens());
              break;
            case 16:
              setState(35);
              match(16);
              _localctx.table.set("H H(0-2) * * *", getHashForTokens());
              break;
            case 17:
              setState(37);
              match(17);
              _localctx.table.set("H * * * *", getHashForTokens());
              break;
          } 
          throw new NoViableAltException(this);
        default:
          throw new NoViableAltException(this);
      } 
    } catch (RecognitionException re) {
      _localctx.exception = re;
      this._errHandler.reportError(this, re);
      this._errHandler.recover(this, re);
    } finally {
      exitRule();
    } 
    return _localctx;
  }
  
  public final ExprContext expr(int field) throws RecognitionException {
    ExprContext _localctx = new ExprContext(this._ctx, getState(), field);
    enterRule(_localctx, 2, 1);
    try {
      enterOuterAlt(_localctx, 1);
      setState(43);
      _localctx.term = term(field);
      _localctx.lhs = _localctx.term.bits;
      setState(49);
      this._errHandler.sync(this);
      int _la = this._input.LA(1);
      if (_la == 6) {
        setState(45);
        match(6);
        setState(46);
        _localctx.expr = expr(field);
        _localctx.rhs = _localctx.expr.bits;
      } 
      _localctx.bits = _localctx.lhs | _localctx.rhs;
    } catch (RecognitionException re) {
      _localctx.exception = re;
      this._errHandler.reportError(this, re);
      this._errHandler.recover(this, re);
    } finally {
      exitRule();
    } 
    return _localctx;
  }
  
  public final TermContext term(int field) throws RecognitionException {
    TermContext _localctx = new TermContext(this._ctx, getState(), field);
    enterRule(_localctx, 4, 2);
    try {
      int _la, _la, _la, _la;
      setState(101);
      this._errHandler.sync(this);
      switch (((ParserATNSimulator)getInterpreter()).adaptivePredict(this._input, 7, this._ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
          setState(53);
          _localctx.token = token();
          _localctx.s = _localctx.token.value;
          setState(55);
          match(3);
          setState(56);
          _localctx.token = token();
          _localctx.e = _localctx.token.value;
          setState(62);
          this._errHandler.sync(this);
          _la = this._input.LA(1);
          if (_la == 5) {
            setState(58);
            match(5);
            setState(59);
            _localctx.token = token();
            _localctx.d = _localctx.token.value;
          } 
          _localctx.bits = doRange(_localctx.s, _localctx.e, _localctx.d, _localctx.field);
          break;
        case 2:
          enterOuterAlt(_localctx, 2);
          setState(66);
          _localctx.token = token();
          rangeCheck(_localctx.token.value, _localctx.field);
          _localctx.bits = 1L << _localctx.token.value;
          break;
        case 3:
          enterOuterAlt(_localctx, 3);
          setState(69);
          match(4);
          setState(74);
          this._errHandler.sync(this);
          _la = this._input.LA(1);
          if (_la == 5) {
            setState(70);
            match(5);
            setState(71);
            _localctx.token = token();
            _localctx.d = _localctx.token.value;
          } 
          _localctx.bits = doRange(_localctx.d, _localctx.field);
          break;
        case 4:
          enterOuterAlt(_localctx, 4);
          setState(77);
          match(8);
          setState(78);
          match(9);
          setState(79);
          _localctx.token = token();
          _localctx.s = _localctx.token.value;
          setState(81);
          match(3);
          setState(82);
          _localctx.token = token();
          _localctx.e = _localctx.token.value;
          setState(84);
          match(10);
          setState(89);
          this._errHandler.sync(this);
          _la = this._input.LA(1);
          if (_la == 5) {
            setState(85);
            match(5);
            setState(86);
            _localctx.token = token();
            _localctx.d = _localctx.token.value;
          } 
          _localctx.bits = doHash(_localctx.s, _localctx.e, _localctx.d, _localctx.field);
          break;
        case 5:
          enterOuterAlt(_localctx, 5);
          setState(93);
          match(8);
          setState(98);
          this._errHandler.sync(this);
          _la = this._input.LA(1);
          if (_la == 5) {
            setState(94);
            match(5);
            setState(95);
            _localctx.token = token();
            _localctx.d = _localctx.token.value;
          } 
          _localctx.bits = doHash(_localctx.d, _localctx.field);
          break;
      } 
    } catch (RecognitionException re) {
      _localctx.exception = re;
      this._errHandler.reportError(this, re);
      this._errHandler.recover(this, re);
    } finally {
      exitRule();
    } 
    return _localctx;
  }
  
  public final TokenContext token() throws RecognitionException {
    TokenContext _localctx = new TokenContext(this._ctx, getState());
    enterRule(_localctx, 6, 3);
    try {
      enterOuterAlt(_localctx, 1);
      setState(103);
      _localctx.TOKEN = match(1);
      _localctx.value = Integer.parseInt(_localctx.TOKEN.getText());
    } catch (RecognitionException re) {
      _localctx.exception = re;
      this._errHandler.reportError(this, re);
      this._errHandler.recover(this, re);
    } finally {
      exitRule();
    } 
    return _localctx;
  }
}
