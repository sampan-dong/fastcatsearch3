/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.fastcatsearch.db.object.BannedDictionary;
import org.fastcatsearch.db.object.BasicDictionary;
import org.fastcatsearch.db.object.CustomDictionary;
import org.fastcatsearch.db.object.IndexingHistory;
import org.fastcatsearch.db.object.IndexingResult;
import org.fastcatsearch.db.object.IndexingSchedule;
import org.fastcatsearch.db.object.JobHistory;
import org.fastcatsearch.db.object.RecommendKeyword;
import org.fastcatsearch.db.object.SearchEvent;
import org.fastcatsearch.db.object.SearchMonInfoHDWMY;
import org.fastcatsearch.db.object.SearchMonInfoMinute;
import org.fastcatsearch.db.object.SynonymDictionary;
import org.fastcatsearch.db.object.SystemMonInfoHDWMY;
import org.fastcatsearch.db.object.SystemMonInfoMinute;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.keyword.KeywordFail;
import org.fastcatsearch.keyword.KeywordHit;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.IRSettings;
import org.fastcatsearch.settings.Settings;


public class DBService extends AbstractService {

	public final static String DB_NAME = "db";
	private String JDBC_URL;
	protected Connection conn = null;
	
	public IndexingResult IndexingResult;
	public SynonymDictionary SynonymDictionary;
	public IndexingSchedule IndexingSchedule;
	public IndexingHistory IndexingHistory;
	public JobHistory JobHistory;
	public SearchEvent SearchEvent;
	public CustomDictionary CustomDictionary;
	public BannedDictionary BannedDictionary;
	public BasicDictionary BasicDictionary;
	public KeywordHit KeywordHit;
	public KeywordFail KeywordFail;
	public RecommendKeyword RecommendKeyword;
	
	//모니터링.
	public SystemMonInfoMinute SystemMonInfoMinute;
	public SystemMonInfoHDWMY SystemMonInfoHDWMY;
	public SearchMonInfoMinute SearchMonInfoMinute;
	public SearchMonInfoHDWMY SearchMonInfoHDWMY;
	
	protected static DBService instance;
	
	public static DBService getInstance(){
		return instance;
	}
	public void asSingleton() {
		instance = this;
	}
	
	public DBService(Environment environment, Settings settings, ServiceManager serviceManager){
		super(environment, settings, serviceManager);
	}
	
	protected boolean doStart() throws ServiceException {
		JDBC_URL = "jdbc:derby:" + IRSettings.path(DB_NAME);
		
		IRConfig config = IRSettings.getConfig();
		String dbhandlerType = config.getString("dbhandler.type");

		if("external".equals(dbhandlerType)) {
			String cls = config.getString("dbhandler.external.jdbccls");
			String url = config.getString("dbhandler.external.jdbcurl");
			String user = config.getString("dbhandler.external.jdbcuser");
			String pass = config.getString("dbhandler.external.jdbcpass");
			logger.info(cls+":"+url+":"+user+":"+pass);
			try {
				Class.forName(cls).newInstance();
			} catch (Exception e) {
				throw new ServiceException("Cannot load driver class!", e);
			}
			
			try {
				conn = DriverManager.getConnection(url,user,pass);
				//2012-10-16
				//오토커밋으로 변경.
//				conn.setAutoCommit(false);
				logger.info("DBHandler create and init DB!");
				this.testAndInitDB();
//				conn.commit();
			} catch (SQLException e) {
				//conn = createDB(url,user,pass);
				throw new ServiceException("관리DB로의 연결을 생성할수 없습니다.", e);
			}
			logger.info("DBHandler started! " + conn);
			return true;
		} else {
			try {
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			} catch (Exception e) {
				throw new ServiceException("Cannot load driver class!", e);
			}
			
			try {
				conn = DriverManager.getConnection(JDBC_URL);
			} catch (SQLException e) {
				logger.info("DBHandler create and init DB!");
				// if DB is not created.
				conn = createDB(JDBC_URL,null,null);
				if (conn == null) {
					throw new ServiceException("내부 DB로의 연결을 생성할수 없습니다. DB를 이미 사용중인 프로세스가 있는지 확인필요.", e);
				}
			}
			
			try {
				//오토커밋으로 변경.
				initDB(conn);
			} catch (SQLException e1) {
				throw new ServiceException(e1);
			}
			
			try {
				testAndInitDB();
			} catch (SQLException e) {
				throw new ServiceException(e);
			}
			
			try {
				initMONDB(conn);
			} catch (SQLException e1) {
				throw new ServiceException(e1);
			}
			
			try {
				testAndInitMONDB();
			} catch (SQLException e) {
				throw new ServiceException(e);
			}
			
			logger.info("DBHandler started! " + conn);
			return true;
			
		}
	}

	private void initDB(Connection conn) throws SQLException {
		IndexingResult = new IndexingResult();
		SynonymDictionary = new SynonymDictionary();
		IndexingSchedule = new IndexingSchedule();
		IndexingHistory = new IndexingHistory();
		JobHistory = new JobHistory();
		SearchEvent = new SearchEvent();
		CustomDictionary = new CustomDictionary();
		BannedDictionary = new BannedDictionary();
		BasicDictionary = new BasicDictionary();
		KeywordHit = new KeywordHit();
		KeywordFail = new KeywordFail();
		SearchMonInfoMinute = new SearchMonInfoMinute();
		SearchMonInfoHDWMY = new SearchMonInfoHDWMY();
		
		IndexingResult.setConnection(conn);
		SynonymDictionary.setConnection(conn);
		IndexingSchedule.setConnection(conn);
		IndexingHistory.setConnection(conn);
		JobHistory.setConnection(conn);
		SearchEvent.setConnection(conn);
		CustomDictionary.setConnection(conn);
		BannedDictionary.setConnection(conn);
		BasicDictionary.setConnection(conn);
		KeywordHit.setConnection(conn);
		KeywordFail.setConnection(conn);
		SearchMonInfoMinute.setConnection(conn);
		SearchMonInfoHDWMY.setConnection(conn);
		
		IndexingResult.repairStatus();
		
	}
	
	private void testAndInitDB() throws SQLException {
		IndexingResult.testAndCreate();
		SynonymDictionary.testAndCreate();
		IndexingSchedule.testAndCreate();
		IndexingHistory.testAndCreate();
		JobHistory.testAndCreate();
		SearchEvent.testAndCreate();
		CustomDictionary.testAndCreate();
		BannedDictionary.testAndCreate();
		BasicDictionary.testAndCreate();
		KeywordHit.testAndCreate();
		KeywordFail.testAndCreate();
		SearchMonInfoMinute.testAndCreate();
		SearchMonInfoHDWMY.testAndCreate();
		
//		IndexingResult.prepareID();
		SynonymDictionary.prepareID();
//		IndexingSchedule.prepareID();
		IndexingHistory.prepareID();
		JobHistory.prepareID();
		SearchEvent.prepareID();
		CustomDictionary.prepareID();
		BannedDictionary.prepareID();
		BasicDictionary.prepareID();
		KeywordHit.prepareID();
		KeywordFail.prepareID();
		SearchMonInfoMinute.prepareID();
		SearchMonInfoHDWMY.prepareID();
		
		IndexingResult.repairStatus();
	}
	
	private void initMONDB(Connection conn) throws SQLException {
		RecommendKeyword = new RecommendKeyword();
		SystemMonInfoMinute = new SystemMonInfoMinute();
		SystemMonInfoHDWMY = new SystemMonInfoHDWMY();
		
		RecommendKeyword.setConnection(conn);
		SystemMonInfoMinute.setConnection(conn);
		SystemMonInfoHDWMY.setConnection(conn);
		
	}
	
	private void testAndInitMONDB() throws SQLException {
		RecommendKeyword.testAndCreate();
		SystemMonInfoMinute.testAndCreate();
		SystemMonInfoHDWMY.testAndCreate();
		
		SystemMonInfoMinute.prepareID();
		SystemMonInfoHDWMY.prepareID();
		
	}
	
	private Connection createDB(String jdbcurl, String jdbcuser, String jdbcpass) {
		try {
			logger.info("Creating Fastcat DB = " + jdbcurl);
			if(jdbcuser!=null && jdbcpass!=null) {
				return DriverManager.getConnection(jdbcurl + ";create=true",jdbcuser,jdbcpass);
			} else {
				return DriverManager.getConnection(jdbcurl + ";create=true");
			}
		} catch (SQLException e) {

		}
		return null;
	}

	protected boolean doStop() throws ServiceException {
		try {
			logger.info("DBHandler shutdown! " + conn);
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			return false;
		}
		return true;
	}
	
	//db
	public int updateOrInsertSQL(String sql) throws SQLException{
		Statement stmt = conn.createStatement();
		int n = stmt.executeUpdate(sql);
		return n;
	}
	
	public ResultSet selectSQL(String sql) throws SQLException{
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		return rs;
	}
	
	//mon db
	public int updateOrInsertSQLMONDB(String sql) throws SQLException{
		Statement stmt = conn.createStatement();
		int n = stmt.executeUpdate(sql);
		stmt.close();
		return n;
	}
	
	public ResultSet selectSQLMONDB(String sql) throws SQLException{
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		stmt.close();
		return rs;
	}
	//for batch insert
	public Connection getConn() {
		return conn;
	}

	@Override
	protected boolean doClose() throws ServiceException {
		return true;
	}

}

