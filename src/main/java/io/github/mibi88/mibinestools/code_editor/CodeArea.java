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

package io.github.mibi88.mibinestools.code_editor;

import java.awt.Font;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.undo.UndoManager;

/**
 *
 * @author mibi88
 */
public class CodeArea extends JTextPane {
    private int fontSize;
    private StyleContext styleContext;
    private Style opcode;
    private Style comment;
    private Style label;
    private Style pseudoFunctions;
    private Style number;
    private UndoManager undoManager;

    /**
     * Create a new CodeArea.
     * @param fontSize The size of the font.
     */
    public CodeArea(int fontSize) {
        super();
        this.fontSize = fontSize;
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        undoManager = new UndoManager();
        styleContext = new StyleContext();
        opcode = styleContext.addStyle("opcode", null);
        StyleConstants.setBold(opcode, true);
        comment = styleContext.addStyle("comment", null);
        StyleConstants.setItalic(comment, true);
        label = styleContext.addStyle("label", null);
        StyleConstants.setUnderline(label, true);
        pseudoFunctions = styleContext.addStyle("pseudoFunctions",
                null);
        StyleConstants.setItalic(pseudoFunctions, true);
        number = styleContext.addStyle("number", null);
        StyleConstants.setItalic(number, true);
        Runnable highlightRunnable = new Runnable() {
            @Override
            public void run() {
                highlight();
            }
        };
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(highlightRunnable);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(highlightRunnable);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                return;
            }
        });
        getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                System.out.println(e.getEdit().getPresentationName());
                undoManager.addEdit(e.getEdit());
            }
        });
    }
    
    /**
     * Reset the CodeArea.
     */
    public void reset() {
        setText("");
        highlight();
        undoManager.die();
    }
    
    /**
     * Highlight the text in the CodeArea.
     */
    public void highlight() {
        Style defaultStyle = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);
        getStyledDocument().setCharacterAttributes(0,
                getStyledDocument().getLength(), defaultStyle,
                true);
        String text = getText();
        highlightPattern(text, "^\\s*[a-z]{3}[$|\\s|;]", opcode);
        highlightPattern(text, "\\..*$", pseudoFunctions);
        highlightPattern(text, "^.*:", label);
        highlightPattern(text, ";.*$", comment);
        highlightPattern(text, "#?[$|%]?\\d*[\\s|$]", number);
    }
    
    private void highlightPattern(String text, String regex, Style style) {
        int start = 0;
        Pattern pattern = Pattern.compile(regex,
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = pattern.matcher(text);
        while(m.find(start)){
            getStyledDocument().setCharacterAttributes(m.start(),
                    m.end()-m.start(), style, true);
            start = m.end();
        }
    }
    
    /**
     * Undo the last action.
     */
    public void undo() {
        if(undoManager.canUndo()){
            undoManager.undo();
        }
    }
    
    /**
     * Redo the last action.
     */
    public void redo() {
        if(undoManager.canRedo()){
            undoManager.redo();
        }
    }
}
