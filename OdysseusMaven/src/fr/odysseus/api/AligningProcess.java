package fr.odysseus.api;

import java.io.BufferedReader;
import java.io.File;
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
		BufferedReader in = new BufferedReader(new FileReader(Console.GRDICT));
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
		File fileTest = new File(Console.SEQUENCESFR);
		String[] directories = fileTest.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});


		HashSet <String> setMotsTexte=new HashSet<String>();
		//		for (int compteurFichiers = 0; compteurFichiers < directories.length; compteurFichiers++) {
		for (String book:directories){
			//			int indexChant = compteurFichiers + 1;
			//			String indexChant=fileName.substring(fileName.indexOf("chant")+5);
			//			if (compteurFichiers<9){
			//				indexChant="0"+(compteurFichiers+1);
			//			}
			//			else{
			//				indexChant=""+(compteurFichiers+1);
			//			}
			//			File dir = new File(Console.SEQUENCESFR+"chant"+ indexChant);
			File dir = new File(Console.SEQUENCESFR+book);

			File[] filesParChant = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xml");
				}
			});

			for (File file : filesParChant) {
				if (!file.getName().contains("odyssee")){
					Path path = Paths.get( Console.SEQUENCESFR+book+ "/" + FilenameUtils.getBaseName(file.getName()) + ".txt");

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
		setDistribDict(vec.wordToVec(setMotsTexte, Console.W2V+"RepertoireW2V.txt", Console.W2V+"WordToVek.txt"));

		CreateFiles.saveDistribDictToCSV(getDistribDict(), Console.OUTPUTDICT);
		for (String book:directories){
			String indexChant=book.substring(book.indexOf("chant")+5);
			//			String indexChant="";
			//			if (compteurFichiers<9){
			//				indexChant="0"+(compteurFichiers+1);
			//			}
			//			else{
			//				indexChant=""+(compteurFichiers+1);
			//			}
			//			File dir = new File(Console.SEQUENCESFR+"chant"+ indexChant);
			File dir = new File(Console.SEQUENCESFR+book);
			File[] filesParChant = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xml");
				}
			});
			boolean doneOnce=false;
			for (File file : filesParChant) {
				ResourceCollection myTexts = ResourceCollection.newInstance();

				myTexts.addText("seqFr", Console.SEQUENCESFR+book+ "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
				myTexts.addText("seqLat", Console.SEQUENCESLAT+book+ "/" + "odysseelat1000_"+ indexChant + ".xml");
				myTexts.addAtt("seqFrLem", "lemma", Console.SEQUENCESFR+book+ "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
				myTexts.addAtt("seqLatLem", "lemma", Console.SEQUENCESLAT+book+ "/" + "odysseelat1000_"+ indexChant + ".xml");
				myTexts.addAtt("seqFrTag", "tag", Console.SEQUENCESFR+book+ "/" + FilenameUtils.getBaseName(file.getName()) + ".xml");
				myTexts.addAtt("seqLatTag", "tag", Console.SEQUENCESLAT+book+ "/" + "odysseelat1000_"+ indexChant + ".xml");
				myTexts.addText("seqGr", Console.SEQUENCESGR+book+ "/" +"odyssee1000_"+ indexChant + ".xml");
				myTexts.addAtt("seqGrLem", "lemma", Console.SEQUENCESGR+book+ "/" +"odyssee1000_"+ indexChant + ".xml");
				myTexts.addAtt("seqGrTag", "tag", Console.SEQUENCESGR+book+ "/" +"odyssee1000_"+ indexChant + ".xml");
				myTexts.addAtt("seqPiLem","lemma", Console.PIVOT+book+ "/" + "sommer1886_" + indexChant + ".xml");
				myTexts.addText("seqPi", Console.PIVOT+book+ "/" + "sommer1886_" + indexChant + ".xml");


				String nomFichier = StringUtils.substringBefore(file.getName(), ".xml");
				System.out.println("*********************************************************************************************");
				System.out.println("********************PREMIER ALIGNEMENT POUR LE FICHIER " + nomFichier + "************************");
				System.out.println("*********************************************************************************************");

				LinkedList<NWRecord> listfr = new LinkedList<NWRecord>();
				LinkedList<NWRecord> listgr = new LinkedList<NWRecord>();
				LinkedList<NWRecord> listlat = new LinkedList<NWRecord>();

				Aligner aligner=new Aligner();
				aligner.setDistribDict(getDistribDict());
				aligner.setDictionary(getDictionary());
				aligner.setTrgFrequency(getTrgFrequency());

//				if (nomFichier.contains("odysseelat")){ /*activer cette condition si seulement un alignement souhaité*/
					listfr=aligner.alignment(nomFichier,Console.NAMESFR, myTexts, getDictionary(), "seqFr");
					File directory = new File(Console.OUTPUT+"xml/chant"+indexChant+"/");
					if (!directory.exists()){
						directory.mkdirs();
					}
					CreateFiles.createXMLFromNWRecordListByBook(Console.OUTPUT+"xml/chant"+indexChant+"/"+file.getName(), listfr);
					if (doneOnce==false){
						aligner.setGrFr(grFrDict);
						listgr=aligner.alignment("odyssee1000_"+indexChant,Console.NAMESGR, myTexts, getDictionary(), "seqGr");
						CreateFiles.createXMLFromNWRecordListByBook(Console.PUNCT+"odyssee1000_"+indexChant+".xml", listgr);
						CreateFiles.createXMLFromNWRecordListByBook(directory+"/odyssee1000_"+indexChant+".xml", listgr);
						listlat=aligner.alignment("odysseelat1000_"+indexChant,Console.NAMESLAT, myTexts, getDictionary(), "seqLat");
						CreateFiles.createXMLFromNWRecordListByBook(directory+"/odysseelat1000_"+indexChant+".xml", listlat);
						
					}
					doneOnce=true;
//				} /*fin de la condition d'un alignement*/
				
			}
		}
	}
}