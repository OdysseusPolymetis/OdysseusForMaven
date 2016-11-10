package fr.odysseus.api;

import java.io.File;

public class Init {

	final static String SOURCE="./sourceFiles/plainTxt";
	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis();

//		BookDivision division=new BookDivision();
//		File fileTest = new File(SOURCE);     
//		File[] directories = fileTest.listFiles();
//		division.frenchBookDivision(directories);
//		new FrenchTagger();
//		new CollectFrenchSequences();
//		new LemmaLatin();
//		new LemmaGreek();
		Aligner align= new Aligner();
		align.proceedToGlobalAlignment();
//		CalculStats calculs=new CalculStats();
//		calculs.initContentEditorPanes();
		long endTime = System.currentTimeMillis();
		System.out.println("****************");
		System.out.println("TEMPS D'EXÉCUTION GLOBAL : " +((endTime-startTime)/1000) );
		System.out.println("****************");
	}
}