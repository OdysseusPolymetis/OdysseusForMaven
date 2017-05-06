package fr.odysseus.api;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.unc.epidoc.transcoder.TransCoder;
import fr.odysseus.utils.NamesPatternMatcher;

/**
 * gets all the attributes from Greek xml source files and rearrange them in suitable xml sequences for alignement
 * also converts from betacode to unicode
 */
public class LemmaGreek {

	final static String SOURCE="./input/xml/grxml/";
	final static String TARGETSEQ="./input/seq/grSeq/";
	final static String TARGETNAM="./input/names/grname/";
	public LemmaGreek() throws Exception{

		System.out.println("Début du tagging grec");
		
		File fileRoot=new File(SOURCE);
		File[] listeFilesChants=fileRoot.listFiles();
		List<String>listNomsGrecsLemmaNForms=new ArrayList<String>();
		HashSet<String>setNomsGrecsLemmaNForms=new HashSet<String>();

		for (File file:listeFilesChants){
			String numChant=file.getName().substring(file.getName().lastIndexOf("_")+1, file.getName().indexOf(".xml"));
			File fichierXML=new File(SOURCE+file.getName());
			SAXBuilder builder = new SAXBuilder();
			Document document;
			document = builder.build(fichierXML);

			Element rootNode = document.getRootElement();

			Element book=rootNode.getChild("book"+numChant);
			List<Element> listeSentences = book.getChildren("sentence");

			List<String> lemNames =new ArrayList<String>();
			List<String> formNames =new ArrayList <String>();
			Map<String, String> tags =new HashMap<String, String>();
			List<String> lems = new ArrayList<String>();
			List<String> forms = new ArrayList<String>();
			for (Element eSentence : listeSentences) {

				List<Element> listeWords = eSentence.getChildren("word");

				for (Element eWord : listeWords){

					String lemma = eWord.getAttributeValue("lemma");

					if (lemma.startsWith("*")){
						lemNames.add(lemma);	
					}
					
					lems.add(lemma);		

					String form = eWord.getAttributeValue("form");
					if (form.startsWith("*")){
						formNames.add(form);	
					}
					forms.add(form);	
					
					String tag= eWord.getAttributeValue("postag");
					
					String abbreviate=tag.substring(0, 1);
					String replacement="null";
					if (abbreviate.startsWith("n"))replacement="NOM";
					if (abbreviate.startsWith("v"))replacement="VER:infi";
					if (abbreviate.startsWith("t"))replacement="VER:pper";
					if (abbreviate.startsWith("a"))replacement="ADJ";
					if (abbreviate.startsWith("d"))replacement="ADV";
					if (abbreviate.startsWith("l"))replacement="DET:ART";
					if (abbreviate.startsWith("g"))replacement="DET:ART";
					if (abbreviate.startsWith("c"))replacement="KON";
					if (abbreviate.startsWith("r"))replacement="PRP";
					if (abbreviate.startsWith("p"))replacement="PRO";
					if (abbreviate.startsWith("m"))replacement="NUM";
					if (abbreviate.startsWith("i"))replacement="INT";
					if (abbreviate.startsWith("e"))replacement="INT";
					if (abbreviate.startsWith("u"))replacement="PUN";
					TransCoder tcKey=new TransCoder();
					tcKey.setParser("BetaCode");
					tcKey.setConverter("UnicodeC");
					String result = tcKey.getString(lemma);
					tags.put(result,replacement);
				}
			}
			
			listNomsGrecsLemmaNForms.addAll(lemNames);
			setNomsGrecsLemmaNForms.addAll(lemNames);
			
			
			for (int indexTrans=0;indexTrans<lems.size(); indexTrans++){
				TransCoder tc = new TransCoder();
				tc.setParser("BetaCode");
				tc.setConverter("UnicodeC");
				String resultLemma = "";
				String resultForm = "";
				if (lems.get(indexTrans).contains("SENT")){
					resultLemma=lems.get(indexTrans);
					resultForm=forms.get(indexTrans);
				}
				else if (lems.get(indexTrans).length()<1&&forms.get(indexTrans).length()>0){
					resultLemma = forms.get(indexTrans);
					resultForm = forms.get(indexTrans);
				}
				else if (lems.get(indexTrans).contains("comma")){
					resultLemma = ",";
					resultForm = ",";
				}
				else {
					resultLemma = tc.getString(lems.get(indexTrans));
					resultForm = tc.getString(forms.get(indexTrans));
				}
				lems.set(indexTrans, resultLemma);
				forms.set(indexTrans, resultForm);
			}
			
			for (int indexTrans=0;indexTrans<lemNames.size(); indexTrans++){
				TransCoder tc = new TransCoder();
				tc.setParser("BetaCode");
				tc.setConverter("UnicodeC");
				String resultLemma="";
				String resultForm="";
				if (lemNames.get(indexTrans).contains("SENT")){
					resultLemma=lemNames.get(indexTrans);
					resultForm=formNames.get(indexTrans);
				}
				else if (!lemNames.get(indexTrans).contains("comma")&&!lemNames.get(indexTrans).contains("punc")){
					resultLemma = tc.getString(lemNames.get(indexTrans));
					resultForm = tc.getString(formNames.get(indexTrans));
				}
				
				else{
					resultLemma=lemNames.get(indexTrans);
					resultForm=formNames.get(indexTrans);
				}
				lemNames.set(indexTrans, resultLemma);
				formNames.set(indexTrans, resultForm);
			}
			
			StringBuilder sblemmes = new StringBuilder();
			for (String nom:lems){
				sblemmes.append(nom+" ");
			}

			String namesLemma[]=lemNames.toArray(new String [lemNames.size()]);
			NamesPatternMatcher namesMatcherLemma= new NamesPatternMatcher(NamesPatternMatcher.DEF_PATTERN, sblemmes.toString(), namesLemma);
			List<CharSequence> seqLem = new ArrayList<CharSequence>();
			seqLem = namesMatcherLemma.getSequences();
			
			StringBuilder sbforms = new StringBuilder();
			for (String nom:forms){
				sbforms.append(nom+" ");
			}
			
			String namesForm[]=formNames.toArray(new String [formNames.size()]);
			NamesPatternMatcher namesMatcherForm= new NamesPatternMatcher(NamesPatternMatcher.DEF_PATTERN, sbforms.toString(), namesForm);
			List<CharSequence> seqform = new ArrayList<CharSequence>();
			seqform = namesMatcherForm.getSequences();
			
			Element root = new Element("root");
			Document doc = new Document(root);
			
			int counterID=1;
			
			for (int index=0; index<seqLem.size(); index++){
				Element ID=new Element("ID"+counterID);
				ID.setAttribute("text", seqform.get(index).toString().replaceAll("\\s{2,}"," "));
				ID.setAttribute("lemma", seqLem.get(index).toString().replaceAll("\\s{2,}"," "));
				
				
				StringBuilder sbTags=new StringBuilder();
				for (String key:seqLem.get(index).toString().split(" ")){
					if (tags.containsKey(key)){
						sbTags.append(tags.get(key)+" ");
					}
					else if (key.length()>0){
						sbTags.append("PUN ");
					}
				}		
				ID.setAttribute("tag", sbTags.toString().replaceAll("SENT ", "PUN ").replaceAll("\\s{2,}"," "));
				if (ID.getAttributeValue("tag").split(" ").length!=ID.getAttributeValue("lemma").split(" ").length){
					System.out.println(ID.getAttributeValue("tag"));
					System.out.println(ID.getAttributeValue("tag").split(" ").length);
					System.out.println(ID.getAttributeValue("lemma"));
					System.out.println(ID.getAttributeValue("lemma").split(" ").length);
				}
				root.addContent(ID);
				counterID++;
			}
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
				File fileOut = new File(TARGETSEQ+"chant"+numChant+"/odyssee1000_"+numChant+".xml");
				fileOut.getParentFile().mkdirs();
				xmlOutput.output(doc, new FileWriter(fileOut));
				File fileListNoms = new File(TARGETNAM+"odyssee1000_"+numChant+".txt");
				fileListNoms.getParentFile().mkdirs();
				PrintWriter printWriterListNoms = new PrintWriter(fileListNoms);

				for (String nom:listNomsGrecsLemmaNForms){
					if (!nom.contains("SENT")){
						printWriterListNoms.println (nom);
					}
					
				}
				printWriterListNoms.close ();

			System.out.println("Done : "+file.getName());
		}
		File fileSetNoms = new File(TARGETNAM+"GreekNames.txt");
		fileSetNoms.getParentFile().mkdirs();
		PrintWriter printWriterSetNoms = new PrintWriter(fileSetNoms);

		for (String nom:setNomsGrecsLemmaNForms){
			if (!nom.contains("SENT")){
				printWriterSetNoms.println (nom);
			}
		}
		printWriterSetNoms.close ();
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
