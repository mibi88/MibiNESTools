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
public abstract class NESController extends Controller {
    public static final byte A_BUTTON = (byte)(1);
    public static final byte B_BUTTON = (byte)(1<<1);
    public static final byte SELECT_BUTTON = (byte)(1<<2);
    public static final byte START_BUTTON = (byte)(1<<3);
    public static final byte UP_BUTTON = (byte)(1<<4);
    public static final byte DOWN_BUTTON = (byte)(1<<5);
    public static final byte LEFT_BUTTON = (byte)(1<<6);
    public static final byte RIGHT_BUTTON = (byte)(1<<7);
    
    @Override
    public byte read() {
        byte value = (byte)(reg&1);
        
        // Shift reg
        reg >>= 1;
        reg |= 1<<7;
        
        return value;
    }

    @Override
    public void cycle() {
        if(strobe){
            reg = getInput();
        }
    }
    
    public abstract byte getInput();
    
}
