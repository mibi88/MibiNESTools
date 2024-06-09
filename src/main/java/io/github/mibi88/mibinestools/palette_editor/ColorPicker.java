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
package io.github.mibi88.mibinestools.palette_editor;

import io.github.mibi88.mibinestools.chr_editor.TileCanvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author mibi88
 */
public class ColorPicker extends Palette {
    private byte currentColor;
    public ColorPicker(int[][] palette, TileCanvas tileCanvas) {
        super(palette);
        for(int i=0;i<4;i++){
            final byte nesColor = (byte)i;
            colors[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentColor = nesColor;
                    tileCanvas.setCurrentColor(currentColor);
                }
            });
        }
    }
}
