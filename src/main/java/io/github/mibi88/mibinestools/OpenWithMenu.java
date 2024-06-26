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
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * @author mibi88
 */
public class OpenWithMenu extends JMenu {

    /**
     * Create a new open with menu.
     * @param window The window used with this menu.
     */
    public OpenWithMenu(Window window) {
        super("Open with...");
        reset(window);
    }
    
    /**
     * Reset this menu.
     * @param window The window used with this menu.
     */
    public void reset(Window window) {
        removeAll();
        int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        JMenuItem inCurrentEditor = new JMenuItem(
                "The current editor");
        inCurrentEditor.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, InputEvent.SHIFT_MASK | modifier));
        inCurrentEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.openFile(null, true);
            }
        });
        add(inCurrentEditor);
        addSeparator();
    }
    
    /**
     * Add an editor to this menu.
     * @param editor The editor to add.
     * @param window The window used with this menu.
     */
    public void addEditor(Class editor, Window window) {
        JMenuItem menuItem;
        try {
            menuItem = new JMenuItem((String)editor
                    .getMethod("getEditorName").invoke(null));
        } catch (Exception ex) {
            Logger.getLogger(NewFileMenu.class.getName()).log(
                    Level.SEVERE, null, ex);
            menuItem = new JMenuItem("Unknown Editor");
        }
        add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    window.openFile(editor, false);
                } catch (Exception ex) {
                    Logger.getLogger(NewFileMenu.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        });
    }
}
