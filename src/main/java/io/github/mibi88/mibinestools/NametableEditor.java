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
    
    @Override
    public void setPalette(int[][] palette) {
        tilePicker.setPalette(palette);
    }
    
    @Override
    public void setPalette(int i) {
        paletteIndex = i;
        if(nametablePane != null){
            nametablePane.paletteChanged();
        }
    }
    
    public PaletteEditor getPaletteEditor() {
        return paletteEditor;
    }
    
    public int[][] getCurrentPalette() {
        return currentPalette;
    }
    
    public void setCHR(CHRData chrData) {
        nametablePane.setCHR(chrData);
    }
    
    public void setCurrentTile(int currentTile) {
        nametablePane.setCurrentTile(currentTile);
    }
    
    @Override
    public void setScale(int scale) {
        nametablePane.setScale(scale);
        tilePicker.setScale(scale);
    }
    
    @Override
    public boolean newFile() {
        if(super.newFile()){
            nametablePane.reset();
            undoManager.die();
            return true;
        }
        return false;
    }
    
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
    
    public void setCHRBank(int chrBank) {
        nametablePane.setCHRBank(chrBank);
    }
    
    public int getScale() {
        return window.getScale();
    }
    
    public CHRData getCHRData() {
        return tilePicker.getCHRData();
    }
    
    @Override
    public void undo() {
        if(undoManager.canUndo()){
            undoManager.undo();
        }
    }
    
    @Override
    public void redo() {
        if(undoManager.canRedo()){
            undoManager.redo();
        }
    }
    
    public void setData(byte[] nametable, byte[] attributes) {
        nametablePane.setData(nametable, attributes);
    }
    
    public void addEdit(NametableEdit edit) {
        undoManager.addEdit(edit);
    }
    
    @Override
    public void copy() {
        clipboard = nametablePane.getSelection();
        clipboardW = nametablePane.getSelectionW();
        clipboardH = nametablePane.getSelectionH();
    }
}
