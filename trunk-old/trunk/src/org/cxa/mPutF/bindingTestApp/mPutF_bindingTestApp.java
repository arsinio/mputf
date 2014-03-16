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
package org.cxa.mPutF.bindingTestApp;

import org.cxa.commandLineParser.CommandLineParser;
import org.cxa.commandLineParser.optionListener.OptionNoArgumentListener;
import org.cxa.mPutF.bindings.mPutF;;

/**
 * @author Christopher Armenio
 *
 */
public class mPutF_bindingTestApp
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// create command line parser
		CommandLineParser clp = new CommandLineParser(mPutF_bindingTestApp.class.getSimpleName(),
						"mPutF Binding Test Application\r\n" +
						"Program for simulating the various outputs of the mPutF binding classes");
		
		// add our options
		clp.addOption("as", "assertSimple", "test fails with a simple assert (no line # or msg)", false, new OptionNoArgumentListener()
		{
			@Override
			public void optionIsPresent()
			{
				mPutF.assert_simple(false);
			}
		});
		
		clp.addOption("al", "assertLine", "test fails with an assert (line #, no msg)", false, new OptionNoArgumentListener()
		{
			@Override
			public void optionIsPresent()
			{
				mPutF.assert_line(false);
			}
		});
		
		clp.addOption("am", "assertMessage", "test fails with an assert (line # and msg)", false, new OptionNoArgumentListener()
		{
			@Override
			public void optionIsPresent()
			{
				mPutF.assert_msg(false, "this is an assert message");
			}
		});
		
		clp.addOption("p", "pass", "test passes", false, new OptionNoArgumentListener()
		{
			@Override
			public void optionIsPresent()
			{
				System.exit(0);
			}
		});
		
		clp.addOption("t", "timeout", "test fails by timing out (will run forever)", false, new OptionNoArgumentListener()
		{
			@Override
			public void optionIsPresent()
			{
				while(true)
				{
					Thread.yield();
				}
			}
		});
		
		clp.addOption("e", "exception", "test fails by generating an unhandled exception", false, new OptionNoArgumentListener()
		{
			@SuppressWarnings("null")
			@Override
			public void optionIsPresent()
			{
				String foo = null;
				foo.toString();
			}
		});
		
		// parse our options
		clp.parseOptions(args);
		
		// if we made it here, something was wrong...
		clp.printUsage();
		System.exit(-1);
	}

}
