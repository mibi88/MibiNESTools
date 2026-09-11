/*
 * MibiNESTools - Create NES games easily!
 * Copyright (C) 2025-2026  Mibi88
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

// FIXME: This is a bit messy and could probably be done in a way cleaner way.

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
        ExecutorService service;
        public CommandRunnable(String command, String directory,
                ExternalCommandHandler handler, ExecutorService service,
                ExternalCommand ec) {
            this.command = command;
            this.directory = directory;
            this.handler = handler;
            this.service = service;
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
                    do{
                        try {
                            String line;
                            while(!stop && (line = stdout.readLine()) != null){
                                String outputLine = line;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        handler.stdout(outputLine+"\n");
                                    }
                                });
                            }
                            while(!stop && (line = stderr.readLine()) != null){
                                String outputLine = line;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        handler.stderr(outputLine+"\n");
                                    }
                                });
                            }
                            System.out.println("Checking stop...");
                            if(stop){
                                process.destroyForcibly();
                                System.out.println("Forcibly destroyed process!");
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Window.class
                                    .getName()).log(Level.SEVERE,
                                            null, ex);
                        }
                    }while(process.isAlive());
                    process.waitFor();
                    int rc = process.exitValue();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            handler.onReturn(rc);
                        }
                    });
                }catch(InterruptedException ex){
                    process.destroyForcibly();
                    System.out.println("Killed process!");
                }
            } catch (IOException ex) {
                handler.stderr(ex.getMessage());
                Logger.getLogger(ExternalCommand.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            service.shutdown();
            System.out.println("Finished running!");
        }
    }

    protected boolean stop = false;
    
    public FutureTask<Integer> task;
    
    public ExternalCommand(String command, String folder,
            ExternalCommandHandler handler) {
        ExecutorService service = Executors.newFixedThreadPool(1);

        Runnable runnable = new CommandRunnable(command, folder,
                handler, service, this);
        task = new FutureTask<Integer>(runnable, 0);
        
        service.submit(task);
    }
    
    public void kill() {
        stop = true;
    }
    
    public boolean finished() {
        return task.isDone();
    }
}
