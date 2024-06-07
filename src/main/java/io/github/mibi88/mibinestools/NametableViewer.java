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
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class NametableViewer extends JPanel {
    private CHRData chrData;
    private int[][] palette;
    private int scale;
    private boolean grid;
    private NametableViewerEvent event;
    private byte[] tiles;
    private byte[] attributes;
    private byte currentTile;
    private int chrBank;
    public NametableViewer(CHRData chrData, int[][] palette, int scale,
            boolean grid) {
        super();
        this.chrData = chrData;
        this.palette = palette;
        this.scale = scale;
        this.grid = grid;
        currentTile = Byte.MIN_VALUE;
        tiles = new byte[32*30];
        attributes = new byte[64];
        Arrays.fill(tiles, Byte.MIN_VALUE);
        Dimension size = new Dimension(scale*8*32+16,
                scale*8*30+16);
        setPreferredSize(size);
        repaint();
        handleMouse();
    }
    
    public void reset() {
        return;
    }
    
    public void setPalette(int[][] palette) {
        this.palette = palette;
        repaint();
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
    
    public void save(File file) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(file);
        fileStream.write(tiles);
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
    
    public void setEventHandler(NametableViewerEvent event) {
        this.event = event;
    }
    
    public void setCurrentTile(int currentTile) {
        this.currentTile = (byte)(currentTile-Byte.MIN_VALUE);
        System.out.println(this.currentTile);
        System.out.println(Byte.MIN_VALUE);
    }
    
    public void setCHRBank(int chrBank) {
        this.chrBank = chrBank;
    }
    
    private void handleMouse() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                return;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setTile(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setTile(e);
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
                setTile(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                return;
            }
        });
    }
    
    private void setTile(MouseEvent e) {
        int tileX = e.getX()/(scale*8);
        int tileY = e.getY()/(scale*8);
        if(event != null){
            event.tileChanged(tileX, tileY);
            if(tileX >= 0 && tileX < 32 && tileY >= 0 && tileY < 30){
                tiles[tileY*32+tileX] = currentTile;
            }
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
                BufferedImage image = chrData.generateTileImage(tile,
                        palette, scale);
                g.drawImage(image, x*8*scale, y*8*scale, this);
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
    }
}
