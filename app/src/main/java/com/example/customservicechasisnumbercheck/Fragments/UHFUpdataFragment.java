package com.example.customservicechasisnumbercheck.Fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.customservicechasisnumbercheck.Activities.MainActivity;
import com.example.customservicechasisnumbercheck.R;
import com.example.customservicechasisnumbercheck.filebrowser.FileManagerActivity;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;


public class UHFUpdataFragment extends Fragment implements View.OnClickListener{

    MainActivity mContext;
    TextView tvPath,tvMsg;
    Button btSelect;
    Button btnUpdata,btnReadVere;
    String TAG="DeviceAPI_UHFUpdata";
    RadioButton rbSTM32 , rbR2000;
    String version;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uhfupdata, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
          mContext = (MainActivity) getActivity();
          tvPath=(TextView) mContext.findViewById(R.id.tvPath);
          tvMsg=(TextView)mContext.findViewById(R.id.tvMsg);
          btSelect=(Button) mContext.findViewById(R.id.btSelect);
          btnUpdata=(Button) mContext.findViewById(R.id.btnUpdata);
          btnReadVere=(Button)mContext.findViewById(R.id.btnReadVere);

          rbSTM32=(RadioButton) mContext.findViewById(R.id.rbSTM32);
          rbR2000=(RadioButton) mContext.findViewById(R.id.rbR2000);

          btSelect.setOnClickListener(this);
          btnUpdata.setOnClickListener(this);
          btnReadVere.setOnClickListener(this);
          init();
    }
    @Override
    public void onClick(View view) {
          switch (view.getId()){
              case R.id.btSelect:
                  Intent intent=new Intent(mContext, FileManagerActivity.class);
                  startActivity(intent);
                  break;
              case R.id.btnUpdata:
                  updata();
                  break;
              case R.id.btnReadVere:
                  String v=mContext.uhf.getSTM32Version();//获取版本号
                  tvMsg.setText("version:"+v);
                  break;
          }
     }
    @Override
    public void onDestroyView() {
        mContext.unregisterReceiver(pathReceiver);
        super.onDestroyView();
    }
    public void updata(){
        String filePath = tvPath.getText().toString();
        if (filePath==null || filePath.isEmpty()) {
            Toast.makeText(mContext,R.string.up_msg_sel_file, Toast.LENGTH_SHORT).show();
            return;
        }
        if(filePath.toLowerCase().lastIndexOf(".bin")<0) {
            Toast.makeText(mContext,"文件格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!(mContext.uhf.getConnectStatus()== RFIDWithUHFBluetooth.StatusEnum.CONNECTED)){
            Toast.makeText(mContext,"请先连接蓝牙!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!rbR2000.isChecked() && !rbSTM32.isChecked()){
            Toast.makeText(mContext,"请选择射频模块或者主板升级!", Toast.LENGTH_SHORT).show();
            return;
        }
        tvMsg.setText("");
        int flag=rbR2000.isChecked()?0:1;

        if(flag==0)
            version=mContext.uhf.getR2000Version();//获取版本号
        else
            version=mContext.uhf.getSTM32Version();//获取版本号
        tvMsg.setText("version:"+version);
        Log.d(TAG, "version="+version );

        new UpdateTask(filePath,flag).execute();
    }

    class UpdateTask extends AsyncTask<String, Integer, Boolean> {

        String path="";
        int flag;
        ProgressDialog progressDialog=null;
        public UpdateTask(String path, int flag){
            this.path=path;
            this.flag=flag;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
           // mypDialog.setMessage("init...");
            progressDialog=new ProgressDialog(mContext);
            progressDialog.setMessage("准备升级uhf模块...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
        @Override
        protected Boolean doInBackground(String... strings) {
            boolean result = false;
            File uFile = new File(path);
            if (!uFile.exists()) {
                return false;
            }
            long uFileSize = uFile.length();
            int packageCount = (int)(uFileSize/64);
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(path, "r");
            } catch (FileNotFoundException e) {
            }
            if (raf == null) {
                return false;
            }

            if(!mContext.uhf.uhfReBoot(flag)) {
                Log.d(TAG, "uhfJump2Boot 失败" );
                return false;
            }
            sleep(2000);
            Log.d(TAG, "UHF uhfStartUpdate" );
            if(!mContext.uhf.uhfStartUpdate()) {
                Log.d(TAG, "uhfStartUpdate 失败");
                return false;
            }
            int pakeSize=64;
            byte[] currData=new byte[(int)uFileSize];
            for(int k=0;k<packageCount;k++){
                int index=k*pakeSize;
                try {
                    int rsize = raf.read(currData,index , pakeSize);
//                    Log.d(TAG, "总包数量="+uFileSize+"  beginPack=" +index + " endPack=" + (index+pakeSize-1) +" rsize="+rsize);
                } catch (IOException e) {
                    stop();
                    return  false;
                }
                byte[] data= Arrays.copyOfRange(currData,index,index+pakeSize);
//                Log.d(TAG,"data="+ StringUtility.bytes2HexString(data,data.length));
                if (mContext.uhf.uhfUpdateData(data)) {
                    result = true;
                    publishProgress(index+pakeSize, (int)uFileSize);
                }else{
                    Log.d(TAG, "uhfUpdating 失败" );
                    stop();
                    return  false;
                }

            }
            if(uFileSize%pakeSize!=0){
                int index=packageCount*pakeSize;
                int len=(int)(uFileSize%pakeSize);
                try {
                    int rsize = raf.read(currData, index, len);
                    Log.d(TAG, "beginPack=" +index + " countPack=" + len +" rsize="+rsize);
                } catch (IOException e) {
                    stop();
                    return  false;
                }
                if (mContext.uhf.uhfUpdateData(Arrays.copyOfRange(currData,index,index+len))) {
                    result = true;
                    publishProgress((int)uFileSize, (int)uFileSize);
                } else {
                    Log.d(TAG, "uhfUpdating 失败" );
                    stop();
                    return  false;
                }
            }
            stop();
            return result;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage((values[0] * 100 / values[1]) + "% " + mContext.getString(R.string.app_msg_Upgrade));
            tvMsg.setText("version:"+version);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                Toast.makeText(mContext, R.string.uhf_msg_upgrade_fail, Toast.LENGTH_SHORT).show();
                tvMsg.setText(R.string.uhf_msg_upgrade_fail);
                tvMsg.setTextColor(Color.RED);
            } else {
                Toast.makeText(mContext, R.string.uhf_msg_upgrade_succ, Toast.LENGTH_SHORT).show();
                tvMsg.setText(R.string.uhf_msg_upgrade_succ);
                tvMsg.setTextColor(Color.GREEN);
            }
            tvMsg.setText(tvMsg.getText()+" version="+ (flag==0?mContext.uhf.getR2000Version():mContext.uhf.getSTM32Version()));
            progressDialog.dismiss();
        }

        private void sleep(int time){
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        private void stop(){
            Log.d(TAG, "UHF uhfStopUpdate" );
            if(!mContext.uhf.uhfEndUpdate())
                Log.d(TAG, "uhfStopUpdate 失败" );
            sleep(2000);
        }
    }

//-----------------------------------------------------------------------------
    PathReceiver pathReceiver=new PathReceiver();
    public void init(){
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(FileManagerActivity.Path_ACTION);
        mContext.registerReceiver(pathReceiver,intentFilter);
    }
    public class PathReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvPath.setText(intent.getStringExtra(FileManagerActivity.Path_Key));
        }
    }

}
