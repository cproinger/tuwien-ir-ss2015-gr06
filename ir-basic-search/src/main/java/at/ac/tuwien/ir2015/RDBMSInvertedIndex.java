package at.ac.tuwien.ir2015;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;

public class RDBMSInvertedIndex implements InvertedIndex {
	
	private static class SimpleSearchResult implements ISearchResult {
		private StringBuilder sb = new StringBuilder();
		private void addLine(String line) {
			sb.append(line).append("\n");
		}
		@Override
		public String toString() {
			return sb.toString();
		}
	}

	private static final String SELECT_DICTIONARY_FOR_DOC = "from {0}DICTIONARY p "
						+ "		join {0}POSTING o on o.DICTIONARY_id = p.id "
						+ "		join document d on d.id = o.document_id "
						+ "where p.value = ? ";
	
	private IndexType it;
	
	public RDBMSInvertedIndex(IndexType it) {
		this.it = it;
		
		try(Connection con = getConnection();
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES where table_name = '{0}DICTIONARY'");
			rs.next();
			int tableCount = rs.getInt(1);
			if(tableCount < 1) {
				rs = stmt.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES where table_name = 'DOCUMENT'");
				rs.next();
				if(rs.getInt(1) < 1) {
					stmt.execute("CREATE TABLE document ("
							+ "	id identity primary key , name varchar(255) "
							+ ");");
					stmt.execute("CREATE UNIQUE INDEX idx_docname on document(name);");
				}
				
				stmt.execute("CREATE TABLE {0}DICTIONARY ("
						+ " id identity primary key , value varchar(255)" //hm to short???
						
						+ ");");
				
				stmt.execute("CREATE TABLE {0}POSTING ("
						+ " DICTIONARY_id bigint references {0}DICTIONARY(id), "
						+ " document_id bigint references document(id), "
						+ " times int not null default 0, "
						+ " primary key(DICTIONARY_id, document_id)"
						+ ")");
				
				stmt.execute("CREATE UNIQUE INDEX idx_{0}DICTIONARY on {0}DICTIONARY(value);");
				
				stmt.execute("CREATE INDEX idx_{0}DICTIONARY_POSTING on {0}POSTING(DICTIONARY_id);");
			}
		} catch (SQLException e) {
			throw new RuntimeException("error initializing db", e);
		}
	}

	private Connection getConnection() throws SQLException {
		return Persistence.getDataSource(it.toString().toUpperCase() + "_").getConnection();
	}

	@Override
	public void add(AbstractIRDoc doc) {
		try(Connection con = getConnection();) {
			Long docId = null;
			try (PreparedStatement pstmt = con.prepareStatement(
					"merge into document(name) KEY(name) values(?)", Statement.RETURN_GENERATED_KEYS)) {
				pstmt.setString(1, doc.getName());
				executeUpdateWithCheck(pstmt);
				ResultSet rs = pstmt.getGeneratedKeys();
				if(rs.next()) {
					docId = rs.getLong(1);
				}
			} 
//			catch(SQLException e) {
//				if(e.getErrorCode() == 23505) {
//					//duplicate
//					e.printStackTrace();
//					throw new RuntimeException("duplicate document")
//				} else {
//					throw e;
//				}
//			}
			
			String insOcc = "insert into {0}POSTING(DICTIONARY_id, document_id, times) "
					//+ "	KEY(DICTIONARY_id, document_id) "
					+ "	values("
					+ "		(select id from {0}DICTIONARY where value = ?), "
					+ (docId == 0 ? "		(select id from document where name = ?), " : "?, ")
					+ "?"//+ "		(ifnull((select o.times " + SELECT_DICTIONARY_FOR_DOC + " and d.name = ?), 0) + 1)"
					+ ")";
			try(PreparedStatement occurence = con.prepareStatement(
					insOcc);
				PreparedStatement DICTIONARY = con.prepareStatement(
						"merge into {0}DICTIONARY(value) KEY(value) values(?)");
					) {		
				for(Map.Entry<String, Integer> e : getCounts(doc).entrySet()) {	
					if(!DICTIONARYs.contains(e.getKey())) {
						DICTIONARY.setString(1, e.getKey());
						executeUpdateWithCheck(DICTIONARY);
						DICTIONARYs.add(e.getKey());
					}
					occurence.setString(1, e.getKey());
					if(docId == 0)
						occurence.setString(2, doc.getName());
					else
						occurence.setLong(2, docId);
					occurence.setInt(3, e.getValue());
	//					pstmt.setString(3, e.getKey());
	//					pstmt.setString(4, doc.getName());
					//executeUpdateWithCheck(pstmt);
					occurence.addBatch();
				}
				occurence.executeBatch();
			}
		} catch (SQLException e) {
			throw new RuntimeException("data access exception for add", e);
		}
	}

	private Map<String, Integer> getCounts(AbstractIRDoc doc) {
		return it.getCounts(doc);
	}

	private HashSet<String> DICTIONARYs = new HashSet<>();
	
	private void executeUpdateWithCheck(PreparedStatement pstmt)
			throws SQLException {
		int c = pstmt.executeUpdate();
		if(c != 1)
			throw new IllegalStateException("merge returned " + c);
	}

	@Override
	public IndexValue get(String s) {
		try(Connection con = getConnection();
			PreparedStatement pstmt = con.prepareStatement(
					"select d.name AS D_NAME, o.times "
					+ SELECT_DICTIONARY_FOR_DOC
					+ "");
			) {
			pstmt.setString(1, s);
			try (ResultSet rs = pstmt.executeQuery();) {
				System.out.print(".");
				if(!rs.next())
					return null;
				IndexValue iv = new IndexValue(new LazyIrDoc(rs.getString("D_NAME")), rs.getInt("times"));
				while(rs.next()) {
					iv.add(new LazyIrDoc(rs.getString("D_NAME")), rs.getInt("times"));
				}
				return iv;
			}
		} catch (SQLException e) {
			throw new RuntimeException("data access exception for get", e);
		}
	}

	@Override
	public ISearchResult search(AbstractIRDoc doc, String runName) {
		String sql = " select top " + "100"
				+ " count(logtf_p1), sum(logtf_p1) as score, name AS D_NAME from ( "
				+ " select 1+ log(times) logtf_p1, d.name from {0}POSTING o "
				+ "    join document d on o.document_id = d.id "
				+ "    join {0}DICTIONARY p on p.id = o.DICTIONARY_id "
				+ " where p.value in (? :in) "
				+ " ) as x "
				+ " group by name "
				+ " order by sum(logtf_p1) desc"; 
		sql = sql.replace(":in", new String(new char[getCounts(doc).size() -1 ]).replace("\0", ", ?"));
		
		try(Connection con = getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql)) {
			int i = 1;
			for(String s : getCounts(doc).keySet()) {
				pstmt.setString(i++, s);
			}			
			SimpleSearchResult sr = new SimpleSearchResult();
			int j = 1;
			try(ResultSet rs = pstmt.executeQuery();) {
				while(rs.next()) {
					
					String docName = rs.getString("D_NAME");
					double score = rs.getDouble("score");
					String line = String.format(ISearchResult.RESULT_FORMAT
							, doc.getName()
							, docName
							, j
							, score
							, runName);
					j++;
					sr.addLine(line);
				}
			}
			return sr;
		} catch (SQLException e) {
			throw new RuntimeException("data access exception for search", e);
		}
		
//		
//		SearchResult sr = new SearchResult(doc.getName(), runName);
//		Logg.info("counts: " + doc.getCounts().size());
//		for(String s : doc.getCounts().keySet()) {
//			IndexValue b = get(s);
//			if(b != null) {
//				//hit
//				sr.add(b);
//			}
//		}
//		return sr;
	}
	
//	private class PersistentIndexValue extends IndexValue {
//		
//	}
}
