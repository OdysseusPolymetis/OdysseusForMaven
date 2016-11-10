/**
 * 
 */
package fr.odysseus.dataModels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import fr.odysseus.utils.IO;

/**
 * @author angelodel80
 *
 */
public class ResourceCollection {

	LinkedHashMap<String, String> collection;
	LinkedHashMap<String, List<Element>> collectionGreek;
	
	/**
	 * 
	 */
	private ResourceCollection() {
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		collection = new LinkedHashMap<String, String>();
		collectionGreek = new LinkedHashMap<String, List<Element>>();
		
	}
	
	static public ResourceCollection newInstance(){
		return new ResourceCollection();
		
	}
	
	
	public void add(String name, String path){
		IO input = new IO(path);
		String resource = "";
		if(input!=null)
			resource = input.readAll();
		collection.put(name, resource);
		input.close();
	}
	public void addGreek(String name, String path) throws JDOMException, IOException{
    IO input = new IO(path);
    
    List<Element> resource=new ArrayList<Element>();
    
    resource = input.readAllGreek(path);
    collectionGreek.put(name, resource);  
    
    input.close();
  }
	
	public String get(String name){
		return collection.get(name);
	}
	
//	public void addLemma (String name, String path) throws JDOMException, IOException{
//	  IO input = new IO(path);
//	  List<Element> resource = new ArrayList<Element>();
//    if(input!=null)
//      resource = input.readAllXML(path);
//    StringBuffer lemmaSplitable=new StringBuffer();
//    for (Element lemma:resource){ 
//      lemmaSplitable.append("iii");
//      lemmaSplitable.append(lemma.getAttributeValue("lemma"));
//      
//    }
//    collection.put(name, lemmaSplitable.toString());
//    input.close();
//	}
	public void addText (String name,  String path) throws JDOMException, IOException{
    IO input = new IO(path);
    List<Element> resource = new ArrayList<Element>();
    if(input!=null)
      resource = input.readAllXML(path);
    StringBuffer textSplitable=new StringBuffer();
    for (Element sequence:resource){ 
      textSplitable.append("\n");
      textSplitable.append(sequence.getAttributeValue("text"));
    }
    collection.put(name, textSplitable.toString());
    input.close();
  }
	
	public void addAtt (String name, String attribute, String path) throws JDOMException, IOException{
    IO input = new IO(path);
    List<Element> resource = new ArrayList<Element>();
    if(input!=null)
      resource = input.readAllXML(path);
    StringBuffer lemmaSplitable=new StringBuffer();
    for (Element lemma:resource){ 
      lemmaSplitable.append("\n");
      lemmaSplitable.append(lemma.getAttributeValue(attribute));
    }
    collection.put(name, lemmaSplitable.toString());
    input.close();
  }
	
	public List<Element> getGreek(String name){
    return collectionGreek.get(name);
  }
	
//	public String getGreekString(String name){
//
//    StringBuilder strB=new StringBuilder();
//    for (String str:getGreekLines(name)){
//      strB.append(str+"\\n");
//    }
//    return strB.toString();
//	}
	
//	public String getGreekLemmaString(String name){
//
//    StringBuilder strB=new StringBuilder();
//    for (String str:getGreekLemmaLines(name)){
//      strB.append(str+"\\n");
//    }
//    return strB.toString();
//  }
	
	
	
	public String[] getLines(String name){
		StringBuffer resource = new StringBuffer(this.get(name));
		return split(resource, "\\n");
	}
	
	public String[] getLemmaLines(String name){
    StringBuffer resource = new StringBuffer(this.get(name));
    return split(resource, "\\n");
  }
	
	public String[] getTagLines(String name){
    StringBuffer resource = new StringBuffer(this.get(name));
    return split(resource, "\\n");
  }
	
	public String[] getTextLines(String name){
    StringBuffer resource = new StringBuffer(this.get(name));
    return split(resource, "\\n");
  }
	
	
//	public String[] getGreekLines(String name){
//    
//    List<Element> resource = new ArrayList<Element>(this.getGreek(name));
//    StringBuffer strBuild=new StringBuffer();
//    for (Element eSentence : resource) {
//      List<Element> listeWords = eSentence.getChildren("word");
//      StringBuffer strBuild2=new StringBuffer();
//      for (Element eWord : listeWords){
//        String form = eWord.getAttributeValue("form");
//        strBuild2.append(form+" ");
//      }
//      strBuild.append(strBuild2+"\n");
//    }   
//    return split(strBuild, "\\n");   
//  }
	
//public String[] getGreekLemmaLines(String name){
//    
//    List<Element> resource = new ArrayList<Element>(this.getGreek(name));
//   
//    StringBuffer strBuild=new StringBuffer();
//    for (Element eSentence : resource) {
//      List<Element> listeWords = eSentence.getChildren("word");
//      
//      StringBuffer strBuild2=new StringBuffer();
//      for (Element eWord : listeWords){
//        String form = eWord.getAttributeValue("lemma");
//        
//        strBuild2.append(form+" ");
//      }
//      strBuild.append(strBuild2);
//    } 
//    return split(strBuild, "\\n");   
//  }
	
	public String[] getTokensSpaces(String name){
		StringBuffer resource = new StringBuffer(this.get(name));
		return split(resource, "\\s");
		
	}
//	public String[] getGreekTokensSpaces(String name){
//    StringBuffer resource = new StringBuffer(this.getGreekString(name));
//    return split(resource, "\\s");
//    
//  }
	
	public String[] split(StringBuffer resource, String pattern){
		return new String(resource).split(pattern);
	}
	
	public Set<String> getTokenTypes(String name){
		Set<String> ret = new LinkedHashSet<String>( Arrays.asList(getTokensSpaces(name)));
		return ret;
	}
//	public Set<String> getGreekTokenTypes(String name){
//    Set<String> ret = new LinkedHashSet<String>( Arrays.asList(getGreekTokensSpaces(name)));
//    return ret;
//  }
	
	

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}