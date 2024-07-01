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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * This class emulates the NES PPU.
 * @author mibi88
 */
public class Screen extends JPanel {
    private byte[] ppuRAM;
    private byte[] oam;
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
    private int scrollX;
    private int scrollY;
    private byte[] secondaryOAM;
    Stack<Byte> pixels;
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
    // PPUADDR
    private int ppuAddr;
    // PPUSCROLL
    private int ppuScrollX;
    private int ppuScrollY;
    // OAMADDR
    private int oamAddr;
    // OAMDATA
    private byte oamData;
    // OAMDMA
    private byte oamDMA;

    /**
     * Create a new screen.
     * @param patternTable The CHR data to use for rendering.
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
        secondaryOAM = new byte[32];
        pixels = new Stack<Byte>();
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
    
    /**
     * Run one PPU frame.
     * @param cpu The CPU.
     */
    public void runAccurate(CPU cpu) {
        // Very accurate PPU emulation
        // (currently not working)
        int lastCPUCycle = 0;
        byte nameTableByte = 0x00;
        byte attributeTableByte = 0x00;
        byte patternTableTileLow = 0x00;
        byte patternTableTileHigh = 0x00;
        if(showSprites || showBackground){
            int ppuCycles = 261*341-(odd ? 1 : 0);
            int lastRead = 0;
            for(int i=0;i<ppuCycles;i++){
                if(i > 341-(odd ? 1 : 0) && i < 241*341-(odd ? 1 : 0)){
                    // TODO: Sprite 0 hit special case.
                    // Background, etc.
                    int scanline = (i+(odd ? 1 : 0))/341;
                    int cycle = (i+(odd ? 1 : 0))%341;
                    int pixel = cycle;
                    if(cycle >= 1 && cycle <= 256){
                        if(lastRead < 8){
                            switch(lastRead){
                                case 2:
                                    nameTableByte = getNametableByte(
                                            scanline, pixel);
                                    break;
                                case 4:
                                    attributeTableByte = getAttributeByte(
                                            scanline, pixel);
                                    break;
                                case 6:
                                    patternTableTileLow =
                                            patternTable[nameTableByte];
                                    break;
                            }
                            lastRead++;
                        }else{
                            patternTableTileHigh =
                                            patternTable[nameTableByte+8];
                            lastRead = 0;
                        }
                    }
                    // Sprite evaluation
                    if(i >= 1 && i <= 64){
                        Arrays.fill(secondaryOAM, Byte.MAX_VALUE);
                    }
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
    
    public void runCheap(CPU cpu) {
        // Inaccurate PPU emulation.
        // (currently not working)
        int lastCPUCycle = 0;
        if(showSprites || showBackground){
            int ppuCycles = 261*341-(odd ? 1 : 0);
            for(int i=0;i<ppuCycles;i++){
                int scanline = (i+(odd ? 1 : 0))/341;
                int cycle = (i+(odd ? 1 : 0))%341;
                int pixel = cycle;
                Stack<Integer> sprites = new Stack<Integer>();
                if(scanline >= 1 && scanline <= 240){
                    if(pixel == 0){
                        //
                    }
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
    
    private byte getNametableByte(int visibleScanline, int pixel) {
        int x = (scrollX+pixel)/8;
        int y = (scrollY+visibleScanline)/8;
        int addr = 0x2000;
        if(y >= 30){
            addr = 0x2800;
            y -= 30;
        }
        if(x >= 32){
            addr += 0x400;
            x -= 32;
        }
        return ppuRAM[addr+y*32+x];
    }
    
    private byte getAttributeByte(int visibleScanline, int pixel) {
        int x = (scrollX+pixel)/32;
        int y = (scrollY+visibleScanline)/32;
        int addr = 0x23C0;
        if(y >= 8){
            addr = 0x2BC0;
            y -= 8;
        }
        if(x >= 8){
            addr += 0x400;
            x -= 8;
        }
        return ppuRAM[addr+y*8+x];
    }
}
