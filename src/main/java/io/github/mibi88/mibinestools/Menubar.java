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

import javax.swing.JMenuBar;

/**
 *
 * @author mibi88
 */
public class Menubar extends JMenuBar {
    private Filemenu fileMenu;
    private Editmenu editMenu;
    private Viewmenu viewMenu;
    private Aboutmenu aboutMenu;

    /**
     * Initialize the menubar
     * @param window The editor window
     */
    public Menubar(Window window) {
        createMenus(window);
    }
    
    private void createMenus(Window window) {
        fileMenu = new Filemenu(window);
        add(fileMenu);
        editMenu = new Editmenu(window);
        add(editMenu);
        viewMenu = new Viewmenu(window);
        add(viewMenu);
        aboutMenu = new Aboutmenu(window);
        add(aboutMenu);
    }
    
    /**
     * Get the View menu.
     * @return The view menu
     */
    public Viewmenu getViewMenu() {
        return viewMenu;
    }
    
    /**
     * Get the new file menu.
     * @return Returns the new file menu.
     */
    public NewFileMenu getNewFileMenu() {
        return fileMenu.getNewFileMenu();
    }
    
    /**
     * Get the open with menu.
     * @return Returns the open with menu.
     */
    public OpenWithMenu getOpenWithMenu() {
        return fileMenu.getOpenWithMenu();
    }
}
