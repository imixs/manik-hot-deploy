package org.imixs.maven.manik;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * The Imixs Manik Autodeploy Plugin runs during the LifecyclePhase INSTALL on
 * the goal 'deploy'. This goal can be used to autodeploy an artifact into a
 * target directory (e.g. the Autodeploy folder of an application server).
 * <p>
 * Plugin configuration example:
 * 
 * <pre>
 * {@code		
	<autodeployments>
		<deployment>
			<!-- wildcard deployment -->
			<source>target/*.war</source>
			<target>docker/deployments</target>
		</deployment>
	</autodeployments>
  }
 * </pre>
 * 
 * You can also use complex wildcard patterns like
 * 
 * <pre>
 * target/*.{war,ear,jar}
 * </pre>
 * <p>
 * The target must be an existing directory.
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

	org.apache.maven.model.FileSet fileset;

	public void execute() throws MojoExecutionException, MojoFailureException {

		int count = 0;

		Path basePath = project.getBasedir().toPath();

		if (autodeployments.size() > 0) {
			getLog().info("Starting auto deployment....");

			for (AutoDeployment deployment : autodeployments) {
				Path sourcePath =getSourcePath(deployment, basePath);
				Path targetPath =getTargetPath(deployment, basePath);
				getLog().info("..... source: " + sourcePath.toString());
				getLog().info("..... target: " + targetPath.toString());
				
			
				try {
					//Path sourcePath = Paths.get(source);
					/*
					 * Now we parse the source directory with the NIO DirectoryStream to detect all
					 * files supporting wildcards
					 * 
					 * e.g. *.{war,ear,jar} e.g. *.war
					 */
					DirectoryStream<Path> stream = Files.newDirectoryStream(sourcePath.getParent(),
							sourcePath.getFileName().toString());
					for (Path path : stream) {
						// copy single artifact
						copyArtefact(path, targetPath.resolve(path.getFileName()), deployment.isUnpack());
						count++;
					}
					stream.close();

				} catch (IOException e) {
					getLog().warn("Failed to copy target file: " + e.getMessage());
				}
				getLog().info(count + " auto-deployments completed.");
			}
		} else {
			getLog().info("No auto-deployments defined");
		}
	}

	
/**
 * Computes the source path of a deployment object
 * @param deployment
 * @param basePath
 * @return
 */
	public static Path getSourcePath(AbstractDeployment deployment ,Path basePath) {
		Path source = Paths.get(deployment.getSource());
		// relative path?
		if (!source.isAbsolute()) {
			source = basePath.resolve(source);
		}

		return source;
	}
	/**
	 * Computes the target path of a deployment object
	 * @param deployment
	 * @param basePath
	 * @return
	 */
	public static Path getTargetPath(AbstractDeployment deployment,Path basePath) {
		Path target = Paths.get(deployment.getTarget());
		// relative path?

		if (!target.isAbsolute()) {
			target = basePath.resolve(target);
		}

		return target;
	}
	
	
	/**
	 * This method copies a single artifact. If the path is a directory, the method
	 * prints a warning and skips the artifact.
	 * <p>
	 * If the param 'unpack' is true then the method tries to unzip the artifact.
	 * 
	 * @throws IOException
	 * 
	 */
	public void copyArtefact(Path path, Path targetPath, boolean unpack) throws IOException {
		if (new File(path.toString()).isDirectory()) {
			getLog().info("..... WARNING - artifact is an directory - deployment skipped");
		} else {
			// copy file
			getLog().info("..... deploy: " + path.toString() + " : unpack=" + unpack);
			if (!unpack) {
				Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
			} else {
				// unzip artifact....
				unzipArtifact(path.toString(), targetPath.toString());

				// Wildfly Support: touch a .dodeploy file
				touchFile(targetPath.getParent().toString() + "/" + path.getFileName() + ".dodeploy");
			}
		}
	}

	private void touchFile(String path) throws IOException {
		File doDeployFile = new File(path);
		if (!doDeployFile.exists()) {
			doDeployFile.createNewFile();
		}
	}

	/**
	 * Helper Method to unzip an artifact
	 * <p>
	 * 
	 * @see https://www.baeldung.com/java-compress-and-uncompress
	 * @param fileZip - artifact
	 * @param target  - target folder
	 * @throws IOException
	 */
	private void unzipArtifact(String fileZip, String target) throws IOException {

		File destDir = new File(target);
		byte[] buffer = new byte[1024];
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				File newFile = newFile(destDir, zipEntry);
				if (zipEntry.isDirectory()) {
					if (!newFile.isDirectory() && !newFile.mkdirs()) {
						throw new IOException("Failed to create directory " + newFile);
					}
				} else {
					// fix for Windows-created archives
					File parent = newFile.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory " + parent);
					}

					// write file content
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				}
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		}
	}

	/**
	 * Helper Method for method unzipArtifact
	 * 
	 * @param destinationDir
	 * @param zipEntry
	 * @return
	 * @throws IOException
	 */
	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}
}
