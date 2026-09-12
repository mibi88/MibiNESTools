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

/**
 *
 * @author mibi88
 */
public abstract class Rom {
    protected byte[] data;
    
    protected byte ppuIOBus;
    
    protected PPU ppu;
    protected APU apu;
    
    protected Controller controller1;
    protected Controller controller2;
    
    /**
     * Create a new instance of the Rom class.
     * @param data The content of the ROM file.
     */
    public Rom(byte[] data) {
        this.data = data;
    }
    
    /**
     * Read a byte.
     * @param address The address to read from.
     * @return The byte read.
     */
    public abstract byte read(int address);
    
    public abstract void write(int address, byte value);
    
    public abstract byte readVram(int address);
    
    public abstract void writeVram(int address, byte value);
    
    public void cpuCycleStart() {
        // Do nothing
    }
    
    public void cpuCycleEnd() {
        // Do nothing
    }
    
    public byte setPPUIOBus(byte ppuIOBus) {
        return this.ppuIOBus = ppuIOBus;
    }
    
    public byte getPPUIOBus() {
        return ppuIOBus;
    }
    
    public void setPPU(PPU ppu) {
        this.ppu = ppu;
    }
    
    public void setAPU(APU apu) {
        this.apu = apu;
    }
    
    public void setController1(Controller controller) {
        controller1 = controller;
    }
    
    public void setController2(Controller controller) {
        controller2 = controller;
    }
    
    public void reset() {
        // Do nothing
    }
}
