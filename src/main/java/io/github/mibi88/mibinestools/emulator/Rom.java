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
    byte[] data;

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
            data = new byte[0xFFFF];
        }
    }
    
    /**
     * Get the CHR data from the ROM.
     * @return The CHR data.
     */
    public CHRData getCHRData() {
        return new CHRData(Arrays.copyOfRange(data, 0x8000,
                0xA000));
    }
    
    /**
     * Get the raw CHR data from the ROM.
     * @return The CHR data.
     */
    public byte[] getRawCHRData() {
        return Arrays.copyOfRange(data, 0x8000, 0xA000);
    }
}