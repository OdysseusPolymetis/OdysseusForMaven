package fr.odysseus.utils;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import fr.odysseus.dataModels.NWRecord;

public class CreateFiles {

	public static void createFiles(LinkedHashSet<List<NWRecord>>liste, String name) throws IOException{
		System.setProperty("treetagger.home", "/home/bilbo/Bureau/outilsJava/TreeTaggerInstall/");
		System.out.println("longueur de la liste de listes : "+liste.size());

		/* c'est dans cette fonction qu'on crée les xml de sortie, avec les ID fixes ayant comme attributs les POS*/

		Element racine = new Element("racineGrecque");

		Element racine2 = new Element("author");

		int numeroDuChant=1;
		//    int numeroDuChant=1;

		for (List<NWRecord>nouvelleListe:liste){


			Element book = new Element("book"+numeroDuChant);

			Element book2= new Element("book"+numeroDuChant);

			int counter=0;

			for (NWRecord record:nouvelleListe){
				Element ID = new Element("ID"+String.valueOf(counter+1));
				Element ID2 = new Element("ID"+String.valueOf(counter+1));
				ID.setText(record.getSrc());
				ID2.setText(record.getTrg());

				ID2.setAttribute(new Attribute ("lemma", record.getLemma()));
				ID2.setAttribute(new Attribute("tag",record.getTag()));
				book.addContent(ID);
				book2.addContent(ID2);
				counter++;
			}
			racine.addContent(book);

			racine2.addContent(book2);
			numeroDuChant++;
		}

		Document document = new Document(racine);
		Document document2 = new Document(racine2);
		XMLBuilder builder=new XMLBuilder();
		builder.enregistre("./Output/OutputFrancais/"+name+"Target.xml", document2);
		builder.enregistre("./Output/outPutGrec/GreekSource1000Target.xml", document);
	}

	public void mergeFiles(TreeMap<String, List<NWRecord>> map) throws IOException{
		String nomFichierPrinc="";
		Map<String,LinkedHashSet<List<NWRecord>>>listeDeListes=new HashMap<String,LinkedHashSet<List<NWRecord>>>();

		for (Entry <String, List<NWRecord>>entry:map.entrySet()){
			LinkedHashSet<List<NWRecord>>setValues=new LinkedHashSet<List<NWRecord>>();
			String nomFichierPrincTemp=FilenameUtils.getBaseName(entry.getKey());
			nomFichierPrinc=StringUtils.substringBefore(nomFichierPrincTemp, "Chant");
			System.out.println(nomFichierPrinc);

			for (Entry <String, List<NWRecord>>entry2:map.entrySet()){
				if (entry2.getKey().startsWith(nomFichierPrinc)&&entry2!=entry){
					setValues.add(entry2.getValue());
					setValues.add(entry.getValue());
				}
			}
			listeDeListes.put(nomFichierPrinc, setValues);
		} 
		System.out.println(listeDeListes.size());
		System.out.println(listeDeListes.keySet());
		System.out.println(listeDeListes.values());
		for (Entry <String,LinkedHashSet<List<NWRecord>>> entryGlobal:listeDeListes.entrySet()){
			createFiles(entryGlobal.getValue(), entryGlobal.getKey());
		}
	}
	
	
	public static void createXMLFromNWRecordListByBook(String name, List<NWRecord>listeDeNWR){
			Element racine = new Element("file");
			racine.setAttribute("name", name);
			int counter=1;
			for (int i=1;i<listeDeNWR.size();i++){
				NWRecord record=listeDeNWR.get(i);
				Element ID = new Element("ID"+String.valueOf(counter));
				
					ID.setAttribute(new Attribute ("text", record.getTrg()));
					ID.setAttribute(new Attribute ("lemma", record.getLemma()));
				
				
				ID.setAttribute(new Attribute("tag",record.getTag()));
				racine.addContent(ID);
				counter++;
			}
			Document document = new Document(racine);
			XMLBuilder builder=new XMLBuilder();
			builder.enregistre(name, document);
	}
	
	public static void saveDistribDictToCSV (HashMap<String, Set<String>> hashMap, String folderPath) throws IOException{
		Path path = Paths.get(folderPath);
		
		
		File file =new File(folderPath+".tsv");
		if (!file.getParentFile().isDirectory()){
			Files.createDirectories(path);
		}
		
		@SuppressWarnings("resource")
		FileWriter writer = new FileWriter(file);

		writer.append("Clé\t");
		writer.append("Siminymes\t");
		writer.append('\n');
		for (String key:hashMap.keySet()){
			StringBuilder sb=new StringBuilder();
			for (String values:hashMap.get(key)){
				sb.append(values+ " ");
			}
			writer.append(key+"\t");
			writer.append(sb.toString()+"\t");
			writer.append('\n');
		}
	}

	public static void saveDistribDict (HashMap<Element, List<Element>> distribDict, String path){
		
		Element racine = new Element("root");
		for (Element key:distribDict.keySet()){
			List<Element>valuesDetached=new ArrayList<Element>();
			for (Element value:distribDict.get(key)){
				Element valueDetached=value.clone().detach();
				valuesDetached.add(valueDetached);
			}
			Element keyDetached=key.clone().detach();
			keyDetached.addContent(valuesDetached);
			racine.addContent(keyDetached);
		}
		Document document = new Document(racine);
		XMLBuilder builder=new XMLBuilder();
		builder.enregistre(path, document);
	}

	@SuppressWarnings("unused")
	public Element saveRootSecondDistribDict (HashMap<Element, List<Element>> mergedDistribDict, HashMap<Element, List<Element>>presentDistribDict, String name) throws JDOMException{
		Element root=new Element("root");
		for (Element elemAttached:mergedDistribDict.keySet()){
			Element elemDetached=elemAttached.detach();
			root.addContent(elemDetached);
		}
		Document doc=new Document(root);
		if (mergedDistribDict.keySet().isEmpty()){
//			System.out.println("le dictionnaire est encore vide");
			for (Element elemSource:presentDistribDict.keySet()){
				mergedDistribDict.put(elemSource, presentDistribDict.get(elemSource));
			}  
		}
		else {
			for (Element elemSource:presentDistribDict.keySet()){
				for (Element elemMerged:mergedDistribDict.keySet()){
					if (elemMerged.getAttributeValue("Key").matches(elemSource.getAttributeValue("Key"))){			
					}
					else{
					}
				}
				if (true){
					String query="//*[@Key='"+elemSource.getAttributeValue("Key")+"']";
					XPathExpression<Element> xpe = XPathFactory.instance().compile(query, Filters.element());
					for (Element urle : xpe.evaluate(doc)) {
						mergedDistribDict.get(urle).addAll(presentDistribDict.get(elemSource));
//						System.err.println("ajout des valeurs à une clé existante");
					}
				}
				else {
					mergedDistribDict.put(elemSource, presentDistribDict.get(elemSource));
					System.err.println("ajout de la clé ET des valeurs");
				}
			}

		}

		Element rootTarget=new Element("root");
		for (Element keyMerged:mergedDistribDict.keySet()){
			Element keyForMergedRoot=new Element("Key");
			keyForMergedRoot=keyMerged.clone();
			for(Element elemValue:mergedDistribDict.get(keyMerged)){
				Element valueForMergedRoot=new Element("Value");
				valueForMergedRoot=elemValue.clone();
				keyForMergedRoot.addContent(valueForMergedRoot);
			}
			rootTarget.addContent(keyForMergedRoot);
		}
		return rootTarget;
	}
	
	@SuppressWarnings("unused")
	public Element mergeRootSecondDict (HashMap<Element, List<Element>> mergedDistribDict, HashMap<Element, List<Element>>presentDistribDict) throws JDOMException{
		Element root=new Element("root");
		for (Element elemAttached:mergedDistribDict.keySet()){
			Element elemDetached=elemAttached.detach();
			root.addContent(elemDetached);
		}
		Document doc=new Document(root);
		if (mergedDistribDict.keySet().isEmpty()){
			System.out.println("le dictionnaire est encore vide");
			for (Element elemSource:presentDistribDict.keySet()){
				mergedDistribDict.put(elemSource, presentDistribDict.get(elemSource));
			}  
		}
		else {
			for (Element elemSource:presentDistribDict.keySet()){
				for (Element elemMerged:mergedDistribDict.keySet()){
					if (elemMerged.getAttributeValue("Key").matches(elemSource.getAttributeValue("Key"))){			
					}
					else{
					}
				}
				if (true){
					String query="//*[@Key='"+elemSource.getAttributeValue("Key")+"']";
					XPathExpression<Element> xpe = XPathFactory.instance().compile(query, Filters.element());
					for (Element urle : xpe.evaluate(doc)) {
						mergedDistribDict.get(urle).addAll(presentDistribDict.get(elemSource));
//						System.err.println("ajout des valeurs à une clé existante");
					}
				}
				else {
					mergedDistribDict.put(elemSource, presentDistribDict.get(elemSource));
					System.err.println("ajout de la clé ET des valeurs");
				}
			}

		}

		Element rootTarget=new Element("root");
		for (Element keyMerged:mergedDistribDict.keySet()){
			Element keyForMergedRoot=new Element("Entry");
			keyForMergedRoot=keyMerged.clone();
			for(Element elemValue:mergedDistribDict.get(keyMerged)){
				Element valueForMergedRoot=new Element("Value");
				valueForMergedRoot=elemValue.clone();
				keyForMergedRoot.addContent(valueForMergedRoot);
			}
			rootTarget.addContent(keyForMergedRoot);
		}
		return rootTarget;
	}
	
	public static HashMap<Element, List<Element>>rootToDict(Element root, HashMap<Element, List<Element>>dict){
		for (Element key:root.getChildren()){
			dict.put(key, key.getChildren());
		}
		return dict;
		
	}
	
	public static Element dictToRoot(HashMap<Element, List<Element>>dict){
		Element root=new Element("root");
		if (dict.keySet().isEmpty()){
			System.out.println("LE DICTIONNAIRE EST VIDE");
		}
		for (Element key:dict.keySet()){
			Element keyRoot=new Element("Entry");
			keyRoot.setAttribute("Key", key.getAttributeValue("Key"));
			for (Element value:dict.get(key)){
				Element valueRoot=new Element("Value");
				valueRoot.setAttribute("value", value.getAttributeValue("value"));
				valueRoot.setAttribute("score", value.getAttributeValue("score"));
				keyRoot.addContent(valueRoot);
			}
			root.addContent(keyRoot);
		}
		return root;
		
	}
  public void createFiles1(LinkedHashSet<List<NWRecord>>liste, String name) throws IOException{
    System.setProperty("treetagger.home", "/home/bilbo/Bureau/outilsJava/TreeTaggerInstall/");
    System.out.println("longueur de la liste de listes : "+liste.size());
    
    /* c'est dans cette fonction qu'on crée les xml de sortie, avec les ID fixes ayant comme attributs les POS*/

    Element racine = new Element("racineGrecque");
    
    Element racine2 = new Element("author");
   
    int numeroDuChant=1;
//    int numeroDuChant=1;

for (List<NWRecord>nouvelleListe:liste){
  
  
      Element book = new Element("book"+numeroDuChant);
      
      Element book2= new Element("book"+numeroDuChant);
      
      int counter=0;

      for (NWRecord record:nouvelleListe){
        Element ID = new Element("ID"+String.valueOf(counter+1));
        Element ID2 = new Element("ID"+String.valueOf(counter+1));
        ID.setText(record.getSrc());
        ID2.setText(record.getTrg());
           
        ID2.setAttribute(new Attribute ("lemma", record.getLemma()));
        ID2.setAttribute(new Attribute("tag",record.getTag()));
        book.addContent(ID);
        book2.addContent(ID2);
        counter++;
      }
      racine.addContent(book);
      
      racine2.addContent(book2);
      numeroDuChant++;
    }

//racine.sortChildren(Ordering.usingToString());
//racine2.sortChildren(Ordering.usingToString());
Document document = new Document(racine);
Document document2 = new Document(racine2);
XMLBuilder builder=new XMLBuilder();
builder.enregistre("./Output/outPutFrancais/"+name+"Target.xml", document2);
builder.enregistre("./Output/outPutGrec/GreekSource1000Target.xml", document);
  }
  
  public void mergeFiles1(TreeMap<String, List<NWRecord>> map) throws IOException{
    
    String nomFichierPrinc="";
    Map<String,LinkedHashSet<List<NWRecord>>>listeDeListes=new HashMap<String,LinkedHashSet<List<NWRecord>>>();
    
    for (Entry <String, List<NWRecord>>entry:map.entrySet()){
      LinkedHashSet<List<NWRecord>>setValues=new LinkedHashSet<List<NWRecord>>();
      String nomFichierPrincTemp=FilenameUtils.getBaseName(entry.getKey());
      nomFichierPrinc=StringUtils.substringBefore(nomFichierPrincTemp, "Chant");
      System.out.println(nomFichierPrinc);
     
      for (Entry <String, List<NWRecord>>entry2:map.entrySet()){
        if (entry2.getKey().startsWith(nomFichierPrinc)&&entry2!=entry){
          setValues.add(entry2.getValue());
          setValues.add(entry.getValue());
        }
      }
      listeDeListes.put(nomFichierPrinc, setValues);
    } 
    System.out.println(listeDeListes.size());
    System.out.println(listeDeListes.keySet());
    for (Entry <String,LinkedHashSet<List<NWRecord>>> entryGlobal:listeDeListes.entrySet()){
      createFiles(entryGlobal.getValue(), entryGlobal.getKey());
    }
  }
  
  
  public void saveMainDistribDict (HashMap <String, Set<String>> distribDict){
    Element racine = new Element("DistribDict");
    for (String key:distribDict.keySet()){
      if (key.equals("")==false){
        Element KEY = new Element(key); 
        Set<String>values=distribDict.get(key);
        for (String value:values){
          Element VALUE = new Element(value);
          KEY.addContent(VALUE);
        }
        racine.addContent(KEY);
      }
    }
    Document document = new Document(racine);
    XMLBuilder builder=new XMLBuilder();
    builder.enregistre("./Output/DistribDict/DistribDict.xml", document);
  }

}