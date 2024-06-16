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

import java.io.File;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author mibi88
 */
public class FileTree extends JTree {
    FileTreeEvent event;
    /**
     * Create a new file tree.
     * @param folder The folder to create the tree of (can be null).
     */
    public FileTree(File folder) {
        super();
        update(folder);
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode)e.getPath()
                                .getLastPathComponent();
                if(node != null && event != null){
                    FileInfo info = (FileInfo)node.getUserObject();
                    File file = info.getFile();
                    if(file != null){
                        if(file.isFile()){
                            event.fileSelected(file);
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Set the event handler to handle file selection events.
     * @param event The event handler.
     */
    public void setEventHandler(FileTreeEvent event) {
        this.event = event;
    }
    
    /**
     * Update the content of the file tree.
     * @param folder The folder to create the tree of.
     */
    public void update(File folder) {
        DefaultMutableTreeNode rootNode =
                new DefaultMutableTreeNode(new FileInfo(folder));
        if(folder != null){
            addFiles(rootNode, folder);
        }
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        setModel(treeModel);
    }
    
    private void addFiles(DefaultMutableTreeNode node, File folder) {
        for(File file : folder.listFiles()){
            DefaultMutableTreeNode newNode =
                    new DefaultMutableTreeNode(new FileInfo(file));
            node.add(newNode);
            if(file.isDirectory()){
                addFiles(newNode, file);
            }
        }
    }
    
    private class FileInfo {
        File file;
        public FileInfo(File file) {
            this.file = file;
        }
        
        public File getFile() {
            return file;
        }
        
        @Override
        public String toString() {
            if(file != null){
                return file.getName();
            }
            return "Open a project folder";
        }
    }
}
