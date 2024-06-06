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
package io.github.mibi88.mibinestools;

import java.awt.LayoutManager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author mibi88
 */
public abstract class Editor extends JPanel {
    private final String editorName;
    private File file;
    private boolean fileSaved;
    
    public Editor(String editorName) {
        super();
        fileSaved = true;
        this.editorName = editorName;
    }
    
    public Editor(String editorName, LayoutManager lm) {
        super(lm);
        fileSaved = true;
        this.editorName = editorName;
    }
    
    
    public String getEditorName() {
        return editorName;
    }
    
    protected void error() {
        file = null;
        fileSaved = false;
    }
    
    public String getFileName() {
        return file == null ? "Unsaved file" : file.getName();
    }
    
    /**
     * Create a new file.
     * @return Returns true if a new file should be created. Else it returns
     * false
     */
    public boolean newFile() {
        String fileName = getFileName();
        if(!fileSaved){
            JOptionPane askForNewFile = new JOptionPane();
            int selected = askForNewFile.showConfirmDialog(this,
                    fileName + " is not saved!\n"
                            + "Do you really want to create a new file?",
                    "Unsaved changes",
                    JOptionPane.YES_NO_OPTION);
            if(selected != JOptionPane.OK_OPTION
                        || askForNewFile.getValue() == null){
                return false;
            }
        }
        file = null;
        fileSaved = true;
        updateTitle();
        return true;
    }
    
    public boolean openFile(File file) {
        String fileName = getFileName();
        if(!fileSaved){
            JOptionPane askForOpenFile = new JOptionPane();
            int selected = askForOpenFile.showConfirmDialog(this,
                    fileName + " is not saved!\n"
                            + "Do you really want to open this file?",
                    "Unsaved changes",
                    JOptionPane.YES_NO_OPTION);
            if(selected != JOptionPane.OK_OPTION
                        || askForOpenFile.getValue() == null){
                return false;
            }
        }
        this.file = file;
        fileSaved = true;
        updateTitle();
        return true;
    }
    
    public void saveFile() {
        fileSaved = true;
        updateTitle();
    }
    
    public void saveAsFile(File file) {
        this.file = file;
        fileSaved = true;
        updateTitle();
    }
    
    public void setGrid(boolean grid) {
        return;
    }
    
    public void setScale(int scale) {
        return;
    }
    
    public void setPalette(int[][] palette) {
        return;
    }
    
    public void fileEdited() {
        fileSaved = false;
        updateTitle();
    }
    
    public boolean isEditingFile() {
        return file != null;
    }
    
    public boolean getFileSaved() {
        return fileSaved;
    }
    
    protected File getFile() {
        return file;
    }
    
    public boolean isSelected() {
        JTabbedPane tabbedPane = getTabbedPane();
        if(SwingUtilities.isDescendingFrom(this,
                tabbedPane.getSelectedComponent())){
            return true;
        }
        return false;
    }
    
    public void updateTitle() {
        String title = fileSaved ? "" : "*";
        if(file != null){
            title += file.getName() + " - ";
        }
        title += editorName;
        try {
            setTitle(title);
        } catch (Exception ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
    }
    
    private JTabbedPane getTabbedPane() {
        return (JTabbedPane)SwingUtilities
                .getAncestorOfClass(JTabbedPane.class, this);
    }
    
    private int getTabIndex() throws Exception {
        JTabbedPane tabbedPane = getTabbedPane();
        for(int i=0;i<tabbedPane.getTabCount();i++){
            if(SwingUtilities.isDescendingFrom(this,
                    tabbedPane.getComponentAt(i))){
                return i;
            }
        }
        throw new Exception("Tab not found!");
    }
    
    private void setTitle(String title) throws Exception {
        int i = getTabIndex();
        JTabbedPane tabbedPane = getTabbedPane();
        tabbedPane.setTitleAt(i, title);
    }
}
