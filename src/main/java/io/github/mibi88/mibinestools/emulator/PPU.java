/*
 * MibiNESTools - Create NES games easily!
 * Copyright (C) 2026  Mibi88
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

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author mibi88
 */
public class PPU {
    private Rom rom;
    
    private Screen screen;
    
    private PPUHandler handler;
    
    private CPU cpu;
    
    private int v;
    private int t;
    private byte x;
    private boolean w;
    
    private boolean isEven;
    
    private byte[] oam;
    private byte[] secondaryOAM;
    
    private int lowShift;
    private int highShift;
    
    private int cycle;
    
    private int oamAddr;
    
    private byte readBuffer;
    
    private byte attr1Shift;
    private byte attr2Shift;
    
    private byte attrLatch1;
    private byte attrLatch2;
    
    private byte ctrl;
    private byte mask;
    
    private boolean vBlank;
    private boolean sprite0Hit;
    private boolean spriteOverflow;
    
    private boolean mayKeepVBlankClear;
    private boolean keepVBlankClear;
    
    private boolean isRendering;
    
    private static final byte CTRL_INC = (byte)(1<<2);
    private static final byte CTRL_BIG_SPRITES = (byte)(1<<5);
    private static final byte CTRL_NMI = (byte)(1<<7);
    
    private static final byte MASK_GRAYSCALE = (byte)1;
    private static final byte MASK_BG_LEFTMOST_8PX = (byte)(1<<1);
    private static final byte MASK_SPRITES_LEFTMOST_8PX = (byte)(1<<2);
    private static final byte MASK_BACKGROUND = (byte)(1<<3);
    private static final byte MASK_SPRITES = (byte)(1<<4);
    private static final byte MASK_RENDER = (byte)(3<<3);
    
    public PPU(Rom rom, Screen screen, CPU cpu) {
        this.rom = rom;
        this.screen = screen;
        this.cpu = cpu;
        
        oam = new byte[256];
        secondaryOAM = new byte[32];
        
        handler = null;
    }
    
    public void setHandler(PPUHandler handler) {
        this.handler = handler;
    }
    
    private void onCycle() {
        if(cycle == 0){
            cpu.cycle();
        }
        
        cycle++;
        cycle %= 3;
        
        if(handler != null) handler.onCycle();
    }
    
    private void outputAPixel() {
        // TODO
        
        int attribute = (attr1Shift>>7);
        int bgColor = (lowShift>>7)|((highShift>>7)<<1);
        
        byte color = rom.readVram(0x3F00+4*attribute+bgColor);
        
        screen.putPixel(color);
    }
    
    private void emulateVisibleScanline(boolean preRender, boolean first) {
        if(first && !isEven){
            rom.readVram(0x2000|(v&0x0FFF));
        }
        onCycle();

        Arrays.fill(secondaryOAM, (byte)0xFF);
        for(int i=0;i<2;i++){
            onCycle();
            outputAPixel();

            int tileId = rom.readVram(0x2000|(v&0x0FFF));
            onCycle();
            outputAPixel();

            onCycle();
            outputAPixel();

            int attr = rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                    ((v>>2)&7));
            onCycle();
            outputAPixel();

            onCycle();
            outputAPixel();

            int lowBp = rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                    ((v>>12)&7));
            onCycle();
            outputAPixel();

            onCycle();
            outputAPixel();

            int highBp = rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                    (1<<3)|((v>>12)&7));

            // Fill the shift registers
            lowShift &= ~0xFF;
            highShift &= ~0xFF;

            lowShift |= lowBp;
            highShift |= highBp;

            attrLatch1 = (byte)(attr>>((v&2)+((v>>4)&4)));
            attrLatch2 = (byte)(attr>>((v&2)+((v>>4)&4))>>1);

            // Increment coarse X in v
            int x = (v&0b11111)+1;

            v &= ~0b11111;
            v |= x&0b11111;

            // Switch nametable on overflow
            v ^= (x&(1<<5))<<5;

            onCycle();
            outputAPixel();
        }

        for(int i=0;i<29;i++){
            // TODO: Evaluate sprites
            onCycle();
            outputAPixel();

            int tileId = rom.readVram(0x2000|(v&0x0FFF));
            onCycle();
            outputAPixel();

            onCycle();
            outputAPixel();

            int attr = rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                    ((v>>2)&7));
            onCycle();
            outputAPixel();

            onCycle();
            outputAPixel();

            int lowBp = rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                    ((v>>12)&7));
            onCycle();
            outputAPixel();

            onCycle();
            outputAPixel();

            int highBp = rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                    (1<<3)|((v>>12)&7));

            // Fill the shift registers
            lowShift &= ~0xFF;
            highShift &= ~0xFF;

            lowShift |= lowBp;
            highShift |= highBp;

            attrLatch1 = (byte)(attr>>((v&2)+((v>>4)&4)));
            attrLatch2 = (byte)(attr>>((v&2)+((v>>4)&4))>>1);

            // Increment coarse X in v
            int x = (v&0b11111)+1;

            v &= ~0b11111;
            v |= x&0b11111;

            // Switch nametable on overflow
            v ^= (x&(1<<5))<<5;

            onCycle();
            outputAPixel();
        }
        
        {
            // TODO: Evaluate sprites
            onCycle();
            outputAPixel();
            
            int tileId = rom.readVram(0x2000|(v&0x0FFF));
            onCycle();
            outputAPixel();
            
            onCycle();
            outputAPixel();
            
            int attr = rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                    ((v>>2)&7));
            onCycle();
            outputAPixel();
            
            onCycle();
            outputAPixel();
            
            int lowBp = rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                    ((v>>12)&7));
            onCycle();
            outputAPixel();
            
            onCycle();
            outputAPixel();
            
            int highBp = rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                    (1<<3)|((v>>12)&7));
            
            // Fill the shift registers
            lowShift &= ~0xFF;
            highShift &= ~0xFF;
            
            lowShift |= lowBp;
            highShift |= highBp;
            
            attrLatch1 = (byte)(attr>>((v&2)+((v>>4)&4)));
            attrLatch2 = (byte)(attr>>((v&2)+((v>>4)&4))>>1);
            
            // Increment coarse X in v
            int x = (v&0b11111)+1;
            
            v &= ~0b11111;
            v |= x&0b11111;
            
            // Switch nametable on overflow
            v ^= (x&(1<<5))<<5;
            
            // Increment the vertical position in v
            int y = v;
            
            y += (1<<12);
            y += (y&(y<<15))>>10;
            
            if((y&0b1111100000) == (30<<5)){
                y &= ~0b1111100000;
                v ^= 0x800;
            }
            
            v &= ~((7<<12)|0b1111100000);
            v |= y&((7<<12)|0b1111100000);
            
            onCycle();
            outputAPixel();
        }
        
        // TODO: Load the sprite tile data
        for(int i=0;i<320-257;i++) onCycle();
        
        for(int i=0;i<2;i++){
            onCycle();

            int tileId = rom.readVram(0x2000|(v&0x0FFF));
            onCycle();

            onCycle();

            int attr = rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                    ((v>>2)&7));
            onCycle();

            onCycle();

            int lowBp = rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                    ((v>>12)&7));
            onCycle();

            onCycle();

            int highBp = rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                    (1<<3)|((v>>12)&7));

            // Fill the shift registers
            lowShift &= ~0xFF;
            highShift &= ~0xFF;

            lowShift |= lowBp;
            highShift |= highBp;

            attrLatch1 = (byte)(attr>>((v&2)+((v>>4)&4)));
            attrLatch2 = (byte)(attr>>((v&2)+((v>>4)&4))>>1);

            // Increment coarse X in v
            int x = (v&0b11111)+1;

            v &= ~0b11111;
            v |= x&0b11111;

            // Switch nametable on overflow
            v ^= (x&(1<<5))<<5;

            onCycle();
        }
        
        // Dummy nametable fetches
        
        onCycle();

        rom.readVram(0x2000|(v&0x0FFF));
        onCycle();
        
        onCycle();
        
        if(!preRender && isEven){
            rom.readVram(0x2000|(v&0x0FFF));
            onCycle();
        }
    }
    
    public void emulateFrame() {
        emulateVisibleScanline(true, false);
        
        emulateVisibleScanline(false, true);
        for(int i=0;i<239;i++){
            emulateVisibleScanline(false, false);
        }
        
        // Post-render scanline
        for(int i=0;i<340;i++) onCycle();
        
        // First VBlank scanline
        
        onCycle();
        
        cpu.setNmiPin(false);
        
        for(int i=0;i<339;i++) onCycle();
        
        // VBlank scanlines
        for(int i=0;i<(260-241)*340;i++) onCycle();
        
        isEven = !isEven;
    }
    
    public byte read(int register) {
        switch(register){
            case 0:
                // PPUCTRL
                
                break;
                
            case 1:
                // PPUMASK
                
                break;
                
            case 2:
                // PPUSTATUS
                
                rom.ppuIOBus &= 0b11111;
                rom.ppuIOBus |= vBlank ? 1<<7 : 0;
                rom.ppuIOBus |= sprite0Hit ? 1<<6 : 0;
                rom.ppuIOBus |= spriteOverflow ? 1<<5 : 0;
                
                vBlank = false;
                
                if(mayKeepVBlankClear){
                    keepVBlankClear = true;
                }
                
                w = false;
                
                break;
                
            case 3:
                // OAMADDR
                
                break;
                
            case 4:
                // OAMDATA
                rom.ppuIOBus = oam[oamAddr];
                
                break;
                
            case 5:
                // PPUSCROLL
                
                break;
                
            case 6:
                // PPUADDR
                
                break;
                
            case 7:
            {
                // PPUDATA
                
                // XXX: Is it correct?
                byte value = readBuffer;
                
                int address = v&0b11111111111111;
                
                readBuffer = rom.readVram(address);
                rom.ppuIOBus = readBuffer;
                
                if(isRendering){
                    // Increment coarse X in v
                    int x = (v&0b11111)+1;

                    v &= ~0b11111;
                    v |= x&0b11111;

                    // Switch nametable on overflow
                    v ^= (x&(1<<5))<<5;

                    // Increment the vertical position in v
                    int y = v;

                    y += (1<<12);
                    y += (y&(y<<15))>>10;

                    if((y&0b1111100000) == (30<<5)){
                        y &= ~0b1111100000;
                        v ^= 0x800;
                    }

                    v &= ~((7<<12)|0b1111100000);
                    v |= y&((7<<12)|0b1111100000);
                    
                    // Have I understood the "read next value" that is written
                    // in the wiki correctly?
                    readBuffer = rom.readVram(v&0b11111111111111);
                    rom.ppuIOBus = readBuffer;
                }else{
                    v += ((ctrl&CTRL_INC)>>2)*31+1;
                }
                
                // Palette reads are unbuffered, if supported by the PPU
                
                if(address >= 0x3F00){
                    value = readBuffer;
                    
                    address = ((address&0xFF)+0x2700)&0b11111111111111;
                    readBuffer = rom.readVram(address);
                    
                    value = (byte)((value&0x3F)|(readBuffer&~0x3F));
                }
                
                return value;
            }
        }
        
        return rom.ppuIOBus;
    }
    
    public void write(int register, byte value) {
        // TODO
    }
}
