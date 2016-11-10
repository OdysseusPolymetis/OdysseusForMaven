/**
 * 
 */
package fr.odysseus.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * @author angelodel80
 * 
 * inspired from princeton.edu 
 */
public class IO {

	private Scanner scanner;
	private SAXBuilder xmlBuilder;

	   private static final String charsetName = "UTF-8";
	   private static final java.util.Locale usLocale = 
	        new java.util.Locale("fr", "FR");
	   
//	private static final Pattern EMPTY = Pattern.compile("");
	   
	private static final Pattern WHITESPACE = Pattern.compile("\\p{javaWhitespace}+");
	private static final Pattern EVERYTHING = Pattern.compile("\\A");

	/**
	 * 
	 */
	public IO(String path) {
		// TODO Auto-generated constructor stub


		try {
			// read file from local file system
			File file = new File(path);
			if (file.exists()) {
				scanner = new Scanner(file, charsetName);
				xmlBuilder=new SAXBuilder();
				scanner.useLocale(usLocale);
				return;
			}

			// next try for files included in jar
			URL url = getClass().getResource(path);

			// or URL from web
			if (url == null) { url = new URL(path); }

			URLConnection site = url.openConnection();

			// in order to set User-Agent, replace above line with these two
			// HttpURLConnection site = (HttpURLConnection) url.openConnection();
			// site.addRequestProperty("User-Agent", "Mozilla/4.76");

			InputStream is     = site.getInputStream();
			scanner            = new Scanner(new BufferedInputStream(is), charsetName);
			scanner.useLocale(usLocale);
		}
		catch (IOException ioe) {
			System.err.println("Could not open " + path);
		}
	}

	public String readAll() {
		if (!scanner.hasNextLine())
			return "";

		String result = scanner.useDelimiter(EVERYTHING).next();
		scanner.useDelimiter(WHITESPACE); // but let's do it anyway
		return result;
	}
	
	public List<Element> readAllGreek(String path) throws JDOMException, IOException {
	  // read file from local file system
      Document document = xmlBuilder.build(new File(path));
      Element rootNode = document.getRootElement();
      List<Element> listeSentences = rootNode.getChildren("sentence");
    return listeSentences;
  }
	
	public List<Element> readAllXML(String path) throws JDOMException, IOException {
    // read file from local file system
      Document document = xmlBuilder.build(new File(path));
      Element rootNode = document.getRootElement();
      List<Element> listeSentences = rootNode.getChildren();
    return listeSentences;
  }
	
	public void close() {
        scanner.close();  
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
 
	}

}