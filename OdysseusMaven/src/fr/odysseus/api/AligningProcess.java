package fr.odysseus.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import alix.fr.Occ;
import alix.fr.Tokenizer;
import fr.odysseus.dataModels.ResourceCollection;
import fr.odysseus.dataModels.NWRecord;
import fr.odysseus.utils.Aligner;
import fr.odysseus.utils.CreateFiles;
import fr.odysseus.utils.WordToVec;
import fr.odysseus.utils.dictionary.CloseMatcher;

public class AligningProcess{

	public static final String SOURCE_HOME=".";
	static final String SEQUENCESFR="./sourceFiles/sequences/frenchSequences/";
	static final String SEQUENCESGR="./sourceFiles/sequences/greekSequences/";
	static final String SEQUENCESLAT="./sourceFiles/sequences/latinSequences/";
	static final String PUNCT="./sourceFiles/sequences/greekPunct/";
	static final String PIVOT="./sourceFiles/sequences/pivot/";
	static final String NAMESFR="./sourceFiles/names/frenchNames/";
	static final String NAMESGR="./sourceFiles/names/greekNames/";
	static final String DICOVEK="./sourceFiles/sourceDictionaries/repertoireDicovek/";
	static final String W2V="./sourceFiles/sourceDictionaries/word2Vec/";
	static final String GRDICT="./sourceFiles/sourceDictionaries/dict.tsv";
	static final String OUTPUTDICT="./outputFiles/dictionaries/word2Vec/";
	static final String OUTPUT="./outputFiles/";
	CloseMatcher close;
	private double gapPenality;
	private HashMap<String, Set<String>> dictionary;
	public HashMap<String, HashSet<String>>grFrDict;
	public HashMap<String, Set<String>> distribDict;
	public HashMap<String, Integer> srcFrequency;
	public HashMap<String, Integer> trgFrequency;

	public void setDictionary(HashMap<String, Set<String>> dictionary) {
		this.dictionary = dictionary;
	}

	public HashMap<String, Set<String>> getDictionary() {
		return dictionary;
	}
	
	public void setGrFr(HashMap<String, HashSet<String>> dictionary) {
		this.grFrDict = dictionary;
	}

	public HashMap<String, HashSet<String>> getGrFr() {
		return grFrDict;
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

	public void initCloseMatcher() throws IOException{
		close = new CloseMatcher();
		try {
			setDictionary(close.getDictionary());
		} catch (Exception e) {
			e.printStackTrace();
		}
		BufferedReader in = new BufferedReader(new FileReader(GRDICT));
        String line;
        grFrDict=new HashMap<String, HashSet<String>>();
        while ((line = in.readLine()) != null) {
            String columns[] = line.split("\t");
            
            if (!grFrDict.containsKey(columns[1])) {
            	HashSet<String>tmpSet=new HashSet<String>();
            	tmpSet.add(columns[0]);
                grFrDict.put(columns[1], tmpSet);
            }
            else{
            	HashSet<String>tmpSet=grFrDict.get(columns[1]);
            	tmpSet.add(columns[0]);
            	grFrDict.put(columns[1], tmpSet);
            }

        }
        in.close();
	}

	public void proceedToGlobalAlignment() throws Exception{
		initCloseMatcher();
		
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
		

		HashSet <String> setMotsTexte=new HashSet<String>();
		for (int compteurFichiers = 0; compteurFichiers < directories.length; compteurFichiers++) {
//			int indexChant = compteurFichiers + 1;
			String indexChant="";
			if (compteurFichiers<9){
				indexChant="0"+(compteurFichiers+1);
			}
			else{
				indexChant=""+(compteurFichiers+1);
			}
			File dir = new File(SEQUENCESFR+"Chant"+ indexChant);
			
			
			File[] filesParChant = dir.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().endsWith(".xml");
			    }
			});
			
			for (File file : filesParChant) {
				if (!file.getName().contains("Odyssee")){
					Path path = Paths.get( SEQUENCESFR+"Chant" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".txt");
					
					String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
					
					Tokenizer toks = new Tokenizer(text);
					Occ occ=new Occ();
					while ( toks.token(occ) ) {
						setMotsTexte.add(occ.orth().toString());
					}
				}
			}
		} 

		
		WordToVec vec=new WordToVec();
		setDistribDict(vec.wordToVec(setMotsTexte, W2V+"RepertoireW2V.txt", W2V+"WordToVek.txt"));

		CreateFiles.saveDistribDictToCSV(getDistribDict(), OUTPUTDICT);
		for (int compteurFichiers = 0; compteurFichiers < directories.length; compteurFichiers++) {
			String indexChant="";
			if (compteurFichiers<9){
				indexChant="0"+(compteurFichiers+1);
			}
			else{
				indexChant=""+(compteurFichiers+1);
			}
			File dir = new File(SEQUENCESFR+"Chant"+ indexChant);
			
			File[] filesParChant = dir.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().endsWith(".xml");
			    }
			});
			boolean doneOnce=false;
			for (File file : filesParChant) {
				ResourceCollection myTexts = ResourceCollection.newInstance();
				
					myTexts.addText("sequencesFrancaises", SEQUENCESFR+"Chant" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.addText("sequencesLatines", SEQUENCESLAT+"Chant" + indexChant + "/" + "OdysseeLat1000Chant"+ indexChant + "NomsCoupe.xml");
					myTexts.addAtt("sequencesFrancaisesLemmes", "lemma", SEQUENCESFR+"Chant" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.addAtt("sequencesLatinesLemmes", "lemma", SEQUENCESLAT+"Chant" + indexChant + "/" + "OdysseeLat1000Chant"+ indexChant + "NomsCoupe.xml");
					myTexts.addAtt("sequencesFrancaisesTags", "tag", SEQUENCESFR+"Chant" + indexChant + "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
					myTexts.addAtt("sequencesLatinesTags", "tag", SEQUENCESLAT+"Chant" + indexChant + "/" + "OdysseeLat1000Chant"+ indexChant + "NomsCoupe.xml");
					myTexts.addText("sequencesGrecques", SEQUENCESGR+"Chant" + indexChant + "/" +"Odyssee1000Chant"+ indexChant + "NomsCoupe.xml");
					myTexts.addAtt("sequencesGrecquesLemmes", "lemma", SEQUENCESGR+"Chant" + indexChant + "/" +"Odyssee1000Chant"+ indexChant + "NomsCoupe.xml");
					myTexts.addAtt("sequencesGrecquesTags", "tag", SEQUENCESGR+"Chant" + indexChant + "/" +"Odyssee1000Chant"+ indexChant + "NomsCoupe.xml");
					myTexts.addAtt("sequencesPivotLemmes","lemma", PIVOT+"Chant" + indexChant + "/" + "Sommer1886Chant" + indexChant + "NomsCoupe.xml");
					myTexts.addText("sequencesPivot", PIVOT+"Chant" + indexChant + "/" + "Sommer1886Chant" + indexChant + "NomsCoupe.xml");
				
				
				String nomFichier = StringUtils.substringBefore(file.getName(), "Noms");
				System.out.println("*********************************************************************************************");
				System.out.println("********************PREMIER ALIGNEMENT POUR LE FICHIER " + nomFichier + "************************");
				System.out.println("*********************************************************************************************");

				LinkedList<NWRecord> nouvelleListe = new LinkedList<NWRecord>();
				LinkedList<NWRecord> nouvelleListeGrecque = new LinkedList<NWRecord>();
				
				Aligner aligner=new Aligner();
				aligner.setDistribDict(getDistribDict());
				aligner.setDictionary(getDictionary());
				aligner.setTrgFrequency(getTrgFrequency());
				
				String fileBaseName=file.getName().substring(0, file.getName().indexOf("NomsCoupe"));
				nouvelleListe=aligner.alignment(fileBaseName,NAMESFR, myTexts, getDictionary(), "sequencesFrancaises");
				File directory = new File(OUTPUT+"xml/Chant"+indexChant+"/");
				if (!directory.exists()){
					directory.mkdirs();
				}
				CreateFiles.createXMLFromNWRecordListByBook(OUTPUT+"xml/Chant"+indexChant+"/"+file.getName(), nouvelleListe);
				if (doneOnce==false){
					aligner.setGrFr(grFrDict);
					nouvelleListeGrecque=aligner.alignment("Odyssee1000Chant"+indexChant,NAMESGR, myTexts, getDictionary(), "sequencesGrecques");
					CreateFiles.createXMLFromNWRecordListByBook(PUNCT+"Odyssee1000Chant"+indexChant+".xml", nouvelleListeGrecque);	
				}
				doneOnce=true;
			}
		}
	}

}