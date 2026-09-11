/*
 * MibiNESTools - Create NES games easily!
 * Copyright (C) 2024, 2026  Mibi88
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
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 *
 * @author mibi88
 */
public class Emulator extends Editor {
    private static final String editorName = "Emulator";
    
    private Screen screen;
    
    private Rom rom;
    
    private CPU cpu;
    private PPU ppu;
    private APU apu;
    private DMA dma;
    
    private Controller controller1;
    private Controller controller2;
    
    private Timer timer = null;
    
    /**
     * Create a new emulator.
     * @param window The window to use with this emulator.
     */
    public Emulator(Window window) {
        super(window, new BorderLayout(), editorName);
        
        try {
            rom = new Rom(null);
            
            screen = new Screen(window.getScale());
            add(screen, BorderLayout.CENTER);
            
            hardReset();
        } catch (Exception ex) {
            Logger.getLogger(Emulator.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    public void hardReset() {
        cpu = new CPU(rom);
        ppu = new PPU(rom, screen, cpu);
        apu = new APU();
        dma = new DMA(rom);

        controller1 = new Controller() {
        };
        controller2 = new Controller() {
        };
        
        if(timer != null) timer.stop();
        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ppu.emulateFrame();
            }
        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
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
            Rom rom = new Rom(file);
            
            hardReset();
        } catch (Exception ex) {
            Logger.getLogger(Emulator.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return true;
    }
    
    @Override
    public void close() {
        if(timer != null) timer.stop();
    }
    
    /**
     * Set the scale of the pixels on the screen.
     * @param scale The scale of the pixels on the screen.
     */
    @Override
    public void setScale(int scale) {
        screen.setScale(scale);
    }
}
