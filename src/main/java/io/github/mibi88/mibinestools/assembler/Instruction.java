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

package io.github.mibi88.mibinestools.assembler;

/**
 *
 * @author mibi88
 */
public class Instruction {
    private String opcodeName;
    private String arg;
    private String register;
    private AddressingMode mode;

    /**
     * Store an instruction.
     * @param opcodeName The name of the opcode.
     * @param arg The value following the opcode (if there is one).
     * @param register The register used for indexed addressing (can be null).
     */
    public Instruction(String opcodeName, String arg, String register) {
        this.opcodeName = opcodeName;
        this.arg = arg;
        this.register = register;
        setMode();
    }
    
    private void setMode() {
        mode = null;
    }
}
