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

/**
 *
 * @author mibi88
 */
public class Line {
    private DrawEvent event;

    /**
     * Draw a line.
     * @param event The DrawEvent to use to draw the line.
     */
    public Line(DrawEvent event) {
        this.event = event;
    }

    /**
     * This method implements Bresenham's line algorithm as shown in
     * https://en.wikipedia.org/wiki/Bresenham's_line_algorithm#All_cases
     * @param x1 The starting position of the line.
     * @param y1 The starting position of the line.
     * @param x2 The end position of the line.
     * @param y2 The end position of the line.
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2-x1);
        int sx = x1 < x2 ? 1 : -1;
        int dy = -Math.abs(y2-y1);
        int sy = y1 < y2 ? 1 : -1;
        int error = dx + dy;
        while(true){
            event.setPixel(x1, y1);
            if(x1 == x2 && y1 == y2){
                break;
            }
            int e2 = 2*error;
            if(e2 >= dy){
                if(x1 == x2){
                    break;
                }
                error = error+dy;
                x1 = x1 + sx;
            }
            if(e2 <= dx){
                if(y1 == y2){
                    break;
                }
                error = error+dx;
                y1 = y1+sy;
            }
        }
    }
}
