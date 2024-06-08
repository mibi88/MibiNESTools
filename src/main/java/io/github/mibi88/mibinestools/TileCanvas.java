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
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class TileCanvas extends JPanel {
    private final int MAX_SCALE = 128;
    private int scale;
    private byte[] data;
    private int[][] palette;
    private int w, h;
    private byte currentColor;
    private CanvasEvent event;
    public TileCanvas(int scale, int w, int h, int[][] palette,
            byte currentColor) {
        super();
        this.scale = scale;
        this.palette = palette;
        this.currentColor = currentColor;
        data = null;
        updateSize(w, h);
        setPixelOnClick();
        repaint();
    }
    
    private void setPixelOnClick() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                return;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int tileX = e.getX()/scale;
                int tileY = e.getY()/scale;
                if(event != null){
                    event.beforeChange();
                }
                setPixel(tileX, tileY, currentColor, false);
                return;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int tileX = e.getX()/scale;
                int tileY = e.getY()/scale;
                setPixel(tileX, tileY, currentColor, true);
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
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int tileX = e.getX()/scale;
                int tileY = e.getY()/scale;
                setPixel(tileX, tileY, currentColor, false);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                return;
            }
        });
    }
    
    public void setCurrentColor(byte currentColor) {
        this.currentColor = currentColor;
    }
    
    public void setPixel(int x, int y, byte value, boolean end) {
        if(x >= 0 && x < w && y >= 0 && y < h){
            data[y*w+x] = (byte)(value&0b00000011);
            repaint();
            if(event != null){
                event.canvasUpdate(end);
            }
        }
    }
    
    public void zoomIn() {
        if(scale < MAX_SCALE){
            scale++;
            setPreferredSize(new Dimension(h*scale, w*scale));
            repaint();
        }
    }
    
    public void zoomOut() {
        if(scale > 1){
            scale--;
            setPreferredSize(new Dimension(h*scale, w*scale));
            repaint();
        }
    }
    
    public void updateSize(int w, int h) {
        // TODO: Keep data before resizing?
        data = new byte[w*h];
        this.w = w;
        this.h = h;
        setPreferredSize(new Dimension(h*scale, w*scale));
    }
    
    public void setEventHandler(CanvasEvent event) {
        this.event = event;
    }
    
    public void loadData(byte[] data) throws Exception {
        if(this.data.length == data.length){
            this.data = data;
            repaint();
        }else{
            throw new Exception("Bad data size!");
        }
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void setPalette(int[][] palette) {
        this.palette = palette;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                g.setColor(Color.red);
                Color color = new Color(palette[data[y*w+x]][0],
                        palette[data[y*w+x]][1], palette[data[y*w+x]][2]);
                g.setColor(color);
                g.fillRect(x*scale, y*scale, scale, scale);
            }
        }
    }
}
