package org.irods.jargon.core.unittest;

import org.irods.jargon.core.query.IRODSGenQueryTest;
import org.irods.jargon.core.query.IRODSGenQueryTranslatorTest;
import org.irods.jargon.core.query.TranslatedIRODSQueryTest;
import org.irods.jargon.core.remoteexecute.RemoteExecuteServiceImplTest;
import org.irods.jargon.core.security.IRODSPasswordUtilitiesTest;
import org.irods.jargon.core.utils.IRODSUriUtilsTest;
import org.irods.jargon.core.utils.LocalFileUtilsTest;
import org.irods.jargon.core.utils.MiscIRODSUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ConnectionTests.class, IRODSGenQueryTest.class,
		IRODSGenQueryTranslatorTest.class, TranslatedIRODSQueryTest.class,
		ProtocolTests.class, AOTests.class, FileTests.class, RuleTests.class,
		PackingInstructionTests.class, DomainTests.class, TransferTests.class,
		LocalFileUtilsTest.class, RemoteExecuteServiceImplTest.class,
		IRODSPasswordUtilitiesTest.class, IRODSUriUtilsTest.class,
		MiscIRODSUtilsTest.class, AuthTests.class, ChecksumTests.class })
/**
 * Suite to run all tests (except long running and functional), further refined by settings in testing.properites.  Some subtests may be shut
 * off by these properties.
 */
public class AllTests {

}
