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

/**
 *
 * @author mibi88
 */
public class NRom extends Rom {
    private byte[] ram;
    
    private byte[] vram;
    
    private byte[] chr;
    
    private boolean horizontal;
    private boolean chrRam;
    
    private byte bus;
    
    private int prgRomStart;
    private int prgRomSize;

    public NRom(byte[] data) {
        super(data);
        
        ram = new byte[0x800];
        
        vram = new byte[0x400*2+0x20];
        
        prgRomStart = 16;
        
        horizontal = (data[6]&1) == 0;
        if((data[6]&(1<<2)) != 0){
            // This ROM has a trainer
            
            prgRomStart += 512;
        }
        
        prgRomSize = data[4]*16*1024;
        
        if(data[5] == 0){
            chrRam = true;
            
            chr = new byte[0x2000];
        }else{
            chr = Arrays.copyOfRange(data, prgRomStart+prgRomSize,
                    prgRomStart+prgRomSize+data[5]*8*1024);
        }
        
        System.out.println("This is a NROM rom!");
    }
    
    /**
     * Read a byte.
     * @param address The address to read from.
     * @return The byte read.
     */
    @Override
    public byte read(int address) {
        if(address >= 0x8000){
            return bus = data[prgRomStart+(address-0x8000)%prgRomSize];
        }else if(address < 0x0800){
            return bus = ram[address];
        }else if(address < 0x2000){
            return bus = ram[address%0x0800];
        }else if(address < 0x4000){
            return bus = ppu.read(address&7);
        }else if(address < 0x4018){
            // TODO: Read from the APU
            // TODO: Let the APU handle reads to $4016 and $4017
            // TODO: Correctly return open bus for reads to $4016 and $4017
            
            if(address == 0x4016){
                return bus = controller1.read();
            }else if(address == 0x4017){
                return bus = controller2.read();
            }
        }else if(address < 0x4020){
            // CPU test mode
        }
        
        return bus;
    }
    
    @Override
    public void write(int address, byte value) {
        //
    }
    
    @Override
    public byte readVram(int address) {
        if(address < 0x200){
            return chr[address];
        }else if(address < 0x3000){
            if(horizontal){
                return vram[((address-0x2000)&~0xC00)|((address&0x800)>>1)];
            }
            
            return vram[(address-0x2000)&0x7FF];
        }else if(address >= 0x3F00){
            if((address&3) == 0){
                return vram[0x800+(address&0xF)];
            }
            
            return vram[0x800+(address&0x1F)];
        }
        
        return ppuIOBus;
    }
    
    @Override
    public void writeVram(int address, byte value) {
        if(address >= 0x2000 && address < 0x3000){
            if(horizontal){
                vram[((address-0x2000)&~0xC00)|((address&0x800)>>1)] = value;
            }else{
                vram[(address-0x2000)&0x7FF] = value;
            }
        }else if(address >= 0x3F00){
            if((address&3) == 0){
                vram[0x800+(address&0xF)] = value;
            }else{
                vram[0x800+(address&0x1F)] = value;
            }
        }else if(chrRam && address < 0x2000){
            chr[address] = value;
        }
    }
}
