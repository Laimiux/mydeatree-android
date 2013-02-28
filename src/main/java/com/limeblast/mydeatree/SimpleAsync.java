package com.limeblast.mydeatree;

import android.os.AsyncTask;
import android.os.Handler;

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 1/21/13
 * Time: 6:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleAsync extends AsyncTask<String, Integer, String> {
    @Override
    protected String doInBackground(String... strings) {
        // Moved to background thread



        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}

