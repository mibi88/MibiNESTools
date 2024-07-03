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

package io.github.mibi88.mibinestools.cpu;

/**
 *
 * @author mibi88
 */
public class Instructions {
    public final static String[] names = {
        // 0x00 to 0x1F
        "BRK",
        "ORA",
        "STP",
        "SLO",
        "NOP",
        "ORA",
        "ASL",
        "SLO",
        "PHP",
        "ORA",
        "ASL",
        "ANC",
        "NOP",
        "ORA",
        "ASL",
        "SLO",
        "BPL",
        "ORA",
        "STP",
        "SLO",
        "NOP",
        "ORA",
        "ASL",
        "SLO",
        "CLC",
        "ORA",
        "NOP",
        "SLO",
        "NOP",
        "ORA",
        "ASL",
        "SLO",
        // 0x20 to 0x3F
        "JSR",
        "AND",
        "STP",
        "RLA",
        "BIT",
        "AND",
        "ROL",
        "RLA",
        "PLP",
        "AND",
        "ROL",
        "ANC",
        "BIT",
        "AND",
        "ROL",
        "RLA",
        "BMI",
        "AND",
        "STP",
        "RLA",
        "NOP",
        "AND",
        "ROL",
        "RLA",
        "SEC",
        "AND",
        "NOP",
        "RLA",
        "NOP",
        "AND",
        "ROL",
        "RLA",
        // 0x40 to 0x5F
        "RTI",
        "EOR",
        "STP",
        "SRE",
        "NOP",
        "EOR",
        "LSR",
        "SRE",
        "PHA",
        "EOR",
        "LSR",
        "ALR",
        "JMP",
        "EOR",
        "LSR",
        "SRE",
        "BVC",
        "EOR",
        "STP",
        "SRE",
        "NOP",
        "EOR",
        "LSR",
        "SRE",
        "CLI",
        "EOR",
        "NOP",
        "SRE",
        "NOP",
        "EOR",
        "LSR",
        "SRE",
        // 0x60 to 0x7F
        "RTS",
        "ADC",
        "STP",
        "RRA",
        "NOP",
        "ADC",
        "ROR",
        "RRA",
        "PLA",
        "ADC",
        "ROR",
        "ARR",
        "JMP",
        "ADC",
        "ROR",
        "RRA",
        "BVS",
        "ADC",
        "STP",
        "RRA",
        "NOP",
        "ADC",
        "ROR",
        "RRA",
        "SEI",
        "ADC",
        "NOP",
        "RRA",
        "NOP",
        "ADC",
        "ROR",
        "RRA",
        // 0x80 to 9F
        "NOP",
        "STA",
        "NOP",
        "SAX",
        "STY",
        "STA",
        "STX",
        "SAX",
        "DEY",
        "NOP",
        "TXA",
        "XAA",
        "STY",
        "STA",
        "STX",
        "SAX",
        "BCC",
        "STA",
        "STP",
        "AHX",
        "STY",
        "STA",
        "STX",
        "SAX",
        "TYA",
        "STA",
        "TXS",
        "TAS",
        "SHY",
        "STA",
        "SHX",
        "AHX",
        // 0xA0 to 0xBF
        "LDY",
        "LDA",
        "LDX",
        "LAX",
        "LDY",
        "LDA",
        "LDX",
        "LAX",
        "TAY",
        "LDA",
        "TAX",
        "LAX",
        "LDY",
        "LDA",
        "LDX",
        "LAX",
        "BCS",
        "LDA",
        "STP",
        "LAX",
        "LDY",
        "LDA",
        "LDX",
        "LAX",
        "CLV",
        "LDA",
        "TSX",
        "LAS",
        "LDY",
        "LDA",
        "LDX",
        "LAX",
        // C0 to DF
        "CPY",
        "CMP",
        "NOP",
        "DCP",
        "CPY",
        "CMP",
        "DEC",
        "DCP",
        "INY",
        "CMP",
        "DEX",
        "AXS",
        "CPY",
        "CMP",
        "DEC",
        "DCP",
        "BNE",
        "CMP",
        "STP",
        "DCP",
        "NOP",
        "CMP",
        "DEC",
        "DCP",
        "CLD",
        "CMP",
        "NOP",
        "DCP",
        "NOP",
        "CMP",
        "DEC",
        "DCP",
        // E0 to FF
        "CPX",
        "SBC",
        "NOP",
        "ISC",
        "CPX",
        "SBC",
        "INC",
        "ISC",
        "INX",
        "SBC",
        "NOP",
        "SBC",
        "CPX",
        "SBC",
        "INC",
        "ISC",
        "BEQ",
        "SBC",
        "STP",
        "ISC",
        "NOP",
        "SBC",
        "INC",
        "ISC",
        "SED",
        "SBC",
        "NOP",
        "ISC",
        "NOP",
        "SBC",
        "INC",
        "ISC"
    };
    
    public final static int[] cycles = {
        7, 6, 0, 8, 3, 3, 5, 5, 3, 2, 2, 2, 4, 4, 6, 6, // 0x00 to 0x0F
        2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7, // 0x10 to 0x1F
        6, 6, 0, 8, 3, 3, 5, 5, 4, 2, 2, 2, 4, 4, 6, 6, // 0x20 to 0x2F
        2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7, // 0X30 to 0x3F
        6, 6, 0, 8, 3, 3, 5, 5, 3, 2, 2, 2, 3, 4, 6, 6, // 0x40 to 0x4F
        2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7, // 0x50 to 0x5F
        6, 6, 0, 8, 3, 3, 5, 5, 4, 2, 2, 2, 5, 4, 6, 6, // 0x60 to 0x6F
        2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7, // 0x70 to 0x7F
        2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4, // 0x80 to 0x8F
        2, 6, 0, 6, 4, 4, 4, 4, 2, 5, 2, 5, 5, 5, 5, 5, // 0x90 to 0x9F
        2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4, // 0xA0 to 0xAF
        2, 5, 0, 5, 4, 4, 4, 4, 2, 4, 2, 4, 4, 4, 4, 4, // 0xB0 to 0xBF
        2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6, // 0xC0 to 0xCF
        2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7, // 0xD0 to 0xDF
        2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6, // 0xE0 to 0xEF
        2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7  // 0xF0 to 0xFF
    };
    
    public final static boolean[] addACycleOnPageCrossing = {
        false, false, false, false, false, false, false, false, // 0x00 to 0x07
        false, false, false, false, false, false, false, false, // 0x08 to 0x0F
        true,  true,  false, false, false, false, false, false, // 0x10 to 0x17
        false, true,  false, false, true,  true,  false, false, // 0x18 to 0x1F
        false, false, false, false, false, false, false, false, // 0x20 to 0x27
        false, false, false, false, false, false, false, false, // 0x28 to 0x2F
        true,  true,  false, false, false, false, false, false, // 0x30 to 0x37
        false, true,  false, false, true,  true,  false, false, // 0x38 to 0x3F
        false, false, false, false, false, false, false, false, // 0x40 to 0x47
        false, false, false, false, false, false, false, false, // 0x48 to 0x4F
        true,  true,  false, false, false, false, false, false, // 0x50 to 0x57
        false, true,  false, false, true,  true,  false, false, // 0x58 to 0x5F
        false, false, false, false, false, false, false, false, // 0x60 to 0x67
        false, false, false, false, false, false, false, false, // 0x68 to 0x6F
        true,  true,  false, false, false, false, false, false, // 0x70 to 0x77
        false, true,  false, false, true,  true,  false, false, // 0x78 to 0x7F
        false, false, false, false, false, false, false, false, // 0x80 to 0x87
        false, false, false, false, false, false, false, false, // 0x88 to 0x8F
        true,  false, false, false, false, false, false, false, // 0x90 to 0x97
        false, false, false, false, false, false, false, false, // 0x98 to 0x9F
        false, false, false, false, false, false, false, false, // 0xA0 to 0xA7
        false, false, false, false, false, false, false, false, // 0xA8 to 0xAF
        true,  true,  false, true,  false, false, false, false, // 0xB0 to 0xB7
        false, true,  false, true,  true,  true,  true,  true,  // 0xB8 to 0xBF
        false, false, false, false, false, false, false, false, // 0xC0 to 0xC7
        false, false, false, false, false, false, false, false, // 0xC8 to 0xCF
        true,  true,  false, false, false, false, false, false, // 0xD0 to 0xD7
        false, true,  false, false, true,  true,  false, false, // 0xD8 to 0xDF
        false, false, false, false, false, false, false, false, // 0xE0 to 0xE7
        false, false, false, false, false, false, false, false, // 0xE8 to 0xEF
        true,  true,  false, false, false, false, false, false, // 0xF0 to 0xF7
        false, true,  false, false, true,  true,  false, false, // 0xF8 to 0xFF
    };
    
    public final static AddressingMode[] addressingModes = {
        // 0x00 to 0x0F
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.IMMEDIATE,
        AddressingMode.ACCUMULATOR,
        AddressingMode.IMMEDIATE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        // 0x10 to 0x1F
        AddressingMode.RELATIVE,
        AddressingMode.INDIRECT_INDEXED,
        AddressingMode.IMPLIED,
        AddressingMode.INDIRECT_INDEXED,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        // 0x20 to 0x2F
        AddressingMode.ABSOLUTE,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.IMMEDIATE,
        AddressingMode.IMPLIED,
        AddressingMode.IMMEDIATE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        // 0x30 to 0x3F
        AddressingMode.RELATIVE,
        AddressingMode.INDIRECT_INDEXED,
        AddressingMode.IMPLIED,
        AddressingMode.INDIRECT_INDEXED,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        // 0x40 to 0x4F
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.IMMEDIATE,
        AddressingMode.IMPLIED,
        AddressingMode.IMMEDIATE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        // 0x50 to 0x5F
        AddressingMode.RELATIVE,
        AddressingMode.INDIRECT_INDEXED,
        AddressingMode.IMPLIED,
        AddressingMode.INDIRECT_INDEXED,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        // 0x60 to 0x6F
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.IMMEDIATE,
        AddressingMode.ACCUMULATOR,
        AddressingMode.IMMEDIATE,
        AddressingMode.ABSOLUTE_INDIRECT,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        // 0x70 to 0x7F
        AddressingMode.RELATIVE,
        AddressingMode.INDIRECT_INDEXED,
        AddressingMode.IMPLIED,
        AddressingMode.INDIRECT_INDEXED,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.INDEXED_ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.IMPLIED,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        AddressingMode.INDEXED_ABSOLUTE,
        // 0x80 to 0x8F
        AddressingMode.IMMEDIATE,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.IMMEDIATE,
        AddressingMode.INDEXED_INDIRECT,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.ZERO_PAGE,
        AddressingMode.IMPLIED,
        AddressingMode.IMMEDIATE,
        AddressingMode.IMPLIED,
        AddressingMode.IMMEDIATE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
        AddressingMode.ABSOLUTE,
    };
}
