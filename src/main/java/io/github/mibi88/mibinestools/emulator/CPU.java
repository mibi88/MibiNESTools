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
public class CPU {
    private Rom rom;
    
    private int pc;
    
    private int s;
    private int p;
    private int a;
    private int x;
    private int y;
    
    private int cycle;
    private int targetCycle;
    
    private int opcode;
    private int t;
    private int tmp1, tmp2;
    private int lastRead;
    
    private boolean jammed;
    private boolean halted;
    
    private short pinHandling;
    
    private boolean rdy;
    
    private boolean irqPin;
    private boolean nmiPin;
    private boolean nmiPinLast;
    private boolean shouldNmi;
    private boolean shouldIrq;
    private boolean nmiDetected;
    private boolean irqDetected;
    
    private boolean executeIntNext;
    private boolean executeInt;
    
    private boolean isIrq;
    
    private boolean opcodeLoaded;
    
    private boolean skipAnd;
    
    private static final int C_FLAG = 1;
    private static final int Z_FLAG = 1<<1;
    private static final int I_FLAG = 1<<2;
    private static final int D_FLAG = 1<<3;
    private static final int B_FLAG = 1<<4;
    private static final int V_FLAG = 1<<6;
    private static final int N_FLAG = 1<<7;
    
    public CPU(Rom rom) {
        this.rom = rom;
        
        pc = 0;
        jammed = false;
        halted = false;
        
        s = 0xFD;
        a = 0;
        x = 0;
        y = 0;
        p = I_FLAG;
        
        cycle = 8;
        targetCycle = 0;
        
        rdy = true;
        
        irqPin = false;
        nmiPin = false;
        nmiPinLast = false;
        
        shouldNmi = false;
        shouldIrq = false;
        nmiDetected = false;
        irqDetected = false;
        
        executeIntNext = false;
        executeInt = false;
        
        opcodeLoaded = false;
        
        skipAnd = false;
    }
    
    private int read(int addr) {
        int v = Byte.toUnsignedInt(rom.read(addr));
        
        if(!rdy) halted = true;
        
        lastRead = v;
        
        return v;
    }
    
    private void write(int addr, int value) {
        rom.write(addr, (byte)value);
    }
    
    private void updateNZ(int reg) {
        if(reg == 0) p |= Z_FLAG;
        else p &= ~Z_FLAG;
        if((reg&(1<<7)) != 0) p |= N_FLAG;
        else p &= ~N_FLAG;
    }
    
    private void cmp(int reg, int value) {
        if(reg >= value) p |= C_FLAG;
        else p &= ~C_FLAG;
        if(reg == value) p |= Z_FLAG;
        else p &= ~Z_FLAG;
        if(((reg-value)&(1<<7)) != 0) p |= N_FLAG;
        else p &= ~N_FLAG;
    }
    
    private void adc(int value) {
        int oldA = this.a;
        
        a = (a+value+(p&C_FLAG));
        
        if((a&(~0xFF)) != 0) p |= C_FLAG;
        else p &= ~C_FLAG;
        
        if(((a^oldA)&(a^value)&(1<<7)) != 0) p |= V_FLAG;
        else p &= ~V_FLAG;
        
        a &= 0xFF;
        
        updateNZ(a);
    }
    
    private void sbc(int value) {
        int oldA = this.a;
        
        a = (a-value-(p&C_FLAG));
        
        if((a&(~0xFF)) == 0) p |= C_FLAG;
        else p &= ~C_FLAG;
        
        if(((a^oldA)&(a^(~value))&(1<<7)) != 0) p |= V_FLAG;
        else p &= ~V_FLAG;
        
        a &= 0xFF;
        
        updateNZ(a);
    }
    
    public int asl(int value) {
        p &= ~C_FLAG;
        p |= (value>>7)&1;
        
        value <<= 1;
        
        value &= 0xFF;
        
        updateNZ(value);
        
        return value;
    }
    
    public int rol(int value) {
        int cFlag = p&C_FLAG;
        
        p &= ~C_FLAG;
        p |= (value>>7)&1;
        
        value <<= 1;
        value |= cFlag;
        
        value &= 0xFF;
        
        updateNZ(value);
        
        return value;
    }
    
    public int lsr(int value) {
        p &= ~C_FLAG;
        p |= value&1;
        
        value >>= 1;
        
        value &= 0xFF;
        
        updateNZ(value);
        
        return value;
    }
    
    public int ror(int value) {
        int cFlag = p&C_FLAG;
        
        p &= ~C_FLAG;
        p |= value&1;
        
        value >>= 1;
        value |= cFlag<<7;
        
        value &= 0xFF;
        
        updateNZ(value);
        
        return value;
    }
    
    public void bit(int value) {
        if((value&a) == 0) p |= Z_FLAG;
        else p &= ~Z_FLAG;
        
        p &= (1<<6)-1;
        p |= value&(0b11<<6);
    }
    
    private void imp(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 2;
                
                break;
                
            case 2:
                op.operation(this, 0);
                
                break;
        }
    }
    
    private void imm(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 2;
                
                break;
                
            case 2:
                op.operation(this, t);
                
                pc++;
                
                break;
        }
    }
    
    private void absRead(Operation op) {
        switch(cycle) {
            case 1:
                targetCycle = 4;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                tmp1 = t|(read(pc)<<8);
                pc++;

                break;
                
            case 4:
                op.operation(this, read(tmp1));
                
                break;
        }
    }
    
    private void absRMW(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 6;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                tmp1 = t|(read(pc)<<8);
                
                pc++;
                
                break;
                
            case 4:
                t = read(tmp1);
                
                break;
                
            case 5:
                write(tmp1, t);
                
                t = op.operation(this, t);
                
                break;
                
            case 6:
                write(tmp1, t);
                
                break;
        }
    }
    
    private void absStore(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 4;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                tmp1 = t|(read(pc)<<8);
                
                pc++;
                
                break;
                
            case 4:
                write(tmp1, op.operation(this, 0));
                
                break;
        }
    }
    
    private void zpRead(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 3;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                op.operation(this, read(t));
        }
    }
    
    private void zpRMW(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 5;
                
                break;
                
            case 2:
                pc++;
                
                tmp1 = t;
                
                break;
                
            case 3:
                t = read(tmp1);
                
                break;
                
            case 4:
                write(tmp1, t);
                t = op.operation(this, t);
                
                break;
                
            case 5:
                write(tmp1, t);
                
                break;
        }
    }
    
    private void zpStore(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 3;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                write(t, op.operation(this, 0));
                
                break;
        }
    }
    
    private void zpIRead(int i, Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 4;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                read(t);
                t += i;
                t &= 0xFF;
                
                break;
                
            case 4:
                op.operation(this, read(t));
                
                break;
        }
    }
    
    private void zpIRMW(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 6;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                read(t);
                
                t += x;
                t &= 0xFF;
                
                break;
                
            case 4:
                tmp1 = t;
                t = read(tmp1);
                
                break;
                
            case 5:
                write(tmp1, t);
                t = op.operation(this, t);
                
                break;
                
            case 6:
                write(tmp1, t);
                
                break;
        }
    }
    
    private void zpIStore(int i, Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 4;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                read(t);
                
                t += i;
                t &= 0xFF;
                
                break;
                
            case 4:
                write(t, op.operation(this, 0));
                
                break;
        }
    }
    
    private void absIRead(int i, Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 4;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
            {
                tmp1 = t;
                
                t = read(pc);
                
                tmp1 += i;
                
                int tmp = tmp1>>8;
                tmp1 = (tmp1&0xFF)|(t<<8);
                t = tmp;
                
                pc++;
                
                break;
            }
            
            case 4:
            {
                int tmp = read(tmp1);
                if(t != 0){
                    tmp1 += t<<8;
                    tmp1 &= 0xFFFF;
                    
                    targetCycle++;
                }else{
                    op.operation(this, tmp);
                }
                
                break;
            }
            
            case 5:
                // NOTE: This case is only executed if a page boundary has been
                //       crossed.
                op.operation(this, read(tmp1));
                
                break;
        }
    }
    
    private void absIRMW(int i, Operation op) {
        // TODO
    }
    
    private void absIStore(int i, Operation op) {
        // TODO
    }
    
    private void relative(boolean shouldBranch) {
        // TODO
    }
    
    private void idxIndRead(Operation op) {
        // TODO
    }
    
    private void idxIndRMW(Operation op) {
        // TODO
    }
    
    private void idxIndStore(Operation op) {
        // TODO
    }
    
    private void indIdxRead(Operation op) {
        // TODO
    }
    
    private void indIdxRMW(Operation op) {
        // TODO
    }
    
    private void indIdxStore(Operation op) {
        // TODO
    }
    
    private void indIdxSH(Operation op) {
        // TODO
    }
    
    private void absISH(Operation op) {
        // TODO
    }
    
    public void cycle() {
        /*
         * To implement this, I read https://www.nesdev.org/6502_cpu.txt.
         *
         * The follwing websites were also useful:
         * https://www.nesdev.org/wiki/CPU_unofficial_opcodes
         * http://www.6502.org/users/obelisk/6502/reference.html
         * https://www.oxyron.de/html/opcodes02.html
         * https://www.nesdev.org/wiki/Instruction_reference#ADC
         */
        
        rom.cpuCycleStart();
        
        if(jammed) return;
        if(halted){
            read(lastRead);
            
            if(rdy) halted = false; // XXX: Is it accurate?
            return;
        }
        
        // The internal signals are raised during phi 1 of each cycle
        if(nmiDetected) shouldNmi = true;
        if(irqDetected) shouldIrq = true;
        
        if(cycle == 2){
            t = read(lastRead);
        }else if(cycle > targetCycle){
            opcode = read(pc);
            
            cycle = 1;
            targetCycle = 2;
            if(executeIntNext){
                executeInt = true;
                executeIntNext = false;
            }else{
                pc++;
            }
        }
        
        do{
            if(opcodeLoaded){
                cycle = 1;
                targetCycle = 2;
                if(executeIntNext){
                    executeInt = true;
                    executeIntNext = false;
                }else{
                    pc++;
                }
                
                opcodeLoaded = false;
            }
            
            if(executeInt){
                switch(cycle){
                    case 1:
                        targetCycle = 7;
                        
                        // BRK is forced into the opcode register
                        opcode = 0x00;
                        
                    case 2:
                        // NOTE: PC inrement is not performed on interrupt,
                        //       only on BRK
                        break;
                        
                    case 3:
                        write(0x0100+s, pc>>8);
                        s--;
                        break;
                        
                    case 4:
                        // NOTE: write only keeps the lower 8 bits anyways
                        write(0x0100+s, pc);
                        s--;
                        
                        isIrq = true;
                        if(shouldNmi){
                            isIrq = false;
                            shouldNmi = false;
                        }
                        break;
                        
                    case 5:
                        write(0x0100+s, p);
                        s--;
                        break;
                        
                    case 6:
                        pc &= 0xFF00;
                        pc |= read(isIrq ? 0xFFFE : 0xFFFA);
                        break;
                        
                    case 7:
                        pc &= 0xFF;
                        pc |= read(isIrq ? 0xFFFF : 0xFFFB)<<8;
                        
                        if(!isIrq) shouldNmi = false;
                        executeInt = false;
                }
                
                cycle++;
                return;
            }
            
            switch(opcode){
                //
            }
        }while(opcodeLoaded);
        
        if((opcode&31) != 16 && opcode != 0 && cycle == targetCycle-1){
            // Check for interrupts

            if(shouldNmi || (shouldIrq && (p&I_FLAG) == 0)){
                executeIntNext = true;
            }
        }

        // The edge detector and level detector polling is performed on
        // phi 2 of each cycle
        nmiDetected = (nmiPin != nmiPinLast) && !nmiPin;
        irqDetected = !irqPin;

        // The internal signal for a detected IRQ is only high during a
        // single cycle
        shouldIrq = false;

        cycle++;

        nmiPinLast = nmiPin;
        
        rom.cpuCycleEnd();
    }
}
