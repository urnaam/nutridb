package com.codebionic.android.nutridb;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.Vector;
    
public class NutriInstaller extends Activity
{

    // The shared path to all app expansion files
    private final static String EXP_PATH = "/Android/obb/";
    private final static int MAINVER = 1;
    private final static int PATCHVER = 0;

    static public String[] getAPKExpansionFiles(Context ctx) {

        int mainVersion = MAINVER;
        int patchVersion = PATCHVER;
        String packageName = ctx.getPackageName();
        Vector<String> ret = new Vector<String>();

        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // Build the full path to the app's expansion files
            File root = Environment.getExternalStorageDirectory();
            File expPath = new File(root.toString() + EXP_PATH + packageName);

            // Check that expansion file path exists
            if (expPath.exists()) {
                if ( mainVersion > 0 ) {
                    String strMainPath = expPath + File.separator + "main." +
                        mainVersion + "." + packageName + ".obb";
                    File main = new File(strMainPath);
                    if ( main.isFile() ) {
                        ret.add(strMainPath);
                    }
                }
                if ( patchVersion > 0 ) {
                    String strPatchPath = expPath + File.separator + "patch." +
                            mainVersion + "." + packageName + ".obb";
                    File main = new File(strPatchPath);
                    if ( main.isFile() ) {
                            ret.add(strPatchPath);
                    }
                }
            }
        }

        String[] retArray = new String[ret.size()];
        ret.toArray(retArray);
        return retArray;
    } 


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
