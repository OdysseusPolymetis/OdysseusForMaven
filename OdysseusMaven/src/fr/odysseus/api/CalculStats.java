package fr.odysseus.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.simmetrics.metrics.GeneralizedJaccard;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import fr.odysseus.utils.CreateFiles;
import fr.odysseus.utils.LevenshteinDistance;
import fr.odysseus.utils.ListageRepertoire;

public class CalculStats {
	File repertoireSource[];
	public HashMap<String, HashMap<String, Set<String>>> listSecondDict;

	public HashMap<Element, List<Element>> distribDict;


	JFrame frame;
	JFrame frameDict;
	Double nombreDHapax;
	Double expFreq;
	Double nombreDeMots;
	StringBuffer editor;
	StringBuffer doc;
	StringBuffer docSax;

	StringBuffer kit;

	StringBuffer scroll;
	StringBuffer racine;
	StringBuffer nomFile;
	LinkedList<JEditorPane>listeDesPanes;

	SAXBuilder xmlBuilder;

	List<String>eachHTML;


	public Element setRoot(File file) throws IOException, JDOMException {
		SAXBuilder sxb=new SAXBuilder();
		Document doc=sxb.build(file);
		Element racine=doc.getRootElement();
		return racine;
	}

	public String getFileBaseName(File file){
		String name=FilenameUtils.getBaseName(file.getName());
		return name;
	}
	public HashMap<Element, List<Element>> getDefaultDistribDict(String path) throws JDOMException, IOException{
		distribDict=new HashMap<Element, List<Element>>();
		Document document = xmlBuilder.build(new File(path));
		Element rootNode = document.getRootElement();
		CreateFiles.rootToDict(rootNode, distribDict);

		return distribDict;
	}
	public void initContentEditorPanes() throws IOException, BadLocationException, JDOMException{
		repertoireSource=ListageRepertoire.listeRepertoire(new File("./Output/XML"));
		LinkedList<Element>listeDesRacines=new LinkedList<Element>();
		LinkedList<String>listeDesNoms=new LinkedList<String>();

		for( int i = 0; i < repertoireSource.length; i++ ) {
			Element racine=setRoot(repertoireSource[i]);
			listeDesRacines.add(racine);
			String name = getFileBaseName(repertoireSource[i]);
			listeDesNoms.add(name);
		}
		insertHTML(listeDesRacines, listeDesNoms);
	}


	public void insertHTML(LinkedList<Element>racines, LinkedList<String>fichiers) throws BadLocationException, IOException, JDOMException

	{
		Path path=Paths.get("./Dictionnaire/stopWords.txt");
		List<String>stopWords=Files.readAllLines(path);

//		EuclideanDistance<String>euclidian=new EuclideanDistance<>();
//		CosineSimilarity<String> cosine=new CosineSimilarity<>();
//		StringDistance hamming=new StringDistance();
//		JaroWinkler jaro=new JaroWinkler();
//		Jaccard<String>jaccard=new Jaccard<>();
		GeneralizedJaccard<String>jaccard=new GeneralizedJaccard<>();
		LevenshteinDistance distance=new LevenshteinDistance();
		for (int i=0; i<racines.size();i++){
			String nomFichierEnCours=racines.get(i).getAttributeValue("name");
			String nomCommun=nomFichierEnCours.substring(nomFichierEnCours.indexOf("Chant"));

			File dir=new File("./Output/XML/");
			File[] filesEnCours = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(nomCommun+".xml");
				}
			});
			int nombreDeFichiersAComparer=filesEnCours.length;

			System.out.println(nomFichierEnCours);
			XPathExpression<Element> expr;
			XPathExpression<Element> exprPrevious = null;
			XPathExpression<Element> exprNext= null;
			List<Document>autresDocs=new ArrayList<Document>();
			for (int counterAutresDocs=0; counterAutresDocs<repertoireSource.length;counterAutresDocs++){	  
				if (counterAutresDocs!=i&&repertoireSource[counterAutresDocs].getName().contains(nomCommun)){
					File autreDoc=repertoireSource[counterAutresDocs];
					SAXBuilder jdomBuild = new SAXBuilder();
					Document jdomDoc  = jdomBuild.build(autreDoc);
					autresDocs.add(jdomDoc);
				}	 
			}

			List<Element>listeElementsActuels=racines.get(i).getChildren();

			XPathFactory xFactory = XPathFactory.instance();

			new HashMap<Integer, List<String>>();
			List<Element>listIDs=racines.get(i).getChildren();
			int counterID=1;

			new ArrayList<String>();


			List<String>ids=new ArrayList<String>();
			for (Element ID:listIDs){
				List<String>idsInHTMLFormat=new ArrayList<String>();
				LinkedHashMap<String[], String[]>tableauDeCorrespondances=new LinkedHashMap<String[], String[]>();

				List<Element>total=new ArrayList<Element>();
				new ArrayList<List<Element>>();
				StringBuilder stringbuild=new StringBuilder();

				String chantActuel=nomFichierEnCours.substring(nomFichierEnCours.indexOf("Chant"));
				SAXBuilder sxb=new SAXBuilder();
				Document documentSource=sxb.build(new File("./Source/GreekPunct/Odyssee1000"+chantActuel+".xml"));
				Element racineSource=documentSource.getRootElement();
				Element IDSource=racineSource.getChild("ID"+counterID);
				String []tagSource=IDSource.getAttributeValue("tag").split(" ");
				String tagActuel[]=listeElementsActuels.get(counterID-1).getAttributeValue("tag").split(" ");
				List<String>listeTagSource=new ArrayList<String>(Arrays.asList(tagSource));
				List<String>listeTagTarget=new ArrayList<String>(Arrays.asList(tagActuel));
				listeTagSource.remove("PUN");
				listeTagTarget.remove("PUN");
				listeTagSource.remove("Indef");
				listeTagTarget.remove("Indef");
				listeTagTarget.remove("DET:ART");
				listeTagTarget.remove("DET:POS");
				listeTagTarget.remove("PRP:det");

//				String taggedTarget=IDSource.getAttributeValue("tag");
//				String taggedSource=listeElementsActuels.get(counterID-1).getAttributeValue("tag");
//				taggedSource=taggedSource.replaceAll("PUN", "");
//				taggedSource=taggedSource.replaceAll("Indef", "");
//				taggedSource=taggedSource.replaceAll("\\s{2,}", " ");
//				taggedTarget=taggedTarget.replaceAll("PUN", "");
//				taggedTarget=taggedTarget.replaceAll("Indef", "");
//				taggedTarget=taggedTarget.replaceAll("DET:[A-Z]+", "");
//				taggedTarget=taggedTarget.replaceAll("PRP:det", "");
//				taggedTarget=taggedTarget.replaceAll("\\s{2,}", " ");
				int percentDist=0;
//				Set <String>vecteurSource=new HashSet<String>();
//				vecteurSource.addAll(listeTagSource);
//				Set <String>vecteurTarget=new HashSet<String>();
//				vecteurSource.addAll(listeTagTarget);
				
				Multiset <String>vecteurSource=HashMultiset.create(listeTagSource);
				Multiset <String>vecteurTarget=HashMultiset.create(listeTagTarget);

				float similarity=jaccard.compare(vecteurSource,vecteurTarget);

//				System.out.println(similarity);
				if (similarity>0.2&&similarity<0.4){
					percentDist=1;
				}
				else if (similarity>0.4&&similarity<0.6){
					percentDist=2;
				}
				else if (similarity>0.6&&similarity<0.7){
					percentDist=3;
				}
				else if (similarity>0.7&&similarity<0.8){
					percentDist=4;
				}
				else if (similarity>0.8){
					percentDist=5;
				}
				
//				else {
//					percentDist=((Math.round((similarity*100/nombreDeFichiersAComparer)/5)));
//				}
				
//				percentDist=((Math.round((similarity*100/nombreDeFichiersAComparer)/5)))+2;

				if (Math.abs(listeTagSource.size()-listeTagTarget.size())>15&&percentDist>3){
					percentDist=percentDist-3;
				}

				else if (Math.abs(listeTagSource.size()-listeTagTarget.size())>10&&percentDist>2){
					percentDist=percentDist-2;
				}

				else if (Math.abs(listeTagSource.size()-listeTagTarget.size())>5&&percentDist>1){
					percentDist--;
				}

				expr = xFactory.compile("/file/ID"+(counterID)+"[@lemma]", Filters.element());
				if (counterID>0){
					exprPrevious=xFactory.compile("/file/ID"+(counterID-1)+"[@lemma]", Filters.element());
				}
				if (counterID<listIDs.size()){
					exprNext=xFactory.compile("/file/ID"+(counterID+1)+"[@lemma]", Filters.element());
				}
				for (Document docToTest:autresDocs){
					List<Element> elementsToCompare=expr.evaluate(docToTest);
					List<Element> elementsToComparePrevious=exprPrevious.evaluate(docToTest);
					List<Element> elementsToCompareNext=exprNext.evaluate(docToTest);
					total.addAll(elementsToCompareNext);
					total.addAll(elementsToComparePrevious);
					total.addAll(elementsToCompare);
				} 


				for (Element last:total){
					if (!last.getAttributeValue("lemma").equals("^")){
						stringbuild.append(last.getAttributeValue("lemma")+" ");
					}
				}
				String lemmaAndForm[]=new String[2];
				String othersAndLeven[]=new String[2];
				lemmaAndForm[0]=ID.getAttributeValue("lemma");
				lemmaAndForm[1]=ID.getAttributeValue("text");
				othersAndLeven[0]=stringbuild.toString();
				othersAndLeven[1]=String.valueOf(percentDist);
				tableauDeCorrespondances.put(lemmaAndForm, othersAndLeven);	
				String sourceFileName=("./Output/HTML/"+racines.get(i).getAttributeValue("name").toLowerCase());
				String fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.indexOf("chant"))+"_"+sourceFileName.substring(sourceFileName.indexOf("chant")+5,sourceFileName.indexOf("noms"));
				for (Entry<String[], String[]>entry:tableauDeCorrespondances.entrySet()){

					
					entry.getKey()[1]=entry.getKey()[1].replaceAll("[0-9]", "");
					entry.getKey()[0]=entry.getKey()[0].replaceAll("[0-9]", "");
					entry.getKey()[1]=entry.getKey()[1].replaceAll("\\s{2,}", " ");
					entry.getKey()[0]=entry.getKey()[0].replaceAll("\\s{2,}", " ");
					String tableauMotsEnAnalyseLemmas[]=entry.getKey()[0].split("\\s");
					String tableauMotsEnAnalyseForms[]=entry.getKey()[1].split("\\s");
					String tableauMotsAComparer[]=entry.getValue()[0].split("\\s");
					List<String>wordsonebyone=new ArrayList<String>();
					StringBuilder chaineMotsTarget=new StringBuilder();
					for (String comp:tableauMotsAComparer){
						chaineMotsTarget.append(comp+" ");
					}

					StringBuilder sbForListHTML=new StringBuilder();
//					System.out.println(tableauMotsEnAnalyseForms.length);
//					System.out.println(tableauMotsEnAnalyseLemmas.length);
					for(int l=0;l<tableauMotsEnAnalyseForms.length;l++){
//System.out.println(tableauMotsEnAnalyseForms[l]);
//System.out.println(tableauMotsEnAnalyseLemmas[l]);
						if (tableauMotsEnAnalyseLemmas[l]!=""){
							
							distance=new LevenshteinDistance();
							int count=0;
							int countArrondi=0;
							String wordsChainToCompare[]=chaineMotsTarget.toString().split(" ");

							for (String word:wordsChainToCompare){
								Pattern p=Pattern.compile(tableauMotsEnAnalyseLemmas[l],Pattern.LITERAL|Pattern.CASE_INSENSITIVE);
								Matcher m=p.matcher(word);
								int levDist=distance.computeLevenshteinDistance(word, tableauMotsEnAnalyseLemmas[l]);
								if (m.find()&&!word.contains("NiM")&&!tableauMotsEnAnalyseLemmas[l].contains("NiM")
										&&!stopWords.contains(tableauMotsEnAnalyseLemmas[l].toLowerCase())&&word.length()>1){
									count++;

								}
								else if (levDist<3&&word.length()>4&&!word.contains("NiM")&&!tableauMotsEnAnalyseLemmas[l].contains("NiM")
										&&!stopWords.contains(tableauMotsEnAnalyseLemmas[l].toLowerCase())){
									count++;
								}

							}
							if (count>0){
								int countScore=(((count+1)*5)/nombreDeFichiersAComparer);
								if(countScore>5){
									countArrondi=5;
								}
								else if (countScore<1) {
									countArrondi=countScore+2;
								}
								else{
									countArrondi=countScore+1;
								}
							}
							else if(count==0&&tableauMotsEnAnalyseLemmas[l].length()>3&&!stopWords.contains(tableauMotsEnAnalyseLemmas[l])){
								countArrondi=1;
							}
							else{
								countArrondi=0;
							}
							tableauMotsEnAnalyseForms[l]=tableauMotsEnAnalyseForms[l].replaceAll("_B", "");
							tableauMotsEnAnalyseForms[l]=tableauMotsEnAnalyseForms[l].replaceAll("_N", "");
							wordsonebyone.add("<mark class=\"freq"+countArrondi+"\">"+tableauMotsEnAnalyseForms[l]+"</mark>");
						}
					}

					for (String word:wordsonebyone){
						sbForListHTML.append(word+ " ");
					}

					idsInHTMLFormat.add(sbForListHTML.toString());
				}
				StringBuilder division=new StringBuilder();

				division.append("\n<div class=\"chunk syn"+percentDist+"\" id=\""+fileName+"-"+counterID+"\"><b>"+counterID+" </b>");
				for (String id:idsInHTMLFormat){
					division.append(id+" ");
				}
				division.append("</div>");
				ids.add(division.toString());
				counterID++;
			} 

			String sourceFileName=("./Output/HTML/"+racines.get(i).getAttributeValue("name").toLowerCase());

			String fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.indexOf("chant"))+"_"+sourceFileName.substring(sourceFileName.indexOf("chant")+5,sourceFileName.indexOf("noms"));
			String fileNamePath=fileName.replaceAll("_0", "_");
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("./Output/HTML/"+fileNamePath+".html"), "UTF-8"));
			writer.write("<section id=\""+fileName+"\" class=\"parallel\">");
			for (String chunk:ids){
				writer.write(chunk);
			}
			writer.write("</section>");
			writer.close();
		}
	}
}

