package org.openntf.maven.nsfodp.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpRequest;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

public enum ODPMojoUtil {
	;
	
	/**
	 * Adds server credential information from the user's settings.xml, if applicable.
	 * 
	 * @param wagonManager the active WagonManager to use for credential lookup
	 * @param serverId the server ID to find credentials for
	 * @param req the request to add credentials to
	 * @param log the logger to use
	 * @return the effective username of the request
	 * @throws MojoExecutionException if the server ID is specified but credentials cannot be found
	 */
	public static String addAuthenticationInfo(WagonManager wagonManager, String serverId, HttpRequest req, Log log) throws MojoExecutionException {
		String userName;
		if(serverId != null && !serverId.isEmpty()) {
			// Look up credentials for the server
			AuthenticationInfo info = wagonManager.getAuthenticationInfo(serverId);
			if(info == null) {
				throw new MojoExecutionException("Could not find server credentials for specified server ID: " + serverId);
			}
			userName = info.getUserName();
			if(userName == null || userName.isEmpty()) {
				// Then just use Anonymous
				if(log.isDebugEnabled()) {
					log.debug("Configured username is blank - acting as Anonymous");
				}
				userName = "Anonymous";
			} else {
				if(log.isDebugEnabled()) {
					log.debug("Authenticating as user " + userName);
				}
				String password = info.getPassword();
				
				// Create a Basic auth header
				// This is instead of HttpClient's credential handling because of how
				//   Domino handles the auth handshake.
				String enc = Base64.encodeBase64String((userName + ":" + password).getBytes());
				req.addHeader("Authorization", "Basic " + enc);
			}
		} else {
			if(log.isDebugEnabled()) {
				log.debug("No username specified - acting as Anonymous");
			}
			userName = "Anonymous";
		}
		return userName;
	}
}
