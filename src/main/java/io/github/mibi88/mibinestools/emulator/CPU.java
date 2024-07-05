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
        // I really love jump tables... But I can't do them because JAVA!
        switch(opcode) {
            // 0x00 to 0x0F
            case 0x00:
                // BRK
                pc++;
                bFlag = true;
                nmi();
                break;
            case 0x01:
                // ORA (Indirect, X)
                a = a|rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x02:
                // TODO
                break;
            case 0x03:
                // TODO
                break;
            case 0x04:
                // TODO
                break;
            case 0x05:
                // ORA Zero page addressing
                a = a|rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x06:
                // ASL Zero page addressing
                byte in = rom.read(value);
                byte out = (byte)(in<<1);
                carry = (in&0b10000000) != 0;
                rom.write(value, (byte)(in<<1));
                zero = a == 0;
                negative = (out&0b10000000) != 0;
                break;
            case 0x07:
                // TODO
                break;
            case 0x08:
                // PHP
                bFlag = true;
                push(getStatus());
                break;
            case 0x09:
                // ORA Immediate addressing
                a = a|value;
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x0A:
                // ASL
                carry = (a&0b10000000) != 0;
                a = a<<1;
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x0B:
                // TODO
                break;
            case 0x0C:
                // TODO
                break;
            case 0x0D:
                // ORA Absolute addressing
                a = a|rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x0E:
                // ASL Absolute addressing
                in = rom.read(value);
                out = (byte)(in<<1);
                carry = (in&0b10000000) != 0;
                rom.write(value, (byte)(in<<1));
                zero = a == 0;
                negative = (out&0b10000000) != 0;
                break;
            case 0x0F:
                // TODO
                break;
            // 0x10 to 0x1F
            case 0x10:
                // BPL
                // TODO: Add a cycle if branch succeeds.
                if(!negative){
                    pc += value;
                }
                break;
            case 0x11:
                // ORA Indirect indexed addressing
                a = a|rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x12:
                // TODO
                break;
            case 0x13:
                // TODO
                break;
            case 0x14:
                // TODO
                break;
            case 0x15:
                // ORA Indexed zero page addressing
                a = a|rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x16:
                // ASL Indexed zero page addressing
                in = rom.read(value);
                out = (byte)(in<<1);
                carry = (in&0b10000000) != 0;
                rom.write(value, (byte)(in<<1));
                zero = a == 0;
                negative = (out&0b10000000) != 0;
                break;
            case 0x17:
                // TODO
                break;
            case 0x18:
                // CLC
                carry = false;
                break;
            case 0x19:
                // ORA Absolute indexed addressing
                a = a|rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x1A:
                // TODO
                break;
            case 0x1B:
                // TODO
                break;
            case 0x1C:
                // TODO
                break;
            case 0x1D:
                // ORA Absolute indexed addressing
                a = a|rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x1E:
                // ASL Absolute indexed addressing
                in = rom.read(value);
                out = (byte)(in<<1);
                carry = (in&0b10000000) != 0;
                rom.write(value, (byte)(in<<1));
                zero = a == 0;
                negative = (out&0b10000000) != 0;
                break;
            case 0x1F:
                // TODO
                break;
            // 0x20 to 0x2F
            case 0x20:
                // JSR Absolute addressing.
                push((byte)(pc>>8));
                push((byte)pc);
                pc = value;
                break;
            case 0x21:
                // AND Indexed indirect addressing
                a = a&rom.read(value);
                zero = a == 0;
                negative = (a&0b10000000) != 0;
                break;
            case 0x22:
                // TODO
                break;
            case 0x23:
                // TODO
                break;
            case 0x24:
                // BIT Zero page addressing
                in = rom.read(value);
                out = (byte)(in&a);
                zero = out == 0;
                overflow = (in&0b01000000) != 0;
                negative = (in&0b10000000) != 0;
                break;
            case 0x25:
                // TODO
                break;
            case 0x26:
                // TODO
                break;
            case 0x27:
                // TODO
                break;
            case 0x28:
                // TODO
                break;
            case 0x29:
                // TODO
                break;
            case 0x2A:
                // TODO
                break;
            case 0x2B:
                // TODO
                break;
            case 0x2C:
                // TODO
                break;
            case 0x2D:
                // TODO
                break;
            case 0x2E:
                // TODO
                break;
            case 0x2F:
                // TODO
                break;
            // 0x30 to 0x3F
            case 0x30:
                // TODO
                break;
            case 0x31:
                // TODO
                break;
            case 0x32:
                // TODO
                break;
            case 0x33:
                // TODO
                break;
            case 0x34:
                // TODO
                break;
            case 0x35:
                // TODO
                break;
            case 0x36:
                // TODO
                break;
            case 0x37:
                // TODO
                break;
            case 0x38:
                // TODO
                break;
            case 0x39:
                // TODO
                break;
            case 0x3A:
                // TODO
                break;
            case 0x3B:
                // TODO
                break;
            case 0x3C:
                // TODO
                break;
            case 0x3D:
                // TODO
                break;
            case 0x3E:
                // TODO
                break;
            case 0x3F:
                // TODO
                break;
            // 0x40 to 0x4F
            case 0x40:
                // TODO
                break;
            case 0x41:
                // TODO
                break;
            case 0x42:
                // TODO
                break;
            case 0x43:
                // TODO
                break;
            case 0x44:
                // TODO
                break;
            case 0x45:
                // TODO
                break;
            case 0x46:
                // TODO
                break;
            case 0x47:
                // TODO
                break;
            case 0x48:
                // TODO
                break;
            case 0x49:
                // TODO
                break;
            case 0x4A:
                // TODO
                break;
            case 0x4B:
                // TODO
                break;
            case 0x4C:
                // TODO
                break;
            case 0x4D:
                // TODO
                break;
            case 0x4E:
                // TODO
                break;
            case 0x4F:
                // TODO
                break;
            // 0x50 to 0x5F
            case 0x50:
                // TODO
                break;
            case 0x51:
                // TODO
                break;
            case 0x52:
                // TODO
                break;
            case 0x53:
                // TODO
                break;
            case 0x54:
                // TODO
                break;
            case 0x55:
                // TODO
                break;
            case 0x56:
                // TODO
                break;
            case 0x57:
                // TODO
                break;
            case 0x58:
                // TODO
                break;
            case 0x59:
                // TODO
                break;
            case 0x5A:
                // TODO
                break;
            case 0x5B:
                // TODO
                break;
            case 0x5C:
                // TODO
                break;
            case 0x5D:
                // TODO
                break;
            case 0x5E:
                // TODO
                break;
            case 0x5F:
                // TODO
                break;
            // 0x60 to 0x6F
            case 0x60:
                // TODO
                break;
            case 0x61:
                // TODO
                break;
            case 0x62:
                // TODO
                break;
            case 0x63:
                // TODO
                break;
            case 0x64:
                // TODO
                break;
            case 0x65:
                // TODO
                break;
            case 0x66:
                // TODO
                break;
            case 0x67:
                // TODO
                break;
            case 0x68:
                // TODO
                break;
            case 0x69:
                // TODO
                break;
            case 0x6A:
                // TODO
                break;
            case 0x6B:
                // TODO
                break;
            case 0x6C:
                // TODO
                break;
            case 0x6D:
                // TODO
                break;
            case 0x6E:
                // TODO
                break;
            case 0x6F:
                // TODO
                break;
            // 0x70 to 0x7F
            case 0x70:
                // TODO
                break;
            case 0x71:
                // TODO
                break;
            case 0x72:
                // TODO
                break;
            case 0x73:
                // TODO
                break;
            case 0x74:
                // TODO
                break;
            case 0x75:
                // TODO
                break;
            case 0x76:
                // TODO
                break;
            case 0x77:
                // TODO
                break;
            case 0x78:
                // TODO
                break;
            case 0x79:
                // TODO
                break;
            case 0x7A:
                // TODO
                break;
            case 0x7B:
                // TODO
                break;
            case 0x7C:
                // TODO
                break;
            case 0x7D:
                // TODO
                break;
            case 0x7E:
                // TODO
                break;
            case 0x7F:
                // TODO
                break;
            // 0x80 to 0x8F
            case 0x80:
                // TODO
                break;
            case 0x81:
                // TODO
                break;
            case 0x82:
                // TODO
                break;
            case 0x83:
                // TODO
                break;
            case 0x84:
                // TODO
                break;
            case 0x85:
                // TODO
                break;
            case 0x86:
                // TODO
                break;
            case 0x87:
                // TODO
                break;
            case 0x88:
                // TODO
                break;
            case 0x89:
                // TODO
                break;
            case 0x8A:
                // TODO
                break;
            case 0x8B:
                // TODO
                break;
            case 0x8C:
                // TODO
                break;
            case 0x8D:
                // TODO
                break;
            case 0x8E:
                // TODO
                break;
            case 0x8F:
                // TODO
                break;
            // 0x90 to 0x9F
            case 0x90:
                // TODO
                break;
            case 0x91:
                // TODO
                break;
            case 0x92:
                // TODO
                break;
            case 0x93:
                // TODO
                break;
            case 0x94:
                // TODO
                break;
            case 0x95:
                // TODO
                break;
            case 0x96:
                // TODO
                break;
            case 0x97:
                // TODO
                break;
            case 0x98:
                // TODO
                break;
            case 0x99:
                // TODO
                break;
            case 0x9A:
                // TODO
                break;
            case 0x9B:
                // TODO
                break;
            case 0x9C:
                // TODO
                break;
            case 0x9D:
                // TODO
                break;
            case 0x9E:
                // TODO
                break;
            case 0x9F:
                // TODO
                break;
            // 0xA0 to 0xAF
            case 0xA0:
                // TODO
                break;
            case 0xA1:
                // TODO
                break;
            case 0xA2:
                // TODO
                break;
            case 0xA3:
                // TODO
                break;
            case 0xA4:
                // TODO
                break;
            case 0xA5:
                // TODO
                break;
            case 0xA6:
                // TODO
                break;
            case 0xA7:
                // TODO
                break;
            case 0xA8:
                // TODO
                break;
            case 0xA9:
                // TODO
                break;
            case 0xAA:
                // TODO
                break;
            case 0xAB:
                // TODO
                break;
            case 0xAC:
                // TODO
                break;
            case 0xAD:
                // TODO
                break;
            case 0xAE:
                // TODO
                break;
            case 0xAF:
                // TODO
                break;
            // 0xB0 to 0xBF
            case 0xB0:
                // TODO
                break;
            case 0xB1:
                // TODO
                break;
            case 0xB2:
                // TODO
                break;
            case 0xB3:
                // TODO
                break;
            case 0xB4:
                // TODO
                break;
            case 0xB5:
                // TODO
                break;
            case 0xB6:
                // TODO
                break;
            case 0xB7:
                // TODO
                break;
            case 0xB8:
                // TODO
                break;
            case 0xB9:
                // TODO
                break;
            case 0xBA:
                // TODO
                break;
            case 0xBB:
                // TODO
                break;
            case 0xBC:
                // TODO
                break;
            case 0xBD:
                // TODO
                break;
            case 0xBE:
                // TODO
                break;
            case 0xBF:
                // TODO
                break;
            // 0x10 to 0x1F
            case 0xC0:
                // TODO
                break;
            case 0xC1:
                // TODO
                break;
            case 0xC2:
                // TODO
                break;
            case 0xC3:
                // TODO
                break;
            case 0xC4:
                // TODO
                break;
            case 0xC5:
                // TODO
                break;
            case 0xC6:
                // TODO
                break;
            case 0xC7:
                // TODO
                break;
            case 0xC8:
                // TODO
                break;
            case 0xC9:
                // TODO
                break;
            case 0xCA:
                // TODO
                break;
            case 0xCB:
                // TODO
                break;
            case 0xCC:
                // TODO
                break;
            case 0xCD:
                // TODO
                break;
            case 0xCE:
                // TODO
                break;
            case 0xCF:
                // TODO
                break;
            // 0xD0 to 0xDF
            case 0xD0:
                // TODO
                break;
            case 0xD1:
                // TODO
                break;
            case 0xD2:
                // TODO
                break;
            case 0xD3:
                // TODO
                break;
            case 0xD4:
                // TODO
                break;
            case 0xD5:
                // TODO
                break;
            case 0xD6:
                // TODO
                break;
            case 0xD7:
                // TODO
                break;
            case 0xD8:
                // TODO
                break;
            case 0xD9:
                // TODO
                break;
            case 0xDA:
                // TODO
                break;
            case 0xDB:
                // TODO
                break;
            case 0xDC:
                // TODO
                break;
            case 0xDD:
                // TODO
                break;
            case 0xDE:
                // TODO
                break;
            case 0xDF:
                // TODO
                break;
            // 0xE0 to 0xEF
            case 0xE0:
                // TODO
                break;
            case 0xE1:
                // TODO
                break;
            case 0xE2:
                // TODO
                break;
            case 0xE3:
                // TODO
                break;
            case 0xE4:
                // TODO
                break;
            case 0xE5:
                // TODO
                break;
            case 0xE6:
                // TODO
                break;
            case 0xE7:
                // TODO
                break;
            case 0xE8:
                // TODO
                break;
            case 0xE9:
                // TODO
                break;
            case 0xEA:
                // TODO
                break;
            case 0xEB:
                // TODO
                break;
            case 0xEC:
                // TODO
                break;
            case 0xED:
                // TODO
                break;
            case 0xEE:
                // TODO
                break;
            case 0xEF:
                // TODO
                break;
            // 0xF0 to 0xFF
            case 0xF0:
                // TODO
                break;
            case 0xF1:
                // TODO
                break;
            case 0xF2:
                // TODO
                break;
            case 0xF3:
                // TODO
                break;
            case 0xF4:
                // TODO
                break;
            case 0xF5:
                // TODO
                break;
            case 0xF6:
                // TODO
                break;
            case 0xF7:
                // TODO
                break;
            case 0xF8:
                // TODO
                break;
            case 0xF9:
                // TODO
                break;
            case 0xFA:
                // TODO
                break;
            case 0xFB:
                // TODO
                break;
            case 0xFC:
                // TODO
                break;
            case 0xFD:
                // TODO
                break;
            case 0xFE:
                // TODO
                break;
            case 0xFF:
                // TODO
                break;
        }
    }
}
