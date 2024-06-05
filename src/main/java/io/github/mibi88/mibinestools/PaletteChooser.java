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

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author mibi88
 */
public class PaletteChooser extends JPanel {
    private JLabel label;
    private JSpinner chooser;
    public PaletteChooser(PaletteEditor paletteEditor) {
        super(new GridLayout(1, 2));
        label = new JLabel("Palette:");
        add(label);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0,
                7, 1);
        chooser = new JSpinner(spinnerModel);
        add(chooser);
        paletteEditor.usePalette((int)chooser.getValue());
        chooser.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                paletteEditor.usePalette((int)chooser.getValue());
            }
        });
    }
    
    public int getValue() {
        return (int)chooser.getValue();
    }
}
