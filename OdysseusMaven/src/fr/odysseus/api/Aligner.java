package fr.odysseus.api;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import fr.odysseus.dataModels.ResourceCollection;
import fr.odysseus.muthovek.Dicovek.SimRow;
import fr.odysseus.dataModels.Lexik;
import fr.odysseus.muthovek.Dicovek;
import fr.odysseus.dataModels.Tokenizer;
import fr.odysseus.dataModels.NWRecord;
import fr.odysseus.utils.CreateFiles;
import fr.odysseus.utils.Frequency;
import fr.odysseus.utils.NeedlemanWunsch;
import fr.odysseus.utils.WordToVec;
import fr.odysseus.utils.dictionary.CloseMatcher;

public class Aligner{

	public static final String SOURCE_HOME=".";
	static final String SEQUENCESFR="./sourceFiles/sequences/frenchSequences/";
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

	public void initCloseMatcher(){
		close = new CloseMatcher();
		try {
			setDictionary(close.getDictionary());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void proceedToGlobalAlignment() throws Exception{
		initCloseMatcher();
		
//		vec.wordToVec(SOURCE_HOME + "/Source/TextesFrancais/RepertoireDicovek/R.txt", SOURCE_HOME + "/Source/TextesFrancais/RepertoireDicovek/WordToVek");
		System.out.println(dictionary);
		parcoursFichiers();
	}

	public void parcoursFichiers() throws Exception{
		File fileTest = new File(SEQUENCESFR);
		String[] directories = fileTest.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		Dicovek veks = new Dicovek(5, 5, Lexik.STOPLIST);

		veks.walk(DICOVEK);
		

		HashSet <String> setMotsTexte=new HashSet<String>();
		for (int compteurFichiers = 0; compteurFichiers < directories.length; compteurFichiers++) {
			int indexChant = compteurFichiers + 1;
			File dir=null;
			if (indexChant<10){
				dir = new File(SEQUENCESFR+"Chant0"+ indexChant);
			}
			else{
				dir = new File(SEQUENCESFR+"Chant"+ indexChant);
			}
			
			File[] filesParChant = dir.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().endsWith(".xml");
			    }
			});
			new LinkedList<List<NWRecord>>();
			
			for (File file : filesParChant) {
				if (!file.getName().contains("Odyssee")){
					Path path=null;
					if (indexChant<10){
						path = Paths.get( SEQUENCESFR+"Chant0" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".txt");
					}
					else{
						path = Paths.get( SEQUENCESFR+"Chant" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".txt");
					}
					String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
					Tokenizer toks = new Tokenizer(text);
					while( toks.read() ) {	
						setMotsTexte.add(toks.getString());
					}
				}
			}
		} 
//		setDistribDict(distribDictionary(veks, setMotsTexte));
		WordToVec vec=new WordToVec();
		setDistribDict(vec.wordToVec(setMotsTexte, W2V+"RepertoireW2V.txt", W2V+"WordToVek.txt"));

		CreateFiles.saveDistribDictToCSV(getDistribDict(), OUTPUTDICT);
		LinkedHashSet<List<NWRecord>>setDesRecords=new LinkedHashSet<List<NWRecord>>();
		for (int compteurFichiers = 0; compteurFichiers < directories.length; compteurFichiers++) {
			int indexChant = compteurFichiers + 1;
			File dir=null;
			if (indexChant<10){
				dir = new File(SEQUENCESFR+"Chant0"+ indexChant);
			}
			else{
				dir = new File(SEQUENCESFR+"Chant"+ indexChant);
			}
			
			File[] filesParChant = dir.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().endsWith(".xml");
			    }
			});
			for (File file : filesParChant) {
				ResourceCollection myTexts = ResourceCollection.newInstance();
				if (indexChant<10){
					myTexts.addText("sequencesFrancaises", SEQUENCESFR+"Chant0" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.addAtt("sequencesFrancaisesLemmes", "lemma", SEQUENCESFR+"Chant0" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.addAtt("sequencesFrancaisesTags", "tag", SEQUENCESFR+"Chant0" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.add("sequencesGrecques", PIVOT+"Chant0" + indexChant + "/" + "Sommer1886Chant0" + indexChant + "NomsCoupe.txt");
				}
				else{
					myTexts.addText("sequencesFrancaises", SEQUENCESFR+"Chant" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.addAtt("sequencesFrancaisesLemmes", "lemma", SEQUENCESFR+"Chant" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.addAtt("sequencesFrancaisesTags", "tag", SEQUENCESFR+"Chant" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.add("sequencesGrecques", PIVOT+"Chant" + indexChant + "/" + "Sommer1886Chant" + indexChant + "NomsCoupe.txt");
				}
				
				String nomFichier = StringUtils.substringBefore(file.getName(), "Noms");
				System.out.println("*********************************************************************************************");
				System.out.println("********************PREMIER ALIGNEMENT POUR LE FICHIER " + nomFichier + "************************");
				System.out.println("*********************************************************************************************");

				LinkedList<NWRecord> nouvelleListe = new LinkedList<NWRecord>();
				boolean greekOrNot=false;
				if (file.getName().contains("Odyssee1000")){
					greekOrNot=true;
				}
				nouvelleListe=firstAlignment(file, myTexts, getDictionary(), greekOrNot);

				setDesRecords.add(nouvelleListe);
				if (file.getName().contains("Odyssee1000")){
					CreateFiles.createXMLFromNWRecordListByBook(PUNCT+file.getName(), nouvelleListe, greekOrNot);
				}
				CreateFiles.createXMLFromNWRecordListByBook(OUTPUT+"xml/"+file.getName(), nouvelleListe, greekOrNot);
			}
		}
	}

	public LinkedList<NWRecord> firstAlignment(File file, ResourceCollection myTexts, HashMap<String, Set<String>> dictionary, boolean greekOrNot) throws Exception {
		NeedlemanWunsch nw= new NeedlemanWunsch();
		String fileName=file.getName().substring(0, file.getName().indexOf("NomsCoupe"));
		String numChant=fileName.substring(fileName.indexOf("Chant")+5);
		long startTime = System.currentTimeMillis();
		this.setGapPenality(0.0);
		setDictionary(dictionary);
		setDistribDict(getDistribDict());
		Path pathTrg=Paths.get(NAMESFR+fileName+".txt");
		Path pathSrc=Paths.get(NAMESFR+"Sommer1886Chant"+numChant+".txt");
		List<String> noms = Files.readAllLines(pathTrg, StandardCharsets.UTF_8);
		List<String> nomsGr = Files.readAllLines(pathSrc, StandardCharsets.UTF_8);
		
		setSrcFrequency(Frequency.frequency(nomsGr));
		setTrgFrequency(Frequency.frequency(noms));
		LinkedList<NWRecord> maListe=new LinkedList<NWRecord>();
		if (greekOrNot=true){
			maListe = nw.PerformAlignment(myTexts.getLines("sequencesGrecques"), myTexts.getLemmaLines("sequencesFrancaisesLemmes"),myTexts.getTextLines("sequencesFrancaises"),
					 myTexts.getTagLines("sequencesFrancaisesTags"), getDictionary(), getDistribDict(), getSrcFrequency(), getTrgFrequency());
		}
		else{
			maListe = nw.PerformAlignment(myTexts.getLines("sequencesGrecques"), myTexts.getTextLines("sequencesFrancaises"),
					myTexts.getLemmaLines("sequencesFrancaisesLemmes"), myTexts.getTagLines("sequencesFrancaisesTags"), getDictionary(), getDistribDict(), getSrcFrequency(),getTrgFrequency());
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("****************");
		System.out.println("PREMIER ALIGNEMENT : " + ((endTime - startTime) / 1000));
		System.out.println("****************");
		System.out.println("*********************************************************************************************");
		System.out.println("DEBUT DU REAGENCEMENT DES ALIGNEMENTS");
		System.out.println("*********************************************************************************************");
		LinkedList<NWRecord> nouvelleListe = new LinkedList<NWRecord>();
		nouvelleListe.addAll(maListe);
		for (int j = 0; j < maListe.size(); j++) {
			if (nouvelleListe.get(j).getSrc() == "^" && nouvelleListe.get(j).getTrg() == "^") {
				maListe.remove(nouvelleListe.get(j));
				nouvelleListe.remove(j);
				j--;
//				System.out.println("je réorganise en 1");
			} else if (nouvelleListe.get(j).getSrc().equals("^") && nouvelleListe.get(j).getTrg().equals("")) {
				maListe.remove(nouvelleListe.get(j));
				nouvelleListe.remove(j);
				j--;
//				System.out.println("je réorganise en 2");
			} else if (nouvelleListe.get(0).getSrc().equals("^") && nouvelleListe.get(0).getTrg().equals("") == false) {
				nouvelleListe.get(1).setTrg(maListe.get(0).getTrg() + " " + maListe.get(1).getTrg());
				nouvelleListe.get(1).setLemma(maListe.get(0).getLemma() + " " + maListe.get(1).getLemma());
				nouvelleListe.get(1).setTag(maListe.get(0).getTag() + " " + maListe.get(1).getTag());
				nouvelleListe.remove(0);
				maListe.remove(0);
				j--;
//				System.out.println("je réorganise en 3");
			} else if (j < nouvelleListe.size() && j > 0 && nouvelleListe.get(j).getSrc().equals("^") && nouvelleListe.get(j).getTrg().equals("") == false) {
				nouvelleListe.get(j - 1).setTrg(maListe.get(j - 1).getTrg() + " " + maListe.get(j).getTrg());
				nouvelleListe.get(j - 1).setLemma(maListe.get(j - 1).getLemma() + " " + maListe.get(j).getLemma());
				nouvelleListe.get(j - 1).setTag(maListe.get(j - 1).getTag() + " " + maListe.get(j).getTag());
				nouvelleListe.remove(j);
				maListe.remove(j);
				j--;
//				System.out.println("je réorganise en 4");
			}
		}
		LinkedList<NWRecord> lastList = new LinkedList<NWRecord>();
		lastList.addAll(nouvelleListe);
		for (int j = 1; j < lastList.size()-1; j++) {
			String motSourceActuel=lastList.get(j).getSrc().split(" ")[0];
			String motSourceIndicePlus=lastList.get(j+1).getSrc().split(" ")[0];
			String motSourceIndiceMoins=lastList.get(j-1).getSrc().split(" ")[0];
			String motTarget=lastList.get(j+1).getTrg().split(" ")[0];
			
			if(j < lastList.size()&& lastList.get(j).getTrg().equals("^")&&dictionary.containsKey(motSourceActuel)&&motSourceActuel.contains(motTarget)
					&&!motSourceIndicePlus.contains(motTarget)&&!motSourceIndiceMoins.contains(motTarget)){
				if (dictionary.get(motSourceActuel).contains(motTarget)||motSourceActuel.contains(motTarget)){
					System.out.println("je rentre dans la première condition avec "+motSourceActuel+ " à l'indice "+j);
					lastList.get(j).setTrg(lastList.get(j + 1).getTrg());
					lastList.get(j).setLemma(lastList.get(j + 1).getLemma());
					lastList.get(j).setTag(lastList.get(j + 1).getTag());
					lastList.get(j+1).setTrg("^");
					lastList.get(j+1).setLemma("^");
					lastList.get(j+1).setTag("^");
					j++;
				}
			}
			else {
				motTarget=lastList.get(j-1).getTrg().split(" ")[0];
				motSourceIndicePlus=lastList.get(j-1).getSrc().split(" ")[0];
				if(dictionary.containsKey(motSourceActuel)&& lastList.get(j).getTrg().equals("^")&&motSourceActuel.contains(motTarget)
						&&!motSourceIndicePlus.contains(motTarget)&&!motSourceIndiceMoins.contains(motTarget)){
					if (dictionary.get(motSourceActuel).contains(motTarget)||motSourceActuel.contains(motTarget)){
						System.out.println("je rentre dans la deuxième condition avec "+motSourceActuel+ " à l'indice "+j);
						lastList.get(j).setTrg(lastList.get(j - 1).getTrg());
						lastList.get(j).setLemma(lastList.get(j - 1).getLemma());
						lastList.get(j).setTag(lastList.get(j - 1).getTag());
						lastList.get(j-1).setTrg("^");
						lastList.get(j-1).setLemma("^");
						lastList.get(j-1).setTag("^");
//						j--;
					}
				}
			}
		}
		return lastList;
	}

	public HashMap<String, Set<String>> distribDictionary(Dicovek veks, HashSet<String>setMotsTexte) throws IOException{

		DecimalFormat df = new DecimalFormat("0.0000");
		HashMap<String, Set<String>> distribDictionary=new HashMap<String, Set<String>>();
		int limit = 3;
		List<SimRow> table;
		for (String mot:setMotsTexte) {
			Set<String> vecteursCorrespondants = new HashSet<String>();
			if (mot == null || "".equals(mot)) {
				System.exit(0);
			}
			table = veks.syns(mot);
			if ( table == null ) continue;
			for (SimRow row:table) {
				String vecteurUnique="";
				if (vecteursCorrespondants.size()<5){
					vecteurUnique=row.term;
					
					if (!row.term.equals(mot)){
						vecteursCorrespondants.add(vecteurUnique);
					}
				}
				if (--limit == 0 ) break;
			}
			if (!vecteursCorrespondants.isEmpty()){
				distribDictionary.put(mot, vecteursCorrespondants);
			}
		}
		setDistribDict(distribDictionary);
		return distribDictionary;
	}
	
	static String readFile(String path, Charset encoding) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	public static List<String> readLines(String fileName) throws Exception {
		List<String> list = new ArrayList<>();

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			list = stream
					.collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	

}