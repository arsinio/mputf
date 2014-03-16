/**
 * Copyright 2013 opencxa.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cxa.mPutF.tests;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cxa.mPutF.ParseException;
import org.cxa.mPutF.tests.localProcess.LocalProcessTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The base class for all mPutF tests
 * 
 * @author Christopher Armenio
 */
public abstract class Test
{
	private static final String NODE_NAME = "name";
	private static final String NODE_DESC = "description";
	private static final String NODE_TEST_TYPE = "testType";
	private static final String NODE_MAX_TEST_TIME = "maxTestTime_s";
	private static final String NODE_TEST_OPTIONS = "testOptions";
	
	private static Logger staticLogger = LogManager.getLogger("TestParser");
	
	
	public enum TestResult
	{
		TEST_RESULT_PASS,
		TEST_RESULT_FAILURE,
		TEST_RESULT_ERROR
	}
	
	
	private final String name;
	private final String description;
	private final Integer maxTestTime_s;
	protected TestResult testResult = null;
	protected Logger logger = null;
	
	
	protected Test(String nameIn, String descIn, Integer maxTestTime_sIn)
	{
		this.name = nameIn;
		this.description = descIn;
		this.maxTestTime_s = maxTestTime_sIn;
		
		this.logger = LogManager.getLogger(String.format("%s::%s", this.getClass().getSimpleName(), this.name));
		this.logger.trace("test instance created");
	}
	
	
	/**
	 * Returns the name of this test (as described in the XML configuration file)
	 * 
	 * @return the name of this test
	 */
	public String getName()
	{
		return this.name;
	}
	
	
	/**
	 * Returns the user-friendly description of this test (as described in the XML configuration file)
	 * 
	 * @return the description of this test
	 */
	public String getDescription()
	{
		return this.description;
	}
	
	
	/**
	 * Returns the maximum amount of time for which this test should run (as described in the XML configuration file)
	 * 
	 * @return the maximum time in milliseconds for which this test should run
	 */
	public Integer getMaxTextTime_s()
	{
		return this.maxTestTime_s;
	}
	
	
	/**
	 * Returns the result of the is test
	 * 
	 * @return the result of this test, or
	 * 		NULL if this test has not been run
	 */
	public TestResult getTestResult()
	{
		return this.testResult;
	}
	
	
	/**
	 * This is a blocking function, during which the test is completely executed
	 * and the result of which, are placed in the specified parent element of
	 * the specified XML document
	 * 
	 * @param xmlDocIn the XML document which can be used to create sub-elements 
	 * @param parentElementIn the parent which should host the node created by this test
	 */
	public abstract void runTest(Document xmlDocIn, Element parentElementIn);
	
	
	/**
	 * Parses a test from the given XML {@link Node}
	 * 
	 * @param xmlFileIn the XML file from which this test will be parsed
	 * @param testNodeIn the {@link Node} describing this test
	 * 
	 * @return a parsed test
	 * @throws ParseException on error parsing test
	 */
	public static Test parseTest(File xmlFileIn, Node testNodeIn) throws ParseException
	{
		String testName = null;
		String testDesc = null;
		String testType = null;
		String strTestMaxTime_s = null;
		Integer testMaxTime_s = null;
		Node testOptions = null;
		
		// get our attributes
		NamedNodeMap attributes = testNodeIn.getAttributes();
		testName = attributes.getNamedItem(NODE_NAME).getNodeValue();
		testDesc = attributes.getNamedItem(NODE_DESC).getNodeValue();
		testType = attributes.getNamedItem(NODE_TEST_TYPE).getNodeValue();
		try
		{
			strTestMaxTime_s = attributes.getNamedItem(NODE_MAX_TEST_TIME).getNodeValue();
			testMaxTime_s = (strTestMaxTime_s != null) ? Integer.parseInt(strTestMaxTime_s) : null;
		}
		catch(NumberFormatException e)
		{
			throw new ParseException(xmlFileIn, ((testName == null) ? "<unknownTest>" : testName),
					String.format("error parsing attribute '%s'::'%s'", NODE_MAX_TEST_TIME, strTestMaxTime_s));
		}
		
		staticLogger.trace(String.format("parsed test -- name:'%s'  testType:'%s'  maxTestTime_ms:'%d'", testName, testType, testMaxTime_s));
		
		
		// try to parse our options
		NodeList nList = testNodeIn.getChildNodes();
		for( int i = 0; i < nList.getLength(); i++ )
		{
			Node currNode = nList.item(i);
			if( currNode.getNodeName().equals(NODE_TEST_OPTIONS) )
			{
				if( testOptions != null ) throw new ParseException(xmlFileIn, ((testName == null) ? "<unknownTest>" : testName), "multiple options nodes detected");
				
				testOptions = currNode;
				staticLogger.trace(String.format("parsed test options for '%s'", ((testName == null) ? "<unknownTest>" : testName)));
			}
		}
		
		
		// make sure we got all of our required nodes
		if( testName == null )
		{
			throw new ParseException(xmlFileIn, "<unknownTest>", String.format("missing attribute '%s'", NODE_NAME));
		}
		else if( testDesc == null )
		{
			throw new ParseException(xmlFileIn, ((testName == null) ? "<unknownTest>" : testName),
					String.format("missing attribute '%s'", NODE_DESC));
		}
		if( testType == null )
		{
			throw new ParseException(xmlFileIn, ((testName == null) ? "<unknownTest>" : testName),
					String.format("missing attribute '%s'", NODE_TEST_TYPE));
		}
		
		// we have all of our required nodes...now create our test (if possible)
		Test retVal = null;
		if( testType.equals(LocalProcessTest.TEST_TYPE_STR) )
		{
			// note: this may throw a parse exception
			retVal = new LocalProcessTest(testName, testDesc, testMaxTime_s, xmlFileIn, testOptions);
		}
		else
		{
			// unknown test type
			throw new ParseException(xmlFileIn, testName, String.format("unknown test type '%s'", testType) );
		}
		
		return retVal;
	}
}
