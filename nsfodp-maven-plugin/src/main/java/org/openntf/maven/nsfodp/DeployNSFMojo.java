/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.maven.nsfodp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.openntf.maven.nsfodp.util.ODPMojoUtil;
import org.openntf.maven.nsfodp.util.ResponseUtil;

/**
 * Goal which compiles deploys an NSF to a Domino server.
 */
@Mojo(name="deploy", defaultPhase=LifecyclePhase.DEPLOY)
public class DeployNSFMojo extends AbstractMojo {
	
	public static final String SERVLET_PATH = "/org.openntf.nsfosp/deployment"; //$NON-NLS-1$
	
	@Parameter(defaultValue="${project}", readonly=true)
	private MavenProject project;
	
	@Component
	private WagonManager wagonManager;

	/**
	 * The server id in settings.xml to use when authenticating with the deployment server, or
	 * <code>null</code> to authenticate as anonymous.
	 */
	@Parameter(property="nsfodp.deploy.server", required=false)
	private String deployServer;
	/**
	 * The base URL of the ODP deployment server, e.g. "http://my.server".
	 */
	@Parameter(property="nsfodp.deploy.serverUrl", required=false)
	private URL deployServerUrl;
	/**
	 * Whether to replace the design of an existing database in the specified path.
	 */
	@Parameter(property="nsfodp.deploy.replaceDesign")
	private boolean deployReplaceDesign;
	/**
	 * The destination path for the deployed database.
	 */
	@Parameter(property="nsfodp.deploy.destPath", required=false)
	private String deployDestPath;

	private Log log;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		
		MavenProject project = Objects.requireNonNull(this.project, "project cannot be null"); //$NON-NLS-1$
		Artifact artifact = project.getArtifact();
		File artifactFile = artifact.getFile();
		if(!artifactFile.exists()) {
			if(log.isInfoEnabled()) {
				log.info("Artifact file does not exist; skipping deployment");
			}
			return;
		}
		if(!artifactFile.isFile()) {
			if(log.isInfoEnabled()) {
				log.info("Artifact file is not a regular file; skipping deployment");
			}
			return;
		}
		
		URL deploymentServerUrl = this.deployServerUrl;
		if(deploymentServerUrl == null || deploymentServerUrl.toString().isEmpty()) {
			if(log.isInfoEnabled()) {
				log.info("Deployment server URL is empty; skipping deployment");
			}
			return;
		}
		String deployDestPath = this.deployDestPath;
		if(deployDestPath == null || deployDestPath.isEmpty()) {
			if(log.isInfoEnabled()) {
				log.info("Deployment dest path is empty; skipping deployment");
			}
			return;
		}
		
		try(CloseableHttpClient client = HttpClients.createDefault()) {
			URI servlet = deploymentServerUrl.toURI().resolve(SERVLET_PATH);
			if(log.isInfoEnabled()) {
				log.info("Deploying NSF with server " + servlet);
			}
			HttpPost post = new HttpPost(servlet);
			
			ODPMojoUtil.addAuthenticationInfo(this.wagonManager, this.deployServer, post, this.log);
			
			HttpEntity entity = MultipartEntityBuilder.create()
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.setContentType(ContentType.MULTIPART_FORM_DATA)
					.addTextBody("replaceDesign", String.valueOf(this.deployReplaceDesign)) //$NON-NLS-1$
					.addTextBody("destPath", deployDestPath) //$NON-NLS-1$
					.addBinaryBody("file", artifactFile) //$NON-NLS-1$
					.build();
			post.setEntity(entity);
			
			HttpResponse res = client.execute(post);
			HttpEntity responseEntity = ResponseUtil.checkResponse(log, res);
 			
			try(InputStream is = responseEntity.getContent()) {
				ResponseUtil.monitorResponse(log, is);
			}
		} catch(MojoExecutionException e) {
			throw e;
		} catch(IOException | URISyntaxException e) {
			throw new MojoExecutionException("Exception while deploying artifact", e);
		}
	}

}
