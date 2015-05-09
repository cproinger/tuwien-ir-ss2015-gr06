trec_eval -q -m map -c qrels.txt ../target/BM25-trec.txt > ../target/BM25-eval.csv
trec_eval -q -m map -c qrels.txt ../target/BM25L-trec.txt > ../target/BM25L-eval.csv
trec_eval -q -m map -c qrels.txt ../target/LuceneDefault-trec.txt > ../target/LuceneDefault-eval.csv
trec_eval -q -m map -c qrels.txt myIndex-trec.txt > ../target/myIndex-eval.csv