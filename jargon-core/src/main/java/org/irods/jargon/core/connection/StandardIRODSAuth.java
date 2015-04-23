package org.irods.jargon.core.connection;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AuthResponseInp;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.irods.jargon.core.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard iRODS challange/response mechanism
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class StandardIRODSAuth extends AuthMechanism {

	public static final Logger log = LoggerFactory
			.getLogger(StandardIRODSAuth.class);

	/**
	 * Do the normal iRODS password challenge/response sequence
	 * 
	 * @param irodsAccount
	 * @param irodsCommands
	 * @return <code>String</code> with the iRODS challenge value, which will be
	 *         returned in the Auth response
	 * @throws JargonException
	 */
	private String sendStandardPassword(final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsCommands)
			throws JargonException {

		log.info("sending standard irods password");

		cachedChallenge = sendAuthRequestAndGetChallenge(irodsCommands);

		String response = challengeResponse(cachedChallenge,
				irodsAccount.getPassword(), irodsCommands);
		AuthResponseInp authResponse_PI = new AuthResponseInp(
				irodsAccount.getProxyName(), response);

		// should be a header with no body if successful
		irodsCommands.irodsFunction(RequestTypes.RODS_API_REQ.getRequestType(),
				authResponse_PI.getParsedTags(), AUTH_RESPONSE_AN);

		return cachedChallenge;
	}

	/**
	 * Add the password to the end of the challenge string, pad to the correct
	 * length, and take the md5 of that.
	 */
	private String challengeResponse(final String challenge, String password,
			final AbstractIRODSMidLevelProtocol irodsCommands)
			throws JargonException {
		// Convert base64 string to a byte array
		byte[] chal = null;
		byte[] temp = Base64.fromString(challenge);

		if (IRODSAccount.isDefaultObfuscate()) {
			try {
				password = new PasswordObfuscator(new File(password))
						.encodePassword();
			} catch (Throwable e) {
				log.error("error during account obfuscation", e);
			}
		}

		if (password.length() < ConnectionConstants.MAX_PASSWORD_LENGTH) {
			// pad the end with zeros to MAX_PASSWORD_LENGTH
			chal = new byte[ConnectionConstants.CHALLENGE_LENGTH
					+ ConnectionConstants.MAX_PASSWORD_LENGTH];
		} else {
			log.error("password is too long");
			throw new IllegalArgumentException("Password is too long");
		}

		// add the password to the end
		System.arraycopy(temp, 0, chal, 0, temp.length);
		try {
			temp = password.getBytes(irodsCommands.getPipelineConfiguration()
					.getDefaultEncoding());
		} catch (UnsupportedEncodingException e1) {
			log.error("unsupported encoding of:{}", irodsCommands
					.getPipelineConfiguration().getDefaultEncoding(), e1);
			throw new JargonException("unsupported encoding:"
					+ irodsCommands.getPipelineConfiguration()
							.getDefaultEncoding());
		}
		System.arraycopy(temp, 0, chal, ConnectionConstants.CHALLENGE_LENGTH,
				temp.length);

		// get the md5 of the challenge+password
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			chal = digest.digest(chal);
		} catch (GeneralSecurityException e) {
			SecurityException se = new SecurityException();
			se.initCause(e);
			log.error(
					"general security exception, initCause is:"
							+ e.getMessage(), e);
			throw se;
		}

		// after md5 turn any 0 into 1
		for (int i = 0; i < chal.length; i++) {
			if (chal[i] == 0) {
				chal[i] = 1;
			}
		}

		// return to Base64
		return Base64.toString(chal);
	}

	@Override
	protected AbstractIRODSMidLevelProtocol processAuthenticationAfterStartup(
			final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsCommands,
			final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {
		log.info("authenticate");
		String challengeValue = sendStandardPassword(irodsAccount,
				irodsCommands);
		log.info("auth was successful");
		AuthResponse authResponse = new AuthResponse();
		authResponse.setAuthenticatedIRODSAccount(irodsAccount);
		authResponse.setAuthenticatingIRODSAccount(irodsAccount);
		authResponse.setChallengeValue(challengeValue);
		authResponse.setStartupResponse(startupResponseData);
		authResponse.setSuccessful(true);
		log.info("auth response was:{}", authResponse);
		irodsCommands.setAuthResponse(authResponse);
		irodsCommands.setIrodsAccount(irodsAccount);
		return irodsCommands;
	}

}
