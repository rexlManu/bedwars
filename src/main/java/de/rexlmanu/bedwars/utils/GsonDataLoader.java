/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 18.06.2019 / 02:12                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public class GsonDataLoader<T> {

    private final File dataFile;
    private final Class dataClass;
    private final Timer timer = new Timer();
    private T gsonObject;

    public GsonDataLoader(File dataFile, Class<T> dataClass) {
        this.dataFile = dataFile;
        this.dataClass = dataClass;
    }

    public GsonDataLoader<T> enableAutoSaving(int interval) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                save();
            }
        }, interval, interval);
        return this;
    }

    public GsonDataLoader<T> disableAutoSaving() {
        timer.cancel();
        return this;
    }

    public GsonDataLoader<T> load() {
        try {
            if (! dataFile.exists()) {
                dataFile.mkdirs();
                dataFile.delete();
                gsonObject = (T) dataClass.newInstance();
                save();
            } else {
                read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public GsonDataLoader<T> save() {
        try {
            write();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public T getData() {
        return gsonObject;
    }

    private void write() throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dataFile));
        new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(gsonObject, bufferedWriter);
        bufferedWriter.close();
    }

    private void read() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile));
        gsonObject = (T) new Gson().fromJson(bufferedReader, dataClass);
        bufferedReader.close();
    }

}