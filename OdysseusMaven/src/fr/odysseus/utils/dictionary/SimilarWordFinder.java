package fr.odysseus.utils.dictionary;


import java.io.File;

import java.util.Arrays;
import java.util.HashSet;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.SpellChecker;

import org.apache.lucene.store.NIOFSDirectory;
/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class SimilarWordFinder {
	//final static String testWord="καταβάλλω";
	//final NIOFSDirectory spellIndexDirectory=new NIOFSDirectory(new File("/home/federico/shit/lucene_index_grc"));
	final static String testWord="Calypso";
	
	final NIOFSDirectory spellIndexDirectory=new NIOFSDirectory(new File("./lucene_index_fr").toPath());
	
	SpellChecker spellChecker;
	IndexReader indexReader;
	public SimilarWordFinder() throws Exception{
		init();
	}
	public void init() throws Exception{
		indexReader=DirectoryReader.open(spellIndexDirectory);
//		indexReader=IndexReader.open(spellIndexDirectory);
		spellChecker = new SpellChecker(spellIndexDirectory);
	}
	public void printSimilarWords(String word) throws Exception{
		String[] ress;
		ress=spellChecker.suggestSimilar(word,3,0.6f);
		for(String res:ress){
			System.out.println(res);
		}
	}
	public HashSet<String> getSimilarWords(String srcWord) throws Exception{
//		System.err.println("getSimilarWords");
		HashSet<String> similarWords = new HashSet<String>();
		String[] resultWorlds = spellChecker.suggestSimilar(srcWord, 2, 0.5f);
		similarWords.addAll(Arrays.asList(resultWorlds));
		return similarWords;
	}
	
//	public static void main(String[] args) throws Exception{
//		
//		
//		
//	}
}