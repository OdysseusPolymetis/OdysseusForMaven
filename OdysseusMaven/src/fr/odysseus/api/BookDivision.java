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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//import org.apache.commons.lang3.StringUtils;




public class BookDivision {

	final static String OUT="./sourceFiles/plainTxtByBook/";
	public void frenchBookDivision(File[] repertoire) throws Exception {
		
//		log.info("Début du découpage en chants de chaque texte français");
		
		for (File file:repertoire){
			String path=file.getPath();;
			String text=readFile(path, StandardCharsets.UTF_8);
			//			System.out.println("Pour le fichier : "+path);
			text=text.replaceAll("\\s0[a-z]*", "O");

			String []tabTitre=text.split("\\n");
			int index=0;
			for (String nombre:tabTitre){
				Pattern p1= Pattern.compile(".*(Chant [A-Z]+)");
				Matcher m1=p1.matcher(nombre);
				Pattern p2=Pattern.compile("\\b.*[A-Z]+\\b");
				Matcher m2=p2.matcher(nombre);
				while (m1.find()){

					if (nombre.length()<13){
//												System.out.println("Cas 1 : "+m1.group());	
						String chiffreRomain=m1.group().substring(6);
//												System.out.println(decode(chiffreRomain));
						nombre=nombre.replace(m1.group(), "Chant"+String.valueOf(decode(chiffreRomain)));
						tabTitre[index]=nombre.replace(m1.group(), nombre);

					}
				}


				while (m2.find()){
					if (nombre.length()<7){
//												System.out.println(text);
//												System.out.println("Cas 2 : "+m2.group());
						tabTitre[index]=nombre.replace(m2.group(), "Chant"+String.valueOf(decode(m2.group())));
						//						System.out.println("Chant"+String.valueOf(decode(m2.group())));
					}
				}
				index++;
			}  
			StringBuilder sb= new StringBuilder();

			for (String ligne:tabTitre){
				sb.append(ligne+"\n");
			}

//			String tabText[]=sb.toString().split("Chant([0-9]+)");
//			Pattern p3=Pattern.compile("Chant([0-9]+)");
//			Matcher m3=p3.matcher(sb.toString());
//			List<String>texteDeChaqueChant=new ArrayList();
			String []texteDeChaqueChant=sb.toString().split("Chant([0-9]+)");
//			while (m3.find()){
////				System.out.println(m3.group());
////				texteDeChaqueChant.add
//			}
			
//			for (String chant : texteDeChaqueChant){
////				System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
////				System.out.println(chant);
//			}
			
			int i=0;
			for (String chant:texteDeChaqueChant){
				if (i>0){
					Writer writer;
					if (chant!=""||chant.matches("\\n")){
						String nomDeBaseDuFichier=path.substring(path.lastIndexOf("Txt")+3, path.indexOf(".txt"));
						if (i<10){
							Path pathToFile = Paths.get(OUT+"Chant0"+(i)+"/");
							File filePerBook =new File(OUT+"Chant0"+(i)+"/"+nomDeBaseDuFichier+"Chant0"+(i)+".txt");
							if (!filePerBook.getParentFile().isDirectory()){
								Files.createDirectories(pathToFile);
							}
							writer = new BufferedWriter(new OutputStreamWriter(
								    new FileOutputStream(OUT+"Chant0"+(i)+"/"+nomDeBaseDuFichier+"Chant0"+(i)+".txt"), "UTF-8"));
						}
						else{
							Path pathToFile = Paths.get(OUT+"Chant"+(i)+"/");
							File filePerBook =new File(OUT+"Chant"+(i)+"/"+nomDeBaseDuFichier+"Chant"+(i)+".txt");
							if (!filePerBook.getParentFile().isDirectory()){
								Files.createDirectories(pathToFile);
							}
							writer = new BufferedWriter(new OutputStreamWriter(
								    new FileOutputStream(OUT+"Chant"+(i)+"/"+nomDeBaseDuFichier+"Chant"+(i)+".txt"), "UTF-8"));
						}
						
						
						
						
						chant=chant.replaceAll("[*]", "");
						chant=chant.replaceAll("[0-9]*", "");
						chant=chant.replace("\\", "");
						chant=chant.replace("■","");
						chant=chant.replace("((", "«");
						chant=chant.replace("))", "»");
						chant=chant.replaceAll("\\­"," - ");
						chant=chant.replaceFirst("\\n", "");
						chant=chant.replaceAll("\\n", " / ");
						chant=chant.replaceAll("[\\s\\xA0\\t\\n\\x0B\\f\\r]+", " ");
						chant=chant.replace(" / / ", " / ");
						chant=chant.replace("Pallas-Minerve", "Pallas - Minerve");
						chant=chant.replace("Minerve-Pallas", "Minerve - Pallas");
						chant=chant.replaceAll("Pallas Athéné", "Pallas_Athéné");
						Pattern p=Pattern.compile("([a-z]+)([A-Z]{1}[a-zéèêôûîùà]+)");
						Matcher m=p.matcher(chant);
						while (m.find()){
//							System.out.println(m.group());
							chant=chant.replaceAll("([a-z]+)([A-Z]{1}[a-zéèêôûîùà]+)", m.group(1)+" "+m.group(2));
//							System.out.println(m.group(1)+" "+m.group(2));
						}
//						System.out.println(chant);
						writer.write(chant);
						
						writer.close ();
						
					}
				}
				i++;
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