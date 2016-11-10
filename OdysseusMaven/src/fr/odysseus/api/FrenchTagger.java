package fr.odysseus.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fr.odysseus.utils.Lemmatizers;

public class FrenchTagger {
	final static String SOURCE="./sourceFiles/plainTxtByBook/";
	final static String OUT="./sourceFiles/xml/frenchXML/";
	HashMap <String, String[]>tags;
	public HashMap<String, String[]> getTags() {
		return tags;
	}
	public FrenchTagger() throws IOException{
		System.out.println("Début du Tagging");

		System.setProperty("treetagger.home", "./lib/taggers/Treetagger/");
		MaxentTagger tagger =  new MaxentTagger("./lib/taggers/StanfordTagger/french.tagger");
		

		TreeTaggerWrapper<String>tt = new TreeTaggerWrapper<String>();
		List <Object>listAllFiles=new ArrayList<Object>();
		Path start=Paths.get(SOURCE);	
		Files.walk(start)
			.filter( path -> path.toFile().isFile())
			.filter( path -> path.toString().endsWith(".txt"))
			.forEach(listAllFiles::add);

		for (Object path:listAllFiles){
			List<String[]>motsTags=new ArrayList<String[]>();
			String fileName=path.toString().substring(path.toString().lastIndexOf("\\"),path.toString().lastIndexOf(".txt"));
			String text=readFile(path.toString(), StandardCharsets.UTF_8);
			Lemmatizers lemmatizers=new Lemmatizers();
			text=text.replaceAll("'", " ' ");
			text=text.replaceAll("’", " ' ");
			text=text.replaceAll(",", " , ");
			text=text.replaceAll("\\.", " . ");
			text=text.replaceAll(";", " ; ");
			text=text.replaceAll(":", " : ");
			text=text.replaceAll("-", " - ");
			text=text.replaceAll("\\s{2,}", " ");
			lemmatizers.getNames(text, motsTags, tt, tagger);
			motsTags.addAll(lemmatizers.getTags());
			
			System.out.println("Stockage en XML du fichier "+fileName);
			Element root = new Element("root");
			Element book=new Element ("book"+fileName.substring(fileName.lastIndexOf("Chant")+5));
			Document doc = new Document(root);
			
			List<List<String[]>> sentences = motsTags.stream()
		            .collect(() -> {
		                List<List<String[]>> list = new ArrayList<>();
		                list.add(new ArrayList<>());
		                return list;
		            },
		            (list, s) -> {
		                if (s[2].contains("SENT")) {
		                    list.add(new ArrayList<>());
		                } else {
		                    list.get(list.size() - 1).add(s);
		                }
		            },
		            (list1, list2) -> {
		                list1.get(list1.size() - 1).addAll(list2.remove(0));
		                list1.addAll(list2);
		            });
			
			
			for (List<String[]>sentence:sentences){
				Element sent=new Element("sentence");
				Element punc=new Element("word");
				punc.setAttribute("form", ".");
				punc.setAttribute("lemma", ".");
				punc.setAttribute("postag", "PUN");
				for (String []word:sentence){
					if (!word[0].matches("-[LRB]{3,}-")){
						Element mot=new Element("word");
						mot.setAttribute("form",word[0]);
						mot.setAttribute("lemma",word[1]);
						mot.setAttribute("postag",word[2]);
						sent.addContent(mot);
					}
				}
				sent.addContent(punc);
				book.addContent(sent);
			}
			root.addContent(book);
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			File fileOut = new File(OUT+fileName+".xml");
			fileOut.getParentFile().mkdirs();
			xmlOutput.output(doc, new FileWriter(fileOut));
		}
	}
	static String readFile(String path, Charset encoding) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
}
