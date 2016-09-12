package com.jsc0.dashboardabroad;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity implements DashboardView.DegreeCallback {

    private DashboardView dashboardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dashboardView = (DashboardView)findViewById(R.id.dashboardView);
        dashboardView.setCallback(this);//seekbar.innerRadius
    }

    @Override
    public void Degree(int degree) {
        Log.i("Degree: ", degree+"");
//        dashboardView.setRealTimeValue(degree-1);
//        dashboardView.postInvalidate();
    }
}
