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

import io.github.mibi88.mibinestools.cpu.AddressingMode;
import io.github.mibi88.mibinestools.cpu.Instructions;
import java.util.Arrays;

/**
 *
 * @author mibi88
 */
public class CPU {
    private Rom rom;
    private int cycle;
    private int wait;
    private int value;
    private int size;
    AddressingMode addressingMode;
    private int opcode;
    private int pc;
    private int a, x, y;
    private int s;
    private boolean nmi;
    private boolean irq;
    private boolean start;
    private int address;
    private int operand;
    // P register
    private boolean carry;
    private boolean zero;
    private boolean interruptDisable;
    private boolean decimal;
    private boolean bFlag;
    private boolean overflow;
    private boolean negative;
    
    public CPU(Rom rom) {
        this.rom = rom;
        this.s = 0x00;
        reset();
        start = true;
    }
    
    public void nmi() {
        nmi = true;
    }
    
    public void irq() {
        if(!interruptDisable){
            irq = true;
        }
    }
    
    public void reset() {
        s -= 3;
        if(s < 0){
            s += 0xFF;
        }
        this.pc = rom.reset();
    }
    
    public void push(byte value) {
        rom.write(0x0100+s, value);
        s--;
        if(s < 0){
            s += 0x100;
        }
        //System.out.println("PUSH: "+Instructions.names[opcode]);
    }
    
    public byte pull() {
        s++;
        s %= 0x100;
        byte value = rom.read(0x0100+s);
        //System.out.println("PULL: "+Instructions.names[opcode]);
        return value;
    }
    
    public byte getStatus() {
        byte status = 0x00;
        status |= carry ? 0b00000001 : 0x00;
        status |= zero ? 0b00000010 : 0x00;
        status |= interruptDisable ? 0b00000100 : 0x00;
        status |= decimal ? 0b00001000 : 0x00;
        status |= bFlag ? 0b00010000 : 0x00;
        status |= 0b00100000;
        status |= overflow ? 0b01000000 : 0x00;
        status |= negative ? 0b10000000 : 0x00;
        return status;
    }
    
    public void setStatus(byte status) {
        carry = (status&0b00000001) != 0;
        zero = (status&0b00000010) != 0;
        interruptDisable = (status&0b00000100) != 0;
        decimal = (status&0b00001000) != 0;
        bFlag = (status&0b00010000) != 0;
        overflow = (status&0b01000000) != 0;
        negative = (status&0b10000000) != 0;
    }
    
    private void getOpcode() {
        // Fetch the opcode.
        if(nmi){
            push((byte)(pc>>8));
            push((byte)pc);
            push(getStatus());
            this.pc = rom.nmi();
            nmi = false;
            irq = false;
        }
        if(irq){
            push((byte)(pc>>8));
            push((byte)pc);
            push(getStatus());
            this.pc = rom.irq();
            irq = false;
        }
        opcode = rom.read(pc)&0xFF;
        wait = Instructions.cycles[opcode];
        // TODO: Extra cycle when crossing pages.
        addressingMode = Instructions.addressingModes[opcode];
        int index = Arrays.asList(Instructions.addressingModeList)
                .indexOf(addressingMode);
        size = Instructions.size[index];
    }
    
    public void cycle() {
        if(start){
            getOpcode();
            start = false;
            return;
        }
        // TODO: Interrupt hijacking.
        // Run the current opcode
        switch(opcode) {
            case 0x00:
                switch(cycle){
                    case 0:
                        pc++;
                        rom.read(pc);
                        break;
                    case 1:
                        pc++;
                        rom.write(s|0x100, (byte)(pc>>8));
                        break;
                    case 2:
                        rom.write((s-1)&0xFF|0x100, (byte)pc);
                        break;
                    case 4:
                }
                break;
            default:
                System.out.println("Unknown instruction: "
                        +Instructions.names[opcode]);
        }
        // Continue
        if(cycle >= wait){
            getOpcode();
            cycle = 0;
        }else{
            cycle++;
        }
    }
    
    public String disassemble() {
        String name = Instructions.names[opcode];
        switch(addressingMode){
            case ACCUMULATOR:
            case IMPLIED:
                return name;
            case IMMEDIATE:
                return String.format("%s #$%02X", name,
                        value);
            case ZERO_PAGE:
            case INDEXED_ZERO_PAGE:
                String out = String.format("%s $%02X", name,
                        value);
                char register = Instructions.registers
                        .charAt(opcode);
                if(register != ' '){
                    out += ", " + register;
                }
                return out;
            case ABSOLUTE:
            case INDEXED_ABSOLUTE:
                out = String.format("%s $%04X", name,
                        value);
                register = Instructions.registers
                        .charAt(opcode);
                if(register != ' '){
                    out += ", " + register;
                }
                return out;
            case RELATIVE:
                return String.format("%s %d", name,
                        value);
            case INDEXED_INDIRECT:
                return String.format("%s (%02X, X)",
                        name, value);
            case INDIRECT_INDEXED:
                return String.format("%s (%02X), Y",
                        name, value);
            case ABSOLUTE_INDIRECT:
                return String.format("%s ($%04X)", name,
                            value);
        }
        return name;
    }
}
