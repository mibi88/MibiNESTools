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

import io.github.mibi88.mibinestools.palette_editor.PaletteEditor;
import io.github.mibi88.mibinestools.chr_editor.CHRData;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class NametableViewer extends JPanel {
    private CHRData chrData;
    private PaletteEditor paletteEditor;
    private int scale;
    private boolean grid;
    private NametableViewerEvent event;
    private byte[] tiles;
    private byte[] attributes;
    private byte currentTile;
    private int chrBank;
    private int selectX, selectY, selectW, selectH;
    private boolean[] overlay;

    /**
     * Create a nametable viewer.
     * @param chrData The CHR data that contains the tiles.
     * @param paletteEditor The palette editor used to edit the palettes used to
     * display the nametable.
     * @param scale The scale of the content.
     * @param grid True if a grid should be drawn.
     */
    public NametableViewer(CHRData chrData, PaletteEditor paletteEditor,
            int scale, boolean grid) {
        super();
        this.chrData = chrData;
        this.paletteEditor = paletteEditor;
        this.scale = scale;
        this.grid = grid;
        currentTile = Byte.MIN_VALUE;
        reset();
        Arrays.fill(tiles, Byte.MIN_VALUE);
        Dimension size = new Dimension(scale*8*32+16,
                scale*8*30+16);
        setPreferredSize(size);
        repaint();
        handleMouse();
    }
    
    /**
     * Reset the nametable viewer.
     */
    public void reset() {
        tiles = new byte[32*30];
        overlay = new boolean[32*30];
        attributes = new byte[64];
    }
    
    /**
     * Set the CHR data to use to display the nametable.
     * @param chrData The CHR data to use.
     */
    public void setCHR(CHRData chrData) {
        this.chrData = chrData;
        repaint();
    }
    
    /**
     * Set if a grid should be drawn.
     * @param grid True if a grid should be displayed.
     */
    public void setGrid(boolean grid) {
        this.grid = grid;
        repaint();
    }
    
    /**
     * Set the scale of the content.
     * @param scale The scale.
     */
    public void setScale(int scale) {
        this.scale = scale;
        Dimension size = new Dimension(scale*8*32+16,
                scale*8*30+16);
        setPreferredSize(size);
        repaint();
    }
    
    /**
     * Set the selection
     * @param selectX1 The selection starting position.
     * @param selectY1 The selection starting position.
     * @param selectX2 The selection end position.
     * @param selectY2 The selection end position.
     */
    public void setSelection(int selectX1, int selectY1, int selectX2,
            int selectY2) {
        selectX1 = Math.max(0, Math.min(selectX1, 32));
        selectY1 = Math.max(0, Math.min(selectY1, 30));
        selectX2 = Math.max(0, Math.min(selectX2, 32));
        selectY2 = Math.max(0, Math.min(selectY2, 30));
        selectX = Math.min(selectX1, selectX2);
        selectY = Math.min(selectY1, selectY2);
        selectW = Math.max(selectX1, selectX2)-selectX;
        selectH = Math.max(selectY1, selectY2)-selectY;
        repaint();
    }
    
    /**
     * Save the nametable.
     * @param file The file to save the nametable to.
     * @throws IOException Gets thrown on failure.
     */
    public void save(File file) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(file);
        byte[] fixedTiles = tiles.clone();
        for(int i=0;i<fixedTiles.length;i++){
            fixedTiles[i] += Byte.MIN_VALUE;
        }
        fileStream.write(fixedTiles);
        fileStream.write(attributes);
        fileStream.close();
    }
    
    /**
     * Load a nametable from a file.
     * @param file The file to load the nametable from.
     * @throws IOException Gets thrown on failure.
     */
    public void open(File file) throws IOException {
        FileInputStream fileStream = new FileInputStream(file);
        fileStream.read(tiles);
        for(int i=0;i<tiles.length;i++){
            tiles[i] -= Byte.MIN_VALUE;
        }
        fileStream.read(attributes);
        fileStream.close();
    }
    
    /**
     * Set the content of the nametable viewer from byte arrays.
     * @param nametable The nametable data.
     * @param attributes The attribute table.
     */
    public void setData(byte[] nametable, byte[] attributes) {
        tiles = nametable;
        this.attributes = attributes;
        repaint();
    }
    
    /**
     * Set the event handler that handles editing the nametable.
     * @param event The event handler.
     */
    public void setEventHandler(NametableViewerEvent event) {
        this.event = event;
    }
    
    /**
     * Set the current tile.
     * @param currentTile The number of the current tile.
     */
    public void setCurrentTile(int currentTile) {
        this.currentTile = (byte)(currentTile-Byte.MIN_VALUE);
    }
    
    /**
     * Set the CHR bank to use to display this nametable.
     * @param chrBank The CHR bank to use.
     */
    public void setCHRBank(int chrBank) {
        this.chrBank = chrBank;
        repaint();
    }
    
    /**
     * Get the nametable data.
     * @return The nametable data.
     */
    public byte[] getNametable() {
        return tiles;
    }
    
    /**
     * Get the attribute table.
     * @return The attribute table.
     */
    public byte[] getAttributes() {
        return attributes;
    }
    
    /**
     * Get the tiles in the selection.
     * @return The tiles in the selection.
     */
    public byte[] getSelection() {
        if(selectW != 0 && selectH != 0){
            byte[] data = new byte[selectW*selectH];
            for(int y=0;y<selectH;y++){
                for(int x=0;x<selectW;x++){
                    data[y*selectW+x] = tiles[(selectY+y)*32+(selectX+x)];
                }
            }
            return data;
        }
        return null;
    }
    
    /**
     * Fill the selection
     * @param tile The tile to fill the selection with.
     */
    public void fillSelection(int tile) {
        for(int y=0;y<selectH;y++){
            for(int x=0;x<selectW;x++){
                tiles[(selectY+y)*32+(selectX+x)] =
                        (byte)(tile-Byte.MIN_VALUE);
            }
        }
        repaint();
    }
    
    /**
     * Fill the selection.
     * @param data The data to load in the selected area of the nametable.
     * @param w The width of the area.
     * @param h The height of the area.
     */
    public void fillSelection(byte[] data, int w, int h) {
        for(int y=0;y<Math.min(selectH, h);y++){
            for(int x=0;x<Math.min(selectW, w);x++){
                tiles[(selectY+y)*32+(selectX+x)] = data[y*w+x];
            }
        }
        repaint();
    }
    
    /**
     * Get the width of the selection.
     * @return The width of the selection.
     */
    public int getSelectionW() {
        return selectW;
    }
    
    /**
     * Get the height of the selection.
     * @return The height of the selection.
     */
    public int getSelectionH() {
        return selectH;
    }
    
    private void handleMouse() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                return;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(event != null){
                    int tileX = e.getX()/(scale*8);
                    int tileY = e.getY()/(scale*8);
                    event.beforeChange(tileX, tileY);
                }
                setTile(e, false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setTile(e, true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                return;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                return;
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setTile(e, false);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                return;
            }
        });
    }
    
    /**
     * Set a tile of the nametable
     * @param tileX The position of the tile.
     * @param tileY The position of the tile.
     */
    public void setTile(int tileX, int tileY) {
        if(tileX >= 0 && tileX < 32 && tileY >= 0 && tileY < 30){
            tiles[tileY*32+tileX] = currentTile;
        }
    }
    
    /**
     * Set the palette to use to draw a specific part of the nametable
     * @param tileX The position of the tile that should be drawn with this
     * palette.
     * @param tileY The position of the tile that should be drawn with this
     * palette.
     * @param palette The index of the palette to use.
     */
    public void setPalette(int tileX, int tileY, int palette) {
        if(tileX >= 0 && tileX < 32 && tileY >= 0 && tileY < 30){
            palette &= 0b00000011;
            int attrPos = (tileY/4)*8+(tileX/4);
            int pos = (tileY/2%2)*2+(tileX/2%2);
            attributes[attrPos] &= ~(0b11<<pos*2);
            attributes[attrPos] |= palette<<pos*2;
        }
    }
    
    private void setTile(MouseEvent e, boolean end) {
        int tileX = e.getX()/(scale*8);
        int tileY = e.getY()/(scale*8);
        if(event != null){
            event.tileChanged(tileX, tileY, end);
        }
        repaint();
    }
    
    /**
     * Set a pixel of the overlay.
     * @param x The position of the pixel.
     * @param y The position of the pixel.
     * @param value If the pixel should be on or off.
     */
    public void setOverlayPixel(int x, int y, boolean value) {
        if(x >= 0 && x < 32 && y >= 0 && y < 30){
            overlay[y*32+x] = value;
        }
    }
    
    /**
     * Turn all tiles of the overlay off.
     */
    public void clearOverlay() {
        Arrays.fill(overlay, false);
    }
    
    /**
     * Draw this widget.
     * @param g The awt Graphics.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for(int y=0;y<30;y++){
            for(int x=0;x<32;x++){
                int tile = chrBank*256+(int)tiles[y*32+x]-Byte.MIN_VALUE;
                int attrPos = (y/4)*8+(x/4);
                int pos = (y/2%2)*2+(x/2%2);
                int bit1 = 1<<pos*2;
                //System.out.println(Integer.toBinaryString(bit1|bit1<<1));
                int palette = attributes[attrPos]&(bit1|bit1<<1);
                palette >>= pos*2;
                //System.out.println(palette%4);
                try {
                    BufferedImage image = chrData.generateTileImage(
                            tile,
                            paletteEditor.getPalette(palette%4), scale);
                    g.drawImage(image, x*8*scale, y*8*scale, this);
                } catch (Exception ex) {
                    Logger.getLogger(NametableViewer.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                if(overlay[y*32+x]){
                    g.setColor(new Color(255, 255, 255, 127));
                    g.fillRect(x*8*scale, y*8*scale, 8*scale, 8*scale);
                }
            }
            if(grid){
                g.setColor(Color.GRAY);
                g.drawLine(0, y*8*scale, 32*8*scale, y*8*scale);
            }
        }
        g.setColor(Color.GRAY);
        if(grid){
            for(int x=0;x<32;x++){
                g.drawLine(x*8*scale, 0, x*8*scale, 30*8*scale);
            }
        }
        
        if(selectW != 0 && selectH != 0){
            // Show the selection
            g.setColor(Color.WHITE);
            g.drawLine(selectX*8*scale, selectY*8*scale,
                    (selectX+selectW)*8*scale, selectY*8*scale);
            g.drawLine(selectX*8*scale, (selectY+selectH)*8*scale,
                    (selectX+selectW)*8*scale, (selectY+selectH)*8*scale);
            g.drawLine(selectX*8*scale, selectY*8*scale, selectX*8*scale,
                    (selectY+selectH)*8*scale);
            g.drawLine((selectX+selectW)*8*scale, selectY*8*scale,
                    (selectX+selectW)*8*scale, (selectY+selectH)*8*scale);
        }
    }
}
