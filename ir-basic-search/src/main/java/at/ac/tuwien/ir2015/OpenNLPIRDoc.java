package at.ac.tuwien.ir2015;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.Tokenizer;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class OpenNLPIRDoc extends AbstractIRDoc {
	
    public OpenNLPIRDoc(String name, InputStream is) {
		super(name, is);
	}

	public CountingMap process() {
		try {
			TokenizerModel model = new TokenizerModel(getClass().getResource("/en-token.bin"));
			TokenizerME tok = new TokenizerME(model);
			
			//WhitespaceTokenizer tok = new WhitespaceTokenStream()
			
		
			Scanner sc = new Scanner(is);
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				
				line = line.replaceAll("[>!:|/]", " ");
				line = line.replaceAll("--+", " ");
				line = line.replaceAll("\\.\\.+", " ");
				line = line.replaceAll("\\. ", " ");
				line = line.replaceAll("  ", " ");
				
				String[] tokenized = tok.tokenize(line);
				String lastToken = null;
				for(String t : tokenized) {
					String actToken = t;//t.intern();
					bagOfWords.add(actToken);
					
					if(lastToken != null) {
						biword.add(lastToken + " " + actToken);
					}
					lastToken = actToken;
				}
				//System.out.println(Arrays.toString(tokenized));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(tokens);
		return bagOfWords;
	}
	

}
