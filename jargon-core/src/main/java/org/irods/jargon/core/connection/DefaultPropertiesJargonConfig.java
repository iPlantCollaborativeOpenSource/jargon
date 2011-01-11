/**
 * 
 */
package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.irods.jargon.core.exception.JargonException;

/**
 * Reference class that parses the default Jargon properties (jargon.properties)
 * and provides easy methods to obtain configuration information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DefaultPropertiesJargonConfig implements JargonProperties {

	private final Properties jargonProperties;

	/**
	 * Default constructor will load the default properties from the
	 * 'jargon.properties' file on the classpath.
	 * 
	 * @throws JargonException
	 *             if properties cannot be loaded.
	 */
	public DefaultPropertiesJargonConfig() throws JargonException {

		ClassLoader loader = this.getClass().getClassLoader();
		InputStream in = loader.getResourceAsStream("jargon.properties");
		jargonProperties = new Properties();

		try {
			jargonProperties.load(in);
		} catch (IOException ioe) {
			throw new JargonException("error loading jargon.properties", ioe);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#isUseParallelTransfer()
	 */
	@Override
	public boolean isUseParallelTransfer() throws JargonException {
		String propVal = verifyPropExistsAndGetAsString("transfer.use.parallel");
		return Boolean.valueOf(propVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#getMaxParallelThreads()
	 */
	@Override
	public int getMaxParallelThreads() throws JargonException {
		return verifyPropExistsAndGetAsInt("transfer.max.parallel.threads");
	}

	private String verifyPropExistsAndGetAsString(final String propKey)
			throws JargonException {
		String propVal = (String) jargonProperties.get(propKey);
		if (propVal == null) {
			throw new JargonException(propKey + " not set in jargon.properties");
		}
		return propVal;
	}

	private int verifyPropExistsAndGetAsInt(final String propKey)
			throws JargonException {
		String propVal = verifyPropExistsAndGetAsString(propKey);

		try {
			return Integer.parseInt(propVal);
		} catch (NumberFormatException nfe) {
			throw new JargonException("prop " + propKey
					+ "did not result in an int value, was:" + propVal);
		}

	}

}