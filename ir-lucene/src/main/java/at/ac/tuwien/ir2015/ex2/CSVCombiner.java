package at.ac.tuwien.ir2015.ex2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.print.attribute.Size2DSyntax;

import org.apache.lucene.search.payloads.AveragePayloadFunction;

public class CSVCombiner {

	private static class EvalResult {
		private String name;
		private TreeMap<String, String> topicToEval = new TreeMap<>();

		public EvalResult(String name) {
			super();
			this.name = name;
		}

	}

	public static void main(String[] args) throws IOException {
		File[] csvFiles = new File("target").listFiles((f, s) -> s
				.endsWith(".csv") && !s.equals("eval.csv"));

		Function<File, EvalResult> mapper = (File f) -> {
			EvalResult res = new EvalResult(f.getName());
			try (BufferedReader br = new BufferedReader(new FileReader(f));) {
				String line = "";
				while ((line = br.readLine()) != null) {
					String[] parts = line.split("\t");
					res.topicToEval.put(parts[1], parts[2]);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return res;
		};
		Stream<EvalResult> results = Arrays.asList(csvFiles).stream()
				.map(mapper);
		EvalResult[] arr = results.toArray(size -> new EvalResult[size]);

		try (PrintWriter bw = new PrintWriter(new FileWriter(new File(
				"target/eval.csv")));) {
			for(int i = 0; i < arr.length; i++) {
				bw.print("\t");
				String name = arr[i].name;
				bw.print(name.substring(0, name.indexOf("-")));
			};
			bw.println();
			TreeMap<String, String[]> map = new TreeMap<String, String[]>();
			for (int i = 0; i < arr.length; i++) {
				final int j = i;
				arr[i].topicToEval.forEach((key, value) -> {
					String[] v = map.get(key);
					if(v == null) v = new String[arr.length];
					v[j] = value;
					map.put(key, v);
				});
			}
			map.forEach((k, v) -> {
				bw.print(k);
				Arrays.asList(v).forEach(s -> bw.print("\t" + s));
				bw.println();
			});
			
		}
	}
}
