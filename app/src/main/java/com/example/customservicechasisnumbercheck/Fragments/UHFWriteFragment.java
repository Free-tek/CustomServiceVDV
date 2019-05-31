package com.example.customservicechasisnumbercheck.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;


import com.example.customservicechasisnumbercheck.Activities.MainActivity;
import com.example.customservicechasisnumbercheck.R;
import com.example.customservicechasisnumbercheck.Utils;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;
import com.rscja.utility.StringUtility;

public class UHFWriteFragment extends Fragment implements View.OnClickListener{

    private MainActivity mContext;
    Spinner SpinnerBank_Write;
    EditText EtPtr_Write;
    EditText EtLen_Write;
    EditText EtData_Write;
    EditText EtAccessPwd_Write;
    EditText etLen_filter_wt;

    Button BtWrite;

    CheckBox cb_filter_wt,cb_QT_W;
    EditText etPtr_filter_wt;
    EditText etData_filter_wt;
    RadioButton rbEPC_filter_wt;
    RadioButton rbTID_filter_wt;
    RadioButton rbUser_filter_wt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_uhfwrite, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        SpinnerBank_Write = (Spinner) getView().findViewById(R.id.SpinnerBank_Write);
        EtPtr_Write = (EditText) getView().findViewById(R.id.EtPtr_Write);
        EtLen_Write = (EditText) getView().findViewById(R.id.EtLen_Write);
        EtData_Write = (EditText) getView().findViewById(R.id.EtData_Write);
        EtAccessPwd_Write = (EditText) getView().findViewById(R.id.EtAccessPwd_Write);
        etLen_filter_wt= (EditText) getView().findViewById(R.id.etLen_filter_wt);
        BtWrite = (Button) getView().findViewById(R.id.BtWrite);

        cb_QT_W= (CheckBox) getView().findViewById(R.id.cb_QT_W);
        cb_filter_wt = (CheckBox) getView().findViewById(R.id.cb_filter_wt);
        etPtr_filter_wt = (EditText) getView().findViewById(R.id.etPtr_filter_wt);
        etData_filter_wt = (EditText) getView().findViewById(R.id.etData_filter_wt);
        rbEPC_filter_wt = (RadioButton) getView().findViewById(R.id.rbEPC_filter_wt);
        rbTID_filter_wt = (RadioButton) getView().findViewById(R.id.rbTID_filter_wt);
        rbUser_filter_wt = (RadioButton) getView().findViewById(R.id.rbUser_filter_wt);

        rbEPC_filter_wt.setOnClickListener(this);
        rbTID_filter_wt.setOnClickListener(this);
        rbUser_filter_wt.setOnClickListener(this);
        BtWrite.setOnClickListener(this);



        cb_filter_wt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    String data = etData_filter_wt.getText().toString().trim();
                    String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                    if(data==null || data.isEmpty() || !data.matches(rex)) {
                        Toast.makeText(mContext,"过滤的数据必须是十六进制数据", Toast.LENGTH_SHORT).show();
                        cb_filter_wt.setChecked(false);
                        return;
                    }
                }
            }
        });
        SpinnerBank_Write.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String element = adapterView.getItemAtPosition(i).toString();// 得到spanner的值
                if(element.equals("EPC")){
                    EtPtr_Write.setText("2");
                }else{
                    EtPtr_Write.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.rbEPC_filter_wt:
                etPtr_filter_wt.setText("32");
                break;
            case  R.id.rbTID_filter_wt:
                etPtr_filter_wt.setText("0");
                break;
            case  R.id.rbUser_filter_wt:
                etPtr_filter_wt.setText("0");
                break;
            case R.id.BtWrite:
                write();
                break;
        }
    }
    private void write(){
        String strPtr = EtPtr_Write.getText().toString().trim();
        if (strPtr==null || strPtr.isEmpty()) {
            Toast.makeText(mContext, R.string.uhf_msg_addr_not_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (!StringUtility.isDecimal(strPtr)) {
            Toast.makeText(mContext, R.string.uhf_msg_addr_must_decimal, Toast.LENGTH_SHORT).show();
            return;
        }

        String strPWD = EtAccessPwd_Write.getText().toString().trim();// 访问密码
        if (strPWD==null || strPWD.isEmpty()) {
            strPWD = "00000000";
        }
        if (strPWD.length() != 8) {
            Toast.makeText(mContext, R.string.uhf_msg_addr_must_len8, Toast.LENGTH_SHORT).show();
            return;
        } else if (!Utils.vailHexInput(strPWD)) {
            Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
            return;
        }


        String strData = EtData_Write.getText().toString().trim();// 要写入的内容
        if (strData==null || strData.isEmpty()) {
            Toast.makeText(mContext, R.string.uhf_msg_write_must_not_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (!Utils.vailHexInput(strData)) {
            Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
            return;
        }

        // 多字单次
        String cntStr = EtLen_Write.getText().toString().trim();
        if (cntStr==null || cntStr.isEmpty()) {
            Toast.makeText(mContext, R.string.uhf_msg_len_not_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (!StringUtility.isDecimal(cntStr)) {
            Toast.makeText(mContext, R.string.uhf_msg_len_must_decimal, Toast.LENGTH_SHORT).show();
            return;
        }

        if ((strData.length()) % 4 != 0) {
            Toast.makeText(mContext, R.string.uhf_msg_write_must_len4x, Toast.LENGTH_SHORT).show();
            return;
        } else if (!Utils.vailHexInput(strData)) {
            Toast.makeText(mContext, R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
            return;
        }


        boolean result=false;
        String Bank="";
        Bank=SpinnerBank_Write.getSelectedItem().toString();
        if (cb_filter_wt.isChecked())// 指定标签
        {
            if(etPtr_filter_wt.getText().toString()==null || etPtr_filter_wt.getText().toString().isEmpty()){
                etPtr_filter_wt.setText("0");
            }
            if(etLen_filter_wt.getText().toString()==null || etLen_filter_wt.getText().toString().isEmpty()){
                Toast.makeText(mContext, "过滤数据长度不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            int filterPtr= Integer.parseInt(etPtr_filter_wt.getText().toString());
            String filterData=etData_filter_wt.getText().toString();
            int filterCnt= Integer.parseInt(etLen_filter_wt.getText().toString());
            String filterBank="EPC";
            if(rbEPC_filter_wt.isChecked()){
                filterBank="EPC";
            }else if(rbTID_filter_wt.isChecked()){
                filterBank="TID";
            }else if(rbUser_filter_wt.isChecked()){
                filterBank="USER";
            }
            boolean r=false;
            r=mContext.uhf.writeData(strPWD,
                    RFIDWithUHFBluetooth.BankEnum.valueOf(filterBank),
                    filterPtr,
                    filterCnt,
                    filterData,
                    RFIDWithUHFBluetooth.BankEnum.valueOf(Bank),
                    Integer.parseInt(strPtr),
                    Integer.parseInt(cntStr),
                    strData
            );

            if(r){
                result=true;
            }else{
                result=false;
            }

        } else {
            boolean r=false;
            r= mContext.uhf.writeData(strPWD,
                    RFIDWithUHFBluetooth.BankEnum.valueOf(Bank),
                    Integer.parseInt(strPtr),
                    Integer.valueOf(cntStr), strData);

            if (r) {
                result=true;
                Toast.makeText(mContext, R.string.uhf_msg_write_succ, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, R.string.uhf_msg_write_fail, Toast.LENGTH_SHORT).show();
            }
        }
        if(result){
            Utils.playSound(1);
        }else{
            Utils.playSound(2);
        }
    }
}
