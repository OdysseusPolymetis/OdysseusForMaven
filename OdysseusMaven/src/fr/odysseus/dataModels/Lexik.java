package fr.odysseus.dataModels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

/**
 * Preloaded list of words
 * Lists are not too big, should not be a problem for memory.
 * @author gloieux-f
 *
 */
public class Lexik
{

  /** French names on which keep Capitalization */
  public static final HashSet<String> NAMES;
  /** French stopwords */
  public static final HashSet<String> STOPLIST;
  /** 130 000 types French lexicon seems not too bad for memory */
  public static final HashSet<String> WORDS;
  /** French common words at start of sentences, render to lower case */
  // public static final HashSet<String> LC;
  
  static {
    String l;
    BufferedReader buf;
    STOPLIST = new HashSet<String>();
    NAMES = new HashSet<String>();
    WORDS = new HashSet<String>();
    try {
      buf = new BufferedReader( 
        new InputStreamReader(
          Tokenizer.class.getResourceAsStream( "stoplist.txt" ), 
          StandardCharsets.UTF_8
        )
      );
      
      while ((l = buf.readLine()) != null) STOPLIST.add( l );
      buf.close();
      
      buf = new BufferedReader( 
        new InputStreamReader(
          Tokenizer.class.getResourceAsStream( "names.txt" ), 
          StandardCharsets.UTF_8
        )
      );
      while ((l = buf.readLine()) != null) NAMES.add( l );
      buf.close();
      
      buf = new BufferedReader( 
        new InputStreamReader(
          Tokenizer.class.getResourceAsStream( "words.txt" ), 
          StandardCharsets.UTF_8
        )
      );
      while ((l = buf.readLine()) != null) WORDS.add( l );
      buf.close();
    } 
    catch (IOException e) {
      e.printStackTrace();
    }


  }

}
