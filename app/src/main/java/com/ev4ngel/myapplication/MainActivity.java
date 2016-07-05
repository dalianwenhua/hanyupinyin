package com.ev4ngel.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import dji.sdk.Battery.DJIBattery;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Camera.DJICameraParameters;
import dji.sdk.Camera.DJICameraSettingsDef;
import dji.sdk.Camera.DJIMedia;
import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.Gimbal.DJIGimbal;
import dji.sdk.MissionManager.DJICustomMission;
import dji.sdk.MissionManager.DJIMission;
import dji.sdk.MissionManager.DJIMissionManager;
import dji.sdk.MissionManager.MissionStep.DJIFollowmeMissionStep;
import dji.sdk.MissionManager.MissionStep.DJIGoToStep;
import dji.sdk.MissionManager.MissionStep.DJIMissionStep;
import dji.sdk.SDKManager.DJISDKManager;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DJIBattery.DJIBatteryStateUpdateCallback,
        DJIBaseComponent.DJICompletionCallback,
        DJICamera.CameraGeneratedNewMediaFileCallback,
        Toolbar.OnMenuItemClickListener,
        DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback,
        View.OnClickListener
{
    private DJIGimbal gimbal=null;
    private DJICamera camera=null;
    private DJIBattery battery=null;
    private DJIFlightController flightController=null;
    private DJIMissionManager mMissonManager=null;
    private DJICustomMission mMission=null;
    private ArrayList<DJIFlightControllerDataType.DJILocationCoordinate3D> missionLocations;
    private View projectPage;//project页面
    private View airlinePage;//航线页面
    private View settingPage;//设置页面
    private View camOrMapPage;//相机/地图页面
    private TextView gpsCountView;//gps数量显示
    private TextView batteryRemainView;//电池电量显示
    private TextView batteryVolView;//
    private TextView velX;
    private TextView velY;
    private TextView velZ;
    private TextView posLat;//
    private TextView posLng;
    private TextView posHeight;//
    private TextView attitudeView;//
    private TextView batteryTempView;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //refreshUI();
            if(AutoflyApplication.isAircraftConnected()){
                Tools.showToast(getApplicationContext(),"连接成功！");
                getCamera();
                getGimbal();
                getBattery();
                getFlightController();
                registerListener();
             }
        }
    };
    public void registerListener(){
        battery.setBatteryStateUpdateCallback(this);
        camera.setDJICameraGeneratedNewMediaFileCallback(this);
        flightController.setUpdateSystemStateCallback(this);
    }
    /*
    private void refreshUI(){
        Tools.showToast(getApplicationContext(), "Connection successfully");
    }
    */
    private DJIBaseProduct getProduct()
    {
        try {
            return AutoflyApplication.getProductInstance();
        }catch(Exception e)
        {
            Tools.showToast(getApplicationContext(),e.getMessage());
        }
        return null;
    }
    private void getGimbal()
    {
        if( AutoflyApplication.isAircraftConnected())
        {
            gimbal = getProduct().getGimbal();
            if (gimbal == null) {
                Tools.showToast(getApplicationContext(), "Init Gimbal Fail");
            }
            else{
                Tools.showToast(getApplicationContext(),"Get Gimbal Successful");
            }
        }
        else {
            Tools.showToast(getApplicationContext(),"Get Gimbal Fail");
        }
    }
    private void getCamera()
    {
        if(AutoflyApplication.isAircraftConnected())
        {
            camera = getProduct().getCamera();
            if (camera == null) {
                Tools.showToast(getApplicationContext(), "Init Camera Fail");
            }
            Tools.showToast(getApplicationContext(),"Camera successful");
    }
    else {
            Tools.showToast(getApplicationContext(),"Camera Fail");
        }
    }
    private void getBattery()
    {
        if(AutoflyApplication.isAircraftConnected())
        {
            battery = getProduct().getBattery();
            if (battery == null) {
                Tools.showToast(getApplicationContext(), "Init Battery Fail");
            }
            Tools.showToast(getApplicationContext(),"Battery successful");
        }
        else {
            Tools.showToast(getApplicationContext()," Battery Fail");
        }
    }
    private void getFlightController()
    {
            if (AutoflyApplication.getAircraftInstance() != null) {
                flightController = AutoflyApplication.getAircraftInstance().getFlightController();
                if (flightController == null) {
                    Tools.showToast(getApplicationContext(), "Init flightc Fail");
                } else {
                    Tools.showToast(getApplicationContext(), "Get flightc Successful");
                }
            } else {
                Tools.showToast(getApplicationContext(), "Get flightc Fail");
            }
    }



    private void generateMissions()
    {
        DJIFlightControllerDataType.DJILocationCoordinate3D loc;
        ArrayList<DJIMissionStep> steps=new ArrayList<DJIMissionStep>() ;
        for(int i=0;i<missionLocations.size();i++) {
            DJIGoToStep gotoStep = new DJIGoToStep(1,1,1,this);
            steps.add(gotoStep);
        }
        //mMissonManager.prepareMission(new DJICustomMission(steps),null,this);

    }
    private void initUi()
    {
        projectPage=findViewById(R.id.project);
        airlinePage=findViewById(R.id.airline);//).setVisibility(View.GONE);
        camOrMapPage=findViewById(R.id.cam_or_map);//).setVisibility(View.GONE);
        settingPage=findViewById(R.id.setting);//).setVisibility(View.GONE);
        gpsCountView=(TextView)findViewById(R.id.gps_count);
        batteryRemainView=(TextView)findViewById(R.id.battery_view);
        batteryVolView=(TextView)findViewById(R.id.battery_vol_view);
        batteryTempView=(TextView)findViewById(R.id.battery_temp_view);
    }
    private void initParams()
    {
        missionLocations=new ArrayList<DJIFlightControllerDataType.DJILocationCoordinate3D>();
        mMissonManager=DJIMissionManager.getInstance();
        getGimbal();
        getCamera();
        getBattery();
        initUi();

    }
    private void setPageInvisibility()
    {
        projectPage.setVisibility(View.GONE);
        airlinePage.setVisibility(View.GONE);
        camOrMapPage.setVisibility(View.GONE);
        settingPage.setVisibility(View.GONE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.getChildAt(0);
        initParams();
        setPageInvisibility();//设置所有page不可见
        projectPage.setVisibility(View.VISIBLE);//将project设置可见

        IntentFilter filter = new IntentFilter();
        filter.addAction(AutoflyApplication.FLAG_CONNECTION_CHANGE);
        getApplicationContext().registerReceiver(mReceiver, filter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        /*new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //ArrayList<WayPoint> a=apf.WayPoints;

        /*String rlt="[{\"Position\" : {\"Latitude\" : 23,\"Longitude\" : 123,\"Altitude\" : 23},\"Heading\" : 111,\"StartTime\" : \"2016-7-1 11:46:33\",\"StopTime\" : \"2016-7-1 11:46:40\",\"Photos\" : [{\"Name\" : \"DJI_111.jpg\",\"Yaw\" : 1,\"Pitch\" : 3},{\"Name\" : \"DJI_113.jpg\",\"Yaw\" : 1,\"Pitch\" : 2}, {\"Name\" : \"DJI_115.jpg\",\"Yaw\" : 1,\"Pitch\" : 4}]},{\"Position\" : {\"Latitude\" : 11,\"Longitude\" : 21,\"Altitude\" : 23},\"Heading\" : 111,\"StartTime\" : \"2016-7-1 11:45:30\",\"StopTime\" : \"2016-7-1 11:45:40\",\"Photos\" : [{\"Name\" : \"DJI_1133.jpg\",\"Yaw\" : 2,\"Pitch\" : 34},{\"Name\" : \"DJI_1133.jpg\",\"Yaw\" : 2,\"Pitch\" : 34},{\"Name\" : \"DJI_1133.jpg\",\"Yaw\" : 2,\"Pitch\" : 34},{\"Name\" : \"DJI_1133.jpg\",\"Yaw\" : 2,\"Pitch\" : 34},{\"Name\" : \"DJI_1133.jpg\",\"Yaw\" : 2,\"Pitch\" : 34},{\"Name\" : \"DJI_1133.jpg\",\"Yaw\" : 2,\"Pitch\" : 34},{\"Name\" : \"DJI_1133.jpg\",\"Yaw\" : 2,\"Pitch\" : 34}]}]";
        try {
            JSONArray jA = new JSONArray(new JSONTokener(rlt));
            for(int i=0;i<jA.length();i++)
            {
                WayPoint wp=new WayPoint(getApplicationContext(),(JSONObject)jA.get(i));
                Toast.makeText(getApplicationContext(), "Lat:"+wp.lat+",st:"+wp.startTime,Toast.LENGTH_LONG).show();;
            }

        }catch(JSONException e)
        {
            Toast.makeText(getApplicationContext(),"F",Toast.LENGTH_LONG).show();
        }
        */
        //int len=a.size();
        //Toast.makeText(getApplicationContext(),apf.WayPoints.get(0).name,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_project) {
            // Handle the camera action
            setPageInvisibility();
            projectPage.setVisibility(View.VISIBLE);

        } else if (id == R.id.nav_airline) {
            setPageInvisibility();
            airlinePage.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_cam_or_map) {
            setPageInvisibility();
           camOrMapPage.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_settings) {
            setPageInvisibility();
            settingPage.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResult(DJIBattery.DJIBatteryState state) {
        batteryVolView.setText(state.getCurrentVoltage()+"mV");
        batteryRemainView.setText(state.getBatteryEnergyRemainingPercent()*100+"%");
        batteryTempView.setText(state.getBatteryTemperature()+"C");
        Tools.showToast(getApplicationContext(),""+state.getBatteryEnergyRemainingPercent()+","+state.getCurrentVoltage());
    }

    @Override
    public void onResult(DJIError djiError) {
        Tools.showToast(getApplicationContext(),djiError.getDescription());
    }

    @Override
    public void onResult(DJIMedia djiMedia) {

    }

    @Override
    protected void onDestroy() {
        getApplicationContext().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        String msg = "";
        switch (item.getItemId()) {
            case R.id.action_settings:
                msg += "Click setting";
                break;
        }

        if(!msg.equals("")) {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState state) {
        gpsCountView.setText(""+state.getSatelliteCount());

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fab) {
            if (AutoflyApplication.isAircraftConnected()&&camera!=null)
            {
                camera.startShootPhoto(DJICameraSettingsDef.CameraShootPhotoMode.Single,this);
            }
            else
            {
                Tools.showToast(getApplicationContext(),"Camera Fail");
            }
        }
    }
}