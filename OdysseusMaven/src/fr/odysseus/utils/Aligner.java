package fr.odysseus.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fr.odysseus.dataModels.NWRecord;
import fr.odysseus.dataModels.ResourceCollection;
import fr.odysseus.utils.dictionary.CloseMatcher;

public class Aligner {

	public static final String SOURCE_HOME=".";
	static final String SEQUENCESFR="./sourceFiles/sequences/frenchSequences/";
	static final String SEQUENCESGR="./sourceFiles/sequences/greekPunct/";
	static final String SEQUENCESLAT="./sourceFiles/sequences/latinSequences/";
	static final String PUNCT="./sourceFiles/sequences/greekPunct/";
	static final String PIVOT="./sourceFiles/sequences/pivot/";
	static final String NAMESFR="./sourceFiles/names/frenchNames/";
	static final String DICOVEK="./sourceFiles/sourceDictionaries/repertoireDicovek/";
	static final String W2V="./sourceFiles/sourceDictionaries/word2Vec/";
	static final String OUTPUTDICT="./outputFiles/dictionaries/word2Vec/";
	static final String OUTPUT="./outputFiles/";
	CloseMatcher close;
	private double gapPenality;
	private HashMap<String, Set<String>> dictionary;
	public HashMap<String, Set<String>> distribDict;
	public HashMap<String, Integer> srcFrequency;
	public HashMap<String, Integer> trgFrequency;

	public void setDictionary(HashMap<String, Set<String>> dictionary) {
		this.dictionary = dictionary;
	}

	public HashMap<String, Set<String>> getDictionary() {
		return dictionary;
	}

	public double getGapPenality() {
		return gapPenality;
	}

	public void setGapPenality(double gapPenality) {
		this.gapPenality = gapPenality;
	}

	public HashMap<String, Set<String>> getDistribDict() {
		return distribDict;
	}

	public void setDistribDict(HashMap<String, Set<String>> dict) {
		this.distribDict = dict;
	}

	public HashMap<String, Integer> getSrcFrequency() {
		return srcFrequency;
	}

	public void setSrcFrequency(HashMap<String, Integer> frequency) {
		this.srcFrequency = frequency;
	}

	public HashMap<String, Integer> getTrgFrequency() {
		return trgFrequency;
	}

	public void setTrgFrequency(HashMap<String, Integer> frequency) {
		this.trgFrequency = frequency;
	}


	public LinkedList<NWRecord> alignment(String fileName,String path, ResourceCollection myTexts, HashMap<String, Set<String>> dictionary, String sequences) throws Exception {
		NeedlemanWunsch nw= new NeedlemanWunsch();
		String numChant=fileName.substring(fileName.indexOf("Chant")+5);
		long startTime = System.currentTimeMillis();
		this.setGapPenality(0.0);
		setDictionary(dictionary);
		setDistribDict(getDistribDict());
		Path pathTrg=Paths.get(path+fileName+".txt");
		Path pathSrc=Paths.get(NAMESFR+"Sommer1886Chant"+numChant+".txt");
		List<String> noms = Files.readAllLines(pathTrg, StandardCharsets.UTF_8);
		List<String> nomsGr = Files.readAllLines(pathSrc, StandardCharsets.UTF_8);
		setSrcFrequency(Frequency.frequency(nomsGr));
		setTrgFrequency(Frequency.frequency(noms));
		LinkedList<NWRecord> maListe=new LinkedList<NWRecord>();

		maListe = nw.PerformAlignment(myTexts.getLemmaLines("sequencesPivotLemmes"), myTexts.getLemmaLines(sequences+"Lemmes"),
				myTexts.getTextLines(sequences), myTexts.getTagLines(sequences+"Tags"), getDictionary(), getDistribDict(), getSrcFrequency(),getTrgFrequency());
		


		long endTime = System.currentTimeMillis();
		System.out.println("****************");
		System.out.println("ALIGNEMENT : " + ((endTime - startTime) / 1000));
		System.out.println("****************");
		System.out.println("*********************************************************************************************");
		System.out.println("REAGENCEMENT DES ALIGNEMENTS");
		System.out.println("*********************************************************************************************");
		LinkedList<NWRecord> nouvelleListe = new LinkedList<NWRecord>();
		nouvelleListe.addAll(maListe);
		ReorderingLists order=new ReorderingLists();
		nouvelleListe=order.reorder(maListe, nouvelleListe);
		return nouvelleListe;
	}
}
