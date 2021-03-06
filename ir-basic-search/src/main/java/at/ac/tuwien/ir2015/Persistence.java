package at.ac.tuwien.ir2015;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.DelegatingConnection;
import org.apache.commons.dbcp2.DelegatingStatement;

public class Persistence {

	public static final String IR_DB_FILE = "ir.dbFile";
	private static volatile BasicDataSource dataSource = null;
	private static Connection keepDBOpenCon;
	
	public static DataSource getDataSource() {
		if(dataSource == null) {
			synchronized (Persistence.class) {
				if(dataSource == null) {					
					BasicDataSource ds = new BasicDataSource();
					ds.setUrl(System.getProperty(IR_DB_FILE, "jdbc:h2:file:./target/ir_db;MAX_COMPACT_TIME=10000"));//;CACHE_SIZE=10240"));
					ds.setUsername("sa");
					ds.setMinIdle(2);
					ds.setInitialSize(2);
					ds.setMaxTotal(2);
					ds.setPoolPreparedStatements(true);
					ds.setDefaultAutoCommit(true);
					try {
						keepDBOpenCon = ds.getConnection();
					} catch (SQLException e) {
						throw new RuntimeException("cannot get connection");
					}
					dataSource = ds;
				}
			}
		}
		return dataSource;
	}
	
	public static DataSource getDataSource(final String indexName) {
		final DataSource ds = getDataSource();
		return (DataSource) Proxy.newProxyInstance(Persistence.class.getClassLoader(), 
				new Class[]{DataSource.class}, new InvocationHandler() {
					
					@Override
					public Object invoke(Object proxy, Method method, Object[] args)
							throws Throwable {
						if("getConnection".equals(method.getName()) && (args == null || args.length == 0)) {
							return newConnectionProxy(ds.getConnection());
						} else {
							return method.invoke(ds, args);
						}
					}

					private Connection newConnectionProxy(Connection connection) {
						return new DelegatingConnection<Connection>(connection) {
							@Override
							public PreparedStatement prepareStatement(
									String arg0) throws SQLException {
								return super.prepareStatement(MessageFormat.format(arg0, indexName));
							}
							
							@Override
							public Statement createStatement()
									throws SQLException {
								return new DelegatingStatement(this, super.createStatement()) {
									@Override
									public boolean execute(String arg0)
											throws SQLException {
										return super.execute(MessageFormat.format(arg0, indexName));
									}
									
									@Override
									public ResultSet executeQuery(String arg0)
											throws SQLException {
										arg0 = arg0.replace("'", "#");
										String s = MessageFormat.format(arg0, indexName);
										s = s.replace("#", "'");
										return super.executeQuery(s);
									}
								};
							}
						};
					}
				});
	}

	public static void close() {
		if(dataSource != null) {
			try {
				keepDBOpenCon.close();
				dataSource.close();
			} catch (SQLException e) {
				throw new RuntimeException("ex while closing datasource", e);
			}
		}
	}
}
