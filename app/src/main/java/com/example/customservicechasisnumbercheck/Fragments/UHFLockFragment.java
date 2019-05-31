package com.example.customservicechasisnumbercheck.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.customservicechasisnumbercheck.Activities.MainActivity;
import com.example.customservicechasisnumbercheck.R;
import com.example.customservicechasisnumbercheck.Utils;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;


public class UHFLockFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "UHFLockFragment";
    private MainActivity mContext;
    EditText EtAccessPwd_Lock;
    Button btnLock;
    EditText etLockCode;
    CheckBox cb_filter_lock;
    EditText etPtr_filter_lock;
    EditText etLen_filter_lock;
    EditText etData_filter_lock;
    RadioButton rbEPC_filter_lock;
    RadioButton rbTID_filter_lock;
    RadioButton rbUser_filter_lock;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_uhflock, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        etLockCode = (EditText) getView().findViewById(R.id.etLockCode);
        EtAccessPwd_Lock = (EditText) getView().findViewById(R.id.EtAccessPwd_Lock);
        btnLock = (Button) getView().findViewById(R.id.btnLock);

        etPtr_filter_lock = (EditText) getView().findViewById(R.id.etPtr_filter_lock);
        etLen_filter_lock = (EditText) getView().findViewById(R.id.etLen_filter_lock);

        rbEPC_filter_lock= (RadioButton) getView().findViewById(R.id.rbEPC_filter_lock);
        rbTID_filter_lock= (RadioButton) getView().findViewById(R.id.rbTID_filter_lock);
        rbUser_filter_lock= (RadioButton) getView().findViewById(R.id.rbUser_filter_lock);

        cb_filter_lock = (CheckBox) getView().findViewById(R.id.cb_filter_lock);
        etData_filter_lock = (EditText) getView().findViewById(R.id.etData_filter_lock);

        rbEPC_filter_lock.setOnClickListener(this);
        rbTID_filter_lock.setOnClickListener(this);
        rbUser_filter_lock.setOnClickListener(this);
        btnLock.setOnClickListener(this);


        cb_filter_lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    String data = etData_filter_lock.getText().toString().trim();
                    String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                    if(data==null || data.isEmpty() || !data.matches(rex)) {
                        Toast.makeText(mContext,"过滤的数据必须是十六进制数据", Toast.LENGTH_SHORT).show();
                        cb_filter_lock.setChecked(false);
                        return;
                    }
                }
            }
        });

        etLockCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.tvLockCode);
                final View vv = LayoutInflater.from(mContext).inflate(R.layout.uhf_dialog_lock_code, null);
                builder.setView(vv);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        etLockCode.getText().clear();
                    }
                });

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RadioButton rbOpen = (RadioButton) vv.findViewById(R.id.rbOpen);
                        RadioButton rbLock= (RadioButton) vv.findViewById(R.id.rbLock);
                        CheckBox cbPerm = (CheckBox) vv.findViewById(R.id.cbPerm);

                        CheckBox cbKill = (CheckBox) vv.findViewById(R.id.cbKill);
                        CheckBox cbAccess = (CheckBox) vv.findViewById(R.id.cbAccess);
                        CheckBox cbEPC = (CheckBox) vv.findViewById(R.id.cbEPC);
                        CheckBox cbTid = (CheckBox) vv.findViewById(R.id.cbTid);
                        CheckBox cbUser = (CheckBox) vv.findViewById(R.id.cbUser);
                        String mask = "";
                        String value = "";
                        int[] data=new int[20];
                        if(cbUser.isChecked()) {
                            data[11] = 1;
                            if (cbPerm.isChecked()) {
                                data[0] = 1;
                                data[10] = 1;
                            }
                            if(rbLock.isChecked()){
                                data[1]=1;
                            }
                        }
                        if(cbTid.isChecked()){
                            data[13]=1;
                            if(cbPerm.isChecked()){
                                data[12]=1;
                                data[2]=1;
                            }
                            if(rbLock.isChecked()){
                                data[3]=1;
                            }
                        }
                        if(cbEPC.isChecked()){
                            data[15]=1;
                            if(cbPerm.isChecked()){
                                data[14]=1;
                                data[4]=1;
                            }
                            if(rbLock.isChecked()){
                                data[5]=1;
                            }
                        }
                        if(cbAccess.isChecked()){
                            data[17]=1;
                            if(cbPerm.isChecked()){
                                data[16]=1;
                                data[6]=1;
                            }
                            if(rbLock.isChecked()){
                                data[7]=1;
                            }
                        }
                        if(cbKill.isChecked()){
                            data[19]=1;
                            if(cbPerm.isChecked()){
                                data[18]=1;
                                data[8]=1;
                            }
                            if(rbLock.isChecked()){
                                data[9]=1;
                            }
                        }
                        StringBuffer stringBuffer=new StringBuffer();
                        stringBuffer.append("0000");
                        for(int k=data.length-1;k>=0;k--){
                            stringBuffer.append(data[k]+"");
                        }

                        String code =binaryString2hexString(stringBuffer.toString());
                        Log.i(TAG, "  tempCode="+stringBuffer.toString()+"  code=" + code);

                        etLockCode.setText(code.replace(" ", "0") + "");
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.rbEPC_filter_lock:
                etPtr_filter_lock.setText("32");
                break;
            case  R.id.rbTID_filter_lock:
                etPtr_filter_lock.setText("0");
                break;
            case  R.id.rbUser_filter_lock:
                etPtr_filter_lock.setText("0");
                break;
            case  R.id.btnLock:
                lock();
                break;
        }
    }

    public void lock(){
        String strPWD = EtAccessPwd_Lock.getText().toString().trim();// 访问密码
        String strLockCode = etLockCode.getText().toString().trim();

        if (!TextUtils.isEmpty(strPWD)) {
            if (strPWD.length() != 8) {
                Toast.makeText(mContext,R.string.uhf_msg_addr_must_len8, Toast.LENGTH_SHORT).show();
                return;
            } else if (!Utils.vailHexInput(strPWD)) {
                Toast.makeText(mContext,R.string.rfid_mgs_error_nohex, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(mContext,R.string.rfid_mgs_error_nopwd, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(strLockCode)) {
            Toast.makeText(mContext,R.string.rfid_mgs_error_nolockcode, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean result=false;
        if(cb_filter_lock.isChecked()){
            String filterData=etData_filter_lock.getText().toString();
            if(filterData==null || filterData.isEmpty()){
                Toast.makeText(mContext, "过滤数据不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if(etPtr_filter_lock.getText().toString()==null || etPtr_filter_lock.getText().toString().isEmpty()){
                Toast.makeText(mContext, "过滤起始地址不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if(etLen_filter_lock.getText().toString()==null || etLen_filter_lock.getText().toString().isEmpty()){
                Toast.makeText(mContext, "过滤数据长度不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            int filterPtr= Integer.parseInt(etPtr_filter_lock.getText().toString());
            int filterCnt= Integer.parseInt(etLen_filter_lock.getText().toString());
            String filterBank="EPC";
            if(rbEPC_filter_lock.isChecked()){
                filterBank="EPC";
            }else if(rbTID_filter_lock.isChecked()){
                filterBank="TID";
            }else if(rbUser_filter_lock.isChecked()){
                filterBank="USER";
            }

            if (mContext.uhf.lockMem(strPWD,
                    RFIDWithUHFBluetooth.BankEnum.valueOf(filterBank),
                    filterPtr,
                    filterCnt,
                    filterData,
                    strLockCode)) {
                result=true;
                Toast.makeText(mContext,R.string.rfid_mgs_lock_succ, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext,R.string.rfid_mgs_lock_fail, Toast.LENGTH_SHORT).show();
            }
        }else{
            if (mContext.uhf.lockMem(strPWD, strLockCode)) {
                result=true;
                Toast.makeText(mContext,R.string.rfid_mgs_lock_succ, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext,R.string.rfid_mgs_lock_fail, Toast.LENGTH_SHORT).show();
            }
        }
        if(!result){
            Utils.playSound(2);
        }else{
            Utils.playSound(1);
        }

    }
    public static String binaryString2hexString(String bString)
    {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4)
        {
            iTmp = 0;
            for (int j = 0; j < 4; j++)
            {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }
}
