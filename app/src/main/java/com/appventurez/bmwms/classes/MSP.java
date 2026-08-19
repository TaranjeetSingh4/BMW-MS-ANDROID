package com.appventurez.bmwms.classes;

import android.content.Context;
import android.content.SharedPreferences;

public class MSP {

    private static Context ctx;
    private static MSP instance;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public MSP(Context context) {
        ctx = context;
        sharedPreferences = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized MSP getInstance(Context context){
        if (instance == null){
            instance = new MSP(context);
        }
        return instance;
    }

    public String getStringData(String s){
        return sharedPreferences.getString(s,null);
    }

    public void setStringData(String s,String s1){
        editor.putString(s,s1).apply();
    }

    public boolean containsData(String s){
        return sharedPreferences.contains(s);
    }

    public void removeAll(){
        editor.clear().apply();
    }

    public void removeData(String s){
        editor.remove(s).apply();
    }

}
