package hudson.model.labels;

import hudson.util.QuotedStringTokenizer;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
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

public class LabelExpressionParser extends Parser {
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
  
  public static final int RULE_expr = 0;
  
  public static final int RULE_term1 = 1;
  
  public static final int RULE_term2 = 2;
  
  public static final int RULE_term3 = 3;
  
  public static final int RULE_term4 = 4;
  
  public static final int RULE_term5 = 5;
  
  public static final int RULE_term6 = 6;
  
  public static final String[] ruleNames;
  
  private static final String[] _LITERAL_NAMES;
  
  private static final String[] _SYMBOLIC_NAMES;
  
  public static final Vocabulary VOCABULARY;
  
  @Deprecated
  public static final String[] tokenNames;
  
  public static final String _serializedATN = "\004\001\nP\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\002\004\007\004\002\005\007\005\002\006\007\006\001\000\001\000\001\000\001\000\001\001\001\001\001\001\001\001\001\001\001\001\005\001\031\b\001\n\001\f\001\034\t\001\001\002\001\002\001\002\001\002\001\002\001\002\003\002$\b\002\001\003\001\003\001\003\001\003\001\003\001\003\005\003,\b\003\n\003\f\003/\t\003\001\004\001\004\001\004\001\004\001\004\001\004\005\0047\b\004\n\004\f\004:\t\004\001\005\001\005\001\005\001\005\001\005\001\005\001\005\003\005C\b\005\001\006\001\006\001\006\001\006\001\006\001\006\001\006\001\006\001\006\003\006N\b\006\001\006\000\000\007\000\002\004\006\b\n\f\000\000O\000\016\001\000\000\000\002\022\001\000\000\000\004\035\001\000\000\000\006%\001\000\000\000\b0\001\000\000\000\nB\001\000\000\000\fM\001\000\000\000\016\017\003\002\001\000\017\020\006\000￿￿\000\020\021\005\000\000\001\021\001\001\000\000\000\022\023\003\004\002\000\023\032\006\001￿￿\000\024\025\005\005\000\000\025\026\003\004\002\000\026\027\006\001￿￿\000\027\031\001\000\000\000\030\024\001\000\000\000\031\034\001\000\000\000\032\030\001\000\000\000\032\033\001\000\000\000\033\003\001\000\000\000\034\032\001\000\000\000\035\036\003\006\003\000\036#\006\002￿￿\000\037 \005\004\000\000 !\003\006\003\000!\"\006\002￿￿\000\"$\001\000\000\000#\037\001\000\000\000#$\001\000\000\000$\005\001\000\000\000%&\003\b\004\000&-\006\003￿￿\000'(\005\002\000\000()\003\b\004\000)*\006\003￿￿\000*,\001\000\000\000+'\001\000\000\000,/\001\000\000\000-+\001\000\000\000-.\001\000\000\000.\007\001\000\000\000/-\001\000\000\00001\003\n\005\00018\006\004￿￿\00023\005\001\000\00034\003\n\005\00045\006\004￿￿\00057\001\000\000\00062\001\000\000\0007:\001\000\000\00086\001\000\000\00089\001\000\000\0009\t\001\000\000\000:8\001\000\000\000;<\003\f\006\000<=\006\005￿￿\000=C\001\000\000\000>?\005\003\000\000?@\003\f\006\000@A\006\005￿￿\000AC\001\000\000\000B;\001\000\000\000B>\001\000\000\000C\013\001\000\000\000DE\005\006\000\000EF\003\002\001\000FG\005\007\000\000GH\006\006￿￿\000HN\001\000\000\000IJ\005\b\000\000JN\006\006￿￿\000KL\005\n\000\000LN\006\006￿￿\000MD\001\000\000\000MI\001\000\000\000MK\001\000\000\000N\r\001\000\000\000\006\032#-8BM";
  
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
    _ATN = (new ATNDeserializer()).deserialize("\004\001\nP\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\002\004\007\004\002\005\007\005\002\006\007\006\001\000\001\000\001\000\001\000\001\001\001\001\001\001\001\001\001\001\001\001\005\001\031\b\001\n\001\f\001\034\t\001\001\002\001\002\001\002\001\002\001\002\001\002\003\002$\b\002\001\003\001\003\001\003\001\003\001\003\001\003\005\003,\b\003\n\003\f\003/\t\003\001\004\001\004\001\004\001\004\001\004\001\004\005\0047\b\004\n\004\f\004:\t\004\001\005\001\005\001\005\001\005\001\005\001\005\001\005\003\005C\b\005\001\006\001\006\001\006\001\006\001\006\001\006\001\006\001\006\001\006\003\006N\b\006\001\006\000\000\007\000\002\004\006\b\n\f\000\000O\000\016\001\000\000\000\002\022\001\000\000\000\004\035\001\000\000\000\006%\001\000\000\000\b0\001\000\000\000\nB\001\000\000\000\fM\001\000\000\000\016\017\003\002\001\000\017\020\006\000￿￿\000\020\021\005\000\000\001\021\001\001\000\000\000\022\023\003\004\002\000\023\032\006\001￿￿\000\024\025\005\005\000\000\025\026\003\004\002\000\026\027\006\001￿￿\000\027\031\001\000\000\000\030\024\001\000\000\000\031\034\001\000\000\000\032\030\001\000\000\000\032\033\001\000\000\000\033\003\001\000\000\000\034\032\001\000\000\000\035\036\003\006\003\000\036#\006\002￿￿\000\037 \005\004\000\000 !\003\006\003\000!\"\006\002￿￿\000\"$\001\000\000\000#\037\001\000\000\000#$\001\000\000\000$\005\001\000\000\000%&\003\b\004\000&-\006\003￿￿\000'(\005\002\000\000()\003\b\004\000)*\006\003￿￿\000*,\001\000\000\000+'\001\000\000\000,/\001\000\000\000-+\001\000\000\000-.\001\000\000\000.\007\001\000\000\000/-\001\000\000\00001\003\n\005\00018\006\004￿￿\00023\005\001\000\00034\003\n\005\00045\006\004￿￿\00057\001\000\000\00062\001\000\000\0007:\001\000\000\00086\001\000\000\00089\001\000\000\0009\t\001\000\000\000:8\001\000\000\000;<\003\f\006\000<=\006\005￿￿\000=C\001\000\000\000>?\005\003\000\000?@\003\f\006\000@A\006\005￿￿\000AC\001\000\000\000B;\001\000\000\000B>\001\000\000\000C\013\001\000\000\000DE\005\006\000\000EF\003\002\001\000FG\005\007\000\000GH\006\006￿￿\000HN\001\000\000\000IJ\005\b\000\000JN\006\006￿￿\000KL\005\n\000\000LN\006\006￿￿\000MD\001\000\000\000MI\001\000\000\000MK\001\000\000\000N\r\001\000\000\000\006\032#-8BM".toCharArray());
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (i = 0; i < _ATN.getNumberOfDecisions(); i++)
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i); 
  }
  
  private static String[] makeRuleNames() { return new String[] { "expr", "term1", "term2", "term3", "term4", "term5", "term6" }; }
  
  private static String[] makeLiteralNames() { return new String[] { null, "'&&'", "'||'", "'!'", "'->'", "'<->'", "'('", "')'" }; }
  
  private static String[] makeSymbolicNames() { return new String[] { 
        null, "AND", "OR", "NOT", "IMPLIES", "IFF", "LPAREN", "RPAREN", "ATOM", "WS", 
        "STRINGLITERAL" }; }
  
  @Deprecated
  public String[] getTokenNames() { return tokenNames; }
  
  public Vocabulary getVocabulary() { return VOCABULARY; }
  
  public String getGrammarFileName() { return "LabelExpressionParser.g4"; }
  
  public String[] getRuleNames() { return ruleNames; }
  
  public String getSerializedATN() { return "\004\001\nP\002\000\007\000\002\001\007\001\002\002\007\002\002\003\007\003\002\004\007\004\002\005\007\005\002\006\007\006\001\000\001\000\001\000\001\000\001\001\001\001\001\001\001\001\001\001\001\001\005\001\031\b\001\n\001\f\001\034\t\001\001\002\001\002\001\002\001\002\001\002\001\002\003\002$\b\002\001\003\001\003\001\003\001\003\001\003\001\003\005\003,\b\003\n\003\f\003/\t\003\001\004\001\004\001\004\001\004\001\004\001\004\005\0047\b\004\n\004\f\004:\t\004\001\005\001\005\001\005\001\005\001\005\001\005\001\005\003\005C\b\005\001\006\001\006\001\006\001\006\001\006\001\006\001\006\001\006\001\006\003\006N\b\006\001\006\000\000\007\000\002\004\006\b\n\f\000\000O\000\016\001\000\000\000\002\022\001\000\000\000\004\035\001\000\000\000\006%\001\000\000\000\b0\001\000\000\000\nB\001\000\000\000\fM\001\000\000\000\016\017\003\002\001\000\017\020\006\000￿￿\000\020\021\005\000\000\001\021\001\001\000\000\000\022\023\003\004\002\000\023\032\006\001￿￿\000\024\025\005\005\000\000\025\026\003\004\002\000\026\027\006\001￿￿\000\027\031\001\000\000\000\030\024\001\000\000\000\031\034\001\000\000\000\032\030\001\000\000\000\032\033\001\000\000\000\033\003\001\000\000\000\034\032\001\000\000\000\035\036\003\006\003\000\036#\006\002￿￿\000\037 \005\004\000\000 !\003\006\003\000!\"\006\002￿￿\000\"$\001\000\000\000#\037\001\000\000\000#$\001\000\000\000$\005\001\000\000\000%&\003\b\004\000&-\006\003￿￿\000'(\005\002\000\000()\003\b\004\000)*\006\003￿￿\000*,\001\000\000\000+'\001\000\000\000,/\001\000\000\000-+\001\000\000\000-.\001\000\000\000.\007\001\000\000\000/-\001\000\000\00001\003\n\005\00018\006\004￿￿\00023\005\001\000\00034\003\n\005\00045\006\004￿￿\00057\001\000\000\00062\001\000\000\0007:\001\000\000\00086\001\000\000\00089\001\000\000\0009\t\001\000\000\000:8\001\000\000\000;<\003\f\006\000<=\006\005￿￿\000=C\001\000\000\000>?\005\003\000\000?@\003\f\006\000@A\006\005￿￿\000AC\001\000\000\000B;\001\000\000\000B>\001\000\000\000C\013\001\000\000\000DE\005\006\000\000EF\003\002\001\000FG\005\007\000\000GH\006\006￿￿\000HN\001\000\000\000IJ\005\b\000\000JN\006\006￿￿\000KL\005\n\000\000LN\006\006￿￿\000MD\001\000\000\000MI\001\000\000\000MK\001\000\000\000N\r\001\000\000\000\006\032#-8BM"; }
  
  public ATN getATN() { return _ATN; }
  
  public LabelExpressionParser(TokenStream input) {
    super(input);
    this._interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }
  
  public final ExprContext expr() throws RecognitionException {
    ExprContext _localctx = new ExprContext(this._ctx, getState());
    enterRule(_localctx, 0, 0);
    try {
      enterOuterAlt(_localctx, 1);
      setState(14);
      _localctx.term1 = term1();
      _localctx.l = _localctx.term1.l;
      setState(16);
      match(-1);
    } catch (RecognitionException re) {
      _localctx.exception = re;
      this._errHandler.reportError(this, re);
      this._errHandler.recover(this, re);
    } finally {
      exitRule();
    } 
    return _localctx;
  }
  
  public final Term1Context term1() throws RecognitionException {
    Term1Context _localctx = new Term1Context(this._ctx, getState());
    enterRule(_localctx, 2, 1);
    try {
      enterOuterAlt(_localctx, 1);
      setState(18);
      _localctx.term2 = term2();
      _localctx.l = _localctx.term2.l;
      setState(26);
      this._errHandler.sync(this);
      int _la = this._input.LA(1);
      while (_la == 5) {
        setState(20);
        match(5);
        setState(21);
        _localctx.term2 = term2();
        _localctx.r = _localctx.term2.l;
        _localctx.l = _localctx.l.iff(_localctx.r);
        setState(28);
        this._errHandler.sync(this);
        _la = this._input.LA(1);
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
  
  public final Term2Context term2() throws RecognitionException {
    Term2Context _localctx = new Term2Context(this._ctx, getState());
    enterRule(_localctx, 4, 2);
    try {
      enterOuterAlt(_localctx, 1);
      setState(29);
      _localctx.term3 = term3();
      _localctx.l = _localctx.term3.l;
      setState(35);
      this._errHandler.sync(this);
      int _la = this._input.LA(1);
      if (_la == 4) {
        setState(31);
        match(4);
        setState(32);
        _localctx.term3 = term3();
        _localctx.r = _localctx.term3.l;
        _localctx.l = _localctx.l.implies(_localctx.r);
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
  
  public final Term3Context term3() throws RecognitionException {
    Term3Context _localctx = new Term3Context(this._ctx, getState());
    enterRule(_localctx, 6, 3);
    try {
      enterOuterAlt(_localctx, 1);
      setState(37);
      _localctx.term4 = term4();
      _localctx.l = _localctx.term4.l;
      setState(45);
      this._errHandler.sync(this);
      int _la = this._input.LA(1);
      while (_la == 2) {
        setState(39);
        match(2);
        setState(40);
        _localctx.term4 = term4();
        _localctx.r = _localctx.term4.l;
        _localctx.l = _localctx.l.or(_localctx.r);
        setState(47);
        this._errHandler.sync(this);
        _la = this._input.LA(1);
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
  
  public final Term4Context term4() throws RecognitionException {
    Term4Context _localctx = new Term4Context(this._ctx, getState());
    enterRule(_localctx, 8, 4);
    try {
      enterOuterAlt(_localctx, 1);
      setState(48);
      _localctx.term5 = term5();
      _localctx.l = _localctx.term5.l;
      setState(56);
      this._errHandler.sync(this);
      int _la = this._input.LA(1);
      while (_la == 1) {
        setState(50);
        match(1);
        setState(51);
        _localctx.term5 = term5();
        _localctx.r = _localctx.term5.l;
        _localctx.l = _localctx.l.and(_localctx.r);
        setState(58);
        this._errHandler.sync(this);
        _la = this._input.LA(1);
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
  
  public final Term5Context term5() throws RecognitionException {
    Term5Context _localctx = new Term5Context(this._ctx, getState());
    enterRule(_localctx, 10, 5);
    try {
      setState(66);
      this._errHandler.sync(this);
      switch (this._input.LA(1)) {
        case 6:
        case 8:
        case 10:
          enterOuterAlt(_localctx, 1);
          setState(59);
          _localctx.term6 = term6();
          _localctx.l = _localctx.term6.l;
          break;
        case 3:
          enterOuterAlt(_localctx, 2);
          setState(62);
          match(3);
          setState(63);
          _localctx.term6 = term6();
          _localctx.l = _localctx.term6.l;
          _localctx.l = _localctx.l.not();
          break;
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
  
  public final Term6Context term6() throws RecognitionException {
    Term6Context _localctx = new Term6Context(this._ctx, getState());
    enterRule(_localctx, 12, 6);
    try {
      setState(77);
      this._errHandler.sync(this);
      switch (this._input.LA(1)) {
        case 6:
          enterOuterAlt(_localctx, 1);
          setState(68);
          match(6);
          setState(69);
          _localctx.term1 = term1();
          setState(70);
          match(7);
          _localctx.l = _localctx.term1.l;
          _localctx.l = _localctx.l.paren();
          break;
        case 8:
          enterOuterAlt(_localctx, 2);
          setState(73);
          _localctx.ATOM = match(8);
          _localctx.l = LabelAtom.get(_localctx.ATOM.getText());
          break;
        case 10:
          enterOuterAlt(_localctx, 3);
          setState(75);
          _localctx.STRINGLITERAL = match(10);
          _localctx.l = LabelAtom.get(QuotedStringTokenizer.unquote(_localctx.STRINGLITERAL.getText()));
          break;
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
}
