package org.imixs.maven.manik;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * The Imixs Manik Hotdeploy Plugin can be started by the goal 'hotdeploy' This
 * goal can be used to hotdeploy changed made in a source directory.
 * <p>
 * Plugin configuration example:
 * 
 * <pre>
 * {@code		
	<hotdeployments>
		<deployment>
			<source>src/main/webapp</source>
			<target>docker/deployments/imixs-admin.war</target>
		</deployment>
	</hotdeployments>
  }
 * </pre>
 * 
 * This plugin was inspired by
 * https://github.com/fizzed/maven-plugins/blob/master/watcher/src/main/java/com/fizzed/maven/watcher/RunMojo.java
 * 
 * See also:
 * https://docs.oracle.com/javase/tutorial/essential/io/notification.html
 * 
 * @author rsoika
 *
 */
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

	private WatchService watcher;

	private Map<WatchKey, Path> watchKeyMap;

	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException, MojoFailureException {

		int count = 0;
		this.watchKeyMap = new HashMap<>();

		String baseDir = project.getBasedir().toString();

		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (Exception e) {
			throw new MojoExecutionException("Unable to create watch service");
		}

		if (hotdeployments.size() > 0) {
			getLog().info("Starting hot deployment....");

			for (HotDeployment deployment : hotdeployments) {
				String source = deployment.getSource();
				String target = deployment.getTarget();

				// relative path?
				if (!source.startsWith(SEPARATOR)) {
					source = baseDir + SEPARATOR + source;
				}
				if (!target.startsWith(SEPARATOR)) {
					target = baseDir + SEPARATOR + target;
				}
				// test if target folder ends with /
				if (!target.endsWith(SEPARATOR)) {
					target = target + SEPARATOR;
				}
				getLog().info("..... source: " + source);
				getLog().info("..... target: " + target);

				Path sourcePath = Paths.get(source);
				Path targetPath = Paths.get(target);
				try {
					// register watchKey for all event types
					
					registerRecursive(sourcePath,targetPath);
					
//					WatchKey watchKey = sourcePath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
//							StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
//					watchKeyMap.put(watchKey, targetPath);
//					
					getLog().info("..... watchKey registered");
				} catch (IOException x) {
					System.err.println(x);
				}

			}
		}

		System.out.println("===============> ..starting watcher loop....");
		for (;;) {
			try {

				// wait for key to be signaled
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException x) {
					return;
				}

				// get the targetPath from the watchKeyMap....
				Path targetPath = watchKeyMap.get(key);
				// process all events...
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					// an OVERFLOW event can occur regardless if events are lost or discarded.
					if (kind == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}

					
					// The filename is the
					// context of the event.
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();

					System.out.println(" event: " + " kind=" + event.kind() + " file=" +  filename.toString());
					System.out.println(" targetPath= " + targetPath.toString());

					// do something....
				}
				// Reset the key -- this step is critical if you want to
				// receive further watch events. If the key is no longer valid,
				// the directory is inaccessible so exit the loop.
				boolean valid = key.reset();
				if (!valid) {
					break;
				}

			} catch (ClosedWatchServiceException e) {
				break;
			}
		}
	}
	
	
	
	
	private void registerRecursive(final Path root, Path targetPath) throws IOException {
	    // register all subfolders
	    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	           WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
	           watchKeyMap.put(key, targetPath);
	            return FileVisitResult.CONTINUE;
	        }
	    });
	}
}
