package fr.odysseus.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import alix.fr.Occ;
import alix.fr.Tokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fr.odysseus.utils.Lemmatizers;

/**
 * tags all raw text in French, using openNLP, TreeTagger, Stanford Tagger, and small local blacklists or greenlists (provided with the program)
 */
public class FrenchTagger {
	final static String SOURCE="./sourceFiles/plainTxtByBook/";
	final static String OUT="./sourceFiles/xml/frenchXML/";
	HashMap <String, String[]>tags;
	final static String DICT="./sourceFiles/sourceDictionaries/";
	static String greenList[]={"Minerve","Saturne","Agamemnon","Eurymaque","Atrée","Oreste", "Egisthe", "Polybe","Neptune","Ethiopien","Nestor","Ilion","Polyphème", 
			"Ops","Grecs","Achéens","Cronos","Soleil","soleil", "Olympien","olympien","Cyclope","Cyclopes","Calypso", "Muse", "Mante", "Ope", "Ithaquois", 
			"Ithacquois","Illos", "Laërte", "Sœurs", "Témésé", "Athéné", "Ethiopie","éthiopien", "Grégeois", "Grégeoise","Grégeoises", "Mente", "Antinois",
			"Sparte", "grecs", "Atlas", "Thon","Alcippé", "Océan", "zéphyr", "Iphthimé", "Halosydné", "Sunion","Pergame","Ethiopiens","Mermeride",
			"Havre", "phénicien","Phénicien", "Phéniciens","Phrygiens","Phrygien", "Nègres","Nègre","Panachéens","Laertes","Egiste","argiens","argien"};
	static String bugAlix[]={"d","l","t","animerai","ressaisirait"};
	static String punctLemma[]={",",";",":","!","?",".","(",")","\"","'","/"};
	
	static String corresp [][]={
			{"UNKNOWN","Indef"},
		    {"NULL","Indef"},
		    {"VERB", "VER"},
		    {"VERBaux" , "VER"},
		    {"VERBppass" , "VER:pper"},
		    {"VERBppres" , "VER"},
		    {"VERBsup" , "VER"},
		    {"SUB" , "NOM"},
		    {"ADJ" , "ADJ"},
		    {"ADV" , "ADV"},
		    {"ADVneg" , "ADV"},
		    {"ADVplace" , "ADV"},
		    {"ADVtemp" , "ADV"},
		    {"ADVquant" , "ADV"},
		    {"ADVindef" , "ADV"},
		    {"ADVinter" , "ADV"},
		    {"PREP" , "PRP"},
		    {"DET" , "DET:ART"},
		    {"DETart" , "DET:ART"},
		    {"DETprep" , "DET:ART"},
		    {"DETnum" , "DET:ART"},
		    {"DETindef" , "DET:ART"},
		    {"DETinetr", "DET:ART"},
		    {"DETdem" , "DET:ART"},
		    {"DETposs" , "DET:ART"},
		    {"PRO" , "PRO"},
		    {"PROpers" , "PRO"},
		    {"PROrel" , "PRO"},
		    {"PROindef" , "PRO"},
		    {"PROdem" , "PRO"},
		    {"PROinter" , "PRO"},
		    {"PROposs" , "PRO"},
		    {"CONJ" , "KON"},
		    {"NAME" , "NAM"},
		    {"NAMEpers" , "NAM"},
		    {"NAMEpersm" , "NAM"},
		    {"NAMEpersf" , "NAM"},
		    {"NAMEplace" , "NAM"},
		    {"NAMEorg" , "NAM"},
		    {"NAMEpeople" , "NAM"},
		    {"NAMEevent" , "NAM"},
		    {"NAMEauthor" , "NAM"},
		    {"NAMEfict", "NAM"},
		    {"NAMEtitle" , "NAM"},
		    {"NAMEanimal" , "NAM"},
		    {"NAMEdemhum" , "NAM"},
		    {"NAMEgod", "NAM"},
		    {"EXCL" , "PUN"},
		    {"NUM" , "NUM"},
		    {"PUN" , "PUN"},
		    {"PUNsent" , "SENT"}};
		       

	
	
	public HashMap<String, String[]> getTags() {
		return tags;
	}
	public FrenchTagger() throws IOException{
		System.out.println("Début du Tagging");
//
		System.setProperty("treetagger.home", "./lib/taggers/Treetagger/");
		MaxentTagger tagger =  new MaxentTagger("./lib/taggers/StanfordTagger/french.tagger");
		

		TreeTaggerWrapper<String>tt = new TreeTaggerWrapper<String>();
		
		HashMap <String, String>mapCorresp=new HashMap<String, String>();
		for (String elem[]:corresp){
			mapCorresp.put(elem[0], elem[1]);
		}
		
		
//		Path blackList = Paths.get(DICT+"blackList.txt");
//		List<String>motsBlackList = Files.readAllLines(blackList);
		List <Object>listAllFiles=new ArrayList<Object>();
		Path start=Paths.get(SOURCE);	
		Files.walk(start)
			.filter( path -> path.toFile().isFile())
			.filter( path -> path.toString().endsWith(".txt"))
			.forEach(listAllFiles::add);

		for (Object path:listAllFiles){
			List<String[]>motsTags=new ArrayList<String[]>();
			String fileName=path.toString().substring(path.toString().lastIndexOf("\\"),path.toString().lastIndexOf(".txt"));
			String text=readFile(path.toString(), StandardCharsets.UTF_8);
			Lemmatizers lemmatizers=new Lemmatizers();
			text=text.replaceAll("'", " ' ");
			text=text.replaceAll("’", " ' ");
			text=text.replaceAll(",", " , ");
			text=text.replaceAll("\\.", " . ");
			text=text.replaceAll(";", " ; ");
			text=text.replaceAll(":", " : ");
			text=text.replaceAll("-", " - ");
			text=text.replaceAll("\\s{2,}", " ");
			
			
			
			lemmatizers.getNames(text, motsTags, tt, tagger);
			motsTags=lemmatizers.getTags();
			
			System.out.println("Stockage en XML du fichier "+fileName);
			Element root = new Element("root");
			Element book=new Element ("book"+fileName.substring(fileName.lastIndexOf("Chant")+5));
			Document doc = new Document(root);
			
			List<List<String[]>> sentences = motsTags.stream()
		            .collect(() -> {
		                List<List<String[]>> list = new ArrayList<>();
		                list.add(new ArrayList<>());
		                return list;
		            },
		            (subList, s) -> {
		                if (s[2].contains("SENT")) {
		                	subList.add(new ArrayList<>());
		                } 
		                else {
		                    subList.get(subList.size() - 1).add(s);
		                }
		            },
		            (list1, list2) -> {
		                list1.get(list1.size() - 1).addAll(list2.remove(0));
		                list1.addAll(list2);
		            });
			
			for (int counterSent=0;counterSent<sentences.size()-1;counterSent++){
				Element sent=new Element("sentence");
				Element punc=new Element("word");
				punc.setAttribute("form", ".");
				punc.setAttribute("lemma", ".");
				punc.setAttribute("postag", "PUN");
				for (String []word:sentences.get(counterSent)){
					if (!word[0].matches("-[LRB]{3,}-")){
						Element mot=new Element("word");
						mot.setAttribute("form",word[0]);
						mot.setAttribute("lemma",word[1]);
						mot.setAttribute("postag",word[2]);
						sent.addContent(mot);
					}
				}
				sent.addContent(punc);
				book.addContent(sent);
			}
			root.addContent(book);
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			File fileOut = new File(OUT+fileName+".xml");
			fileOut.getParentFile().mkdirs();
			xmlOutput.output(doc, new FileWriter(fileOut));
		}
	}
	static String readFile(String path, Charset encoding) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	public static void lemmaWithAlix() throws IOException{
		System.out.println("Début du Tagging");
				
				HashMap <String, String>mapCorresp=new HashMap<String, String>();
				for (String elem[]:corresp){
					mapCorresp.put(elem[0], elem[1]);
				}
				
				List <Object>listAllFiles=new ArrayList<Object>();
				Path start=Paths.get(SOURCE);	
				Files.walk(start)
					.filter( path -> path.toFile().isFile())
					.filter( path -> path.toString().endsWith(".txt"))
					.forEach(listAllFiles::add);

				for (Object path:listAllFiles){
					List<String[]>motsTags=new ArrayList<String[]>();
					String fileName=path.toString().substring(path.toString().lastIndexOf("\\"),path.toString().lastIndexOf(".txt"));
					String text=readFile(path.toString(), StandardCharsets.UTF_8);
					text=text.replaceAll("\\s{2,}", " ");
					
					Tokenizer tok=new Tokenizer(text);
					Occ occ=new Occ();
					while ( tok.token(occ) ) {
						String monOcc[]=new String[3];
						monOcc[0]=occ.graph().toString();
						monOcc[1]=occ.lem().toString();
						
						if (Arrays.asList(greenList).contains(occ.graph().toString())||Arrays.asList(greenList).contains(occ.lem().toString())){
							monOcc[1]=occ.orth().toString();
							monOcc[2]="NAM";
						}
						else{
							
							if(Arrays.asList(punctLemma).contains(occ.orth().toString())){
								monOcc[1]=occ.orth().toString();
							}
							
							if(occ.lem().toString().length()<1){
								monOcc[1]=occ.orth().toString();
							}
							
							if (mapCorresp.containsKey(occ.tag().toString())){
								monOcc[2]=mapCorresp.get(occ.tag().toString());
							}
							else{
								monOcc[2]=occ.tag().toString();
							}
						}
						motsTags.add(monOcc);
					}
					
					System.out.println("Stockage en XML du fichier "+fileName);
					Element root = new Element("root");
					Element book=new Element ("book"+fileName.substring(fileName.lastIndexOf("Chant")+5));
					Document doc = new Document(root);
					
					List<List<String[]>> sentences = motsTags.stream()
				            .collect(() -> {
				                List<List<String[]>> list = new ArrayList<>();
				                list.add(new ArrayList<>());
				                return list;
				            },
				            (subList, s) -> {
				                if (s[2].contains("SENT")) {
				                	subList.add(new ArrayList<>());
				                } 
				                else {
				                    subList.get(subList.size() - 1).add(s);
				                }
				            },
				            (list1, list2) -> {
				                list1.get(list1.size() - 1).addAll(list2.remove(0));
				                list1.addAll(list2);
				            });
					
					for (int counterSent=0;counterSent<sentences.size()-1;counterSent++){
						Element sent=new Element("sentence");
						Element punc=new Element("word");
						punc.setAttribute("form", ".");
						punc.setAttribute("lemma", ".");
						punc.setAttribute("postag", "PUN");
						for (String []word:sentences.get(counterSent)){
							if (!word[0].matches("-[LRB]{3,}-")){
								Element mot=new Element("word");
								mot.setAttribute("form",word[0]);
								mot.setAttribute("lemma",word[1]);
								mot.setAttribute("postag",word[2]);
								sent.addContent(mot);
							}
						}
						sent.addContent(punc);
						book.addContent(sent);
					}
					root.addContent(book);
					XMLOutputter xmlOutput = new XMLOutputter();
					xmlOutput.setFormat(Format.getPrettyFormat());
					File fileOut = new File(OUT+fileName+".xml");
					fileOut.getParentFile().mkdirs();
					xmlOutput.output(doc, new FileWriter(fileOut));
				}
		
	}
	
}
