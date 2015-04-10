package at.ac.tuwien.ir2015;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.cli.ParseException;

public class App {
	
	private class Reader implements Consumer<ZipEntry> {

		@Override
		public void accept(ZipEntry t) {
			try (InputStream is = zip.getInputStream(t);) {
				System.out.println("reading: " + t.getName());
				
				LuceneIRDoc doc = new LuceneIRDoc(t.getName(), is);
				doc.process();
				try {
					docQueue.put(doc);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				throw new RuntimeException("error reading " + t.getName(), e);
			}
		}		
	}
	/**
	 * mein laptop (cproinger) is schon alt und einmal hat er sich abgeschaltet vermutlich
	 * weil er zu heiß wurde wegen der hohen cpu-last. Könnte so ein
	 * System-property angeben welches z.B. alle x files eine pause macht. 
	 * 
	 * aber momentan gehts eh, mal schaun. 
	 */
	private static final String IR_PAUSE = "ir.pause";
	
	public static final String IR_TOPIC_FILE = "ir.topicFile";
	
	public static final String IR_ZIP = "ir.zip";
	
	/**
	 * change by specifying zip file, for example -Dzip=X/z.zip
	 */
	private static String docs = System.getProperty(IR_ZIP, 
			"e:/tu/information retrieval/20_newsgroups_subset.zip");
	

	static int getMaxResults() {
		return Integer.parseInt(System.getProperty("ir.maxResults", "100"));
	}
	
	public static void main(String[] args) throws ZipException, IOException, ParseException {
		long start = System.currentTimeMillis();
		new App().index();
		long took = System.currentTimeMillis() - start;
		System.out.println("took " + (took / 1000) + " seconds");
	}

	private BagOfWords bagOfWords = new BagOfWords();

	private BlockingQueue<LuceneIRDoc> docQueue = new LinkedBlockingQueue<LuceneIRDoc>(20);

	private ZipFile zip;

	private void index() throws ZipException, IOException {
		Thread t = new Thread() {
			@Override
			public void run() {
				LuceneIRDoc doc = null;
				try {
					while((doc = docQueue.take()) != null) {
						System.out.println("adding to index: " + doc.getName()); 
						bagOfWords.add(doc);
					}
				} catch (InterruptedException e) {
					System.out.println("interrupted");
				}
				System.out.println("index done");
			}
		};
		t.start();
		
		this.zip = new ZipFile(new File(docs));
		try {
			Reader reader = new Reader();
			zip.stream()
				.parallel()
				.forEach(reader);
		} finally {
			this.zip.close();
		}

		t.interrupt();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
