package at.ac.tuwien.ir2015;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

	private static final String SELECT_POSTING_FOR_DOC = "from {0}posting p "
						+ "		join {0}occurrence o on o.posting_id = p.id "
						+ "		join document d on d.id = o.document_id "
						+ "where p.value = ? ";
	
	private final String name;//TODO use separate tables. 
	
	public RDBMSInvertedIndex(String name) {
		this.name = name;	
		
		try(Connection con = getConnection();
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES where table_name = '{0}POSTING'");
			rs.next();
			int tableCount = rs.getInt(1);
			if(tableCount < 1) {
				rs = stmt.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES where table_name = 'DOCUMENT'");
				rs.next();
				if(rs.getInt(1) < 1) {
					stmt.execute("CREATE TABLE document ("
							+ "	id identity primary key , name varchar(255) "
							+ ");");
				}
				
				stmt.execute("CREATE TABLE {0}posting ("
						+ " id identity primary key , value varchar(255)" //hm to short???
						
						+ ");");
				
				stmt.execute("CREATE TABLE {0}occurrence ("
						+ " posting_id bigint references {0}posting(id), "
						+ " document_id bigint references document(id), "
						+ " times int not null default 0, "
						+ " primary key(posting_id, document_id)"
						+ ")");
				
				stmt.execute("CREATE UNIQUE INDEX idx_{0}docname on {0}document(name);");
				stmt.execute("CREATE UNIQUE INDEX idx_{0}posting on {0}posting(value);");
				stmt.execute("CREATE INDEX idx_{0}posting_occurrence on {0}occurrence(posting_id);");
			}
		} catch (SQLException e) {
			throw new RuntimeException("error initializing db", e);
		}
	}

	private Connection getConnection() throws SQLException {
		return Persistence.getDataSource(name.toUpperCase() + "_").getConnection();
	}

	@Override
	public void add(AbstractIRDoc doc) {
		try(Connection con = getConnection();) {			
			try (PreparedStatement pstmt = con.prepareStatement(
					"merge into document(name) KEY(name) values(?)")) {
				pstmt.setString(1, doc.getName());
				executeUpdateWithCheck(pstmt);
			}
			
			for(Map.Entry<String, Integer> e : doc.getCounts().entrySet()) {				
				try(
						PreparedStatement pstmt = con.prepareStatement(
								"merge into {0}posting(value) KEY(value) values(?)");
						) {				
					pstmt.setString(1, e.getKey());
					executeUpdateWithCheck(pstmt);
				}
				
				String sql = "merge into {0}occurrence(posting_id, document_id, times) "
				+ "	KEY(posting_id, document_id) "
				+ "	values("
				+ "		(select id from {0}posting where value = ?), "
				+ "		(select id from {0}document where name = ?), "
				+ "?"//+ "		(ifnull((select o.times " + SELECT_POSTING_FOR_DOC + " and d.name = ?), 0) + 1)"
				+ ")";
				try(PreparedStatement pstmt = con.prepareStatement(
						sql);
				) {		
					pstmt.setString(1, e.getKey());
					pstmt.setString(2, doc.getName());
					pstmt.setInt(3, e.getValue());
//					pstmt.setString(3, e.getKey());
//					pstmt.setString(4, doc.getName());
					
					executeUpdateWithCheck(pstmt);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("data access exception for add", e);
		}
	}

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
					+ SELECT_POSTING_FOR_DOC
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
				+ " select 1+ log(times) logtf_p1, d.name from {0}occurrence o "
				+ "    join document d on o.document_id = d.id "
				+ "    join {0}posting p on p.id = o.posting_id "
				+ " where p.value in (? :in) "
				+ " ) as x "
				+ " group by name "
				+ " order by sum(logtf_p1) desc"; 
		sql = sql.replace(":in", new String(new char[doc.getCounts().size() -1 ]).replace("\0", ", ?"));
		
		try(Connection con = getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql)) {
			int i = 1;
			for(String s : doc.getCounts().keySet()) {
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
//		System.out.println("counts: " + doc.getCounts().size());
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
