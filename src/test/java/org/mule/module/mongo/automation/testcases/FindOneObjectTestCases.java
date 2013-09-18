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

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;

import com.mongodb.DBObject;

public class FindOneObjectTestCases extends MongoTestParent {

	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		try {
			// create the collection
			testObjects = (HashMap<String, Object>) context.getBean("findOneObject");
			MessageProcessor flow = lookupMessageProcessorConstruct("create-collection");
			flow.process(getTestEvent(testObjects));
			
			// create the object
			// dbObject is modified in the insert-object flow
			flow = lookupMessageProcessorConstruct("insert-object");
			flow.process(getTestEvent(testObjects));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Category({ RegressionTests.class })
	@Test
	public void testFindOneObject() {
		try {
			DBObject dbObject = (DBObject) testObjects.get("dbObjectRef");
			
			MessageProcessor flow = lookupMessageProcessorConstruct("find-one-object");
			MuleEvent response = flow.process(getTestEvent(testObjects));
			
			// Get the retrieved DBObject
			// No MongoException means that it found a match (we are matching using ID)
			DBObject payload = (DBObject) response.getMessage().getPayload();
			assertNotNull(payload);
			assertTrue(payload.equals(dbObject));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@After
	public void tearDown() {
		try {
			// drop the collection
			MessageProcessor flow = lookupMessageProcessorConstruct("drop-collection");
			flow.process(getTestEvent(testObjects));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
