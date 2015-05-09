package at.ac.tuwien.ir2015.ex2;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import at.ac.tuwien.ir2015.ex2.similarity.BM25LSimilarity;

public class MyIndexSearcher {
	String indexPath;
	String queryText;
	public enum SearchType {BM25, BM25L};
	SearchType searchType;
	
	public MyIndexSearcher(String indexPath, String queryText, SearchType searchType) {
		this.indexPath = indexPath;
		this.queryText = queryText;
		this.searchType = searchType;
		
	}

	public void search() throws IOException, ParseException {
		
		List<IndexableField> fields;
		Field f;
		String patternString = ".*<path:(.*)>";
		String fileName = "";
		int rank = 1;
		float score = 0.0f;
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		if (searchType == SearchType.BM25) {
			searcher.setSimilarity(new BM25Similarity());
		} else {
			searcher.setSimilarity(new BM25LSimilarity(1.2F, 0.75F, 0.5F));
		}
		Analyzer analyzer = new StandardAnalyzer();
		
		QueryParser parser = new QueryParser("contents", analyzer);
		Query query = parser.parse(queryText);
//		Query query = parser.parse("search for atheism");
		TopFieldDocs result = searcher.search(query, 10, Sort.RELEVANCE);
		Pattern pattern = Pattern.compile(patternString);
		for(ScoreDoc d : result.scoreDocs) {
			Iterator myIt; 
			
			fields = searcher.doc(d.doc).getFields();
			myIt = fields.iterator();
			while (myIt.hasNext()) {
				f = (Field) myIt.next();
		        Matcher matcher = pattern.matcher(f.toString());
		        while(matcher.find()) {
		            fileName = matcher.group(1);
		        }
			}
			score = searcher.explain(query, d.doc).getValue();
			System.out.println("TopicX Q0 " + fileName + " " + rank++ + " " + score + " group6-experiment1");

			//System.out.println(d);
			//System.out.println(d.score);

			//System.out.println(searcher.explain(query, d.doc));
		}
	}
}

