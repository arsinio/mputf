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
package org.cxa.mPutF.bindingOutputParser;

import org.cxa.mPutF.bindings.mPutF;

/**
 * A utility class for parsing the stdout/stderr outputs from the
 * mPutF bindings for results
 * 
 * @author Christopher Armenio
 */
public abstract class BindingOutputParser
{
	/**
	 * A class representing the output from a program utilizing
	 * {@ref org.cxa.mPutF.bindings.mPutF} for assertion reporting
	 * 
	 * @author Christopher Armenio
	 */
	public static class Assertion
	{
		private final String filePath;
		private final Integer lineNumber;
		private final String message;
		
		private Assertion(String filePathIn, Integer lineNumIn, String msgIn)
		{
			this.filePath = filePathIn;
			this.lineNumber = lineNumIn;
			this.message = msgIn;
		}
		
		
		/**
		 * If present, returns the file in which the assertion occurred
		 * 
		 * @return the file in which the assertion occurred,
		 * 		or NULL if not present
		 */
		public String getFile()
		{
			return this.filePath;
		}
		
		
		/**
		 * If present, returns the line number at which the assertion occurred
		 * 
		 * @return the line number at which the assertion occurred,
		 * 		or NULL if not present
		 */
		public Integer getLineNumber()
		{
			return this.lineNumber;
		}
		
		
		/**
		 * If present, returns the user-specified assert associated
		 * with this assertion
		 * 	
		 * @return the user-specified message, or NULL if not present
		 */
		public String getMessage()
		{
			return this.message;
		}
		
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			String location = null;
			if( (this.filePath != null) && (this.lineNumber == null) ) location = this.filePath;
			else if( (this.filePath != null) && (this.lineNumber != null) ) location = String.format("%s:%d", this.filePath, this.lineNumber);
			
			String retVal = "assert";
			if( (location == null) && (this.message != null) ) retVal = this.message;
			else if( (location != null) && (this.message == null) ) retVal = location;
			else if( (location != null) && (this.message != null) ) retVal = String.format("%s - %s", location, this.message);
			
			return retVal;
		}
	}
	
	
	/**
	 * Parses the first assertion detected in the stderr output
	 * 
	 * @param stderrIn the stderr output
	 * 
	 * @return the first assertion detected, or
	 * 		NULL if no assertion was detected.
	 */
	public static Assertion parseAssertion(String stderrIn)
	{
		if( stderrIn == null ) return null;
		
		int assertIndex = stderrIn.indexOf(mPutF.ASSERT_TEXT);
		if( assertIndex == -1 ) return null;
		
		String fileName = null;
		Integer lineNumber = null;
		String message = null;
		
		// we have an assert...figure out what happened
		int assertFollowupIndex = assertIndex + mPutF.ASSERT_TEXT.length() + 2;			// 2 -> crlf
		if( assertFollowupIndex >= stderrIn.length() ) return new Assertion(null, null, null);
		String lines[] = stderrIn.substring(assertFollowupIndex).split("\r\n");
		for( String currLine : lines )
		{
			System.out.printf("currLine: '%s'\r\n", currLine);
			
			if( currLine.startsWith(mPutF.PREAMBLE_LOCATION) )
			{
				// we do have a location...try to parse it...
				int lastColonIndex = currLine.lastIndexOf(":");
				if( lastColonIndex == -1 ) continue;
				fileName = currLine.substring(mPutF.PREAMBLE_LOCATION.length(), lastColonIndex);
				if( lastColonIndex+1 >= currLine.length() ) continue;
				String strLineNumber = currLine.substring(lastColonIndex+1);
				try
				{
					lineNumber = Integer.parseInt(strLineNumber);
					if( lineNumber == null ) continue;
				}
				catch( NumberFormatException e ) { continue; }
			}
			else if( currLine.startsWith(mPutF.PREAMBLE_MESSAGE) )
			{
				// we do have a message...this one is pretty easy to parse
				message = currLine.substring(mPutF.PREAMBLE_MESSAGE.length());
			}
			else
			{
				// unknown line...stop parsing
				break;
			}
		}
		
		System.out.printf("fileName: '%s'  lineNumber: '%d'  message: '%s'\r\n", fileName, lineNumber, message);
		
		// we're done parsing our lines...create our assertion
		return new Assertion(fileName, lineNumber, message);
	}
}
