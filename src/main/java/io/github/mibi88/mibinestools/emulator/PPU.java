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

/**
 *
 * @author mibi88
 */
public class PPU {
    private class Sprite {
        public int downCounter;
        
        public byte lowBp;
        public byte highBp;
        
        public byte flags;
    }
    
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
    
    private Sprite[] spriteFIFO;
    
    private int lowShift;
    private int highShift;
    
    private int cycle;
    
    private int oamAddr;
    private int secondaryOAMAddr;
    private int spriteEvalState;
    private byte spriteValue;
    
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
    
    // private int cycleCount;
    
    public PPU(Rom rom, Screen screen, CPU cpu) {
        this.rom = rom;
        this.screen = screen;
        this.cpu = cpu;
        
        oam = new byte[256];
        secondaryOAM = new byte[32];
        spriteFIFO = new Sprite[8];
        
        for(int i=0;i<8;i++){
            spriteFIFO[i] = new Sprite();
        }
        
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
        
        // cycleCount++;
    }
    
    private void outputAPixel(boolean isLastPixel) {
        if((mask&MASK_RENDER) == 0){
            if((v&0x7F00) == 0x3F00) screen.putPixel(rom.readVram(v));
            else screen.putPixel(rom.readVram(0x3F00));
        }
        
        int attribute = (attr1Shift>>(7-x))&1|(((attr2Shift>>(7-x))&1)<<1);
        int bgColor = (mask&MASK_BACKGROUND) != 0 ?
                ((lowShift>>(15-x))&1)|(((highShift>>(15-x))&1)<<1) : 0;
        
        int spriteColor = 0;
        
        int spritePalette = 0;
        int spritePriority = 0;
        
        boolean spriteZero = false;
        
        if((mask&MASK_SPRITES) != 0){
            int index = -1;

            for(int i=8;i-- > 0;){
                if(spriteFIFO[i].downCounter <= 0){
                    int color = (spriteFIFO[i].lowBp>>7)&1;
                    color |= ((spriteFIFO[i].highBp>>7)&1)<<1;

                    spriteFIFO[i].lowBp <<= 1;
                    spriteFIFO[i].highBp <<= 1;

                    if(color != 0){
                        spriteColor = color;
                        spritePalette = spriteFIFO[i].flags&2;
                        spritePriority = spriteFIFO[i].flags&(1<<5);
                        
                        spriteZero = (spriteFIFO[i].flags&4) != 0;

                        index = i;
                    }
                }else{
                    spriteFIFO[i].downCounter--;
                }
            }

            int colorIndex = 0;

            // FIXME: Make sprite 0 hit detection accurate.
            // NOTE: spriteZero is only set if spriteColor != 0 so we don't need
            //       to check that
            if(spriteZero && bgColor != 0 && !isLastPixel){
                sprite0Hit = true;
            }
        }
        
        byte color;
        
        if(spriteColor == 0 || (spritePriority != 0 && bgColor != 0)){
            if(bgColor == 0) attribute = 0; // XXX: This probably isn't accurate
            color = rom.readVram(0x3F00+4*attribute+bgColor);
        }else{
            color = rom.readVram(0x3F00+4*(spritePalette+4)+spriteColor);
        }
        
        color &= 0x3F;
        if((mask&MASK_GRAYSCALE) != 0) color &= 0x30;
        
        screen.putPixel(color);
    }
    
    private void shiftBackground() {
        lowShift <<= 1;
        lowShift |= 1;
        highShift <<= 1;
        highShift |= 1;
        
        attr1Shift <<= 1;
        attr1Shift |= attrLatch1;
        attr2Shift <<= 1;
        attr2Shift |= attrLatch2;
    }
    
    private void spriteReadCycle(int scanline) {
        spriteValue = oam[oamAddr];
    }
    
    private void spriteWriteCycle(int scanline) {
        switch(spriteEvalState){
            case 0:
                // Copy the first byte and check if the Y coordinate is in
                // range
                secondaryOAM[secondaryOAMAddr] = spriteValue;
                if(Byte.toUnsignedInt(spriteValue) <= scanline &&
                        Byte.toUnsignedInt(spriteValue)+8 > scanline){
                    if(false){
                        System.out.printf("%03d: Sprite at %02X in range!\n",
                                scanline, oamAddr);
                    }
                    secondaryOAMAddr++;
                    spriteEvalState = 1;
                    oamAddr++;
                    oamAddr &= 0xFF;
                }else{
                    oamAddr += 4;
                    oamAddr &= 0xFF;
                    
                    if(oamAddr == 0){
                        // All sprites got evaluated
                        
                        oamAddr = 0;
                        spriteEvalState = 8;
                    }else if(secondaryOAMAddr < 32){
                        spriteEvalState = 0;
                    }else{
                        // Exactly eight sprites have been found

                        spriteEvalState = 4;
                        
                        // NOTE: The following code is the same as in case 4
                        if(Byte.toUnsignedInt(spriteValue) <= scanline &&
                                Byte.toUnsignedInt(spriteValue)+8 > scanline){
                            spriteOverflow = true;

                            spriteEvalState = 5;
                        }else{
                            oamAddr += 5;
                            oamAddr &= 0xFF;

                            if((oamAddr&~3) == 0){
                                // All sprites got evaluated

                                spriteEvalState = 8;
                            }
                        }
                    }
                }
                
                break;
                
            case 1:
                // Copy the remaining bytes
                secondaryOAM[secondaryOAMAddr++] = spriteValue;
                spriteEvalState++;
                oamAddr++;
                oamAddr &= 0xFF;
                
                break;
                
            case 2:
                // Copy the remaining bytes
                
                // XXX: How does the PPU actually know which sprite is
                //      sprite zero?
                spriteValue &= ~4;
                if(oamAddr == 2) spriteValue |= 4;
                
                secondaryOAM[secondaryOAMAddr++] = spriteValue;
                spriteEvalState++;
                oamAddr++;
                oamAddr &= 0xFF;
                
                break;
                
            case 3:
                // Copy the remaining bytes
                secondaryOAM[secondaryOAMAddr++] = spriteValue;
                spriteEvalState++;
                oamAddr++;
                oamAddr &= 0xFF;
                
                if(oamAddr == 0){
                    // All sprites got evaluated
                    
                    oamAddr = 0;
                    spriteEvalState = 8;
                    
                    break;
                }else if(secondaryOAMAddr < 32){
                    spriteEvalState = 0;
                    
                    break;
                }else{
                    // Exactly eight sprites have been found
                    
                    spriteEvalState = 4;
                }
                
            case 4:
                if(Byte.toUnsignedInt(spriteValue) <= scanline &&
                        Byte.toUnsignedInt(spriteValue)+8 > scanline){
                    spriteOverflow = true;
                    oamAddr++;
                    oamAddr &= 0xFF;
                    
                    spriteEvalState = 5;
                }else{
                    oamAddr += 5;
                    oamAddr &= 0xFF;
                    
                    if((oamAddr&~3) == 0){
                        // All sprites got evaluated
                        
                        spriteEvalState = 8;
                    }
                }
                
                break;
                
            case 5:
            case 6:
                oamAddr++;
                oamAddr &= 0xFF;
                spriteEvalState++;
                
                break;
                
            case 7:
                oamAddr++;
                oamAddr &= 0xFF;
                spriteEvalState = 4;
                
                break;
                
            case 8:
                break;
                
            default:
                System.err.println("Sprite evaluation state out of range!");
        }
    }
    
    private void emulateVisibleScanline(int scanline,
            boolean preRender, boolean first) {
        if(first && !isEven && (mask&MASK_RENDER) != 0){
            if((mask&MASK_BACKGROUND) != 0) rom.readVram(0x2000|(v&0x0FFF));
        }
        onCycle();
        
        if(preRender){
            if((ctrl&CTRL_NMI) != 0) cpu.setNmiPin(true);
            
            vBlank = false;
            sprite0Hit = false;
            spriteOverflow = false;
        }
        
        secondaryOAMAddr = 0;

        for(int i=0;i<8;i++){
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            // XXX: What value should it contain when rendering is disabled?
            int tileId = 0;
            if((mask&MASK_BACKGROUND) != 0){
                tileId = Byte.toUnsignedInt(rom.readVram(0x2000|
                        (v&0x0FFF)));
            }
            if(!preRender && (mask&MASK_SPRITES) != 0){
                secondaryOAM[secondaryOAMAddr++] = (byte)0xFF;
            }
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            // XXX: What value should it contain when rendering is disabled?
            int attr = 0;
            if((mask&MASK_BACKGROUND) != 0){
                attr = Byte.toUnsignedInt(
                        rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                                ((v>>2)&7)));
            }
            if(!preRender && (mask&MASK_SPRITES) != 0){
                secondaryOAM[secondaryOAMAddr++] = (byte)0xFF;
            }
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            // XXX: What value should it contain when rendering is disabled?
            int lowBp = 0;
            if((mask&MASK_BACKGROUND) != 0){
                lowBp = Byte.toUnsignedInt(
                        rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                                ((v>>12)&7)));
            }
            if(!preRender && (mask&MASK_SPRITES) != 0){
                secondaryOAM[secondaryOAMAddr++] = (byte)0xFF;
            }
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            if((mask&MASK_BACKGROUND) != 0){
                int highBp = Byte.toUnsignedInt(
                        rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                                (1<<3)|((v>>12)&7)));
                
                // Fill the shift registers
                lowShift &= ~0xFF;
                highShift &= ~0xFF;

                lowShift |= lowBp;
                highShift |= highBp;

                attrLatch1 = (byte)((attr>>((v&2)+((v>>4)&4)))&1);
                attrLatch2 = (byte)((attr>>((v&2)+((v>>4)&4))>>1)&1);

                // Increment coarse X in v
                int x = (v&0b11111)+1;

                v &= ~0b11111;
                v |= x&0b11111;

                // Switch nametable on overflow
                v ^= (x&(1<<5))<<5;
            }

            if(!preRender && (mask&MASK_SPRITES) != 0){
                secondaryOAM[secondaryOAMAddr++] = (byte)0xFF;
            }
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
        }

        secondaryOAMAddr = 0;
        spriteEvalState = 0;
        
        for(int i=0;i<23;i++){
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteReadCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            // XXX: What value should it contain when rendering is disabled?
            int tileId = 0;
            if((mask&MASK_BACKGROUND) != 0){
                tileId = Byte.toUnsignedInt(rom.readVram(0x2000|
                        (v&0x0FFF)));
            }
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteWriteCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteReadCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            // XXX: What value should it contain when rendering is disabled?
            int attr = 0;
            if((mask&MASK_BACKGROUND) != 0){
                attr = Byte.toUnsignedInt(
                        rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                                ((v>>2)&7)));
            }
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteWriteCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteReadCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            // XXX: What value should it contain when rendering is disabled?
            int lowBp = 0;
            if((mask&MASK_BACKGROUND) != 0){
                lowBp = Byte.toUnsignedInt(
                        rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                                ((v>>12)&7)));
            }
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteWriteCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteReadCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();

            if((mask&MASK_BACKGROUND) != 0){
                int highBp = Byte.toUnsignedInt(
                        rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                            (1<<3)|((v>>12)&7)));
                
                // Fill the shift registers
                lowShift &= ~0xFF;
                highShift &= ~0xFF;

                lowShift |= lowBp;
                highShift |= highBp;

                attrLatch1 = (byte)((attr>>((v&2)+((v>>4)&4)))&1);
                attrLatch2 = (byte)((attr>>((v&2)+((v>>4)&4))>>1)&1);

                // Increment coarse X in v
                int x = (v&0b11111)+1;

                v &= ~0b11111;
                v |= x&0b11111;

                // Switch nametable on overflow
                v ^= (x&(1<<5))<<5;
            }

            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteWriteCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
        }
        
        {
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteReadCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
            
            // XXX: What value should it contain when rendering is disabled?
            int tileId = 0;
            if((mask&MASK_BACKGROUND) != 0){
                tileId = Byte.toUnsignedInt(rom.readVram(0x2000|(v&0x0FFF)));
            }
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteWriteCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteReadCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
            
            // XXX: What value should it contain when rendering is disabled?
            int attr = 0;
            if((mask&MASK_BACKGROUND) != 0){
                attr = Byte.toUnsignedInt(
                        rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                                ((v>>2)&7)));
            }
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteWriteCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteReadCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
            
            // XXX: What value should it contain when rendering is disabled?
            int lowBp = 0;
            if((mask&MASK_BACKGROUND) != 0){
                lowBp = Byte.toUnsignedInt(
                        rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                                ((v>>12)&7)));
            }
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteWriteCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteReadCycle(scanline);
            }
            
            if(!preRender) outputAPixel(false);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
            
            if((mask&MASK_BACKGROUND) != 0){
                int highBp = Byte.toUnsignedInt(
                        rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                                (1<<3)|((v>>12)&7)));

                // Fill the shift registers
                lowShift &= ~0xFF;
                highShift &= ~0xFF;

                lowShift |= lowBp;
                highShift |= highBp;

                attrLatch1 = (byte)((attr>>((v&2)+((v>>4)&4)))&1);
                attrLatch2 = (byte)((attr>>((v&2)+((v>>4)&4))>>1)&1);

                // Increment coarse X in v
                int x = (v&0b11111)+1;

                v &= ~0b11111;
                v |= x&0b11111;

                // Switch nametable on overflow
                v ^= (x&(1<<5))<<5;

                // Increment the vertical position in v
                int y = v;

                y += (1<<12);
                y += (y&(1<<15))>>10;

                if((y&0b1111100000) == (30<<5)){
                    y &= ~0b1111100000;
                    v ^= 0x800;
                }

                v &= ~((7<<12)|0b1111100000);
                v |= y&((7<<12)|0b1111100000);
            }
            
            if(!preRender && (mask&MASK_SPRITES) != 0){
                spriteWriteCycle(scanline);
            }
            
            if(!preRender) outputAPixel(true);
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
            onCycle();
        }
        
        if((mask&MASK_BACKGROUND) != 0){
            v &= ~(0b11111|0x400);
            v |= t&(0b11111|0x400);
        }
        
        secondaryOAMAddr = 0;
        
        for(int i=0;i<3;i++){
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                rom.readVram(0x2000|(v&0x0FFF));
            }
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                        ((v>>2)&7));
            }
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            int tileId = Byte.toUnsignedInt(secondaryOAM[secondaryOAMAddr+1]);
            int y = Byte.toUnsignedInt(secondaryOAM[secondaryOAMAddr]);
            byte attr = secondaryOAM[secondaryOAMAddr+2];
            
            int bpLine = ((scanline-y)&7)^(((attr>>7)&1)*7);
            
            if((mask&MASK_SPRITES) != 0){
                spriteFIFO[i].lowBp = rom.readVram((tileId<<4)|bpLine);
                if((attr&(1<<6)) != 0){
                    byte l = spriteFIFO[i].lowBp;
                    spriteFIFO[i].lowBp = (byte)(((l>>7)&1)|
                            ((l>>5)&2)|
                            ((l>>3)&4)|
                            ((l>>1)&8)|
                            ((l<<1)&16)|
                            ((l<<3)&32)|
                            ((l<<5)&64)|
                            ((l<<7)&128));
                }
            }
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                spriteFIFO[i].highBp = rom.readVram((tileId<<4)|8|bpLine);
                if((attr&(1<<6)) != 0){
                    byte l = spriteFIFO[i].highBp;
                    spriteFIFO[i].highBp = (byte)(((l>>7)&1)|
                            ((l>>5)&2)|
                            ((l>>3)&4)|
                            ((l>>1)&8)|
                            ((l<<1)&16)|
                            ((l<<3)&32)|
                            ((l<<5)&64)|
                            ((l<<7)&128));
                }
                
                // XXX: When should I initialize the sprite FIFO?
                spriteFIFO[i].downCounter = Byte
                        .toUnsignedInt(secondaryOAM[secondaryOAMAddr+3]);
                spriteFIFO[i].flags = secondaryOAM[secondaryOAMAddr+2];
                
                secondaryOAMAddr += 4;
            }
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
        }
        
        for(int i=0;i<3;i++){
            if(preRender && (mask&MASK_BACKGROUND) != 0){
                v &= ~((0b11111<<5)|(0b111111111111<<3)|0x800);
                v |= t&((0b11111<<5)|(0b111111111111<<3)|0x800);
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                rom.readVram(0x2000|(v&0x0FFF));
            }
            
            if(preRender && (mask&MASK_BACKGROUND) != 0){
                v &= ~((0b11111<<5)|(0b111111111111<<3)|0x800);
                v |= t&((0b11111<<5)|(0b111111111111<<3)|0x800);
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if(preRender && (mask&MASK_BACKGROUND) != 0){
                v &= ~((0b11111<<5)|(0b111111111111<<3)|0x800);
                v |= t&((0b11111<<5)|(0b111111111111<<3)|0x800);
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                        ((v>>2)&7));
            }
            
            if(preRender && (mask&MASK_BACKGROUND) != 0){
                v &= ~((0b11111<<5)|(0b111111111111<<3)|0x800);
                v |= t&((0b11111<<5)|(0b111111111111<<3)|0x800);
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if(preRender && (mask&MASK_BACKGROUND) != 0){
                v &= ~((0b11111<<5)|(0b111111111111<<3)|0x800);
                v |= t&((0b11111<<5)|(0b111111111111<<3)|0x800);
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            int tileId = Byte.toUnsignedInt(secondaryOAM[secondaryOAMAddr+1]);
            int y = Byte.toUnsignedInt(secondaryOAM[secondaryOAMAddr]);
            byte attr = secondaryOAM[secondaryOAMAddr+2];
            
            int bpLine = ((scanline-y)&7)^(((attr>>7)&1)*7);
            
            if((mask&MASK_SPRITES) != 0){
                spriteFIFO[i+3].lowBp = rom.readVram((tileId<<4)|bpLine);
                if((attr&(1<<6)) != 0){
                    byte l = spriteFIFO[i+3].lowBp;
                    spriteFIFO[i+3].lowBp = (byte)(((l>>7)&1)|
                            ((l>>5)&2)|
                            ((l>>3)&4)|
                            ((l>>1)&8)|
                            ((l<<1)&16)|
                            ((l<<3)&32)|
                            ((l<<5)&64)|
                            ((l<<7)&128));
                }
            }
            
            if(preRender && (mask&MASK_BACKGROUND) != 0){
                v &= ~((0b11111<<5)|(0b111111111111<<3)|0x800);
                v |= t&((0b11111<<5)|(0b111111111111<<3)|0x800);
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if(preRender && (mask&MASK_BACKGROUND) != 0){
                v &= ~((0b11111<<5)|(0b111111111111<<3)|0x800);
                v |= t&((0b11111<<5)|(0b111111111111<<3)|0x800);
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                spriteFIFO[i+3].highBp = rom.readVram((tileId<<4)|8|bpLine);
                if((attr&(1<<6)) != 0){
                    byte l = spriteFIFO[i+3].highBp;
                    spriteFIFO[i+3].highBp = (byte)(((l>>7)&1)|
                            ((l>>5)&2)|
                            ((l>>3)&4)|
                            ((l>>1)&8)|
                            ((l<<1)&16)|
                            ((l<<3)&32)|
                            ((l<<5)&64)|
                            ((l<<7)&128));
                }
                
                // XXX: When should I initialize the sprite FIFO?
                spriteFIFO[i+3].downCounter = Byte
                        .toUnsignedInt(secondaryOAM[secondaryOAMAddr+3]);
                spriteFIFO[i+3].flags = secondaryOAM[secondaryOAMAddr+2];
                
                secondaryOAMAddr += 4;
            }
            
            if(preRender && (mask&MASK_BACKGROUND) != 0){
                v &= ~((0b11111<<5)|(0b111111111111<<3)|0x800);
                v |= t&((0b11111<<5)|(0b111111111111<<3)|0x800);
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
        }
        
        for(int i=0;i<2;i++){
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                rom.readVram(0x2000|(v&0x0FFF));
            }
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                        ((v>>2)&7));
            }
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            int tileId = Byte.toUnsignedInt(secondaryOAM[secondaryOAMAddr+1]);
            int y = Byte.toUnsignedInt(secondaryOAM[secondaryOAMAddr]);
            byte attr = secondaryOAM[secondaryOAMAddr+2];
            
            int bpLine = ((scanline-y)&7)^(((attr>>7)&1)*7);
            
            if((mask&MASK_SPRITES) != 0){
                spriteFIFO[i+6].lowBp = rom.readVram((tileId<<4)|bpLine);
                if((attr&(1<<6)) != 0){
                    byte l = spriteFIFO[i+6].lowBp;
                    spriteFIFO[i+6].lowBp = (byte)(((l>>7)&1)|
                            ((l>>5)&2)|
                            ((l>>3)&4)|
                            ((l>>1)&8)|
                            ((l<<1)&16)|
                            ((l<<3)&32)|
                            ((l<<5)&64)|
                            ((l<<7)&128));
                }
            }
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            
            if((mask&MASK_SPRITES) != 0){
                spriteFIFO[i+6].highBp = rom.readVram((tileId<<4)|8|bpLine);
                if((attr&(1<<6)) != 0){
                    byte l = spriteFIFO[i+6].highBp;
                    spriteFIFO[i+6].highBp = (byte)(((l>>7)&1)|
                            ((l>>5)&2)|
                            ((l>>3)&4)|
                            ((l>>1)&8)|
                            ((l<<1)&16)|
                            ((l<<3)&32)|
                            ((l<<5)&64)|
                            ((l<<7)&128));
                }
                
                // XXX: When should I initialize the sprite FIFO?
                spriteFIFO[i+6].downCounter = Byte
                        .toUnsignedInt(secondaryOAM[secondaryOAMAddr+3]);
                spriteFIFO[i+6].flags = secondaryOAM[secondaryOAMAddr+2];
                
                secondaryOAMAddr += 4;
            }
            
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
        }
        
        for(int i=0;i<2;i++){
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();

            // XXX: What value should it contain when rendering is disabled?
            int tileId = 0;
            if((mask&MASK_BACKGROUND) != 0){
                tileId = Byte.toUnsignedInt(rom.readVram(0x2000|(v&0x0FFF)));
            }
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();

            // XXX: What value should it contain when rendering is disabled?
            int attr = 0;
            if((mask&MASK_BACKGROUND) != 0){
                attr = Byte.toUnsignedInt(
                        rom.readVram((0x2000+32*30)|(v&0x0C00)|((v>>4)&0x38)|
                                ((v>>2)&7)));
            }
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();

            // XXX: What value should it contain when rendering is disabled?
            int lowBp = 0;
            if((mask&MASK_BACKGROUND) != 0){
                lowBp = Byte.toUnsignedInt(
                        rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                                ((v>>12)&7)));
            }
            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();

            if((mask&MASK_BACKGROUND) != 0){
                int highBp = Byte.toUnsignedInt(
                        rom.readVram(((ctrl&(1<<4))<<(12-4))|(tileId<<4)|
                                (1<<3)|((v>>12)&7)));

                // Fill the shift registers
                lowShift &= ~0xFF;
                highShift &= ~0xFF;

                lowShift |= lowBp;
                highShift |= highBp;

                attrLatch1 = (byte)((attr>>((v&2)+((v>>4)&4)))&1);
                attrLatch2 = (byte)((attr>>((v&2)+((v>>4)&4))>>1)&1);

                // Increment coarse X in v
                int x = (v&0b11111)+1;

                v &= ~0b11111;
                v |= x&0b11111;

                // Switch nametable on overflow
                v ^= (x&(1<<5))<<5;
            }

            if((mask&MASK_SPRITES) != 0) oamAddr = 0;
            onCycle();
            if((mask&MASK_BACKGROUND) != 0) shiftBackground();
        }
        
        // Dummy nametable fetches
        
        onCycle();

        if((mask&MASK_BACKGROUND) != 0) rom.readVram(0x2000|(v&0x0FFF));
        onCycle();
        
        onCycle();
        
        if(!((mask&MASK_RENDER) != 0 && preRender && isEven)){
            if((mask&MASK_BACKGROUND) != 0) rom.readVram(0x2000|(v&0x0FFF));
            onCycle();
        }
        
        if(false){
            for(int i=0;i<8;i++){
                System.out.printf("%03d: Sprite fifo #%d -- "
                        + "c: %03d l: %02X h: %02X f: %02X\n", scanline, i,
                        spriteFIFO[i].downCounter, spriteFIFO[i].lowBp,
                        spriteFIFO[i].highBp, spriteFIFO[i].flags);
            }
        }
    }
    
    public void emulateFrame() {
        // cycleCount = 0;
        
        if(false){
            for(int i=0,n=0;i<64;i++,n+=4){
                System.out.printf("Sprite #%02d: x: %03d y: %03d "
                        + "t: %02x a: %02x\n",
                        i, oam[n+3], oam[n], oam[n+1], oam[n+2]);
            }
        }
        
        isRendering = true;
        emulateVisibleScanline(261, true, false);
        
        emulateVisibleScanline(0, false, true);
        for(int i=0;i<239;i++){
            //System.out.printf("Before scanline: %d", cycleCount);
            emulateVisibleScanline(i+1, false, false);
            //System.out.printf("After scanline: %d", cycleCount);
        }
        isRendering = false;
        
        // Post-render scanline
        for(int i=0;i<341;i++) onCycle();
        
        // First VBlank scanline
        
        onCycle();
        
        if(!keepVBlankClear) vBlank = true;
        keepVBlankClear = false;
        
        if((ctrl&CTRL_NMI) != 0) cpu.setNmiPin(false);
        
        for(int i=0;i<340;i++) onCycle();
        
        // VBlank scanlines
        for(int i=0;i<(260-241)*341;i++) onCycle();
        
        isEven = !isEven;
        
        // System.out.println(cycleCount);
        if(false){
            System.out.println("CHR");
            for(int i=0;i<8192;i+=256){
                int n;
                for(n=0;n<255;n++){
                    System.out.printf("%02x ", rom.readVram(i+n));
                }
                System.out.printf("%02x\n", rom.readVram(i+n));
            }
            System.out.println("Nametable at $2000");
            for(int i=0;i<1024;i+=32){
                int n;
                for(n=0;n<31;n++){
                    System.out.printf("%02x ", rom.readVram(0x2000+i+n));
                }
                System.out.printf("%02x\n", rom.readVram(0x2000+i+n));
            }
            System.out.println("Nametable at $2400");
            for(int i=0;i<1024;i+=32){
                int n;
                for(n=0;n<31;n++){
                    System.out.printf("%02x ", rom.readVram(0x2400+i+n));
                }
                System.out.printf("%02x\n", rom.readVram(0x2400+i+n));
            }
            System.out.println("Nametable at $2800");
            for(int i=0;i<1024;i+=32){
                int n;
                for(n=0;n<31;n++){
                    System.out.printf("%02x ", rom.readVram(0x2800+i+n));
                }
                System.out.printf("%02x\n", rom.readVram(0x2800+i+n));
            }
            System.out.println("Nametable at $2C00");
            for(int i=0;i<1024;i+=32){
                int n;
                for(n=0;n<31;n++){
                    System.out.printf("%02x ", rom.readVram(0x2C00+i+n));
                }
                System.out.printf("%02x\n", rom.readVram(0x2C00+i+n));
            }
        }
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
                
                // System.out.printf("Read to PPUDATA v: %04X\n", v);
                
                // XXX: Is it correct?
                byte value = readBuffer;
                
                int address = v&0b11111111111111;
                
                readBuffer = rom.readVram(address);
                rom.ppuIOBus = readBuffer;
                
                if(isRendering && (mask&MASK_RENDER) != 0){
                    // Increment coarse X in v
                    int x = (v&0b11111)+1;

                    v &= ~0b11111;
                    v |= x&0b11111;

                    // Switch nametable on overflow
                    v ^= (x&(1<<5))<<5;

                    // Increment the vertical position in v
                    int y = v;

                    y += (1<<12);
                    y += (y&(1<<15))>>10;

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
                    v += (((ctrl&CTRL_INC)>>2)&1)*31+1;
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
        rom.ppuIOBus = value;
        
        if(false){
            System.out.printf("Writing %02X to register %d -- v: %04X\n",
                    value, register, v);
        }
        
        switch(register){
            case 0:
                // PPUCTRL
                
                // TODO: Handle PPU startup properly
                
                t &= ~(3<<10);
                t |= (value&3)<<10;
                
                ctrl = value;
                
                break;
                
            case 1:
                // PPUMASK
                
                // TODO: Handle PPU startup properly
                
                mask = value;
                
                break;
                
            case 2:
                // PPUSTATUS
                
                break;
                
            case 3:
                // OAMADDR
                
                // TODO: Emulate corruption
                
                oamAddr = Byte.toUnsignedInt(value);
                
                break;
                
            case 4:
                // OAMDATA
                
                oam[oamAddr] = value;
                oamAddr++;
                oamAddr &= 0xFF;
                
                break;
                
            case 5:
                // PPUSCROLL
                
                // TODO: Handle PPU startup properly
                
                if(w){
                    // 2nd write
                    
                    t &= ~((7<<12)|(0b1111100000));
                    t |= (value&7)<<12;
                    t |= ((value>>3)&0b11111)<<5;
                    
                    w = false;
                }else{
                    // First write
                    
                    t &= ~0b11111;
                    t |= (value>>3);
                    x = (byte)(value&0b111);
                    w = true;
                }
                
                break;
                
            case 6:
                // PPUADDR
                
                // TODO: Handle PPU startup properly
                
                if(w){
                    // 2nd write
                    
                    t &= ~0xFF;
                    t |= Byte.toUnsignedInt(value);
                    
                    v = t;
                    
                    // System.out.printf("Write to PPUADDR -- "
                    //         + "2nd write v: %04X\n",
                    //         v);
                    
                    w = false;
                }else{
                    // First write
                    
                    // System.out.printf("Write to PPUADDR -- "
                    //         + "1st write byte: %02X\n",
                    //         v);
                    
                    t &= 0xFF;
                    t |= (value&0b111111)<<8;
                    
                    w = true;
                }
                
                break;
                
            case 7:
                // PPUDATA
                
                // System.out.printf("Write to PPUDATA v: %04X byte: %02X\n", v,
                //         value);
                
                rom.writeVram(v&0b11111111111111, value);
                
                if(isRendering && (mask&MASK_RENDER) != 0){
                    // Increment coarse X in v
                    int x = (v&0b11111)+1;

                    v &= ~0b11111;
                    v |= x&0b11111;

                    // Switch nametable on overflow
                    v ^= (x&(1<<5))<<5;

                    // Increment the vertical position in v
                    int y = v;

                    y += (1<<12);
                    y += (y&(1<<15))>>10;

                    if((y&0b1111100000) == (30<<5)){
                        y &= ~0b1111100000;
                        v ^= 0x800;
                    }

                    v &= ~((7<<12)|0b1111100000);
                    v |= y&((7<<12)|0b1111100000);
                    
                    // Have I understood the "read next value" that is written
                    // in the wiki correctly?
                    rom.ppuIOBus = rom.readVram(v&0b11111111111111);
                }else{
                    v += (((ctrl&CTRL_INC)>>2)&1)*31+1;
                }
                
                break;
        }
    }
}
