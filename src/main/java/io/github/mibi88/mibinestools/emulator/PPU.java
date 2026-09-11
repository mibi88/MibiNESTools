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
    
    private int ctrl;
    
    private int lowShift;
    private int highShift;
    
    private int cycle;
    
    private byte attr1Shift;
    private byte attr2Shift;
    
    private byte attrLatch1;
    private byte attrLatch2;
    
    // For debugging:
    private byte color;
    
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
        // For debugging:
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

        for(int i=0;i<30;i++){
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
        // For debugging:
        color = (byte)new Random().nextInt();
        
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
}
