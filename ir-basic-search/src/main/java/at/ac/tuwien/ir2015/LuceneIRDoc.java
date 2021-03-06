package at.ac.tuwien.ir2015;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import at.ac.tuwien.ir2015.util.CountingMap;

public class LuceneIRDoc extends AbstractIRDoc {

	private final InputStream is;
	
	public LuceneIRDoc(String name, InputStream is) {
		super(name);
		this.is = is;
	}

	/**
	 * Vocabulary 
	 * 
	 * Normalize the vocabulary by applying any combination of the
	 * techniques de-scribed in Chapter 2 of the Introduciton to Information
	 * Retrieval book (case folding, removing stopwords, stemming). These
	 * options should be exposed as parameters in the index creation phase.
	 * 
	 * stemming, stopwords and caseFolding can be deactivated by
	 * setting the respective of the system-properties to false. 
	 * 
	 * @author cproinger
	 *
	 */
	private class MyAnalyzer extends Analyzer {

		@Override
		protected TokenStreamComponents createComponents(String fieldName) {
		    final Tokenizer source = new StandardTokenizer();
		    TokenStream result = new StandardFilter(source);
		    
		    result = new EnglishPossessiveFilter(result);
		    
		    if(!"false".equals(System.getProperty(AbstractIRDoc.IR_ANALYZER_CASE_FOLDING)))
		    	result = new LowerCaseFilter(result);
		    
		    
		    if(!"false".equals(System.getProperty(AbstractIRDoc.IR_ANALYZER_STOPWORDS)))
		    	result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		    
//		    if(!stemExclusionSet.isEmpty())
//		      result = new SetKeywordMarkerFilter(result, stemExclusionSet);
		    
		    if(!"false".equals(System.getProperty(AbstractIRDoc.IR_ANALYZER_STEM)))
		    	result = new PorterStemFilter(result);
		    
		    return new TokenStreamComponents(source, result);
		}
		
	}
	
	public void process() {
		this.counts = new CountingMap();
		this.bicounts = new CountingMap();
		//Tokenizer tokenizer = new WhitespaceTokenizer();
		
		try (
			Analyzer analyzer = new MyAnalyzer();//new EnglishAnalyzer();//new StandardAnalyzer();
			TokenStream stream = analyzer.tokenStream("f", new InputStreamReader(is));
			) {
			
			CharTermAttribute att = stream.addAttribute(CharTermAttribute.class);
			
			stream.reset();
			String last = null;
			while(stream.incrementToken()) {
				//String t = stream.reflectAsString(true);
				//Logg.info(att.toString());
				String curr = att.toString();
				//Logg.info(curr);
				counts.add(curr);
				if(last != null) {
					bicounts.add(last + " " + curr);
				}
				last = curr;
			}
			stream.end();
			
		} catch (IOException e) {
			throw new RuntimeException("unexpected IO Exception", e);
		}
		//PorterStemFilter stemFilter = new PorterStemFilter(in);
	}
	
	@Override
	public Map<String, Integer> getCounts() {
		if(counts == null) {
			//sicherheitshalber implizit verarbeiten falls das noch nicht passiert ist. 
			process();
		}
		return counts;
	}
}
