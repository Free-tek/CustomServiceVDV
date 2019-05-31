package com.example.customservicechasisnumbercheck.Fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.customservicechasisnumbercheck.Activities.MainActivity;
import com.example.customservicechasisnumbercheck.R;
import com.example.customservicechasisnumbercheck.Utils;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;

public class BarcodeFragment extends Fragment implements View.OnClickListener{


    static boolean isExit_=false;
    MainActivity mContext;
    ScrollView scrBarcode;
    TextView tvData;
    Button btnScan,btClear;
    Object lock=new Object();
    Spinner spingCodingFormat;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.obj.toString()!=null) {
                tvData.setText(tvData.getText() + msg.obj.toString() + "\r\n");
                scroll2Bottom(scrBarcode, tvData);
                Utils.playSound(1);
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barcode, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isExit_=false;
         scrBarcode=(ScrollView)getActivity().findViewById(R.id.scrBarcode);
         tvData=(TextView)getActivity().findViewById(R.id.tvData);
         btnScan=(Button)getActivity().findViewById(R.id.btnScan);
          btClear=(Button)getActivity().findViewById(R.id.btClear);
         btnScan.setOnClickListener(this);
         btClear.setOnClickListener(this);
        spingCodingFormat=(Spinner)getActivity().findViewById(R.id.spingCodingFormat);
         mContext=(MainActivity) getActivity();
         mContext.uhf.setKeyEventCallback(new RFIDWithUHFBluetooth.KeyEventCallback() {
            @Override
            public void getKeyEvent(int keycode) {
                Log.d("DeviceAPI_setKeyEvent","  keycode ="+keycode +"   ,isExit_="+isExit_);
                if(!isExit_ && mContext.uhf.getConnectStatus()== RFIDWithUHFBluetooth.StatusEnum.CONNECTED)
                   scan();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isExit_=true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnScan:
                scan();
                break;
            case R.id.btClear:
                tvData.setText("");
                break;
        }
    }


    private synchronized void scan(){
       if(!isRuning){
           isRuning=true;
           new ScanThread().start();
       }
    }

    boolean isRuning=false;
    class   ScanThread  extends Thread {
       public void run(){
           String data=null;
           byte[] temp=mContext.uhf.scanBarcodeToBytes();
           if(temp!=null) {
               if (spingCodingFormat.getSelectedItemPosition() == 1) {
                   try {
                       data = new String(temp, "utf8");
                   } catch (Exception ex) {
                   }
               } else if (spingCodingFormat.getSelectedItemPosition() == 2) {
                   try {
                       data = new String(temp, "gb2312");
                   } catch (Exception ex) {
                   }
               } else {
                   data = new String(temp);
               }

               if (data != null && !data.isEmpty()) {
                   Log.d("DeviceAPI_setKeyEvent","data="+data);
                   Message msg = Message.obtain();
                   msg.obj = data;
                   handler.sendMessage(msg);
               }
           }
           isRuning=false;
       }
   }

    public static void scroll2Bottom(final ScrollView scroll, final View inner) {
        Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (scroll == null || inner == null) {
                    return;
                }
                // 内层高度超过外层
                int offset = inner.getMeasuredHeight()
                        - scroll.getMeasuredHeight();
                if (offset < 0) {
                    offset = 0;
                }
                scroll.scrollTo(0, offset);
            }
        });

    }
}
