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

/**
 *
 * @author mibi88
 */
public class DMA {
    private boolean cycle;
    
    private byte value;
    
    private int step;
    
    private boolean aligned;
    
    private boolean doOAMDMA;
    private boolean doDMCDMA;
    
    private int page;
    
    private Rom rom;
    
    public DMA(Rom rom) {
        this.rom = rom;
        
        cycle = false; // TODO: Pick a random value
        
        value = 0;
        step = 0;
        
        doOAMDMA = false;
        doDMCDMA = false;
        
        aligned = true;
    }
    
    public void cycle(CPU cpu) {
        if(doOAMDMA){
            cpu.setRdyPin(false);
            if(cpu.isHalted() && ((!cycle && !aligned) || aligned)){
                if(cycle){
                    // put cycle

                    rom.write(0x2004, value);
                    
                    step++;
                    
                    if(step >= 256){
                        doOAMDMA = false;
                        cpu.setRdyPin(true);
                    }
                }else{
                    // get cycle

                    value = rom.read(page|step);
                }
                
                aligned = true;
            }
        }else{
            aligned = false;
            step = 0;
        }
        
        cycle = !cycle;
    }
    
    public void startOAMDMA(int page){
        this.page = page<<8;
        
        doOAMDMA = true;
    }
}
