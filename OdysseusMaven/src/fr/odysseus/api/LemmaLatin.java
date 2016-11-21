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
	final static String SOURCE="./sourceFiles/xml/LatinXML";
	final static String TARGET="./sourceFiles/sequences/latinSequences";
	final static String NAMES="./sourceFiles/names/latinNames/";
	public LemmaLatin() throws Exception{

		File fileRoot=new File(SOURCE);
		File[] listeFilesChants=fileRoot.listFiles();
		Set<String>nomsGrecs=new HashSet<String>();
		String blackList[]={ ""};

		for (File file:listeFilesChants){
			System.out.println(file.getName());
			String numChant=file.getName().substring(file.getName().lastIndexOf("Chant")+5, file.getName().indexOf(".xml"));
			File fichierXML=new File(SOURCE+file.getName());
			SAXBuilder builder = new SAXBuilder();
			Document document;

			document = builder.build(fichierXML);

			Element rootNode = document.getRootElement();

			Element book=rootNode.getChild("book"+numChant);
			
			List<Element> listeSentences = book.getChildren("sentence");
			
			List<String> nomsLemmatises =new ArrayList<String>();
			List<String> nomsFlechis =new ArrayList<String>();
			Map<String, String> tags =new HashMap<String, String>();
			List<String> listeTousLemmes = new ArrayList<String>();
			List<String> listeToutesFormes = new ArrayList<String>();
			for (Element eSentence : listeSentences) {

				List<Element> listeWords = eSentence.getChildren("word");

				for (Element eWord : listeWords){
					
					String motLemmatise = eWord.getAttributeValue("lemma");
					String motFlechi=eWord.getAttributeValue("form");
					
					if (motLemmatise.startsWith("*")){
						nomsLemmatises.add(motLemmatise);	
					}
					
					if (motFlechi.startsWith("*")){
						nomsFlechis.add(motFlechi);	
					}
					
					listeTousLemmes.add(motLemmatise);		
					listeToutesFormes.add(motFlechi);
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
					if (abbreviate.startsWith("U"))replacement="";
					tags.put(motLemmatise,replacement);
				}
			}
			
			nomsGrecs.addAll(nomsLemmatises);	
			nomsLemmatises.removeAll(Arrays.asList(blackList));
			StringBuilder sbLemmes = new StringBuilder();
			for (String lemme:listeTousLemmes){
				sbLemmes.append(lemme+" ");
			}
			
			StringBuilder sbForms = new StringBuilder();
			for (String form:listeToutesFormes){
				sbForms.append(form+" ");
			}

			String tableauNomsLemmatises[]=nomsLemmatises.toArray(new String [nomsLemmatises.size()]);
			String tableauNomsFlechis[]=nomsFlechis.toArray(new String [nomsFlechis.size()]);
			NamesPatternMatcher namesMatcher= new NamesPatternMatcher(NamesPatternMatcher.DEF_PATTERN, sbLemmes.toString(), tableauNomsLemmatises);
			List<CharSequence> sequencesTexteADecouper = new ArrayList<CharSequence>();
			sequencesTexteADecouper = namesMatcher.getSequences();
			NamesPatternMatcher namesMatcherForms= new NamesPatternMatcher(NamesPatternMatcher.DEF_PATTERN, sbForms.toString(), tableauNomsFlechis);
			List<CharSequence> texteFlechiADecouper = new ArrayList<CharSequence>();
			texteFlechiADecouper = namesMatcherForms.getSequences();

			StringBuilder sbTexteLemma=new StringBuilder();
			for (int index=0; index<sequencesTexteADecouper.size(); index++){
				sequencesTexteADecouper=regroup("*Phoebus", "*Apollo", sequencesTexteADecouper, index);
				String source = sequencesTexteADecouper.get(index).toString();
				sbTexteLemma.append(source+"//n");
			}
			
			StringBuilder sbTexteFlechi=new StringBuilder();
			for (int index=0; index<texteFlechiADecouper.size(); index++){
				texteFlechiADecouper=regroup("*Phoebus", "*Apollo", texteFlechiADecouper, index);
				String source = texteFlechiADecouper.get(index).toString();
				sbTexteFlechi.append(source+"//n");
			}

			Element root = new Element("root");
			Document doc = new Document(root);
			int counterID=1;
			int indexLemmaForm=0;
			for (String IDString:sbTexteLemma.toString().split("//n")){
				Element ID=new Element("ID"+counterID);
				ID.setAttribute("text", sbTexteFlechi.toString().split("//n")[indexLemmaForm].replace("*", ""));
				ID.setAttribute("lemma", IDString.replace("*", ""));
				StringBuilder sbTags=new StringBuilder();
				for (String key:IDString.split(" ")){
					if (tags.containsKey(key)){
						sbTags.append(tags.get(key)+" ");
					}
				}		
				ID.setAttribute("tag", sbTags.toString().replaceAll("  ", " "));
				root.addContent(ID);
				counterID++;
				indexLemmaForm++;
			}
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			if (Integer.parseInt(numChant)<10){
				File fileOut = new File(TARGET+"/Chant0"+numChant+"/OdysseeLat1000Chant0"+numChant+"NomsCoupe.xml");
				fileOut.getParentFile().mkdirs();
				xmlOutput.output(doc, new FileWriter(fileOut));
			}
			else{
				File fileOut = new File(TARGET+"/Chant"+numChant+"/OdysseeLat1000Chant"+numChant+"NomsCoupe.xml");
				fileOut.getParentFile().mkdirs();
				xmlOutput.output(doc, new FileWriter(fileOut));
			}
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
