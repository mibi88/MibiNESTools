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

import io.github.mibi88.mibinestools.Editor;
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

    /**
     * Initialize the palette editor.
     * @param defaultPalette The default palette.
     * @param editor The editor it is used with.
     */
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
    
    /**
     * Reset the palette editor.
     */
    public void reset() {
        //
    }
    
    private void handleEvents() {
        paletteList.setEventHandler(new PaletteListEvent() {
            @Override
            public void paletteChanged(int i) {
                usePalette(paletteChooser.getValue());
            }
        });
    }
    
    /**
     * Set the palette to use when rendering graphics.
     * @param i The palette index.
     */
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
    
    /**
     * Get the selected palette.
     * @return The palette data.
     */
    public int[][] getCurrentPalette() {
        return currentPalette;
    }
    
    /**
     * Get the palette at a specific index.
     * @param i The palette index.
     * @return The palette data.
     * @throws Exception Gets thrown if the index is out of bounds.
     */
    public int[][] getPalette(int i) throws Exception {
        final int index = (i%4)*2+i/4;
        return paletteList.getPaletteData(index);
    }
    
    /**
     * Get the index of the selected palette.
     * @return The palette index.
     */
    public int getCurrentPaletteIndex() {
        return currentPaletteIndex;
    }
}
