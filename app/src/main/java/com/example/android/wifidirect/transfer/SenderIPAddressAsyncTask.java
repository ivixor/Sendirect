package com.example.android.wifidirect.transfer;

import android.os.AsyncTask;
import android.util.Log;

import com.example.android.wifidirect.util.LocalIPAddress;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class SenderIPAddressAsyncTask extends AsyncTask<Void, Void, Void> {

    private String destinationAddress;

    public void sendIP(String groupOnwerIP) {
        try {
            Log.d("ip sender", "sending group owner address: " + groupOnwerIP);
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect((new InetSocketAddress(groupOnwerIP, 8989)), 5000);
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(new String("BROFIST"));
            oos.close();
            os.close();
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.d("ip sender", "inside server");
            ServerSocket serverSocket = new ServerSocket(8989);
            serverSocket.setReuseAddress(true);
            Socket client = serverSocket.accept();
            ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
            Object object = objectInputStream.readObject();
            if (object.getClass().equals(String.class) && ((String) object).equals("BROFIST")) {
                destinationAddress = client.getInetAddress().toString().substring(1);
                Log.d("ip sender", "Client IP address: " + client.getInetAddress());
                Log.d("ip sender", "Server IP address: " + LocalIPAddress.getLocalIPAddress());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getDestinationAddress() {
        if (destinationAddress != null && !destinationAddress.equals(LocalIPAddress.getLocalIPAddress())) {
            return destinationAddress;
        } else {
            return null;
        }
    }
}
