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
 *  	rsoika,Alexander 
 * 
 *******************************************************************************/

package org.imixs.eclipse.manik;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Disable action to the hot deploy nature for a project
 * 
 * @author Alexander
 *
 */
public class DisableNatureAction implements IObjectActionDelegate {

  private ISelection selection;

  public void run(IAction action) {
    if (selection instanceof IStructuredSelection) {
      for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
          .hasNext();) {
        Object element = it.next();
        IProject project = null;
        if (element instanceof IProject) {
          project = (IProject) element;
        } else if (element instanceof IAdaptable) {
          project = (IProject) ((IAdaptable) element)
              .getAdapter(IProject.class);
        }
        if (project != null) {
          disableNature(project);
        }
      }
    }
  }

  public void selectionChanged(IAction action, ISelection selection) {
    this.selection = selection;
  }

  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  private void disableNature(IProject project) {
    Console console = new Console();
    console.println("Disable Manik for " + project);
    try {
      IProjectDescription description = project.getDescription();
      String[] natures = description.getNatureIds();

      for (int i = 0; i < natures.length; ++i) {
        if (HotdeployNature.NATURE_ID.equals(natures[i])) {
          // Remove the nature
          String[] newNatures = new String[natures.length - 1];
          System.arraycopy(natures, 0, newNatures, 0, i);
          System.arraycopy(natures, i + 1, newNatures, i, natures.length - i
              - 1);
          description.setNatureIds(newNatures);
          project.setDescription(description, null);
          console.println("Disabled Manik for " + project);
          return;
        }
      }
    } catch (CoreException e) {
      console.println(e.getMessage());
    }
  }

}
