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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

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

import fr.odysseus.utils.LevenshteinDistance;
import fr.odysseus.utils.ListageRepertoire;

/**
 * @author Marianne Reboul
 */

public class StatisticalComparison {

	File repertoireSource[]; /* directory for source xml aligned files */

	/**
	 * @return the root element of each file
	 */
	/**
	 * @param the file
	 */
	public Element setRoot(File file) throws IOException, JDOMException {
		/* directory for source xml aligned files */

		SAXBuilder sxb=new SAXBuilder();
		Document doc=sxb.build(file);
		Element racine=doc.getRootElement();
		return racine;
	}

	/**
	 * called from the main console, getting all the roots from all the xml and comparing their elements
	 */
	public void automaticComparison() throws IOException, BadLocationException, JDOMException{

		repertoireSource=ListageRepertoire.listeRepertoire(new File("./outputFiles/xml")); /* creating source directory with all roots */
		LinkedList<Element>listeDesRacines=new LinkedList<Element>();

		for( int i = 0; i < repertoireSource.length; i++ ) {
			Element racine=setRoot(repertoireSource[i]);
			listeDesRacines.add(racine);
		}
		insertHTML(listeDesRacines); /* generating html */
	}

	/**
	 * inserts the data in html output file
	 * @param the list of roots from all the files in the xml folder
	 */
	public void insertHTML(LinkedList<Element>racines) throws BadLocationException, IOException, JDOMException
	{
		Path path=Paths.get("./sourceFiles/sourceDictionaries/stopWords.txt");
		List<String>stopWords=Files.readAllLines(path); /* listing all stopwords */

		GeneralizedJaccard<String>jaccard=new GeneralizedJaccard<>();
		LevenshteinDistance distance=new LevenshteinDistance();

		for (int i=0; i<racines.size();i++){
			int brightred=0;
			int brightgreen=0;
			int averagesyntax=0;
			int totalNbOfWords=0;
			String nomFichierEnCours=racines.get(i).getAttributeValue("name");
			String nomCommun=nomFichierEnCours.substring(nomFichierEnCours.indexOf("Chant"));

			File dir=new File("./outputFiles/xml/");
			/* making sure we only take the .xml extensions into account */
			File[] filesEnCours = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(nomCommun+".xml");
				}
			});
			int nombreDeFichiersAComparer=filesEnCours.length;

			System.out.println(nomFichierEnCours);

			/* XPath process to get all the specific lemmas and forms we need to statistically compare them */
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

			List<Element>listeElementsActuels=racines.get(i).getChildren(); /* words and lemmas of the file currently studied */

			XPathFactory xFactory = XPathFactory.instance();

			new HashMap<Integer, List<String>>();
			List<Element>listIDs=racines.get(i).getChildren(); /* all words and lemmas from all the other files except the one studied */
			int counterID=1;

			List<String>ids=new ArrayList<String>();

			for (Element ID:listIDs){
				List<String>idsInHTMLFormat=new ArrayList<String>();
				LinkedHashMap<String[], String[]>tableauDeCorrespondances=new LinkedHashMap<String[], String[]>();

				List<Element>total=new ArrayList<Element>();
				StringBuilder stringbuild=new StringBuilder();

				String chantActuel=nomFichierEnCours.substring(nomFichierEnCours.indexOf("Chant"));
				SAXBuilder sxb=new SAXBuilder();
				Document documentSource=sxb.build(
						new File("./sourceFiles/sequences/greekPunct/Odyssee1000"+chantActuel+".xml")); /* File to compare Greek syntax */
				Element racineSource=documentSource.getRootElement();
				Element IDSource=racineSource.getChild("ID"+counterID);
				String []tagSource=IDSource.getAttributeValue("tag").split(" "); /* all postags from Greek */
				String tagActuel[]=listeElementsActuels.get(counterID-1).getAttributeValue("tag").split(" "); /* all postags from French */
				List<String>listeTagSource=new ArrayList<String>(Arrays.asList(tagSource));
				List<String>listeTagTarget=new ArrayList<String>(Arrays.asList(tagActuel));
				listeTagSource.remove("PUN");
				listeTagTarget.remove("PUN");
				listeTagSource.remove("Indef");
				listeTagTarget.remove("Indef");
				listeTagTarget.remove("DET:ART");
				listeTagTarget.remove("DET:POS");
				listeTagTarget.remove("PRP:det");

				/* This part is not very efficient : 
				 * TODO either modify data or find a quicker and more efficient way */
				Collections.replaceAll(listeTagSource, "VER:impf", "VER");
				Collections.replaceAll(listeTagSource, "VER:simp", "VER");
				Collections.replaceAll(listeTagSource, "VER:futu", "VER");
				Collections.replaceAll(listeTagSource, "VER:pres", "VER");
				Collections.replaceAll(listeTagSource, "VER:infi", "VER");
				Collections.replaceAll(listeTagTarget, "VER:impf", "VER");
				Collections.replaceAll(listeTagTarget, "VER:simp", "VER");
				Collections.replaceAll(listeTagTarget, "VER:futu", "VER");
				Collections.replaceAll(listeTagTarget, "VER:pres", "VER");
				Collections.replaceAll(listeTagTarget, "VER:infi", "VER");

				int percentDist=0;

				Multiset <String>vecteurSource=HashMultiset.create(listeTagSource); /* multisets for Generalized Jaccard */
				Multiset <String>vecteurTarget=HashMultiset.create(listeTagTarget);

				float similarity=jaccard.compare(vecteurSource,vecteurTarget);

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

				/* decrease if source length and target length too different */
				if (Math.abs(listeTagSource.size()-listeTagTarget.size())>15&&percentDist>3){
					percentDist=percentDist-3;
				}

				else if (Math.abs(listeTagSource.size()-listeTagTarget.size())>10&&percentDist>2){
					percentDist=percentDist-2;
				}

				else if (Math.abs(listeTagSource.size()-listeTagTarget.size())>5&&percentDist>1){
					percentDist--;
				}
				if (percentDist>1){
					averagesyntax++;
				}

				/* evaluate expression for all lemmas from previous or next alignment for counting */
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

				/* get all lemmas from previous or next alignment into account for counting */
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
				String sourceFileName=("./outputFiles/html/"+racines.get(i).getAttributeValue("name").toLowerCase());
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
					
					for(int l=0;l<tableauMotsEnAnalyseForms.length;l++){
						int brightRedWord=0;
						int brightGreenWord=0;

						if (tableauMotsEnAnalyseLemmas[l]!=""){
							distance=new LevenshteinDistance();
							int count=0;
							int countArrondi=0;
							String wordsChainToCompare[]=chaineMotsTarget.toString().split(" ");

							/* comparing word of current file to other words in same alignment ID in other files
							 * they may match (find()) or have a sufficiently low levenshtein distance to be considered as the same word */
							for (String word:wordsChainToCompare){
								Pattern p=Pattern.compile(tableauMotsEnAnalyseLemmas[l],Pattern.LITERAL|Pattern.CASE_INSENSITIVE);
								Matcher m=p.matcher(word);
								int levDist=distance.computeLevenshteinDistance(word, tableauMotsEnAnalyseLemmas[l]);
								//								float levDist=distance.compare(word, tableauMotsEnAnalyseLemmas[l]);

								if (m.find()&&!stopWords.contains(tableauMotsEnAnalyseLemmas[l].toLowerCase())&&word.length()>1){
									count++;
								}
								else if (levDist<3&&word.length()>4&&!stopWords.contains(tableauMotsEnAnalyseLemmas[l].toLowerCase())){
									count++;
								}
							}

							/* counting occurences and making the counting relative (5 categories) */
							if (count>0){
								int countScore=(((count+1)*5)/nombreDeFichiersAComparer);

								if(countScore>5){
									countArrondi=5;
									brightGreenWord++;
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
								brightRedWord++;
							}
							else{
								countArrondi=0;
							}
							tableauMotsEnAnalyseForms[l]=tableauMotsEnAnalyseForms[l].replaceAll("_B", "");
							tableauMotsEnAnalyseForms[l]=tableauMotsEnAnalyseForms[l].replaceAll("_N", "");
							if (countArrondi==6|countArrondi==5){
								wordsonebyone.add("<mark class=\"freq"+countArrondi+" high\">"+tableauMotsEnAnalyseForms[l]+"</mark>");
							}
							else if (countArrondi==1){
								wordsonebyone.add("<mark class=\"freq"+countArrondi+" low\">"+tableauMotsEnAnalyseForms[l]+"</mark>");
							}
							else {
								wordsonebyone.add("<mark class=\"freq"+countArrondi+"\">"+tableauMotsEnAnalyseForms[l]+"</mark>");
							}
							
						}
						brightred+=brightRedWord;
						brightgreen+=brightGreenWord;
					}
					
					totalNbOfWords+=tableauMotsEnAnalyseForms.length;
					
					for (String word:wordsonebyone){
						sbForListHTML.append(word+ " ");
					}
					
					idsInHTMLFormat.add(sbForListHTML.toString());
				}
				
				StringBuilder division=new StringBuilder();

				/* converting to html */
				division.append("\n<div class=\"chunk syn"+percentDist+"\" id=\""+fileName+"-"+counterID+"\"><b>"+counterID+" </b>");

				for (String id:idsInHTMLFormat){
					division.append(id+" ");
				}
				division.append("</div>");
				ids.add(division.toString());
				counterID++;
			} 
			
			brightgreen=(brightgreen*100)/totalNbOfWords;
			brightred=(brightred*100)/totalNbOfWords;
			averagesyntax=(averagesyntax*100)/listeElementsActuels.size();

			String sourceFileName=("./outputFiles/html/"+racines.get(i).getAttributeValue("name").toLowerCase());

			String fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.indexOf("chant"))+"_"+sourceFileName.substring(sourceFileName.indexOf("chant")+5,sourceFileName.indexOf("noms"));
			String fileNamePath=fileName.replaceAll("_0", "_");
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("./outputFiles/html/"+fileNamePath+".html"), "UTF-8"));
			writer.write("<section id=\""+fileName+"\" class=\"parallel\" title=\"Statistiques générales : \nHaute Fréquence : "+brightgreen+" ;\nBasse Fréquence : "
					+brightred+" ;\nProximité Syntaxique : "+averagesyntax+"\">");
//			writer.write("\n<details  green=\""+brightgreen+"\" red=\""+brightred+"\" syntax=\""+averagesyntax+"\">");

			for (String chunk:ids){
				writer.write(chunk);
			}
			writer.write("</section>");
			writer.close();
		}
	}
}

