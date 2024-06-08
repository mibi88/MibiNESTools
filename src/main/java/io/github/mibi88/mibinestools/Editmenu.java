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
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * @author mibi88
 */
public class Editmenu extends JMenu {
    private JMenuItem undo;
    private JMenuItem redo;
    private JMenuItem copy;
    private JMenuItem cut;
    private JMenuItem paste;

    /**
     * Initialize the edit menu
     * @param window Tool window
     */
    public Editmenu(Window window) {
        super("Edit");
        int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        undo = new JMenuItem("Undo");
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                modifier));
        redo = new JMenuItem("Redo");
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                modifier));
        copy = new JMenuItem("Copy");
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                modifier));
        cut = new JMenuItem("Cut");
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                modifier));
        paste = new JMenuItem("Paste");
        paste.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_V, modifier));
        add(undo);
        add(redo);
        addSeparator();
        add(copy);
        add(cut);
        add(paste);
        addActions(window);
    }
    
    private void addActions(Window window) {
        // TODO: Add actions
        undo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.undo();
            }
        });
        redo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.redo();
            }
        });
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.copy();
            }
        });
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.cut();
            }
        });
        paste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.paste();
            }
        });
    }
}
