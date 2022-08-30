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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "hotdeploy", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true)
public class MojoHotdeployPlugin extends AbstractMojo {

	/*
	 * Note, the property variable name must be equal here!
	 */
	@Parameter(property = "hotdeployments", alias = "hotdeploy.hotdeployments", required = true)
	protected List<HotDeployment> hotdeployments;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	private WatchService watchService;

	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("===============> ..execute ");

	
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (Exception e) {
			throw new MojoExecutionException("Unable to create watch service");
		}
		getLog().info("Registering " + hotdeployments.size() + " sources...");

		for (HotDeployment deployment : hotdeployments) {
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
