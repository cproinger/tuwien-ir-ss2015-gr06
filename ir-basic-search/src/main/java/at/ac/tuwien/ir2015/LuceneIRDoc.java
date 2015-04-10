package at.ac.tuwien.ir2015;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.text.AttributeSet.CharacterAttribute;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

public class LuceneIRDoc extends AbstractIRDoc {

	public LuceneIRDoc(String name, InputStream is) {
		super(name, is);
	}

	private class MyAnalyzer extends Analyzer {

		@Override
		protected TokenStreamComponents createComponents(String fieldName) {
			// TODO Auto-generated method stub
			Tokenizer t = null;
			
			return null;
		}
		
	}
	
	private CountingMap counts = new CountingMap(); 
	private CountingMap bicounts = new CountingMap();
	
	public void process() {
		//Tokenizer tokenizer = new WhitespaceTokenizer();
		
		Reader stopwords = null;
		try (
			Analyzer analyzer = new EnglishAnalyzer();//new StandardAnalyzer();
			TokenStream stream = analyzer.tokenStream("f", new InputStreamReader(is));
			) {
			
			CharTermAttribute att = stream.addAttribute(CharTermAttribute.class);
			
			stream.reset();
			String last = null;
			while(stream.incrementToken()) {
				//String t = stream.reflectAsString(true);
				//System.out.println(att.toString());
				String curr = att.toString();
				//System.out.println(curr);
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
	
	public Map<String, Integer> getCounts() {
		return counts;
	}
}
