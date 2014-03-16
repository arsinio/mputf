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
package org.cxa.mPutF;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cxa.mPutF.tests.Test;
import org.cxa.mPutF.tests.Test.TestResult;
import org.cxa.timeUtils.TimeDiff;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class which represents a container for a collection
 * of child {@link Test}s
 * 
 * @author Christopher Armenio
 */
public class TestSuite
{
	private static final String NODE_NAME = "name";
	
	
	private static Logger staticLogger = LogManager.getLogger("TestSuiteParser");
	
	
	private final String name;
	private final List<Test> tests;
	private final File outputFilePath;
	private Logger logger = null;
	
	
	private TestSuite(String nameIn, List<Test> testsIn, File outputFilePathIn)
	{
		this.name = nameIn;
		this.tests = testsIn;
		this.outputFilePath = outputFilePathIn;
		
		this.logger = LogManager.getLogger(String.format("%s::%s", this.getClass().getSimpleName(), this.name));
		this.logger.trace("test suite instance created");
	}
	
	
	/**
	 * Runs all contained tests and outputs the result to the XML file
	 * specified in the command-line arguments
	 * 
	 * @throws ParserConfigurationException on error creating XML output file
	 * @throws TransformerFactoryConfigurationError on error creating XML output file
	 * @throws TransformerException on error creating XML output file
	 */
	public void runAllTests() throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException
	{
		// create our xml file in memory and add our test suite info
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		doc.setXmlStandalone(true);
		
		Element elem_testSuite = doc.createElement("testsuite");
		elem_testSuite.setAttribute("name", this.name);
		elem_testSuite.setAttribute("tests", String.valueOf(this.tests.size()));
		
		// start a timer so we know how long ALL of the tests took
		TimeDiff td_totalElapsedTime = new TimeDiff();
		td_totalElapsedTime.setStartTime_now();
		
		// actually run each test
		this.logger.trace("starting runAllTests");
		int numErrors = 0;
		int numFailures = 0;
		for( Test currTest : this.tests )
		{
			currTest.runTest(doc, elem_testSuite);
			
			// record the number of errors and failures
			if( currTest.getTestResult() == TestResult.TEST_RESULT_ERROR ) numErrors++;
			else if( currTest.getTestResult() == TestResult.TEST_RESULT_FAILURE ) numFailures++;
		}
		this.logger.trace(String.format("runAllTests complete...saving results to '%s'", this.outputFilePath.getAbsolutePath()));
		
		// set our total elapsed time
		elem_testSuite.setAttribute("errors", String.valueOf(numErrors));
		elem_testSuite.setAttribute("failures", String.valueOf(numFailures));
		elem_testSuite.setAttribute("time", String.format("%.2f", ((float)td_totalElapsedTime.getElapsedTime_ms()) / 1000.0));
		elem_testSuite.setAttribute("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().getTime()).toString());
		doc.appendChild(elem_testSuite);
		
		// get ready to output our file
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		// make sure our output directory exists
		if( (this.outputFilePath.getParentFile() != null) && !this.outputFilePath.getParentFile().exists() ) this.outputFilePath.getParentFile().mkdirs();
		// actually output our file
		StreamResult result = new StreamResult(this.outputFilePath);
		transformer.transform(source, result);
		this.logger.trace("test results saved succesfully");
	}
	
	
	/**
	 * Parses a test suite and all child tests from the given XML file
	 * 
	 * @param xmlFileIn the XML file from which to parse tests
	 * @param outputFileIn the output file in which to store test outputs/results
	 * 
	 * @return a ready-to-run test suite
	 * @throws ParseException on error parsing the provided XML file
	 */
	public static TestSuite parseTestSuite(File xmlFileIn, File outputFileIn) throws ParseException
	{
		if( xmlFileIn == null ) throw new ParseException("target configuration file is null");
		
		staticLogger.trace(String.format("trying to open xmlFile '%s'", xmlFileIn.getAbsolutePath()));
		
		// try to parse our xml configuration file
		DocumentBuilder dBuilder;
		Document doc;
		try
		{
			dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = dBuilder.parse(xmlFileIn);
			doc.getDocumentElement().normalize();
		}
		catch(ParserConfigurationException |SAXException | IOException e)
		{
			throw new ParseException(xmlFileIn, e.getMessage());
		}
		
		// if we made it here, we successfully parse _some_ kind of XML...
		// see if we can make sense of it
		staticLogger.trace("file opened");
		
		// first, try to parse our test suite name
		NodeList suiteList = doc.getElementsByTagName("testSuite");
		if( suiteList.getLength() > 1 ) throw new ParseException(xmlFileIn, "only one test suite per file supported");
		else if( suiteList.getLength() == 0 ) throw new ParseException(xmlFileIn, "no test suite definition found");
		Node suiteName = suiteList.item(0).getAttributes().getNamedItem(NODE_NAME);
		if( suiteName == null ) throw new ParseException(xmlFileIn, String.format("missing test suite attribute'%s'", NODE_NAME));
		String strSuiteName = suiteName.getTextContent();
		staticLogger.trace(String.format("starting to parse tests for testSuite '%s'", strSuiteName));
		
		List<Test> tests = new ArrayList<Test>();
		NodeList nList = doc.getElementsByTagName("test");
		for( int i = 0; i < nList.getLength(); i++ )
		{
			// creating this test may throw a ParseException
			tests.add(Test.parseTest(xmlFileIn, nList.item(i)));
		}
		staticLogger.trace("all tests parsed successfully");
		
		// if we made it here, we successfully parsed our tests
		return new TestSuite(strSuiteName, tests, outputFileIn);
	}
}

