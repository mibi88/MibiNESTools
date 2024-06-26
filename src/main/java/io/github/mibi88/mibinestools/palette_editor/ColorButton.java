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
package io.github.mibi88.mibinestools.palette_editor;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.image.BufferedImage;
import javax.swing.JButton;

/**
 *
 * @author mibi88
 */
public class ColorButton extends JButton {
    private BufferedImage image;
    private ComponentAdapter componentAdapter;

    /**
     * Create a new colored button.
     * @param color The color of the button.
     */
    public ColorButton(Color color) {
        super();
        updateColor(color);
    }
    
    /**
     * Change the color of the button.
     * @param color The new color of the button.
     */
    public void updateColor(Color color) {
        setBackground(color);
    }
}
