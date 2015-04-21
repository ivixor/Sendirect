package com.example.android.wifidirect.diagram;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import com.example.android.wifidirect.R;
import com.example.android.wifidirect.util.DataWrapper;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlotDiagramActivity extends Activity {

    private List<Long> keySet;
    private List<Long> values;

    private GraphicalView plotView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataWrapper dw = (DataWrapper) getIntent().getSerializableExtra("data");

        int size = dw.getHashMap().size();
        keySet = new ArrayList<>(dw.getHashMap().keySet());
        values = new ArrayList<>(dw.getHashMap().values());

        TimeSeries series = new TimeSeries("Line1");

        for (int i = 0; i < size; i++) {
            long time = keySet.get(i);
            long value = values.get(i);
            series.add(time, value);
        }

        String period = getIntent().getSerializableExtra("period").toString();

        String dataType = getIntent().getSerializableExtra("data_type").toString();

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
        //seriesRenderer.setColor(Color.rgb(131, 3, 0)); // maroon
        seriesRenderer.setColor(Color.rgb(171, 193, 53)); // nice green
        seriesRenderer.setPointStyle(PointStyle.CIRCLE);
        seriesRenderer.setPointStrokeWidth(30f);
        seriesRenderer.setFillPoints(true);
        seriesRenderer.setDisplayBoundingPoints(true);
        seriesRenderer.setLineWidth(2.5f);
        seriesRenderer.setDisplayChartValues(true);
        seriesRenderer.setChartValuesTextSize(10f);

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.addSeriesRenderer(seriesRenderer);
        renderer.setChartTitle("Amount of " + dataType + " bytes over time (" + period + ")");
        renderer.setChartTitleTextSize(25);
        renderer.setMargins(new int[] {30, 30, 35, 0});
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setXTitle("\n\nDate");
        renderer.setYTitle("Bytes");
        renderer.setLabelsColor(Color.CYAN);
        renderer.setAxisTitleTextSize(23);

        renderer.setYAxisMin(0);
        renderer.setYAxisMax(series.getMaxY() * 1.1);

        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.LEFT);
        renderer.setXLabelsPadding(10);
        renderer.setYLabelsPadding(-5);
        renderer.setLabelsTextSize(13);
        renderer.setYLabelsColor(0, Color.rgb(131, 3, 0));
        renderer.setZoomEnabled(true, true);
        renderer.setZoomButtonsVisible(true);
        renderer.setPanEnabled(true, true);

        renderer.setShowGridX(true);
        renderer.setShowLegend(false);
        renderer.setXLabels(0);

        for (int i = 0; i < size; i++) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy\nHH:mm:ss");
            renderer.addXTextLabel(keySet.get(i), formatter.format(new Date(keySet.get(i))));
        }

        plotView = ChartFactory.getLineChartView(this, dataset, renderer);

        setContentView(plotView);
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

        File file = new File("plot-" + new Date().getTime() + ".jpg");

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sendirect/images/");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        plotView.setDrawingCacheEnabled(true);
        int width = plotView.getWidth();
        int height = plotView.getHeight();
        plotView.measure(width, height);
        Bitmap bmp = Bitmap.createBitmap(plotView.getDrawingCache());
        plotView.setDrawingCacheEnabled(false);
        FileOutputStream fos = new FileOutputStream(folder.getAbsolutePath() + "/" + file.getName());
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
    }
}
