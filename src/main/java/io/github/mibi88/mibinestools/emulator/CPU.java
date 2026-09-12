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
        
        pc = Byte.toUnsignedInt(rom.read(0xFFFC))|
                (Byte.toUnsignedInt(rom.read(0xFFFD))<<8);
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
        switch(cycle){
            case 1:
                targetCycle = 7;
                
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
                read(tmp1);
                
                tmp1 += t<<8;
                tmp1 &= 0xFFFF;
                
                break;
                
            case 5:
                t = read(tmp1);
                
                break;
                
            case 6:
                write(tmp1, t);
                
                t = op.operation(this, t);
                
                break;
                
            case 7:
                write(tmp1, t);
                
                break;
        }
    }
    
    private void absIStore(int i, Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 5;
                
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
                read(tmp1);
                
                tmp1 += t<<8;
                tmp1 &= 0xFFFF;
                
                break;
                
            case 5:
                write(tmp1, op.operation(this, 0));
                
                break;
        }
    }
    
    private void relative(boolean shouldBranch) {
        switch(cycle){
            case 1:
                targetCycle = 3;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
            {
                int tmp = read(pc);
                
                if(shouldBranch){
                    tmp1 = pc+(byte)t;
                    
                    pc = (tmp&0xFF)|(pc&0xFF00);
                    if(pc != tmp1){
                        // Check for interrupts

                        if(shouldNmi || (shouldIrq && (p&I_FLAG) == 0)){
                            executeIntNext = true;
                        }
                    }
                    
                    targetCycle++;
                }else{
                    opcode = tmp;
                    opcodeLoaded = true;
                }
                
                break;
            }
            
            case 4:
            {
                int tmp = read(pc);
                
                if(pc != tmp1){
                    pc = tmp1;
                }else{
                    opcode = tmp;
                    opcodeLoaded = true;
                }
                
                break;
            }
        }
    }
    
    private void idxIndRead(Operation op) {
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
                tmp1 = read(t);
                
                break;
                
            case 5:
                tmp1 |= read((t+1)&0xFF)<<8;
                
                break;
                
            case 6:
                op.operation(this, read(tmp1));
                
                break;
        }
    }
    
    private void idxIndRMW(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 8;
                
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
                tmp1 = read(t);
                
                break;
                
            case 5:
                tmp1 |= read((t+1)&0xFF)<<8;
                
                break;
                
            case 6:
                t = read(tmp1);
                
                break;
                
            case 7:
                write(tmp1, t);
                t = op.operation(this, t);
                
                break;
                
            case 8:
                write(tmp1, t);
                
                break;
        }
    }
    
    private void idxIndStore(Operation op) {
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
                tmp1 = read(t);
                
                break;
                
            case 5:
                tmp1 |= read((t+1)&0xFF)<<8;
                
                break;
                
            case 6:
                write(tmp1, op.operation(this, 0));
        }
    }
    
    private void indIdxRead(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 5;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                tmp1 = read(t);
                
                break;
                
            case 4:
                tmp1 |= read((t+1)&0xFF)<<8;
                tmp2 = tmp1+y;
                tmp1 = (tmp2&0xFF)|(tmp1&0xFF00);
                
                break;
                
            case 5:
            {
                int tmp = read(tmp1);
                
                if(tmp1 != tmp2){
                    tmp1 = tmp2;
                    targetCycle++;
                }else{
                    op.operation(this, tmp);
                }
                
                break;
            }
            
            case 6:
                // This cycle is only executed if the effective address was
                // fixed
                
                op.operation(this, read(tmp1));
                
                break;
        }
    }
    
    private void indIdxRMW(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 8;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                tmp1 = read(t);
                
                break;
                
            case 4:
                tmp1 |= read((t+1)&0xFF)<<8;
                tmp2 = tmp1+y;
                tmp1 = (tmp2&0xFF)|(tmp1&0xFF00);
                
                break;
                
            case 5:
                read(tmp1);
                
                break;
                
            case 6:
                t = read(tmp2);
                
                break;
                
            case 7:
                write(tmp1, t);
                t = op.operation(this, t);
                
                break;
                
            case 8:
                write(tmp1, t);
        }
    }
    
    private void indIdxStore(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 6;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                tmp1 = read(t);
                
                break;
                
            case 4:
                tmp1 |= read((t+1)&0xFF)<<8;
                tmp2 = tmp1+y;
                tmp1 = (tmp2&0xFF)|(tmp1&0xFF00);
                
                break;
                
            case 5:
            {
                read(tmp1);
                
                break;
            }
            
            case 6:
                write(tmp2, op.operation(this, 0));
                
                break;
        }
    }
    
    private void indIdxSH(Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 6;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
                tmp1 |= read(t);
                
                break;
                
            case 4:
                skipAnd = !rdy;
                
                tmp1 |= read((t+1)&0xFF)<<8;
                tmp2 = tmp1+y;
                tmp1 = (tmp2&0xFF)|(tmp1&0xFF00);
                
                break;
                
            case 5:
                t = tmp2-read(tmp1);
                t &= 0xFF;
                
                break;
                
            case 6:
                if(t != 0 && !skipAnd) tmp1 &= (tmp1<<8)|0xFF;
                write(tmp1, op.operation(this, t));
                
                break;
        }
    }
    
    private void absISH(int i, Operation op) {
        switch(cycle){
            case 1:
                targetCycle = 5;
                
                break;
                
            case 2:
                pc++;
                
                break;
                
            case 3:
            {
                skipAnd = !rdy;
                tmp1 = t;
                t = read(pc);
                tmp1 += i;
                tmp1 &= 0xFF;
                int tmp = tmp1>>8;
                tmp1 = (tmp1&0xFF)|(t<<8);
                t = tmp;
                pc++;
                
                break;
            }
            
            case 4:
                read(tmp1);
                tmp1 += t<<8;
                
                break;
                
            case 5:
                if(t != 0 && !skipAnd) tmp1 &= (tmp1<<8)|0xFF;
                write(tmp1, op.operation(this, a));
        }
    }
    
    public void setRdyPin(boolean rdy) {
        this.rdy = rdy;
    }
    
    public void setIrqPin(boolean irq) {
        this.irqPin = irq;
    }
    
    public void setNmiPin(boolean nmi) {
        this.nmiPin = nmi;
    }
    
    public boolean isHalted() {
        return halted;
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
                case 0x00:
                    // BRK
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 7;
                            
                            break;
                            
                        case 2:
                            pc++;
                            
                            break;
                            
                        case 3:
                            write(0x0100+s, pc>>8);
                            s--;
                            p |= B_FLAG;
                            
                            break;
                            
                        case 4:
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
                            
                            break;
                    }
                    
                    break;
                    
                case 0x40:
                    // RTI
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 6;
                            
                            break;
                            
                        case 3:
                            s++;
                            
                            break;
                            
                        case 4:
                            p = read(0x0100+s);
                            s++;
                            
                            break;
                            
                        case 5:
                            pc &= 0xFF00;
                            pc |= read(0x0100+s);
                            s++;
                            
                            break;
                            
                        case 6:
                            pc &= 0xFF;
                            pc |= read(0x0100+s)<<8;
                            
                            break;
                    }
                    
                    break;
                    
                case 0x60:
                    // RTS
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 6;
                            
                            break;
                            
                        case 3:
                            s++;
                            
                            break;
                            
                        case 4:
                            pc &= 0xFF00;
                            pc |= read(0x0100+s);
                            s++;
                            
                            break;
                            
                        case 5:
                            pc &= 0xFF;
                            pc |= read(0x0100+s)<<8;
                            
                            break;
                            
                        case 6:
                            pc++;
                    }
                    
                    break;
                    
                case 0x48:
                    // PHA
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 3;
                            
                            break;
                            
                        case 3:
                            write(0x0100+s, a);
                            s--;
                            
                            break;
                    }
                    
                    break;
                    
                case 0x08:
                    // PHP
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 3;
                            
                            break;
                            
                        case 3:
                            write(0x0100+s, p|(1<<5)|B_FLAG);
                            s--;
                            
                            break;
                    }
                    
                    break;
                    
                case 0x68:
                    // PLA
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 4;
                            
                            break;
                            
                        case 3:
                            s++;
                            
                            break;
                            
                        case 4:
                            a = read(0x0100+s);
                            
                            updateNZ(a);
                            
                            break;
                    }
                    
                    break;
                    
                case 0x28:
                    // PLP
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 4;
                            
                            break;
                            
                        case 3:
                            s++;
                            
                            break;
                            
                        case 4:
                            p = read(0x0100+s)&~B_FLAG;
                            
                            break;
                    }
                    
                    break;
                    
                case 0x20:
                    // JSR
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 6;
                            
                            break;
                            
                        case 2:
                            pc++;
                            
                            break;
                            
                        case 4:
                            write(0x0100+s, pc>>8);
                            s--;
                            
                            break;
                            
                        case 5:
                            write(0x0100+s, pc);
                            s--;
                            
                            break;
                            
                        case 6:
                            pc = t|(read(pc)<<8);
                            
                            break;
                    }
                    
                    break;
                    
                // Official opcodes with implied or accumulator addressing
                
                case 0x0A:
                    // ASL
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a = asl(a);
                        }
                    });
                    
                    break;
                    
                case 0x18:
                    // CLC
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return p &= ~C_FLAG;
                        }
                    });
                    
                    break;
                    
                case 0x2A:
                    // ROL
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a = rol(a);
                        }
                    });
                    
                    break;
                    
                case 0x38:
                    // SEC
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return p |= C_FLAG;
                        }
                    });
                    
                    break;
                    
                case 0x4A:
                    // LSR
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a = lsr(a);
                        }
                    });
                    
                    break;
                    
                case 0x58:
                    // CLI
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return p &= ~I_FLAG;
                        }
                    });
                    
                    break;
                    
                case 0x6A:
                    // ROR
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a = ror(a);
                        }
                    });
                    
                    break;
                    
                case 0x78:
                    // SEI
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return p |= I_FLAG;
                        }
                    });
                    
                    break;
                    
                case 0x88:
                    // DEY
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            y--;
                            y &= 0xFF;
                            
                            updateNZ(y);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x8A:
                    // TXA
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = x;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x98:
                    // TYA
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = y;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x9A:
                    // TXS
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            s = x;
                            
                            updateNZ(s);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xA8:
                    // TAY
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            y = a;
                            
                            updateNZ(y);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xAA:
                    // TAX
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x = a;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xB8:
                    // CLV
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return p &= ~V_FLAG;
                        }
                    });
                    
                    break;
                    
                case 0xBA:
                    // TSX
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x = s;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xC8:
                    // INY
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            y++;
                            y &= 0xFF;
                            
                            updateNZ(y);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xCA:
                    // DEX
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x--;
                            x &= 0xFF;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xD8:
                    // CLD
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            p &= ~D_FLAG;
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xE8:
                    // INX
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x++;
                            x &= 0xFF;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                case 0xEA:
                    // NOP
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return 0; // Do nothing
                        }
                    });
                    
                    break;
                    
                case 0xF8:
                    // SED
                    
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return p |= D_FLAG;
                        }
                    });
                    
                    break;
                    
                // Opcodes with immediate addressing
                
                case 0x09:
                    // ORA
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a |= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x29:
                    // AND
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x49:
                    // EOR
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x69:
                    // ADC
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            adc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xA0:
                    // LDY
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            y = value;
                            
                            updateNZ(y);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xA2:
                    // LDX
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x = value;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xA9:
                    // LDA
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xC0:
                    // CPY
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(y, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xC9:
                    // CMP
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xE0:
                    // CPX
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(x, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xE9:
                    // NOTE: According to No More Secrets, instructions $EB and
                    //       $E9 are the same, said Fiskbit on the NesDev
                    //       discord.
                case 0xEB:
                    // SBC
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            sbc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                // Absolute addressing
                    
                case 0x4C:
                    // JMP
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 3;
                            
                            break;
                            
                        case 2:
                            pc++;
                            
                            break;
                            
                        case 3:
                            pc = t|(read(pc)<<8);
                            
                            break;
                    }
                    
                    break;
                
                // Absolute addressing -- read instructions
                
                case 0x0D:
                    // ORA
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a |= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x2C:
                    // BIT
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            bit(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x2D:
                    // AND
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x4D:
                    // EOR
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x6D:
                    // ADC
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            adc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xAC:
                    // LDY
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            y = value;
                            
                            updateNZ(y);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xAD:
                    // LDA
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xAE:
                    // LDX
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x = value;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xCC:
                    // CPY
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(y, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xCD:
                    // CMP
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xEC:
                    // CPX
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(x, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xED:
                    // SBC
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            sbc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                // Absolute addressing -- read-modify-write (RMW) instructions.
                
                case 0x0E:
                    // ASL
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return asl(value);
                        }
                    });
                    
                    break;
                    
                case 0x2E:
                    // ROL
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return rol(value);
                        }
                    });
                    
                    break;
                    
                case 0x4E:
                    // LSR
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return lsr(value);
                        }
                    });
                    
                    break;
                    
                case 0x6E:
                    // ROR
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return ror(value);
                        }
                    });
                    
                    break;
                    
                case 0xCE:
                    // DEC
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            updateNZ(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xEE:
                    // INC
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            updateNZ(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                // Absolute addressing -- store instructions
                
                case 0x8C:
                    // STY
                    
                    absStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return y;
                        }
                    });
                    
                    break;
                    
                case 0x8D:
                    // STA
                    
                    absStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a;
                        }
                    });
                    
                    break;
                    
                case 0x8E:
                    // STX
                    
                    absStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return x;
                        }
                    });
                    
                    break;
                    
                // Zeropage addressing
                // Zeropage addressing -- read instructions
                
                case 0x05:
                    // ORA
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a |= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x24:
                    // BIT
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            bit(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x25:
                    // AND
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x45:
                    // EOR
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x65:
                    // ADC
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            adc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xA4:
                    // LDY
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            y = value;
                            
                            updateNZ(y);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xA5:
                    // LDA
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xA6:
                    // LDX
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x = value;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xC4:
                    // CPY
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(y, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xC5:
                    // CMP
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xE4:
                    // CPX
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(x, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xE5:
                    // SBC
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            sbc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                // Zeropage addressing -- RMW instructions
                
                case 0x06:
                    // ASL
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return asl(value);
                        }
                    });
                    
                    break;
                    
                case 0x26:
                    // ROL
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return rol(value);
                        }
                    });
                    
                    break;
                    
                case 0x46:
                    // LSR
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return lsr(value);
                        }
                    });
                    
                    break;
                    
                case 0x66:
                    // ROR
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return ror(value);
                        }
                    });
                    
                    break;
                    
                case 0xC6:
                    // DEC
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            updateNZ(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xE6:
                    // INC
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            updateNZ(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                // Zeropage addressing -- store instructions
                
                case 0x84:
                    // STY
                    
                    zpStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return y;
                        }
                    });
                    
                    break;
                    
                case 0x85:
                    // STA
                    
                    zpStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a;
                        }
                    });
                    
                    break;
                    
                case 0x86:
                    // STX
                    
                    zpStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return x;
                        }
                    });
                    
                    break;
                    
                // Indexed zeropage addressing
                // Indexed zeropage addressing -- read instructions
                // Indexed with X
                    
                case 0x15:
                    // ORA
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a |= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x35:
                    // AND
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x55:
                    // EOR
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x75:
                    // ADC
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            adc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xB4:
                    // LDY
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            y = value;
                            
                            updateNZ(y);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xB5:
                    // LDA
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xD5:
                    // CMP
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xF5:
                    // SBC
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            sbc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                
                // Indexed with Y
                    
                case 0xB6:
                    // LDX
                    
                    zpIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x = value;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                
                // Indexed zeropage addressing -- RMW instructions
                    
                case 0x16:
                    // ASL
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return asl(value);
                        }
                    });
                    
                    break;
                    
                case 0x36:
                    // ROL
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return rol(value);
                        }
                    });
                    
                    break;
                    
                case 0x56:
                    // LSR
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return lsr(value);
                        }
                    });
                    
                    break;
                    
                case 0x76:
                    // ROR
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return ror(value);
                        }
                    });
                    
                    break;
                    
                case 0xD6:
                    // DEC
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            updateNZ(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xF6:
                    // INC
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            updateNZ(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                
                // Indexed zeropage addressing -- store instructions
                // Indexed with X
                    
                case 0x94:
                    // STY
                    
                    zpIStore(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return y;
                        }
                    });
                    
                    break;
                    
                case 0x95:
                    // STA
                    
                    zpIStore(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a;
                        }
                    });
                    
                    break;
                
                // Indexed with Y
                    
                case 0x96:
                    // STX
                    
                    zpIStore(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return x;
                        }
                    });
                    
                    break;
                
                // Absolute indexed addressing
                // Absolute indexed addressing -- read instructions
                // Indexed with X
                    
                case 0xBC:
                    // LDY
                    
                    absIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            y = value;
                            
                            updateNZ(y);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x1D:
                    // ORA
                    
                    absIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a |= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x3D:
                    // AND
                    
                    absIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x5D:
                    // EOR
                    
                    absIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x7D:
                    // ADC
                    
                    absIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            adc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xBD:
                    // LDA
                    
                    absIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xDD:
                    // CMP
                    
                    absIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xFD:
                    // SBC
                    
                    absIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            sbc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                
                // Indexed with Y
                    
                case 0x19:
                    // ORA
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a |= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x39:
                    // AND
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x59:
                    // EOR
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x79:
                    // ADC
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            adc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xB9:
                    // LDA
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xD9:
                    // CMP
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xF9:
                    // SBC
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            sbc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xBE:
                    // LDX
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            x = value;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                
                // Absolute indexed addressing -- RMW instructions
                    
                case 0x1E:
                    // ASL
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return asl(value);
                        }
                    });
                    
                    break;
                    
                case 0x3E:
                    // ROL
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return rol(value);
                        }
                    });
                    
                    break;
                    
                case 0x5E:
                    // LSR
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return lsr(value);
                        }
                    });
                    
                    break;
                    
                case 0x7E:
                    // ROR
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return ror(value);
                        }
                    });
                    
                    break;
                    
                case 0xDE:
                    // DEC
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            updateNZ(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xFE:
                    // INC
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            updateNZ(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                
                // Absolute addressing -- store instructions
                // Indexed with X
                
                case 0x9D:
                    // STA
                    
                    absIStore(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a;
                        }
                    });
                    
                    break;
                
                // Indexed with Y
                    
                case 0x99:
                    // STA
                    
                    absIStore(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a;
                        }
                    });
                    
                    break;
                
                // Relative addressing
                    
                case 0x10:
                    // BPL
                    
                    relative((p&N_FLAG) == 0);
                    
                    break;
                    
                case 0x30:
                    // BMI
                    
                    relative((p&N_FLAG) != 0);
                    
                    break;
                    
                case 0x50:
                    // BVC
                    
                    relative((p&V_FLAG) == 0);
                    
                    break;
                    
                case 0x70:
                    // BVS
                    
                    relative((p&V_FLAG) != 0);
                    
                    break;
                    
                case 0x90:
                    // BCC
                    
                    relative((p&C_FLAG) == 0);
                    
                    break;
                    
                case 0xB0:
                    // BCS
                    
                    relative((p&C_FLAG) != 0);
                    
                    break;
                    
                case 0xD0:
                    // BNE
                    
                    relative((p&Z_FLAG) == 0);
                    
                    break;
                    
                case 0xF0:
                    // BEQ
                    
                    relative((p&Z_FLAG) != 0);
                    
                    break;
                
                // Indexed indirect addressing
                // Indexed indirect addressing -- read instructions
                    
                case 0x01:
                    // ORA
                    
                    idxIndRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a |= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x21:
                    // AND
                    
                    idxIndRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x41:
                    // EOR
                    
                    idxIndRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x61:
                    // ADC
                    
                    idxIndRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            adc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xA1:
                    // LDA
                    
                    idxIndRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xC1:
                    // CMP
                    
                    idxIndRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xE1:
                    // SBC
                    
                    idxIndRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            sbc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                
                // Indexed indirect addressing -- store instructions
                    
                case 0x81:
                    // STA
                    
                    idxIndStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a;
                        }
                    });
                    
                    break;
                
                // Indirect indexed addressing
                // Indirect indexed addressing -- read instructions
                    
                case 0x11:
                    // ORA
                    
                    indIdxRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a |= value;
                            
                            updateNZ(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x31:
                    // AND
                    
                    indIdxRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= value;
                            
                            updateNZ(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x51:
                    // EOR
                    
                    indIdxRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a ^= value;
                            
                            updateNZ(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x71:
                    // ADC
                    
                    indIdxRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            adc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xB1:
                    // LDA
                    
                    indIdxRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            
                            updateNZ(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xD1:
                    // CMP
                    
                    indIdxRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a, value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xF1:
                    // SBC
                    
                    indIdxRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            sbc(value);
                            
                            return 0;
                        }
                    });
                    
                    break;
                
                // Indirect indexed addressing -- write instructions
                    
                case 0x91:
                    // STA
                    
                    indIdxStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a;
                        }
                    });
                
                // Indirect absolute addressing
                    
                case 0x6C:
                    // JMP
                    
                    switch(cycle){
                        case 1:
                            targetCycle = 5;
                            
                            break;
                            
                        case 2:
                            pc++;
                            
                            break;
                            
                        case 3:
                            tmp1 = read(pc)<<8;
                            tmp1 |= t;
                            pc++;
                            
                            break;
                            
                        case 4:
                            t = read(tmp1);
                            
                            break;
                            
                        case 5:
                            pc = read((tmp1&0xFF00)|((tmp1+1)&0xFF))<<8;
                            pc |= t;
                            
                            break;
                    }
                    
                    break;
                
                // Unofficial opcodes
                // Implied addressing
                    
                case 0x1A:
                case 0x3A:
                case 0x5A:
                case 0x7A:
                case 0xDA:
                case 0xFA:
                    // NOP
                    imp(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return 0; // Do nothing
                        }
                    });
                    
                    break;
                
                // Immediate addressing
                    
                case 0x80:
                case 0x82:
                case 0x89:
                case 0xC2:
                case 0xE2:
                    // NOP
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return 0; // Do nothing
                        }
                    });
                    
                    break;
                    
                case 0xAB:
                    // LAX
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            x = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x0B:
                case 0x2B:
                    // ANC
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= t;
                            
                            updateNZ(a);
                            
                            p &= ~C_FLAG;
                            p |= (p>>7)&1;
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x4B:
                    // ALR
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= t;
                            
                            updateNZ(a);
                            a = lsr(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x6B:
                    // ARR
                    
                    // FIXME: The overflow flag doesn't get set correctly
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a &= t;
                            
                            int tmp = a;
                            a = ror(a);
                            
                            p &= ~(1<<7);
                            p |= a&(1<<7);
                            
                            if(a != 0){
                                p &= ~Z_FLAG;
                            }else{
                                p |= Z_FLAG;
                            }
                            
                            p &= ~V_FLAG;
                            p |= (tmp^(a<<1))&(1<<6);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x8B:
                    // XAA
                    
                    // XXX: Maybe I should make it a bit unreliable
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = x;
                            a &= t;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0xCB:
                    // AXS
                    
                    imm(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            cmp(a&x, t);
                            x = (a&x)-t;
                            
                            updateNZ(x);
                            
                            return 0;
                        }
                    });
                    
                    break;
                
                // Absolute addressing
                    
                case 0x0C:
                case 0x3C:
                    // NOP
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return 0; // Do nothing
                        }
                    });
                    
                    break;
                    
                case 0xAF:
                    // LAX
                    
                    absRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            // XXX: Should LAX perform one or two reads?
                            
                            a = value;
                            x = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x0F:
                    // SLO
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = asl(value);
                            
                            a |= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x2F:
                    // RLA
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = rol(value);
                            
                            a &= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x4F:
                    // SRE
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = lsr(value);
                            
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x6F:
                    // RRA
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = ror(value);
                            adc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xCF:
                    // DCP
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            cmp(a, value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xEF:
                    // ISC
                    
                    absRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            sbc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x8F:
                    // SAX
                    
                    // NOTE: Apparently it is unstable on the NES
                    
                    absStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a&x;
                        }
                    });
                    
                    break;
                
                // Indexed absolute addressing
                // Indexed with X
                    
                case 0x1C:
                case 0x5C:
                case 0x7C:
                case 0xDC:
                case 0xFC:
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return value; // Do nothing
                        }
                    });
                    
                    break;
                    
                case 0x1F:
                    // SLO
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = asl(value);
                            
                            a |= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x3F:
                    // RLA
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = rol(value);
                            
                            a &= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x5F:
                    // SRE
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = lsr(value);
                            
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x7F:
                    // RRA
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = ror(value);
                            
                            adc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xDF:
                    // DCP
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            cmp(a, value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xFF:
                    // ISC
                    
                    absIRMW(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            sbc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x9C:
                    // SHY
                    
                    absISH(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            int tmp = y;
                            if(!skipAnd) tmp &= ((tmp1>>8)+1);
                            
                            return tmp;
                        }
                    });
                    
                    break;
                
                // Indexed with Y
                    
                case 0xBF:
                    // LAX
                    
                    absIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            x = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x1B:
                    // SLO
                    
                    absIRMW(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = asl(value);
                            
                            a |= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x3B:
                    // RLA
                    
                    absIRMW(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = rol(value);
                            
                            a &= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x5B:
                    // SRE
                    
                    absIRMW(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = lsr(value);
                            
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x7B:
                    // RRA
                    
                    absIRMW(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = ror(value);
                            adc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xDB:
                    // DCP
                    
                    absIRMW(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            cmp(a, value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xFB:
                    // ISC
                    
                    absIRMW(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            sbc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x9B:
                    // TAS
                    
                    absISH(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            s = a&x;
                            
                            int tmp = s;
                            if(!skipAnd) tmp &= ((tmp1>>8)+1);
                            
                            return tmp;
                        }
                    });
                    
                    break;
                    
                case 0x9F:
                    // AHX
                    
                    absISH(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            int tmp = a&x;
                            if(!skipAnd) tmp &= ((tmp1>>8)+1);
                            
                            return tmp;
                        }
                    });
                    
                    break;
                    
                case 0x9E:
                    // SHX
                    
                    absISH(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            int tmp = x;
                            if(!skipAnd) tmp &= ((tmp1>>8)+1);
                            
                            return tmp;
                        }
                    });
                    
                    break;
                    
                case 0xBB:
                    // LAS
                    
                    absISH(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value&s;
                            x = a;
                            s = a;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                
                // Zeropage addressing
                    
                case 0x04:
                case 0x44:
                case 0x64:
                    // NOP
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return 0; // Do nothing
                        }
                    });
                    
                    break;
                    
                case 0xA7:
                    // LAX
                    
                    zpRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            x = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x07:
                    // SLO
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = asl(value);
                            a |= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x27:
                    // RLA
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = rol(value);
                            a &= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x47:
                    // SRE
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = lsr(value);
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x67:
                    // RRA
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = ror(value);
                            adc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xC7:
                    // DCP
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            cmp(a, value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xE7:
                    // ISC
                    
                    zpRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            sbc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x87:
                    // SAX
                    
                    // NOTE: Apparently it is unstable on the NES
                    
                    zpStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a&x;
                        }
                    });
                    
                    break;
                
                // Indexed zeropage addressing
                // Indexed with X
                    
                case 0x14:
                case 0x34:
                case 0x54:
                case 0x74:
                case 0xD4:
                case 0xF4:
                    // NOP
                    
                    zpIRead(x, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return 0; // Do nothing
                        }
                    });
                    
                    break;
                    
                case 0x17:
                    // SLO
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = asl(value);
                            
                            a |= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x37:
                    // RLA
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = rol(value);
                            
                            a &= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x57:
                    // SRE
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = lsr(value);
                            
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x77:
                    // RRA
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = ror(value);
                            adc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xD7:
                    // DCP
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            cmp(a, value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xF7:
                    // ISC
                    
                    zpIRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            sbc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                
                // Indexed with Y
                    
                case 0xB7:
                    // LAX
                    
                    zpIRead(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            x = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x97:
                    // SAX
                    
                    // NOTE: Apparently it is unstable on the NES
                    
                    zpIStore(y, new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a&x;
                        }
                    });
                    
                    break;
                
                // Indexed indirect addressing
                    
                case 0xA3:
                    // LAX
                    
                    idxIndRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            x = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x03:
                    // SLO
                    
                    idxIndRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = asl(value);
                            
                            a |= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x23:
                    // RLA
                    
                    idxIndRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = rol(value);
                            
                            a &= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x43:
                    // SRE
                    
                    idxIndRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = lsr(value);
                            
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x63:
                    // RRA
                    
                    idxIndRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = ror(value);
                            adc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xC3:
                    // DCP
                    
                    idxIndRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            cmp(a, value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                    
                case 0xE3:
                    // ISC
                    
                    idxIndRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            sbc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x83:
                    // SAX
                    
                    // NOTE: Apparently it is unstable on the NES
                    
                    idxIndStore(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            return a&x;
                        }
                    });
                    
                    break;
                
                // Indirect indexed addressing
                    
                case 0xB3:
                    // LAX
                    
                    indIdxRead(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            a = value;
                            x = value;
                            
                            updateNZ(a);
                            
                            return 0;
                        }
                    });
                    
                    break;
                    
                case 0x13:
                    // SLO
                    
                    indIdxRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = asl(value);
                            a |= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x33:
                    // RLA
                    
                    indIdxRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = rol(value);
                            a &= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x53:
                    // SRE
                    
                    indIdxRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = lsr(value);
                            a ^= value;
                            
                            updateNZ(a);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x73:
                    // RRA
                    
                    indIdxRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value = ror(value);
                            adc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xD3:
                    // DCP
                    
                    indIdxRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value--;
                            value &= 0xFF;
                            
                            cmp(a, value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0xF3:
                    // ISC
                    
                    indIdxRMW(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            value++;
                            value &= 0xFF;
                            
                            sbc(value);
                            
                            return value;
                        }
                    });
                    
                    break;
                    
                case 0x93:
                    // AHX
                    
                    indIdxSH(new Operation() {
                        @Override
                        public int operation(CPU cpu, int value) {
                            int tmp = a&x;
                            
                            if(!skipAnd) tmp &= ((tmp1>>8)+1);
                            
                            return tmp;
                        }
                    });
                    
                    break;
                
                // STP
                    
                case 0x02:
                case 0x12:
                case 0x22:
                case 0x32:
                case 0x42:
                case 0x52:
                case 0x62:
                case 0x72:
                case 0x92:
                case 0xB2:
                case 0xD2:
                case 0xF2:
                    // STP
                    
                    // XXX: Am I emulating it accurately?
                    
                    jammed = true;
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
