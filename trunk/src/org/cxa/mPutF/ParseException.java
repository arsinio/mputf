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

/**
 * An exception which occurrs whilst parsing tests/test suites
 * from the provided XML files
 * 
 * @author Christopher Armenio
 */
public class ParseException extends Exception
{
	private static final long serialVersionUID = 2799005877182461943L;


	/**
	 * Creates a ParseException with the specified error message
	 * 
	 * @param msgIn the error message
	 */
	public ParseException(String msgIn)
	{
		super(msgIn);
	}
	
	
	/**
	 * Creates a ParseException which occurred in the given file with the
	 * given error message.
	 * 
	 * @param fileLoc the file which caused this exception
	 * @param msgIn the error message
	 */
	public ParseException(File fileLoc, String msgIn)
	{
		super(String.format("while parsing '%s': %s", fileLoc.getName(), msgIn));
	}
	
	
	/**
	 * Creates a ParseException which occurred in the given file whilst 
	 * parsing the given test
	 * 
	 * @param fileLoc the file which caused this exception
	 * @param testNameIn the test in which this exception occurred
	 * @param msgIn the error message
	 */
	public ParseException(File fileLoc, String testNameIn, String msgIn)
	{
		super(String.format("in '%s::%s': %s", fileLoc.getName(), testNameIn, msgIn));
	}
}
