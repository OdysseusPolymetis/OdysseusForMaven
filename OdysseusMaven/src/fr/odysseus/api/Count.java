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
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

import fr.odysseus.utils.Accents;
import fr.odysseus.utils.dictionary.GrPhoneTransformer;

/**
 * @author Marianne Reboul
 */

public class Count {

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
		for (File file:tmpColl){
			repertoireSource.put(file.getName(), file);
		}
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
	public void insertHTML(HashMap<String, Element>roots) throws BadLocationException, IOException, JDOMException
	{
		HashSet<String> stopWords = new HashSet<String>(FileUtils.readLines(new File("./input/dict/stopWords.txt"))); /* listing all stopwords */
		HashSet<String> falsePhons = new HashSet<String>(FileUtils.readLines(new File("./input/dict/falsePhons.txt"))); /* listing all stopwords */
		GrPhoneTransformer trans=new GrPhoneTransformer();
		GeneralizedJaccard<String>jaccard=new GeneralizedJaccard<>();

		for (String name:roots.keySet()){
			int brightred=0;
			int brightgreen=0;
			float averagesyntax=0;
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

			List<Document>autresDocs=new ArrayList<Document>();
			for (String nameOther:roots.keySet()){	  
				if (nameOther!=name&&repertoireSource.get(nameOther).getName().contains(nomCommun)&&!nameOther.contains("odyssee")){
					File autreDoc=repertoireSource.get(nameOther);
					SAXBuilder jdomBuild = new SAXBuilder();
					Document jdomDoc  = jdomBuild.build(autreDoc);
					autresDocs.add(jdomDoc);
				}	 
			}

			List<Element>idsMain=roots.get(name).getChildren();

			List<String>ids=new ArrayList<String>();

			String sourceFileName=roots.get(name).getAttributeValue("name");
			String fileName="";
			if (sourceFileName.contains("odyssee")){
				fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.indexOf("_"))+"_"+sourceFileName.substring(sourceFileName.indexOf("_")+1,sourceFileName.indexOf(".xml"));
			}
			else {
				fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.lastIndexOf("_"))+"_"+sourceFileName.substring(sourceFileName.lastIndexOf("_")+1,sourceFileName.indexOf(".xml"));
			}

			String chantActuel=nomFichierEnCours.substring(nomFichierEnCours.lastIndexOf("_")+1, nomFichierEnCours.lastIndexOf("_")+3);
			HashMap <String, Integer>nbPlag=new HashMap<String, Integer>();
			List<String>plagAuth=new ArrayList<String>();
			SAXBuilder sxb=new SAXBuilder();
			Document documentSource=sxb.build(
					new File(Console.PUNCT+"odyssee1000_"+chantActuel+".xml")); /* File to compare Greek syntax */
			List<Float>dists=new ArrayList<Float>();
			for (Element idMain:idsMain){
				XPathFactory xFactory = XPathFactory.instance();
				XPathExpression<Element> expr = xFactory.compile("/file/"+idMain.getName()+"/word[@tag]", Filters.element());
				List<String>idsInHTMLFormat=new ArrayList<String>();
				List <Element> elemGr=expr.evaluate(documentSource); /* all postags from Greek */
				List<Element>elemFr=expr.evaluate(roots.get(name));
				List<String>tagsGr=new ArrayList<String>();
				List<String>tagsFr=new ArrayList<String>();
				for (Element word:elemGr){
					if (word.getAttributeValue("tag")!="PUN")tagsGr.add(word.getAttributeValue("tag"));
				}
				for (Element word:elemFr){
					if (word.getAttributeValue("tag")!="PUN")tagsFr.add(word.getAttributeValue("tag"));
				}
				HashSet<String>toRem=new HashSet<String>(Arrays.asList("Indef", "DET:ART","DET:POS","PRP:det"));
				if (!nomFichierEnCours.contains("odyssee")){
					tagsFr.removeAll(toRem);
				}
				else{
					Collections.replaceAll(tagsFr, "VER:impf", "VER");
					Collections.replaceAll(tagsFr, "VER:simp", "VER");
					Collections.replaceAll(tagsFr, "VER:futu", "VER");
					Collections.replaceAll(tagsFr, "VER:pres", "VER");
					Collections.replaceAll(tagsFr, "VER:infi", "VER");
				}

				Collections.replaceAll(tagsGr, "VER:impf", "VER");
				Collections.replaceAll(tagsGr, "VER:simp", "VER");
				Collections.replaceAll(tagsGr, "VER:futu", "VER");
				Collections.replaceAll(tagsGr, "VER:pres", "VER");
				Collections.replaceAll(tagsGr, "VER:infi", "VER");



				int globalDist=0;
				float percentDist=0;

				Multiset <String>vecteurSource=HashMultiset.create(tagsGr); /* multisets for Generalized Jaccard */
				Multiset <String>vecteurTarget=HashMultiset.create(tagsFr);

				float similarity=jaccard.compare(vecteurSource,vecteurTarget);

				percentDist=similarity*100;
				
				if (similarity>0.2&&similarity<0.4){
					globalDist=1;
				}
				else if (similarity>0.4&&similarity<0.6){
					globalDist=2;
				}
				else if (similarity>0.6&&similarity<0.7){
					globalDist=3;
				}
				else if (similarity>0.7&&similarity<0.8){
					globalDist=4;
				}
				else if (similarity>0.8){
					globalDist=5;
				}

				/* decrease if source length and target length too different */
				if (Math.abs(tagsFr.size()-tagsGr.size())>15&&globalDist>3){
					globalDist=globalDist-3;
				}

				else if (Math.abs(tagsFr.size()-tagsGr.size())>10&&globalDist>2){
					globalDist=globalDist-2;
				}

				else if (Math.abs(tagsFr.size()-tagsGr.size())>5&&globalDist>1){
					globalDist--;
				}
				if (globalDist>1){
					globalDist++;
				}

				List<String>wordsonebyone=new ArrayList<String>();
				List<Element>words=idMain.getChildren();
				StringBuilder sbForListHTML=new StringBuilder();
				totalNbOfWords+=words.size();
				for (Element mainWord:words){
					int brightRedWord=0;
					int brightGreenWord=0;
					HashMap <Integer, HashSet<String>> plag=new HashMap<Integer, HashSet<String>>();
					HashSet<String>auth=new HashSet<String>();
					
					String lemma=mainWord.getAttributeValue("lemma");
					String form=mainWord.getAttributeValue("text");
					int count=0;
					int countArrondi=0;
					int indID=Integer.decode(idMain.getName().replaceAll("ID", ""));
					boolean phon=false;
					for (Element grLem:elemGr){
						int levDist=StringUtils.getLevenshteinDistance(trans.transform(grLem.getAttributeValue("lemma")), lemma);
//						if (levDist<3&&lemma.length()>4&&Character.isUpperCase(lemma.charAt(0))){
//							phon=true;
//							System.out.println("je rentre dans la condition avec : ");
//							System.out.println(trans.transform(grLem.getAttributeValue("lemma")));
//							System.out.println(lemma);
//						}
						if(levDist<4&&lemma.length()>6&&Character.isLowerCase(lemma.charAt(0))
								&&!falsePhons.contains(lemma)
								&&trans.transform(grLem.getAttributeValue("lemma")).length()>4
								&&!falsePhons.contains(trans.transform(grLem.getAttributeValue("lemma")))){
							phon=true;
						}
					}
					for (Document secDoc:autresDocs){
						Element secID=secDoc.getRootElement().getChild(idMain.getName());
						String nameAuth=secDoc.getRootElement().getAttributeValue("name").substring(secDoc.getRootElement().getAttributeValue("name").lastIndexOf("/")+1, secDoc.getRootElement().getAttributeValue("name").lastIndexOf("_")-4);
						Element secIDmin1=null;
						Element secIDmin2=null;
						Element secIDplus1=null;
						Element secIDplus2=null;

						if (indID>0){
							secIDmin1=secDoc.getRootElement().getChild("ID"+(indID-1));
						}
						if (indID>1){
							secIDmin2=secDoc.getRootElement().getChild("ID"+(indID-2));
						}
						if (indID+1<secDoc.getRootElement().getChildren().size()){
							secIDplus1=secDoc.getRootElement().getChild("ID"+(indID+1));
						}
						if (indID+2<secDoc.getRootElement().getChildren().size()){
							secIDplus2=secDoc.getRootElement().getChild("ID"+(indID+2));
						}
						if (!stopWords.contains(lemma)&&!stopWords.contains(lemma.toLowerCase())){
							for (Element secWord:secID.getChildren()){
								int levDist=StringUtils.getLevenshteinDistance(secWord.getAttributeValue("lemma"), lemma);
								if (lemma.equals(secWord.getAttributeValue("lemma"))){
									count++;
									auth.add(nameAuth);
								}
								else if (levDist<2&&lemma.length()>4){
									count++;
									auth.add(nameAuth);
								}
							}
							if (secIDmin1!=null){
								for (Element secWord:secIDmin1.getChildren()){
									int levDist=StringUtils.getLevenshteinDistance(secWord.getAttributeValue("lemma"), lemma);
									if (lemma.equals(secWord.getAttributeValue("lemma"))){
										count++;
										auth.add(nameAuth);
									}
									else if (levDist<2&&lemma.length()>4){
										count++;
										auth.add(nameAuth);
									}
								}
							}
							if (secIDmin2!=null){
								for (Element secWord:secIDmin2.getChildren()){
									int levDist=StringUtils.getLevenshteinDistance(secWord.getAttributeValue("lemma"), lemma);
									if (lemma.equals(secWord.getAttributeValue("lemma"))){
										count++;
										auth.add(nameAuth);
									}
									else if (levDist<2&&lemma.length()>4){
										count++;
										auth.add(nameAuth);
									}
								}
							}
							if (secIDplus1!=null){
								for (Element secWord:secIDplus1.getChildren()){
									int levDist=StringUtils.getLevenshteinDistance(secWord.getAttributeValue("lemma"), lemma);
									if (lemma.equals(secWord.getAttributeValue("lemma"))){
										count++;
										auth.add(nameAuth);
									}
									else if (levDist<2&&lemma.length()>4){
										count++;
										auth.add(nameAuth);
									}
								}
							}
							if (secIDplus2!=null){
								for (Element secWord:secIDplus2.getChildren()){
									int levDist=StringUtils.getLevenshteinDistance(secWord.getAttributeValue("lemma"), lemma);
									if (lemma.equals(secWord.getAttributeValue("lemma"))){
										count++;
										auth.add(nameAuth);
									}
									else if (levDist<2&&lemma.length()>4){
										count++;
										auth.add(nameAuth);
									}
								}
							}
						}
						if (nbPlag.containsKey(nameAuth)){
							int tmp=nbPlag.get(nameAuth)+count;
							nbPlag.put(nameAuth, tmp);
						}
						else{
							nbPlag.put(nameAuth, count);
						}
					}

					if (count>0){
						int countScore=((count*5)/nombreDeFichiersAComparer);
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
					else if(count==0&&lemma.length()>3&&!stopWords.contains(Accents.removeDiacriticalMarks(lemma.toLowerCase()))){
						countArrondi=1;
						brightRedWord++;
					}
					else{
						countArrondi=0;
					}
					if (!auth.isEmpty()&& count<4){
						plag.put(countArrondi, auth);
					}

					form=form.replaceAll("blk", "");
					form=form.replaceAll("_N", "");
					form=form.replaceAll("\\/", "<br>");
					StringBuilder buildHtml=new StringBuilder();
					String authPlag="";
					if (plag.containsKey(countArrondi)){
						authPlag=plag.get(countArrondi).toString().replaceAll(","," ");
						if (countArrondi==2){
							plagAuth.addAll(plag.get(countArrondi));
						}
					}

					if (phon==true&&!fileName.contains("odyssee")){
						form="<i>"+form+"</i>";
					}
					
					if (fileName.contains("odyssee")||stopWords.contains(form.toLowerCase())){
						buildHtml.append("<mark class=\"freq0\">"+form+"</mark>");
						wordsonebyone.add(buildHtml.toString());
					}
					else{
						if (countArrondi==6|countArrondi==5){
							buildHtml.append("<mark class=\"freq"+countArrondi+" high\">"+form+"</mark>");
							wordsonebyone.add(buildHtml.toString());
						}
						else if (countArrondi==1){
							buildHtml.append("<mark class=\"freq"+countArrondi+" low\">"+form+"</mark>");
							wordsonebyone.add(buildHtml.toString());
						}
						else if (countArrondi==2){
							if (plag.containsKey(countArrondi)){
								if (plag.get(countArrondi).size()<4&&plag.get(countArrondi).size()>0){
									buildHtml.append("<mark class=\"freq2 plag\"><a title=\""+authPlag+"\">"+form+"</a></mark>");
								}
							}
							else{
								buildHtml.append("<mark class=\"freq0 plag\">"+form+"</mark>");
							}
							wordsonebyone.add(buildHtml.toString());
						}
						else {
							buildHtml.append("<mark class=\"freq"+countArrondi+"\">"+form+"</mark>");
							wordsonebyone.add(buildHtml.toString());
						}
					}
					brightred+=brightRedWord;
					brightgreen+=brightGreenWord;
				}
				for (String word:wordsonebyone){
					sbForListHTML.append(word+ " ");
				}
				idsInHTMLFormat.add(sbForListHTML.toString());
				StringBuilder division=new StringBuilder();
				String counterID=idMain.getName().replaceAll("ID", "");
				division.append("\n<div class=\"chunk syn"+globalDist+"\" id=\""+fileName+"-"+counterID+"\"><b>"+counterID+" </b>");

				for (String id:idsInHTMLFormat){
					division.append(id+" ");
				}
				division.append("</div>");
				ids.add(division.toString());
				dists.add(percentDist);
			}

			for (String author:plagAuth){
				nbPlag.put(author, (Collections.frequency(plagAuth, author)*100)/plagAuth.size());
			}
			LinkedHashMap <String, Integer>top5=new LinkedHashMap<String, Integer>();
			top5=sortMyMapByValue(nbPlag);
			
			brightgreen=(brightgreen*100)/totalNbOfWords;
			brightred=(brightred*100)/totalNbOfWords;
			float totalDists=0;
			for (float dist:dists){
				totalDists+=dist;
			}
			averagesyntax=Math.round(totalDists/dists.size());
			fileName=sourceFileName.substring(sourceFileName.lastIndexOf("/")+1, sourceFileName.indexOf("_"))+"_"+sourceFileName.substring(sourceFileName.indexOf("_")+1,sourceFileName.indexOf(".xml"));

			String fileNamePath=fileName.replaceAll("_0", "_");
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Console.OUTPUTHTML+fileNamePath+".html"), "UTF-8"));
			writer.write("<section id=\""+fileName+"\" class=\"parallel\" title=\"Haute Fréquence : "+brightgreen+"% ;\nBasse Fréquence : "
					+brightred+"% ;\nProximité Syntaxique : "+averagesyntax+"% ;\nReprises basse fréquence : ");
			for (String auth:top5.keySet()){
				writer.write(auth+":"+top5.get(auth)+"% ");
			}
			writer.write("\">");
			
			for (String chunk:ids){
				writer.write(chunk);
			}
			writer.write("</section>");
			writer.close();
		}
	}
	public static LinkedHashMap<String, Integer>sortMyMapByValue(HashMap<String, Integer>map){
		LinkedHashMap<String, Integer> sortedMap = 
				map.entrySet().stream().
				sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) 
				.limit(5).
				collect(Collectors.toMap(Entry::getKey, Entry::getValue,
						(e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
}
}

