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
import io.github.mibi88.mibinestools.emulator.Emulator;
import io.github.mibi88.mibinestools.output_view.OutputView;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import javax.swing.SwingUtilities;
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
    private FileItem[] files;
    
    // Set useInternalAssembler to true when the assembler works.
    private boolean useInternalAssembler = false;
    
    private String assemblerCommand = "ca65 {source} -o {output}";
    private String linkerCommand = "ld65 {files} -o mibinestools.nes -t nes";
    
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
        availableEditors.add(Emulator.class);
        availableEditors.add(OutputView.class);
        
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
     * @return Returns the newly created editor.
     */
    public Editor openEditor(String extension, File file) {
        for(Class c : availableEditors) {
            try {
                String[] extensions = (String[])c.getMethod("getExtension")
                        .invoke(null);
                if(Arrays.asList(extensions).contains(extension)){
                    return openEditor(c, file);
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
        return null;
    }
    
    /**
     * Open a new editor.
     * @param editor The editor class to use.
     * @param file The file to open (can be null).
     * @throws Exception Thrown on failure.
     * @return Returns the newly created editor.
     */
    public Editor openEditor(Class editor, File file) throws Exception {
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
                }else{
                    editor.close();
                }
            }
        });
        int index = tabs.indexOfComponent(editorInstance);
        tabs.setTabComponentAt(index, closableTab);
        tabs.setSelectedIndex(index);
        if(file != null){
            editorInstance.openFile(file);
        }
        editorInstance.updateTitle();
        editors.add(editorInstance);
        return editorInstance;
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
    
    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        for(Class editorClass : availableEditors) {
            try {
                String editorName = (String)editorClass
                        .getMethod("getEditorName").invoke(null);
                String[] extensions = (String[])editorClass
                        .getMethod("getExtension").invoke(null);
                FileNameExtensionFilter chrFilter =
                        new FileNameExtensionFilter(editorName + " Files",
                                extensions);
                fileChooser.addChoosableFileFilter(chrFilter);
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        if(projectFolder != null){
            fileChooser.setCurrentDirectory(projectFolder);
        }
        return fileChooser;
    }
    
    /**
     * Open a file in the selected editor.
     * @param editor The editor to open the file in (can be null).
     * @param inCurrent If the file should be opened in the currently selected
     * editor.
     */
    public void openFile(Class editor, boolean inCurrent) {
        JFileChooser fileChooser = createFileChooser();
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
            System.out.println("Failed to save file: " + ex.getMessage());
        }
    }
    
    /**
     * Create a new file in the selected editor.
     */
    public void newFile() {
        try {
            editors.get(getSelectedEditor()).newFile();
        } catch (Exception ex) {
            System.out.println("Failed to create a new file: "
                    + ex.getMessage());
        }
    }
    
    /**
     * Save the file as in the selected editor.
     */
    public void saveAsFile() {
        JFileChooser fileChooser = createFileChooser();
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
                System.out.println("Failed to save file: " + ex.getMessage());
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
        files = settings.getSourceFiles();
        useInternalAssembler = !settings.useExternalCompiler();
        assemblerCommand = settings.getAssemblerCommand();
        linkerCommand = settings.getLinkerCommand();
    }
    
    public boolean useExternalCompiler() {
        return !useInternalAssembler;
    }
    
    public String getAssemblerCommand() {
        return assemblerCommand;
    }
    
    public String getLinkerCommand() {
        return linkerCommand;
    }
    
    /**
     * Build the current project.
     */
    public void build() {
        OutputView output;
        ArrayList<String> commands = new ArrayList<String>();
        if(files != null){
            try {
                output = (OutputView)openEditor(OutputView.class,
                        null);
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        ArrayList<String> outputFiles = new ArrayList<String>();
                        for(FileItem file : files) {
                            String outputFile = buildFile(output, file);
                            if(outputFile != null){
                                outputFiles.add(outputFile);
                            }
                        }
                        linkFiles(output, outputFiles);
                    }
                };
                thread.start();
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    public void linkFiles(OutputView output, ArrayList <String> objectFiles) {
        if(useInternalAssembler){
            // TODO
        }else{
            try {
                String fileList = "";
                for(int i=0;i<objectFiles.size();i++){
                    fileList += objectFiles.get(i) + " ";
                }
                String command = linkerCommand
                        .replace("{files}", fileList)
                        .replace("{folder}",
                                projectFolder != null ?
                                        projectFolder.getAbsolutePath() : "");
                String[] dir = {projectFolder != null ?
                                        projectFolder.getAbsolutePath() : ""};
                Process process = Runtime.getRuntime().exec(command, dir);
                Thread killProcess = new Thread() {
                    @Override
                    public void run() {
                        process.destroy();
                    }
                };
                Runtime.getRuntime().addShutdownHook(killProcess);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        output.println("Running command \"" + command +
                                "\"...");
                    }
                });
                BufferedReader stdout = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                BufferedReader stderr = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()));
                while(process.isAlive()){
                    try {
                        String line;
                        String text = "";
                        while((line = stdout.readLine()) != null){
                            text += line+"\n";
                        }
                        while((line = stderr.readLine()) != null){
                            text += line+"\n";
                        }
                        String toPrint = text;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                output.print(toPrint);
                            }
                        });
                    } catch (IOException ex) {
                        Logger.getLogger(Window.class
                                .getName()).log(Level.SEVERE,
                                        null, ex);
                    }
                }
                process.waitFor();
                int rc = process.exitValue();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(rc == 0){
                            output.println(
                                    "Program successfully linked!\n");
                        }else{
                            output.println(
                                    "Failed to link the program!\n");
                        }
                    }
                });
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    public String buildFile(OutputView output, FileItem file) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                output.println("=== Assembling " + file + "... ===");
            }
        });
        String outputFile = file.getFile().getAbsolutePath()+".o";
        if(useInternalAssembler){
            try {
                Assembler assembler = new Assembler(file.getFile());
            } catch (Exception ex) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        output.println("Failed to assemble " + file + "!\n");
                    }
                });
                Logger.getLogger(Window.class.getName())
                        .log(Level.SEVERE, null, ex);
                return null;
            }
        }else{
            try {
                String command = assemblerCommand
                        .replace("{source}",
                                file.getFile().getAbsolutePath())
                        .replace("{output}", outputFile)
                        .replace("{folder}",
                                projectFolder != null ?
                                        projectFolder.getAbsolutePath() : "");
                /*String  command = "ls";*/
                String[] dir = {projectFolder != null ?
                                        projectFolder.getAbsolutePath() : ""};
                Process process = Runtime.getRuntime().exec(command, dir);
                Thread killProcess = new Thread() {
                    @Override
                    public void run() {
                        process.destroy();
                    }
                };
                Runtime.getRuntime().addShutdownHook(killProcess);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        output.println("Running command \"" + command +
                                "\"...");
                    }
                });
                BufferedReader stdout = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                BufferedReader stderr = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()));
                while(process.isAlive()){
                    try {
                        String line;
                        String text = "";
                        while((line = stdout.readLine()) != null){
                            text += line+"\n";
                        }
                        while((line = stderr.readLine()) != null){
                            text += line+"\n";
                        }
                        String toPrint = text;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                output.print(toPrint);
                            }
                        });
                    } catch (IOException ex) {
                        Logger.getLogger(Window.class
                                .getName()).log(Level.SEVERE,
                                        null, ex);
                    }
                }
                process.waitFor();
                int rc = process.exitValue();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(rc == 0){
                            output.println(file + " assembled successfully!\n");
                        }else{
                            output.println("Failed to assemble " + file +
                                    "!\n");
                        }
                    }
                });
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        return outputFile;
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
        for(Editor editor : editors) {
            editor.close();
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
