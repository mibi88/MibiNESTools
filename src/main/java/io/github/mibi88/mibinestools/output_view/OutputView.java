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

package io.github.mibi88.mibinestools.output_view;

import io.github.mibi88.mibinestools.Editor;
import io.github.mibi88.mibinestools.Window;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JTextArea;

/**
 *
 * @author mibi88
 */
public class OutputView extends Editor {
    private static String editorName = "Terminal Output";
    private OutputBar toolBar;
    // TODO: Add JScrollPane
    private JTextArea output;
    
    public OutputView(Window window) {
        super(window, new BorderLayout());
        toolBar = new OutputBar();
        add(toolBar, BorderLayout.WEST);
        output = new JTextArea();
        output.setEditable(false);
        output.getCaret().setVisible(true);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(output, BorderLayout.CENTER);
    }
    
    public void print(String text) {
        output.append(text);
    }
    
    public void println(String text) {
        output.append(text + "\n");
    }
    
    public static String getEditorName() {
        return editorName;
    }
    
    /**
     * Get the file extension of the files that can be opened with this editor.
     * @return The file extension.
     */
    public static String[] getExtension() {
        return new String[]{"log"};
    }
}
