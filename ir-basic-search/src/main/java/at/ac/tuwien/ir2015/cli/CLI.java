package at.ac.tuwien.ir2015.cli;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import at.ac.tuwien.ir2015.App;

public class CLI {
	
	private static Options options = new Options();
	static {		
		options.addOption("f", "topicFile", true, "file with a topic to search for");
		options.addOption("?", "help", false, "display the help text");
	}
	
	public static void main(String[] args) {
		//Arrays.asList(args).forEach(s -> System.out.println(s));
		CommandLineParser parser = new BasicParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			if(cmd.hasOption("?")) {
				printHelp();
				return;
			}
			if(cmd.hasOption("f")) {
				System.setProperty(App.IR_TOPIC_FILE, cmd.getOptionValue("f"));
			}
			
			
		} catch (ParseException e) {
			e.printStackTrace();
			
			printHelp();
		}
	}

	private static void printHelp() {
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp("ir", options);
	}
}
