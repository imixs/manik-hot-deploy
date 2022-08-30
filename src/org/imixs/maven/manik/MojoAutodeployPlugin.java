package org.imixs.maven.manik;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
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

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		System.out.println("===============> we are in the install phase..execute ");

	
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (Exception e) {
			throw new MojoExecutionException("Unable to create watch service");
		}
		getLog().info("Registering " + autodeployments.size() + " sources...");

		for (AutoDeployment deployment : autodeployments) {
			getLog().info("..... source=" + deployment.getSource());
			getLog().info("..... target=" + deployment.getTarget());
		}

	}
	// ...
}
