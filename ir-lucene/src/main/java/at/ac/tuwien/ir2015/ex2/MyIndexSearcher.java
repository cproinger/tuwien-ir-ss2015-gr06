package at.ac.tuwien.ir2015.ex2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;

import at.ac.tuwien.ir2015.ex2.similarity.BM25LSimilarity;

public class MyIndexSearcher {

	/**
	 * org.apache.lucene.demo.IndexFiles.main(String[])
	 * indexes content in this field. 
	 */
	private static final String CONTENTS = "contents";

	public enum SearchType {BM25, BM25L};
	
	private SearchType searchType;
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private QueryParser parser;
	private PrintStream printStream = System.out;
	
	public MyIndexSearcher(IndexReader ir, SearchType searchType) throws IOException {
		this.searchType = searchType;
		reader = ir;
		searcher = new IndexSearcher(reader);
		if (searchType == SearchType.BM25) {
			searcher.setSimilarity(new BM25Similarity());
		} else {
			searcher.setSimilarity(new BM25LSimilarity(1.2F, 0.75F, 0.5F));
		}
		analyzer = createAnalyzer();
		parser = new QueryParser(CONTENTS, analyzer);
	}
	
	public MyIndexSearcher(IndexReader ir, SearchType searchType, PrintStream printStream) throws IOException {
		this(ir, searchType);
		this.printStream = printStream;
	}

	/**
	 * für ad-hoc suchen (suche ohne konkretes query-dokument) wird explain auch ausgegeben. 
	 */
	public void search(String queryText) throws ParseException, IOException {
		final Query query = parser.parse(queryText);
		doSearchAndFormat("ad-hoc-query", "experiment-name1", query, new TrecFormatter(reader, printStream) {
			@Override
			public void format(String queryName, TopDocs result,
					String experimentName) throws IOException {
				super.format(queryName, result, experimentName);
//				for(ScoreDoc d : result.scoreDocs) {
//					printStream.println(searcher.explain(query, d.doc));
//				}
			}
		});
	}
		
	public void search(File f, String experimentName) throws FileNotFoundException, IOException {
		try(Analyzer a = createAnalyzer();
				TokenStream ts = a.tokenStream(CONTENTS, new FileReader(f));) {
				
				Query query = toQuery(ts);
				
				doSearchAndFormat(f.getName(), experimentName, query, new TrecFormatter(reader, printStream));
		}
	}

	private StandardAnalyzer createAnalyzer() {
		//may change to EnglishAnalyzer?
		//org.apache.lucene.demo.IndexFiles.main(String[]) uses StandardAnalyzer though. 
		return new StandardAnalyzer();
	}

	private Query toQuery(TokenStream stream) throws IOException {
		CharTermAttribute att = stream.addAttribute(CharTermAttribute.class);
		
		//TermToBytesRefAttribute ttbfa = stream.addAttribute(TermToBytesRefAttribute.class);
		
		stream.reset();
		ArrayList<Term> terms = new ArrayList<>();
		//ArrayList<BytesRef> brefs = new ArrayList<>();
		while(stream.incrementToken()) {
			String curr = att.toString();
			Term t = new Term(CONTENTS, curr);
			
			//BytesRef bref = ttbfa.getBytesRef();
			//brefs.add(bref);
			//System.out.println(new String(bref.bytes));
			
			//sb.append(curr).append(" ");
			System.out.println(curr);
			terms.add(t);
//			if(true) break;
		}
		stream.end();

		//use CommonTermsQuery with StandardAnalyzer?
		//return new TermsQuery(CONTENTS, brefs);
		System.out.println("terms.size(): " + terms.size());
		//strangely this does not work the same way a boolean-query with shoulds works
		//this returns score 1 for every document. 
//		return new TermsQuery(terms);
		BooleanQuery bq = new BooleanQuery();
		terms.forEach(t -> bq.add(new TermQuery(t), Occur.SHOULD));
		return bq;
	}

	private void doSearchAndFormat(String queryName, String experimentName,
			Query query, TrecFormatter form) throws IOException {
		TopDocs result = searcher.search(query, 10);
		form.format(queryName, result, experimentName);
	}
}
