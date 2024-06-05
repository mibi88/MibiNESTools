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

import java.awt.GridLayout;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class PaletteList extends JPanel {
    private EditablePalette[] palettes;
    private PaletteListEvent event;
    public PaletteList(int[][] defaultPalette, ColorList colorList) {
        super(new GridLayout(4, 2));
        palettes = new EditablePalette[8];
        for(int i=0;i<8;i++) {
            final int index = i;
            palettes[i] = new EditablePalette(defaultPalette, colorList);
            add(palettes[i]);
            palettes[i].setEventHandler(new PaletteEvent() {
                @Override
                public void paletteChanged() {
                    if(event != null){
                        event.paletteChanged(index);
                    }
                }
            });
        }
    }
    
    public void setEventHandler(PaletteListEvent event) {
        this.event = event;
    }
    
    public int[][] getPaletteData(int i) throws Exception {
        if(i >= 0 && i < 8){
            return palettes[i].getPaletteData();
        }
        throw new Exception("Bad palette index!");
    }
}
