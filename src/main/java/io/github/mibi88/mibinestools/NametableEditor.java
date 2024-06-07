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

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author mibi88
 */
public class NametableEditor extends Editor {
    private NametableViewer nametableViewer;
    private JScrollPane nametableViewerPane;
    private PaletteEditor paletteEditor;
    private JTabbedPane tilePane;
    private JSplitPane splitPane;
    private TilePicker tilePicker;
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
        tilePicker = new TilePicker(this, window);
        nametableViewer = new NametableViewer(tilePicker.getCHRData(),
                currentPalette, window.getScale(), true);
        nametableViewerPane = new JScrollPane(nametableViewer);
        nametableViewerPane.setMinimumSize(new Dimension(300, 0));
        paletteEditor = new PaletteEditor(currentPalette,
                this);
        
        tilePane = new JTabbedPane();
        tilePane.addTab("Pattern Table", tilePicker);
        tilePane.addTab("Palette Editor", paletteEditor);
        tilePane.setMinimumSize(new Dimension(250, 0));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
                nametableViewerPane, tilePane);
        add(splitPane);
        
        nametableViewer.setEventHandler(new NametableViewerEvent() {
            @Override
            public void tileChanged(int tx, int ty) {
                return;
            }
        });
    }
    
    public void setPalette(int[][] palette) {
        nametableViewer.setPalette(palette);
        tilePicker.setPalette(palette);
    }
    
    public int[][] getCurrentPalette() {
        return currentPalette;
    }
    
    public void setCHR(CHRData chrData) {
        nametableViewer.setCHR(chrData);
    }
    
    public void setCurrentTile(int currentTile) {
        nametableViewer.setCurrentTile(currentTile);
    }
    
    @Override
    public void setScale(int scale) {
        nametableViewer.setScale(scale);
        tilePicker.setScale(scale);
    }
}
