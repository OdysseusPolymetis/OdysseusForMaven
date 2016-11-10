
package fr.odysseus.lucene;


import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.WildcardQuery;


/** The abstract base class for queries.
    <p>Instantiable subclasses are:
    <ul>
    <li> {@link TermQuery}
    <li> {@link BooleanQuery}
    <li> {@link WildcardQuery}
    <li> {@link PhraseQuery}
    <li> {@link PrefixQuery}
    <li> {@link MultiPhraseQuery}
    <li> {@link FuzzyQuery}
    <li> {@link RegexpQuery}
    <li> {@link TermRangeQuery}
    <li> {@link NumericRangeQuery}
    <li> {@link ConstantScoreQuery}
    <li> {@link DisjunctionMaxQuery}
    <li> {@link MatchAllDocsQuery}
    </ul>
    <p>See also the family of {@link org.apache.lucene.search.spans Span Queries}
       and additional queries available in the <a href="{@docRoot}/../queries/overview-summary.html">Queries module</a>
*/
public abstract class HomeQuery extends Query {
  private float boost = 1.0f;                     // query boost factor

  public void setBoost(float b) { boost = b; }


  public float getBoost() { return boost; }

  /** Prints a query to a string, with <code>field</code> assumed to be the 
   * default field and omitted.
   */
  public abstract String toString(String field);

  /** Prints a query to a string. */

//  public final String toString() {
//    return toString("");
//  }

  /**
   * Expert: Constructs an appropriate Weight implementation for this query.
   * <p>
   * Only implemented by primitive queries, which re-write to themselves.
   *
   * @param needsScores   True if document scores ({@link Scorer#score}) or match
   *                      frequencies ({@link Scorer#freq}) are needed.
   */
  public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
    throw new UnsupportedOperationException("Query " + this + " does not implement createWeight");
  }

  /** Expert: called to re-write queries into primitive queries. For example,
   * a PrefixQuery will be rewritten into a BooleanQuery that consists
   * of TermQuerys.
   */
  public Query rewrite(IndexReader reader) throws IOException {
    if (boost != 1f) {
      HomeQuery rewritten = clone();
      rewritten.setBoost(1f);
      return new BoostQuery(rewritten, boost);
    }
    return this;
  }

  @Override
  public HomeQuery clone() {
    try {
      return (HomeQuery)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Clone not supported: " + e.getMessage());
    }
  }

  @Override
  public int hashCode() {
    return Float.floatToIntBits(boost) ^ getClass().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    HomeQuery other = (HomeQuery) obj;
    return Float.floatToIntBits(boost) == Float.floatToIntBits(other.boost);
  }
}
