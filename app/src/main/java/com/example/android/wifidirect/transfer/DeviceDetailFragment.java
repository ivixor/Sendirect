package com.example.android.wifidirect.transfer;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wifidirect.R;
import com.example.android.wifidirect.file_manager.FileChooser;
import com.example.android.wifidirect.util.LocalIPAddress;
import com.example.android.wifidirect.util.StatHandler;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 42;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    WifiP2pConfig config;

    private ProgressDialog progressDialog = null;

    private long fileSize = 0;
    private String destinationAddress = null;
    private SenderIPAddressAsyncTask getIPtask;

    private boolean isDestinationSet;

    private FileServerAsyncTask task;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                config.groupOwnerIntent = 0;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                        );
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);

                //mContentView.findViewById(R.id.btn_receive).setVisibility(View.VISIBLE);
            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        task.quit();
                        task.cancel(true);
                        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
                        //mContentView.findViewById(R.id.btn_receive).setVisibility(View.GONE);
                        //mContentView.findViewById(R.id.btn_stop).setVisibility(View.GONE);
                    }
                });



        /*mContentView.findViewById(R.id.btn_receive).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        task = new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text));
                        task.execute();
                        mContentView.findViewById(R.id.btn_receive).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.btn_stop).setVisibility(View.VISIBLE);
                    }
                });*/

        /*mContentView.findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.quit();
                task.cancel(true);
                mContentView.findViewById(R.id.btn_stop).setVisibility(View.GONE);
                mContentView.findViewById(R.id.btn_receive).setVisibility(View.VISIBLE);
            }
        });*/

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), FileChooser.class);
                        //startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });

        //getIPtask = new SenderIPAddressAsyncTask();
        //getIPtask.execute();

        return mContentView;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (task != null) {
            task.cancel(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != 0) {
            // User has picked an image. Transfer it to group owner i.e peer using
            // FileTransferService.
            //Uri uri = data.getData();
            //DataWrapper dw = (DataWrapper) data.getSerializableExtra();
            //ArrayList<Item> items = new ArrayList<Item>(dw.getFilesData());
            TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
            statusText.setText("Sending: " + "files");
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
            serviceIntent.putExtra("files_to_send", data.getSerializableExtra("files_to_send"));
            getActivity().startService(serviceIntent);
        }
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress() + "\n"
                                       + "Local IP - " + LocalIPAddress.getLocalIPAddress());


        /*if (!isDestinationSet) {
            if (!info.groupOwnerAddress.getHostAddress().equals(LocalIPAddress.getLocalIPAddress())) {
                Log.d("LOCAL IP", "not same");
                (new Thread() {
                    public void run() {
                        getIPtask.cancel(true);
                        getIPtask.sendIP(info.groupOwnerAddress.getHostAddress());
                    }
                }).start();
                destinationAddress = info.groupOwnerAddress.getHostAddress();
            } else {
                Log.d("LOCAL IP", "same");
                destinationAddress = getIPtask.getDestinationAddress();
            }

            isDestinationSet = true;
        }*/


        if (info.groupFormed && info.isGroupOwner) {
            task = new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text));
            task.execute();
            //new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
             //       .execute();

            //if (this.getView().getVisibility() == View.VISIBLE) {
                //mContentView.findViewById(R.id.btn_receive).setVisibility(View.VISIBLE);
                //mContentView.findViewById(R.id.btn_stop).setVisibility(View.GONE);
            //}

            ((TextView) mContentView.findViewById(R.id.status_text)).setText("Device is not ready for data receiving");
        } else if (info.groupFormed) {
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }

        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());
    }

    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    public static class FileServerAsyncTask extends AsyncTask<Void, Integer, Void> {

        private Context context;
        private TextView statusText;

        private ServerSocket serverSocket;

        private boolean isDownloadComplete;
        private boolean showProgress;

        private static ProgressDialog loadProgressDialog = null;
        private int currentFileNumber;
        private int overallFileNumber;

        private boolean done = false;

        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        public static void startProgressDialog(Context context) {
            loadProgressDialog = new ProgressDialog(context);
            loadProgressDialog.setCancelable(true);
            loadProgressDialog.setMessage("Downloading file(s)...");
            loadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            loadProgressDialog.setProgress(0);
            loadProgressDialog.setMax(100);
        }

        public void quit() {
            done = true;

            serverSocket = null;
            statusText.setText("Device is not ready to receive data");
            Log.d("server", "server stopped");
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.d("server", "server started");

            while (!done) {
                if (!isCancelled()) {
                    try {
                        serverSocket = new ServerSocket(8988);
                        Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                        Socket client = serverSocket.accept();
                        Log.d(WiFiDirectActivity.TAG, "Server: connection done");

                        isDownloadComplete = false;
                        showProgress = false;

                        receive(client);

                        serverSocket.close();

                        //return null;
                    } catch (IOException e) {
                        Log.e(WiFiDirectActivity.TAG, e.getMessage());
                        //return null;
                    }
                }
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            if (showProgress) {
                if (!loadProgressDialog.isShowing()) {
                    loadProgressDialog.show();
                }

                loadProgressDialog.setProgress(progress[0]);
                loadProgressDialog.setMessage("Loading " + (currentFileNumber + 1) + "/" + overallFileNumber);

                if (loadProgressDialog.getProgress() >= 100) {
                    loadProgressDialog.setProgress(100);
                    loadProgressDialog.setMessage("Done!");
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                          loadProgressDialog.dismiss();
                        }
                    }, 1000);

                    showProgress = false;
                }
            }
        }

        @Override
        protected void onPostExecute(Void result) {

            if (isDownloadComplete) {
                Toast.makeText(this.context, "Download complete", Toast.LENGTH_SHORT).show();
                statusText.setText("Data was received successfully");

            } else {
                Toast.makeText(this.context, "Insufficient storage space", Toast.LENGTH_SHORT).show();
                statusText.setText("An error happened during the receiving data");
            }
        }

        @Override
        protected void onPreExecute() {
            statusText.setText("Device is ready to receive data");
            startProgressDialog(this.context);
        }

        @Override
        protected void onCancelled() {
            serverSocket = null;
            statusText.setText("Device is not ready to receive data");
        }

        public void receive(Socket socket) {
            try {
                long totalSize = 0;
                long totalFileSize = 0;

                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                int number = dis.readInt();
                ArrayList<File> files = new ArrayList<File>(number);
                overallFileNumber = number;

                ArrayList<Long> sizes = new ArrayList<Long>(number);
                for (int i = 0; i < number; i++) {
                    long size = dis.readLong();
                    sizes.add(size);
                    totalSize += size;
                }

                for (int i = 0; i < number; i++) {
                    File file = new File(dis.readUTF());
                    files.add(file);
                }

                StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
                long availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();

                if (totalSize < availableSpace) {

                    showProgress = true;

                    int n = 0;
                    byte[] buf = new byte[4092];

                    for (int i = 0; i < files.size(); i++) {
                        if (!isCancelled()) {
                            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Sendirect/" + files.get(i).getName());
                            long fileSize = sizes.get(i);
                            long total = totalSize;
                            while (fileSize > 0 && (n = dis.read(buf, 0, (int)Math.min(buf.length, fileSize))) != -1) {
                                totalFileSize += n;
                                currentFileNumber = i;
                                //publishProgress((int)((totalFileSize * 100) / fileSize));
                                publishProgress((int)((totalFileSize * 100) / total));

                                fos.write(buf, 0, n);
                                fileSize -= n;
                                //total -= n;
                                fos.flush();
                            }

                            fos.close();
                        }
                    }

                    isDownloadComplete = true;
                } else {
                    isDownloadComplete = false;
                    statusText.setText("Insufficient storage space");
                }

                dis.close();

                StatHandler.writeStat(files, totalSize, "Rx");

            }  catch (IOException e) {
                Log.d(WiFiDirectActivity.TAG, e.toString());
            }
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
}
