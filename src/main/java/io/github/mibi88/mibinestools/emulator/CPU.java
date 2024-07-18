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
    
    public void cycle() {
        // TODO: Interrupt hijacking.
        if(cycle == 0){
            if(nmi){
                //System.out.printf("NMI: PC=%04X\n", pc);
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
            String name = Instructions.names[opcode];
            /*if(name.equals("ISC")){
                System.out.printf("%s pc=%02X: %s\n",
                        addressingMode, pc, disassemble());
            }*/
            
            pc += size;
        }
        if(cycle >= wait){
            runOpcode(opcode, value);
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
    
    private int getNum() {
        int num = value;
        if(addressingMode != AddressingMode.IMMEDIATE){
            num = rom.read(value);
        }else if(addressingMode == AddressingMode.ACCUMULATOR){
            num = a;
        }
        num &= 0xFF;
        return num;
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
        switch(Instructions.names[opcode]) {
            case "BRK":
                pc++;
                bFlag = true;
                nmi();
                break;
            case "ORA":
                int num = getNum();
                a = a|num;
                zero = (a&0xFF) == 0;
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
                    zero = (a&0xFF) == 0;
                    negative = (a&0b10000000) != 0;
                }else{
                    num = getNum();
                    byte out = (byte)(num<<1);
                    carry = (num&0b10000000) != 0;
                    rom.write(value, (byte)(num<<1));
                    zero = (a&0xFF) == 0;
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
                num = getNum();
                a = a&num;
                zero = (a&0xFF) == 0;
                negative = (a&0b10000000) != 0;
                break;
            case "RLA":
                // TODO
                break;
            case "BIT":
                num = getNum();
                zero = (num&a) == 0;
                overflow = (num&0b01000000) != 0;
                negative = (num&0b10000000) != 0;
                break;
            case "ROL":
                num = getNum();
                carry = (num&0b10000000) != 0;
                byte out = (byte)(num<<1);
                out |= (num&0b10000000)>>7;
                zero = (a&0xFF) == 0;
                negative = (out&0b10000000) != 0;
                if(addressingMode == AddressingMode.ACCUMULATOR){
                    a = out;
                }else{
                    rom.write(value, out);
                }
                break;
            case "PLP":
                setStatus(pull());
                break;
            case "BMI":
                // TODO: Add a cycle if branch succeeds.
                if(negative){
                    pc += value;
                }
                break;
            case "SEC":
                carry = true;
                break;
            case "RTI":
                setStatus(pull());
                pc = pull()&0xFF;
                pc |= (pull()&0xFF)<<8;
                System.out.printf("RTI: %04X\n", pc);
                break;
            case "EOR":
                num = getNum();
                a ^= num;
                zero = (a&0xFF) == 0;
                negative = (a&0b10000000) != 0;
                break;
            case "SRE":
                // TODO
                break;
            case "LSR":
                if(addressingMode == AddressingMode.ACCUMULATOR){
                    carry = (a&0b00000001) != 0;
                    a >>= 1;
                    zero = (a&0xFF) == 0;
                    negative = (a&0b10000000) != 0;
                }else{
                    num = getNum();
                    carry = (num&0b00000001) != 0;
                    num >>= 1;
                    zero = (num&0xFF) == 0;
                    negative = (num&0b10000000) != 0;
                    rom.write(value, (byte)num);
                }
                break;
            case "ALR":
                // TODO
                break;
            case "JMP":
                pc = value;
                break;
            case "BVC":
                // TODO: Add a cycle if branch succeeds.
                if(!overflow){
                    pc += value;
                }
                break;
            case "CLI":
                interruptDisable = false;
                break;
            case "RTS":
                pc = pull()&0xFF;
                pc |= (pull()&0xFF)<<8;
                break;
            case "ADC":
                num = getNum();
                int add = carry ? 1 : 0;
                out = (byte)(a+num+add);
                carry = a+num+add > 0xFF;
                overflow = (out&0b10000000) != (a&0b10000000);
                a = out;
                zero = (a&0xFF) == 0;
                negative = (num&0b10000000) != 0;
                break;
            case "RRA":
                // TODO
                break;
            case "ROR":
                num = getNum();
                carry = (num&0b00000001) != 0;
                out = (byte)(num>>1);
                out |= (num&0b00000001)<<7;
                zero = (a&0xFF) == 0;
                negative = (out&0b10000000) != 0;
                if(addressingMode == AddressingMode.ACCUMULATOR){
                    a = out;
                }else{
                    rom.write(value, out);
                }
                break;
            case "PLA":
                a = pull();
                zero = (a&0xFF) == 0;
                negative = (a&0b10000000) != 0;
                break;
            case "ARR":
                // TODO
                break;
            case "BVS":
                // TODO: Add a cycle if branch succeeds.
                if(overflow){
                    pc += value;
                }
                break;
            case "SEI":
                interruptDisable = true;
                break;
            case "STA":
                rom.write(value, (byte)a);
                break;
            case "SAX":
                // TODO
                break;
            case "STY":
                rom.write(value, (byte)y);
                break;
            case "STX":
                rom.write(value, (byte)x);
                break;
            case "DEY":
                y--;
                if(y < 0){
                    y += 0x100;
                }
                zero = (y&0xFF) == 0;
                negative = (y&0b10000000) != 0;
                break;
            case "TXA":
                a = x;
                zero = (a&0xFF) == 0;
                negative = (a&0b10000000) != 0;
                break;
            case "XAA":
                // TODO
                break;
            case "BCC":
                // TODO: Add a cycle if branch succeeds.
                if(!carry){
                    pc += value;
                }
                break;
            case "AHX":
                // TODO
                break;
            case "TYA":
                a = y;
                zero = (a&0xFF) == 0;
                negative = (a&0b10000000) != 0;
                break;
            case "TXS":
                s = a&0xFF;
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
                num = getNum();
                y = num;
                zero = (y&0xFF) == 0;
                negative = (y&0b10000000) != 0;
                break;
            case "LDA":
                num = getNum();
                a = num;
                zero = (a&0xFF) == 0;
                negative = (a&0b10000000) != 0;
                break;
            case "LDX":
                num = getNum();
                x = num;
                zero = (x&0xFF) == 0;
                negative = (x&0b10000000) != 0;
                break;
            case "LAX":
                // TODO
                break;
            case "TAY":
                y = a;
                zero = (y&0xFF) == 0;
                negative = (y&0b10000000) != 0;
                break;
            case "TAX":
                x = a;
                zero = (x&0xFF) == 0;
                negative = (x&0b10000000) != 0;
                break;
            case "BCS":
                // TODO: Add a cycle if branch succeeds.
                if(carry){
                    pc += value;
                }
                break;
            case "CLV":
                overflow = false;
                break;
            case "TSX":
                x = s;
                zero = (x&0xFF) == 0;
                negative = (x&0b10000000) != 0;
                break;
            case "LAS":
                // TODO
                break;
            case "CPY":
                num = getNum();
                carry = y >= num;
                zero = (y&0xFF) == num;
                negative = ((y-num)&0b10000000) != 0;
                break;
            case "CMP":
                num = getNum();
                carry = a >= num;
                zero = (a&0xFF) == num;
                negative = ((a-num)&0b10000000) != 0;
                break;
            case "DCP":
                // TODO
                break;
            case "INY":
                y++;
                y %= 0x100;
                zero = (y&0xFF) == 0;
                negative = (y&0b10000000) != 0;
                break;
            case "DEX":
                x--;
                if(x < 0){
                    x += 0x100;
                }
                zero = (x&0xFF) == 0;
                negative = (x&0b10000000) != 0;
                break;
            case "AXS":
                // TODO
                break;
            case "DEC":
                num = getNum();
                num--;
                if(num < 0){
                    num += 0x100;
                }
                zero = (num&0xFF) == 0;
                negative = (num&0b10000000) != 0;
                rom.write(value, (byte)num);
                break;
            case "BNE":
                // TODO: Add a cycle if branch succeeds.
                if(!zero){
                    pc += value;
                }
                break;
            case "CLD":
                decimal = false;
                break;
            case "CPX":
                num = getNum();
                carry = x >= num;
                zero = (x&0xFF) == num;
                negative = ((x-num)&0b10000000) != 0;
                break;
            case "SBC":
                num = getNum();
                add = carry ? 0 : 1;
                out = (byte)(a-num-add);
                carry = a-num-add <= 0xFF;
                overflow = (out&0b10000000) != (a&0b10000000);
                a = out;
                zero = (a&0xFF) == 0;
                negative = (num&0b10000000) != 0;
                break;
            case "ISC":
                // TODO
                break;
            case "INC":
                num = getNum();
                num++;
                num %= 0x100;
                zero = (num&0xFF) == 0;
                negative = (num&0b10000000) != 0;
                rom.write(value, (byte)num);
                break;
            case "INX":
                x++;
                x %= 0x100;
                zero = (x&0xFF) == 0;
                negative = (x&0b10000000) != 0;
                break;
            case "BEQ":
                // TODO: Add a cycle if branch succeeds.
                if(zero){
                    pc += value;
                }
                break;
            case "SED":
                decimal = true;
                break;
            case "PHA":
                push((byte)a);
                break;
        }
    }
}
