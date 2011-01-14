package org.irods.jargon.core.transfer;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.exception.JargonException;
import org.junit.Test;

public class DefaultTransferControlBlockTest {

	@Test
	public void testInstance() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
				.instance();
		Assert.assertNotNull(testControlBlock);
	}

	@Test(expected = JargonException.class)
	public void testInstanceBadMaxErrors() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
				.instance(null, -3);
		Assert.assertNotNull(testControlBlock);
	}

	@Test
	public void testInstanceGoodMaxErrors() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
				.instance(null, 5);
		Assert.assertNotNull(testControlBlock);
	}
	
	@Test
	public void testNoFiltersWhenLastGoodPathEmpty() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
		.instance("", 5);
		
		TestCase.assertTrue("did not pass filter when no last good path", testControlBlock.filter("bbb"));
	}
	
	@Test
	public void testFiltersWhenLastGoodPathNotEmptyAndFilterNotYetHit() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
		.instance("bbb", 5);
		TestCase.assertFalse("did not pass filter when no last good path", testControlBlock.filter("aaa"));
	}
	
	@Test
	public void testFiltersWhenLastGoodPathNotEmptyAndFilterHit() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
		.instance("aaa", 5);
		testControlBlock.filter("aaa");
		TestCase.assertTrue("did not pass filter when no last good path", testControlBlock.filter("bbb"));
	}

}
