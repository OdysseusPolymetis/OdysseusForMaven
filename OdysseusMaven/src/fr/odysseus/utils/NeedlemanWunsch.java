package fr.odysseus.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.jdom2.Document;
import org.simmetrics.metrics.JaroWinkler;
import org.simmetrics.metrics.MongeElkan;

import fr.odysseus.utils.dictionary.GrPhoneTransformer;
import fr.odysseus.dataModels.NWRecord;



public class NeedlemanWunsch {

	public final String gapChar = "^";
	private double gapPenality;
	private GrPhoneTransformer phonetransformer;
	private HashMap<String, Set<String>> dictionary;
	public HashMap<String, Set<String>> distribDict;
	public HashMap<String, Integer> srcFrequency;
	public HashMap<String, Integer> trgFrequency;
	Document doc;

	public double getGapPenality() {
		return gapPenality;
	}

	public void setGapPenality(double gapPenality) {
		this.gapPenality = gapPenality;
	}

	public GrPhoneTransformer getPhonetransformer() {
		return phonetransformer;
	}

	public void setPhonetransformer(GrPhoneTransformer phonetransformer) {
		this.phonetransformer = phonetransformer;
	}

	public void setDictionary(HashMap<String, Set<String>> dictionary) {
		this.dictionary = dictionary;
	}

	public void setDistribDictionary(HashMap<String, Set<String>> distdictionary) {
		this.distribDict = distdictionary;
	}

	public void setSrcFrequency(HashMap<String, Integer> frequency) {
		this.srcFrequency = frequency;
	}
	public void setTrgFrequency(HashMap<String, Integer> frequency) {
		this.trgFrequency = frequency;
	}


	public LinkedList<NWRecord> PerformAlignment(String[] chunksSrc, String[] chunksTrg, String[] chunksLemmaTrg, String[] chunksTagTrg, 
			HashMap<String, Set<String>> dictionary,HashMap<String, Set<String>> distdictionary, HashMap<String, Integer>srcFrequency,
			HashMap<String, Integer>trgFrequency) {
		this.setDictionary(dictionary);
		this.setDistribDictionary(distdictionary);
		this.setSrcFrequency(srcFrequency);
		this.setTrgFrequency(trgFrequency);
		setGapPenality(-2.0);
		LinkedList<NWRecord> alignments = new LinkedList<NWRecord>();

		int n = chunksSrc.length;
		int m = chunksTrg.length;
		double[][] simMatrix = new double[n][m];
		double[][] pathMatrix = new double[n + 1][m + 1];

		fillSimMatrix(simMatrix, chunksSrc, chunksTrg);

		fillPathMatrix(pathMatrix, simMatrix, chunksSrc, chunksTrg);

		alignments = makeAlignment(simMatrix, pathMatrix, chunksSrc, chunksTrg, chunksLemmaTrg, chunksTagTrg);

		return alignments;
	}

	private LinkedList<NWRecord> makeAlignment(double[][] simMatrix,
			double[][] pathMatrix, String[] chunksSrc, String[] chunksTrg, String[] chunksLemmaTrg, String[] chunksTagTrg) {

		LinkedList<NWRecord> alignResults = new LinkedList<NWRecord>();
		LinkedList<String> tmpMatchSrc = new LinkedList<String>();
		LinkedList<String> tmpMatchTrg = new LinkedList<String>();
		LinkedList<Double> tmpMatchScore = new LinkedList<Double>();
		LinkedList<String> tmpMatchLemma = new LinkedList<String>();
		LinkedList<String> tmpMatchTag = new LinkedList<String>();
		int i = chunksSrc.length;
		int j = chunksTrg.length;

		while (i > 0 && j > 0) {

			double score = pathMatrix[i][j];
			double scoreDiagInv = pathMatrix[i - 1][j - 1];
			double scoreLeft = pathMatrix[i - 1][j];

			if (score == scoreDiagInv + simMatrix[i - 1][j - 1]) {

				tmpMatchSrc.add(chunksSrc[i - 1]);
				tmpMatchTrg.add(chunksTrg[j - 1]);
				tmpMatchScore.add(score);
				tmpMatchLemma.add(chunksLemmaTrg[j - 1]);
				tmpMatchTag.add(chunksTagTrg[j - 1]);

				i = i - 1;
				j = j - 1;

			} else if (score == scoreLeft + getGapPenality()) {
				tmpMatchSrc.add(chunksSrc[i - 1]);

				tmpMatchTrg.add(gapChar);
				tmpMatchLemma.add(gapChar);
				tmpMatchTag.add(gapChar);
				tmpMatchScore.add(score);

				i = i - 1;
			} else {

				tmpMatchSrc.add(gapChar);
				tmpMatchTrg.add(chunksTrg[j - 1]);
				tmpMatchLemma.add(chunksLemmaTrg[j - 1]);
				tmpMatchTag.add(chunksTagTrg[j - 1]);
				tmpMatchScore.add(score);
				j = j - 1;
			}
		}

		while (i > 0) {
			tmpMatchSrc.add(chunksSrc[i - 1]);
			tmpMatchTrg.add(gapChar);
			tmpMatchLemma.add(gapChar);
			tmpMatchTag.add(gapChar);
			tmpMatchScore.add(0.0);
			i = i - 1;
		}

		while (j > 0) {
			tmpMatchSrc.add(gapChar);
			tmpMatchTrg.add(chunksTrg[j - 1]);
			tmpMatchLemma.add(chunksLemmaTrg[j - 1]);
			tmpMatchTag.add(chunksTagTrg[j - 1]);
			tmpMatchScore.add(0.0);
			j = j - 1;
		}

		for (int k = tmpMatchTrg.size() - 1; k >= 0; k--) {
			NWRecord record = new NWRecord();
			record.setSrc(tmpMatchSrc.get(k));
			record.setTrg(tmpMatchTrg.get(k));
			record.setScore(tmpMatchScore.get(k));
			record.setLemma(tmpMatchLemma.get(k));
			record.setTag(tmpMatchTag.get(k));
			alignResults.add(record);
		}
		return alignResults;
	}

	private void fillPathMatrix(double[][] pathMatrix, double[][] simMatrix, String[] chunksSrc, String[] chunksTrg) {
		//      System.err.println("FILL PATH MATRIX");
		pathMatrix[0][0] = 0.0;

		for (int i = 1; i <= chunksSrc.length; i++) {
			pathMatrix[i][0] = i * getGapPenality();
		}
		for (int j = 1; j <= chunksTrg.length; j++) {
			pathMatrix[0][j] = j * getGapPenality();
		}
		for (int i = 1; i <= chunksSrc.length; i++) {
			for (int j = 1; j <= chunksTrg.length; j++) {
				double scoreDown = pathMatrix[i - 1][j] + getGapPenality();
				double scoreRight = pathMatrix[i][j - 1] + getGapPenality();
				double scoreDiag = pathMatrix[i - 1][j - 1] + simMatrix[i - 1][j - 1];
				double bestScore = Math.max(Math.max(scoreDown, scoreRight), scoreDiag);
				pathMatrix[i][j] = bestScore;
			}
		}
	}

	private void fillSimMatrix(double[][] simMatrix, String[] chunksSrg, String[] chunksTrg) {
		IntStream.range(0, chunksSrg.length).forEach(x
				-> IntStream.range(0, chunksTrg.length).forEach(y
						-> simMatrix[x][y] = simScore(chunksSrg[x], chunksTrg[y])));

	}

	/* Méthode de similarité modifiable, en fonction des dictionnaires, des fréquences, etc.*/
	private double simScore(String src, String trg) {
		src=src.replaceAll("[0-9]", "");
		trg=trg.replaceAll("[0-9]", ""); 

		double similarityScore = 0.0;
		if (!check(src)) {
			return similarityScore;
		}

		Set<String> possibleTradux = new HashSet<>();
		List<String> findTradux = new ArrayList<>();
		if (src.length()>0 && trg.length()>0) {
			String[] srcToks=null;
			String [] trgToks=null;
			if (src.contains(" ")&&!src.matches("\\s+")){
				srcToks=src.split("\\s");
			}
			else{
				srcToks=new String[1];
				srcToks[0]=src;
				
			}
			if (trg.contains(" ")&&!trg.matches("\\s+")){
				trgToks=trg.split("\\s");
			}
			else{
				trgToks=new String[1];
				trgToks[0]=trg;
			}
			
			if (Collections.disjoint(Arrays.asList(srcToks), dictionary.keySet())==false){
				List<String>tmp=new ArrayList<String>(Arrays.asList(srcToks));
				tmp.retainAll(dictionary.keySet());
				for (String tok : tmp) {
					Set<String>valuesToKey=dictionary.get(tok);
					for (String value:valuesToKey){
						possibleTradux.add(value);
					}
				}
			}
			
			if (srcToks[0].contains(trgToks[0])||trgToks[0].contains(srcToks[0])){
				if (!dictionary.containsKey(srcToks[0])&&dictionary.containsKey(trgToks[0])){
					possibleTradux.add(srcToks[0]);
					possibleTradux.add(trgToks[0]);
				}
				else if (dictionary.containsKey(srcToks[0])){
					dictionary.get(srcToks[0]).add(trgToks[0]);
				}
				else if (dictionary.containsKey(trgToks[0])){
					dictionary.get(trgToks[0]).add(srcToks[0]);
				}
			}

			if (!distribDict.equals(null)||!distribDict.isEmpty()) {
				if (Collections.disjoint(Arrays.asList(srcToks), distribDict.keySet())==false){
					List<String>tmp=new ArrayList<String>(Arrays.asList(srcToks));
					tmp.retainAll(distribDict.keySet());
					for (String tok : tmp) {
						Set<String>valuesToKey=distribDict.get(tok);
						for (String value:valuesToKey){
							possibleTradux.add(value);
						}
					}
				}
			}

			if (Math.abs(srcToks.length-trgToks.length)<5){
				similarityScore+=0.5;
			}

			
			
			
			if (srcToks.length>0&&trgToks.length>0){
				
				if (srcFrequency.keySet().contains(srcToks[0])&&trgFrequency.keySet().contains(trgToks[0])
						&&(dictionary.containsKey(srcToks[0])|dictionary.containsKey(trgToks[0]))){
					if (srcFrequency.get(srcToks[0])<10&&trgFrequency.get(trgToks[0])<10){
						if (dictionary.containsKey(srcToks[0])){
							
							if (dictionary.get(srcToks[0]).contains(trgToks[0])){
								similarityScore+=2;
							}
						}
						else if (dictionary.containsKey(trgToks[0])){
							if (dictionary.get(trgToks[0]).contains(srcToks[0])){
								similarityScore+=2;
							}
						}
					}
				}
			}

			MongeElkan monge=new MongeElkan(new JaroWinkler());
			float simMonge=monge.compare(new ArrayList<String>(Arrays.asList(srcToks)), new ArrayList<String>(Arrays.asList(trgToks)));
			if (simMonge>0.8){
				similarityScore+=2;
			}

			for (String trad : possibleTradux) {
				if (trg.contains(trad)&&!"".equals(trg)) {
					findTradux.add(trad);
					similarityScore += 1.0;
				}
			}
		}
		
		return similarityScore;
	}
	private boolean check(String src) {
		return !("".equals(src));
	}

	public static String stripAccents(Set<String> string) {
		String s = string.toString();
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return s;
	}
}
