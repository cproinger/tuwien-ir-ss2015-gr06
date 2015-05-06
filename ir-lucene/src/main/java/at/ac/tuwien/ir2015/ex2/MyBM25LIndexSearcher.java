package at.ac.tuwien.ir2015.ex2;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import at.ac.tuwien.ir2015.ex2.similarity.BM25LSimilarity;

public class MyBM25LIndexSearcher {

	public void search() throws IOException, ParseException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("target/index")));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25LSimilarity());
		Analyzer analyzer = new StandardAnalyzer();
		
		QueryParser parser = new QueryParser("contents", analyzer);
//		Query query = parser.parse("search for atheism");
		Query query = parser.parse("jail cat fish");
		TopFieldDocs result = searcher.search(query, 10, Sort.RELEVANCE);
		for(ScoreDoc d : result.scoreDocs) {
			System.out.println(d);
			System.out.println(searcher.explain(query, d.doc));
		}
	}
}
