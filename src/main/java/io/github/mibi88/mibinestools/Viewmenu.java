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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * @author mibi88
 */
public class Viewmenu extends JMenu {
    private JMenuItem zoomIn;
    private JMenuItem zoomOut;
    private JCheckBoxMenuItem grid;
    public Viewmenu(Window window) {
        super("View");
        int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        zoomIn = new JMenuItem("Zoom in");
        zoomIn.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_PLUS, modifier));
        zoomOut = new JMenuItem("Zoom out");
        zoomOut.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_MINUS, modifier));
        grid = new JCheckBoxMenuItem("Tile grid");
        grid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                modifier));
        grid.setState(true);
        
        add(zoomIn);
        add(zoomOut);
        add(grid);
        addActions(window);
    }
    
    public boolean getGrid() {
        return grid.getState();
    }
    
    private void addActions(Window window) {
        grid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                window.setGrid(grid.getState());
            }
        });
        zoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                window.zoomIn();
            }
        });
        zoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                window.zoomOut();
            }
        });
    }
}
