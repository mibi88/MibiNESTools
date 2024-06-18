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

import io.github.mibi88.mibinestools.assembler.Assembler;
import io.github.mibi88.mibinestools.nametable_editor.NametableEditor;
import io.github.mibi88.mibinestools.chr_editor.CHREditor;
import io.github.mibi88.mibinestools.code_editor.CodeEditor;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
    
    private JSplitPane splitPane;
    private JPanel editorPanel;
    private ProjectToolbar projectToolbar;
    private JTabbedPane tabs;
    private JScrollPane treePane;
    private FileTree fileTree;
    
    private ArrayList<Editor> editors;
    private ArrayList<Class> availableEditors;
    
    private int scale;
    
    private File projectFolder;
    
    /**
     * Initialize the GUI
     */
    public Window() {
        super();
        scale = 8;
        initWindow(640, 480);
    }
    
    private void initWindow(int width, int height) {
        setTitle(TITLE);
        setSize(width, height);
        
        setVisible(true);
        
        
        setDefaultCloseOperation(Window.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                quit();
            }
        });
        
        menubar = new Menubar(this);
        setJMenuBar(menubar);
        
        editorPanel = new JPanel(new GridBagLayout());
        projectToolbar = new ProjectToolbar(this);
        tabs = new JTabbedPane();
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        editorPanel.add(projectToolbar, c);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        editorPanel.add(tabs, c);
        
        editors = new ArrayList<Editor>();
        availableEditors = new ArrayList<Class>();
        
        availableEditors.add(CHREditor.class);
        availableEditors.add(NametableEditor.class);
        availableEditors.add(CodeEditor.class);
        
        updateMenus();
        
        fileTree = new FileTree(null);
        fileTree.setEventHandler(new FileTreeEvent() {
            @Override
            public void fileSelected(File file) {
                openFile(file);
            }
        });
        treePane = new JScrollPane(fileTree);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane,
                editorPanel);
        
        add(splitPane);
    }
    
    /**
     * Open a file in a new editor.
     * @param extension The extension of the file.
     * @param file The file to open.
     */
    public void openEditor(String extension, File file) {
        for(Class c : availableEditors) {
            try {
                String[] extensions = (String[])c.getMethod("getExtension")
                        .invoke(null);
                if(Arrays.asList(extensions).contains(extension)){
                    openEditor(c, file);
                    return;
                }
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        JOptionPane optionPane = new JOptionPane();
        optionPane.showMessageDialog(this,
                "Unknown file extension\n"
                + "Use \"Open with...\" to open this file.");
    }
    
    /**
     * Open a new editor.
     * @param editor The editor class to use.
     * @param file The file to open (can be null).
     * @throws Exception Thrown on failure.
     */
    public void openEditor(Class editor, File file) throws Exception {
        Constructor constructor = editor.getConstructor(
                this.getClass());
        Editor editorInstance = (Editor)constructor.newInstance(this);
        tabs.addTab(editorInstance.getEditorName(),
                editorInstance);
        ClosableTab closableTab = new ClosableTab(tabs,
                        editorInstance);
        closableTab.setEventHandler(new CloseEvent() {
            @Override
            public void tabClosed(int index) {
                return;
            }

            @Override
            public void tabClosed(Editor editor) {
                if(!editors.remove(editor)) {
                    System.out.println("Failed to remove editor!");
                }
            }
        });
        int index = tabs.indexOfComponent(editorInstance);
        tabs.setTabComponentAt(index, closableTab);
        tabs.setSelectedIndex(index);
        if(file != null){
            editorInstance.openFile(file);
        }
        editors.add(editorInstance);
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if(index > 0){
            return name.substring(index+1);
        }
        return "";
    }
    
    private void updateMenus() {
        NewFileMenu newFileMenu = menubar.getNewFileMenu();
        OpenWithMenu openWithMenu = menubar.getOpenWithMenu();
        newFileMenu.reset(this);
        openWithMenu.reset(this);
        for(Class c : availableEditors){
            newFileMenu.addEditor(c, this);
            openWithMenu.addEditor(c, this);
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
    
    /**
     * Open a file.
     * @param file The file to open.
     */
    public void openFile(File file) {
        for(Editor editor : editors) {
            if(file.equals(editor.getFile())){
                tabs.setSelectedComponent(editor);
                return;
            }
        }
        openEditor(getFileExtension(file), file);
    }
    
    /**
     * Open a file in the selected editor.
     * @param editor The editor to open the file in (can be null).
     * @param inCurrent If the file should be opened in the currently selected
     * editor.
     */
    public void openFile(Class editor, boolean inCurrent) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter chrFilter =
                new FileNameExtensionFilter("CHR Files", "chr");
        fileChooser.addChoosableFileFilter(chrFilter);
        int out = fileChooser.showOpenDialog(this);
        if(out == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if(editor == null){
                if(inCurrent){
                    try {
                        editors.get(getSelectedEditor()).openFile(file);
                    } catch (Exception ex) {
                        Logger.getLogger(Window.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }else{
                    openEditor(getFileExtension(file), file);
                }
            }else{
                try {
                    openEditor(editor, file);
                } catch (Exception ex) {
                    Logger.getLogger(Window.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Open a folder.
     */
    public void openFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int out = fileChooser.showOpenDialog(this);
        if(out == JFileChooser.APPROVE_OPTION){
            File folder = fileChooser.getSelectedFile();
            fileTree.update(folder);
            projectFolder = folder;
        }
    }
    
    /**
     * Save a file in the selected editor.
     */
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
    
    /**
     * Create a new file in the selected editor.
     */
    public void newFile() {
        try {
            editors.get(getSelectedEditor()).newFile();
        } catch (Exception ex) {
            System.out.println("No open editor");
        }
    }
    
    /**
     * Save the file as in the selected editor.
     */
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
                if(selected != JOptionPane.OK_OPTION
                        || askOverwrite.getValue() == null){
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
    
    /**
     * Open the project settings window.
     */
    public void openProjectSettings() {
        ProjectSettings settings = new ProjectSettings(this);
    }
    
    /**
     * Save the project settings.
     * @param settings The project settings dialog.
     */
    public void saveProjectSettings(ProjectSettings settings) {
        FileItem[] files = settings.getSourceFiles();
        for(FileItem file : files) {
            System.out.println("Assembling " + file + "...");
            try {
                Assembler assembler = new Assembler(file.getFile());
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Get the project folder.
     * @return Returns the project folder.
     */
    public File getProjectFolder() {
        return projectFolder;
    }
    
    /**
     * Enable or disable the grid in tilemaps etc.
     * @param grid True if the grid should be drawn.
     */
    public void setGrid(boolean grid) {
        for(Editor editor : editors) {
            editor.setGrid(grid);
        }
    }
    
    /**
     * Check if the grid should be drawn.
     * @return Returns true if the grid should be drawn
     */
    public boolean getGrid() {
        return menubar.getViewMenu().getGrid();
    }
    
    /**
     * Increase the scale of the content of the editors.
     */
    public void zoomIn() {
        if(scale < MAX_SCALE){
            scale++;
            for(Editor editor : editors) {
                editor.setScale(scale);
            }
        }
    }
    
    /**
     * Decrease the scale of the content of the editors.
     */
    public void zoomOut() {
        if(scale > 1){
            scale--;
            for(Editor editor : editors) {
                editor.setScale(scale);
            }
        }
    }
    
    /**
     * Returns the scale of the content of the editors.
     * @return The scale.
     */
    public int getScale() {
        return scale;
    }
    
    /**
     * Close the window
     */
    public void quit() {
        for(Editor editor : editors) {
            if(!editor.getFileSaved()){
                String fileName = editor.getFileName();
                JOptionPane askToQuit = new JOptionPane();
                int selected = askToQuit.showConfirmDialog(this,
                                fileName + " is not saved!\n"
                                        + "Do you really want to quit?",
                                "Unsaved changes",
                                JOptionPane.YES_NO_OPTION);
                if(selected != JOptionPane.OK_OPTION
                        || askToQuit.getValue() == null){
                    return;
                }else{
                    break;
                }
            }
        }
        dispose();
    }
    
    /**
     * Undo the last action in the selected editor.
     */
    public void undo() {
        try {
            editors.get(getSelectedEditor()).undo();
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Redo the last action in the selected editor.
     */
    public void redo() {
        try {
            editors.get(getSelectedEditor()).redo();
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Copy some data in the selected editor.
     */
    public void copy() {
        try {
            editors.get(getSelectedEditor()).copy();
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Cut some data in the selected editor.
     */
    public void cut() {
        try {
            editors.get(getSelectedEditor()).cut();
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Paste some data in the selected editor.
     */
    public void paste() {
        try {
            editors.get(getSelectedEditor()).paste();
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Load a plugin.
     */
    public void loadPlugin() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter propertiesFilter =
                new FileNameExtensionFilter("Properties",
                        "properties");
        fileChooser.addChoosableFileFilter(propertiesFilter);
        int out = fileChooser.showOpenDialog(this);
        if(out == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try {
                availableEditors.add(PluginLoader.loadPlugin(file,
                        this));
                updateMenus();
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
}
