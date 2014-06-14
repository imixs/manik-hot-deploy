/*******************************************************************************
 *  Manik Hot Deploy
 *  Copyright (C) 2010 Ralph Soika  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Contributors:  
 *  	Ralph Soika ,Alexander
 * 
 *******************************************************************************/
package org.imixs.eclipse.manik;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

/**
 * The Builder Class for hot deployment resource files
 * 
 * @author rsoika,Alexander
 * 
 */
public class HotdeployBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.imixs.eclipse.manik.hotdeployBuilder";

	private String[] skipFolderList = { "/src/main/resources/",
			"/src/main/java/", "/src/test/resources/", "/src/test/java/" };

	private String[] skipSourcePathList = { "/classes/", "/src/main/webapp/" };

	private String hotdeployTarget = "";
	private String autodeployTarget = "";
	private String sourceFilePath = "";
	private String sourceFileName = "";

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		Console console = new Console();

		if (kind == FULL_BUILD) {
			console.println("FULL_BUILD not supported");
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				console.println("MODE not supported");
			} else {
				incrementalBuild(delta, monitor);
			}
		}

		return null;
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		delta.accept(new HotdeployDeltaVisitor());
	}

	/**
	 * copies the resource into the target server. The method distinguishes
	 * between two modes: In the case the the file resource ends in .ear or .war
	 * the file will be copied into the autodeploy folder. In all other cases
	 * the method tries to perform a hotdeployment into the hotdeployment
	 * folder. The method termnates if not deployment folder is defined.
	 * 
	 * In case of a hotdeployment the target of the file to be copied is
	 * computed by the helper method computeTarget()
	 * 
	 * The method did not compute any copy of a directory ressource.
	 * 
	 * If .ear or .war file is autodeployed the method checks the maven /target
	 * folder pattern. In this case only root artefacts will be deployed!
	 * 
	 * @param resource
	 *            The SourceFile
	 * 
	 * @param bremove
	 *            indicates if the Sourcefile should be removed or added
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	void deployResource(IResource resource, boolean bremove, String action)
			throws CoreException {

		String targetFilePath = null;

		// open a new console..
		Console console = new Console();

		// do not deploy directory resources!
		if (!(resource instanceof IFile))
			return;

		IFile file = (IFile) resource;
		// console.println(action + " " + file.getFullPath());
		sourceFileName = file.getName();
		sourceFilePath = file.getFullPath().toString();

		// skip source files like /src/main/java/*
		for (String value : skipFolderList) {
			if (sourceFilePath.contains(value)) {
				// console.println("Skipping resource: " + sourceFilePath
				// + " because it contains: " + value);
				return;
			}
		}

		// read deployment directory settings....
		autodeployTarget = this.getProject().getPersistentProperty(
				new QualifiedName("",
						TargetPropertyPage.AUTODEPLOY_DIR_PROPERTY));

		hotdeployTarget = this.getProject()
				.getPersistentProperty(
						new QualifiedName("",
								TargetPropertyPage.HOTDEPLOY_DIR_PROPERTY));

		if ("".equals(hotdeployTarget))
			hotdeployTarget = null;
		if ("".equals(autodeployTarget))
			autodeployTarget = null;

		// check for an missing/invalid confiugration
		if (autodeployTarget == null && hotdeployTarget == null) {
			console.println("[ERROR]: Missing configuration. Please check your manik deployment properties for this project.");
			return;
		}

		// check if a .ear or .war file should be autodeplyoed....
		if ((sourceFileName.endsWith(".ear") || sourceFileName.endsWith(".war"))) {
			// verify if target autodeploy folder exists!
			if (autodeployTarget == null)
				return; // no op..

			if (!autodeployTarget.endsWith("/"))
				autodeployTarget += "/";
			File targetTest = new File(autodeployTarget);
			if (!targetTest.exists()) {
				console.println("[ERROR]: autodeploy directory '"
						+ autodeployTarget
						+ "' dose not exist. Please check your manik properties for this project.");
				return;
			}

			// verify if sourceFileName includes a maven /target folder pattern
			if (sourceFilePath.indexOf("/target/") > -1) {
				// in this case only root artefacts will be copied. No .war
				// files included in a /target sub folder!
				if (sourceFilePath.indexOf('/',
						sourceFilePath.indexOf("/target/")+8) > -1)
					return; // no op!

			}

			targetFilePath = autodeployTarget + sourceFileName;

		} else {
			// Hotdepoyment mode!
			if (hotdeployTarget == null)
				// no hotdeployTarget defined
				return;

			// optimize path....
			if (!hotdeployTarget.endsWith("/"))
				hotdeployTarget += "/";

			// compute the target path....
			targetFilePath = computeTarget();

		}

		// if the target file was not computed return....
		if (targetFilePath == null)
			return;

		// now copy / delete the file....
		OutputStream out = null;
		InputStream is = null;
		try {
			if (bremove) {
				// remove file
				File f = new File(targetFilePath);
				f.delete();
				console.println("[DELETE]: " + targetFilePath);
			} else {
				// Copy the file....
				is = file.getContents();
				File fOutput = new File(targetFilePath);
				out = new FileOutputStream(fOutput);
				byte buf[] = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// log message..
				if (sourceFileName.endsWith(".ear")
						|| sourceFileName.endsWith(".war"))
					console.println("[AUTODEPLOY]: " + sourceFilePath);
				else
					console.println("[HOTDEPLOY]: " + sourceFilePath);

			}
		} catch (IOException ex) {
			// unable to copy file
			// console.println("[ERROR]: "+ex.getMessage());
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				console.println("[ERROR]: closing stream: " + e.getMessage());
			}

		}

	}

	/**
	 * This method dose the magic of the manik hot deployer. The method computes
	 * the target goal inside an application target. There are three different
	 * cases:
	 * 
	 * case-1): .war or .ear files are simply deployed into the target root. No
	 * hierarchy is computed
	 * 
	 * case-2): the target is a web application. This is indicated by the
	 * existence of an /WEB-INF folder. in this case we check for two different
	 * source files
	 * 
	 * case-2-a): the source file contains the path /target/classes/ - so we
	 * copy the source into [target]/WEB-INF/classes
	 * 
	 * case-2-b): otherwise we copy into the target root strip any
	 * /target/main/webcontent präfixes
	 * 
	 * 
	 * case-3): the target is no web application. So we can copy the source into
	 * the target root stripp any /target/main/src präfixes
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param resource
	 * @throws CoreException
	 */
	private String computeTarget() {
		File folder = null;

		// hotdeplyoment mode
		// test if deployment is enabled
		if (hotdeployTarget == null)
			return null;

		/* case-2 case a and b included */
		// test if the sourcefile contains a source path which needs to be
		// removed ?
		for (String value : skipSourcePathList) {
			if (sourceFilePath.contains(value)) {

				String path = sourceFilePath.substring(sourceFilePath
						.indexOf(value) + value.length() - 0);

				// now test if the target folder is a web application and the
				// sourcfile is a /classes/ file
				// - test for /WEB-INF/ folder
				if (sourceFilePath.contains("/classes/")) {
					folder = new File(hotdeployTarget + "/WEB-INF/");
					if (folder.exists()) {
						// target is web app - so we need to extend the
						// target....
						path = "/WEB-INF/classes/" + path;
						// console.println("Target is a web application changed target path to: "
						// + path);
					}
				}

				if (path.indexOf('/') > -1) {
					folder = new File(hotdeployTarget
							+ path.substring(0, path.lastIndexOf('/')));
					// test target folder - if not exists we did not create the
					// path and return null...
					if (!folder.exists()) {
						return null;
						// console.println("Target folder does not exist. Creating: "
						// + folder.getAbsolutePath());
						// folder.mkdirs();
					}
				}
				// console.println("Target is: " + target + path);
				return hotdeployTarget + path;

			}
		}
		// console.println("Target is: " + target + sourceFilePath);
		return hotdeployTarget + sourceFilePath;

	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		myConsole.activate();
		return myConsole;
	}

	class HotdeployDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				deployResource(resource, false, "ADDED");
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				deployResource(resource, true, "REMOVED");
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				deployResource(resource, false, "CHANGED");
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

}
