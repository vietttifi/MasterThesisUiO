package com.sensordroid.Handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by sveinpg on 17.02.16.
 */
public class DispatchTCPHandler implements Runnable {
    private final String frame; // String to send/
    private final PrintWriter pw; // Printwriter of the TCP-socket
    private final OutputStream outputStream;

    public DispatchTCPHandler(final String frame, OutputStream outputStream, PrintWriter printWriter){
        this.frame = frame;
        this.pw = printWriter;
        this.outputStream = outputStream;
    }

    /*
        Sends the given string over the TCP-socket.
     */
    @Override
    public void run() {
        // To avoid synchronization problems
        synchronized (outputStream) {
            pw.write(frame + "\n"); // Use newline to separate json-packets
            //pw.write(frame);
            pw.flush();
            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
