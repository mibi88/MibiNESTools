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
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class Tilemap extends JPanel {
    private CHRData chrData;
    private int[][] palette;
    private int scale;
    private boolean grid;
    private TilemapEvent event;
    private int selectedX, selectedY;
    public Tilemap(CHRData chrData, int[][] palette, int scale, boolean grid) {
        super();
        this.chrData = chrData;
        this.palette = palette;
        this.scale = scale;
        this.grid = grid;
        Dimension size = new Dimension(scale*8*16+16,
                scale*8*16*chrData.getChrBanks()+16);
        setPreferredSize(size);
        repaint();
        handleMouse();
    }
    
    public void reset() {
        selectedX = 0;
        selectedY = 0;
        if(event != null){
            event.tileSelected(selectedX, selectedY);
        }
        repaint();
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
        Dimension size = new Dimension(scale*8*16+16,
                scale*8*16*chrData.getChrBanks()+16);
        setPreferredSize(size);
        repaint();
    }
    
    public void setEventHandler(TilemapEvent event) {
        this.event = event;
    }
    
    public int getSelectedX() {
        return selectedX;
    }
    
    public int getSelectedY() {
        return selectedY;
    }
    
    private void handleMouse() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tileX = e.getX()/(scale*8);
                int tileY = e.getY()/(scale*8);
                if(tileX != selectedX || tileY != selectedY){
                    selectedX = tileX;
                    selectedY = tileY;
                    if(event != null){
                        event.tileSelected(selectedX, selectedY);
                    }
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                return;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                return;
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
    }
    
    /**
     *
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GRAY);
        for(int y=0;y<16*chrData.getChrBanks();y++){
            for(int x=0;x<16;x++){
                BufferedImage image = chrData.generateTileImage(y*16+x, palette,
                    scale);
                g.drawImage(image, x*8*scale, y*8*scale, this);
            }
            if(grid){
                g.drawLine(0, y*8*scale, 16*8*scale, y*8*scale);
            }
        }
        if(grid){
            for(int x=0;x<16;x++){
                g.drawLine(x*8*scale, 0, x*8*scale,
                        16*chrData.getChrBanks()*8*scale);
            }
        }
        // Show the selection
        g.setColor(Color.WHITE);
        g.drawLine(selectedX*8*scale, selectedY*8*scale, (selectedX+1)*8*scale,
                selectedY*8*scale);
        g.drawLine(selectedX*8*scale, (selectedY+1)*8*scale,
                (selectedX+1)*8*scale, (selectedY+1)*8*scale);
        g.drawLine(selectedX*8*scale, selectedY*8*scale, selectedX*8*scale,
                (selectedY+1)*8*scale);
        g.drawLine((selectedX+1)*8*scale, selectedY*8*scale,
                (selectedX+1)*8*scale, (selectedY+1)*8*scale);
    }
}
