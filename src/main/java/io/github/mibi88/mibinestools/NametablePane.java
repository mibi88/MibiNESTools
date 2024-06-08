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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author mibi88
 */
public class NametablePane extends JPanel {
    private NametableViewer nametableViewer;
    private JScrollPane nametableViewerPane;
    private NametableToolbar nametableToolbar;
    
    public NametablePane(NametableEditor editor){
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        nametableToolbar = new NametableToolbar();
        nametableViewer = new NametableViewer(editor.getCHRData(),
                editor.getPaletteEditor(), editor.getScale(),
                true);
        nametableViewerPane = new JScrollPane(nametableViewer);
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 1;
        c.weightx = 0;
        add(nametableToolbar, c);
        c.gridx++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        add(nametableViewerPane, c);
        
        nametableViewer.setEventHandler(new NametableViewerEvent() {
            @Override
            public void tileChanged(int tx, int ty) {
                editor.fileEdited();
            }
        });
    }
    
    public void setScale(int scale) {
        nametableViewer.setScale(scale);
        nametableViewerPane.revalidate();
    }
    
    public void open(File file) throws IOException {
        nametableViewer.open(file);
    }
    
    public void save(File file) throws IOException {
        nametableViewer.save(file);
    }
    
    public void setCHR(CHRData chrData) {
        nametableViewer.setCHR(chrData);
    }
    
    public void setCHRBank(int chrBank) {
        nametableViewer.setCHRBank(chrBank);
    }
    
    public void setCurrentTile(int currentTile) {
        nametableViewer.setCurrentTile(currentTile);
    }
    
    public void paletteChanged() {
        nametableViewer.repaint();
    }
}
