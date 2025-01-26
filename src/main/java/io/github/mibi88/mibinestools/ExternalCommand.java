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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author mibi88
 */
public class ExternalCommand {
    private class CommandRunnable implements Runnable {
        String command;
        String directory;
        ExternalCommandHandler handler;
        ExternalCommand ec;
        public CommandRunnable(String command, String directory,
                ExternalCommandHandler handler, ExternalCommand ec) {
            this.command = command;
            this.directory = directory;
            this.handler = handler;
            this.ec = ec;
        }
        
        @Override
        public void run() {
            Process process;
            String[] dir = {directory};
            try {
                handler.onStart(ec);
                process = Runtime.getRuntime().exec(command, dir);
                try{
                    BufferedReader stdout = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
                    BufferedReader stderr = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()));
                    while(process.isAlive()){
                        try {
                            String line;
                            String stdoutText = "";
                            String stderrText = "";
                            while((line = stdout.readLine()) != null){
                                stdoutText += line+"\n";
                            }
                            while((line = stderr.readLine()) != null){
                                stderrText += line+"\n";
                            }
                            String toPrintStdout = stdoutText;
                            String toPrintStderr = stderrText;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    handler.stdout(toPrintStdout);
                                    handler.stderr(toPrintStderr);
                                }
                            });
                        } catch (IOException ex) {
                            Logger.getLogger(Window.class
                                    .getName()).log(Level.SEVERE,
                                            null, ex);
                        }
                    }
                    process.waitFor();
                    int rc = process.exitValue();
                    handler.onReturn(rc);
                }catch(InterruptedException ex){
                    process.destroyForcibly();
                    System.out.println("Killed process!");
                }
            } catch (IOException ex) {
                Logger.getLogger(ExternalCommand.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            System.out.println("Finished running!");
        }
    }
    
    public FutureTask<Integer> task;
    
    public ExternalCommand(String command, String folder,
            ExternalCommandHandler handler) {
        Runnable runnable = new CommandRunnable(command, folder,
                handler, this);
        task = new FutureTask<Integer>(runnable, 0);
        task.run();
    }
    
    public boolean kill() {
        return task.cancel(true);
    }
    
    public boolean finished() {
        return task.isDone();
    }
}
