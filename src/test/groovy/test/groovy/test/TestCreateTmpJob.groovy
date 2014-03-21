package test.groovy.test

import javax.inject.Inject;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
 
import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.junit.runner.JUnitCore;
import test.groovy.common.TestUTCommon;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestCreateTmpJob {
	static final String BIN_HOME = "/home/thanhmx/GitHub/maixuanthanh/wiperdog/bin"
    public TestCreateTmpJob() {}

	@Inject
	private org.osgi.framework.BundleContext context;
	String wd = System.getProperty("user.dir");
    @Configuration
    public Option[] config() {
        return options(
			// felix log level
			systemProperty("felix.log.level").value("4"), // 4 = DEBUG
			// setup properties for fileinstall bundle.
			systemProperty("felix.home").value(wd),
			// Pax-exam make this test code into OSGi bundle at runtime, so 
			// we need "groovy-all" bundle to use this groovy test code.
            mavenBundle("org.codehaus.groovy", "groovy-all", "2.2.1"),
            junitBundles()
            );
    }
	String result;
	String expected;
	String strCmd;
	String message;
	String strTool;
	String dirDefault;
	def listCmd;
	File dir;
	TestUTCommon testCommon = new TestUTCommon();
	
	@Before
	public void prepare() {
		result = "";
		expected = "";
		strCmd = "";
		message = "";
		strTool = "";
		dirDefault = wd + "/src/test/groovy/test/groovy/main";
		listCmd = []
		dir = new File(wd)
		if(System.getProperty("os.name").contains("Win")) {
			strTool = "/genjob.bat"
		} else {
			strTool = "/genjob.sh"
		}
		// get directory for test
		if (new File(BIN_HOME + strTool).exists()) {
			// set permisson for test
			if(System.getProperty("os.name").contains("Linux")) {
				// set permission pax-exam test
				String strSetPermission = "chmod -R 755 " + wd
				testCommon.runCmd(strSetPermission)
				// set permission wiperdog/bin
				strSetPermission = "chmod -R 755 " + BIN_HOME
				testCommon.runCmd(strSetPermission)
			}
			// Test tool in the folder GitHub
			println "Start test create job in the folder GitHub: " + BIN_HOME
			strCmd = BIN_HOME + strTool;
		} else {
			// Test tool in the folder default
			println "Can not find the folder GitHub. Start test create job in the folder default: " + dirDefault
			strCmd = dirDefault + strTool;
		}
	}

	@After
	public void finish() {
	}
	
	//===============Check create Job in the folder to run batch/bash file when run command with option "-n" ==================
	/**
	 * Run command contains one option with data is not empty.
	 * JobName does not exist.
	 * Command: genjob -n TestJob01
	 * Expected: 
	 * 		- file TestJob01.job will be created in the folder to run batch/bash file.
	 *		- contents of the keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_01() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob01")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob01.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		//assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob01.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob01.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob01.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains one option with data is empty.
	 * Command: genjob -n ""
	 * Expected: can not create job file in the folder to run batch/bash file.
	 */
	@Test
	public void create_tmp_job_02() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob02.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("Incorrect format"))
		assertFalse(new File(wd + "/TestJob02.job").exists())
	}	
	
	/**
	 * Run command contains one option and does not contains data.
	 * Command: genjob -n
	 * Expected: can not create job file in the folder to run batch/bash file.
	 */
	@Test
	public void create_tmp_job_03() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob03.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("Incorrect format"))
		assertFalse(new File(wd + "/TestJob03.job").exists())
	}
	
	/**
	 * Run command with other option "-n"
	 * Command: genjob --abc
	 * Expected: can not create job file in the folder to run batch/bash file.
	 */
	@Test
	public void create_tmp_job_04() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-abc")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob04.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("Incorrect format"))
		assertFalse(new File(wd + "/TestJob04.job").exists())
	}
	
	//==============Check create Job in the folder to run batch/bash file when run command with more than 2 options=================
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with data is not empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob05 -f "testFetchaction" 
	 * Expected: 
	 * 		- file TestJob05.job will be created in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION is not comment out with data is "testFetchaction".
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_05() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob05")
		listCmd.add("-f")
		listCmd.add("testFetchaction")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob05.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob05.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob05.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob05.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with data is empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob06 -f "" 
	 * Expected: 
	 * 		- file TestJob06.job will be created in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION is not comment out with data is empty.
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_06() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob06")
		listCmd.add("-f")
		listCmd.add("")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob06.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob06.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob06.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob06.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with does not contains data
	 * JobName does not exist.
	 * Command: genjob -n TestJob07 -f
	 * Expected: 
	 *		- file TestJob07.job will be created in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION is not comment out with data is default.
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_07() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob07")
		listCmd.add("-f")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob07.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob07.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob07.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob07.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -q with data is not empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob08 -q "testQuery"
	 * Expected: 
	 * 		- file TestJob08.job will be created in the folder to run batch/bash file.
	 * 		- contents of the QUERY is not comment out with data is "testQuery".
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_08() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob08")
		listCmd.add("-q")
		listCmd.add("testQuery")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob08.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob08.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob08.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob08.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -q with data is empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob09 -q ""
	 * Expected: 
	 * 		- file TestJob09.job will be created in the folder to run batch/bash file.
	 * 		- contents of the QUERY is not comment out with data is empty.
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_09() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob09")
		listCmd.add("-q")
		listCmd.add("")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob09.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob09.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob09.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob09.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -q with does not contains data
	 * JobName does not exist.
	 * Command: genjob -n TestJob10 -q
	 * Expected: 
	 * 		- file TestJob10.job will be created in the folder to run batch/bash file.
	 * 		- contents of the QUERY is not comment out with data is default.
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_10() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob10")
		listCmd.add("-q")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob10.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob10.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob10.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob10.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -c with data is not empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob11 -c "testCommand"
	 * Expected: 
	 * 		- file TestJob11.job will be created in the folder to run batch/bash file.
	 * 		- contents of the COMMAND is not comment out with data is "testCommand".
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_11() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob11")
		listCmd.add("-c")
		listCmd.add("testCommand")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob11.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob11.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob11.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob11.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -c with data is empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob12 -c ""
	 * Expected: 
	 * 		- file TestJob12.job will be created in the folder to run batch/bash file.
	 * 		- contents of the COMMAND is not comment out with data is empty.
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_12() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob12")
		listCmd.add("-c")
		listCmd.add("")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob12.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob12.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob12.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob12.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -c with does not contains data
	 * JobName does not exist.
	 * Command: genjob -n TestJob13 -c
	 * Expected: 
	 * 		- file TestJob13.job will be created in the folder to run batch/bash file.
	 * 		- contents of the COMMAND is not comment out with data is default.
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_13() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob13")
		listCmd.add("-c")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob13.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob13.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob13.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob13.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -d with data is not empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob14 -d "testDbexec"
	 * Expected: 
	 * 		- file TestJob14.job will be created in the folder to run batch/bash file.
	 * 		- contents of the DBEXEC is not comment out with data is "testDbexec". 
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_14() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob14")
		listCmd.add("-d")
		listCmd.add("testDbexec")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob14.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob14.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob14.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob14.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -d with data is empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob15 -d ""
	 * Expected: 
	 * 		- file TestJob15.job will be created in the folder to run batch/bash file.
	 * 		- contents of the DBEXEC is not comment out with data is empty.
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_15() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob15")
		listCmd.add("-d")
		listCmd.add("")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob15.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob15.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob15.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob15.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -d with does not contains data
	 * JobName does not exist.
	 * Command: genjob -n TestJob16 -d
	 * Expected: 
	 * 		- file TestJob16.job will be created in the folder to run batch/bash file.
	 * 		- contents of the DBEXEC is not comment out with data is default.
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_16() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob16")
		listCmd.add("-d")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob16.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob16.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob16.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob16.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option does not contains "-f" or "-q" or "-c" or "-d"
	 * JobName does not exist.
	 * Command: genjob -n TestJob17 -aaa "not exist"
	 * Expected: 
	 *		- file TestJob17.job will be created in the folder to run batch/bash file.
	 *		- contents of the keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_17() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob17")
		listCmd.add("-aaa")
		listCmd.add("not exist")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob17.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob17.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob17.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob17.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 3 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with data is not empty
	 *   - the third option is -q with data is not empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob18 -f "testFetchaction" -q "testQuery"
	 * Expected: 
	 * 		- file TestJob18.job will be created in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION, QUERY is not comment out with data correspond is "testFetchaction" and "testQuery"
	 *		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_18() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob18")
		listCmd.add("-f")
		listCmd.add("testFetchaction")
		listCmd.add("-q")
		listCmd.add("testQuery")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob18.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob18.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob18.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob18.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 4 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with data is not empty
	 *   - the third option is -q with data is not empty
	 *   - the fourth option is -c with data is not empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob19 -f "testFetchaction" -q "testQuery" -c "testCommand"
	 * Expected: 
	 * 		- file TestJob19.job will be created in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION, QUERY, COMMAND is not comment out with data correspond is "testFetchaction", "testQuery" and "testCommand".
	 *		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_19() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob19")
		listCmd.add("-f")
		listCmd.add("testFetchaction")
		listCmd.add("-q")
		listCmd.add("testQuery")
		listCmd.add("-c")
		listCmd.add("testCommand")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob19.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob19.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob19.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob19.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 5 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with data is not empty
	 *   - the third option is -q with data is not empty
	 *   - the fourth option is -c with data is not empty
	 *   - the fifth option is -d with data is not empty
	 * JobName does not exist.
	 * Command: genjob -n TestJob20 -f "testFetchaction" -q "testQuery" -c "testCommand" -d "testDbexec"
	 * Expected: 
	 * 		- file TestJob20.job will be created in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION, QUERY, COMMAND, DBEXEC is not comment out with data correspond is "testFetchaction", "testQuery", "testCommand" and "testDbexec".
	 * 		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_20() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob20")
		listCmd.add("-f")
		listCmd.add("testFetchaction")
		listCmd.add("-q")
		listCmd.add("testQuery")
		listCmd.add("-c")
		listCmd.add("testCommand")
		listCmd.add("-d")
		listCmd.add("testDbexec")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob20.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob20.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob20.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob20.job")).text
		assertEquals(expected, result)
	}
	
	//============================Check update data to Job has existed=====================================
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with new data
	 * JobName has exist and data of FETCHACTION is "testFetchaction"
	 * Command: genjob -n TestJob21 -f "testFetchactionNew"
	 * Expected: 
	 * 		- file TestJob21.job will be update in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION is not comment out with new data is "testFetchactionNew".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_21() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob21")
		listCmd.add("-f")
		listCmd.add("testFetchactionNew")
		// create job file for test if not exists
		testCommon.createJobTest(wd + "/src/test/resources/data_test/input/TestJob21.job", wd + "/TestJob21.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("UPDATED"))
		assertTrue(new File(wd + "/TestJob21.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob21.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob21.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -q with new data
	 * JobName has exist and data of QUERY is "testQuery"
	 * Command: genjob -n TestJob22 -q "testQueryNew"
	 * Expected: 
	 * 		- file TestJob22.job will be update in the folder to run batch/bash file.
	 * 		- contents of the QUERY is not comment out with new data is "testQueryNew".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_22() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob22")
		listCmd.add("-q")
		listCmd.add("testQueryNew")
		// create job file for test if not exists
		testCommon.createJobTest(wd + "/src/test/resources/data_test/input/TestJob22.job", wd + "/TestJob22.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("UPDATED"))
		assertTrue(new File(wd + "/TestJob22.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob22.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob22.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -c with new data
	 * JobName has exist and data of COMMAND is "testCommand".
	 * Command: genjob -n TestJob23 -c "testCommandNew"
	 * Expected: 
	 * 		- file TestJob23.job will be update in the folder to run batch/bash file.
	 * 		- contents of the COMMAND is not comment out with new data is "testCommandNew".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_23() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob23")
		listCmd.add("-c")
		listCmd.add("testCommandNew")
		// create job file for test if not exists
		testCommon.createJobTest(wd + "/src/test/resources/data_test/input/TestJob23.job", wd + "/TestJob23.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("UPDATED"))
		assertTrue(new File(wd + "/TestJob23.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob23.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob23.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -d with new data
	 * JobName has exist and data of DBEXEC is "testDbexec".
	 * Command: genjob -n TestJob24 -d "testDbexecNew"
	 * Expected: 
	 * 		- file TestJob24.job will be update in the folder to run batch/bash file.
	 * 		- contents of the DBEXEC is not comment out with new data is "testDbexecNew". 
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_24() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob24")
		listCmd.add("-d")
		listCmd.add("testDbexecNew")
		// create job file for test if not exists
		testCommon.createJobTest(wd + "/src/test/resources/data_test/input/TestJob24.job", wd + "/TestJob24.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("UPDATED"))
		assertTrue(new File(wd + "/TestJob24.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob24.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob24.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 3 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with new data
	 *   - the third option is -q with new data
	 * JobName has exist and data of FETCHACTION is "testFetchaction", contents of the key QUERY is comment out. 
	 * Command: genjob -n TestJob25 -f "testFetchactionNew" -q "testQueryNew"
	 * Expected: 
	 * 		- file TestJob25.job will be update in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION, QUERY is not comment out with new data correspond is "testFetchactionNew", "testQueryNew". 
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_25() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob25")
		listCmd.add("-f")
		listCmd.add("testFetchactionNew")
		listCmd.add("-q")
		listCmd.add("testQueryNew")
		// create job file for test if not exists
		testCommon.createJobTest(wd + "/src/test/resources/data_test/input/TestJob25.job", wd + "/TestJob25.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("UPDATED"))
		assertTrue(new File(wd + "/TestJob25.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob25.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob25.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 4 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with new data
	 *   - the third option is -q with new data
	 *   - the fourth option is -c with new data
	 * JobName has exist and data of FETCHACTION is "testFetchaction", contents of the key QUERY is comment out, 
	 * 		data of COMMAND is "testCommand".
	 * Command: genjob -n TestJob26 -f "testFetchactionNew" -q "testQueryNew" -c "testCommandNew"
	 * Expected:
	 * 		- file TestJob26.job will be update in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION, QUERY, COMMAND is not comment out with new data correspond is "testFetchactionNew", "testQueryNew", "testCommandNew".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_26() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob26")
		listCmd.add("-f")
		listCmd.add("testFetchactionNew")
		listCmd.add("-q")
		listCmd.add("testQueryNew")
		listCmd.add("-c")
		listCmd.add("testCommandNew")
		// create job file for test if not exists
		testCommon.createJobTest(wd + "/src/test/resources/data_test/input/TestJob26.job", wd + "/TestJob26.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("UPDATED"))
		assertTrue(new File(wd + "/TestJob26.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob26.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob26.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 5 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with new data
	 *   - the third option is -q with new data
	 *   - the fourth option is -c with new data
	 *   - the fifth option is -d with new data
	 * JobName has exist and data of FETCHACTION is "testFetchaction", contents of the key QUERY is comment out, 
	 * 		data of COMMAND is "testCommand", contents of the key DBEXEC is comment out.
	 * Command: genjob -n TestJob27 -f "testFetchactionNew" -q "testQueryNew" -c "testCommandNew" -d "testDbexecNew"
	 * Expected:
	 * 		- file TestJob27.job will be update in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION, QUERY, COMMAND, DBEXEC is not comment out with new data correspond is "testFetchactionNew", "testQueryNew", "testCommandNew", "testDbexecNew".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_27() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob27")
		listCmd.add("-f")
		listCmd.add("testFetchactionNew")
		listCmd.add("-q")
		listCmd.add("testQueryNew")
		listCmd.add("-c")
		listCmd.add("testCommandNew")
		listCmd.add("-d")
		listCmd.add("testDbexecNew")
		// create job file for test if not exists
		testCommon.createJobTest(wd + "/src/test/resources/data_test/input/TestJob27.job", wd + "/TestJob27.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("UPDATED"))
		assertTrue(new File(wd + "/TestJob27.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob27.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob27.job")).text
		assertEquals(expected, result)
	}

	/**
	 * Run command contains 5 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with no data
	 *   - the third option is -q with new data
	 *   - the fourth option is -c with new data
	 *   - the fifth option is -d with no data
	 * JobName has exist and data of FETCHACTION is "testFetchaction", contents of the key QUERY is comment out,
	 * 		data of COMMAND is "testCommand", contents of the key DBEXEC is comment out.
	 * Command: genjob -n TestJob -f -q "testQueryNew" -c "testCommandNew" -d
	 * Expected:
	 * 		- file TestJob.job will be update in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION, DBEXEC is not comment out with new data correspond is default.
	 * 		- contents of the QUERY, COMMAND is not comment out with new data correspond is "testQueryNew", "testCommandNew".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_28() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob28")
		listCmd.add("-f")
		listCmd.add("-q")
		listCmd.add("testQueryNew")
		listCmd.add("-c")
		listCmd.add("testCommandNew")
		listCmd.add("-d")
		// create job file for test if not exists
		testCommon.createJobTest(wd + "/src/test/resources/data_test/input/TestJob28.job", wd + "/TestJob28.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("UPDATED"))
		assertTrue(new File(wd + "/TestJob28.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob28.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob28.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command does not contains option.
	 * Command: genjob
	 * Expected: can not create job file in the folder to run batch/bash file.
	 */
	@Test
	public void create_tmp_job_29() {		
		// run command
		listCmd.add(strCmd)
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("Incorrect format"))
		assertFalse(new File(wd + "/TestJob29.job").exists())
	}
	
	//==============Check create Job in the folder correspond to data of option "-fp"=================
	/**
	 * Run command contains option "-fp":
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with data is not empty
	 *   - the third option is -q with data is not empty
	 *   - the fourth option is -fp with path to write file is exist
	 * JobName does not exist.
	 * Command: genjob -n TestJob30 -f "testFetchaction" -q -fp "/path_write_file_exist/"
	 * Expected: 
	 * 		- file TestJob30.job will be created in the folder correspond to data of "-fp".
	 * 		- contents of the FETCHACTION is not comment out with data correspond is "testFetchaction".
	 * 		- contents of the QUERY is not comment out with data correspond is default.
	 *		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_30() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob30")
		listCmd.add("-f")
		listCmd.add("testFetchaction")
		listCmd.add("-q")
		listCmd.add("-fp")
		listCmd.add(wd + "/src/test/resources/data_test/output")
		// remove job file if exists
		testCommon.cleanData(wd + "/src/test/resources/data_test/output/TestJob30.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/src/test/resources/data_test/output/TestJob30.job").exists())
		// get data output of function
		result = (new File(wd + "/src/test/resources/data_test/output/TestJob30.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob30.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains option "-fp":
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with data is not empty
	 *   - the third option is -q with data is not empty
	 *   - the fourth option is -fp with path to write file does not exist
	 * JobName does not exist.
	 * Command: genjob -n TestJob31 -f "testFetchaction" -q -fp "/path_write_file_not_exist/"
	 * Expected: file TestJob31.job can not create.
	 */
	@Test
	public void create_tmp_job_31() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob31")
		listCmd.add("-f")
		listCmd.add("testFetchaction")
		listCmd.add("-q")
		listCmd.add("-fp")
		listCmd.add("/path/to/write/file/not/exist")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CAN NOT CREATE JOB"))
		assertFalse(new File("/path/to/write/file/not/exist/TestJob31.job").exists())
	}
	
	/**
	 * Run command contains option "-fp":
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with data is not empty
	 *   - the third option is -q with data is not empty
	 *   - the fourth option is -fp with no data
	 * JobName does not exist.
	 * Command: genjob -n TestJob32 -f "testFetchaction" -q -fp
	 * Expected:
	 * 		- file TestJob32.job will be created in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION is not comment out with data correspond is "testFetchaction".
	 * 		- contents of the QUERY is not comment out with data correspond is default.
	 *		- contents of the other keys into job is comment out.
	 */
	@Test
	public void create_tmp_job_32() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob32")
		listCmd.add("-f")
		listCmd.add("testFetchaction")
		listCmd.add("-q")
		listCmd.add("-fp")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob32.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob32.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob32.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob32.job")).text
		assertEquals(expected, result)
	}
	
	//============================Check create Job with data contains space=====================================
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with new data
	 * JobName does not exist.
	 * Command: genjob -n TestJob33 -f "test Fetchaction New"
	 * Expected:
	 * 		- file TestJob33.job will be update in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION is not comment out with new data is "test Fetchaction New".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_33() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob33")
		listCmd.add("-f")
		listCmd.add("test Fetchaction New")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob33.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob33.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob33.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob33.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -q with new data
	 * JobName does not exist.
	 * Command: genjob -n TestJob34 -q "test Query New"
	 * Expected:
	 * 		- file TestJob34.job will be update in the folder to run batch/bash file.
	 * 		- contents of the QUERY is not comment out with new data is "test Query New".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_34() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob34")
		listCmd.add("-q")
		listCmd.add("test Query New")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob34.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob34.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob34.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob34.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -c with new data
	 * JobName does not exist.
	 * Command: genjob -n TestJob35 -c "test Command New"
	 * Expected:
	 * 		- file TestJob35.job will be update in the folder to run batch/bash file.
	 * 		- contents of the COMMAND is not comment out with new data is "test Command New".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_35() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob35")
		listCmd.add("-c")
		listCmd.add("test Command New")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob35.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob35.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob35.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob35.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 2 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -d with new data
	 * JobName does not exist.
	 * Command: genjob -n TestJob36 -d "test Dbexec New"
	 * Expected:
	 * 		- file TestJob36.job will be update in the folder to run batch/bash file.
	 * 		- contents of the DBEXEC is not comment out with new data is "test Dbexec New".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_36() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob36")
		listCmd.add("-d")
		listCmd.add("test Dbexec New")
		// remove job file if exists
		testCommon.cleanData(wd + "/TestJob36.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/TestJob36.job").exists())
		// get data output of function
		result = (new File(wd + "/TestJob36.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob36.job")).text
		assertEquals(expected, result)
	}
	
	/**
	 * Run command contains 5 option:
	 *   - the first option is -n with data is not empty
	 *   - the second option is -f with new data
	 *   - the third option is -q with new data
	 *   - the fourth option is -c with new data
	 *   - the fifth option is -d with new data
	 *   - the sixth option is -fp with path to write file is exist
	 * JobName does not exist.
	 * Command: genjob -n TestJob37 -f "test Fetchaction New" -q "test Query New" -c "test Command New" -d "test Dbexec New" -fd "/path_to_write_file"
	 * Expected:
	 * 		- file TestJob37.job will be update in the folder to run batch/bash file.
	 * 		- contents of the FETCHACTION, QUERY, COMMAND, DBEXEC is not comment out with new data correspond is "test Fetchaction New", "test Query New", "test Command New", "test Dbexec New".
	 * 		- contents of the other keys does not changed.
	 */
	@Test
	public void create_tmp_job_37() {
		// set list command for create job
		listCmd.add(strCmd)
		listCmd.add("-n")
		listCmd.add("TestJob37")
		listCmd.add("-f")
		listCmd.add("test Fetchaction New")
		listCmd.add("-q")
		listCmd.add("test Query New")
		listCmd.add("-c")
		listCmd.add("test Command New")
		listCmd.add("-d")
		listCmd.add("test Dbexec New")
		listCmd.add("-fp")
		listCmd.add(wd + "/src/test/resources/data_test/output")
		// remove job file if exists
		testCommon.cleanData(wd + "/src/test/resources/data_test/output/TestJob37.job")
		// run command
		message = testCommon.runProcClosure(listCmd, dir, true)
		assertTrue(message.contains("CREATED"))
		assertTrue(new File(wd + "/src/test/resources/data_test/output/TestJob37.job").exists())
		// get data output of function
		result = (new File(wd + "/src/test/resources/data_test/output/TestJob37.job")).text
		// get data expected to compare
		expected = (new File(wd + "/src/test/resources/data_test/expected/TestJob37.job")).text
		assertEquals(expected, result)
	}
}