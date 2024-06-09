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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author mibi88
 */
public class Aboutmenu extends JMenu {
    JMenuItem about;
    AboutPopup aboutPopup;

    /**
     * Create the About menu.
     * @param window The editor window.
     */
    public Aboutmenu(Window window) {
        super("About");
        about = new JMenuItem("About");
        add(about);
        addActions(window);
    }
    
    private void addActions(Window window) {
        aboutPopup = new AboutPopup();
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aboutPopup.show(window);
            }
        });
    }
}
