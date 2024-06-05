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

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class Palette extends JPanel {
    protected ColorButton[] colors;
    public Palette(int[][] palette) {
        super(new GridLayout(1, 3));
        colors = new ColorButton[4];
        for(int i=0;i<4;i++){
            final byte nesColor = (byte)i;
            Color color = new Color(palette[i][0], palette[i][1],
                    palette[i][2]);
            colors[i] = new ColorButton(color);
            add(colors[i]);
        }
    }
    
    public void updatePalette(int[][] palette) {
        for(int i=0;i<4;i++){
            Color color = new Color(palette[i][0], palette[i][1],
                    palette[i][2]);
            colors[i].updateColor(color);
        }
    }
}
