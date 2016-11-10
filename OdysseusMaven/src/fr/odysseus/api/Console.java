package fr.odysseus.api;

import java.io.File;
import java.util.Scanner;

public class Console {

	final static String SOURCE="./sourceFiles/plainTxt";
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
		System.out.println("Voulez-vous faire/refaire l'étiquetage ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			new FrenchTagger();
		}
		System.out.println("Voulez-vous faire/refaire les xml français ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			new CollectFrenchSequences();
		}
		System.out.println("Voulez-vous faire/refaire les xml latins ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			new LemmaLatin();
		}
		System.out.println("Voulez-vous faire/refaire les xml grecs ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			new LemmaGreek();
		}
		System.out.println("Voulez-vous faire/refaire un alignement ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			Aligner align= new Aligner();
			align.proceedToGlobalAlignment();
		}
		System.out.println("Voulez-vous faire/refaire une comparaison statistique ? (y/n)");
		answer=sc.nextLine();
		if (answer.equals("y")){
			CalculStats calculs=new CalculStats();
			calculs.initContentEditorPanes();
		}
	
		long endTime = System.currentTimeMillis();
		System.out.println("****************");
		System.out.println("TEMPS D'EXÉCUTION GLOBAL : " +((endTime-startTime)/1000) );
		System.out.println("****************");
	}
}