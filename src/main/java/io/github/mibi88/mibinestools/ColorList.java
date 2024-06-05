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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class ColorList extends JPanel {
    private int[] currentColor;
    // TODO: Store palette colors in an image in the resources.
    private int[][] list = {
        // First row
        {102, 102, 102},
        {0, 42, 136},
        {20, 18, 168},
        {59, 0, 164},
        {92, 0, 126},
        {111, 0, 64},
        {108, 7, 0},
        {87, 29, 0},
        {52, 53, 0},
        {12, 73, 0},
        {0, 82, 0},
        {0, 79, 8},
        {0, 64, 78},
        {0, 0, 0},
        // 2nd row
        {174, 174, 174},
        {21, 95, 218},
        {66, 64, 254},
        {118, 39, 255},
        {161, 27, 205},
        {184, 30, 124},
        {181, 50, 32},
        {153, 79, 0},
        {108, 110, 0},
        {56, 135, 0},
        {13, 148, 0},
        {0, 144, 50},
        {0, 124, 142},
        {0, 0, 0},
        // 3rd line
        {254, 254, 254},
        {100, 176, 254},
        {147, 144, 254},
        {199, 119, 254},
        {243, 106, 254},
        {254, 110, 205},
        {254, 130, 112},
        {235, 159, 35},
        {189, 191, 0},
        {137, 217, 0},
        {93, 229, 48},
        {69, 225, 130},
        {72, 206, 223},
        {79, 79, 79},
        // 4th line
        {254, 254, 254},
        {193, 224, 254},
        {212, 211, 254},
        {233, 200, 254},
        {251, 195, 254},
        {254, 197, 235},
        {254, 205, 198},
        {247, 217, 166},
        {229, 230, 149},
        {208, 240, 151},
        {190, 245, 171},
        {180, 243, 205},
        {181, 236, 243},
        {184, 184, 184}
    };
    
    private ColorButton[] colors;
    public ColorList() {
        super(new GridLayout(4, 14));
        currentColor = new int[3];
        colors = new ColorButton[list.length];
        for(int i=0;i<list.length;i++){
            final int index = i;
            final int[] rgbColor = list[i];
            Color color = new Color(rgbColor[0], rgbColor[1], rgbColor[2]);
            colors[i] = new ColorButton(color);
            add(colors[i]);
            colors[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentColor = rgbColor;
                }
            });
        }
    }
    
    public int[] getCurrentColor() {
        return currentColor;
    }
}
