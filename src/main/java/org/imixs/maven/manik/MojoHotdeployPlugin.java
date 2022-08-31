package org.imixs.maven.manik;

import java.io.File;
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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "hotdeploy", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true)
public class MojoHotdeployPlugin extends AbstractMojo {
	
	public static final String SEPARATOR = FileSystems.getDefault().getSeparator();
	
	/*
	 * Note, the property variable name must be equal here!
	 */
	@Parameter(property = "hotdeployments", alias = "hotdeploy.hotdeployments", required = true)
	protected List<HotDeployment> hotdeployments;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	private WatchService watchService;

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		int count = 0;

		String baseDir = project.getBasedir().toString();

		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (Exception e) {
			throw new MojoExecutionException("Unable to create watch service");
		}
		getLog().info("Registering " + hotdeployments.size() + " sources...");

		for (HotDeployment deployment : hotdeployments) {
			String source = deployment.getSource();
			String target = deployment.getTarget();
			// relative path?
			if (!source.startsWith(SEPARATOR)) {
				source = baseDir + SEPARATOR + source;
			}
			if (!target.startsWith(SEPARATOR)) {
				target = baseDir + SEPARATOR + target;

				// verify if the target is a directory
				File file = new File(target);
				if (!file.isDirectory()) {
					// skip....
					getLog().warn("..... " + target
							+ " is not a directory! Deployment will be skipped. Please check your plugin configuration.");
					continue;
				}
				// test if target folder ends with /
				if (!target.endsWith(SEPARATOR)) {
					target = target + SEPARATOR;
				}

			}
			
			getLog().info("..... source=" + deployment.getSource());
			getLog().info("..... target=" + deployment.getTarget());
		}

		long longTimeout = 60 * 60 * 24 * 1000L;
		long shortTimeout = 750L;
		long timeout = longTimeout;
		int dueToRunGoal = 0;

		while (true) {
			try {

				if (timeout > shortTimeout) {
					getLog().info("Watcher - waiting for changes...");
				}

				// timeout to poll for (this way we can let lots of quick changes
				// take place -- and only run the goal when things settles down)
				WatchKey watchKey = watchService.poll(timeout, TimeUnit.MILLISECONDS);
				if (watchKey == null) {
					System.out.println("===============> ..tu irgendwas");
				}
				// schedule the goal to run
				timeout = shortTimeout;
				dueToRunGoal++;

				watchKey.reset();
			} catch (InterruptedException e) {
				break;

			} catch (ClosedWatchServiceException e) {
				break;
			}
		}
	}
	// ...
}
