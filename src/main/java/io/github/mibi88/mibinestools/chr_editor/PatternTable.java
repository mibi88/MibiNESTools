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

import io.github.mibi88.mibinestools.chr_editor.CHRData;
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
public class PatternTable extends JPanel {
    private CHRData chrData;
    private int[][] palette;
    private int scale;
    private boolean grid;
    private PatternTableEvent event;
    private int selectedX, selectedY;

    /**
     * Creates a pattern table widget.
     * @param chrData The CHR data to display.
     * @param palette The palette to use.
     * @param scale The scale of the content.
     * @param grid True if a grid should be drawn.
     */
    public PatternTable(CHRData chrData, int[][] palette, int scale,
            boolean grid) {
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
    
    /**
     * Reset the pattern table.
     */
    public void reset() {
        selectedX = 0;
        selectedY = 0;
        if(event != null){
            event.tileSelected(selectedX, selectedY);
        }
        repaint();
    }
    
    /**
     * Set the palette to use to display the tiles.
     * @param palette The palette to use.
     */
    public void setPalette(int[][] palette) {
        this.palette = palette;
        repaint();
    }
    
    /**
     * Set the CHR data to display.
     * @param chrData The CHR data to display.
     */
    public void setCHR(CHRData chrData) {
        this.chrData = chrData;
        repaint();
    }
    
    /**
     * Enable or disable the grid in the CHR editor.
     * @param grid True if the grid should be drawn.
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
        Dimension size = new Dimension(scale*8*16+16,
                scale*8*16*chrData.getChrBanks()+16);
        setPreferredSize(size);
        repaint();
    }
    
    /**
     * Set the event handler to call when the selection changes.
     * @param event The event handler.
     */
    public void setEventHandler(PatternTableEvent event) {
        this.event = event;
    }
    
    /**
     * Get the position of the selection on the X axis.
     * @return The position of the selection on the X axis.
     */
    public int getSelectedX() {
        return selectedX;
    }
    
    /**
     * Get the position of the selection on the Y axis.
     * @return Get the position of the selection on the Y axis.
     */
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
     * Draw this widget
     * @param g The awt Graphics.
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
