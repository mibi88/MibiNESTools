/*
 * MibiNESTools - Create NES games easily!
 * Copyright (C) 2024  Mibi88
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package io.github.mibi88.mibinestools;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * @author mibi88
 */
public class Filemenu extends JMenu {
    NewFileMenu newFile;
    JMenuItem openFolder;
    JMenuItem openFile;
    OpenWithMenu openWith;
    JMenuItem saveFile;
    JMenuItem saveAsFile;
    JMenuItem quit;

    /**
     * Initialize the file menu
     * @param window Tool window
     */
    public Filemenu(Window window) {
        super("File", false);
        int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        newFile = new NewFileMenu(window);
        openFolder = new JMenuItem("Open a folder...");
        openFile = new JMenuItem("Open...");
        openFile.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, modifier));
        openWith = new OpenWithMenu(window);
        saveFile = new JMenuItem("Save");
        saveFile.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, modifier));
        saveAsFile = new JMenuItem("Save as...");
        saveAsFile.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.SHIFT_MASK | modifier));
        quit = new JMenuItem("Quit");
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                modifier));
        add(newFile);
        add(openFolder);
        add(openFile);
        add(openWith);
        add(saveFile);
        add(saveAsFile);
        addSeparator();
        add(quit);
        addActions(window);
    }
    
    private void addActions(Window window) {
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                window.quit();
            }
        });
        openFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                window.openFolder();
            }
        });
        openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                window.openFile(null, false);
            }
        });
        saveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                window.saveFile();
            }
        });
        saveAsFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                window.saveAsFile();
            }
        });
    }
    
    /**
     * Get the new file menu.
     * @return Returns the new file menu.
     */
    public NewFileMenu getNewFileMenu() {
        return newFile;
    }
    
    /**
     * Get the open with menu.
     * @return Returns the open with menu.
     */
    public OpenWithMenu getOpenWithMenu() {
        return openWith;
    }
}
