package com.example.android.wifidirect.diagram;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.example.android.wifidirect.util.DataWrapper;
import com.example.android.wifidirect.R;
import com.example.android.wifidirect.util.StatHandler;

import org.joda.time.DateTime;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class PlotDialog extends DialogFragment {

    private View dialogView;

    private final static String[] DIAGRAM_TYPE  = new String[] {
            "plot",
            "bar",
            "pie"
    };

    private final static long[] PERIOD = new long[] {
            86400000L,
            604800000L,
            2628000000L,
            7884000000L,
            15768000000L,
            31536000000L
    };

    private final static String[] DATA_TYPE = new String[] {
            "Rx",
            "Tx"
    };

    private final static String[] VISUAL_TYPE = new String[] {
            "kilobytes",
            "megabytes",
            "gigabytes"
    };

    private int selectedDiagram = 0;
    private int selectedPeriod = 0;
    private int selectedDataType = 0;
    private int selectedVisualType = 0;

    private void showAlertDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("No data")
                .setMessage("There is no data to visualize for chosen period or data type")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {  }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public LinkedHashMap<Long, Long> sortData(String dataType) {

        LinkedHashMap<Long, Long> sortedData = null;

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Sendirect/statlog/" + DATA_TYPE[selectedDataType]);

        if (folder.exists() && folder.listFiles().length > 0) {
            Map<Long, Long> plotData = StatHandler.readStat(dataType);
            if (plotData != null) {
                SortedSet<Long> keys = new TreeSet<>(plotData.keySet());
                sortedData = new LinkedHashMap<>();

                DateTime currentTime = DateTime.now();

                for (long key : keys) {
                    DateTime dt = new DateTime(currentTime).minus(key);

                    if (selectedPeriod == -1) {
                        sortedData.put(key, plotData.get(key));
                    } else {
                        if (PERIOD[selectedPeriod] >= dt.getMillis()) {
                            sortedData.put(key, plotData.get(key));
                        }
                    }
                }

                if (sortedData.isEmpty()) {
                    sortedData = null;
                }
            }

            return sortedData;
        } else {
            return null;
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialogView = inflater.inflate(R.layout.diagram_setup_dialog, null);
        getDialog().setTitle("Diagram setup");

        Button okButton = (Button) dialogView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String period = "";
                switch (selectedPeriod) {
                    case -1:
                        period = "overall";
                        break;
                    case 0:
                        period = "24 hours";
                        break;
                    case 1:
                        period = "7 days";
                        break;
                    case 2:
                        period = "1 month";
                        break;
                    case 3:
                        period = "3 months";
                        break;
                    case 4:
                        period = "6 months";
                        break;
                    case 5:
                        period = "1 year";
                        break;
                }

                if (DIAGRAM_TYPE[selectedDiagram].equals("plot")) {
                    LinkedHashMap<Long, Long> data = sortData(DATA_TYPE[selectedDataType]);

                    if (data == null) {
                        showAlertDialog();
                    } else {
                        Intent intent = new Intent(getActivity(), PlotDiagramActivity.class);
                        intent.putExtra("data", new DataWrapper(data));
                        intent.putExtra("period", period);
                        intent.putExtra("data_type", DATA_TYPE[selectedDataType]);
                        startActivity(intent);
                    }
                } else if (DIAGRAM_TYPE[selectedDiagram].equals("pie")) {
                    LinkedHashMap<Long, Long> rxData = sortData(DATA_TYPE[0]);
                    LinkedHashMap<Long, Long> txData = sortData(DATA_TYPE[1]);

                    if (rxData == null && txData == null) {
                        showAlertDialog();
                    } else {
                        Intent intent = new Intent(getActivity(), PieDiagramActivity.class);
                        intent.putExtra("rx_data", new DataWrapper(rxData));
                        intent.putExtra("tx_data", new DataWrapper(txData));

                        intent.putExtra("period", period);
                        intent.putExtra("visual_type", VISUAL_TYPE[selectedVisualType]);
                        startActivity(intent);
                    }
                }

                dismiss();
            }
        });

        Button cancelButton = (Button) dialogView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final Spinner diagramSpinner = (Spinner) dialogView.findViewById(R.id.spinner1);
        diagramSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedDiagram = position;

                if (DIAGRAM_TYPE[selectedDiagram].equals("plot")) {
                    dialogView.findViewById(R.id.textView3).setVisibility(View.VISIBLE);
                    dialogView.findViewById(R.id.spinner3).setVisibility(View.VISIBLE);
                    dialogView.findViewById(R.id.textView4).setVisibility(View.GONE);
                    dialogView.findViewById(R.id.spinner4).setVisibility(View.GONE);
                } else {
                    dialogView.findViewById(R.id.textView3).setVisibility(View.GONE);
                    dialogView.findViewById(R.id.spinner3).setVisibility(View.GONE);
                    dialogView.findViewById(R.id.textView4).setVisibility(View.VISIBLE);
                    dialogView.findViewById(R.id.spinner4).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        Spinner periodSpinner = (Spinner) dialogView.findViewById(R.id.spinner2);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPeriod = position - 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        Spinner datatypeSpinner = (Spinner) dialogView.findViewById(R.id.spinner3);
        datatypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDataType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        Spinner visualType = (Spinner) dialogView.findViewById(R.id.spinner4);
        visualType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVisualType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        return dialogView;
    }



}
