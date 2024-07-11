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
            s += 0xFF;
        }
    }
    
    public byte pull() {
        byte value = rom.read(0x0100+s);
        s++;
        s %= 0x100;
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
    
    public void cycle() {
        // TODO: Interrupt hijacking.
        if(cycle == 0){
            if(nmi){
                push(getStatus());
                push((byte)(pc>>8));
                push((byte)pc);
                this.pc = rom.nmi();
                nmi = false;
                irq = false;
            }
            if(irq){
                push(getStatus());
                push((byte)(pc>>8));
                push((byte)pc);
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
            value = 0;
            switch(size){
                case 2:
                    value = rom.read(pc+1);
                    if(addressingMode != AddressingMode.RELATIVE){
                        value &= 0xFF;
                    }
                    break;
                case 3:
                    value = rom.read(pc+1)&0xFF;
                    value |= (rom.read(pc+2)&0xFF)<<8;
                    break;
                default:
                    break;
            }
            System.out.printf("%s pc=%02X: %s\n",
                    addressingMode, pc, disassemble());
            pc += size;
        }
        if(cycle >= wait){
            runOpcode(opcode, value);
            cycle = 0;
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
    
    public void runOpcode(int opcode, int value) {
        // Handle indirect addressing
        switch(addressingMode){
            case INDEXED_INDIRECT:
                value += x&0xFF;
                int low = rom.read(value);
                int high = rom.read((value+1)%0x100);
                value = low|(high<<8);
                break;
            case INDIRECT_INDEXED:
                low = rom.read(value);
                high = rom.read((value+1)%0x100);
                value = low|(high<<8);
                value += y&0xFF;
                value %= 0x10000;
                break;
            case ABSOLUTE_INDIRECT:
                high = rom.read(value);
                if(value%0x100 == 0xFF){
                    low = rom.read(value&0xFF00);
                }else{
                    low = rom.read(value+1);
                }
                value = low|(high<<8);
            default:
                char register = Instructions.registers.charAt(opcode);
                switch(register){
                    case 'X':
                        value += x&0xFF;
                        break;
                    case 'Y':
                        value += y&0xFF;
                        break;
                }
        }
        int num = value;
        if(addressingMode != AddressingMode.IMMEDIATE){
            num = rom.read(value);
        }else if(addressingMode == AddressingMode.ACCUMULATOR){
            num = a;
        }
        
        switch(Instructions.names[opcode]) {
            case "BRK":
                pc++;
                bFlag = true;
                nmi();
                break;
            case "ORA":
                a = a|num;
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case "STP":
                break;
            case "SLO":
                break;
            case "NOP":
                break;
            case "ASL":
                // ASL Zero page addressing
                if(addressingMode == AddressingMode.ACCUMULATOR){
                    carry = (a&0b10000000) != 0;
                    a = a<<1;
                    zero = a == 0;
                    negative = (a&0b10000000) != 0;
                }else{
                    byte out = (byte)(num<<1);
                    carry = (num&0b10000000) != 0;
                    rom.write(value, (byte)(num<<1));
                    zero = a == 0;
                    negative = (out&0b10000000) != 0;
                }
                break;
            case "PHP":
                bFlag = true;
                push(getStatus());
                break;
            case "ANC":
                // TODO
                break;
            case "BPL":
                // TODO: Add a cycle if branch succeeds.
                if(!negative){
                    pc += value;
                }
                break;
            case "CLC":
                // CLC
                carry = false;
                break;
            case "JSR":
                push((byte)(pc>>8));
                push((byte)pc);
                pc = value;
                break;
            case "AND":
                a = a&rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case "RLA":
                // TODO
                break;
            case "BIT":
                byte in = rom.read(value);
                byte out = (byte)(in&a);
                zero = out == 0;
                overflow = (in&0b01000000) != 0;
                negative = (in&0b10000000) != 0;
                break;
            case "ROL":
                // TODO
                break;
            case "PLP":
                // TODO
                break;
            case "BMI":
                // TODO
                break;
            case "SEC":
                // TODO
                break;
            case "RTI":
                // TODO
                break;
            case "EOR":
                // TODO
                break;
            case "SRE":
                // TODO
                break;
            case "LSR":
                // TODO
                break;
            case "ALR":
                // TODO
                break;
            case "JMP":
                // TODO
                break;
            case "BVC":
                // TODO
                break;
            case "CLI":
                // TODO
                break;
            case "RTS":
                // TODO
                break;
            case "ADC":
                // TODO
                break;
            case "RRA":
                // TODO
                break;
            case "ROR":
                // TODO
                break;
            case "PLA":
                // TODO
                break;
            case "ARR":
                // TODO
                break;
            case "BVS":
                // TODO
                break;
            case "SEI":
                // TODO
                break;
            case "STA":
                // TODO
                break;
            case "SAX":
                // TODO
                break;
            case "STY":
                // TODO
                break;
            case "STX":
                // TODO
                break;
            case "DEY":
                // TODO
                break;
            case "TXA":
                // TODO
                break;
            case "XAA":
                // TODO
                break;
            case "BCC":
                // TODO
                break;
            case "AHX":
                // TODO
                break;
            case "TYA":
                // TODO
                break;
            case "TXS":
                // TODO
                break;
            case "TAS":
                // TODO
                break;
            case "SHY":
                // TODO
                break;
            case "SHX":
                // TODO
                break;
            case "LDY":
                // TODO
                break;
            case "LDA":
                // TODO
                break;
            case "LDX":
                // TODO
                break;
            case "LAX":
                // TODO
                break;
            case "TAY":
                // TODO
                break;
            case "TAX":
                // TODO
                break;
            case "BCS":
                // TODO
                break;
            case "CLV":
                // TODO
                break;
            case "TSX":
                // TODO
                break;
            case "LAS":
                // TODO
                break;
            case "CPY":
                // TODO
                break;
            case "CMP":
                // TODO
                break;
            case "DCP":
                // TODO
                break;
            case "INY":
                // TODO
                break;
            case "DEX":
                // TODO
                break;
            case "AXS":
                // TODO
                break;
            case "DEC":
                // TODO
                break;
            case "BNE":
                // TODO
                break;
            case "CLD":
                // TODO
                break;
            case "CPX":
                // TODO
                break;
            case "SBC":
                // TODO
                break;
            case "ISC":
                // TODO
                break;
            case "INC":
                // TODO
                break;
            case "INX":
                // TODO
                break;
            case "BEQ":
                // TODO
                break;
            case "SED":
                // TODO
                break;
        }
    }
}
