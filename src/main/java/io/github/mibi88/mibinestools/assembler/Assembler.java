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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 *
 * @author mibi88
 */
public class Assembler {
    private Parser parser;

    /**
     * Assemble code.
     * @param file The file to load the code from.
     * @throws Exception Thrown when the file reading failed.
     */
    public Assembler(File file) throws Exception {
        FileInputStream fileStream = new FileInputStream(file);
        byte[] data = new byte[fileStream.available()];
        fileStream.read(data);
        parser = new Parser(new String(data));
        ArrayList<Token> tokens = parser.getTokens();
        for(Token token : tokens){
            System.out.println(token);
        }
    }

    /**
     * Assemble code.
     * @param text The code to assemble.
     */
    public Assembler(String text) {
        parser = new Parser(text);
    }
}
