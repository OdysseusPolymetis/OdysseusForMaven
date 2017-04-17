package fr.odysseus.utils.dictionary;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.simmetrics.metrics.Levenshtein;
import edu.unc.epidoc.transcoder.TransCoder;
/**
 * @author Angelo Del Grosso
 * @author Marianne Reboul
 */
public class CloseMatcher {
	static final String NAMES="./sourceFiles/names/";
	
	String correc[][]={{"Arcésios","Ἀρκεισιάδης"},{"Argus","Ἀργειφόντης"},{"Argiens","Grecs"},{"Aurore","Ἠώς","soleil"},{"Diane","Ἄρτεμις"},{"dieu_qui_ébranle_la_terre","Ἐννοσίγαιος"},
	{"dieu_puissant_qui_ébranle_la_terre","Neptune"},{"Eumee","Εὔμαιος","Συβώτης"},{"Euryclée","Εὐρύκλεια"},{"gisthe","Égysthe","Egyste"},{"Harpyes","Ἅρπυιαι"},
	{"Hercule","Ἡράκλειος"},{"Ilion","Τροία"},{"Ilithye","Εἰλειθυίης"},
	{"Ithaque","Γαῖα"},
	{"Jupiter","Ζεύς"},{"Liodes","Λειῶδες"},
	{"Menelas","Μενέλαος","Menelas"},{"Mercure","Ἑρμῆς","Hermès"},{"Mermeros","Mermeride","Mermere"},{"Minerve","Ἀθήνη"},{"Neptune","Ποσειδεών"},
	{"Océan","Πόντος","Ὠκεανός"},{"Parnese","Παρνασός"},{"Personne_N","Οὖτις"},{"Pheacien","Φαίαξ"},{"Phemios","Phemin"},{"Pisenor","Πεισήνωρ"},{"Pisistrate","Πεισίστρατος"},
	{"Polypheme","Πολύφημος"},{"Pramne","Πράμνειος"},{"Rhexenor","Ῥηξήνωρ"},{"soleil","Ἠέλιος"},{"Telemaque","Thelemacque","Τηλέμαχος"},{"Thoosa","Thoote"},
	{"Ulysse","Ὀδυσσεύς"},{"Venus","Ἀφροδίτη"},{"Vulcain","Ἥφαιστος"}};
	
	private HashMap<String, Set<String>> dictionary;
	public CloseMatcher() {
		dictionary = new HashMap<String,Set<String>>();
	}
	public static CloseMatcher getInstance(){
		return new CloseMatcher();
	}
	public int defaultMatch(String word, List<String>greek, List<String>french) throws Exception{
		TransCoder tc = new TransCoder();
		tc.setParser("BetaCode");
		tc.setConverter("UnicodeC");
		int numMatches = 0;
		SimilarWordFinder swf = new SimilarWordFinder();
		HashSet<String> similarWords = swf.getSimilarWords(word);
		Levenshtein lev=new Levenshtein();
		
		for (String tok:greek){
			String change=tok;
				change=change.replaceAll("\\)", "");
				change=change.replaceAll("\\(", "");
				change=change.replaceAll("|", "");
				change=change.replaceAll("\\*", "");
				change=change.replaceAll("\\/", "");
				change=change.replaceAll("=", "");
				change=change.replaceAll("[0-9]", "");
				change=change.replace("h", "é");
				change=change.replace("q", "th");
				change=change.replace("x", "ch");
				change=change.replace("k", "c");
				change=change.replace("w", "o");
				change=change.replace("y", "ps");
				change=change.replace("f", "ph");
			
			float distance=lev.distance(word, change);
			if (distance<3&&word.length()>4){
				tok=tok.replaceAll("[0-9]", "");
				similarWords.add(tc.getString(tok));			
			}
			for (int i=0;i<correc.length;i++){
				
				if (word.contains(correc[i][0])){
					if (correc[i].length>2){
						similarWords.add(correc[i][1]);
						similarWords.add(correc[i][2]);
					}
					else{
						similarWords.add(correc[i][1]);
					}
				}
			}
		}
		for (String tok:french){
			float distance=lev.distance(word, tok);
			if(word.contains(tok)|tok.contains(word)){
				similarWords.add(tok);
			}
			else if (distance<2&&word.length()>4){
				similarWords.add(tok);			
			}
		}
		dictionary.put(word, similarWords);
		
		numMatches = similarWords.size();
		
		return numMatches;
	}
	public HashMap<String, Set<String>> getDictionary() throws Exception {
		Path pathFrench=Paths.get(NAMES+"frenchNames/FrenchNames.txt");
		Path pathGreek=Paths.get(NAMES+"greekNames/GreekNames.txt");
		List<String> french=Files.readAllLines(pathFrench);
		List<String> greek=Files.readAllLines(pathGreek);
    for (String word:french){
      this.defaultMatch(word, greek, french);
//      this.defaultMatch(word, french, false);
    }
		return dictionary;
	}
}