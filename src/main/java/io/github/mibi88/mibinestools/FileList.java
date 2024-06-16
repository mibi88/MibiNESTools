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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

/**
 *
 * @author mibi88
 */
public class FileList extends JPanel {
    private JScrollPane filePane;
    private JScrollPane projectPane;
    private JList fileList;
    private DefaultListModel listModel;
    private FileTree projectTree;
    private JSplitPane splitPane;
    private JButton remove;

    /**
     * Create a new file list.
     * @param window The window to use with it.
     */
    public FileList(Window window) {
        super(new GridBagLayout());
        
        File folder = window.getProjectFolder();
        projectTree = new FileTree(folder);
        projectPane = new JScrollPane(projectTree);
        projectPane.setPreferredSize(new Dimension(100, 0));
        
        listModel = new DefaultListModel();
        fileList = new JList(listModel);
        fileList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        filePane = new JScrollPane(fileList);
        filePane.setPreferredSize(new Dimension(100, 0));
        
        projectTree.setEventHandler(new FileTreeEvent() {
            @Override
            public void fileSelected(File file) {
                for(int i=0;i<listModel.getSize();i++){
                    FileItem fileItem =
                            (FileItem)listModel.getElementAt(i);
                    if(file.equals(fileItem.getFile())) {
                        return;
                    }
                }
                listModel.addElement(new FileItem(file, folder));
                filePane.revalidate();
            }
        });
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                projectPane, filePane);
        
        remove = new JButton("Remove");
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = fileList.getSelectedIndex();
                if(index >= 0){
                    listModel.removeElementAt(index);
                }
            }
        });
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(splitPane, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(remove, c);
    }
}
