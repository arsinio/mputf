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
package org.cxa.mPutF.tests.localProcess;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.cxa.mPutF.ParseException;
import org.cxa.mPutF.bindingOutputParser.BindingOutputParser;
import org.cxa.mPutF.bindingOutputParser.BindingOutputParser.Assertion;
import org.cxa.mPutF.bindings.mPutF;
import org.cxa.mPutF.tests.Test;
import org.cxa.timeUtils.TimeDiff;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the implementation of a {@link Test} subclass which
 * runs a separate executable/process and determines test results
 * based upon the output of said process (usually using mPutF bindings)
 * 
 * @author Christopher Armenio
 */
public class LocalProcessTest extends Test
{
	public static final String TEST_TYPE_STR = "localProcess";
	private static final String NODE_EXECUTABLE = "executable";
	
	
	protected static class TestOutput
	{
		final String stdout;
		final String stderr;
		final Integer retCode;
		final Exception e;
		
		protected TestOutput(int retCodeIn, String stdoutIn, String stderrIn)
		{
			this.e = null;
			this.retCode = retCodeIn;
			this.stdout = stdoutIn;
			this.stderr = stderrIn;
		}
		
		protected TestOutput(Exception eIn, String stdoutIn, String stderrIn)
		{
			this.e = eIn;
			this.retCode = null;
			this.stdout = stdoutIn;
			this.stderr = stderrIn;
		}
		
		protected TestOutput(Exception eIn)
		{
			this.e = eIn;
			this.retCode = null;
			this.stdout = null;
			this.stderr = null;
		}
	}
	
	
	private String executable = null;
	
	
	/**
	 * Creates a local process test from the given arguments
	 * 
	 * @param nameIn the name of this test
	 * @param descIn a user-friendly description of this test
	 * @param maxTestTime_msIn max time, in milliseconds, for which this test should run
	 * @param xmlFileIn the XML file from which this test was parsed
	 * @param optsNodeIn a {@link Node} which contains the test options XML node
	 * 
	 * @throws ParseException on error parsing the XML options for this test
	 */
	public LocalProcessTest(String nameIn, String descIn, Integer maxTestTime_msIn, File xmlFileIn, Node optsNodeIn) throws ParseException
	{
		super(nameIn, descIn, maxTestTime_msIn);
		
		// we _need_ an options node
		this.logger.trace("looking for options node");
		if( optsNodeIn == null ) throw new ParseException(xmlFileIn, this.getName(), "no test options specified");
		
		// if we made it here...we at least have an options section...check it out
		this.logger.trace("parsing options node");
		{
			// we have options...let's see if they make sense
			NodeList childNodes = optsNodeIn.getChildNodes();
			for( int i = 0; i < childNodes.getLength(); i++ )
			{
				Node currNode = childNodes.item(i);
				if( currNode.getNodeName().equals(NODE_EXECUTABLE) )
				{
					// parse the executable
					this.executable = currNode.getTextContent();
					this.logger.trace(String.format("parsed executable '%s'", this.executable));
				}
			}
		}
		
		// the only option we _need_ is the executable
		if( this.executable == null ) throw new ParseException(xmlFileIn, this.getName(), String.format("missing node '%s'", NODE_EXECUTABLE));
		
		this.logger.trace("test parsed successfully");
	}


	/*
	 * (non-Javadoc)
	 * @see org.cxa.mPutF.tests.Test#runTest(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void runTest(Document xmlDocIn, Element parentElementIn)
	{
		Element testXmlElement = xmlDocIn.createElement("testcase");
		testXmlElement.setAttribute("classname", "org.cxa.wtf");
		testXmlElement.setAttribute("name", this.getName());
		
		TestRunner tr = new TestRunner(this.executable);
		TimeDiff testTimer = new TimeDiff();
		TestOutput result = null;
		long testDuration_ms = 0;
		
		// start our test and see how it goes...
		this.logger.trace("starting test");
		tr.startTest();
		while(true)
		{
			if( !tr.isRunning() )
			{
				// test finished (didn't time out)...see if it was normal
				testDuration_ms = testTimer.getElapsedTime_ms();
				result = tr.getTestOutput();
				
				if( result.e != null )
				{
					this.logger.trace(String.format("exception occurred during test '%s'", result.e.getMessage()));
					this.testResult = TestResult.TEST_RESULT_ERROR;
					reportError(xmlDocIn, testXmlElement, "internalException", result.e.getMessage());
				}
				else
				{
					this.logger.trace("test finished of its own volition");
					switch(result.retCode)
					{
						case 0:
							// test passed
							this.logger.trace("test passed");
							break;
						
						case mPutF.EXIT_CODE:
							// test had an assert
							this.logger.trace("retCode matches assert");
							Assertion assertion = BindingOutputParser.parseAssertion(result.stderr);
							if( assertion == null )
							{
								this.testResult = TestResult.TEST_RESULT_ERROR;
								reportError(xmlDocIn, testXmlElement, "possibleAssertion", "retCode matches assertion, but unable to parse assertion output");
							}
							else
							{
								this.testResult = TestResult.TEST_RESULT_PASS;
								this.testResult = TestResult.TEST_RESULT_FAILURE;
								reportFailure(xmlDocIn, testXmlElement, "assertion", assertion.toString());
							}
							break;
						
						default:
							// unknown return code
							this.logger.trace("unknown retCode");
							this.testResult = TestResult.TEST_RESULT_ERROR;
							reportError(xmlDocIn, testXmlElement, "unknownRetCode", String.format("process exited with unknown retCode [%d]", result.retCode));
							break;
					}
				}
				break;
			}
			else if( this.getMaxTextTime_s() != null )
			{
				// test is still running...see if we timed out...
				if( testTimer.isElapsed(this.getMaxTextTime_s() * 1000, TimeUnit.MILLISECONDS) )
				{
					// test timed out...stop it
					this.logger.trace("test timed out...terminating");
					tr.stop();

					// try to parse our results
					testDuration_ms = this.getMaxTextTime_s() * 1000;
					result = tr.getTestOutput();
					this.testResult = TestResult.TEST_RESULT_ERROR;
					reportError(xmlDocIn, testXmlElement, "timeout", String.format("test did not complete within %d seconds", this.getMaxTextTime_s()));
					break;
				}
			}
		}
		
		// output our common stuff (stdout, etderr, status code, etc)
		if( (result.stdout != null) && !result.stdout.isEmpty() ) addStdout(xmlDocIn, testXmlElement, result.stdout);
		if( (result.stderr != null) && !result.stderr.isEmpty() ) addStderr(xmlDocIn, testXmlElement, result.stderr);
		//if( result.retCode != null ) testXmlElement.setAttribute("status", result.retCode.toString() );
		testXmlElement.setAttribute("time", String.format("%.2f", ((float)testDuration_ms) / 1000.0));
		
		parentElementIn.appendChild(testXmlElement);
		this.logger.trace("test complete");
	}
	
	
	private static void reportError(Document xmlDocIn, Element parentElementIn, String errorTypeIn, String msgIn)
	{
		Element errorNode = xmlDocIn.createElement("error");
		errorNode.setAttribute("type", errorTypeIn);
		errorNode.setAttribute("message", msgIn);
		
		parentElementIn.appendChild(errorNode);
	}
	
	
	private static void reportFailure(Document xmlDocIn, Element parentElementIn, String failureTypeIn, String msgIn)
	{
		Element failureNode = xmlDocIn.createElement("failure");
		failureNode.setAttribute("type", failureTypeIn);
		failureNode.setAttribute("message", msgIn);
		
		parentElementIn.appendChild(failureNode);
	}
	
	
	private static void addStdout(Document xmlDocIn, Element parentElementIn, String stdoutIn)
	{
		Element stdoutNode = xmlDocIn.createElement("system-out");
		stdoutNode.setTextContent(stdoutIn);
		
		parentElementIn.appendChild(stdoutNode);
	}
	
	
	private static void addStderr(Document xmlDocIn, Element parentElementIn, String stderrIn)
	{
		Element stderrNode = xmlDocIn.createElement("system-err");
		stderrNode.setTextContent(stderrIn);
		
		parentElementIn.appendChild(stderrNode);
	}
}

