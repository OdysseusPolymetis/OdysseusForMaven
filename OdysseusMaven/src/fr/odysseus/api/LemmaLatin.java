package fr.odysseus.api;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fr.odysseus.utils.NamesPatternMatcher;

/**
 * gets all the attributes from Latin xml source files and rearrange them in suitable xml sequences for alignement
 */
public class LemmaLatin {
	final static String SOURCE="./input/xml/latxml/";
	final static String TARGET="./input/seq/latSeq/";
	final static String NAMES="./input/names/latname/";
	final static String TARGETNAM="./input/names/latname/";
	public LemmaLatin() throws Exception{

		File fileRoot=new File(SOURCE);
		File[] listeFilesChants=fileRoot.listFiles();
		List<String>listNomsGrecsLemmaNForms=new ArrayList<String>();
		Set<String>nomsGrecs=new HashSet<String>();
		String blackList[]={ ""};

		for (File file:listeFilesChants){
			System.out.println(file.getName());
			String numChant=file.getName().substring(file.getName().lastIndexOf("_")+1, file.getName().indexOf(".xml"));
			File fichierXML=new File(SOURCE+file.getName());
			SAXBuilder builder = new SAXBuilder();
			Document document;

			document = builder.build(fichierXML);

			Element rootNode = document.getRootElement();

			Element book=rootNode.getChild("book"+numChant);
			
			List<Element> listeSentences = book.getChildren("sentence");
			
			List<String> lemNames =new ArrayList<String>();
			List<String> formNames =new ArrayList<String>();
			Map<String, String> tags =new HashMap<String, String>();
			List<String> lems = new ArrayList<String>();
			List<String> forms = new ArrayList<String>();
			for (Element eSentence : listeSentences) {

				List<Element> listeWords = eSentence.getChildren("word");

				for (Element eWord : listeWords){
					
					String lemma = eWord.getAttributeValue("lemma");
					String form=eWord.getAttributeValue("form");
					
					if (lemma.startsWith("*")){
						lemNames.add(lemma);	
					}
					
					if (form.startsWith("*")){
						formNames.add(form);	
					}
					
					lems.add(lemma);		
					forms.add(form);
					String tag= eWord.getAttributeValue("postag");

					String abbreviate=tag.substring(0, 1);
					String replacement="null";
					if (abbreviate.startsWith("N"))replacement="NOM";
					if (abbreviate.startsWith("V"))replacement="VER:infi";
					if (abbreviate.startsWith("T"))replacement="VER:pper";
					if (abbreviate.startsWith("A"))replacement="ADJ";
					if (abbreviate.startsWith("D"))replacement="ADV";
					if (abbreviate.startsWith("L"))replacement="DET:ART";
					if (abbreviate.startsWith("G"))replacement="DET:ART";
					if (abbreviate.startsWith("C"))replacement="KON";
					if (abbreviate.startsWith("R"))replacement="PRP";
					if (abbreviate.startsWith("P"))replacement="PRO";
					if (abbreviate.startsWith("M"))replacement="NUM";
					if (abbreviate.startsWith("I"))replacement="INT";
					if (abbreviate.startsWith("E"))replacement="INT";
					if (abbreviate.startsWith("U"))replacement="SENT";
					tags.put(lemma,replacement);
				}
			}
			listNomsGrecsLemmaNForms.addAll(lemNames);
			nomsGrecs.addAll(lemNames);	
			lemNames.removeAll(Arrays.asList(blackList));
			StringBuilder sbLemmes = new StringBuilder();
			for (String lemme:lems){
				sbLemmes.append(lemme+" ");
			}
			
			StringBuilder sbForms = new StringBuilder();
			for (String form:forms){
				sbForms.append(form+" ");
			}

			String arLemNames[]=lemNames.toArray(new String [lemNames.size()]);
			String arFormNames[]=formNames.toArray(new String [formNames.size()]);
			NamesPatternMatcher namesMatcher= new NamesPatternMatcher(NamesPatternMatcher.DEF_PATTERN, sbLemmes.toString(), arLemNames);
			List<CharSequence> seqLems = new ArrayList<CharSequence>();
			seqLems = namesMatcher.getSequences();
			NamesPatternMatcher namesMatcherForms= new NamesPatternMatcher(NamesPatternMatcher.DEF_PATTERN, sbForms.toString(), arFormNames);
			List<CharSequence> seqForms = new ArrayList<CharSequence>();
			seqForms = namesMatcherForms.getSequences();

			StringBuilder sbTexteLemma=new StringBuilder();
			for (int index=0; index<seqLems.size(); index++){
				seqLems=regroup("*Phoebus", "*Apollo", seqLems, index);
				String source = seqLems.get(index).toString();
				sbTexteLemma.append(source+"//n");
			}
			
			StringBuilder sbTexteFlechi=new StringBuilder();
			for (int index=0; index<seqForms.size(); index++){
				seqForms=regroup("*Phoebus", "*Apollo", seqForms, index);
				String source = seqForms.get(index).toString();
				sbTexteFlechi.append(source+"//n");
			}
			String lemmas=sbTexteLemma.toString().replaceAll("\\.", " . ");
			String flechi=sbTexteFlechi.toString().replaceAll("\\.", " . ");
			flechi=flechi.replaceAll(",", " , ");
			lemmas=lemmas.replaceAll(",", " , ");
			flechi=flechi.replaceAll(";", " ; ");
			lemmas=lemmas.replaceAll(";", " ; ");
			flechi=flechi.replaceAll(":", " : ");
			lemmas=lemmas.replaceAll(":", " : ");
			flechi=flechi.replaceAll("\\?", " ? ");
			lemmas=lemmas.replaceAll("\\?", " ? ");
			flechi=flechi.replaceAll("\\]", "");
			lemmas=lemmas.replaceAll("\\]", "");
			flechi=flechi.replaceAll("\\[", "");
			lemmas=lemmas.replaceAll("\\[", "");
			flechi=flechi.replaceAll("\\s{2,}", " ");
			lemmas=lemmas.replaceAll("\\s{2,}", " ");
			Element root = new Element("root");
			Document doc = new Document(root);
			int counterID=1;
			int indexLemmaForm=0;
			for (String IDString:lemmas.split("//n")){
				Element ID=new Element("ID"+counterID);
				ID.setAttribute("text", flechi.split("//n")[indexLemmaForm].replace("*", ""));
				ID.setAttribute("lemma", IDString.replace("*", ""));
				StringBuilder sbTags=new StringBuilder();
				for (String key:IDString.split(" ")){			
					if (tags.containsKey(key)){
						sbTags.append(tags.get(key)+" ");
					}
					else if (key.length()>0) {
						sbTags.append("PUN ");
					}
				}	
				ID.setAttribute("tag", sbTags.toString().replaceAll("SENT ", "PUN ").replaceAll("\\s{2,}"," "));
				
//				if (ID.getAttributeValue("text").split(" ").length!=ID.getAttributeValue("tag").split(" ").length){
//					System.out.println("longueur src : "+ID.getAttributeValue("text").split(" ").length);
//					System.out.println("longueur tags : "+ID.getAttributeValue("tag").split(" ").length);
//				}
				root.addContent(ID);
				counterID++;
				indexLemmaForm++;
			}
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
				File fileOut = new File(TARGET+"/chant"+numChant+"/odysseelat1000_"+numChant+".xml");
				fileOut.getParentFile().mkdirs();
				xmlOutput.output(doc, new FileWriter(fileOut));
				File fileListNoms = new File(TARGETNAM+"odysseelat1000_"+numChant+".txt");
				fileListNoms.getParentFile().mkdirs();
				PrintWriter printWriterListNoms = new PrintWriter(fileListNoms);

				for (String nom:listNomsGrecsLemmaNForms){
					if (!nom.contains("SENT")){
						printWriterListNoms.println (nom);
					}
					
				}
				printWriterListNoms.close ();
		}
		File fileNoms = new File(NAMES+"/latinNames.txt");
		fileNoms.getParentFile().mkdirs();
		PrintWriter printWriterNoms = new PrintWriter(fileNoms);

		for (String nom:nomsGrecs){
			printWriterNoms.println (nom);
		}
		printWriterNoms.close ();
	}

	public List<CharSequence> regroup (String previous, String next ,List<CharSequence>sequencesTexteADecouper, int index){
		if (sequencesTexteADecouper.get(index).toString().matches("\\Q"+previous+" \\E")){
			if (index<sequencesTexteADecouper.size()&&sequencesTexteADecouper.get(index+1).toString().contains(next)){
				CharSequence replacement=sequencesTexteADecouper.get(index).toString()+sequencesTexteADecouper.get(index+1);
				sequencesTexteADecouper.remove(index);
				sequencesTexteADecouper.set(index, replacement);
			}
		}
		return sequencesTexteADecouper;
	}
}
