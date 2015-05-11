package at.ac.tuwien.ir2015.ex2;

import org.apache.lucene.demo.IndexFiles;

public class MyIndexer {

	/**
	 * um zu dokumentieren wie wir den Index aufgebaut haben. 
	 */
	public static void main(String[] args) {
		//parameter: -index target/index -docs "C:\20_newsgroups_subset"
		IndexFiles.main(args);
	}
}
