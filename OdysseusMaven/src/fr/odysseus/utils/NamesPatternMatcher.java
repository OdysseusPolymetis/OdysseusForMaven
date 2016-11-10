package fr.odysseus.utils;


import java.util.List;
import java.util.ArrayList;

import java.util.regex.*;

public class NamesPatternMatcher{
	private String pattern;
	private CharSequence content;
	private CharSequence[] names;
	
	public static final String DEF_PATTERN = "(___NAMEi___.+?)"+"(___NAMEiplus1___)";
	public static final String DEF_CONTENT = "Domani Angelo va a casa con Marianne. Così Marianne è contenta e Angelo pure.";
	public static final String NAMEi = "Angelo";
	public static final String NAMEiplus1 = "Marianne";
	public static final String [] DEF_NAMES;
	
	static {
		
			DEF_NAMES =  new String[] {NamesPatternMatcher.NAMEi, NamesPatternMatcher.NAMEiplus1, NamesPatternMatcher.NAMEiplus1, NamesPatternMatcher.NAMEi };
		
	}
	
	public NamesPatternMatcher(){
		this(NamesPatternMatcher.DEF_PATTERN, NamesPatternMatcher.DEF_CONTENT, NamesPatternMatcher.DEF_NAMES);
	}
	
	public NamesPatternMatcher(String pattern, CharSequence content, CharSequence[] names){
		
		this.pattern = pattern;
		this. content = content;
		this. names = names;
	
	}
	
	public List<CharSequence> getSequences(){
		List<CharSequence> sequences = new ArrayList<CharSequence>();
		/**
		* text treatment before the first occurrence of a name
		*/
		CharSequence first = 
			this.content
				.subSequence(0,
					this.content.toString().indexOf(this.names[0].toString()));
					
		sequences.add(first);
		
//		this.content = StringUtils.replaceOnce(this.content.toString(), first.toString(), "");
		this.content = this.content.toString().replaceFirst("\\Q"+first.toString()+"\\E","");
		/**
		* last part of treatment of the tested part before the first occurrence of a name
		*/
		
		this.names[0] = escape(this.names[0]);
		for(int i=0; i<names.length-1;i++){
			String pTmp = this.pattern;
		
			if(pattern.equals(NamesPatternMatcher.DEF_PATTERN)){
				
				this.names[i+1] = escape(this.names[i+1]);
				 
				pTmp = this.pattern
							 .replaceFirst("___NAMEi___", this.names[i].toString())
							 .replaceFirst("___NAMEiplus1___", this.names[i+1].toString());		
			}
			
			Pattern p = Pattern.compile(pTmp);
			Matcher m = p.matcher(this.content);
			if(m.find()){
				String matched = m.group(1);
				
				sequences.add(matched);
//				this.content = StringUtils.replaceOnce(this.content.toString(), matched, "");
				this.content = this.content.toString().replaceFirst("\\Q"+matched+"\\E","");
//				System.out.println(this.content);

			}
		}
		
	sequences.add(this.content); 
	return sequences;
	}

	private CharSequence escape(CharSequence charSequence) {
		String ret = charSequence.toString();
		
		if( ret.contains("\\") || ret.contains("(") || ret.contains(")") || ret.contains("[") || ret.contains("]") || ret.contains("*") || ret.contains("/") ){
			
			ret = ret.replace("\\", "\\\\\\\\");
			ret = ret.replace("(", "\\\\(");
			ret = ret.replace(")", "\\\\)");
			ret = ret.replace("[", "\\\\[");
			ret = ret.replace("]", "\\\\]");
			ret = ret.replace("*", "\\\\*");
			ret = ret.replace("/", "\\/");
			
		}
		return ret;
	}  
}