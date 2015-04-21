package com.example.android.wifidirect.util;

import android.provider.ContactsContract;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataWrapper implements Serializable {
    private List<File> files;
    private LinkedHashMap<Long, Long> hashMap;

    public DataWrapper(List<File> data) {
        this.files = data;
    }

    public DataWrapper(LinkedHashMap<Long, Long> data) {
        this.hashMap = data;
    }

    public List<File> getFilesData() {
        return files;
    }

    public LinkedHashMap<Long, Long> getHashMap() {
        return hashMap;
    }
}
