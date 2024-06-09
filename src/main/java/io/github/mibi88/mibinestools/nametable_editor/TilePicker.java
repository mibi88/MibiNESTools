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
package io.github.mibi88.mibinestools.nametable_editor;

import io.github.mibi88.mibinestools.chr_editor.PatternTable;
import io.github.mibi88.mibinestools.chr_editor.PatternTableEvent;
import io.github.mibi88.mibinestools.Window;
import io.github.mibi88.mibinestools.chr_editor.CHRData;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author mibi88
 */
public class TilePicker extends JPanel {
    private PatternTable patternTable;
    private JScrollPane patternTablePane;
    
    private TilePickerTools tools;
    
    private CHRData chrData;
    
    private File file;
    
    private NametableEditor editor;
    
    public TilePicker(NametableEditor editor, Window window) {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        this.editor = editor;
        chrData = new CHRData();
        patternTable = new PatternTable(chrData,
                editor.getCurrentPalette(), window.getScale(),
                true);
        patternTablePane = new JScrollPane(patternTable);
        
        tools = new TilePickerTools(this);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(tools, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(patternTablePane, c);
        addActions();
    }
    
    public CHRData getCHRData() {
        return chrData;
    }
    
    public void setPalette(int[][] palette) {
        patternTable.setPalette(palette);
    }
    
    private void addActions() {
        patternTable.setEventHandler(new PatternTableEvent() {
            @Override
            public void tileSelected(int tx, int ty) {
                editor.setCurrentTile(ty*16+tx);
            }
        });
    }
    
    public void openCHR() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter chrFilter =
                new FileNameExtensionFilter("CHR Files", "chr");
        fileChooser.addChoosableFileFilter(chrFilter);
        int out = fileChooser.showOpenDialog(this);
        if(out == JFileChooser.APPROVE_OPTION){
            file = fileChooser.getSelectedFile();
            try {
                updateCHR();
            } catch (Exception ex) {
                Logger.getLogger(Window.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void updateCHR() {
        if(file != null){
            try {
                chrData = new CHRData(file);
                patternTable.setCHR(chrData);
                editor.setCHR(chrData);
                tools.setCHRBanks(chrData.getChrBanks());
                
            } catch (Exception ex) {
                Logger.getLogger(TilePicker.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
    
    public void setScale(int scale) {
        patternTable.setScale(scale);
        patternTablePane.revalidate();
    }
    
    public void setCHRBank(int value) {
        editor.setCHRBank(value);
    }
    
    public int getCHRBanks() {
        return chrData.getChrBanks();
    }
    
    public void setGrid(boolean grid) {
        patternTable.setGrid(grid);
    }
}
