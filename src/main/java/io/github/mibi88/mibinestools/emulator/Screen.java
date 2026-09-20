/*
 * MibiNESTools - Create NES games easily!
 * Copyright (C) 2024, 2026  Mibi88
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

package io.github.mibi88.mibinestools.emulator;

import io.github.mibi88.mibinestools.palette_editor.ColorList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This class emulates the NES PPU.
 * @author mibi88
 */
public class Screen extends JPanel {
    protected final int width = 256;
    protected final int height = 240;
    
    private int x;
    private int y;
    
    private BufferedImage image;
    
    /**
     * Create a new screen.
     */
    public Screen() {
        super();
        
        image = new BufferedImage(256, 240, BufferedImage.TYPE_INT_RGB);
        
        reset();
    }
    
    public void reset() {
        x = 0;
        y = 0;
    }
    
    public void putPixel(byte color) {
        int[] rgbColor = getColor(Byte.toUnsignedInt(color));
        image.setRGB(x, y, (rgbColor[0]<<16)|(rgbColor[1]<<8)|rgbColor[2]);
        
        x++;
        
        if(x >= width){
            x = 0;
            y++;
            
            if(y >= height) {
                y = 0;
                
                Screen thisScreen = this;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        thisScreen.repaint();
                    }
                });
            }
        }
    }
    
    /**
     * Render the screen.
     * @param g The AWT graphics.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Dimension size = getSize();
        
        int w = (int)size.getWidth();
        int h = (int)size.getHeight();
        
        int sw = (5*height*w)/(6*width);
        
        if(sw < h){
            int dy = (h-sw)/2;
            
            g.drawImage(image, 0, dy, w, dy+sw, 0, 0, width, height,
                    Color.BLACK, null);
        }else{
            int sh = (6*width*h)/(5*height);
            
            int dx = (w-sh)/2;
            
            g.drawImage(image, dx, 0, dx+sh, h, 0, 0, width, height,
                    Color.BLACK, null);
        }
    }
    
    private int[] getColor(int index) {
        return ColorList.colorList[index%0x40];
    }
}
