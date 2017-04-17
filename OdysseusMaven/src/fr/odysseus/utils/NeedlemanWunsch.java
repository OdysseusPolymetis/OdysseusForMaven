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

import alix.fr.Occ;
import alix.fr.Tokenizer;
import fr.odysseus.utils.dictionary.GrPhoneTransformer;
import fr.odysseus.dataModels.NWRecord;



public class NeedlemanWunsch {

	public final String gapChar = "^";
	private double gapPenality;
	private GrPhoneTransformer phonetransformer;
	private HashMap<String, Set<String>> dictionary;
	public HashMap<String, HashSet<String>> grFrDict;
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
	
	public void setGrFr(HashMap<String, HashSet<String>> dictionary) {
		this.grFrDict = dictionary;
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


	public LinkedList<NWRecord> PerformAlignment(String[] chunksLemmaSrc, String[] chunksLemTrg, String[] chunksTxtTrg, String[] chunksTagTrg, 
			HashMap<String, Set<String>> dictionary,HashMap<String, Set<String>> distdictionary, HashMap<String, Integer>srcFrequency,
			HashMap<String, Integer>trgFrequency) {
		this.setDictionary(dictionary);
		this.setDistribDictionary(distdictionary);
		this.setSrcFrequency(srcFrequency);
		this.setTrgFrequency(trgFrequency);
		setGapPenality(-2.0);
		LinkedList<NWRecord> alignments = new LinkedList<NWRecord>();

		int n = chunksLemmaSrc.length;
		int m = chunksLemTrg.length;
		double[][] simMatrix = new double[n][m];
		double[][] pathMatrix = new double[n + 1][m + 1];

		fillSimMatrix(simMatrix, chunksLemmaSrc, chunksLemTrg);

		fillPathMatrix(pathMatrix, simMatrix, chunksLemmaSrc, chunksLemTrg);

		alignments = makeAlignment(simMatrix, pathMatrix, chunksLemmaSrc, chunksLemTrg, chunksTxtTrg, chunksTagTrg);

		return alignments;
	}

	private LinkedList<NWRecord> makeAlignment(double[][] simMatrix,
			double[][] pathMatrix, String[] chunksLemSrc, String[] chunksLemTrg, String[] chunksTxtTrg, String[] chunksTagTrg) {

		LinkedList<NWRecord> alignResults = new LinkedList<NWRecord>();
		LinkedList<String> tmpMatchSrc = new LinkedList<String>();
		LinkedList<String> tmpMatchLemTrg = new LinkedList<String>();
		LinkedList<Double> tmpMatchScore = new LinkedList<Double>();
		LinkedList<String> tmpMatchTxtTrg = new LinkedList<String>();
		LinkedList<String> tmpMatchTag = new LinkedList<String>();
		int i = chunksLemSrc.length;
		int j = chunksLemTrg.length;

		while (i > 0 && j > 0) {

			double score = pathMatrix[i][j];
			double scoreDiagInv = pathMatrix[i - 1][j - 1];
			double scoreLeft = pathMatrix[i - 1][j];

			if (score == scoreDiagInv + simMatrix[i - 1][j - 1]) {

				tmpMatchSrc.add(chunksLemSrc[i - 1]);
				tmpMatchLemTrg.add(chunksLemTrg[j - 1]);
				tmpMatchScore.add(score);
				tmpMatchTxtTrg.add(chunksTxtTrg[j - 1]);
				tmpMatchTag.add(chunksTagTrg[j - 1]);

				i = i - 1;
				j = j - 1;

			} else if (score == scoreLeft + getGapPenality()) {
				tmpMatchSrc.add(chunksLemSrc[i - 1]);

				tmpMatchLemTrg.add(gapChar);
				tmpMatchTxtTrg.add(gapChar);
				tmpMatchTag.add(gapChar);
				tmpMatchScore.add(score);

				i = i - 1;
			} else {

				tmpMatchSrc.add(gapChar);
				tmpMatchLemTrg.add(chunksLemTrg[j - 1]);
				tmpMatchTxtTrg.add(chunksTxtTrg[j - 1]);
				tmpMatchTag.add(chunksTagTrg[j - 1]);
				tmpMatchScore.add(score);
				j = j - 1;
			}
		}

		while (i > 0) {
			tmpMatchSrc.add(chunksLemSrc[i - 1]);
			tmpMatchLemTrg.add(gapChar);
			tmpMatchTxtTrg.add(gapChar);
			tmpMatchTag.add(gapChar);
			tmpMatchScore.add(0.0);
			i = i - 1;
		}

		while (j > 0) {
			tmpMatchSrc.add(gapChar);
			tmpMatchLemTrg.add(chunksLemTrg[j - 1]);
			tmpMatchTxtTrg.add(chunksTxtTrg[j - 1]);
			tmpMatchTag.add(chunksTagTrg[j - 1]);
			tmpMatchScore.add(0.0);
			j = j - 1;
		}

		for (int k = tmpMatchLemTrg.size() - 1; k >= 0; k--) {
			NWRecord record = new NWRecord();
			record.setSrc(tmpMatchSrc.get(k));
			record.setLemma(tmpMatchLemTrg.get(k));
			record.setScore(tmpMatchScore.get(k));
			record.setTrg(tmpMatchTxtTrg.get(k));
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

		HashSet<String> possibleTradux = new HashSet<>();
		if (src.length()>0 && trg.length()>0) {
			List<String> srcToks=new ArrayList<String>();
			List<String> trgToks=new ArrayList<String>();

			Tokenizer toks = new Tokenizer(src);
			Occ occ=new Occ();

			while ( toks.token(occ) ) {
				srcToks.add(occ.orth().toString());
			}

			Tokenizer toksTrg = new Tokenizer(trg);
			Occ occTrg=new Occ();

			while ( toksTrg.token(occTrg) ) {
				trgToks.add(occTrg.orth().toString());
			}

			if (!trgToks.isEmpty()&&!srcToks.isEmpty()){
				String trgFirst=trgToks.get(0);
				String srcFirst=srcToks.get(0);

				if (Collections.disjoint(srcToks, dictionary.keySet())==false){
					List<String>tmp=new ArrayList<String>(srcToks);
					tmp.retainAll(dictionary.keySet());
					for (String tok : tmp) {
						Set<String>valuesToKey=dictionary.get(tok);
						for (String value:valuesToKey){
							possibleTradux.add(value);
						}
					}
				}

//				if (srcFirst.contains(trgFirst)||trgFirst.contains(srcFirst)){
//					if (!dictionary.containsKey(srcFirst)&&dictionary.containsKey(trgFirst)){
//						possibleTradux.add(srcFirst);
//						possibleTradux.add(trgFirst);
//					}
//					else if (dictionary.containsKey(srcFirst)){
//						dictionary.get(srcFirst).add(trgFirst);
//					}
//					else if (dictionary.containsKey(trgFirst)){
//						dictionary.get(trgFirst).add(srcFirst);
//					}
//				}

				if (distribDict!=null) {
					if (!distribDict.isEmpty()){
						if (Collections.disjoint(Arrays.asList(srcToks), distribDict.keySet())==false){
							List<String>tmp=new ArrayList<String>(srcToks);
							tmp.retainAll(distribDict.keySet());
							for (String tok : tmp) {
								Set<String>valuesToKey=distribDict.get(tok);
								for (String value:valuesToKey){
									possibleTradux.add(value);
								}
							}
						}
					}
				}
				if (grFrDict!=null) {
					if (!grFrDict.isEmpty()){
						if (Collections.disjoint(Arrays.asList(srcToks), grFrDict.keySet())==false){
							List<String>tmp=new ArrayList<String>(srcToks);
							tmp.retainAll(grFrDict.keySet());
							for (String tok : tmp) {
								Set<String>valuesToKey=grFrDict.get(tok);
								for (String value:valuesToKey){
									possibleTradux.add(value);
								}
							}
						}
					}
				}

				if (Math.abs(srcToks.size()-trgToks.size())<5){
					similarityScore+=1;
				}




				if (srcToks.size()>0&&trgToks.size()>0){

					if (srcFrequency.keySet().contains(srcFirst)&&trgFrequency.keySet().contains(trgFirst)
							&&(dictionary.containsKey(srcFirst)|dictionary.containsKey(trgFirst))){
						if (srcFrequency.get(srcFirst)<10&&trgFrequency.get(trgFirst)<10){
							if (dictionary.containsKey(srcFirst)){

								if (dictionary.get(srcFirst).contains(trgFirst)){
									similarityScore+=2;
								}
							}
							else if (dictionary.containsKey(trgFirst)){
								if (dictionary.get(trgFirst).contains(srcFirst)){
									similarityScore+=2;
								}
							}
						}
					}
				}

				MongeElkan monge=new MongeElkan(new JaroWinkler());
				float simMonge=monge.compare(srcToks, trgToks);
				if (simMonge>0.8){
					similarityScore+=2;
				}
				
				for (String trad : possibleTradux) {
					if (trg.contains(trad)&&!"".equals(trg)) {
						similarityScore += 1.0;
					}
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
