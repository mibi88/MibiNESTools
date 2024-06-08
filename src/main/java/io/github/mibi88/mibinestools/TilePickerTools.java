/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.mibi88.mibinestools;

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
