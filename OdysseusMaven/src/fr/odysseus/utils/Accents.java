package fr.odysseus.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class Accents {
	public static String removeDiacriticalMarks(String string) {
		return Normalizer.normalize(string, Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
}
