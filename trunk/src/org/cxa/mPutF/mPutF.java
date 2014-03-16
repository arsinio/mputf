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

import org.cxa.commandLineParser.CommandLineParser;
import org.cxa.commandLineParser.optionListener.OptionWithArgumentListener;


/**
 * The main class for the Multi-Platform Unit-test Framework (mPutF)
 * 
 * @author Christopher Armenio
 */
public class mPutF
{	
	private static String configFilePath = "config.xml";
	private static String outputFilePath = "testResults.xml";
	
	/**
	 * The execution entry-point
	 * 
	 * @param optsIn command-line arguments/options
	 */
	public static void main(String[] optsIn) 
	{
		parseCmdLineOpts(optsIn);
		
		// we have a configuration file (either default OR specified)...try it
		try
		{
			TestSuite testSuite = TestSuite.parseTestSuite(new File(configFilePath), new File(outputFilePath));
			testSuite.runAllTests();
		}
		catch( Exception e )
		{
			System.err.println(String.format("Error: %s", e.getMessage()));
			System.exit(-2);
		}
	}
	
	
	private static void parseCmdLineOpts(String[] optsIn)
	{
		// create command line parser
		CommandLineParser clp = new CommandLineParser(mPutF.class.getSimpleName(),
						"Multi-platform unit-test framework (mPutF)\r\n" +
						"Program for running unit-tests on any executable type simply by analyzing program output (usually STDOUT and STDERR)");
		
		// add our options
		clp.addOption("c", "config", "path to xml configuration file", false, true, String.class, new OptionWithArgumentListener<String>()
		{
			@Override
			public void optionIsPresent(String argIn)
			{
				configFilePath = argIn;
			}
		});
		
		clp.addOption("o", "outputFile", "path to and name of output file (ex. tests/testResults.xml", false, true, String.class, new OptionWithArgumentListener<String>()
				{
			@Override
			public void optionIsPresent(String argIn)
			{
				outputFilePath = argIn;
			}
		});
		
		// parse our options
		if( !clp.parseOptions(optsIn) )
		{
			clp.printUsage();
			System.exit(-1);
		}
	}
}
