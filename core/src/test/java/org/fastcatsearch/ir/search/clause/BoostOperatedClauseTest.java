/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.search.clause;


import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.clause.BoostOperatedClause;
import org.fastcatsearch.ir.search.clause.UserOperatedClause;
import org.junit.Test;




public class BoostOperatedClauseTest {
	
	@Test
	public void __testFixed() throws IOException {
		int[] docs1 = new int[]{2,5,7};
		
		int[] docs2 = new int[]{3};
		
		UserOperatedClause c1 = new UserOperatedClause(docs1.length, docs1, null);
		UserOperatedClause c2 = new UserOperatedClause(docs2.length, docs2, null);
		
		RankInfo docInfo = new RankInfo();
		
		BoostOperatedClause orClause = new BoostOperatedClause(c1, c2);
		
		
		int i = 0;
		while(orClause.next(docInfo)){
			System.out.println((i+1)+" : "+docInfo.docNo());
			i++;
		}
	}
	
	@Test
	public void testRandom() throws IOException {
		int count1 = 1000;
		int[] docs1 = new int[count1];
		int[] weight1 = new int[count1];
		for(int i=0;i<count1;i++){
			docs1[i] = i;
		}
		Arrays.fill(weight1, 1000);
		
		int count2 = 1000;
		int[] docs2 = new int[count2];
		int[] weight2 = new int[count2];
		Arrays.fill(weight2, 10);
		
		int count = 0;
		for(int i=0;i<count2;i++){
			if(i % 10 == 0){
				docs2[count] = i;
				System.out.println(i+"] "+docs2[count]+" : " + weight2[count]);
				count++;
			}
		}
		System.out.println("------------------");
		System.out.println("boost count : "+count);
				
		UserOperatedClause c1 = new UserOperatedClause(count1, docs1, weight1);
		UserOperatedClause c2 = new UserOperatedClause(count, docs2, weight2);
		
		RankInfo docInfo = new RankInfo();
		
		BoostOperatedClause boostClause = new BoostOperatedClause(c1, c2);
		boostClause.init();
		
		int i = 0;
		while(boostClause.next(docInfo)){
			System.out.println((i+1)+" : "+docInfo);
			i++;
		}
	}
	
	private Random r = new Random(System.currentTimeMillis());
	
	private void makeDocs(int count, int[] docs){
		int d = 0;
		int prev = 0;
		for(int i=0; i<count;i++){
			d = r.nextInt(20);
			docs[i] = (prev + d);
			prev = docs[i];
		}
	}
}
