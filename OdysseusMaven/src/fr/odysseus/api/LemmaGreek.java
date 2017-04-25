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

	final static String SOURCE="./sourceFiles/xml/GreekXML/";
	final static String TARGETSEQ="./sourceFiles/sequences/greekSequences/";
	final static String TARGETNAM="./sourceFiles/names/greekNames/";
	public LemmaGreek() throws Exception{

		System.out.println("Début du tagging grec");
		
		File fileRoot=new File(SOURCE);
		File[] listeFilesChants=fileRoot.listFiles();
		List<String>listNomsGrecsLemmaNForms=new ArrayList<String>();
		HashSet<String>setNomsGrecsLemmaNForms=new HashSet<String>();

		for (File file:listeFilesChants){
			String numChant=file.getName().substring(file.getName().lastIndexOf("Chant")+5, file.getName().indexOf(".xml"));
			File fichierXML=new File(SOURCE+file.getName());
			SAXBuilder builder = new SAXBuilder();
			Document document;
			document = builder.build(fichierXML);

			Element rootNode = document.getRootElement();

			Element book=rootNode.getChild("book"+numChant);
			List<Element> listeSentences = book.getChildren("sentence");

			List<String> lemmes =new ArrayList<String>();
			List<String> forms =new ArrayList <String>();
			Map<String, String> tags =new HashMap<String, String>();
			List<String> listeIntegraleLemma = new ArrayList<String>();
			List<String> listeIntegraleForm = new ArrayList<String>();
			for (Element eSentence : listeSentences) {

				List<Element> listeWords = eSentence.getChildren("word");

				for (Element eWord : listeWords){

					String lemma = eWord.getAttributeValue("lemma");
//					System.out.println(lemma);
//					lemma=lemma.replace("h(=os", "*h(=os");
//					lemma=lemma.replace(")hw/s", "*)hw/s");
//					lemma=lemma.replace("**", "*");
//					lemma=lemma.replaceAll("1", "");
//					lemma=lemma.replace("+/", "/");
//					lemma=lemma.replace("|", "");
//					lemma=lemma.replace("/+", "/");
//					lemma=lemma.replace("comma","");
//					lemma=lemma.replace("period","");
//					lemma=lemma.replace("punc","");

					if (lemma.startsWith("*")){
						lemmes.add(lemma);	
					}
					
					listeIntegraleLemma.add(lemma);		

					String form = eWord.getAttributeValue("form");
//					System.out.println(form);
//					System.out.println("************************");
//					form=form.replaceAll("1", "");
//					form=form.replace("+/", "/");
//					form=form.replace("|", "");
//					form=form.replace("/+", "/");
					if (form.startsWith("*")){
						forms.add(form);	
					}
					listeIntegraleForm.add(form);	
					
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
					if (abbreviate.startsWith("u"))replacement="";
					TransCoder tcKey=new TransCoder();
					tcKey.setParser("BetaCode");
					tcKey.setConverter("UnicodeC");
					String result = tcKey.getString(lemma);
					tags.put(result,replacement);
				}
				
//				listeIntegraleForm.add("SENT");
//				listeIntegraleLemma.add("SENT");
//				lemmes.add("SENT");
//				forms.add("SENT");
			}
			
			
//			lemmes.removeAll(Arrays.asList(blackList));
			listNomsGrecsLemmaNForms.addAll(lemmes);
			setNomsGrecsLemmaNForms.addAll(lemmes);
//			nomsGrecsLemmaNForms.addAll(forms);
			
			
			for (int indexTrans=0;indexTrans<listeIntegraleLemma.size(); indexTrans++){
				TransCoder tc = new TransCoder();
				tc.setParser("BetaCode");
				tc.setConverter("UnicodeC");
				String resultLemma = "";
				String resultForm = "";
				if (listeIntegraleLemma.get(indexTrans).contains("SENT")){
					resultLemma=listeIntegraleLemma.get(indexTrans);
					resultForm=listeIntegraleForm.get(indexTrans);
				}
				else if (!listeIntegraleLemma.get(indexTrans).contains("comma")&&!listeIntegraleLemma.get(indexTrans).contains("punc")){
					resultLemma = tc.getString(listeIntegraleLemma.get(indexTrans));
					resultForm = tc.getString(listeIntegraleForm.get(indexTrans));
				}

				else{
					resultLemma=listeIntegraleLemma.get(indexTrans);
					resultForm=listeIntegraleForm.get(indexTrans);
				}
				listeIntegraleLemma.set(indexTrans, resultLemma);
				listeIntegraleForm.set(indexTrans, resultForm);
			}
			
			for (int indexTrans=0;indexTrans<lemmes.size(); indexTrans++){
				TransCoder tc = new TransCoder();
				tc.setParser("BetaCode");
				tc.setConverter("UnicodeC");
				String resultLemma="";
				String resultForm="";
				if (lemmes.get(indexTrans).contains("SENT")){
					resultLemma=lemmes.get(indexTrans);
					resultForm=forms.get(indexTrans);
				}
				else if (!lemmes.get(indexTrans).contains("comma")&&!lemmes.get(indexTrans).contains("punc")){
					resultLemma = tc.getString(lemmes.get(indexTrans));
					resultForm = tc.getString(forms.get(indexTrans));
				}
				else{
					resultLemma=lemmes.get(indexTrans);
					resultForm=forms.get(indexTrans);
				}
				
				lemmes.set(indexTrans, resultLemma);
				forms.set(indexTrans, resultForm);
			}
			
			StringBuilder sblemmes = new StringBuilder();
			for (String nom:listeIntegraleLemma){
				sblemmes.append(nom+" ");
			}

			String namesLemma[]=lemmes.toArray(new String [lemmes.size()]);
			NamesPatternMatcher namesMatcherLemma= new NamesPatternMatcher(NamesPatternMatcher.DEF_PATTERN, sblemmes.toString(), namesLemma);
			List<CharSequence> sequencesLemma = new ArrayList<CharSequence>();
			sequencesLemma = namesMatcherLemma.getSequences();
			
			StringBuilder sbforms = new StringBuilder();
			for (String nom:listeIntegraleForm){
				sbforms.append(nom+" ");
			}
			
			String namesForm[]=forms.toArray(new String [forms.size()]);
			NamesPatternMatcher namesMatcherForm= new NamesPatternMatcher(NamesPatternMatcher.DEF_PATTERN, sbforms.toString(), namesForm);
			List<CharSequence> sequencesForm = new ArrayList<CharSequence>();
			sequencesForm = namesMatcherForm.getSequences();
			
			Element root = new Element("root");
			Document doc = new Document(root);
			
			int counterID=1;
			
			for (int index=0; index<sequencesLemma.size(); index++){
				Element ID=new Element("ID"+counterID);
				String flexedSeq=sequencesForm.get(index).toString();
				String lemmatisedSeq=sequencesLemma.get(index).toString();
				flexedSeq=flexedSeq.replaceAll(" , ",", ");
				flexedSeq=flexedSeq.replace(" · "," ");
				flexedSeq=flexedSeq.replaceAll(" \\. ",". ");
				flexedSeq=flexedSeq.replaceAll("\\s+;\\s*","; ");
				flexedSeq=flexedSeq.replaceAll("\\s+-\\s*","- ");
				flexedSeq=flexedSeq.replaceAll("\\s+ʼ\\s*","' ");
				flexedSeq=flexedSeq.replaceAll("\\s+'\\s*","' ");
				flexedSeq=flexedSeq.replaceAll(" : ",": ");
				flexedSeq=flexedSeq.replaceAll("[\\s,;:'ʼ.-°]*°[\\s,;:'ʼ.°-]*","° ");
				lemmatisedSeq=lemmatisedSeq.replaceAll(" , ",", ");
				lemmatisedSeq=lemmatisedSeq.replace(" · "," ");
				lemmatisedSeq=lemmatisedSeq.replaceAll(" \\. ",". ");
				lemmatisedSeq=lemmatisedSeq.replaceAll(" ; ","; ");
				lemmatisedSeq=lemmatisedSeq.replaceAll(" : ",": ");
				lemmatisedSeq=lemmatisedSeq.replaceAll("\\s+'\\s*","' ");
				lemmatisedSeq=lemmatisedSeq.replaceAll("\\s{2,}"," ");
				lemmatisedSeq=lemmatisedSeq.replaceAll("[\\s,;:·'ʼ.-°]*°[\\s,;:·'ʼ.°-]*","° ");
				ID.setAttribute("text", flexedSeq.replaceAll("SENT ", ""));
				ID.setAttribute("lemma", lemmatisedSeq.replaceAll("SENT ", ""));
				StringBuilder sbTags=new StringBuilder();
				for (String key:sequencesLemma.get(index).toString().split(" ")){
					if (tags.containsKey(key)){
						sbTags.append(tags.get(key)+" ");
					}
				}		
				ID.setAttribute("tag", sbTags.toString().replaceAll("SENT ", "").replaceAll("\\s{2,}"," "));
				root.addContent(ID);
				counterID++;
			}
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			if (Integer.parseInt(numChant)<10){
				File fileOut = new File(TARGETSEQ+"Chant0"+numChant+"/Odyssee1000Chant0"+numChant+"NomsCoupe.xml");
				fileOut.getParentFile().mkdirs();
				xmlOutput.output(doc, new FileWriter(fileOut));
				File fileListNoms = new File(TARGETNAM+"Odyssee1000Chant0"+numChant+".txt");
				fileListNoms.getParentFile().mkdirs();
				PrintWriter printWriterListNoms = new PrintWriter(fileListNoms);

				for (String nom:listNomsGrecsLemmaNForms){
					if (!nom.contains("SENT")){
						printWriterListNoms.println (nom);
					}
					
				}
				printWriterListNoms.close ();
			}
			else{
				File fileOut = new File(TARGETSEQ+"Chant"+numChant+"/Odyssee1000Chant"+numChant+"NomsCoupe.xml");
				fileOut.getParentFile().mkdirs();
				xmlOutput.output(doc, new FileWriter(fileOut));
				File fileListNoms = new File(TARGETNAM+"Odyssee1000Chant"+numChant+".txt");
				fileListNoms.getParentFile().mkdirs();
				FileWriter printWriterListNoms = new FileWriter(fileListNoms,false);

				for (String nom:listNomsGrecsLemmaNForms){
					if (!nom.contains("SENT")){
						printWriterListNoms.write (nom);
					}
				}
				printWriterListNoms.close ();
			}
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
