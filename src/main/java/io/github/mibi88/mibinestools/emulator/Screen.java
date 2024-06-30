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

package io.github.mibi88.mibinestools.emulator;

import io.github.mibi88.mibinestools.chr_editor.CHRData;
import io.github.mibi88.mibinestools.palette_editor.ColorList;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * This class emulates the NES PPU.
 * @author mibi88
 */
public class Screen extends JPanel {
    private byte[] ppuRAM;
    private byte[] patternTable;
    private int address;
    private Region region;
    private int scale;
    private int width;
    private int height;
    private byte[] screen;
    private boolean renderingDone;
    private boolean odd;
    private float cpuCycleInPPUCycles;
    // PPUCTRL
    private boolean generateNMI;
    private boolean slaveMode;
    private boolean bigSprites;
    private int backgroundCHRBank;
    private int spriteCHRBank;
    private int ppuAddrIncrease;
    private int nametable;
    // PPUMASK
    private boolean emphasizeBlue;
    private boolean emphasizeGreen;
    private boolean emphasizeRed;
    private boolean showSprites;
    private boolean showBackground;
    private boolean showSpritesInLeftmost8px;
    private boolean showBackgroundInLeftmost8px;
    private boolean greyscale;
    // PPUSTATUS
    private boolean inVBlank;
    private boolean sprite0Hit;
    private boolean spriteOverflow;

    /**
     * Create a new screen.
     * @param chrData The CHR data to use for rendering.
     * @param region The region to emulate.
     */
    public Screen(byte[] patternTable, Region region, int scale) {
        super();
        ppuRAM = new byte[0x4000];
        Arrays.fill(ppuRAM, Byte.MIN_VALUE);
        width = 256;
        height = 240;
        screen = new byte[width*height];
        Arrays.fill(screen, Byte.MIN_VALUE);
        this.scale = scale;
        this.region = region;
        if(region == Region.NTSC){
            cpuCycleInPPUCycles = 3f;
        }else{
            cpuCycleInPPUCycles = 3.2f;
        }
        this.patternTable = patternTable;
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
                int[] color = getColor(screen[y*width+x]-Byte.MIN_VALUE);
                g.setColor(new Color(color[0], color[1], color[2]));
                g.fillRect(x*scale, y*scale, scale, scale);
            }
        }
    }
    
    private int[] getColor(int index) {
        return ColorList.colorList[index%0x40];
    }
    
    private void run(CPU cpu) {
        int lastCPUCycle = 0;
        if(showSprites || showBackground){
            int ppuCycles = 261*341-(odd ? 1 : 0);
            int lastRead = 0;
            for(int i=0;i<ppuCycles;i++){
                // TODO: Sprite 0 hit special case.
                // Background, etc.
                if(i >= 1 && i <= 256){
                    if(lastRead < 8){
                        lastRead++;
                    }else{
                        // TODO
                        lastRead = 0;
                    }
                }
                // Sprite evaluation
                if(i >= 1 && i <= 64){
                    // TODO: Initialize secondary OAM.
                }
                // Run a CPU cycle if needed.
                if((int)(i/cpuCycleInPPUCycles) > lastCPUCycle){
                    cpu.cycle();
                    lastCPUCycle = (int)(i/cpuCycleInPPUCycles);
                }
            }
        }else{
            // Rendering is disabled
            int ppuCycles = 341*262;
            for(int i=0;i<ppuCycles;i++){
                if((int)(i/cpuCycleInPPUCycles) > lastCPUCycle){
                    cpu.cycle();
                    lastCPUCycle = (int)(i/cpuCycleInPPUCycles);
                }
            }
        }
        repaint();
    }
}
