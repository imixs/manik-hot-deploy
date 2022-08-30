package org.imixs.maven.manik;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * The Imixs Manik Autodeploy Plugin runs during the LifecyclePhase INSTALL on
 * the goal 'deploy'. This goal can be used to autodeploy an artefact into a
 * target directory (e.g. the Autodeploy folder of an application server).
 * 
 * @author rsoika
 *
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class MojoAutodeployPlugin extends AbstractMojo {

	/*
	 * Note, the property variable name must be equal here!
	 */
	@Parameter(property = "autodeployments", alias = "hotdeploy.autodeployments", required = true)
	protected List<AutoDeployment> autodeployments;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	private WatchService watchService;
	
	org.apache.maven.model.FileSet fileset;

	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		getLog().info("==== Manik Hot Deployment v2.0 ====");
		String baseDir=project.getBasedir().toString();
		
			
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (Exception e) {
			throw new MojoExecutionException("Unable to create watch service");
		}
		getLog().info("..... registering " + autodeployments.size() + " deployments...");

		for (AutoDeployment deployment : autodeployments) {
			String source=
					deployment.getSource();
			String target=deployment.getTarget();
			
			// relative path?
			if (!source.startsWith("/")) {
				source=baseDir+"/"+source;
			}
			if (!target.startsWith("/")) {
				target=baseDir+"/"+target;
			}
					
			getLog().info("..... source=" + source);
			getLog().info("..... target=" + target);
			
			
			// copy file
			Path sourcePath = Paths.get(source);
			Path targetPath=null;
			if (target.endsWith("/")) {
				targetPath = Paths.get(target+sourcePath.getFileName());
			} else {
				targetPath = Paths.get(target);
			}
		
		    try {
		    	
		    	Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
				getLog().info("..... autodeployment successful");
			} catch (IOException e) {
				getLog().warn("Failed to copy target file: " + e.getMessage());
			}
		 
		}

	}
	// ...
}
