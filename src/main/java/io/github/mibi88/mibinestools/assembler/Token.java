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
public class Token {
    private TokenType type;
    private String content;
    
    /**
     * Create a new token.
     * @param type The type of the token.
     * @param content The value of the token.
     */
    public Token(TokenType type, String content) {
        this.type = type;
        this.content = content;
    }
    
    /**
     * Get the type of the token.
     * @return The type of the token.
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Set the type of the token.
     * @param type The type of the token.
     */
    public void setType(TokenType type) {
        this.type = type;
    }

    /**
     * Get the value of the token.
     * @return The value of the token.
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the value of the token.
     * @param content The value of the token.
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Add a char to the token.
     * @param c The char to add to the token.
     */
    public void add(char c) {
        content += c;
    }
}
