package fr.odysseus.utils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;


public class WordToVec {
	
	@SuppressWarnings("serial")
	public HashMap<String, Set<String>> wordToVec(HashSet <String>wordsToChek,String pathIn, String pathOut) throws Exception{
		HashMap<String, Set<String>> distribDict=new HashMap<String, Set<String>>();
//		String filePath = new ClassPathResource("raw_sentences.txt").getFile().getAbsolutePath();
		SentenceIterator iter = new LineSentenceIterator(new File(pathIn));
        iter.setPreProcessor(new SentencePreProcessor() {
            @Override
            public String preProcess(String sentence) {
                return sentence.toLowerCase();
            }
        });
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();
        vec.fit();
        WordVectorSerializer.writeWordVectors(vec, pathOut);
        for (String word:wordsToChek){
        	HashSet <String> siminyms=new HashSet<String>(vec.wordsNearest(word, 3));
        	distribDict.put(word, siminyms);
        }
		return distribDict;
	}
	
}
