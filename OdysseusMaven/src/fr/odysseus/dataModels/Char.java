package fr.odysseus.dataModels;

import java.util.HashMap;

/**
 * Efficient character categorizer, about 500x faster than
 * Character.is*(), optimized for tokenizer in latin scripts.
 * Taken from http://www.tomshut.de/java/index.html
 * 
 * Idea is to populate a big array of properties for the code points.
 * Memory optimization is possible for alphabetic scripts (… or ’ are in
 * 2000-206F block) Chinese is not relevant for such classes but will need a
 * test
 * 
 * For latin script language, apos and dashes are considered as word characters
 * Separation on these chars is language specific
 * 
 * @author glorieux-f
 */
public class Char
{
  /** The 2 bytes unicode */
  static final int SIZE = 65535;
  /** Properties of chars by index */
  static final short[] CHARS = new short[SIZE + 1];
  /** Is a letter (Unicode property) */
  private static final short LETTER = 0x0001;
  /** Is a space (Unicode property) */
  private static final short SPACE = 0x0002;
  /** Is word, specific, with '-' and ‘'’ */
  private static final short WORD = 0x0004;
  /** Punctuation, according to Unicode */
  private static final short PUNCTUATION = 0x0008;
  private static final short PUNCTUATION_OR_SPACE = SPACE | PUNCTUATION;
  private static final short LOWERCASE = 0x0010;
  private static final short UPPERCASE = 0x0020;
  private static final short DIGIT = 0x0100;
  public static HashMap<Character, String>FILENAME = new HashMap<Character, String>();
  static {
    FILENAME.put('a', "a" );
    FILENAME.put('á', "a" );
    FILENAME.put('à', "a" );
    FILENAME.put('ä', "a" );
    FILENAME.put('â', "a" );
    FILENAME.put('A', "a" );
    FILENAME.put('Á', "a" );
    FILENAME.put('À', "a" );
    FILENAME.put('Â', "a" );
    FILENAME.put('Ä', "a" );
    FILENAME.put('æ', "ae" );
    FILENAME.put('Æ', "ae" );
    FILENAME.put('b', "b" );
    FILENAME.put('B', "b" );
    FILENAME.put('c', "c" );
    FILENAME.put('ç', "c" );
    FILENAME.put('C', "c" );
    FILENAME.put('Ç', "c" );
    FILENAME.put('d', "d" );
    FILENAME.put('D', "d" );
    FILENAME.put('e', "e" );
    FILENAME.put('é', "e" );
    FILENAME.put('è', "e" );
    FILENAME.put('ê', "e" );
    FILENAME.put('ë', "e" );
    FILENAME.put('E', "e" );
    FILENAME.put('É', "e" );
    FILENAME.put('È', "e" );
    FILENAME.put('Ë', "e" );
    FILENAME.put('f', "f" );
    FILENAME.put('F', "f" );
    FILENAME.put('g', "g" );
    FILENAME.put('G', "g" );
    FILENAME.put('h', "h" );
    FILENAME.put('H', "h" );
    FILENAME.put('i', "i" );
    FILENAME.put('í', "i" );
    FILENAME.put('ì', "i" );
    FILENAME.put('î', "i" );
    FILENAME.put('ï', "i" );
    FILENAME.put('I', "i" );
    FILENAME.put('Í', "i" );
    FILENAME.put('Ì', "i" );
    FILENAME.put('Î', "i" );
    FILENAME.put('Ï', "i" );
    FILENAME.put('j', "j" );
    FILENAME.put('J', "j" );
    FILENAME.put('k', "k" );
    FILENAME.put('K', "k" );
    FILENAME.put('l', "l" );
    FILENAME.put('L', "l" );
    FILENAME.put('m', "m" );
    FILENAME.put('M', "m" );
    FILENAME.put('n', "n" );
    FILENAME.put('ñ', "n" );
    FILENAME.put('N', "n" );
    FILENAME.put('Ñ', "n" );
    FILENAME.put('o', "o" );
    FILENAME.put('ó', "o" );
    FILENAME.put('ó', "o" );
    FILENAME.put('ò', "o" );
    FILENAME.put('ô', "o" );
    FILENAME.put('ö', "o" );
    FILENAME.put('õ', "o" );
    FILENAME.put('O', "o" );
    FILENAME.put('Ó', "o" );
    FILENAME.put('Ò', "o" );
    FILENAME.put('Ô', "o" );
    FILENAME.put('Ö', "o" );
    FILENAME.put('Õ', "o" );
    FILENAME.put('Ø', "o" );
    FILENAME.put('œ', "oe" );
    FILENAME.put('Œ', "oe" );
    FILENAME.put('p', "p" );
    FILENAME.put('P', "p" );
    FILENAME.put('q', "q" );
    FILENAME.put('Q', "q" );
    FILENAME.put('r', "r" );
    FILENAME.put('R', "r" );
    FILENAME.put('s', "s" );
    FILENAME.put('š', "s" );
    FILENAME.put('S', "s" );
    FILENAME.put('Š', "s" );
    FILENAME.put('t', "t" );
    FILENAME.put('T', "t" );
    FILENAME.put('u', "u" );
    FILENAME.put('ú', "u" );
    FILENAME.put('ù', "u" );
    FILENAME.put('û', "u" );
    FILENAME.put('ü', "u" );
    FILENAME.put('U', "u" );
    FILENAME.put('Ú', "u" );
    FILENAME.put('Ù', "u" );
    FILENAME.put('Û', "u" );
    FILENAME.put('Ü', "u" );
    FILENAME.put('v', "v" );
    FILENAME.put('V', "v" );
    FILENAME.put('w', "w" );
    FILENAME.put('W', "w" );
    FILENAME.put('x', "x" );
    FILENAME.put('X', "x" );
    FILENAME.put('y', "y" );
    FILENAME.put('ý', "y" );
    FILENAME.put('Y', "y" );
    FILENAME.put('Ý', "y" );
    FILENAME.put('z', "z" );
    FILENAME.put('ž', "z" );
    FILENAME.put('Z', "z" );
    FILENAME.put('-', "-" );
    FILENAME.put('_', "_" );
    FILENAME.put('\'', "-" );
    FILENAME.put('’', "-" );
    FILENAME.put( '\u200c', "" );
    FILENAME.put( '\u200d', "" );
    FILENAME.put('0', "0" );
    FILENAME.put('1', "1" );
    FILENAME.put('2', "2" );
    FILENAME.put('3', "3" );
    FILENAME.put('4', "4" );
    FILENAME.put('5', "5" );
    FILENAME.put('6', "6" );
    FILENAME.put('7', "7" );
    FILENAME.put('8', "8" );
    FILENAME.put('9', "9" );
    FILENAME.put( '°', "" );
    FILENAME.put( '.', "," );
    
    int type;
    // infinite loop when size = 65536, a char restart to 0
    for (char c = 0; c < SIZE; c++) {      
      short properties = 0x0;
      if (Character.isDigit( c ))
        properties |= DIGIT;
      // DO NOT modify '<>' values
      // hacky, hyphen maybe part of compound word, or start of a separator like ---
      if ( c == '-' || c == '\'' || c == '’' ) {
        properties |= WORD;
      }
      else if ( c == '&') {
        properties |= WORD;
      }
      else if ( c == '_') {
        properties |= WORD;
      }
      else if (Character.isLetter( c )) {
        properties |= WORD;
        properties |= LETTER;
        if (Character.isUpperCase( c )) {
          properties |= UPPERCASE;
        }
        if (Character.isLowerCase( c )) {
          properties |= LOWERCASE;
        }
        
      }
      else if (Character.isSpaceChar( c )) {
        properties |= SPACE; // Unicode classes, with unbreakable
      }
      else if (Character.isWhitespace( c )) {
        properties |= SPACE; // \n, \r, \t…
      }
      else {
        type = Character.getType( c );
        if (type == Character.CONNECTOR_PUNCTUATION || type == Character.DASH_PUNCTUATION
            || type == Character.END_PUNCTUATION || type == Character.FINAL_QUOTE_PUNCTUATION
            || type == Character.INITIAL_QUOTE_PUNCTUATION || type == Character.OTHER_PUNCTUATION
            || type == Character.START_PUNCTUATION)
          properties |= PUNCTUATION;
      }
      CHARS[c] = properties;
    }

  }

  /**
   * Is a word character (letter, but also, '’-_)
   * 
   * @see Character#isLetter(char)
   */
  public static boolean isWord( char c )
  {
    return (CHARS[c] & WORD) > 0;
  }

  /**
   * Is a letter
   * 
   * @see Character#isLetter(char)
   */
  public static boolean isLetter( char c )
  {
    return (CHARS[c] & LETTER) > 0;
  }
  
  /**
   * Is Numeric
   * 
   * @see Character#isDigit(char)
   */
  public static boolean isDigit( char c )
  {
    return (CHARS[c] & DIGIT) > 0;
  }



  /**
   * Is a lower case letter
   * 
   * @see Character#isLowerCase(char)
   */
  public static boolean isLowerCase( char c )
  {
    return (CHARS[c] & LOWERCASE) > 0;
  }

  /**
   * Is an upper case letter
   * 
   * @see Character#isUpperCase(char)
   */
  public static boolean isUpperCase( char c )
  {
    return (CHARS[c] & UPPERCASE) > 0;
  }

  /**
   * Is a punctuation mark between words
   * 
   * @param ch
   * @return
   */
  public static boolean isPunctuation( char c )
  {
    return (CHARS[c] & PUNCTUATION) > 0;
  }

  /**
   * Is a "whitespace" according to ISO (space, tabs, new lines) and also for
   * Unicode (non breakable spoaces)
   * 
   * @see Character#isSpaceChar(char)
   * @see Character#isWhiteSpace(char)
   */
  public static boolean isSpace( char c )
  {
    return (CHARS[c] & SPACE) > 0;
  }

  /**
   * Convenient method
   * 
   * @param ch
   * @return
   */
  public static boolean isPunctuationOrSpace( char c )
  {
    return (CHARS[c] & PUNCTUATION_OR_SPACE) > 0;
  }

  private Char()
  {
    // Don't
  }

  /**
   * Testing
   */
  public static void main( String args[] )
  {
    System.out.println( "6 Char.isWord:"+Char.isWord( '6' )+" Char.isPunctuationOrSpace: " + Char.isPunctuationOrSpace( '6' ) );
    System.out.println( "- Char.isWord: " + Char.isWord( '-' ) + " Character.isLetter:" + Character.isLetter( '-' ));
    System.out.println( "' Char.isWord: " + Char.isWord( '\'' ) + " Character.isLetter:" + Character.isLetter( '\'' ) );
    System.out.println( "’ Char.isWord: " + Char.isWord( '’' ) + " Character.isLetter:" + Character.isLetter( '’' ) );
    System.out.println( "& Char.isWord: " + Char.isWord( '&' ) + " Character.isLetter:" + Character.isLetter( '&' ));
    System.out.println( "~ Char.isWord: " + Char.isWord( '~' ) + " Character.isLetter:" + Character.isLetter( '~' ));
    System.out.println( ", Char.isWord: " + Char.isWord( ',' ) + " Character.isLetter:" + Character.isLetter( ',' ) + ", isPunctuation: " + Char.isPunctuation( ',' ));
    System.out.println( "_ isPunctuation: " + Char.isPunctuation( '_' ) );
    System.out.println( "- isPunctuation: " + Char.isPunctuation( '-' ) );
    System.out.println( "Œ isUpperCase: " + Char.isUpperCase( 'Œ' ) );
    System.out.println( "à isLowerCase: " + Char.isLowerCase( 'à' ) );
    System.out.println( "&nbsp; isSpace: " + Char.isSpace( ' ' ) );
    System.out.println( "\\n isSpace: " + Char.isSpace( '\n' ) );
    System.out.println( "  isSpace: " + Char.isSpace( ' ' ) );
    System.out.println( "+ isPunctuation: " + Char.isPunctuation( '+' ) );
    System.out.println( "= isPunctuation: " + Char.isPunctuation( '=' ) );
  }
}
