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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author mibi88
 */
public class CHREditor extends Editor {
    private CHRData chrData;
    private int[][] currentPalette;
    private int scale;
    
    private JSplitPane splitPane;
    private JTabbedPane editorPane;
    
    private JScrollPane patternTablePane;
    private PatternTable patternTable;
    
    private TileEditor tileEditor;
    
    private PaletteEditor paletteEditor;
    
    private Window window;
    
    /**
     * Initialize the CHR Editor
     * @param window
     */
    public CHREditor(Window window) {
        super("CHR Editor", new GridLayout(1, 2));
        scale = window.getScale();
        chrData = new CHRData();
        currentPalette = new int[][]{
            {0, 0, 0},
            {79, 79, 79},
            {184, 184, 184},
            {254, 254, 254}
        };
        this.window = window;
        initEditor();
    }
    
    private void initEditor() {
        chrData = new CHRData();
        patternTable = new PatternTable(chrData, currentPalette, scale,
                window.getGrid());
        initPatternTable();
        patternTablePane = new JScrollPane(patternTable);
        patternTablePane.revalidate();
        patternTablePane.setMinimumSize(new Dimension(300, 0));
        tileEditor = new TileEditor(32, currentPalette,
                (byte)1, this);
        paletteEditor = new PaletteEditor(currentPalette,
                this);
        
        editorPane = new JTabbedPane();
        editorPane.addTab("Tile Editor", tileEditor);
        editorPane.addTab("Palette Editor", paletteEditor);
        editorPane.setMinimumSize(new Dimension(250, 0));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
                patternTablePane, editorPane);
        
        add(splitPane);
    }
    
    private void loadSelectedTile(int tx, int ty) {
        try {
            tileEditor.loadTile(chrData.getTile(ty*16+tx), tx, ty);
        } catch (Exception ex) {
            Logger.getLogger(CHREditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    private void initPatternTable() {
        patternTable.setEventHandler(new PatternTableEvent() {
            @Override
            public void tileSelected(int tx, int ty) {
                loadSelectedTile(tx, ty);
            }
        });
    }
    
    /**
     * Loads a CHR file from the disk
     * 
     * @param file The file to load the CHR from
     * @return 
     */
    @Override
    public boolean openFile(File file) {
        try {
            if(super.openFile(file)){
                chrData = new CHRData(file);
                patternTable.setCHR(chrData);
                patternTablePane.revalidate();
                loadSelectedTile(patternTable.getSelectedX(),
                        patternTable.getSelectedY());
                return true;
            }
        } catch (Exception ex) {
            error();
            Logger.getLogger(CHREditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * Save the current file
     */
    @Override
    public void saveFile() {
        File file = getFile();
        if(file != null){
            try {
                chrData.saveCHRData(file);
                super.saveFile();
            } catch (Exception ex) {
                Logger.getLogger(CHREditor.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Save the file as...
     * @param file The file to save the data to
     */
    @Override
    public void saveAsFile(File file) {
        try {
            chrData.saveCHRData(file);
            super.saveAsFile(file);
        } catch (Exception ex) {
            Logger.getLogger(CHREditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Create a new file
     * @return 
     */
    @Override
    public boolean newFile() {
        if(super.newFile()){
            chrData.resetCHRData(2);
            chrData.resetRawData(2);
            patternTable.reset();
            tileEditor.reset();
            paletteEditor.reset();
            return true;
        }
        return false;
    }
    
    public void updateTile(byte[] data, int tx, int ty) {
        try {
            chrData.setTile(data, ty*16+tx);
            patternTable.repaint();
            fileEdited();
        } catch (Exception ex) {
            Logger.getLogger(CHREditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void setGrid(boolean grid) {
        if(patternTable != null){
            patternTable.setGrid(grid);
            patternTablePane.revalidate();
        }
    }
    
    @Override
    public void setScale(int scale) {
        this.scale = scale;
        if(patternTable != null){
            patternTable.setScale(scale);
            patternTablePane.revalidate();
        }
    }
    
    public void setPalette(int[][] palette) {
        patternTable.setPalette(palette);
        tileEditor.setPalette(palette);
    }
    
    public int[][] getCurrentPalette() {
        return paletteEditor.getCurrentPalette();
    }
}
