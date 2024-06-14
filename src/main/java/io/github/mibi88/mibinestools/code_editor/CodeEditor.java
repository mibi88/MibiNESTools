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

import io.github.mibi88.mibinestools.Editor;
import io.github.mibi88.mibinestools.Window;
import io.github.mibi88.mibinestools.nametable_editor.NametableEditor;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;

/**
 *
 * @author mibi88
 */
public class CodeEditor extends Editor {
    private CodeArea codeArea;
    private JScrollPane codeAreaPane;
    public CodeEditor(Window window) {
        super("Code Editor", new GridLayout(1, 1));
        codeArea = new CodeArea(12, this);
        codeAreaPane = new JScrollPane(codeArea);
        add(codeAreaPane);
    }
    
    /**
     * Create a new text file.
     * @return Returns true if the file was created.
     */
    @Override
    public boolean newFile() {
        if(super.newFile()){
            codeArea.reset();
            return true;
        }
        return false;
    }
    
    /**
     * Open a file.
     * @param file The file to load the text from.
     * @return Returns true if the file was opened.
     */
    @Override
    public boolean openFile(File file) {
        if(super.openFile(file)){
            try {
                FileInputStream fileStream = new FileInputStream(file);
                byte[] data = new byte[fileStream.available()];
                fileStream.read(data);
                codeArea.reset();
                codeArea.setText(new String(data));
                codeArea.highlight();
            } catch (IOException ex) {
                Logger.getLogger(NametableEditor.class.getName()).log(
                        Level.SEVERE, null, ex);
                error();
            }
        }
        return true;
    }
    
    /**
     * Save the text.
     */
    @Override
    public void saveFile() {
        File file = getFile();
        if(file != null){
            try {
                PrintWriter printWriter = new PrintWriter(file);
                printWriter.write(codeArea.getText());
                printWriter.close();
                super.saveFile();
            } catch (IOException ex) {
                Logger.getLogger(NametableEditor.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Save the text to a specific file.
     * @param file The file to save the text to.
     */
    @Override
    public void saveAsFile(File file) {
        try {
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.write(codeArea.getText());
            printWriter.close();
            super.saveAsFile(file);
        } catch (IOException ex) {
            Logger.getLogger(NametableEditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Undo the last action.
     */
    @Override
    public void undo() {
        codeArea.undo();
    }
    
    /**
     * Redo the last action.
     */
    @Override
    public void redo() {
        codeArea.redo();
    }
    
    /**
     * Get the file extension of the files that can be opened with this editor.
     * @return The file extension.
     */
    public static String getExtension() {
        return "asm";
    }
}
