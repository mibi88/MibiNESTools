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
    
    public void reset() {
        tiles = new byte[32*30];
        attributes = new byte[64];
    }
    
    public void setCHR(CHRData chrData) {
        this.chrData = chrData;
        repaint();
    }
    
    public void setGrid(boolean grid) {
        this.grid = grid;
        repaint();
    }
    
    public void setScale(int scale) {
        this.scale = scale;
        Dimension size = new Dimension(scale*8*32+16,
                scale*8*30+16);
        setPreferredSize(size);
        repaint();
    }
    
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
    
    public void open(File file) throws IOException {
        FileInputStream fileStream = new FileInputStream(file);
        fileStream.read(tiles);
        for(int i=0;i<tiles.length;i++){
            tiles[i] -= Byte.MIN_VALUE;
        }
        fileStream.read(attributes);
        fileStream.close();
    }
    
    public void setData(byte[] nametable, byte[] attributes) {
        tiles = nametable;
        this.attributes = attributes;
        repaint();
    }
    
    public void setEventHandler(NametableViewerEvent event) {
        this.event = event;
    }
    
    public void setCurrentTile(int currentTile) {
        this.currentTile = (byte)(currentTile-Byte.MIN_VALUE);
    }
    
    public void setCHRBank(int chrBank) {
        this.chrBank = chrBank;
        repaint();
    }
    
    public byte[] getNametable() {
        return tiles;
    }
    
    public byte[] getAttributes() {
        return attributes;
    }
    
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
    
    public void fillSelection(int tile) {
        for(int y=0;y<selectH;y++){
            for(int x=0;x<selectW;x++){
                tiles[(selectY+y)*32+(selectX+x)] =
                        (byte)(tile-Byte.MIN_VALUE);
            }
        }
        repaint();
    }
    
    public void fillSelection(byte[] data, int w, int h) {
        for(int y=0;y<Math.min(selectH, h);y++){
            for(int x=0;x<Math.min(selectW, w);x++){
                tiles[(selectY+y)*32+(selectX+x)] = data[y*w+x];
            }
        }
        repaint();
    }
    
    public int getSelectionW() {
        return selectW;
    }
    
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
    
    public void setTile(int tileX, int tileY) {
        if(tileX >= 0 && tileX < 32 && tileY >= 0 && tileY < 30){
            tiles[tileY*32+tileX] = currentTile;
        }
    }
    
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
     *
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GRAY);
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
            }
            if(grid){
                g.drawLine(0, y*8*scale, 32*8*scale, y*8*scale);
            }
        }
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
