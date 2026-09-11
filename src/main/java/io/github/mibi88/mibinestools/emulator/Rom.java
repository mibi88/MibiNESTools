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

import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author mibi88
 */
public class Rom {
    private byte[] data;
    private Screen screen;
    private byte[] ram;

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
        }
        ram = new byte[0x800];
    }
    
    /**
     * Read a byte.
     * @param address The address to read from.
     * @return The byte read.
     */
    public byte read(int address) {
        return 0; // TODO
    }
    
    public void write(int address, byte value) {
        //
    }
    
    public byte readVram(int address) {
        return 0; // TODO
    }
    
    public void writeVram(int address, byte value) {
        //
    }
    
    public void cpuCycleStart() {
        //
    }
    
    public void cpuCycleEnd() {
        //
    }
}
