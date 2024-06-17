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

import java.util.ArrayList;

/**
 *
 * @author mibi88
 */
public class Parser {
    private boolean inString;
    private boolean inComment;
    private boolean escaped;
    private Token currentToken;
    private ArrayList<Token> tokenList;

    /**
     * Parse assembly code.
     * @param text The code to parse.
     */
    public Parser(String text) {
        currentToken = new Token(null, "");
        tokenList = new ArrayList();
        String tokenEnds = " \t\r\n";
        escaped = false;
        for(int i=0;i<text.length();i++){
            char c = text.charAt(i);
            if(!inComment){
                if(c == '\\'){
                    escaped = true;
                    continue;
                }
                if(!inString){
                    if(tokenEnds.indexOf(c) >= 0){
                        currentToken.add(c);
                    }else{
                        if(currentToken.getContent().length() > 0){
                            addToken(currentToken);
                            currentToken = new Token(null, "");
                        }
                    }
                }
            }
            escaped = false;
        }
    }
    
    private void addToken(Token token) {
        String content = currentToken.getContent();
        String addressStart = "0123456789$%";
        if(content.charAt(0) == '.'){
            token.setType(TokenType.DIRECTIVE);
        }else if(content.charAt(0) == '#'){
            token.setType(TokenType.NUMBER);
        }else if(addressStart.indexOf(content.charAt(0)) >= 0){
            token.setType(TokenType.ADDRESS);
        }else if(content.charAt(content.length()-1) == ':'){
            token.setType(TokenType.LABEL);
        }else if(content.matches("[a-zA-Z]{3}")){
            token.setType(TokenType.OPCODE);
        }
        tokenList.add(token);
    }
}
