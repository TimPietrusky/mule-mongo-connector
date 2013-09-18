/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.mongo.automation.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;

import com.mongodb.BasicDBObject;

public class CountObjectsTestCases extends MongoTestParent {

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		try {
			// Create collection
			testObjects = (HashMap<String, Object>) context.getBean("countObjects");
			lookupMessageProcessorConstruct("create-collection").process(getTestEvent(testObjects));
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
	}

	@After
	public void tearDown() {
		try {
			// Delete collection
			lookupMessageProcessorConstruct("drop-collection").process(getTestEvent(testObjects));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Category({ RegressionTests.class })
	@Test
	public void testCountObjects() {
		
		int numObjects = (Integer) testObjects.get("numObjects");
		
		insertObjects(getEmptyDBObjects(numObjects));
		
		MuleEvent response = null;
		try {
			MessageProcessor countFlow = lookupMessageProcessorConstruct("count-objects");
			testObjects.put("queryRef", new BasicDBObject());
			response = countFlow.process(getTestEvent(testObjects));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(new Long(numObjects), response.getMessage().getPayload());
	}
	
}
