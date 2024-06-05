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

import java.awt.BorderLayout;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;


/**
 *
 * @author mibi88
 */
public class Window extends JFrame {
    private final String TITLE = "MibiNESTools";
    private final int MAX_SCALE = 24;
    
    private Menubar menubar;
    
    private JTabbedPane tabs;
    
    private CHREditor chrEditor;
    private NametableEditor nametableEditor;
    private LevelEditor levelEditor;
    
    private int scale;
    
    private File currentFile;
    
    /**
     * Initialize the GUI
     */
    public Window() {
        scale = 8;
        initWindow(640, 480);
    }
    
    private void initWindow(int width, int height) {
        setTitle(TITLE);
        setSize(width, height);
        
        setVisible(true);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        menubar = new Menubar(this);
        setJMenuBar(menubar);
        
        tabs = new JTabbedPane();
        
        chrEditor = new CHREditor(this);
        nametableEditor = new NametableEditor();
        levelEditor = new LevelEditor();
        
        tabs.addTab("CHR Editor", chrEditor);
        tabs.addTab("Nametable Editor", nametableEditor);
        tabs.addTab("Level Editor", levelEditor);
        
        add(tabs, BorderLayout.CENTER);
    }
    
    public void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter chrFilter =
                new FileNameExtensionFilter("CHR Files", "chr");
        fileChooser.addChoosableFileFilter(chrFilter);
        int out = fileChooser.showOpenDialog(this);
        if(out == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try {
                chrEditor.loadFile(file);
                currentFile = file;
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void saveFile() {
        if(currentFile != null){
            try {
                chrEditor.saveFile(currentFile);
                return;
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        saveAsFile();
    }
    
    public void newFile() {
        currentFile = null;
        chrEditor.newFile();
    }
    
    public void saveAsFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter chrFilter =
                new FileNameExtensionFilter("CHR Files", "chr");
        fileChooser.addChoosableFileFilter(chrFilter);
        int out = fileChooser.showSaveDialog(this);
        if(out == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if(file.exists()){
                JOptionPane askOverwrite = new JOptionPane();
                int selected = askOverwrite.showConfirmDialog(this,
                        "The file " + file.getName() + " already exists!\n"
                                + "Overwrite?", "Existing file",
                        JOptionPane.YES_NO_OPTION);
                if(selected != JOptionPane.OK_OPTION){
                    return;
                }
            }
            currentFile = file;
            saveFile();
        }
    }
    
    public void setGrid(boolean grid) {
        chrEditor.setGrid(grid);
    }
    
    public boolean getGrid() {
        return menubar.getViewMenu().getGrid();
    }
    
    public void zoomIn() {
        if(scale < MAX_SCALE){
            scale++;
            chrEditor.setScale(scale);
        }
    }
    
    public void zoomOut() {
        if(scale > 1){
            scale--;
            chrEditor.setScale(scale);
        }
    }
    
    public int getScale() {
        return scale;
    }
    
    /**
     * Close the window
     */
    public void quit() {
        dispose();
    }
}
