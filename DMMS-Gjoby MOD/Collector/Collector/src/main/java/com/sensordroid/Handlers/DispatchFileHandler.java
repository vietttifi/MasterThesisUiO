package com.sensordroid.Handlers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by sveinpg on 16.03.16.
 */
public class DispatchFileHandler implements Runnable {
    private final String frame; // String to send/
    private final FileWriter writer;

    public DispatchFileHandler(final String frame, FileWriter writer){
        //public DispatchFileHandler(final String frame, FileOutputStream outputStream, PrintWriter writer){
        this.frame = frame;
        this.writer = writer;
        //this.outputStream = outputStream;
    }

    /*
        Sends the given string over the TCP-socket.
     */
    @Override
    public void run() {
        // To avoid synchronization problems
        synchronized (writer) {
            try {
                writer.append(frame + "\n"); // Use newline to separate json-packets
                //writer.flush();
                //outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
