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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class TileCanvas extends JPanel {
    private final int MAX_SCALE = 128;
    private int scale;
    private byte[] data;
    private boolean[] overlay;
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
                    event.beforeChange(tileX, tileY);
                }
                onPixel(tileX, tileY, false);
                return;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int tileX = e.getX()/scale;
                int tileY = e.getY()/scale;
                onPixel(tileX, tileY, true);
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
                onPixel(tileX, tileY, false);
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
    
    public byte getCurrentColor() {
        return currentColor;
    }
    
    private void onPixel(int x, int y, boolean end) {
        if(event != null){
            event.canvasUpdate(x, y, end);
        }
    }
    
    public void setPixel(int x, int y, byte value) {
        if(x >= 0 && x < w && y >= 0 && y < h){
            data[y*w+x] = (byte)(value&0b00000011);
            repaint();
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
        // TODO: Keep data when resizing?
        data = new byte[w*h];
        overlay = new boolean[w*h];
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
    
    public void clearOverlay() {
        Arrays.fill(overlay, false);
    }
    
    public void setOverlayPixel(int x, int y, boolean value) {
        if(x >= 0 && x < w && y >= 0 && y < h){
            overlay[y*w+x] = value;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                Color color = new Color(palette[data[y*w+x]][0],
                        palette[data[y*w+x]][1], palette[data[y*w+x]][2]);
                g.setColor(color);
                g.fillRect(x*scale, y*scale, scale, scale);
                if(overlay[y*w+x]){
                    g.setColor(new Color(255, 255, 255, 127));
                    g.fillRect(x*scale, y*scale, scale, scale);
                }
            }
        }
    }
}
