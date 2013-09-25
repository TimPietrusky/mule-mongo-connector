/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.mongo.automation.testcases;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;

public class GetFileContentUsingQueryMapTestCases extends MongoTestParent {
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		testObjects = (HashMap<String, Object>) context.getBean("getFileContentUsingQueryMap");
		
		createFileFromPayload(testObjects.get("filename1"));
		createFileFromPayload(testObjects.get("filename2"));
	}

	@After
	public void tearDown() {
		deleteFilesCreatedByCreateFileFromPayload();
	}

	@Category({ RegressionTests.class })
	@Test
	public void testGetFileContentUsingQueryMap() {
		try {
			MessageProcessor getFileContentFlow = lookupMessageProcessorConstruct("get-file-content-using-query-map");
			
			MuleEvent response = getFileContentFlow.process(getTestEvent(testObjects));
			
			assertNotNull(response.getMessage());
			assertNotNull(response.getMessage().getPayload());
			assertTrue(response.getMessage().getPayload() instanceof InputStream);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}

}
