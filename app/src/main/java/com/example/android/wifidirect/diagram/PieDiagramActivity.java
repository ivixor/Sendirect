package com.example.android.wifidirect.diagram;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.wifidirect.R;
import com.example.android.wifidirect.util.DataWrapper;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedHashMap;

public class PieDiagramActivity extends Activity {

    private GraphicalView pieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataWrapper dw = (DataWrapper) getIntent().getSerializableExtra("rx_data");
        LinkedHashMap<Long, Long> rxData = dw.getHashMap();

        dw = (DataWrapper) getIntent().getSerializableExtra("tx_data");
        LinkedHashMap<Long, Long> txData = dw.getHashMap();

        String period = getIntent().getSerializableExtra("period").toString();

        String visualType = getIntent().getSerializableExtra("visual_type").toString();

        long rxSum = 0;
        long txSum = 0;

        long overallSum = 0;

        double rxPercentage = 0;
        double txPercentage = 0;

        rxSum = calcSum(rxData);
        txSum = calcSum(txData) + 500000;
        overallSum = rxSum + txSum;

        rxPercentage = (rxSum * 100f) / overallSum;
        txPercentage = 100 - rxPercentage;

        String rx = "";
        String tx = "";

        switch (visualType) {
            case "kilobytes":
                rx = humanReadableByteCount(rxSum, 1);
                tx = humanReadableByteCount(txSum, 1);
                break;
            case "megabytes":
                rx = humanReadableByteCount(rxSum, 2);
                tx = humanReadableByteCount(txSum, 2);
                break;
            case "gigabytes":
                rx = humanReadableByteCount(rxSum, 3);
                tx = humanReadableByteCount(txSum, 3);
                break;
        }

        DecimalFormat df = new DecimalFormat("####0.00");
        CategorySeries series = new CategorySeries("Transmitted / Received data ratio");
        series.add("Tx: " + tx + "\n(" + Double.valueOf(df.format(txPercentage)) + "%)", Double.valueOf(df.format(txPercentage)));
        series.add("Rx: " + rx + "\n(" + Double.valueOf(df.format(rxPercentage)) + "%)", Double.valueOf(df.format(rxPercentage)));

        int[] colors = new int[] {
                Color.rgb(48, 115, 150),
                Color.rgb(217, 159, 24)
        };

        DefaultRenderer renderer = new DefaultRenderer();
        for (int color : colors) {
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            r.setColor(color);
            renderer.addSeriesRenderer(r);
        }
        renderer.setStartAngle(45);
        renderer.setChartTitle("Tx/Rx data ratio (" + period + ")");
        renderer.setChartTitleTextSize(35);

        renderer.setLegendTextSize(25);
        renderer.setLabelsTextSize(25);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setShowLabels(true);

        renderer.setZoomButtonsVisible(true);

        pieView = ChartFactory.getPieChartView(this, series, renderer);

        setContentView(pieView);
    }

    public long calcSum(LinkedHashMap<Long, Long> data) {
        long sum = 0;
        if (data != null) {
            for (long key : data.keySet()) {
                sum += data.get(key);
            }
        }

        return sum;
    }

    public static String humanReadableByteCount(long bytes, int type) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        //int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(type - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, type), pre);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.diagram_activity_action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_save_image:
                try {
                    saveImage();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveImage() throws FileNotFoundException {

        File file = new File("pie-" + new Date().getTime() + ".jpg");

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sendirect/images/");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        pieView.setDrawingCacheEnabled(true);
        int width = pieView.getWidth();
        int height = pieView.getHeight();
        pieView.measure(width, height);
        Bitmap bmp = Bitmap.createBitmap(pieView.getDrawingCache());
        pieView.setDrawingCacheEnabled(false);
        FileOutputStream fos = new FileOutputStream(folder.getAbsolutePath() + "/" + file.getName());
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //pieView.zoomReset();
            return true;
        }
    }
}
