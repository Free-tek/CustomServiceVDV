package com.example.customservicechasisnumbercheck.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.customservicechasisnumbercheck.R;

public class CarDetailsActivity extends AppCompatActivity {

    private TextView BankPaidTo, TellerNo, CarOwner, ChaisNo, Color, DateOfReg, EngineCapacity, EngineNo, MakeOfVehicle, Model, NewRegistration,  OldRegistration, VehicleCategory,  VehicleStatus, YearOfManufacture  ;
    String $bankPaidTo, $carOwner, $tellerNo,$chasisNo, $color, $dateOfReg, $engineCapacity, $engineNo, $makeOfVehicle, $model, $newRegistration, $oldRegistration, $vehicleCategory, $vehicleStatus, $yearOfManufacture;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_details_activity);

            Bundle bundle = getIntent().getExtras();
            $bankPaidTo = bundle.getString("BankPaidTo");
            $carOwner = bundle.getString("CarOwner");
            $chasisNo = bundle.getString("ChasisNo");
            $tellerNo = bundle.getString("TellerNo");
            $color = bundle.getString("Color");
            $dateOfReg = bundle.getString("EngineCapacity");
            $engineCapacity = bundle.getString("BankPaidTo");
            $engineNo = bundle.getString("EngineNo");
            $makeOfVehicle = bundle.getString("MakeOfVehicle");
            $model = bundle.getString("Model");
            $newRegistration = bundle.getString("NewRegistration");
            $oldRegistration = bundle.getString("OldRegistration");
            $vehicleCategory = bundle.getString("VehicleCategory");
            $vehicleStatus = bundle.getString("VehicleStatus");
            $yearOfManufacture = bundle.getString("YearOfManufacture");


            initUI();


        }

    private void initUI() {
        BankPaidTo = (TextView) findViewById(R.id.bank);
        CarOwner = (TextView) findViewById(R.id.name);
        ChaisNo = (TextView) findViewById(R.id.chasisNo);
        TellerNo = (TextView) findViewById(R.id.teller);
        Color = (TextView) findViewById(R.id.car_color);
        DateOfReg = (TextView) findViewById(R.id.date_reg);
        EngineCapacity = (TextView) findViewById(R.id.e_capacity);
        EngineNo = (TextView) findViewById(R.id.e_no);
        MakeOfVehicle = (TextView) findViewById(R.id.car_make);
        Model = (TextView) findViewById(R.id.car_model);
        NewRegistration = (TextView) findViewById(R.id.new_reg);
        OldRegistration = (TextView) findViewById(R.id.old_reg);
        VehicleCategory = (TextView) findViewById(R.id.v_category);
        VehicleStatus = (TextView) findViewById(R.id.v_status);
        YearOfManufacture = (TextView) findViewById(R.id.year_manufacture);


        BankPaidTo.setText($bankPaidTo);
        CarOwner.setText($carOwner);
        ChaisNo.setText($chasisNo);
        TellerNo.setText($tellerNo);
        Color.setText($color);
        DateOfReg.setText($dateOfReg);
        EngineCapacity.setText($engineCapacity);
        EngineNo.setText($engineCapacity);
        MakeOfVehicle.setText($makeOfVehicle);
        Model.setText($model);
        NewRegistration.setText($newRegistration);
        OldRegistration.setText($oldRegistration);
        VehicleCategory.setText($vehicleCategory);
        VehicleStatus.setText($vehicleStatus);
        YearOfManufacture.setText($yearOfManufacture);

    }
}
