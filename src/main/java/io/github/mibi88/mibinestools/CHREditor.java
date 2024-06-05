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
import java.awt.GridLayout;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author mibi88
 */
public class CHREditor extends JPanel {
    private CHRData chrData;
    private int[][] currentPalette;
    private int scale;
    
    private JScrollPane tilemapPane;
    private Tilemap tilemap;
    
    private TileEditor tileEditor;
    
    private PaletteEditor paletteEditor;
    
    private Window window;
    
    /**
     * Initialize the CHR Editor
     * @param window
     */
    public CHREditor(Window window) {
        super(new GridLayout(1, 2));
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
        tilemap = new Tilemap(chrData, currentPalette, scale,
                window.getGrid());
        initTilemap();
        tilemapPane = new JScrollPane(tilemap);
        add(tilemapPane, BorderLayout.CENTER);
        tilemapPane.revalidate();
        tileEditor = new TileEditor(32, currentPalette,
                (byte)1, this);
        add(tileEditor);
        paletteEditor = new PaletteEditor(currentPalette, this);
        add(paletteEditor);
    }
    
    private void loadSelectedTile(int tx, int ty) {
        try {
            tileEditor.loadTile(chrData.getTile(ty*16+tx), tx, ty);
        } catch (Exception ex) {
            Logger.getLogger(CHREditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    private void initTilemap() {
        tilemap.setEventHandler(new TilemapEvent() {
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
     * @throws Exception thrown when failing to load the file
     */
    public void loadFile(File file) throws Exception {
        chrData = new CHRData(file);
        tilemap.setCHR(chrData);
        tilemapPane.revalidate();
        loadSelectedTile(tilemap.getSelectedX(), tilemap.getSelectedY());
    }
    
    public void saveFile(File file) throws Exception {
        chrData.saveCHRData(file);
    }
    
    public void newFile() {
        chrData.resetCHRData(2);
        chrData.resetRawData(2);
        tilemap.reset();
        tileEditor.reset();
        paletteEditor.reset();
    }
    
    public void updateTile(byte[] data, int tx, int ty) {
        try {
            chrData.setTile(data, ty*16+tx);
            tilemap.repaint();
        } catch (Exception ex) {
            Logger.getLogger(CHREditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    public void setGrid(boolean grid) {
        if(tilemap != null){
            tilemap.setGrid(grid);
            tilemapPane.revalidate();
        }
    }
    
    public void setScale(int scale) {
        this.scale = scale;
        if(tilemap != null){
            tilemap.setScale(scale);
            tilemapPane.revalidate();
        }
    }
    
    public void setPalette(int[][] palette) {
        tilemap.setPalette(palette);
        tileEditor.setPalette(palette);
    }
}
