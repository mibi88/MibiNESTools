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
import javax.swing.JButton;
import javax.swing.JScrollPane;

/**
 *
 * @author mibi88
 */
public class NametableEditor extends Editor {
    private NametableViewer nametableViewer;
    private PatternTable patternTable;
    private PaletteEditor paletteEditor;
    private JScrollPane nametableViewerPane;
    private JScrollPane patternTablePane;
    private JButton loadCHR;
    private CHRData chrData;
    private int[][] currentPalette;
    
    /**
     * Initialize the nametable editor
     */
    public NametableEditor(Window window) {
        super("Nametable Editor", new GridLayout(1, 4));
        currentPalette = new int[][]{
            {0, 0, 0},
            {79, 79, 79},
            {184, 184, 184},
            {254, 254, 254}
        };
        chrData = new CHRData();
        nametableViewer = new NametableViewer(chrData, currentPalette,
                window.getScale(), true);
        nametableViewerPane = new JScrollPane(nametableViewer);
        add(nametableViewerPane);
        patternTable = new PatternTable(chrData, currentPalette,
                window.getScale(), true);
        patternTablePane = new JScrollPane(patternTable);
        add(patternTablePane);
        paletteEditor = new PaletteEditor(currentPalette,
                this);
        add(paletteEditor);
        loadCHR = new JButton("Load CHR");
        add(loadCHR);
    }
    
    public void setPalette(int[][] palette) {
        nametableViewer.setPalette(palette);
        patternTable.setPalette(palette);
    }
}
