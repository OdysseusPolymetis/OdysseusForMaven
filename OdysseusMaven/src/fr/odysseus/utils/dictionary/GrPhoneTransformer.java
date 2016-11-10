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
	public String transform(String world){
		Set<Entry<String,String>> mapEntries = rules.entrySet();
		for(Entry<String,String> e : mapEntries){
			//System.err.println(e.getKey());
			world = world.replaceAll(e.getKey(), e.getValue());
		}
		world = postprocessing(world);
		return world;
	}
	private String postprocessing(String world) {
		// TODO Auto-generated method stub
		boolean cond;
		cond = "".equals(world)
				|| "zeus".equals(world)
				|| world.endsWith("a")
				|| world.endsWith("e")
				|| world.endsWith("i")
				|| world.endsWith("o")
				|| world.endsWith("u")
				|| world.endsWith("r")
				|| world.endsWith("S");
		if(!cond)
			return world.substring(0, world.length()-1) ;
		else
			return world;
	}
	private Map<String, String> initialize() {
		Map<String, String> temp = new LinkedHashMap<String, String>();
		temp.put("α", "a");
		temp.put("ἄ", "a");
		temp.put("ἀ", "a");
		temp.put("ά", "a");
		temp.put("ὰ", "a");
		temp.put("ἂ", "a");
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
		temp.put("φ", "ph");
		temp.put("γ", "g");
		temp.put("ι", "i");
		temp.put("ἰ", "i");
		temp.put("ἴ", "i");
		temp.put("ί", "i");
		temp.put("Ἴ", "I");
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
		temp.put("w", "o"); // controllare perchè su originale c'è un p!= u'w(\\s2 ': #salvaSIMS else: p='wS'
		temp.put("ω", "o");
		temp.put("ώ", "o");
		temp.put("ῶ", "o");
		temp.put("ὣ", "o");
		temp.put("g(?=[gkcx])", "n");
//		temp.put("q","t");
		temp.put("\\*ca","Xa"); // per lo Xanto
		temp.put("(^c)|(\\*c)","S");
		temp.put("c","c"); //#x in grafia dotta#
		temp.put("k","ch"); //#k in grafia dotta#
		temp.put("x(?=[aei])","ch");
		temp.put("x(?![aei])","qu");
		temp.put("\\*y","Ps"); //#funzionale solo prima di 'pulizia'
		temp.put("^y","s");
		temp.put("y","y"); //#ps in grafia dotta#
		temp.put("ai","e");
		temp.put("ei(?=[aeiou])","e");
		temp.put("ei","i");
//		temp.put("oi(?=e)","o");
//		temp.put("oi","e"); //#in rari casi, i : es. κοιμητήριον cimitero
		temp.put("au(?=[aeiou])|aύ(?=[aeiou])","av");
		temp.put("eu(?=[aeiou])","ev");
		temp.put("ui","i");
		temp.put("(?<![aeou])u","i"); //#!ATTENZIONE alla posizione di questa regola
		temp.put("ou","u");
//		temp.put("(^pt)|(\\*pt)","t");
//		temp.put("pt","tt");
		temp.put("ti(?=[aeiu])","zi");
		temp.put(";","?");
		temp.put("\\*",""); //#sempre ultima
		return temp;
	}
}