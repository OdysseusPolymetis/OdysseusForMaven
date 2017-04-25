package fr.odysseus.utils;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Frequency {
	public HashMap<String, Integer> setFrequency(String tabTestWord[], String texteGrec){
		HashMap<String, Integer> mapFrequency=new HashMap<String, Integer>();
		for(String str:tabTestWord){
			//		String mot = str.substring(0,3);
			Matcher m = Pattern.compile(str)
					.matcher(texteGrec);
			Integer freq=0;
			while (m.find()) {

				if(freq == null) freq = 1; else freq ++;
				mapFrequency.put(m.group(), freq);

			}}


		return mapFrequency;
	}

	public static HashMap<String, Integer> frequency(List<String> noms){
		HashMap<String, Integer> frequencyMap=new HashMap<String, Integer>();
		for (String nom:noms){
			
			if (frequencyMap.containsKey(nom)){
				frequencyMap.put(nom, frequencyMap.get(nom) + 1);
			} else {
				frequencyMap.put(nom, 1);
			}
		}
		
		return frequencyMap;
	}

}