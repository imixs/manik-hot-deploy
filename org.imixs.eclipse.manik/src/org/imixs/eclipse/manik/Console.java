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

import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console {
  private static final String CONSOLE_NAME = "Hotdeploy Console";

  public void println(String message) {
    MessageConsoleStream consoleStream = findConsole(CONSOLE_NAME)
        .newMessageStream();
    try {
      consoleStream.println(message);
    } finally {
      try {
        consoleStream.close();
      } catch (IOException e) {
        System.err.println("Error closing stream: " + e.getMessage());
      }
    }
  }

  public MessageConsoleStream newMessageStream() {
    return findConsole(CONSOLE_NAME).newMessageStream();
  }

  private MessageConsole findConsole(String name) {
    ConsolePlugin plugin = ConsolePlugin.getDefault();
    IConsoleManager conMan = plugin.getConsoleManager();
    IConsole[] existing = conMan.getConsoles();
    for (int i = 0; i < existing.length; i++)
      if (name.equals(existing[i].getName()))
        return (MessageConsole) existing[i];
    //no console found, so create a new one
    MessageConsole myConsole = new MessageConsole(name, null);
    conMan.addConsoles(new IConsole[] { myConsole });
    myConsole.activate();
    return myConsole;
  }
}
