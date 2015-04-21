package com.example.android.wifidirect.file_manager;

import android.app.ListActivity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.wifidirect.R;
import com.example.android.wifidirect.util.DataWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChooser extends ListActivity {
    private File currentDir;
    private String prevDir;

    private ListView listView;
    private FileArrayAdapter adapter;

    private MenuItem menuItem;

    public static List<File> filesToSend = new ArrayList<File>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listView = this.getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " Selected");
                adapter.toggleSelection(position, menuItem);

                getActionBar().setDisplayHomeAsUpEnabled(true);

                Toast.makeText(getApplicationContext(), "" + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.selected_items_menu, menu);
                menuItem = menu.findItem(R.id.send_files);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.send_files:
                        filesToSend.clear();
                        SparseBooleanArray selected = adapter.getSelectedIDs();

                        for(int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                filesToSend.add(adapter.getItem(selected.keyAt(i)));
                            }
                        }

                        Intent intent = new Intent();
                        intent.putExtra("files_to_send", new DataWrapper(filesToSend));
                        setResult(RESULT_OK, intent);

                        mode.finish();
                        finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.removeSelection();
            }
        });

        currentDir = new File("/sdcard/");
        fill(currentDir);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if (prevDir.equalsIgnoreCase("/")) {
                    Intent upIntent = getParentActivityIntent();
                    if (shouldUpRecreateTask(upIntent)) {
                        TaskStackBuilder.create(getApplicationContext()).addNextIntentWithParentStack(upIntent).startActivities();
                    } else {
                        navigateUpTo(upIntent);
                    }

                    return true;
                } else {
                    currentDir = new File(prevDir);
                    fill(new File(prevDir));

                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void fill(File f) {
        File[]dirs = f.listFiles();
        this.setTitle("Current Dir: "+f.getName());
        List<File> dir = new ArrayList<File>();
        List<File> fls = new ArrayList<File>();
        try{
            for(File ff: dirs)
            {
                if(ff.isDirectory()){
                    dir.add(ff);
                }
                else
                {
                    fls.add(ff);
                }
            }
        } catch(Exception e) { }

        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);

        prevDir = f.getParent();

        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_exploler, dir);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        File o = adapter.getItem(position);

        filesToSend.clear();

        //if (o.getType().equalsIgnoreCase("dir") || o.getType().equalsIgnoreCase("up")) {
        if (o.isDirectory()) {
            //prevDir = currentDir;
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else {
            filesToSend.add(o);
            Intent intent = new Intent();
            intent.putExtra("files_to_send", new DataWrapper(filesToSend));
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
