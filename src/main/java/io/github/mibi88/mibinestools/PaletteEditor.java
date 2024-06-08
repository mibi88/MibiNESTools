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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class PaletteEditor extends JPanel {
    private PaletteList paletteList;
    private ColorList colorList;
    private PaletteChooser paletteChooser;
    private Editor editor;
    private int[][] currentPalette;
    private int currentPaletteIndex;
    public PaletteEditor(int[][] defaultPalette, Editor editor) {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0.75;
        c.gridx = 0;
        c.gridy = 0;
        this.editor = editor;
        colorList = new ColorList();
        add(colorList, c);
        c.gridx = 0;
        c.gridy = 1;
        paletteList = new PaletteList(defaultPalette, colorList);
        add(paletteList, c);
        c.weighty = 0.25;
        c.gridx = 0;
        c.gridy = 2;
        paletteChooser = new PaletteChooser(this);
        add(paletteChooser, c);
        currentPalette = defaultPalette;
        handleEvents();
    }
    
    public void reset() {
        //
    }
    
    public void handleEvents() {
        paletteList.setEventHandler(new PaletteListEvent() {
            @Override
            public void paletteChanged(int i) {
                usePalette(paletteChooser.getValue());
            }
        });
    }
    
    public void usePalette(int i) {
        try {
            // Adapt index of the palette to use 
            final int index = (i%4)*2+i/4;
            currentPalette = paletteList.getPaletteData(index);
            editor.setPalette(currentPalette);
            currentPaletteIndex = i;
            editor.setPalette(i);
        } catch (Exception ex) {
            Logger.getLogger(PaletteEditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    public int[][] getCurrentPalette() {
        return currentPalette;
    }
    
    public int[][] getPalette(int i) throws Exception {
        final int index = (i%4)*2+i/4;
        return paletteList.getPaletteData(index);
    }
    
    public int getCurrentPaletteIndex() {
        return currentPaletteIndex;
    }
}
