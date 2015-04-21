package com.example.android.wifidirect.util;


import android.os.Environment;
import android.util.Log;

import com.example.android.wifidirect.transfer.WiFiDirectActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StatHandler {

    public static void writeStat(ArrayList<File> filesToSend, long totalSize, String type) {

        try {

            File file = new File(new Date().getTime() + ".txt");

            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sendirect/statlog/" + type);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            //FileOutputStream fos = new FileOutputStream(folder.getAbsolutePath() + "/" + file.getName());
            BufferedWriter bw = new BufferedWriter(new FileWriter(folder.getAbsolutePath() + "/" + file.getName()));

            bw.write(String.valueOf(totalSize));
            bw.write("\n");

            for (File f : filesToSend) {
                bw.write(f.getName());
                bw.write("\n");
            }

            bw.close();

            Log.d(WiFiDirectActivity.TAG, "Stat log is done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<Long, Long> readStat(String type) {

        Map<Long, Long> plotData = null;

        try {
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sendirect/statlog/" + type);
            if (folder.exists()) {
                File[] files = folder.listFiles();

                plotData = new HashMap<Long, Long>();

                for (File file : files) {
                    String date = file.getName().split("\\.")[0];

                    BufferedReader br = new BufferedReader(new FileReader(file));
                    long val = Long.parseLong(br.readLine(), 10);
                    plotData.put(Long.parseLong(date), val);
                    br.close();
                }

                return plotData;
            }
        } catch (IOException e) {
            Log.e(WiFiDirectActivity.TAG, e.getMessage());
        } catch (NumberFormatException e) {
            Log.e(WiFiDirectActivity.TAG, e.getMessage());
        }

        return plotData;
    }
}
