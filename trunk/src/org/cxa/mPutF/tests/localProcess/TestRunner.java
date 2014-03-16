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

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cxa.mPutF.tests.localProcess.LocalProcessTest.TestOutput;


/**
 * This is a {@link Runnable} class which actually runs the
 * local process and returns any result
 * 
 * @author Christopher Armenio
 */
public class TestRunner implements Runnable
{		
	private final String executable;
	private final Thread runThread;
	
	private Logger logger = null;
	private TestOutput result = null;
	
	
	/**
	 * Initializes the test runner with the given exectuable
	 * 
	 * @param executableIn the executable command to execute. In general
	 * 		this command should contain the absolute path to an executable,
	 * 		followed by any command-line parameters (ex. /bin/foo -am -arg0 bar)
	 */
	public TestRunner(String executableIn)
	{
		this.executable = executableIn;
		this.runThread = new Thread(this);
		
		this.logger = LogManager.getLogger(this.getClass().getSimpleName());
	}
	
	
	/**
	 * Starts a thread that will run the process specified
	 * in the {@link #TestRunner(String)}
	 */
	public void startTest()
	{
		this.runThread.start();
	}
	
	
	/**
	 * Determines whether the thread/process is still running
	 * 
	 * @return true if the thread/process is still running, false
	 * 		if not
	 */
	public boolean isRunning()
	{
		return this.runThread.isAlive();
	}
	
	
	/**
	 * Forcefully causes the thread/process to terminate
	 */
	public void stop()
	{
		this.runThread.interrupt();
		try
		{
			this.runThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Returns the output of running this test
	 * 
	 * @return the output or NULL on error
	 */
	public TestOutput getTestOutput()
	{
		return this.result;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		TestOutput tmpResult = null;
		Process p = null;
		
		try
		{
			this.logger.trace("testing thread started...running executable");
			p = Runtime.getRuntime().exec(this.executable);
			this.logger.trace("executable started...waiting for finish");
			p.waitFor();
			this.logger.trace("executable finished");
			
			// read our streams and create our result
			tmpResult = new TestOutput(p.exitValue(), IOUtils.toString(p.getInputStream()), IOUtils.toString(p.getErrorStream()));
		}
		catch (Exception e)
		{
			this.logger.warn(String.format("error '%s'", e.getMessage()));
			
			// there was an error at some point...see if we can recover our streams
			String stdout = null;
			String stderr = null;
			
			try
			{
				if( p != null ) stdout = IOUtils.toString(p.getInputStream());
				if( p != null ) stderr = IOUtils.toString(p.getErrorStream());
			}
			catch( IOException e1 ) {}
			
			// save our exception for later processing
			tmpResult = new TestOutput(e, stdout, stderr);
		}
		
		// destroy the process if it is still lingering...
		if( p != null ) p.destroy();
		
		// save our result
		this.result = tmpResult;
		
		this.logger.trace("testing thread finished");
	}

}
