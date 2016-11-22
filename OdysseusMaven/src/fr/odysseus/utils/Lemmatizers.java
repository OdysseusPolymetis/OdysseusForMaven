package fr.odysseus.utils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Lemmatizers {
	List<String[]>tags;
	final static String DICT="./sourceFiles/sourceDictionaries/";
	String greenList[]={"Minerve","Saturne","Agamemnon","Eurymaque","Atrée","Oreste", "Egisthe", "Polybe","Neptune","Ethiopien","Nestor","Ilion","Polyphème", 
			"Ops","Grecs","Achéens","Cronos","Soleil","soleil", "Olympien","olympien","Cyclope","Cyclopes","Calypso", "Muse", "Mante", "Ope", "Ithaquois", 
			"Ithacquois","Illos", "Laërte", "Sœurs", "Témésé", "Athéné", "Ethiopie","éthiopien", "Grégeois", "Grégeoise","Grégeoises", "Mente", "Antinois",
			"Sparte", "grecs", "Atlas", "Thon","Alcippé", "Océan", "zéphyr", "Iphthimé", "Halosydné", "Sunion","Pergame","Ethiopiens","Mermeride",
			"Havre", "phénicien","Phénicien", "Phéniciens","Phrygiens","Phrygien", "Nègres","Nègre","Panachéens","Laertes","Egiste"};

//	String blackImpf[]={"Toutefois","Autrefois","Parfois"};
	String impf[][]={{"aur","avoir"},{"couvr","couvrir"},{"dis", "dire"},{"éclair", "éclairer"},{"entour","entourer"},{"fais", "faire"},{"fall","falloir"},
			{"flatter","flatter"},{"foul","fouler"},{"oser","oser"},{"pouv","pouvoir"},{"répar","réparer"},{"respir","respirer"},{"ser","être"},{"viendr","venir"},
			{"vant", "vanter"},{"voul","vouloir"},{"voudr","vouloir"}};

	String correc[][]={{"j","je","PRO:PER"},{"t","tu","PRO:PER"},{"l","le","DET:ART"},{"d","de","DET:ART"},{"c","cela","PRO:DEM"},{"aux","au","DET:ART"},
			{"au","au","DET:ART"},{"aujourd","aujourd","NOM"},{"hui","hui","NOM"},{"et","et","KON"},{"sur","sur","PRP"},{"je","je","PRO:PER"},{"tu","tu","PRO:PER"},
			{"il","il","PRO:PER"},{"on","on","PRO:PER"},{"elle","elle","PRO:PER"},{"nous","nous","PRO:PER"},{"vous","vous","PRO:PER"},{"ils","ils","PRO:PER"},
			{"elles","elles","PRO:PER"}};

	Path dictionnaire = Paths.get(DICT+"DictionnaireGutenberg.txt");
	Path blackList = Paths.get(DICT+"blackList.txt");

	public void setTags(List<String[]> motsTags) {
		this.tags = motsTags;
	}

	public List<String[]> getTags() {
		return tags;
	}

	public HashSet<String>getNames(String text, List <String[]>motsTags, TreeTaggerWrapper<String>tt, MaxentTagger tagger) throws IOException{
		HashMap<String, String> mapImpf=new HashMap<String, String>();
		for (int indexImpf=0; indexImpf<impf.length;indexImpf++){
			mapImpf.put(impf[indexImpf][0], impf[indexImpf][1]);
		}
		List<String>motsDictionnaire = Files.readAllLines(dictionnaire);
		List<String>motsBlackList = Files.readAllLines(blackList);
		HashSet<String> setDesNomsStanford=new HashSet<String>();
		HashSet<String> setDesNoms=new HashSet<String>();
		tt.setModel("french.par");
		List<String>listeMotsSansEspace=new ArrayList<String>();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		PTBTokenizer ptbt = new PTBTokenizer(new StringReader(text),
				new CoreLabelTokenFactory(), "");

		for (CoreLabel label; ptbt.hasNext(); ) {
			label = (CoreLabel) ptbt.next();
			String mot=label.word();
			listeMotsSansEspace.add(mot);
		}

		String []stanford=listeMotsSansEspace.toArray(new String[listeMotsSansEspace.size()]);
		List<HasWord> sent = Sentence.toWordList(stanford);
		List<TaggedWord> taggedSent = tagger.tagSentence(sent);
		for (TaggedWord tw:taggedSent) {
			if (tw.tag().contains("NPP")&&tw.word()!="") {
				setDesNomsStanford.add(tw.word());
			}
		}
		tt.setHandler(new TokenHandler<String>() {
			public void token(String token, String pos, String lemma) {

				String mot=token;
				String motLower=mot.toLowerCase();


				if (pos!=null){
					if (pos.matches("NAM")&&mot!=""){
						setDesNoms.add(mot);
					}
					String tagNLemma[]=new String[3];
					if (Arrays.asList(greenList).contains(token)){
						setDesNoms.add(mot);
						tagNLemma[2]="NAM";
						tagNLemma[1]=mot;
						tagNLemma[0]=mot;
					}

					else if (setDesNomsStanford.contains(token)&&!(motsDictionnaire.contains(motLower)|motsBlackList.contains(mot))){
						setDesNoms.add(mot);
						tagNLemma[2]="NAM";
						tagNLemma[1]=mot;
						tagNLemma[0]=mot;	
					}
					else if(pos.contains("NAM")&&(motsDictionnaire.contains(motLower)|motsBlackList.contains(mot))){
						setDesNoms.remove(mot);
						setDesNomsStanford.remove(mot);
						String wordBoundary=" "+mot+" ";
						if (wordBoundary.matches("\\s[A-ZÉa-zéèêëîïôöù]{2,}oi([ts]|(ent)|(ez))+\\s")){
							String racine=wordBoundary.replaceAll("oi([ts]|(ent)|(ez))+\\s", "");
							racine=racine.replaceAll("\\s", "").toLowerCase();	
							if (mapImpf.containsKey(racine)){
								tagNLemma[2]="VER:impf";
								tagNLemma[1]=mapImpf.get(racine);
								tagNLemma[0]=mot;
								System.out.println("Je rentre dans la 1ère condition avec : ");
								System.out.println(motLower);
							}
							else if(motsDictionnaire.contains(motLower.replaceAll("oi([ts]|(ent)|(ez))+", "er"))){
								System.out.println("Je rentre dans la condition dictionnaire 1 avec : ");
								System.out.println(motLower);
								tagNLemma[2]="VER:impf";
								tagNLemma[1]=motLower.replaceAll("oi([ts]|(ent)|(ez))+", "er");
								tagNLemma[0]=mot;
							}
							else if(motsDictionnaire.contains(motLower.replaceAll("oi([ts]|(ent)|(ez))+", "oir"))){
								System.out.println("Je rentre dans la condition dictionnaire 2 avec : ");
								System.out.println(motLower);
								tagNLemma[2]="VER:impf";
								tagNLemma[1]=motLower.replaceAll("oi([ts]|(ent)|(ez))+", "oir");
								tagNLemma[0]=mot;
							}
							else if(motsDictionnaire.contains(motLower.replaceAll("oi([ts]|(ent)|(ez))+", "oire"))){
								System.out.println("Je rentre dans la condition dictionnaire 3 avec : ");
								System.out.println(motLower);
								tagNLemma[2]="VER:impf";
								tagNLemma[1]=motLower.replaceAll("oi([ts]|(ent)|(ez))+", "oire");
								tagNLemma[0]=mot;
							}
							else{
								System.out.println("Je rentre dans la condition dictionnaire 4 avec : ");
								System.out.println(mot);
								tagNLemma[2]="Indef";
								tagNLemma[1]=lemma.toLowerCase();
								tagNLemma[0]=mot;	
							}
						}
						else{
							tagNLemma[2]="Indef";
							tagNLemma[1]=lemma.toLowerCase();
							tagNLemma[0]=mot;	
						}				
					}		
					else{			
						tagNLemma[2]=pos;
						tagNLemma[1]=lemma;
						tagNLemma[0]=mot;
					}
					for (int i=0;i<correc.length;i++){
						if (motLower.matches(correc[i][0])){
							tagNLemma[2]=correc[i][2];
							tagNLemma[1]=correc[i][1];
							tagNLemma[0]=mot;
						}
					}
					motsTags.add(tagNLemma);
				}
			}
		});
		try {
			tt.process(listeMotsSansEspace);
		} catch (TreeTaggerException e) {
			e.printStackTrace();
		}
		tt.destroy();

		setTags(motsTags);
		return setDesNoms;

	}
}
