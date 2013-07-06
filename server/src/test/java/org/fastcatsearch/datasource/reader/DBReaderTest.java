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

package org.fastcatsearch.datasource.reader;

import junit.framework.TestCase;

import org.fastcatsearch.datasource.DataSourceSetting;
import org.fastcatsearch.datasource.reader.DBReader.DBReaderConfig;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.settings.IRSettings;


public class DBReaderTest extends TestCase{

	public void testFullIndexing() throws SettingException, IRException{
		IRSettings.setHome("testHome");
		String collection = "blog_db";
		Schema schema = IRSettings.getSchema(collection, true);
		DataSourceSetting dsSetting = IRSettings.getDatasource(collection, true);
		DBReader dbReader = new DBReader(schema, new DBReaderConfig(), null, true);
		
		while(dbReader.hasNext()){
			Document document = dbReader.next();
			System.out.println(document);
		}
	}
	
	public void testAddIndexing() throws SettingException, IRException{
		IRSettings.setHome("testHome");
		String collection = "blog_db";
		Schema schema = IRSettings.getSchema(collection, true);
		DataSourceSetting dsSetting = IRSettings.getDatasource(collection, true);
		DBReader dbReader = new DBReader(schema, new DBReaderConfig(), null, false);
		
		while(dbReader.hasNext()){
			Document document = dbReader.next();
			System.out.println(document);
		}
	}
}
