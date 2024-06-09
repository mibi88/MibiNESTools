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
package io.github.mibi88.mibinestools.chr_editor;

import io.github.mibi88.mibinestools.Editor;
import io.github.mibi88.mibinestools.palette_editor.PaletteEditor;
import io.github.mibi88.mibinestools.Window;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.undo.UndoManager;

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
    
    private UndoManager undoManager;
    
    // It seems complicated to use the clipboard for binary data, so I'm just
    // storing it that way
    private byte[] clipboard;
    
    /**
     * Initialize the CHR Editor.
     * @param window The window in which the editor is.
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
        undoManager = new UndoManager();
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
     * Loads a CHR file from the disk.
     * 
     * @param file The file to load the CHR from.
     * @return Returns true if the file was opened.
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
                undoManager.die();
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
     * Save the current file.
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
     * @param file The file to save the data to.
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
     * Create a new file.
     * @return Returns true if the file was created.
     */
    @Override
    public boolean newFile() {
        if(super.newFile()){
            chrData.resetCHRData(2);
            chrData.resetRawData(2);
            patternTable.reset();
            tileEditor.reset();
            paletteEditor.reset();
            undoManager.die();
            return true;
        }
        return false;
    }
    
    /**
     * Update a tile in the pattern table.
     * @param data The data of the tile.
     * @param tx The position of the tile.
     * @param ty The position of the tile.
     */
    public void updateTile(byte[] data, int tx, int ty) {
        try {
            chrData.setTile(data, ty*16+tx);
            patternTable.repaint();
            if(tileEditor.getTileX() == tx && tileEditor.getTileY() == ty){
                tileEditor.loadTile(data, tx, ty);
            }
            fileEdited();
        } catch (Exception ex) {
            Logger.getLogger(CHREditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Enable or disable the grid in tilemaps etc.
     * @param grid True if the grid should be displayed.
     */
    @Override
    public void setGrid(boolean grid) {
        if(patternTable != null){
            patternTable.setGrid(grid);
            patternTablePane.revalidate();
        }
    }
    
    /**
     * Set the scale of the content.
     * @param scale The scale.
     */
    @Override
    public void setScale(int scale) {
        this.scale = scale;
        if(patternTable != null){
            patternTable.setScale(scale);
            patternTablePane.revalidate();
        }
    }
    
    /**
     * Set the palette to draw the tiles with.
     * @param palette The palette.
     */
    @Override
    public void setPalette(int[][] palette) {
        patternTable.setPalette(palette);
        tileEditor.setPalette(palette);
    }
    
    /**
     * Get the currently used palette.
     * @return The currently used palette.
     */
    public int[][] getCurrentPalette() {
        return paletteEditor.getCurrentPalette();
    }
    
    /**
     * Add an edit to the undoManager.
     * @param edit The edit to add.
     */
    public void addEdit(CHREdit edit) {
        undoManager.addEdit(edit);
    }
    
    /**
     * Undo the last action.
     */
    @Override
    public void undo() {
        if(undoManager.canUndo()){
            undoManager.undo();
        }
    }
    
    /**
     * Redo the last action.
     */
    @Override
    public void redo() {
        if(undoManager.canRedo()){
            undoManager.redo();
        }
    }
    
    /**
     * Copy the selected tile.
     */
    @Override
    public void copy() {
        clipboard = tileEditor.getTile().clone();
    }
    
    /**
     * Cut the selected tile.
     */
    @Override
    public void cut() {
        clipboard = tileEditor.getTile().clone();
        byte[] oldTile = clipboard.clone();
        byte[] newTile = new byte[8*8];
        updateTile(newTile, patternTable.getSelectedX(),
                patternTable.getSelectedY());
        addEdit(new CHREdit(this, oldTile, newTile,
                patternTable.getSelectedX(),
                patternTable.getSelectedY()));
    }
    
    /**
     * Paste a tile to the selected tile.
     */
    @Override
    public void paste() {
        byte[] oldTile = tileEditor.getTile();
        updateTile(clipboard.clone(), patternTable.getSelectedX(),
                patternTable.getSelectedY());
        addEdit(new CHREdit(this, oldTile, clipboard,
                patternTable.getSelectedX(),
                patternTable.getSelectedY()));
    }
}
