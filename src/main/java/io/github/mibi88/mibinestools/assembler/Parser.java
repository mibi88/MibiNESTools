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
    public Parser(String text) throws ParserError {
        currentToken = new Token(null, "");
        tokenList = new ArrayList();
        String tokenEnds = ", \t\r\n";
        String lineEnds = "\r\n";
        escaped = false;
        inComment = false;
        inString = false;
        char old_c = '\0';
        for(int i=0;i<text.length();i++){
            char c = text.charAt(i);
            if(!inComment){
                if(inString){
                    if(c == '"' && old_c != '\\'){
                        inString = false;
                        continue;
                    }
                    old_c = c;
                    currentToken.add(c);
                }else{
                    if(c == '\\'){
                        escaped = true;
                        continue;
                    }
                    if(!escaped && c == ';'){
                        inComment = true;
                        continue;
                    }
                    if(c == '"' && !escaped){
                        inString = true;
                        currentToken.setType(TokenType.STRING);
                        continue;
                    }
                    if(tokenEnds.indexOf(c) < 0){
                        currentToken.add(c);
                    }else{
                        if(currentToken.getContent().length() > 0){
                            addToken(currentToken);
                            currentToken = new Token(null, "");
                        }
                    }
                }
            }else{
                if(lineEnds.indexOf(c) >= 0){
                    inComment = false;
                }
            }
            escaped = false;
        }
        if(currentToken.getContent().length() > 0){
            addToken(currentToken);
        }
    }
    
    private void addToken(Token token) throws ParserError {
        String content = currentToken.getContent();
        String addressStart = "0123456789$%";
        if(token.getType() == null){
            if(content.charAt(0) == '.'){
                token.setType(TokenType.DIRECTIVE);
            }else if(content.charAt(0) == '#'){
                token.setType(TokenType.NUMBER);
            }else if(addressStart.indexOf(content.charAt(0)) >= 0){
                token.setType(TokenType.ADDRESS);
            }else if(content.charAt(0) == '(' &&
                    content.charAt(content.length()-1) == ')'){
                token.setType(TokenType.INDIRECT_ADDRESSING);
            }else if(content.charAt(content.length()-1) == ':'){
                token.setType(TokenType.LABEL);
            }else if(content.matches("[a-zA-Z]{3}")){
                token.setType(TokenType.OPCODE);
            }else if(content.matches("[A|X|Y]")){
                token.setType(TokenType.REGISTER);
            }else if(content.equals("=")){
                token.setType(TokenType.SET);
            }else{
                token.setType(TokenType.NAME);
            }
        }else if(token.getType() == TokenType.STRING){
            token.setContent(parseString(content));
        }
        tokenList.add(token);
    }
    
    /**
     * Get the tokens.
     * @return Returns the ArrayList of tokens.
     */
    public ArrayList<Token> getTokens() {
        return tokenList;
    }
    
    private String parseString(String string) {
        boolean backslash = false;
        String out = "";
        for(int i=0;i<string.length();i++){
            char c = string.charAt(i);
            if(c == '\\'){
                backslash = true;
                continue;
            }
            if(backslash){
                backslash = false;
                switch(c){
                    case 'a':
                        out += 0x07;
                        break;
                    case 'b':
                        out += '\b';
                        break;
                    case 'f':
                        out += '\f';
                        break;
                    case 'n':
                        out += '\n';
                        break;
                    case 'r':
                        out += '\r';
                        break;
                    case 't':
                        out += '\t';
                        break;
                    case 'v':
                        out += 0x0B;
                        break;
                    case '0':
                        out += '\0';
                        break;
                    case 'x':
                        if(i+3<string.length()){
                            String hex = "";
                            hex += string.charAt(i+1);
                            hex += string.charAt(i+2);
                            out += (char)Integer.parseInt(hex, 16);
                            i += 2;
                        }
                        break;
                    default:
                        out += c;
                        
                }
            }else{
                out += c;
            }
        }
        return out;
    }
}
