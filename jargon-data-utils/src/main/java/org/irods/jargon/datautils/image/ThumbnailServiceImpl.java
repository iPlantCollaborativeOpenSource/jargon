package org.irods.jargon.datautils.image;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.domain.RemoteCommandInformation;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.utils.Base64;
import org.irods.jargon.datautils.AbstractDataUtilsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the creation and maintenance of thumb-nail images for image files
 * stored in iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ThumbnailServiceImpl extends AbstractDataUtilsServiceImpl
		implements ThumbnailService {

	public static final Logger log = LoggerFactory
			.getLogger(ThumbnailServiceImpl.class);

	/**
	 * Constructor with required dependencies
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that can create necessary
	 *            objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} that contains the login information
	 */
	public ThumbnailServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Default (no-values) constructor.
	 */
	public ThumbnailServiceImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.image.ThumbnailService#
	 * isIRODSThumbnailGeneratorAvailable()
	 */
	@Override
	public boolean isIRODSThumbnailGeneratorAvailable() throws JargonException {
		log.info("isIRODSThumbnailGeneratorAvailable()");
		boolean found = false;
		EnvironmentalInfoAO environmentalInfoAO = irodsAccessObjectFactory
				.getEnvironmentalInfoAO(getIrodsAccount());
		try {
			log.info("listing available scripts...");
			List<RemoteCommandInformation> scripts = environmentalInfoAO
					.listAvailableRemoteCommands();

			for (RemoteCommandInformation script : scripts) {
				if (script.getCommand().equals("makeThumbnail.py")) {
					found = true;
					break;
				}
			}

			return found;

		} catch (DataNotFoundException e) {
			log.info("no ability to list commands, assume no thumbnail service");
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.image.ThumbnailService#
	 * generateThumbnailForIRODSPathViaRule(java.io.File, java.lang.String)
	 */
	@Override
	public File generateThumbnailForIRODSPathViaRule(
			final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws IRODSThumbnailProcessUnavailableException, JargonException {

		log.info("generateThumbnailForIRODSPathViaRule()");

		if (workingDirectory == null) {
			throw new IllegalArgumentException("null workingDirectory");
		}

		if (irodsAbsolutePathToGenerateThumbnailFor == null
				|| irodsAbsolutePathToGenerateThumbnailFor.isEmpty()) {
			throw new IllegalArgumentException(
					"nul irodsAbsolutePathToGenerateThumbnailFor");
		}

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePathToGenerateThumbnailFor);

		String myPath;
		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			myPath = objStat.getObjectPath();
		} else {
			myPath = irodsAbsolutePathToGenerateThumbnailFor;
		}

		File targetTempFile = createWorkingDirectoryImageFile(workingDirectory,
				irodsAbsolutePathToGenerateThumbnailFor);

		if (targetTempFile.exists()) {
			targetTempFile.delete();
		}

		InputStream is = retrieveThumbnailByIRODSAbsolutePathViaRule(myPath);
		try {
			OutputStream fos = new BufferedOutputStream(new FileOutputStream(
					targetTempFile));
			log.info("have image data, stream to temp file");
			byte[] buffer = new byte[1024];
			int len = is.read(buffer);
			while (len != -1) {
				fos.write(buffer, 0, len);
				len = is.read(buffer);
			}
			fos.flush();
			fos.close();
			is.close();
			return targetTempFile;
		} catch (FileNotFoundException e) {
			log.error("file not found exception for temp image output stream",
					e);
			throw new JargonException(
					"no file found when generating temp image output stream", e);
		} catch (IOException e) {
			log.error("IOException for temp image output stream", e);
			throw new JargonException(
					"IOException when generating temp image output stream", e);
		}
	}

	/**
	 * Create any necessary directories in the working directory to house the
	 * generated image.
	 * 
	 * @param workingDirectory
	 * @param irodsAbsolutePathToGenerateThumbnailFor
	 * @return
	 * @throws JargonException
	 */
	private File createWorkingDirectoryImageFile(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws JargonException {

		log.info("createWorkingDirectoryImageFile(), working directory is:{}",
				workingDirectory);
		log.info("irodsAbsolutePathToGenerateThumbnailFor:{}",
				irodsAbsolutePathToGenerateThumbnailFor);
		if (workingDirectory.exists()) {
			if (workingDirectory.isDirectory()) {
				// OK
			} else {
				throw new IllegalArgumentException("workingDirectory is a file");
			}
		} else {
			log.info("mkdirs on working directory");
			workingDirectory.mkdirs();
		}

		File targetTempFile = new File(workingDirectory,
				irodsAbsolutePathToGenerateThumbnailFor);
		targetTempFile.getParentFile().mkdirs();

		log.info("thumbnail target temp file:${}",
				targetTempFile.getAbsolutePath());
		return targetTempFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.image.ThumbnailService#
	 * retrieveThumbnailByIRODSAbsolutePathViaRule(java.lang.String)
	 */
	@Override
	public InputStream retrieveThumbnailByIRODSAbsolutePathViaRule(
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws JargonException {

		log.info("retrieveThumbnailByIRODSAbsolutePath()");

		if (irodsAbsolutePathToGenerateThumbnailFor == null
				|| irodsAbsolutePathToGenerateThumbnailFor.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToGenerateThumbnailFor");
		}

		log.info("irodsAbsolutePathToGenerateThumbnailFor:{}",
				irodsAbsolutePathToGenerateThumbnailFor);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePathToGenerateThumbnailFor);
		log.info("objStat for photo:{}", objStat);
		String myPath;
		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			myPath = objStat.getObjectPath();
		} else {
			myPath = irodsAbsolutePathToGenerateThumbnailFor;
		}

		log.info("using path:{}", myPath);

		// get Base64 Encoded data from a rule invocation, this represents the
		// generated thumbnail

		StringBuilder sb = new StringBuilder();
		sb.append("@external\n makeThumbnailFromObj {\n");
		sb.append("msiSplitPath(*objPath,*collName,*objName);\n");
		sb.append(" msiAddSelectFieldToGenQuery(\"DATA_PATH\", \"null\", *GenQInp);\n");
		sb.append("msiAddSelectFieldToGenQuery(\"RESC_LOC\", \"null\", *GenQInp);\n");
		sb.append(" msiAddConditionToGenQuery(\"COLL_NAME\", \"=\", *collName, *GenQInp);\n");
		sb.append("msiAddConditionToGenQuery(\"DATA_NAME\", \"=\", *objName, *GenQInp);\n");
		sb.append("msiAddConditionToGenQuery(\"DATA_RESC_NAME\",\"=\", *resource, *GenQInp);\n");
		sb.append("msiExecGenQuery(*GenQInp, *GenQOut);\n");
		sb.append("foreach (*GenQOut)\n{\n");
		sb.append(" msiGetValByKey(*GenQOut, \"DATA_PATH\", *data_path);\n");
		sb.append("msiGetValByKey(*GenQOut, \"RESC_LOC\", *resc_loc);\n}\n");
		sb.append("msiExecCmd(\"makeThumbnail.py\", \"'*data_path'\", *resc_loc, \"null\", \"null\", *CmdOut);\n");
		sb.append("msiGetStdoutInExecCmdOut(*CmdOut, *StdoutStr);\n");
		sb.append(" writeLine(\"stdout\", *StdoutStr);\n}\n");
		sb.append("INPUT *objPath=\'");
		sb.append(myPath.trim());
		sb.append("\',*resource=\'");
		sb.append(irodsAccount.getDefaultStorageResource());
		sb.append("'\n");
		sb.append("OUTPUT ");
		sb.append(THUMBNAIL_RULE_DATA_PARAMETER);
		RuleProcessingAO ruleProcessingAO = getIrodsAccessObjectFactory()
				.getRuleProcessingAO(getIrodsAccount());
		IRODSRuleExecResult result = ruleProcessingAO
				.executeRule(sb.toString());

		String execOut;
		try {
			execOut = (String) result.getOutputParameterResults()
					.get(THUMBNAIL_RULE_DATA_PARAMETER).getResultObject();
		} catch (NullPointerException npe) {
			throw new IRODSThumbnailProcessUnavailableException(
					"no iRODS rule-based thumbnail generation available");
		}

		InputStream is = new java.io.ByteArrayInputStream(
				Base64.fromString(execOut));
		return is;

	}

}
