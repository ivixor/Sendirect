package com.example.android.wifidirect.transfer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.wifidirect.util.DataWrapper;
import com.example.android.wifidirect.util.StatHandler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;


public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public ArrayList<File> filesToSend;

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            //String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            DataWrapper dw = (DataWrapper) intent.getSerializableExtra("files_to_send");
            filesToSend = new ArrayList<File>(dw.getFilesData());
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());

                send(socket);
                Log.d(WiFiDirectActivity.TAG, "Client: Data written");

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void send(Socket socket) {
        try {
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            dos.writeInt(filesToSend.size());
            dos.flush();

            long totalSize = 0;
            for (int i = 0; i < filesToSend.size(); i++) {
                dos.writeLong(filesToSend.get(i).length());
                dos.flush();
                totalSize += filesToSend.get(i).length();
            }

            for (int i = 0; i < filesToSend.size(); i++) {
                dos.writeUTF(filesToSend.get(i).getName());
                dos.flush();
            }

            int n = 0;
            byte[] buf = new byte[4092];
            for (int i = 0; i < filesToSend.size(); i++) {
                FileInputStream fis = new FileInputStream(new File(filesToSend.get(i).getPath()));

                while ((n = fis.read(buf)) != -1) {
                    dos.write(buf, 0, n);
                    dos.flush();
                }
            }

            dos.close();

            StatHandler.writeStat(filesToSend, totalSize, "Tx");

        } catch (FileNotFoundException e) {
            Log.e(WiFiDirectActivity.TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(WiFiDirectActivity.TAG, e.getMessage());
        }
    }
}
