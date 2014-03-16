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
package org.cxa.mPutF.bindings;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a utility class for asserting that various
 * conditions are met throughout the execution of a program.
 * 
 * @author Christopher Armenio
 */
public class mPutF
{
	public static final int EXIT_CODE = 84;
	public static final String ASSERT_TEXT = "**assert**";
	public static final String PREAMBLE_LOCATION = "loc: ";
	public static final String PREAMBLE_MESSAGE = "msg: ";
	
	private static List<AssertListener> assertListeners = new ArrayList<AssertListener>();
	
	
	/**
	 * Registers an assert listener to notify the user of an
	 * assertion (and an impending call to {@link System#exit(int)}
	 * 
	 * @param alIn the listener that should be notified
	 */
	public static void addAssertListener(AssertListener alIn)
	{
		assertListeners.add(alIn);
	}
	
	
	/**
	 * Asserts that the provided condition is true. If it isn't,
	 * simply prints "\r\n**assert**\r\n"
	 * 
	 * @param conditionIn the assertion condition (should be true)
	 */
	public static void assert_simple(boolean conditionIn)
	{
		System.err.printf("\r\n%s\r\n", ASSERT_TEXT);
		System.err.flush();
		System.exit(EXIT_CODE);
	}
	
	
	/**
	 * Asserts that the provided condition is true. If it isn't,
	 * prints "\r\n**assert**\r\nloc: <file>:<lineNum>\r\n" where
	 * <file> and <lineNum> is the filename and line number from 
	 * where this function was called
	 *
	 * @param conditionIn the assertion condition (should be true)
	 */
	public static void assert_line(boolean conditionIn)
	{
		System.err.printf("\r\n%s\r\n%s%s:%d\r\n",
				ASSERT_TEXT,
				PREAMBLE_LOCATION,
				Thread.currentThread().getStackTrace()[2].getFileName(),
				Thread.currentThread().getStackTrace()[2].getLineNumber());
		System.err.flush();
		System.exit(EXIT_CODE);
	}
	
	
	/**
	 * Asserts that the provided condition is true. If it isn't,
	 * prints "\r\n**assert**\r\nloc: <file>:<lineNum>\r\nmsg: <msg>\r\n" where
	 * <file> and <lineNum> is the filename and line number from 
	 * where this function was called, and <msg> is the second
	 * parameter to this function.
	 *
	 * @param conditionIn the assertion condition (should be true)
	 * @param msgIn the message that should be displayed
	 */
	public static void assert_msg(boolean conditionIn, String msgIn)
	{
		System.err.printf("\r\n%s\r\n%s%s:%d\r\n%s%s",
				ASSERT_TEXT,
				PREAMBLE_LOCATION,
				Thread.currentThread().getStackTrace()[2].getFileName(),
				Thread.currentThread().getStackTrace()[2].getLineNumber(),
				PREAMBLE_MESSAGE,
				msgIn);
		System.err.flush();
		System.exit(EXIT_CODE);
	}
}
