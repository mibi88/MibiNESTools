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
import java.util.ArrayList;
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
    
    private ArrayList<Editor> editors;
    
    private int scale;
    
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
        
        editors = new ArrayList<Editor>();
        
        editors.add(new CHREditor(this));
        editors.add(new NametableEditor());
        editors.add(new LevelEditor());
        
        addEditors();
        
        add(tabs, BorderLayout.CENTER);
    }
    
    private void addEditors() {
        for(Editor editor : editors){
            tabs.addTab(editor.getEditorName(), editor);
        }
        for(Editor editor : editors){
            editor.updateTitle();
        }
    }
    
    private int getSelectedEditor() throws Exception {
        for(int i=0;i<editors.size();i++){
            if(editors.get(i).isSelected()){
                return i;
            }
        }
        throw new Exception("Failed to get selected editor!");
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
                editors.get(getSelectedEditor()).openFile(file);
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void saveFile() {
        try {
            if(editors.get(getSelectedEditor()).isEditingFile()){
                try {
                    editors.get(getSelectedEditor()).saveFile();
                    return;
                } catch (Exception ex) {
                    Logger.getLogger(Window.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
            saveAsFile();
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    public void newFile() {
        try {
            editors.get(getSelectedEditor()).newFile();
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
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
            try {
                editors.get(getSelectedEditor()).saveAsFile(file);
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    public void setGrid(boolean grid) {
        try {
            editors.get(getSelectedEditor()).setGrid(grid);
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    public boolean getGrid() {
        return menubar.getViewMenu().getGrid();
    }
    
    public void zoomIn() {
        if(scale < MAX_SCALE){
            scale++;
            try {
                editors.get(getSelectedEditor()).setScale(scale);
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    public void zoomOut() {
        if(scale > 1){
            scale--;
            try {
                editors.get(getSelectedEditor()).setScale(scale);
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
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
