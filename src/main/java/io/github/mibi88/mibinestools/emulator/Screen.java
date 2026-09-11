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
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This class emulates the NES PPU.
 * @author mibi88
 */
public class Screen extends JPanel {
    protected final int width = 256;
    protected final int height = 240;
    
    private int scale;
    
    private byte[] pixels;
    private int currentPixel;
    
    /**
     * Create a new screen.
     */
    public Screen(int scale) {
        super();
        
        this.scale = scale;
        reset();
    }
    
    public void reset() {
        pixels = new byte[width*height];
        currentPixel = 0;
    }
    
    public void putPixel(byte color) {
        pixels[currentPixel++] = color;
        if(currentPixel >= width*height) {
            currentPixel = 0;
            Screen thisScreen = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    thisScreen.repaint();
                }
            });
        }
    }
    
    public void setScale(int scale) {
        this.scale = scale;
        Screen thisScreen = this;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                thisScreen.repaint();
            }
        });
    }
    
    /**
     * Render the screen.
     * @param g The AWT graphics.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for(int y=0;y<height;y++){
            for(int x=0;x<width;x++){
                int[] color = getColor(Byte.toUnsignedInt(pixels[y*width+x]));
                g.setColor(new Color(color[0], color[1], color[2]));
                g.fillRect(x*scale, y*scale, scale, scale);
            }
        }
    }
    
    private int[] getColor(int index) {
        return ColorList.colorList[index%0x40];
    }
}
