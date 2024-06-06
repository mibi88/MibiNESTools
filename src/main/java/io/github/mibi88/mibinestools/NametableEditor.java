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

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class NametableEditor extends Editor {

    /**
     * Initialize the nametable editor
     */
    public NametableEditor() {
        super("Nametable Editor");
        JButton button = new JButton("Nametable editor test");
        add(button);
    }
}
