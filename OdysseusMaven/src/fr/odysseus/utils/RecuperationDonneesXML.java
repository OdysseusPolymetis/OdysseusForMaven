package fr.odysseus.utils;

import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Iterators;
//import com.google.common.collect.Multimap;

//public class RecuperationDonneesXML {


public class RecuperationDonneesXML {
	SAXBuilder sxb=new SAXBuilder();

  public LinkedList<String> recupererDonneesXML(Element racine){
    LinkedList<String> listTrg=new LinkedList<String>();
    String chunk = "";
    List<Element> listBooks1 = racine.getChildren();
    for (Element book:listBooks1)
    {
      Element IDcourant = book;
      List<Element> listID=IDcourant.getChildren();
      for (Element id:listID){
        chunk=id.getText();
        listTrg.add(chunk);
      }
    }
    return listTrg;
  }
  
  public LinkedList<String> recupererLemmesXML(Element racine){
    LinkedList<String> listTrg=new LinkedList<String>();
    String chunk = "";
    List<Element> listBooks1 = racine.getChildren();
    for (Element book:listBooks1)
    {
      Element IDcourant = book;
      List<Element> listID=IDcourant.getChildren();
      for (Element id:listID){
        chunk=id.getAttributeValue("lemma");
        listTrg.add(chunk);
      }
    }
    return listTrg;
  }


  public static int wordCount(String s){
    if (s == null)
      return 0;
    return s.trim().split("\\s+").length;
  }

//  public Map<Integer, List<String>> recupererAutresIDs(int counter, LinkedList<Element>listeDesRacines){
//    LinkedList<String>listTest=null;
//    Map<Integer, List<String>>mapDesRacines=new HashMap<Integer, List<String>>();
//    for (int i=0; i<listeDesRacines.size();i++){
////      if (listeDesRacines.get(i).getName()=="racineGrecque"){
////        listeDesRacines.remove(i);
////      }
//      List<Element> listBooks = Iterators.get(listeDesRacines.iterator(), i).getChildren();
//      LinkedList <Element>listeGlobale=new LinkedList<Element>();
//      for (Element book:listBooks){
//        listeGlobale.addAll(book.getChildren());
//      }
//        listTest=new LinkedList<String>();
//        if (counter<listeGlobale.size()){ 
//          listTest.add(listeGlobale.get(counter).getText());
//        }
////        else{
////          System.out.println("hors compteur");
////        }
//      mapDesRacines.put(i, listTest);
//    }
//    return mapDesRacines;
//  }
  
//  public Map<Integer, List<String>> recupererAutresLemmas(int counter, LinkedList<Element>listeDesRacines){
//    LinkedList<String>listTest=null;
//    Map<Integer, List<String>>mapDesRacines=new HashMap<Integer, List<String>>();
//    for (int i=0; i<listeDesRacines.size();i++){
////      if (listeDesRacines.get(i).getName()=="racineGrecque"){
////        listeDesRacines.remove(i);
////      }
//      List<Element> listBooks = Iterators.get(listeDesRacines.iterator(), i).getChildren();
//      LinkedList <Element>listeGlobale=new LinkedList<Element>();
//      for (Element book:listBooks){
//        listeGlobale.addAll(book.getChildren());
//      }
//        listTest=new LinkedList<String>();
//        if (counter<listeGlobale.size()){ 
//          listTest.add(listeGlobale.get(counter).getAttributeValue("lemma"));
//        }
////        else{
////          System.out.println("hors compteur");
////        }
//      mapDesRacines.put(i, listTest);
//    }
//    return mapDesRacines;
//  }

  
//  public void getOtherDics(HashMap<String, HashMap<String, HashMap<String, Double>>> otherDics,String pathXMLSource, String name) throws JDOMException, IOException{
//	  
	  //	  HashMap<String, HashMap<String, TreeMap<String, Double>>>otherDics=new HashMap<String, HashMap<String, TreeMap<String, Double>>>();
//	  HashMap<String, HashMap<String, Double>>mapDesCles=new HashMap<String, HashMap<String, Double>>();
//	  File file = new File(pathXMLSource);
//	  Document document = sxb.build(file);
//	  file=null;
//	  Element racine = document.getRootElement();
//	  List <Element>listeDesCles = racine.getChildren();
//
//	   //On crée un Iterator sur notre liste
//	   for (Element cle:listeDesCles){
//		   String key=cle.getAttributeValue("key");
//	      List <Element>listeDesValeurs = cle.getChildren();
//	      //HashMultimap<String, Double>mapDesValeurs=HashMultimap.create();
//	      HashMap<String, Double>mapDesValeurs= new HashMap<>();
//	      for(Element valeur:listeDesValeurs){
//	    	  String value=valeur.getAttributeValue("value");
//	    	  String score=valeur.getAttributeValue("score");
//	    	  mapDesValeurs.put(value, Double.parseDouble(score));
//	      }
//	      mapDesCles.put(key, mapDesValeurs);
//	      mapDesValeurs=null;
////	      System.out.print("-");
//	   }
//	   otherDics.put(name, mapDesCles);
//	   mapDesCles=null;
//	  
////	return otherDics;
//	  
//  }
  
//  public void createMergedDics(String pathFileSource, String pathXMLMerged) throws JDOMException, IOException{
//	  ReadSourceFiles reader=new NWAligner();
//	  Element racine = new Element("DistribDict");
//	  
//	  LinkedHashSet<File>filesDicts=reader.run(new File(pathFileSource));
//	  for (File file : filesDicts){
//		  Document docTemp=sxb.build(file);
//		  Element racineTemp=docTemp.getRootElement().detach();
//		  docTemp=null;
//		  racine.addContent(racineTemp);
//		  racineTemp=null;
//	  }
//	  Document document =new Document(racine);
//	  XMLBuilder builder=new XMLBuilder();
//	  builder.enregistre(pathXMLMerged, document);
	  


	  
//  }
  
  
//  public Multimap<Element, List<Element>> getMergedDics(String path) throws JDOMException, IOException{
//
//	  File file =new File(path);
//	  Document docTemp=sxb.build(file);
//	  Multimap<Element, List<Element>>map=ArrayListMultimap.create();
//	  Element racine=docTemp.getRootElement();
//	  
//	  
//	  List <Element>listeDesCles = racine.getChildren();
//	   for (Element cle:listeDesCles){
////		   String key=cle.getAttributeValue("key");
//	      List <Element>listeDesValeurs = cle.getChildren();
//	      map.put(cle, listeDesValeurs);
//
//	   }
//
//	return map;
//  }
}
