package jenkins.org.apache.commons.validator.routines;

import java.io.Serializable;
import java.net.IDN;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import jenkins.util.MemoryReductionUtil;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class DomainValidator implements Serializable {
  private static final int MAX_DOMAIN_LENGTH = 253;
  
  private static final long serialVersionUID = -4407125112880174009L;
  
  private static final String DOMAIN_LABEL_REGEX = "\\p{Alnum}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?";
  
  private static final String TOP_LABEL_REGEX = "\\p{Alpha}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?";
  
  private static final String DOMAIN_NAME_REGEX = "^(?:\\p{Alnum}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?\\.)+(\\p{Alpha}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?)\\.?$";
  
  private static final String UNEXPECTED_ENUM_VALUE = "Unexpected enum value: ";
  
  private final boolean allowLocal;
  
  private final RegexValidator domainRegex;
  
  private final RegexValidator hostnameRegex;
  
  final String[] mycountryCodeTLDsMinus;
  
  final String[] mycountryCodeTLDsPlus;
  
  final String[] mygenericTLDsPlus;
  
  final String[] mygenericTLDsMinus;
  
  final String[] mylocalTLDsPlus;
  
  final String[] mylocalTLDsMinus;
  
  public static DomainValidator getInstance() {
    inUse = true;
    return LazyHolder.DOMAIN_VALIDATOR;
  }
  
  public static DomainValidator getInstance(boolean allowLocal) {
    inUse = true;
    if (allowLocal)
      return LazyHolder.DOMAIN_VALIDATOR_WITH_LOCAL; 
    return LazyHolder.DOMAIN_VALIDATOR;
  }
  
  public static DomainValidator getInstance(boolean allowLocal, List<Item> items) {
    inUse = true;
    return new DomainValidator(allowLocal, items);
  }
  
  private DomainValidator(boolean allowLocal) {
    this.domainRegex = new RegexValidator("^(?:\\p{Alnum}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?\\.)+(\\p{Alpha}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?)\\.?$");
    this.hostnameRegex = new RegexValidator("\\p{Alnum}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?");
    this.allowLocal = allowLocal;
    this.mycountryCodeTLDsMinus = countryCodeTLDsMinus;
    this.mycountryCodeTLDsPlus = countryCodeTLDsPlus;
    this.mygenericTLDsPlus = genericTLDsPlus;
    this.mygenericTLDsMinus = genericTLDsMinus;
    this.mylocalTLDsPlus = localTLDsPlus;
    this.mylocalTLDsMinus = localTLDsMinus;
  }
  
  private DomainValidator(boolean allowLocal, List<Item> items) {
    this.domainRegex = new RegexValidator("^(?:\\p{Alnum}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?\\.)+(\\p{Alpha}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?)\\.?$");
    this.hostnameRegex = new RegexValidator("\\p{Alnum}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?");
    this.allowLocal = allowLocal;
    String[] ccMinus = countryCodeTLDsMinus;
    String[] ccPlus = countryCodeTLDsPlus;
    String[] genMinus = genericTLDsMinus;
    String[] genPlus = genericTLDsPlus;
    String[] localMinus = localTLDsMinus;
    String[] localPlus = localTLDsPlus;
    for (Item item : items) {
      String[] copy = new String[item.values.length];
      for (int i = 0; i < item.values.length; i++)
        copy[i] = item.values[i].toLowerCase(Locale.ENGLISH); 
      Arrays.sort(copy);
      switch (null.$SwitchMap$jenkins$org$apache$commons$validator$routines$DomainValidator$ArrayType[item.type.ordinal()]) {
        case 1:
          ccMinus = copy;
        case 2:
          ccPlus = copy;
        case 3:
          genMinus = copy;
        case 4:
          genPlus = copy;
        case 5:
          localMinus = copy;
        case 6:
          localPlus = copy;
      } 
    } 
    this.mycountryCodeTLDsMinus = ccMinus;
    this.mycountryCodeTLDsPlus = ccPlus;
    this.mygenericTLDsMinus = genMinus;
    this.mygenericTLDsPlus = genPlus;
    this.mylocalTLDsMinus = localMinus;
    this.mylocalTLDsPlus = localPlus;
  }
  
  public boolean isValid(String domain) {
    if (domain == null)
      return false; 
    domain = unicodeToASCII(domain);
    if (domain.length() > 253)
      return false; 
    String[] groups = this.domainRegex.match(domain);
    if (groups != null && groups.length > 0)
      return isValidTld(groups[0]); 
    return (this.allowLocal && this.hostnameRegex.isValid(domain));
  }
  
  final boolean isValidDomainSyntax(String domain) {
    if (domain == null)
      return false; 
    domain = unicodeToASCII(domain);
    if (domain.length() > 253)
      return false; 
    String[] groups = this.domainRegex.match(domain);
    return ((groups != null && groups.length > 0) || this.hostnameRegex
      .isValid(domain));
  }
  
  public boolean isValidTld(String tld) {
    if (this.allowLocal && isValidLocalTld(tld))
      return true; 
    return (isValidInfrastructureTld(tld) || 
      isValidGenericTld(tld) || 
      isValidCountryCodeTld(tld));
  }
  
  public boolean isValidInfrastructureTld(String iTld) {
    String key = chompLeadingDot(unicodeToASCII(iTld).toLowerCase(Locale.ENGLISH));
    return arrayContains(INFRASTRUCTURE_TLDS, key);
  }
  
  public boolean isValidGenericTld(String gTld) {
    String key = chompLeadingDot(unicodeToASCII(gTld).toLowerCase(Locale.ENGLISH));
    return ((arrayContains(GENERIC_TLDS, key) || arrayContains(this.mygenericTLDsPlus, key)) && 
      !arrayContains(this.mygenericTLDsMinus, key));
  }
  
  public boolean isValidCountryCodeTld(String ccTld) {
    String key = chompLeadingDot(unicodeToASCII(ccTld).toLowerCase(Locale.ENGLISH));
    return ((arrayContains(COUNTRY_CODE_TLDS, key) || arrayContains(this.mycountryCodeTLDsPlus, key)) && 
      !arrayContains(this.mycountryCodeTLDsMinus, key));
  }
  
  public boolean isValidLocalTld(String lTld) {
    String key = chompLeadingDot(unicodeToASCII(lTld).toLowerCase(Locale.ENGLISH));
    return ((arrayContains(LOCAL_TLDS, key) || arrayContains(this.mylocalTLDsPlus, key)) && 
      !arrayContains(this.mylocalTLDsMinus, key));
  }
  
  public boolean isAllowLocal() { return this.allowLocal; }
  
  private String chompLeadingDot(String str) {
    if (str.startsWith("."))
      return str.substring(1); 
    return str;
  }
  
  private static final String[] INFRASTRUCTURE_TLDS = { "arpa" };
  
  private static final String[] GENERIC_TLDS = { 
      "aaa", "aarp", "abarth", "abb", "abbott", "abbvie", "abc", "able", "abogado", "abudhabi", 
      "academy", "accenture", "accountant", "accountants", "aco", "actor", "adac", "ads", "adult", "aeg", 
      "aero", "aetna", "afamilycompany", "afl", "africa", "agakhan", "agency", "aig", "airbus", "airforce", 
      "airtel", "akdn", "alfaromeo", "alibaba", "alipay", "allfinanz", "allstate", "ally", "alsace", "alstom", 
      "amazon", "americanexpress", "americanfamily", "amex", "amfam", "amica", "amsterdam", "analytics", "android", "anquan", 
      "anz", "aol", "apartments", "app", "apple", "aquarelle", "arab", "aramco", "archi", "army", 
      "art", "arte", "asda", "asia", "associates", "athleta", "attorney", "auction", "audi", "audible", 
      "audio", "auspost", "author", "auto", "autos", "avianca", "aws", "axa", "azure", "baby", 
      "baidu", "banamex", "bananarepublic", "band", "bank", "bar", "barcelona", "barclaycard", "barclays", "barefoot", 
      "bargains", "baseball", "basketball", "bauhaus", "bayern", "bbc", "bbt", "bbva", "bcg", "bcn", 
      "beats", "beauty", "beer", "bentley", "berlin", "best", "bestbuy", "bet", "bharti", "bible", 
      "bid", "bike", "bing", "bingo", "bio", "biz", "black", "blackfriday", "blockbuster", "blog", 
      "bloomberg", "blue", "bms", "bmw", "bnpparibas", "boats", "boehringer", "bofa", "bom", "bond", 
      "boo", "book", "booking", "bosch", "bostik", "boston", "bot", "boutique", "box", "bradesco", 
      "bridgestone", "broadway", "broker", "brother", "brussels", "budapest", "bugatti", "build", "builders", "business", 
      "buy", "buzz", "bzh", "cab", "cafe", "cal", "call", "calvinklein", "cam", "camera", 
      "camp", "cancerresearch", "canon", "capetown", "capital", "capitalone", "car", "caravan", "cards", "care", 
      "career", "careers", "cars", "casa", "case", "caseih", "cash", "casino", "cat", "catering", 
      "catholic", "cba", "cbn", "cbre", "cbs", "ceb", "center", "ceo", "cern", "cfa", 
      "cfd", "chanel", "channel", "charity", "chase", "chat", "cheap", "chintai", "christmas", "chrome", 
      "church", "cipriani", "circle", "cisco", "citadel", "citi", "citic", "city", "cityeats", "claims", 
      "cleaning", "click", "clinic", "clinique", "clothing", "cloud", "club", "clubmed", "coach", "codes", 
      "coffee", "college", "cologne", "com", "comcast", "commbank", "community", "company", "compare", "computer", 
      "comsec", "condos", "construction", "consulting", "contact", "contractors", "cooking", "cookingchannel", "cool", "coop", 
      "corsica", "country", "coupon", "coupons", "courses", "cpa", "credit", "creditcard", "creditunion", "cricket", 
      "crown", "crs", "cruise", "cruises", "csc", "cuisinella", "cymru", "cyou", "dabur", "dad", 
      "dance", "data", "date", "dating", "datsun", "day", "dclk", "dds", "deal", "dealer", 
      "deals", "degree", "delivery", "dell", "deloitte", "delta", "democrat", "dental", "dentist", "desi", 
      "design", "dev", "dhl", "diamonds", "diet", "digital", "direct", "directory", "discount", "discover", 
      "dish", "diy", "dnp", "docs", "doctor", "dog", "domains", "dot", "download", "drive", 
      "dtv", "dubai", "duck", "dunlop", "dupont", "durban", "dvag", "dvr", "earth", "eat", 
      "eco", "edeka", "edu", "education", "email", "emerck", "energy", "engineer", "engineering", "enterprises", 
      "epson", "equipment", "ericsson", "erni", "esq", "estate", "etisalat", "eurovision", "eus", "events", 
      "exchange", "expert", "exposed", "express", "extraspace", "fage", "fail", "fairwinds", "faith", "family", 
      "fan", "fans", "farm", "farmers", "fashion", "fast", "fedex", "feedback", "ferrari", "ferrero", 
      "fiat", "fidelity", "fido", "film", "final", "finance", "financial", "fire", "firestone", "firmdale", 
      "fish", "fishing", "fit", "fitness", "flickr", "flights", "flir", "florist", "flowers", "fly", 
      "foo", "food", "foodnetwork", "football", "ford", "forex", "forsale", "forum", "foundation", "fox", 
      "free", "fresenius", "frl", "frogans", "frontdoor", "frontier", "ftr", "fujitsu", "fujixerox", "fun", 
      "fund", "furniture", "futbol", "fyi", "gal", "gallery", "gallo", "gallup", "game", "games", 
      "gap", "garden", "gay", "gbiz", "gdn", "gea", "gent", "genting", "george", "ggee", 
      "gift", "gifts", "gives", "giving", "glade", "glass", "gle", "global", "globo", "gmail", 
      "gmbh", "gmo", "gmx", "godaddy", "gold", "goldpoint", "golf", "goo", "goodyear", "goog", 
      "google", "gop", "got", "gov", "grainger", "graphics", "gratis", "green", "gripe", "grocery", 
      "group", "guardian", "gucci", "guge", "guide", "guitars", "guru", "hair", "hamburg", "hangout", 
      "haus", "hbo", "hdfc", "hdfcbank", "health", "healthcare", "help", "helsinki", "here", "hermes", 
      "hgtv", "hiphop", "hisamitsu", "hitachi", "hiv", "hkt", "hockey", "holdings", "holiday", "homedepot", 
      "homegoods", "homes", "homesense", "honda", "horse", "hospital", "host", "hosting", "hot", "hoteles", 
      "hotels", "hotmail", "house", "how", "hsbc", "hughes", "hyatt", "hyundai", "ibm", "icbc", 
      "ice", "icu", "ieee", "ifm", "ikano", "imamat", "imdb", "immo", "immobilien", "inc", 
      "industries", "infiniti", "info", "ing", "ink", "institute", "insurance", "insure", "int", "intel", 
      "international", "intuit", "investments", "ipiranga", "irish", "ismaili", "ist", "istanbul", "itau", "itv", 
      "iveco", "jaguar", "java", "jcb", "jcp", "jeep", "jetzt", "jewelry", "jio", "jll", 
      "jmp", "jnj", "jobs", "joburg", "jot", "joy", "jpmorgan", "jprs", "juegos", "juniper", 
      "kaufen", "kddi", "kerryhotels", "kerrylogistics", "kerryproperties", "kfh", "kia", "kim", "kinder", "kindle", 
      "kitchen", "kiwi", "koeln", "komatsu", "kosher", "kpmg", "kpn", "krd", "kred", "kuokgroup", 
      "kyoto", "lacaixa", "lamborghini", "lamer", "lancaster", "lancia", "land", "landrover", "lanxess", "lasalle", 
      "lat", "latino", "latrobe", "law", "lawyer", "lds", "lease", "leclerc", "lefrak", "legal", 
      "lego", "lexus", "lgbt", "lidl", "life", "lifeinsurance", "lifestyle", "lighting", "like", "lilly", 
      "limited", "limo", "lincoln", "linde", "link", "lipsy", "live", "living", "lixil", "llc", 
      "llp", "loan", "loans", "locker", "locus", "loft", "lol", "london", "lotte", "lotto", 
      "love", "lpl", "lplfinancial", "ltd", "ltda", "lundbeck", "lupin", "luxe", "luxury", "macys", 
      "madrid", "maif", "maison", "makeup", "man", "management", "mango", "map", "market", "marketing", 
      "markets", "marriott", "marshalls", "maserati", "mattel", "mba", "mckinsey", "med", "media", "meet", 
      "melbourne", "meme", "memorial", "men", "menu", "merckmsd", "metlife", "miami", "microsoft", "mil", 
      "mini", "mint", "mit", "mitsubishi", "mlb", "mls", "mma", "mobi", "mobile", "moda", 
      "moe", "moi", "mom", "monash", "money", "monster", "mormon", "mortgage", "moscow", "moto", 
      "motorcycles", "mov", "movie", "msd", "mtn", "mtr", "museum", "mutual", "nab", "nagoya", 
      "name", "nationwide", "natura", "navy", "nba", "nec", "net", "netbank", "netflix", "network", 
      "neustar", "new", "newholland", "news", "next", "nextdirect", "nexus", "nfl", "ngo", "nhk", 
      "nico", "nike", "nikon", "ninja", "nissan", "nissay", "nokia", "northwesternmutual", "norton", "now", 
      "nowruz", "nowtv", "nra", "nrw", "ntt", "nyc", "obi", "observer", "off", "office", 
      "okinawa", "olayan", "olayangroup", "oldnavy", "ollo", "omega", "one", "ong", "onl", "online", 
      "onyourside", "ooo", "open", "oracle", "orange", "org", "organic", "origins", "osaka", "otsuka", 
      "ott", "ovh", "page", "panasonic", "paris", "pars", "partners", "parts", "party", "passagens", 
      "pay", "pccw", "pet", "pfizer", "pharmacy", "phd", "philips", "phone", "photo", "photography", 
      "photos", "physio", "pics", "pictet", "pictures", "pid", "pin", "ping", "pink", "pioneer", 
      "pizza", "place", "play", "playstation", "plumbing", "plus", "pnc", "pohl", "poker", "politie", 
      "porn", "post", "pramerica", "praxi", "press", "prime", "pro", "prod", "productions", "prof", 
      "progressive", "promo", "properties", "property", "protection", "pru", "prudential", "pub", "pwc", "qpon", 
      "quebec", "quest", "qvc", "racing", "radio", "raid", "read", "realestate", "realtor", "realty", 
      "recipes", "red", "redstone", "redumbrella", "rehab", "reise", "reisen", "reit", "reliance", "ren", 
      "rent", "rentals", "repair", "report", "republican", "rest", "restaurant", "review", "reviews", "rexroth", 
      "rich", "richardli", "ricoh", "ril", "rio", "rip", "rmit", "rocher", "rocks", "rodeo", 
      "rogers", "room", "rsvp", "rugby", "ruhr", "run", "rwe", "ryukyu", "saarland", "safe", 
      "safety", "sakura", "sale", "salon", "samsclub", "samsung", "sandvik", "sandvikcoromant", "sanofi", "sap", 
      "sarl", "sas", "save", "saxo", "sbi", "sbs", "sca", "scb", "schaeffler", "schmidt", 
      "scholarships", "school", "schule", "schwarz", "science", "scjohnson", "scot", "search", "seat", "secure", 
      "security", "seek", "select", "sener", "services", "ses", "seven", "sew", "sex", "sexy", 
      "sfr", "shangrila", "sharp", "shaw", "shell", "shia", "shiksha", "shoes", "shop", "shopping", 
      "shouji", "show", "showtime", "shriram", "silk", "sina", "singles", "site", "ski", "skin", 
      "sky", "skype", "sling", "smart", "smile", "sncf", "soccer", "social", "softbank", "software", 
      "sohu", "solar", "solutions", "song", "sony", "soy", "space", "sport", "spot", "spreadbetting", 
      "srl", "stada", "staples", "star", "statebank", "statefarm", "stc", "stcgroup", "stockholm", "storage", 
      "store", "stream", "studio", "study", "style", "sucks", "supplies", "supply", "support", "surf", 
      "surgery", "suzuki", "swatch", "swiftcover", "swiss", "sydney", "systems", "tab", "taipei", "talk", 
      "taobao", "target", "tatamotors", "tatar", "tattoo", "tax", "taxi", "tci", "tdk", "team", 
      "tech", "technology", "tel", "temasek", "tennis", "teva", "thd", "theater", "theatre", "tiaa", 
      "tickets", "tienda", "tiffany", "tips", "tires", "tirol", "tjmaxx", "tjx", "tkmaxx", "tmall", 
      "today", "tokyo", "tools", "top", "toray", "toshiba", "total", "tours", "town", "toyota", 
      "toys", "trade", "trading", "training", "travel", "travelchannel", "travelers", "travelersinsurance", "trust", "trv", 
      "tube", "tui", "tunes", "tushu", "tvs", "ubank", "ubs", "unicom", "university", "uno", 
      "uol", "ups", "vacations", "vana", "vanguard", "vegas", "ventures", "verisign", "versicherung", "vet", 
      "viajes", "video", "vig", "viking", "villas", "vin", "vip", "virgin", "visa", "vision", 
      "viva", "vivo", "vlaanderen", "vodka", "volkswagen", "volvo", "vote", "voting", "voto", "voyage", 
      "vuelos", "wales", "walmart", "walter", "wang", "wanggou", "watch", "watches", "weather", "weatherchannel", 
      "webcam", "weber", "website", "wed", "wedding", "weibo", "weir", "whoswho", "wien", "wiki", 
      "williamhill", "win", "windows", "wine", "winners", "wme", "wolterskluwer", "woodside", "work", "works", 
      "world", "wow", "wtc", "wtf", "xbox", "xerox", "xfinity", "xihuan", "xin", "xn--11b4c3d", 
      "xn--1ck2e1b", "xn--1qqw23a", "xn--30rr7y", "xn--3bst00m", "xn--3ds443g", "xn--3oq18vl8pn36a", "xn--3pxu8k", "xn--42c2d9a", "xn--45q11c", "xn--4gbrim", 
      "xn--55qw42g", "xn--55qx5d", "xn--5su34j936bgsg", "xn--5tzm5g", "xn--6frz82g", "xn--6qq986b3xl", "xn--80adxhks", "xn--80aqecdr1a", "xn--80asehdb", "xn--80aswg", 
      "xn--8y0a063a", "xn--90ae", "xn--9dbq2a", "xn--9et52u", "xn--9krt00a", "xn--b4w605ferd", "xn--bck1b9a5dre4c", "xn--c1avg", "xn--c2br7g", "xn--cck2b3b", 
      "xn--cckwcxetd", "xn--cg4bki", "xn--czr694b", "xn--czrs0t", "xn--czru2d", "xn--d1acj3b", "xn--eckvdtc9d", "xn--efvy88h", "xn--fct429k", "xn--fhbei", 
      "xn--fiq228c5hs", "xn--fiq64b", "xn--fjq720a", "xn--flw351e", "xn--fzys8d69uvgm", "xn--g2xx48c", "xn--gckr3f0f", "xn--gk3at1e", "xn--hxt814e", "xn--i1b6b1a6a2e", 
      "xn--imr513n", "xn--io0a7i", "xn--j1aef", "xn--jlq480n2rg", "xn--jlq61u9w7b", "xn--jvr189m", "xn--kcrx77d1x4a", "xn--kput3i", "xn--mgba3a3ejt", "xn--mgba7c0bbn0a", 
      "xn--mgbaakc7dvf", "xn--mgbab2bd", "xn--mgbca7dzdo", "xn--mgbi4ecexp", "xn--mgbt3dhd", "xn--mk1bu44c", "xn--mxtq1m", "xn--ngbc5azd", "xn--ngbe9e0a", "xn--ngbrx", 
      "xn--nqv7f", "xn--nqv7fs00ema", "xn--nyqy26a", "xn--otu796d", "xn--p1acf", "xn--pssy2u", "xn--q9jyb4c", "xn--qcka1pmc", "xn--rhqv96g", "xn--rovu88b", 
      "xn--ses554g", "xn--t60b56a", "xn--tckwe", "xn--tiq49xqyj", "xn--unup4y", "xn--vermgensberater-ctb", "xn--vermgensberatung-pwb", "xn--vhquv", "xn--vuq861b", "xn--w4r85el8fhu5dnra", 
      "xn--w4rs40l", "xn--xhq521b", "xn--zfr164b", "xxx", "xyz", "yachts", "yahoo", "yamaxun", "yandex", "yodobashi", 
      "yoga", "yokohama", "you", "youtube", "yun", "zappos", "zara", "zero", "zip", "zone", 
      "zuerich" };
  
  private static final String[] COUNTRY_CODE_TLDS = { 
      "ac", "ad", "ae", "af", "ag", "ai", "al", "am", "ao", "aq", 
      "ar", "as", "at", "au", "aw", "ax", "az", "ba", "bb", "bd", 
      "be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", 
      "bs", "bt", "bv", "bw", "by", "bz", "ca", "cc", "cd", "cf", 
      "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cr", "cu", 
      "cv", "cw", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", 
      "dz", "ec", "ee", "eg", "er", "es", "et", "eu", "fi", "fj", 
      "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", 
      "gh", "gi", "gl", "gm", "gn", "gp", "gq", "gr", "gs", "gt", 
      "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", 
      "ie", "il", "im", "in", "io", "iq", "ir", "is", "it", "je", 
      "jm", "jo", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", 
      "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", 
      "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md", "me", "mg", 
      "mh", "mk", "ml", "mm", "mn", "mo", "mp", "mq", "mr", "ms", 
      "mt", "mu", "mv", "mw", "mx", "my", "mz", "na", "nc", "ne", 
      "nf", "ng", "ni", "nl", "no", "np", "nr", "nu", "nz", "om", 
      "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", 
      "ps", "pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", 
      "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", 
      "sl", "sm", "sn", "so", "sr", "ss", "st", "su", "sv", "sx", 
      "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk", "tl", 
      "tm", "tn", "to", "tr", "tt", "tv", "tw", "tz", "ua", "ug", 
      "uk", "us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn", 
      "vu", "wf", "ws", "xn--2scrj9c", "xn--3e0b707e", "xn--3hcrj9c", "xn--45br5cyl", "xn--45brj9c", "xn--54b7fta0cc", "xn--80ao21a", 
      "xn--90a3ac", "xn--90ais", "xn--clchc0ea0b2g2a9gcd", "xn--d1alf", "xn--e1a4c", "xn--fiqs8s", "xn--fiqz9s", "xn--fpcrj9c3d", "xn--fzc2c9e2c", "xn--gecrj9c", 
      "xn--h2breg3eve", "xn--h2brj9c", "xn--h2brj9c8c", "xn--j1amh", "xn--j6w193g", "xn--kprw13d", "xn--kpry57d", "xn--l1acc", "xn--lgbbat1ad8j", "xn--mgb9awbf", 
      "xn--mgba3a4f16a", "xn--mgbaam7a8h", "xn--mgbah1a3hjkrd", "xn--mgbai9azgqp6j", "xn--mgbayh7gpa", "xn--mgbbh1a", "xn--mgbbh1a71e", "xn--mgbc0a9azcg", "xn--mgbcpq6gpa1a", "xn--mgberp4a5d4ar", 
      "xn--mgbgu82a", "xn--mgbpl2fh", "xn--mgbtx2b", "xn--mgbx4cd0ab", "xn--mix891f", "xn--node", "xn--o3cw4h", "xn--ogbpf8fl", "xn--p1ai", "xn--pgbs0dh", 
      "xn--q7ce6a", "xn--qxa6a", "xn--qxam", "xn--rvc1e0am3e", "xn--s9brj9c", "xn--wgbh1c", "xn--wgbl6a", "xn--xkc2al3hye2a", "xn--xkc2dl3a5ee0h", "xn--y9a3aq", 
      "xn--yfro4i67o", "xn--ygbi2ammx", "ye", "yt", "za", "zm", "zw" };
  
  private static final String[] LOCAL_TLDS = { "localdomain", "localhost" };
  
  private static boolean inUse = false;
  
  private static String[] countryCodeTLDsPlus = MemoryReductionUtil.EMPTY_STRING_ARRAY;
  
  private static String[] genericTLDsPlus = MemoryReductionUtil.EMPTY_STRING_ARRAY;
  
  private static String[] countryCodeTLDsMinus = MemoryReductionUtil.EMPTY_STRING_ARRAY;
  
  private static String[] genericTLDsMinus = MemoryReductionUtil.EMPTY_STRING_ARRAY;
  
  private static String[] localTLDsMinus = MemoryReductionUtil.EMPTY_STRING_ARRAY;
  
  private static String[] localTLDsPlus = MemoryReductionUtil.EMPTY_STRING_ARRAY;
  
  public static void updateTLDOverride(ArrayType table, String[] tlds) {
    if (inUse)
      throw new IllegalStateException("Can only invoke this method before calling getInstance"); 
    String[] copy = new String[tlds.length];
    for (int i = 0; i < tlds.length; i++)
      copy[i] = tlds[i].toLowerCase(Locale.ENGLISH); 
    Arrays.sort(copy);
    switch (null.$SwitchMap$jenkins$org$apache$commons$validator$routines$DomainValidator$ArrayType[table.ordinal()]) {
      case 1:
        countryCodeTLDsMinus = copy;
        return;
      case 2:
        countryCodeTLDsPlus = copy;
        return;
      case 3:
        genericTLDsMinus = copy;
        return;
      case 4:
        genericTLDsPlus = copy;
        return;
      case 5:
        localTLDsMinus = copy;
        return;
      case 6:
        localTLDsPlus = copy;
        return;
      case 7:
      case 8:
      case 9:
      case 10:
        throw new IllegalArgumentException("Cannot update the table: " + table);
    } 
    throw new IllegalArgumentException("Unexpected enum value: " + table);
  }
  
  public static String[] getTLDEntries(ArrayType table) {
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    switch (null.$SwitchMap$jenkins$org$apache$commons$validator$routines$DomainValidator$ArrayType[table.ordinal()]) {
      case 1:
        array = countryCodeTLDsMinus;
        return (String[])Arrays.copyOf(array, array.length);
      case 2:
        array = countryCodeTLDsPlus;
        return (String[])Arrays.copyOf(array, array.length);
      case 3:
        array = genericTLDsMinus;
        return (String[])Arrays.copyOf(array, array.length);
      case 4:
        array = genericTLDsPlus;
        return (String[])Arrays.copyOf(array, array.length);
      case 5:
        array = localTLDsMinus;
        return (String[])Arrays.copyOf(array, array.length);
      case 6:
        array = localTLDsPlus;
        return (String[])Arrays.copyOf(array, array.length);
      case 8:
        array = GENERIC_TLDS;
        return (String[])Arrays.copyOf(array, array.length);
      case 7:
        array = COUNTRY_CODE_TLDS;
        return (String[])Arrays.copyOf(array, array.length);
      case 9:
        array = INFRASTRUCTURE_TLDS;
        return (String[])Arrays.copyOf(array, array.length);
      case 10:
        array = LOCAL_TLDS;
        return (String[])Arrays.copyOf(array, array.length);
    } 
    throw new IllegalArgumentException("Unexpected enum value: " + table);
  }
  
  public String[] getOverrides(ArrayType table) {
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    String[] array;
    switch (null.$SwitchMap$jenkins$org$apache$commons$validator$routines$DomainValidator$ArrayType[table.ordinal()]) {
      case 1:
        array = this.mycountryCodeTLDsMinus;
        return (String[])Arrays.copyOf(array, array.length);
      case 2:
        array = this.mycountryCodeTLDsPlus;
        return (String[])Arrays.copyOf(array, array.length);
      case 3:
        array = this.mygenericTLDsMinus;
        return (String[])Arrays.copyOf(array, array.length);
      case 4:
        array = this.mygenericTLDsPlus;
        return (String[])Arrays.copyOf(array, array.length);
      case 5:
        array = this.mylocalTLDsMinus;
        return (String[])Arrays.copyOf(array, array.length);
      case 6:
        array = this.mylocalTLDsPlus;
        return (String[])Arrays.copyOf(array, array.length);
    } 
    throw new IllegalArgumentException("Unexpected enum value: " + table);
  }
  
  public static String unicodeToASCII(String input) {
    if (isOnlyASCII(input))
      return input; 
    try {
      String ascii = IDN.toASCII(input);
      if (IDNBUGHOLDER.IDN_TOASCII_PRESERVES_TRAILING_DOTS)
        return ascii; 
      int length = input.length();
      if (length == 0)
        return input; 
      char lastChar = input.charAt(length - 1);
      switch (lastChar) {
        case '.':
        case '。':
        case '．':
        case '｡':
          return ascii + ".";
      } 
      return ascii;
    } catch (IllegalArgumentException e) {
      return input;
    } 
  }
  
  private static boolean isOnlyASCII(String input) {
    if (input == null)
      return true; 
    for (int i = 0; i < input.length(); i++) {
      if (input.charAt(i) > '')
        return false; 
    } 
    return true;
  }
  
  private static boolean arrayContains(String[] sortedArray, String key) { return (Arrays.binarySearch(sortedArray, key) >= 0); }
}
