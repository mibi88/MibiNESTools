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
package io.github.mibi88.mibinestools.nametable_editor;

import io.github.mibi88.mibinestools.Editor;
import io.github.mibi88.mibinestools.palette_editor.PaletteEditor;
import io.github.mibi88.mibinestools.Window;
import io.github.mibi88.mibinestools.chr_editor.CHRData;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.undo.UndoManager;

/**
 *
 * @author mibi88
 */
public class NametableEditor extends Editor {
    private NametablePane nametablePane;
    private PaletteEditor paletteEditor;
    private JTabbedPane tilePane;
    private JSplitPane splitPane;
    private TilePicker tilePicker;
    private int[][] currentPalette;
    private int paletteIndex;
    private Window window;
    private UndoManager undoManager;
    private byte[] clipboard;
    private int clipboardW, clipboardH;
    
    /**
     * Initialize the nametable editor
     */
    public NametableEditor(Window window) {
        super("Nametable Editor", new GridLayout(1, 4));
        undoManager = new UndoManager();
        currentPalette = new int[][]{
            {0, 0, 0},
            {79, 79, 79},
            {184, 184, 184},
            {254, 254, 254}
        };
        tilePicker = new TilePicker(this, window);
        this.window = window;
        paletteEditor = new PaletteEditor(currentPalette,
                this);
        nametablePane = new NametablePane(this);
        nametablePane.setMinimumSize(new Dimension(300, 0));
        
        tilePane = new JTabbedPane();
        tilePane.addTab("Pattern Table", tilePicker);
        tilePane.addTab("Palette Editor", paletteEditor);
        tilePane.setMinimumSize(new Dimension(250, 0));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
                nametablePane, tilePane);
        add(splitPane);
    }
    
    /**
     * Set the palette to use to display the pattern table.
     * @param palette The palette to use.
     */
    @Override
    public void setPalette(int[][] palette) {
        tilePicker.setPalette(palette);
    }
    
    /**
     * Set the index of the pakette to use.
     * @param i The index of the palette.
     */
    @Override
    public void setPalette(int i) {
        paletteIndex = i;
        if(nametablePane != null){
            nametablePane.paletteChanged();
        }
    }
    
    /**
     * Get the palette editor.
     * @return The palette editor.
     */
    public PaletteEditor getPaletteEditor() {
        return paletteEditor;
    }
    
    /**
     * Get the currently used palette.
     * @return The currently used palette.
     */
    public int[][] getCurrentPalette() {
        return currentPalette;
    }
    
    /**
     * Set the CHR data to use to display the nametable.
     * @param chrData The CHR data to use.
     */
    public void setCHR(CHRData chrData) {
        nametablePane.setCHR(chrData);
    }
    
    /**
     * Set the tile to use to edit the nametable.
     * @param currentTile The tile to use.
     */
    public void setCurrentTile(int currentTile) {
        nametablePane.setCurrentTile(currentTile);
    }
    
    /**
     * Set the scale of the content.
     * @param scale The scale.
     */
    @Override
    public void setScale(int scale) {
        nametablePane.setScale(scale);
        tilePicker.setScale(scale);
    }
    
    /**
     * Create a new nametable.
     * @return Returns true if the nametable was created.
     */
    @Override
    public boolean newFile() {
        if(super.newFile()){
            nametablePane.reset();
            undoManager.die();
            return true;
        }
        return false;
    }
    
    /**
     * Open a nametable.
     * @param file The file to open the nametable from.
     * @return Returns true if the file was opened.
     */
    @Override
    public boolean openFile(File file) {
        if(super.openFile(file)){
            try {
                nametablePane.open(file);
                undoManager.die();
            } catch (IOException ex) {
                Logger.getLogger(NametableEditor.class.getName()).log(
                        Level.SEVERE, null, ex);
                error();
            }
        }
        return true;
    }
    
    /**
     * Save the nametable.
     */
    @Override
    public void saveFile() {
        File file = getFile();
        if(file != null){
            try {
                nametablePane.save(file);
                super.saveFile();
            } catch (IOException ex) {
                Logger.getLogger(NametableEditor.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Save the nametable to a specific file.
     * @param file The file to save the nametable to.
     */
    @Override
    public void saveAsFile(File file) {
        try {
            nametablePane.save(file);
            super.saveAsFile(file);
        } catch (IOException ex) {
            Logger.getLogger(NametableEditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Set the CHR bank to use to display the nametable.
     * @param chrBank The CHR bank to use.
     */
    public void setCHRBank(int chrBank) {
        nametablePane.setCHRBank(chrBank);
    }
    
    /**
     * Get the scale of the content.
     * @return Returns the scale.
     */
    public int getScale() {
        return window.getScale();
    }
    
    /**
     * Enable or disable the grid.
     * @param grid True if the grid should be drawn.
     */
    @Override
    public void setGrid(boolean grid) {
        nametablePane.setGrid(grid);
        tilePicker.setGrid(grid);
    }
    
    /**
     * Get the CHR data used to display the nametable.
     * @return
     */
    public CHRData getCHRData() {
        return tilePicker.getCHRData();
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
     * Set the nametable data from arrays.
     * @param nametable The nametable.
     * @param attributes The attribute table.
     */
    public void setData(byte[] nametable, byte[] attributes) {
        nametablePane.setData(nametable, attributes);
    }
    
    /**
     * Add an edit to the undoManager.
     * @param edit The edit to add.
     */
    public void addEdit(NametableEdit edit) {
        undoManager.addEdit(edit);
    }
    
    /**
     * Copy the selection.
     */
    @Override
    public void copy() {
        clipboard = nametablePane.getSelection();
        clipboardW = nametablePane.getSelectionW();
        clipboardH = nametablePane.getSelectionH();
    }
    
    /**
     * Cut the selection.
     */
    @Override
    public void cut() {
        copy();
        byte[] oldNametable = nametablePane.getNametable().clone();
        byte[] oldAttributes = nametablePane.getAttributes().clone();
        nametablePane.fillSelection(0);
        addEdit(new NametableEdit(this, oldAttributes, oldNametable,
                nametablePane.getAttributes(),
                nametablePane.getNametable()));
    }
    
    /**
     * Paste the selection.
     */
    @Override
    public void paste() {
        if(clipboard != null){
            byte[] oldNametable = nametablePane.getNametable().clone();
            byte[] oldAttributes = nametablePane.getAttributes().clone();
            nametablePane.fillSelection(clipboard, clipboardW,
                    clipboardH);
            addEdit(new NametableEdit(this, oldAttributes, oldNametable,
                    nametablePane.getAttributes(),
                    nametablePane.getNametable()));
        }
    }
}
