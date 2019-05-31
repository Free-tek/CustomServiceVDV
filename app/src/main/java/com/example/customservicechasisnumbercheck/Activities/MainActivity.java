package com.example.customservicechasisnumbercheck.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.customservicechasisnumbercheck.Fragments.UHFReadFragment;
import com.example.customservicechasisnumbercheck.R;
import com.example.customservicechasisnumbercheck.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private BluetoothAdapter mBtAdapter = null;
    private Boolean connectionStatus;

    public boolean scaning=false;
    private  String $battery;

    BTStatus btStatus=new BTStatus();


    public String remoteBTName="";
    public String remoteBTAdd="";

    private final static String TAG = "MainActivity111";


    public RFIDWithUHFBluetooth uhf=RFIDWithUHFBluetooth.getInstance();

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;

    private BluetoothDevice mDevice = null;
    private TextView tvAddress, battery;
    private Button btn_connect, btn_search;
    private Menu menu;
    // private MenuItem connect;

    private ProgressDialog progress;

    private boolean navigationConnectText;
    private String button;

    private FragmentManager fm;

    Class<UHFReadFragment>ii=UHFReadFragment.class;

      private FirebaseFirestore db = FirebaseFirestore.getInstance();
      String $bankPaidTo, $carOwner, $tellerNo, $chasisNo, $color, $dateOfReg, $engineCapacity, $engineNo, $makeOfVehicle, $model, $newRegistration, $oldRegistration, $vehicleCategory, $vehicleStatus, $yearOfManufacture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        navigationConnectText = true;
        initUI();
        uhf.init(this);
        Utils.initSound(this);
        checkLocationEnable();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uhf.getConnectStatus()!= RFIDWithUHFBluetooth.StatusEnum.CONNECTED){

                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Oops..");
                    alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
                    alertDialog.setMessage( "Please pair the android to the handheld device first");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();
                }else{
                    final String data;
                    data = MainActivity.this.uhf.readData("00000000",
                            RFIDWithUHFBluetooth.BankEnum.valueOf("RESERVED"),
                            Integer.parseInt("0"),
                            Integer.parseInt("4"));

                    if(data!=null && data.length()>0) {

                        progress = ProgressDialog.show(MainActivity.this, "Loading", "Please wait...", true);
                        progress.setCancelable(true);

                        DocumentReference documentReference = db.collection("users").document(data);
                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {


                                    $bankPaidTo = documentSnapshot.getString("BankPaidTo");
                                    $carOwner = documentSnapshot.getString("CarOwner");
                                    $chasisNo = documentSnapshot.getString("ChasisNo");
                                    $tellerNo = documentSnapshot.getString("TellerNo");
                                    $color = documentSnapshot.getString("Color");
                                    $dateOfReg = documentSnapshot.getString("DateOfReg");
                                    $engineCapacity = documentSnapshot.getString("EngineCapacity");
                                    $engineNo = documentSnapshot.getString("EngineNo");
                                    $makeOfVehicle = documentSnapshot.getString("MakeOfVehicle");
                                    $model = documentSnapshot.getString("Model");
                                    $newRegistration = documentSnapshot.getString("NewRegistration");
                                    $oldRegistration = documentSnapshot.getString("OldRegistration");
                                    $vehicleCategory = documentSnapshot.getString("VehicleCategory");
                                    $vehicleStatus = documentSnapshot.getString("VehicleStatus");
                                    $yearOfManufacture = documentSnapshot.getString("YearOfManufacture");

                                    Intent intent = new Intent(MainActivity.this, CarDetailsActivity.class);

                                    intent.putExtra("BankPaidTo", $bankPaidTo );
                                    intent.putExtra("TellerNo", $tellerNo );
                                    intent.putExtra("CarOwner", $carOwner );
                                    intent.putExtra("ChasisNo", $chasisNo );
                                    intent.putExtra("Color", $color );
                                    intent.putExtra("DateOfReg", $dateOfReg );
                                    intent.putExtra("EngineCapacity", $engineCapacity );
                                    intent.putExtra("EngineNo", $engineNo );
                                    intent.putExtra("MakeOfVehicle",$makeOfVehicle );
                                    intent.putExtra("Model", $model );
                                    intent.putExtra("NewRegistration", $newRegistration );
                                    intent.putExtra("OldRegistration", $oldRegistration );
                                    intent.putExtra("VehicleCategory", $vehicleCategory );
                                    intent.putExtra("VehicleStatus", $vehicleStatus );
                                    intent.putExtra("YearOfManufacture", $yearOfManufacture );
                                    progress.dismiss();
                                    startActivity(intent);

                                } else {
                                    //document does not exist
                                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                    alertDialog.setTitle("Oops..");
                                    alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
                                        alertDialog.setMessage( "Chasis number details does not exist");
                                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });

                                    alertDialog.show();

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT);
                            }
                        });
                    } else if(data == null){
                        //data is null so this means no tag was read
                        Intent intent = new Intent(MainActivity.this, NotInRange.class);
                        startActivity(intent);

                    }


                }

            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(this);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    protected void initUI() {
        tvAddress=(TextView)findViewById(R.id.tvAddress);
        btn_connect=(Button)findViewById(R.id.btn_connect);
        btn_search=(Button)findViewById(R.id.btn_search);


        btn_connect.setOnClickListener(MainActivity.this);
        btn_search.setOnClickListener(MainActivity.this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        fm = getSupportFragmentManager();


        if(uhf.getConnectStatus()== RFIDWithUHFBluetooth.StatusEnum.CONNECTED){
            btn_search.setVisibility(View.INVISIBLE);
            btn_connect.setText("Disconnect");
        }

    }

    class BTStatus implements  RFIDWithUHFBluetooth.BTStatusCallback{
        @Override
        public void getStatus(final RFIDWithUHFBluetooth.StatusEnum statusEnum,final BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    remoteBTName = "";
                    remoteBTAdd = "";
                    if(statusEnum==RFIDWithUHFBluetooth.StatusEnum.CONNECTED){
                        SystemClock.sleep(500);
                        btn_connect.setText(MainActivity.this.getString(R.string.disConnect));
                        btn_search.setVisibility(View.INVISIBLE);
                        remoteBTName = device.getName();
                        remoteBTAdd = device.getAddress();
                        tvAddress.setText("Connected to " + remoteBTName /*+ " - " + remoteBTAdd*/);
                        Toast.makeText(MainActivity.this, getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
                        if (iConnectStatus != null) {
                            iConnectStatus.getStatus(statusEnum);
                        }
                    }else if(statusEnum==RFIDWithUHFBluetooth.StatusEnum.DISCONNECTED){
                        btn_connect.setText(MainActivity.this.getString(R.string.Connect));
                        if(device!=null) {
                            remoteBTName = device.getName();
                            remoteBTAdd = device.getAddress();
                            tvAddress.setText("Connected to" + remoteBTAdd);
                        }else{
                            tvAddress.setText("Not Connected");
                        }
                        Toast.makeText(MainActivity.this,getString(R.string.disconnect),Toast.LENGTH_SHORT).show();
                        if (iConnectStatus != null) {
                            iConnectStatus.getStatus(statusEnum);
                        }
                    }
                }
            });

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    tvAddress.setText(mDevice.getName()+"("+deviceAddress+")");
                    connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public void updataUI(String oldName,String newName){
        if(oldName!=null && !oldName.isEmpty() && newName!=null && !newName.isEmpty()) {
            tvAddress.setText(tvAddress.getText().toString().replace(oldName, newName));
            remoteBTName=newName;
        }
    }


    //---------------------------------------------------------
    IConnectStatus iConnectStatus=null;
    public  void setConnectStatusNotice(IConnectStatus iConnectStatus){
        this.iConnectStatus=iConnectStatus;
    }
    public interface IConnectStatus {
        public void getStatus(RFIDWithUHFBluetooth.StatusEnum  statusEnum);
    }

    private void connect(String deviceAddress){
        if(uhf.getConnectStatus()== RFIDWithUHFBluetooth.StatusEnum.CONNECTING) {
            Toast.makeText(this,getString(R.string.connecting),Toast.LENGTH_SHORT).show();
        } else {
            uhf.connect(deviceAddress, btStatus);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private  void scanBluetoothDevice(){
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (btn_connect.getText().equals(this.getString(R.string.disConnect))){
                uhf.disconnect();
            }
            Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
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

        if (id == R.id.battery) {
            if(uhf.getConnectStatus()!= RFIDWithUHFBluetooth.StatusEnum.CONNECTED){
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Oops..");
                alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
                alertDialog.setMessage( "Please pair the android to the handheld device first");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                alertDialog.show();
            }else{
                int mbattery = uhf.getBattery();
                if(!scaning){
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Battery");
                    alertDialog.setIcon(R.drawable.battery);
                    alertDialog.setMessage( "" +mbattery + " %");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();
                }

            }

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_connect:
                if(btn_connect.getText().equals(this.getString(R.string.Connect))) {
                    scanBluetoothDevice();
                    btn_search.setVisibility(View.VISIBLE);
                }else{
                    uhf.disconnect();
                    Toast.makeText(this,"Bluetooth Disconnected from Device",Toast.LENGTH_SHORT).show();
                    btn_connect.setText("Connect");
                    btn_search.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_search:
                if(scaning){
                    Toast.makeText(this,getString(R.string.title_stop_read_card),Toast.LENGTH_SHORT).show();
                    return;
                }
                scanBluetoothDevice();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        uhf.free();
        Utils.freeSound();
        super.onDestroy();
        android.os.Process.killProcess(Process.myPid());
    }


    private boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


    private static final int ACCESS_FINE_LOCATION_PERMISSION_REQUEST = 100;
    private static final int REQUEST_ACTION_LOCATION_SETTINGS = 3;

    private void checkLocationEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_REQUEST);
            }
        }
        if (!isLocationEnabled()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, REQUEST_ACTION_LOCATION_SETTINGS);
        }
    }


}
