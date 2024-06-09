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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author mibi88
 */
public class TilePickerTools extends JToolBar {
    private JButton loadCHR;
    private JButton reloadCHR;
    private SpinnerNumberModel chrBankModel;
    private JLabel spinnerLabel;
    private JSpinner chrBank;
    private TilePicker tilePicker;
    
    public TilePickerTools(TilePicker tilePicker) {
        super();
        setFloatable(false);
        setRollover(true);
        
        this.tilePicker = tilePicker;
        
        loadCHR = new JButton("Load CHR");
        add(loadCHR);
        reloadCHR = new JButton("Reload CHR");
        add(reloadCHR);
        spinnerLabel = new JLabel("CHR Bank:");
        add(spinnerLabel);
        chrBankModel = new SpinnerNumberModel(0, 0,
                tilePicker.getCHRBanks()-1, 1);
        chrBank = new JSpinner(chrBankModel);
        add(chrBank);
        addActions();
    }
    
    private void addActions() {
        loadCHR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tilePicker.openCHR();
            }
        });
        reloadCHR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tilePicker.updateCHR();
            }
        });
        chrBank.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tilePicker.setCHRBank((int)chrBank.getValue());
            }
        });
    }
    
    public void setCHRBanks(int value) {
        chrBankModel.setMaximum(value);
    }
}
