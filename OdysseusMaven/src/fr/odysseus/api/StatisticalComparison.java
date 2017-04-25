package fr.odysseus.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.text.BadLocationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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

/**
 * @author Marianne Reboul
 */

public class StatisticalComparison {

	HashMap<String, File> repertoireSource; /* directory for source xml aligned files */

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

		File mainDir=new File(Console.OUTPUTXML);
		String ext[]={"xml"};
		repertoireSource=new HashMap<String,File>();
		Collection<File>tmpColl=FileUtils.listFiles(mainDir, ext, true); /* creating source directory with all roots */
//		repertoireSource=tmpColl.toArray(new File[tmpColl.size()]);
		for (File file:tmpColl){
			repertoireSource.put(file.getName(), file);
		}

//		LinkedList<Element>listeDesRacines=new LinkedList<Element>();
		HashMap<String, Element>mapRoots=new HashMap<String, Element>();

		for( String name:repertoireSource.keySet() ) {
			Element racine=setRoot(repertoireSource.get(name));
			mapRoots.put(name, racine);
		}
		insertHTML(mapRoots); /* generating html */
	}

	/**
	 * inserts the data in html output file
	 * @param the list of roots from all the files in the xml folder
	 */
	@SuppressWarnings("unused")
	public void insertHTML(HashMap<String, Element>roots) throws BadLocationException, IOException, JDOMException
	{
		HashSet<String> stopWords = new HashSet<String>(FileUtils.readLines(new File("./input/dict/stopWords.txt"))); /* listing all stopwords */

		GeneralizedJaccard<String>jaccard=new GeneralizedJaccard<>();
		//		LevenshteinDistance distance=new LevenshteinDistance();

		for (String name:roots.keySet()){

			int brightred=0;
			int brightgreen=0;
			int averagesyntax=0;
			int totalNbOfWords=0;
			String nomFichierEnCours=roots.get(name).getAttributeValue("name");
			String nomCommun=nomFichierEnCours.substring(nomFichierEnCours.lastIndexOf("_"));

			HashSet<File>tmpOthFi=new HashSet<File>();
			Files.walk(Paths.get(Console.OUTPUTXML))
			.filter(Files::isRegularFile)
			.forEach((f)->{
				String file = f.toString();
				if( file.endsWith(nomCommun))
					tmpOthFi.add(new File(file));               
			});

			File[] filesEnCours=tmpOthFi.toArray(new File[tmpOthFi.size()]);
			int nombreDeFichiersAComparer=filesEnCours.length;

			System.out.println(nomFichierEnCours);

			/* XPath process to get all the specific lemmas and forms we need to statistically compare them */
			XPathExpression<Element> expr;
			XPathExpression<Element> exprPrevious = null;
			XPathExpression<Element> exprPreviousBefore = null;
			XPathExpression<Element> exprNext= null;
			XPathExpression<Element> exprNextBefore= null;
			List<Document>autresDocs=new ArrayList<Document>();
//			System.out.println("nom principal : "+name);
			for (String nameOther:roots.keySet()){	  
				if (nameOther!=name&&repertoireSource.get(nameOther).getName().contains(nomCommun)){
					File autreDoc=repertoireSource.get(nameOther);
					SAXBuilder jdomBuild = new SAXBuilder();
					Document jdomDoc  = jdomBuild.build(autreDoc);
					autresDocs.add(jdomDoc);
//					System.out.println("devrait être différent de nom principal : "+nameOther);
				}	 
				
			}

			List<Element>listeElementsActuels=roots.get(name).getChildren(); /* words and lemmas of the file currently studied */

			XPathFactory xFactory = XPathFactory.instance();

			new HashMap<Integer, List<String>>();
			List<Element>listIDs=roots.get(name).getChildren(); /* all words and lemmas from all the other files except the one studied */
			int counterID=1;

			List<String>ids=new ArrayList<String>();

			for (Element ID:listIDs){
				//				long startTimeRep = System.currentTimeMillis();

				List<String>idsInHTMLFormat=new ArrayList<String>();
				LinkedHashMap<String[], String[]>tableauDeCorrespondances=new LinkedHashMap<String[], String[]>();

				List<Element>total=new ArrayList<Element>();
				StringBuilder stringbuild=new StringBuilder();

				String chantActuel=nomFichierEnCours.substring(nomFichierEnCours.lastIndexOf("_")+1, nomFichierEnCours.lastIndexOf("_")+3);
				SAXBuilder sxb=new SAXBuilder();
				Document documentSource=sxb.build(
						new File(Console.PUNCT+"odyssee1000_"+chantActuel+".xml")); /* File to compare Greek syntax */
				Element racineSource=documentSource.getRootElement();
				Element IDSource=racineSource.getChild("ID"+counterID);
				String []tagSource=IDSource.getAttributeValue("tag").split(" "); /* all postags from Greek */
				String tagActuel[]=listeElementsActuels.get(counterID-1).getAttributeValue("tag").split(" "); /* all postags from French */
				List<String>listeTagSource=new ArrayList<String>(Arrays.asList(tagSource));
				List<String>listeTagTarget=new ArrayList<String>(Arrays.asList(tagActuel));
				HashSet<String>toRem=new HashSet<String>(Arrays.asList("PUN", "Indef", "DET:ART","DET:POS","PRP:det"));
				listeTagSource.remove("PUN");
				listeTagTarget.removeAll(toRem);

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
				if (counterID>1){
					exprPreviousBefore=xFactory.compile("/file/ID"+(counterID-2)+"[@lemma]", Filters.element());
				}
				if (counterID>0){
					exprPrevious=xFactory.compile("/file/ID"+(counterID-1)+"[@lemma]", Filters.element());
				}
				if (counterID<listIDs.size()-1){
					exprNextBefore=xFactory.compile("/file/ID"+(counterID+2)+"[@lemma]", Filters.element());
				}
				if (counterID<listIDs.size()){
					exprNext=xFactory.compile("/file/ID"+(counterID+1)+"[@lemma]", Filters.element());
				}

				for (Document docToTest:autresDocs){
					List<Element> elementsToCompare=expr.evaluate(docToTest);
					List<Element> elementsToComparePrevious=new ArrayList<Element>();
					List<Element> elementsToCompareNext=new ArrayList<Element>();
					if (exprPrevious!=null){
						elementsToComparePrevious.addAll(exprPrevious.evaluate(docToTest));
					}

//					if (exprPreviousBefore!=null){
//						elementsToComparePrevious.addAll(exprPreviousBefore.evaluate(docToTest));
//					}

					if (exprNext!=null){
						elementsToCompareNext.addAll(exprNext.evaluate(docToTest));
					}

//					if (exprNextBefore!=null){
//						elementsToCompareNext.addAll(exprNextBefore.evaluate(docToTest));
//					}

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
				String sourceFileName=(Console.OUTPUTHTML+roots.get(name).getAttributeValue("name").toLowerCase());
				String fileName="";
				if (sourceFileName.contains("odyssee")){
					fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.indexOf("_"))+"_"+sourceFileName.substring(sourceFileName.indexOf("_")+1,sourceFileName.indexOf(".xml"));
				}
				else {
					fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.lastIndexOf("_"))+"_"+sourceFileName.substring(sourceFileName.lastIndexOf("_")+1,sourceFileName.indexOf(".xml"));
				}

				//				System.out.println("les lemmes : "+lemmaAndForm[0]);
				//				System.out.println("les formes : "+lemmaAndForm[1]);

				for (Entry<String[], String[]>entry:tableauDeCorrespondances.entrySet()){

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
						String form=tableauMotsEnAnalyseForms[l];
						String lemma=tableauMotsEnAnalyseLemmas[l];
						int brightRedWord=0;
						int brightGreenWord=0;

						if (lemma!=""){

							int count=0;
							int countArrondi=0;
							String wordsChainToCompare[]=chaineMotsTarget.toString().split(" ");

							/* comparing word of current file to other words in same alignment ID in other files
							 * they may match (find()) or have a sufficiently low levenshtein distance to be considered as the same word */

							for (String word:wordsChainToCompare){

								int levDist=StringUtils.getLevenshteinDistance(word, lemma);

								if (word.equals(lemma)|lemma.equals(word)|lemma.toLowerCase().equals(word)|word.toLowerCase().equals(lemma)){
									count++;
								}

								else if (levDist<2&&word.length()>4&&!stopWords.contains(lemma.toLowerCase())){
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
							else if(count==0&&lemma.length()>3&&!stopWords.contains(lemma)){
								countArrondi=1;
								brightRedWord++;
							}
							else{
								countArrondi=0;
							}
							form=form.replaceAll("blk", "");
							form=form.replaceAll("_N", "");
							StringBuilder buildHtml=new StringBuilder();
							if (countArrondi==6|countArrondi==5){
								buildHtml.append("<mark class=\"freq");
								buildHtml.append(countArrondi);
								buildHtml.append(" high\">");
								buildHtml.append(form);
								buildHtml.append("</mark>");
								wordsonebyone.add(buildHtml.toString());
							}
							else if (countArrondi==1){
								buildHtml.append("<mark class=\"freq");
								buildHtml.append(countArrondi);
								buildHtml.append(" low\">");
								buildHtml.append(form);
								buildHtml.append("</mark>");
								wordsonebyone.add(buildHtml.toString());
							}
							else if (countArrondi==2){
								buildHtml.append("<mark class=\"freq");
								buildHtml.append(countArrondi);
								buildHtml.append(" plag\">");
								buildHtml.append(form);
								buildHtml.append("</mark>");
								wordsonebyone.add(buildHtml.toString());
							}
							else {
								buildHtml.append("<mark class=\"freq");
								buildHtml.append(countArrondi);
								buildHtml.append("\">");
								buildHtml.append(form);
								buildHtml.append("</mark>");
								wordsonebyone.add(buildHtml.toString());
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

			String sourceFileName=(Console.OUTPUTHTML+roots.get(name).getAttributeValue("name").toLowerCase());
			String fileName="";
			if (sourceFileName.contains("odyssee")){
				fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.indexOf("_"))+"_"+sourceFileName.substring(sourceFileName.indexOf("_")+1,sourceFileName.indexOf(".xml"));
			}
			else {
				fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.lastIndexOf("_"))+"_"+sourceFileName.substring(sourceFileName.lastIndexOf("_")+1,sourceFileName.indexOf(".xml"));
			}

			String fileNamePath=fileName.replaceAll("_0", "_");
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Console.OUTPUTHTML+fileNamePath+".html"), "UTF-8"));
			writer.write("<section id=\""+fileName+"\" class=\"parallel\" title=\"Statistiques générales : \nHaute Fréquence : "+brightgreen+" ;\nBasse Fréquence : "
					+brightred+" ;\nProximité Syntaxique : "+averagesyntax+"\">");

			for (String chunk:ids){
				writer.write(chunk);
			}
			writer.write("</section>");
			writer.close();
		}
	}
}

