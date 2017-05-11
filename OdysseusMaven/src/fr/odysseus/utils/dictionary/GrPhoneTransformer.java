package fr.odysseus.utils.dictionary;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
/**
 * @author Angelo Del Grosso
 *
 */
final public class GrPhoneTransformer {
	Map<String,String> rules;
	public GrPhoneTransformer() {
		rules = initialize();
	}
	public String transform(String word){
		Set<Entry<String,String>> mapEntries = rules.entrySet();
		for(Entry<String,String> e : mapEntries){
			word = word.replaceAll(e.getKey(), e.getValue());
		}
		word = postprocessing(word);
		return word;
	}
	private String postprocessing(String word) {
		// TODO Auto-generated method stub
		boolean cond;
		cond = "".equals(word)
				|| "zeus".equals(word)
				|| word.endsWith("a")
				|| word.endsWith("e")
				|| word.endsWith("i")
				|| word.endsWith("o")
				|| word.endsWith("u")
				|| word.endsWith("r")
				|| word.endsWith("S");
		if(!cond)
			return word.substring(0, word.length()-1) ;
		else
			return word;
	}
	private Map<String, String> initialize() {
		Map<String, String> temp = new LinkedHashMap<String, String>();
		temp.put("α", "a");
		temp.put("ἄ", "a");
		temp.put("ἀ", "a");
		temp.put("ά", "a");
		temp.put("ά", "a");
		temp.put("ὰ", "a");
		temp.put("ἂ", "a");
		temp.put("ᾶ", "a");
		temp.put("Ἀ","A");
		temp.put("Ἄ","A");
		temp.put("β", "b");
		temp.put("κ", "c");
		temp.put("δ", "d");
		temp.put("Δ", "D");
		temp.put("ε", "e");
		temp.put("ἐ", "e");
		temp.put("ἑ", "e");
		temp.put("ἔ", "e");
		temp.put("έ", "e");
		temp.put("ἕ", "e");
		temp.put("Ἑ", "He");
		temp.put("ζ", "z");
		temp.put("φ", "ph");
		temp.put("γ", "g");
		temp.put("ι", "i");
		temp.put("ἰ", "i");
		temp.put("ἴ", "i");
		temp.put("ί", "i");
		temp.put("Ἴ", "I");
		temp.put("ϰ", "c");
		temp.put("λ", "l");
		temp.put("μ", "m");
		temp.put("ν", "n");
		temp.put("ξ", "qu");
		temp.put("ο", "o");
		temp.put("ό", "o");
		temp.put("ὃ", "o");
		temp.put("π", "p");
		temp.put("Π", "P");
		temp.put("ρ", "r");
		temp.put("Ῥ", "R");
		temp.put("σ", "s");
		temp.put("ς", "s");
		temp.put("Σ", "s");
		temp.put("υ", "u");
		temp.put("ὐ", "u");
		temp.put("ύ", "u");
		temp.put("Ὑ", "Hy");
		temp.put("φ", "ph");
//		temp.put("η", "é");
		temp.put("η", "e");
		temp.put("θ", "th");
		temp.put("Θ", "Th");
		temp.put("ῆ", "e");
		temp.put("ἥ", "è");
		temp.put("ὴ", "e");
		temp.put("ῃ", "e");
		temp.put("ή", "e");
		temp.put("ἡ", "he");
		temp.put("Ἠ", "He");
		temp.put("ψ", "ps");
		
		temp.put("Π", "P");
		temp.put("ἱ", "i");
		temp.put("ί", "i");
		temp.put("ΐ", "i");
		temp.put("ῖ", "i");
		temp.put("Ἰ", "I");
		temp.put("ό", "o");
		temp.put("ὄ", "o");
		temp.put("ὸ", "o");
		temp.put("Ὀ", "O");
		
		temp.put("ύ", "u");
		temp.put("ή", "e");
		
		temp.put("ώ", "w");
		temp.put("τ", "t");
		temp.put("h", "e");
		temp.put("ὐ", "u");
		temp.put("ύ", "u");
		temp.put("ῦ","u");
		temp.put("Λ", "L");
		temp.put("Ὦ", "O");
		temp.put("Ὠ", "O");
		temp.put("Φ", "Ph");
		temp.put("ή\\b", "e");
		temp.put("χ", "ch");
		temp.put("w", "o"); 
		temp.put("ω", "o");
		temp.put("ώ", "o");
		temp.put("ῶ", "o");
		temp.put("ὣ", "o");
		temp.put("έ", "e");
		temp.put("ί", "i");
//		temp.put("g(?=[gkcx])", "n");
//		temp.put("\\*ca","Xa");
//		temp.put("(^c)|(\\*c)","S");
//		temp.put("c","c");
//		temp.put("k","ch");
//		temp.put("x(?=[aei])","ch");
//		temp.put("x(?![aei])","qu");
//		temp.put("\\*y","Ps"); 
//		temp.put("^y","s");
//		temp.put("y","y"); 
//		temp.put("ai","e");
//		temp.put("ei(?=[aeiou])","e");
//		temp.put("ei","i");
////		temp.put("oi(?=e)","o");
////		temp.put("oi","e"); 
//		temp.put("au(?=[aeiou])|aύ(?=[aeiou])","av");
//		temp.put("eu(?=[aeiou])","ev");
//		temp.put("ui","i");
//		temp.put("(?<![aeou])u","i"); 
//		temp.put("ou","u");
////		temp.put("(^pt)|(\\*pt)","t");
////		temp.put("pt","tt");
//		temp.put("ti(?=[aeiu])","zi");
		temp.put(";","?");
		temp.put("\\*",""); 
		return temp;
	}
}