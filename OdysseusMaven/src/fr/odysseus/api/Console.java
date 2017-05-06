package fr.odysseus.api;

import java.io.File;
import java.util.Scanner;

/**
 * main console where main parts of the program are called
 */
public class Console {
	
	final static String SOURCE="./input/txts";
	static final String SEQUENCESFR="./input/seq/frSeq/";
	static final String SEQUENCESGR="./input/seq/grSeq/";
	static final String SEQUENCESLAT="./input/seq/latSeq/";
	static final String PUNCT="./input/seq/grPct/";
	static final String PIVOT="./input/seq/pivot/";
	public static final String NAMESFR="./input/names/frname/";
	public static final String NAMESGR="./input/names/grname/";
	public static final String NAMESLAT="./input/names/latname/";
	static final String DICOVEK="./input/dict/dicovek/";
	static final String W2V="./input/dict/wtov/";
	static final String GRDICT="./input/dict/dict.tsv";
	static final String OUTPUTDICT="./output/dict/wtov/";
	static final String OUTPUT="./output/";
	static final String OUTPUTXML="./output/xml/";
	static final String OUTPUTHTML="./output/html/";
	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis();
		@SuppressWarnings("resource")
		Scanner sc=new Scanner(System.in);
		System.out.println("Voulez-vous faire/refaire la division des livres ? (y/n)");
		String answer=sc.nextLine();
		if (answer.equals("y")){
			BookDivision division=new BookDivision();
			File fileTest = new File(SOURCE);     
			File[] directories = fileTest.listFiles();
			division.frenchBookDivision(directories);
		}
			
		System.out.println("Voulez-vous faire/refaire l'étiquetage français ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			FrenchTagger.lemmaWithAlix();
		}
		
		System.out.println("Voulez-vous faire/refaire les séquences français(es) ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			new CollectFrenchSequences();
		}
		
		System.out.println("Voulez-vous faire/refaire les séquences latines ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			new LemmaLatin();
		}
		
		System.out.println("Voulez-vous faire/refaire les séquences grecques ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			new LemmaGreek();
		}
		
		System.out.println("Voulez-vous faire/refaire un alignement ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			AligningProcess align= new AligningProcess();
			align.proceedToGlobalAlignment();
		}
		
		System.out.println("Voulez-vous faire/refaire une comparaison statistique ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			Count calculs=new Count();
			calculs.automaticComparison();
		}
		
			
		long endTime = System.currentTimeMillis();
		System.out.println("****************");
		System.out.println("TEMPS D'EXÉCUTION GLOBAL : " +((endTime-startTime)/1000) );
		System.out.println("****************");
	}
}