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
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

/**
 *
 * @author mibi88
 */
public class Rom {
    private byte[] data;
    private Screen screen;
    private byte[] ram;
    private byte[] prgRom;

    /**
     * Load a NES ROM.
     * @param file The file to load.
     * @throws Exception Thrown if the file could not be read.
     */
    public Rom(File file) throws Exception {
        if(file != null){
            FileInputStream fileStream = new FileInputStream(file);
            data = new byte[fileStream.available()];
            fileStream.read(data);
            fileStream.close();
        }else{
            data = new byte[0x10000];
            ram = new byte[0x800];
        }
        prgRom = Arrays.copyOfRange(data, 0x0010, 0x4010);
    }
    
    public int nmi() {
        byte[] vectors = getVectors();
        return vectors[0]|(vectors[1]<<8);
    }
    
    public int reset() {
        byte[] vectors = getVectors();
        return vectors[2]|(vectors[3]<<8);
    }
    
    public int irq() {
        byte[] vectors = getVectors();
        return vectors[4]|(vectors[5]<<8);
    }
    
    /**
     * Get the CHR data from the ROM.
     * @return The CHR data.
     */
    public CHRData getCHRData() {
        return new CHRData(getRawCHRData());
    }
    
    /**
     * Get the raw CHR data from the ROM.
     * @return The CHR data.
     */
    public byte[] getRawCHRData() {
        return Arrays.copyOfRange(data, 0x4010, 0x6010);
    }
    
    public byte[] getVectors() {
        return Arrays.copyOfRange(data, 0x400A, 0x4010);
    }
    
    public void setScreen(Screen screen) {
        this.screen = screen;
    }
    
    /**
     * Read a byte.
     * @param address The address to read from.
     * @return The byte read.
     */
    public byte read(int address) {
        if(address >= 0){
            if(address <= 0x1FFF){
                return ram[address%0x800];
            }else if(address <= 0x3FFF){
                switch(address%8){
                    case 0:
                        return screen.readPPUCTRL();
                    case 1:
                        return screen.readPPUMASK();
                    case 2:
                        return screen.readPPUSTATUS();
                    case 3:
                        return screen.readOAMADDR();
                    case 4:
                        return screen.readOAMDATA();
                    case 5:
                        return screen.readPPUSCROLL();
                    case 6:
                        return screen.readPPUADDR();
                    default:
                        return screen.readPPUDATA();
                }
            }else if(address <= 0x401F){
                // TODO: APU
                return 0x00;
            }else if(address >= 0x8000 && address <= 0xFFFF){
                int pos = address-0x8000;
                pos %= 0x4000;
                return prgRom[pos];
            }
        }
        return 0x00;
    }
    
    public void write(int address, byte value) {
        if(address >= 0){
            if(address <= 0x1FFF){
                ram[address%0x800] = value;
            }else if(address <= 0x3FFF){
                switch(address%8){
                    case 0:
                        screen.writePPUCTRL(value);
                    case 1:
                        screen.writePPUMASK(value);
                    case 2:
                        screen.writePPUSTATUS(value);
                    case 3:
                        screen.writeOAMADDR(value);
                    case 4:
                        screen.writeOAMDATA(value);
                    case 5:
                        screen.writePPUSCROLL(value);
                    case 6:
                        screen.writePPUADDR(value);
                    default:
                        screen.writePPUDATA(value);
                }
            }else if(address <= 0x401F){
                // TODO: APU
            }
        }
    }
}
