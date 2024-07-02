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

package io.github.mibi88.mibinestools.emulator;

import io.github.mibi88.mibinestools.Editor;
import io.github.mibi88.mibinestools.Window;
import io.github.mibi88.mibinestools.chr_editor.CHRData;
import java.awt.BorderLayout;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mibi88
 */
public class Emulator extends Editor {
    private static String editorName = "Emulator";
    private Screen screen;
    private CPU cpu;

    /**
     * Create a new emulator.
     * @param window The window to use with this emulator.
     */
    public Emulator(Window window) {
        super(window, new BorderLayout());
        
        try {
            Rom rom = new Rom(null);
            screen = new Screen(rom.getRawCHRData(),
                    Region.NTSC, 2, false);
            cpu = new CPU(rom);
            screen.play(cpu);
            add(screen, BorderLayout.CENTER);
            setEditorName(editorName);
        } catch (Exception ex) {
            Logger.getLogger(Emulator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Get the file extension of the files that this editor can open.
     * @return The file extension of the files that this editor can open.
     */
    public static String[] getExtension() {
        return new String[]{"nes"};
    }
    
    /**
     * Get the name of the editor
     * @return The name of the editor.
     */
    public static String getEditorName() {
        return editorName;
    }
    
    /**
     * Open a ROM.
     * @param file The ROM file.
     * @return If the file was opened
     */
    @Override
    public boolean openFile(File file) {
        if(!super.openFile(file)){
            return false;
        }
        try {
            Rom rom = new Rom(null);
            screen.powerOff();
            cpu = new CPU(rom);
            screen.play(cpu);
        } catch (Exception ex) {
            Logger.getLogger(Emulator.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return true;
    }
}
