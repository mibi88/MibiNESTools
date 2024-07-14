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
    private boolean writeLatch;
    private boolean accurateEmulation;
    private Timer timer;
    private CPU cpu;
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
    private byte ppuAddrByte1;
    private byte ppuAddrByte2;
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
     * @param scale The scale to render the image at.
     * @param accurateEmulation If the emulation should be accurate.
     */
    public Screen(byte[] patternTable, Region region, int scale,
            boolean accurateEmulation, CPU cpu) {
        super();
        reset(patternTable, region, scale, accurateEmulation, cpu);
    }
    
    /**
     * Reset the NES.
     * @param patternTable The CHR ROM.
     * @param region The region.
     * @param scale The scale to render the image at.
     * @param accurateEmulation If the emulation should be accurate.
     */
    public void reset(byte[] patternTable, Region region, int scale,
            boolean accurateEmulation, CPU cpu) {
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
        this.accurateEmulation = accurateEmulation;
        this.cpu = cpu;
        oam = new byte[0x100];
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
     * Run the currently loaded ROM.
     */
    public void play() {
        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    
    /**
     * Stop running this ROM.
     */
    public void powerOff() {
        timer.stop();
    }
    
    /**
     * Run one PPU frame.
     */
    public void run() {
        if(accurateEmulation){
            runAccurate();
        }else{
            runCheap();
        }
    }
    
    
    private void runAccurate() {
        // Very accurate PPU emulation
        // (currently not working)
        scrollX = (nametable&0b00000001)<<8;
        scrollX |= ppuScrollX;
        scrollY = (nametable&0b00000010)<<7;
        scrollY |= ppuScrollY;
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
    
    private void runCheap() {
        // Inaccurate PPU emulation.
        // (should be working)
        int lastCPUCycle = 0;
        scrollX = (nametable&0b00000001)<<8;
        scrollX |= ppuScrollX;
        scrollY = (nametable&0b00000010)<<7;
        scrollY |= ppuScrollY;
        if(showSprites || showBackground){
            int ppuCycles = 261*341-(odd ? 1 : 0);
            for(int i=0;i<ppuCycles;i++){
                int scanline = (i+(odd ? 1 : 0))/341;
                int cycle = (i+(odd ? 1 : 0))%341;
                int pixel = cycle;
                ArrayList<Integer> sprites = new ArrayList<Integer>();
                byte spritePixel = 0x00;
                int spritePalette = 0;
                boolean behindBackground = false;
                byte backgroundPixel = 0x00;
                int backgroundPalette = 0;
                int spriteIndex = 0;
                if(scanline <= 240){
                    if(pixel >= 257 && pixel <= 320){
                        oamAddr = 0;
                    }
                }
                if(scanline == 0){
                    inVBlank = false;
                }
                if(scanline >= 1 && scanline <= 241){
                    if(pixel == 0){
                        spriteOverflow = false;
                        sprites.clear();
                        for(int n=0;n<256;n+=4){
                            if(scanline-1 >= oam[n] && scanline-1 < oam[n]+8 &&
                                    sprites.size() < 8){
                                sprites.add(n);
                            }
                            if(sprites.size() > 8){
                                // TODO: Sprite overflow bug.
                                spriteOverflow = true;
                            }
                        }
                    }
                    if(pixel < 240){
                        if(showSprites){
                            // TODO: Add support for 8x16 sprites!
                            for(int index : sprites){
                                if(pixel <= oam[index+3] &&
                                        pixel < oam[index+3]+8){
                                    byte attributes = oam[index+2];
                                    int tileIndex = oam[index+1]-Byte.MIN_VALUE;
                                    int yPos = (scanline-1)-oam[index];
                                    yPos = yPos < 0 ? 0 : yPos;
                                    yPos = yPos >= 8 ? 7 : yPos;
                                    if((attributes&0b10000000) != 0){
                                        // Vertical flipping
                                        yPos = 7-yPos;
                                    }
                                    byte tileLow =
                                            patternTable[tileIndex*16+yPos];
                                    byte tileHigh = patternTable[tileIndex*16+8
                                            +yPos];
                                    int xPos = pixel-oam[index+3];
                                    if((attributes&0b01000000) != 0){
                                        // Horizontal flipping
                                        xPos = 7-xPos;
                                    }
                                    spritePixel = (byte)((tileLow&1<<xPos)>>xPos
                                            |(tileHigh&1<<xPos)>>xPos<<1);
                                    behindBackground =
                                            (attributes&0b00000100) != 0;
                                    spritePalette = attributes&0b00000011;
                                    spriteIndex = index;
                                    break;
                                }
                            }
                        }
                        if(showBackground){
                            int tileIndex = getNametableByte(scanline-1,
                                    pixel);
                            int xPos = (scrollX+pixel)%8;
                            int yPos = (scrollY+scanline-1)%8;
                            byte tileLow = patternTable[tileIndex*16+yPos];
                            byte tileHigh = patternTable[tileIndex*16+8
                                    +yPos];
                            backgroundPixel = (byte)((tileLow&1<<xPos)>>xPos
                                    |(tileHigh&1<<xPos)>>xPos<<1);
                            backgroundPalette = getAttribute(scanline-1,
                                    pixel);
                        }
                        // TODO: Add support for tint bits, etc.
                        if(showBackground && showSprites){
                            if(spriteIndex == 0 && spritePixel != 0 &&
                                    backgroundPixel != 0){
                                sprite0Hit = true;
                            }
                            if(behindBackground){
                                if(backgroundPixel == 0){
                                    byte color = ppuRAM[0x3F00+spritePalette*4
                                            +spritePixel];
                                    screen[(scanline-1)+pixel] = color;
                                }else{
                                    byte color = ppuRAM[0x3F00
                                            +backgroundPalette*4
                                            +backgroundPixel];
                                    screen[(scanline-1)+pixel] = color;
                                }
                            }else{
                                if(spritePixel == 0){
                                    byte color = ppuRAM[0x3F00
                                            +backgroundPalette*4
                                            +backgroundPixel];
                                    screen[(scanline-1)+pixel] = color;
                                }else{
                                    byte color = ppuRAM[0x3F00+spritePalette*4
                                            +spritePixel];
                                    screen[(scanline-1)+pixel] = color;
                                }
                            }
                        }else if(showSprites){
                            if(spritePixel == 0){
                                byte color = ppuRAM[0x3F00];
                                screen[(scanline-1)+pixel] = color;
                            }else{
                                byte color = ppuRAM[0x3F00+spritePalette*4
                                        +spritePixel];
                                screen[(scanline-1)+pixel] = color;
                            }
                        }else{
                            // Show the background only.
                            byte color = ppuRAM[0x3F00
                                    +backgroundPalette*4
                                    +backgroundPixel];
                            screen[(scanline-1)+pixel] = color;
                        }
                    }
                }
                if(scanline == 242){
                    if(cycle == 0){
                        inVBlank = true;
                        cpu.nmi();
                    }
                }
                if(scanline == 261){
                    sprite0Hit = false;
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
            inVBlank = false;
            for(int i=0;i<ppuCycles;i++){
                if(i == 341*242){
                    inVBlank = true;
                }
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
    
    private int getAttribute(int visibleScanline, int pixel) {
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
        byte attribute = ppuRAM[addr+y*8+x];
        x = (scrollX+pixel)/8;
        y = (scrollY+visibleScanline)/8;
        int pos = (y/2%2)*2+(x/2%2);
        int bit1 = 1<<pos*2;
        int palette = attribute&(bit1|bit1<<1);
        palette >>= pos*2;
        return palette;
    }
    
    /**
     * Write to PPUCTRL.
     * @param value The byte to write.
     */
    public void writePPUCTRL(byte value) {
        nametable = value&0b00000011;
        // ppuAddrIncrease: Add 1 if false, go down one line if true (add 32).
        ppuAddrIncrease = (value&0b00000100) != 0 ? 32 : 1;
        spriteCHRBank = (value&0b00001000) != 0 ? 1 : 0;
        backgroundCHRBank = (value&0b00010000) != 0 ? 1 : 0;
        bigSprites = (value&0b00100000) != 0;
        slaveMode = (value&0b01000000) != 0;
        generateNMI = (value&0b10000000) != 0;
    }
    
    /**
     * Write to PPUMASK.
     * @param value The byte to write.
     */
    public void writePPUMASK(byte value) {
        emphasizeBlue = (value&0b10000000) != 0;
        if(region == Region.NTSC){
            emphasizeGreen = (value&0b01000000) != 0;
            emphasizeRed = (value&0b00100000) != 0;
        }else{
            emphasizeRed = (value&0b01000000) != 0;
            emphasizeGreen = (value&0b00100000) != 0;
        }
        showBackground = (value&0b00010000) != 0;
        showSprites = (value&0b00001000) != 0;
        showBackgroundInLeftmost8px = (value&0b00000100) != 0;
        showSpritesInLeftmost8px = (value&0b00000010) != 0;
        greyscale = (value&0b00000001) != 0;
    }
    
    /**
     * Write to PPUSTATUS.
     * @param value The byte to write.
     */
    public void writePPUSTATUS(byte value) {
        // TODO: Simulate open bus behavior.
    }
    
    /**
     * Write to OAMADDR.
     * @param value The byte to write.
     */
    public void writeOAMADDR(byte value) {
        oamAddr = value&0xFF;
    }
    
    /**
     * Write to OAMDATA.
     * @param value The byte to write.
     */
    public void writeOAMDATA(byte value) {
        oam[oamAddr] = value;
        oamAddr++;
        if(oamAddr > 255){
            oamAddr = 0;
        }
    }
    
    /**
     * Write to PPUSCROLL.
     * @param value The byte to write.
     */
    public void writePPUSCROLL(byte value) {
        if(writeLatch){
            ppuScrollY = value;
        }else{
            ppuScrollX = value;
        }
    }
    
    /**
     * Write to PPUADDR.
     * @param value The byte to write.
     */
    public void writePPUADDR(byte value) {
        // TODO: Bus conflict.
        if(writeLatch){
            ppuAddrByte2 = value;
        }else{
            ppuAddrByte1 = value;
        }
        ppuAddr = (ppuAddrByte1<<8)|ppuAddrByte2;
        ppuAddr %= 0x3FFF;
    }
    
    /**
     * Write to PPUDATA.
     * @param value The byte to write.
     */
    public void writePPUDATA(byte value) {
        ppuRAM[ppuAddr] = value;
        ppuAddr += ppuAddrIncrease;
        ppuAddr %= 0x3FFF;
    }
    
    /**
     * Read PPUCTRL.
     * @return The byte that was read.
     */
    public byte readPPUCTRL() {
        // TODO
        return 0x00;
    }
    
    /**
     * Read PPUMASK.
     * @return The byte that was read.
     */
    public byte readPPUMASK() {
        // TODO
        return 0x00;
    }
    
    /**
     * Read PPUSTATUS.
     * @return The byte that was read.
     */
    public byte readPPUSTATUS() {
        byte value = 0x00;
        value |= inVBlank ? 0b10000000 : 0x00;
        value |= sprite0Hit ? 0b01000000 : 0x00;
        value |= spriteOverflow ? 0b00100000 : 0x00;
        inVBlank = false;
        return value;
    }
    
    /**
     * Read OAMADDR.
     * @return The byte that was read.
     */
    public byte readOAMADDR() {
        // TODO
        return 0x00;
    }
    
    /**
     * Read OAMDATA.
     * @return The byte that was read.
     */
    public byte readOAMDATA() {
        return oam[oamAddr];
    }
    
    /**
     * Read PPUSCROLL.
     * @return The byte that was read.
     */
    public byte readPPUSCROLL() {
        // TODO
        return 0x00;
    }
    
    /**
     * Read PPUADDR.
     * @return The byte that was read.
     */
    public byte readPPUADDR() {
        // TODO
        return 0x00;
    }
    
    /**
     * Read PPUDATA.
     * @return The byte that was read.
     */
    public byte readPPUDATA() {
        byte value = ppuRAM[ppuAddr];
        ppuAddr += ppuAddrIncrease;
        ppuAddr %= 0x3FFF;
        return value;
    }
}
