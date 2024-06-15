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
    private static String editorName = "Unknown Editor";
    private File file;
    private boolean fileSaved;
    
    /**
     * Create a new editor.
     * @param window The window to use with this editor.
     */
    public Editor(Window window) {
        super();
        fileSaved = true;
    }
    
    /**
     * Create an editor with a specific LayoutManager.
     * @param window The window to use with this editor.
     * @param lm The LayoutManager to use.
     */
    public Editor(Window window, LayoutManager lm) {
        super(lm);
        fileSaved = true;
    }
    
    /**
     * Set the name of the editor.
     * @param editorName The new name of the editor.
     */
    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }
    
    /**
     * Get the name of the editor
     * @return The name of the editor.
     */
    public static String getEditorName() {
        return editorName;
    }
    
    /**
     * Call it when the editor encounters an error
     */
    protected void error() {
        file = null;
        fileSaved = false;
    }
    
    /**
     * Get the name of the file.
     * @return The name of the file, or "Unsaved file" if the file isn't saved.
     */
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
    
    /**
     * Open a file
     * @param file The file to open
     * @return Returns true if the file should be opened
     */
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
    
    /**
     * Save the current file
     */
    public void saveFile() {
        fileSaved = true;
        updateTitle();
    }
    
    /**
     * Save the file as
     * @param file The file to save the data to.
     */
    public void saveAsFile(File file) {
        this.file = file;
        fileSaved = true;
        updateTitle();
    }
    
    /**
     * Sets if a grid should be displayed ot not in tilemaps etc.
     * @param grid True if the grid should be displayed
     */
    public void setGrid(boolean grid) {
        return;
    }
    
    /**
     * Sets the scale of the editor content.
     * @param scale The scale of the content
     */
    public void setScale(int scale) {
        return;
    }
    
    /**
     * Sets the palette to use to display graphics.
     * @param palette The palette to use
     */
    public void setPalette(int[][] palette) {
        return;
    }
    
    /**
     * Sets the number of the palette to use.
     * @param i The number of the palette
     */
    public void setPalette(int i) {
        return;
    }
    
    /**
     * Call it when the file was edited.
     */
    public void fileEdited() {
        fileSaved = false;
        updateTitle();
    }
    
    /**
     * Check if a file is opened
     * @return Returns editor is not editing a file.
     */
    public boolean isEditingFile() {
        return file != null;
    }
    
    /**
     * Check if the file the editor is editing is saved.
     * @return Returns true if the file is saved.
     */
    public boolean getFileSaved() {
        return fileSaved;
    }
    
    /**
     * Get the file the editor is editing.
     * @return Returns the file the editor is currently editing, or null if no
     * file is opened.
     */
    protected File getFile() {
        return file;
    }
    
    /**
     * Check if the editor is currently used by the player
     * @return Returns true if the user has selected this editor tab.
     */
    public boolean isSelected() {
        JTabbedPane tabbedPane = getTabbedPane();
        if(SwingUtilities.isDescendingFrom(this,
                tabbedPane.getSelectedComponent())){
            return true;
        }
        return false;
    }
    
    /**
     * Update the title of the editor tab.
     */
    public void updateTitle() {
        String title = fileSaved ? "" : "*";
        if(file != null){
            title += file.getName() + " - ";
        }
        title += getEditorName();
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
    
    /**
     * Gets called when the editor should undo the last action.
     */
    public void undo() {
        return;
    }
    
    /**
     * Gets called when the editor should redo the last action.
     */
    public void redo() {
        return;
    }
    
    /**
     * Gets called if the user wants to copy something.
     */
    public void copy() {
        return;
    }
    
    /**
     * Gets called if the user wants to cut something.
     */
    public void cut() {
        return;
    }
    
    /**
     * Gets called if the user wants to paste something.
     */
    public void paste() {
        return;
    }
    
    /**
     * Get the file extension of the files that this editor can open.
     * @return The file extension of the files that this editor can open.
     */
    public static String[] getExtension() {
        return null;
    }
}
