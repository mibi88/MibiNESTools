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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author mibi88
 */
public class EditablePalette extends Palette {
    private int[][] paletteData;
    private PaletteEvent event;

    /**
     * Create a new editable palette.
     * @param palette The colors of the palette.
     * @param colorList The color list to use with it.
     */
    public EditablePalette(int[][] palette, ColorList colorList) {
        super(palette);
        paletteData = palette.clone();
        for(int i=0;i<4;i++){
            final byte index = (byte)i;
            colors[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int[] currentColor = colorList.getCurrentColor();
                    Color color = new Color(currentColor[0], currentColor[1],
                            currentColor[2]);
                    colors[index].updateColor(color);
                    paletteData[index] = currentColor;
                    if(event != null){
                        event.paletteChanged();
                    }
                }
            });
        }
    }
    
    /**
     * Set the event handler to call when the palette has changed.
     * @param event The event handler.
     */
    public void setEventHandler(PaletteEvent event) {
        this.event = event;
    }
    
    /**
     * Get the palette data.
     * @return The palette data.
     */
    public int[][] getPaletteData() {
        return paletteData;
    }
}
