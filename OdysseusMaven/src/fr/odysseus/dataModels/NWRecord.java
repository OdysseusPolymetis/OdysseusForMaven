/**
 *
 */
package fr.odysseus.dataModels;
/**
 * @author Angelo Del Grosso
 *
 */
public class NWRecord {
	private String src;
	private String trg;
	private double score;
	private String lemma;
	private String tag;
	/**
	 * @return the src
	 */
	public String getSrc() {
		return src;
	}
	/**
	 * @param src the src to set
	 */
	public void setSrc(String src) {
		this.src = src;
	}
	/**
	 * @return the trg
	 */
	public String getTrg() {
		return trg;
	}
	/**
	 * @param trg the trg to set
	 */
	public void setTrg(String trg) {
		this.trg = trg;
	}
	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}
	public String getLemma() {
    return lemma;
  }
  /**
   * @param src the src to set
   */
  public void setLemma(String lemma) {
    this.lemma = lemma;
  }
  
  public String getTag() {
    return tag;
  }
  /**
   * @param src the src to set
   */
  public void setTag(String tag) {
    this.tag = tag;
  }
}