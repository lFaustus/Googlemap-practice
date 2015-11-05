package com.example.volley;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Fluxi on 11/4/2015.
 */
public class VolleyConnection {
    private static VolleyConnection mInstance = null;
    private RequestQueue mRequestQueue;

    private VolleyConnection() {

        mRequestQueue = Volley.newRequestQueue(MyApplication.getAppContext());

    }

    public static VolleyConnection getInstance()
    {
        if(mInstance == null)
            mInstance = new VolleyConnection();
        return mInstance;
    }

    public RequestQueue getRequestQueue()
    {
        return mRequestQueue;
    }
}
