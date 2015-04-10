package at.ac.tuwien.ir2015;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {
	
	private class Reader implements Consumer<ZipEntry> {

		@Override
		public void accept(ZipEntry t) {
			try (InputStream is = zip.getInputStream(t);) {
				System.out.println(t.getName());
				
				OpenNLPIRDoc doc = new OpenNLPIRDoc(t.getName(), is);
				all.addAll(doc.process());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	private CountingMap all = new CountingMap();

	/**
	 * change by specifying zip file, for example -Dzip=X/z.zip
	 */
	private static String docs = System.getProperty("zip", "e:/tu/information retrieval/20_newsgroups_subset.zip");
	
	public static void main(String[] args) throws ZipException, IOException, ParseException {
		Options o = new Options();
		//TODO https://commons.apache.org/proper/commons-cli/usage.html
		o.addOption("f", false, "zip file with stuff in it");
		CommandLine cmd = new BasicParser().parse(o, args);
		if(cmd.hasOption("f"))
			docs = cmd.getOptionValue("f");
		
		if(o.hasOption("help")) {
			new HelpFormatter().printHelp("ir-basic-search", o);
			
		}
			
		new App().index();
		
	}

	private ZipFile zip;

	public static final String IR_TOPIC_FILE = "ir.topicFile";

	private void index() throws ZipException, IOException {
		this.zip = new ZipFile(new File(docs));
		try {
			Reader reader = new Reader();
			zip.stream().parallel().forEach(reader);
			System.out.println(all);
		} finally {
			this.zip.close();
		}
		
	}

	static int getMaxResults() {
		return Integer.parseInt(System.getProperty("ir.maxResults", "100"));
	}
}
