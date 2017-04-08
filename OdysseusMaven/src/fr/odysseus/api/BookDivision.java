package fr.odysseus.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * dividing all raw texts into seperate books and files
 */
public class BookDivision {

	final static String OUT="./sourceFiles/plainTxtByBook/";
	public void frenchBookDivision(File[] repertoire) throws Exception {
		
		for (File file:repertoire){
			HashMap<String, String>numText=new HashMap<String, String>();
			String path=file.getPath();;
			String text=readFile(path, StandardCharsets.UTF_8);
			text=text.replaceAll("\\s0[a-z]*", "O");

			List<String>numsChants=new ArrayList<String>();
			Pattern p= Pattern.compile("Chant[0-9]+");
			Matcher m=p.matcher(text);
			
			while (m.find()){
				numsChants.add(m.group());
			}
			String []texteParChant=text.split("Chant[0-9]+");
			System.out.println("Division en cours : "+file.getName());
			int index=1;
			for (String nombre:numsChants){
				 numText.put(nombre, texteParChant[index]);
				 index++;
			}  
			
			for (String titleChant:numText.keySet()){
				String nomDeBaseDuFichier=file.getName().substring(0, file.getName().indexOf(".txt"));
				String numChant=titleChant.substring(5);
				int intNum=Integer.valueOf(numChant);
				Writer writer;
				if (Integer.valueOf(numChant)<10){
					Path pathToFile = Paths.get(OUT+"Chant0"+(intNum)+"/");
					File filePerBook =new File(OUT+"Chant0"+(intNum)+"/"+nomDeBaseDuFichier+"Chant0"+(intNum)+".txt");
					if (!filePerBook.getParentFile().isDirectory()){
						Files.createDirectories(pathToFile);
					}
					writer = new BufferedWriter(new OutputStreamWriter(
						    new FileOutputStream(OUT+"Chant0"+(intNum)+"/"+nomDeBaseDuFichier+"Chant0"+(intNum)+".txt"), "UTF-8"));
				}
				else{
					Path pathToFile = Paths.get(OUT+"Chant"+(intNum)+"/");
					File filePerBook =new File(OUT+"Chant"+(intNum)+"/"+nomDeBaseDuFichier+"Chant"+(intNum)+".txt");
					if (!filePerBook.getParentFile().isDirectory()){
						Files.createDirectories(pathToFile);
					}
					writer = new BufferedWriter(new OutputStreamWriter(
						    new FileOutputStream(OUT+"Chant"+(intNum)+"/"+nomDeBaseDuFichier+"Chant"+(intNum)+".txt"), "UTF-8"));
				}
				String chant=numText.get(titleChant);
				chant=chant.replaceAll("[*]", "");
				chant=chant.replaceAll("[0-9]*", "");
				chant=chant.replace("\\", "");
				chant=chant.replace("■","");
				chant=chant.replace("((", "«");
				chant=chant.replace("))", "»");
				chant=chant.replace("&lt;&lt;", "''");
				chant=chant.replace("<<", "''");
				chant=chant.replace(">>", "''");
				chant=chant.replaceAll("\\­"," - ");
				chant=chant.replaceFirst("\\n", "");
				chant=chant.replaceAll("\\n", " / ");
				chant=chant.replaceAll("[\\s\\xA0\\t\\n\\x0B\\f\\r]+", " ");
				chant=chant.replace(" / / ", " / ");
				chant=chant.replace("Pallas-Minerve", "Pallas - Minerve");
				chant=chant.replace("Minerve-Pallas", "Minerve - Pallas");
				chant=chant.replaceAll("Pallas Athéné", "Pallas_Athéné");
				Pattern pCorrect=Pattern.compile("([a-z]+)([A-Z]{1}[a-zéèêôûîùà]+)");
				Matcher mCorrect=pCorrect.matcher(chant);
				while (mCorrect.find()){
					chant=chant.replaceAll(mCorrect.group(), mCorrect.group(1)+" "+mCorrect.group(2));
				}
				writer.write(chant);
				writer.close();
			}
		}
	}
	static String readFile(String path, Charset encoding) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}


	private static int decodeSingle(char letter) {
		switch(letter) {
		case 'M': return 1000;
		case 'D': return 500;
		case 'C': return 100;
		case 'L': return 50;
		case 'X': return 10;
		case 'V': return 5;
		case 'I': return 1;
		default: return 0;
		}
	}
	public static int decode(String roman) {
		int result = 0;
		String uRoman = roman.toUpperCase(); //case-insensitive
		for(int i = 0;i < uRoman.length() - 1;i++) {//loop over all but the last character
			//if this character has a lower value than the next character
			if (decodeSingle(uRoman.charAt(i)) < decodeSingle(uRoman.charAt(i+1))) {
				//subtract it
				result -= decodeSingle(uRoman.charAt(i));
			} else {
				//add it
				result += decodeSingle(uRoman.charAt(i));
			}
		}
		//decode the last character, which is always added
		result += decodeSingle(uRoman.charAt(uRoman.length()-1));
		return result;
	}


}