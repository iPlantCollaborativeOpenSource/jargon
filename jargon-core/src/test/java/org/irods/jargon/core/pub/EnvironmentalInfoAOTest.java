/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.pub.domain.RemoteCommandInformation;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class EnvironmentalInfoAOTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetEnvironmentalAO() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties irodsServerProperties = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();
		Assert.assertNotNull(irodsServerProperties);
		irodsSession.closeSession();
	}

	@Test
	public void testGetIRODSServerCurrentTime() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);
		long timeVal = environmentalInfoAO.getIRODSServerCurrentTime();
		Assert.assertTrue("time val was missing", timeVal > 0);
	}

	@Test
	public void testShowLoadedRules() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);
		String ruleVal = environmentalInfoAO.showLoadedRules();
		Assert.assertNotNull("rule data missing", ruleVal);
	}

	@Test
	public void testShowLoadedRulesTriggerCache() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);
		String ruleVal = environmentalInfoAO.showLoadedRules();
		ruleVal = environmentalInfoAO.showLoadedRules();
		Assert.assertNotNull("rule data missing", ruleVal);
	}

	@Test
	public void testIsStrictACLs() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);
		environmentalInfoAO.isStrictACLs();
		// so as not to bias the test (difficult right now to set up as a test
		// case) just see if the method works without error...
		Assert.assertTrue(true);
	}

	/**
	 * Note that this test depends on cmd-scripts/listCommands.sh to be
	 * installed in the target server/bin/cmd directory. If this is not the
	 * case, the test will just silently ignore the error.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListAvailableRemoteCommands() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);

		try {
			List<RemoteCommandInformation> remoteCommands = environmentalInfoAO
					.listAvailableRemoteCommands();
			Assert.assertTrue("did not find any commands",
					remoteCommands.size() > 0);
			RemoteCommandInformation information = remoteCommands.get(0);
			Assert.assertEquals("wrong host", irodsAccount.getHost(),
					information.getHostName());
			Assert.assertEquals("wrong zone", irodsAccount.getZone(),
					information.getZone());
			Assert.assertTrue("no command name", information.getCommand()
					.length() > 0);
		} catch (DataNotFoundException ex) {
			System.out
					.println("for now, ignoring error as listCommands.sh is unavailable in the remote commands dir");
		}

	}

	@Test
	public void testListAvailableMicroservices() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);

		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		} else if (props.isEirods()) {
			return;
		}

		List<String> microservices = environmentalInfoAO
				.listAvailableMicroservices();

		Assert.assertTrue("did not find any microservices",
				microservices.size() > 0);

	}
}
