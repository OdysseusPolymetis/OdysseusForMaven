package fr.odysseus.utils.dictionary;
import java.io.File;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
//import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.store.NIOFSDirectory;
/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class LuceneIndexMaker {
	//final static File fileDict=new File("/home/federico/shit/grc.dic");
	//final static File dirDict=new File("/home/federico/shit/lucene_index_grc");
	final static File fileDict=new File("./input/names/frname/FrenchNames.txt");
	final static File dirDict=new File("./lucene_index_fr");
	IndexWriterConfig iwc;
	SpellChecker spellchecker;
	public LuceneIndexMaker() throws Exception{
		init();
	}
	public void init() throws Exception{
		NIOFSDirectory spellIndexDirectory =new NIOFSDirectory(dirDict.toPath());
		spellchecker = new SpellChecker(spellIndexDirectory);
		iwc=new IndexWriterConfig(new WhitespaceAnalyzer());
	}
	public void makeIndex() throws Exception{
		spellchecker.indexDictionary(new PlainTextDictionary(fileDict.toPath()),iwc,true);
	}
	public static void main(String args[]) throws Exception{
		LuceneIndexMaker luceneIndexMaker=new LuceneIndexMaker();
		luceneIndexMaker.makeIndex();
	}
}