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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fr.odysseus.utils.NamesPatternMatcher;

/**
 * gets all the attributes and rearrange them in suitable xml sequences for alignement
 */
public class CollectFrenchSequences {
	final static String SOURCE="./input/xml/frxml/";
	final static String TARGET="./input/";
	final static String DICOVEK="./input/dict/dicovek/";
	final static String W2V="./input/dict/wtov/";
	final static String PIVOT="./input/seq/pivot/";
	public CollectFrenchSequences() throws Exception{
		System.out.println("Début du ramassage des tags et du séquençage");
		File fileRoot=new File(SOURCE);
		File[] listeFilesChants=fileRoot.listFiles();
		List<String>listNomsGrecsLemmaNForms=new ArrayList<String>();
		HashSet<String>setNomsGrecsLemmaNForms=new HashSet<String>();
		String blackList[]={ ""};
		HashSet <String> namesCompleteSet=new HashSet<String>();
		List<CharSequence>word2VecTraining=new ArrayList<>();

		for (File file:listeFilesChants){
			String fileName=file.getName().substring(0,file.getName().lastIndexOf("_"));
			String numChant=file.getName().substring(file.getName().lastIndexOf("_")+1, file.getName().indexOf(".xml"));
			File fichierXML=new File(SOURCE+file.getName());
			SAXBuilder builder = new SAXBuilder();
			Document document= builder.build(fichierXML);

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

					String postag = eWord.getAttributeValue("postag");
					String lemme=eWord.getAttributeValue("lemma");
					lemme=lemme.replaceAll("à", "a");
					lemme=lemme.replaceAll("é", "e");
					lemme=lemme.replaceAll("è", "e");
					lemme=lemme.replaceAll("â", "a");
					lemme=lemme.replaceAll("ù", "u");
					lemme=lemme.replaceAll("é", "e");
					lemme=lemme.replaceAll("ê", "e");
					if (postag.contains("NAM")){
						namesCompleteSet.add(lemme);
						lemmes.add(lemme);
						forms.add(eWord.getAttributeValue("form"));
					}
					listeIntegraleLemma.add(lemme);		
					listeIntegraleForm.add(eWord.getAttributeValue("form"));	
					tags.put(lemme,postag);
				}
			}

			lemmes.removeAll(Arrays.asList(blackList));
			listNomsGrecsLemmaNForms.addAll(lemmes);
			setNomsGrecsLemmaNForms.addAll(lemmes);

			for (int indexTrans=0;indexTrans<listeIntegraleLemma.size(); indexTrans++){
				String resultLemma = "";
				String resultForm = "";
				if (!listeIntegraleLemma.get(indexTrans).matches("comma")&&!listeIntegraleLemma.get(indexTrans).matches("punc")){
					resultLemma = listeIntegraleLemma.get(indexTrans);
					resultForm = listeIntegraleForm.get(indexTrans);
				}

				else{
					resultLemma="";
					resultForm=listeIntegraleForm.get(indexTrans);
				}
				listeIntegraleLemma.set(indexTrans, resultLemma);
				listeIntegraleForm.set(indexTrans, resultForm);
			}

			for (int indexTrans=0;indexTrans<lemmes.size(); indexTrans++){
				String resultLemma="";
				String resultForm="";
				if (!lemmes.get(indexTrans).matches("comma")&&!lemmes.get(indexTrans).matches("punc")){
					resultLemma = lemmes.get(indexTrans);
					resultForm = forms.get(indexTrans);
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

			word2VecTraining.addAll(sequencesLemma);


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
				flexedSeq=flexedSeq.replaceAll("\\s{2,}"," ");
				lemmatisedSeq=lemmatisedSeq.replaceAll("\\s{2,}"," ");
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
			File fileOut = new File(TARGET+"/seq/frSeq/chant"+numChant+"/"+fileName.toLowerCase()+"_"+numChant+".xml");
			fileOut.getParentFile().mkdirs();
			xmlOutput.output(doc, new FileWriter(fileOut));
			File fileListNoms = new File(TARGET+"/names/frname/"+fileName.toLowerCase()+"_"+numChant+".txt");
			fileListNoms.getParentFile().mkdirs();
			PrintWriter printWriterListNoms = new PrintWriter(fileListNoms);
			
			for (String nom:listNomsGrecsLemmaNForms){
				if (!nom.equals("SENT")){
					printWriterListNoms.println (nom);
				}
			}
			printWriterListNoms.close ();

			File sequences = new File(TARGET+"/seq/frSeq/chant"+numChant+"/"+fileName.toLowerCase()+"_"+numChant+".txt");
			sequences.getParentFile().mkdirs();
			PrintWriter printSequences = new PrintWriter(sequences);
			for (CharSequence sequence:sequencesForm){
				printSequences.println(sequence.toString());
			}
			printSequences.close ();

			File sequencesPrintLemma = new File(DICOVEK+"chant"+numChant+"/"+fileName+"_"+numChant+"Lemma.txt");
			sequencesPrintLemma.getParentFile().mkdirs();
			PrintWriter printLemmatizedSequences = new PrintWriter(sequencesPrintLemma);
			for (CharSequence sequence:sequencesLemma){
				printLemmatizedSequences.println(sequence.toString().replaceAll("SENT", ""));
			}
			printLemmatizedSequences.close ();

			if (fileName.contains("sommer")){
				File pivotSequences = new File(PIVOT+"chant"+numChant+"/"+fileName.toLowerCase()+"_"+numChant+".txt");
				pivotSequences.getParentFile().mkdirs();
				PrintWriter printPivotSequences = new PrintWriter(pivotSequences);
				for (CharSequence sequence:sequencesForm){
					printPivotSequences.println(sequence.toString());
				}
				printPivotSequences.close ();
				XMLOutputter xmlPivot = new XMLOutputter();
				xmlPivot.setFormat(Format.getPrettyFormat());
				File filePivot = new File(PIVOT+"chant"+numChant+"/"+fileName.toLowerCase()+"_"+numChant+".xml");
				filePivot.getParentFile().mkdirs();
				xmlPivot.output(doc, new FileWriter(filePivot));
			}
			System.out.println("Done for : "+fileName);
		}
		File completeSet = new File(TARGET+"names/frname/FrenchNames.txt");
		completeSet.getParentFile().mkdirs();
		FileWriter printCompleteSet = new FileWriter(completeSet, false);
//		namesCompleteSet.remove("SENT");
		for (String nom:namesCompleteSet){
			printCompleteSet.write (nom+"\n");
		}
		printCompleteSet.close ();

		File w2V = new File(W2V+"RepertoireW2V.txt");
		w2V.getParentFile().mkdirs();
		PrintWriter printW2V = new PrintWriter(w2V);

		for (CharSequence nom:word2VecTraining){
			printW2V.println (nom.toString());
		}
		printW2V.close ();
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
