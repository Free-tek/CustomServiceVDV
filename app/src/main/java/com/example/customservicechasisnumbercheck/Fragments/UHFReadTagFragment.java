package com.example.customservicechasisnumbercheck.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.example.customservicechasisnumbercheck.Activities.MainActivity;
import com.example.customservicechasisnumbercheck.R;
import com.example.customservicechasisnumbercheck.Utils;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;
import java.util.HashMap;


public class UHFReadTagFragment extends Fragment implements View.OnClickListener{

    private boolean loopFlag = false;
    private ListView LvTags;
    private Button InventoryLoop,btInventory,btStop;//
    private Button btClear;
    private TextView tv_count,tv_total;
    private boolean isExit=false;
    private long total=0;
    private MainActivity mContext;
    private SimpleAdapter adapter;
    private HashMap<String, String> map;
    private ArrayList<HashMap<String, String>> tagList;
    private String TAG="DeviceAPI_UHFReadTag";

    //--------------------------------------获取 解析数据-------------------------------------------------
    final int FLAG_START=0;//开始
    final int FLAG_STOP=1;//停止
    final int FLAG_UHFINFO=3;
    final int FLAG_SUCCESS=10;//成功
    final int FLAG_FAIL=11;//失败

    boolean isRuning=false;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case FLAG_STOP:
                    if(msg.arg1==FLAG_SUCCESS) {
                        //停止成功
                        btClear.setEnabled(true);
                        btStop.setEnabled(false);
                        InventoryLoop.setEnabled(true);
                        btInventory.setEnabled(true);
                    }else{
                        //停止失败
                        Utils.playSound(2);
                        Toast.makeText(mContext, R.string.uhf_msg_inventory_stop_fail, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case FLAG_START:
                    if(msg.arg1==FLAG_SUCCESS){
                        //开始读取标签成功
                        btClear.setEnabled(false);
                        btStop.setEnabled(true);
                        InventoryLoop.setEnabled(false);
                        btInventory.setEnabled(false);
                    }else{
                        //开始读取标签失败
                        Utils.playSound(2);
                    }
                    break;
                case FLAG_UHFINFO:
                    UHFTAGInfo info =(UHFTAGInfo)msg.obj;
                    addEPCToList(info, "N/A");
                    Utils.playSound(1);
                    break;
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uhfread_tag, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopInventory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isExit=true;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.btClear:
                clearData();
                break;
            case  R.id.InventoryLoop:
                startThread(false);
                break;
            case R.id.btInventory:
                inventory();
                break;
            case  R.id.btStop:
                if(mContext.uhf.getConnectStatus()== RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
                    startThread(true);
                }
                break;

        }
    }
    private  void init(){
        isExit=false;
        mContext.setConnectStatusNotice(new ConnectStatus());
        LvTags=(ListView)mContext.findViewById(R.id.LvTags);
        btInventory=(Button)mContext.findViewById(R.id.btInventory);
        InventoryLoop=(Button)mContext.findViewById(R.id.InventoryLoop);
        btStop=(Button)mContext.findViewById(R.id.btStop);
        btStop.setEnabled(false);
        btClear=(Button)mContext.findViewById(R.id.btClear);
        tv_count=(TextView)mContext.findViewById(R.id.tv_count);
        tv_total=(TextView)mContext.findViewById(R.id.tv_total);


        InventoryLoop.setOnClickListener(this);
        btInventory.setOnClickListener(this);
        btClear.setOnClickListener(this);
        btStop.setOnClickListener(this);
        tagList = new ArrayList<HashMap<String, String>>();
        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[] { "tagData", "tagLen", "tagCount", "tagRssi" },
                new int[] { R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
                        R.id.TvTagRssi });
        LvTags.setAdapter(adapter);
        mContext.uhf.setKeyEventCallback(new RFIDWithUHFBluetooth.KeyEventCallback() {
            @Override
            public void getKeyEvent(int keycode) {
                Log.d("DeviceAPI_ReadTAG","  keycode ="+keycode  +"   ,isExit="+isExit);
               if(!isExit && mContext.uhf.getConnectStatus()== RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
                   startThread(loopFlag);
               }
            }
        });
        clearData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContext.uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
            InventoryLoop.setEnabled(true);
            btInventory.setEnabled(true);
        }else{
            InventoryLoop.setEnabled(false);
            btInventory.setEnabled(false);
        }
    }

    private void clearData() {
        tv_count.setText("0");
        tagList.clear();
        total=0;
        tv_total.setText("0");
        adapter.notifyDataSetChanged();
    }

    /**
     * 停止识别
     */
      private void stopInventory() {
            loopFlag = false;
            RFIDWithUHFBluetooth.StatusEnum statusEnum=mContext.uhf.getConnectStatus();
            Message msg=handler.obtainMessage(FLAG_STOP);
            boolean result=mContext.uhf.stopInventoryTag();
            if (result || statusEnum== RFIDWithUHFBluetooth.StatusEnum.DISCONNECTED) {
                msg.arg1=FLAG_SUCCESS;
            } else {
                msg.arg1=FLAG_FAIL;
            }
            if(statusEnum== RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
              //在连接的情况下，结束之后继续接收未接收完的数据
              //getUHFInfoEx();
            }
            mContext.scaning=false;
            handler.sendMessage(msg);
    }
        class ConnectStatus implements MainActivity.IConnectStatus {
           @Override
           public void getStatus(RFIDWithUHFBluetooth.StatusEnum statusEnum) {
               if(statusEnum== RFIDWithUHFBluetooth.StatusEnum.CONNECTED){
                   if(!loopFlag) {
                       try {
                           Thread.sleep(500);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                       InventoryLoop.setEnabled(true);
                       btInventory.setEnabled(true);
                   }
               }else if(statusEnum== RFIDWithUHFBluetooth.StatusEnum.DISCONNECTED){
                   loopFlag = false;
                   mContext.scaning=false;
                   btClear.setEnabled(true);
                   btStop.setEnabled(false);
                   InventoryLoop.setEnabled(false);
                   btInventory.setEnabled(false);
               }
           }
       }



    public synchronized void startThread(boolean isStop){
        if(isRuning) {
            return;
        }
        isRuning=true;
        new TagThread(isStop).start();
    }

    class TagThread extends Thread {
        boolean isStop=false;
        public TagThread(boolean isStop){
            this.isStop=isStop;
        }
        public void run() {
            if(isStop){
                stopInventory();
                isRuning=false;//执行完成设置成false
            }else{
                Message msg=handler.obtainMessage(FLAG_START);
                if (mContext.uhf.startInventoryTag()) {
                    loopFlag = true;
                    mContext.scaning=true;
                    msg.arg1=FLAG_SUCCESS;
                } else {
                    msg.arg1=FLAG_FAIL;
                }
                handler.sendMessage(msg);
                isRuning=false;//执行完成设置成false
                while (loopFlag) {
                    getUHFInfo();
                }
            }
        }
    }

    private synchronized  boolean getUHFInfo(){
            ArrayList<UHFTAGInfo> list = mContext.uhf.readTagFromBuffer();
            if (list != null) {
                for (int k = 0; k < list.size(); k++) {
                    UHFTAGInfo info = list.get(k);
                    Message msg = handler.obtainMessage(FLAG_UHFINFO);
                    msg.obj =info;
                    handler.sendMessage(msg);
                }
                if (list.size() > 0)
                    return true;
            } else {
                return false;
            }
            return false;
    }
    /**
     * 添加EPC到列表中
     *
     * @param uhftagInfo
     */
    private void addEPCToList(UHFTAGInfo uhftagInfo, String rssi) {
        if (!TextUtils.isEmpty(uhftagInfo.getEPC())) {
            int index = checkIsExist(uhftagInfo.getEPC());

            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("EPC:");
            stringBuilder.append(uhftagInfo.getEPC());
            if(!TextUtils.isEmpty(uhftagInfo.getTid())){
                stringBuilder.append("\r\nTID:");
                stringBuilder.append(uhftagInfo.getTid());
            }
            if(!TextUtils.isEmpty(uhftagInfo.getUser())){
                stringBuilder.append("\r\nUSER:");
                stringBuilder.append(uhftagInfo.getUser());
            }

            map = new HashMap<String, String>();
            map.put("tagUii", uhftagInfo.getEPC());
            map.put("tagData", stringBuilder.toString());
            map.put("tagCount", String.valueOf(1));
            map.put("tagRssi", rssi);
            // mContext.getAppContext().uhfQueue.offer(epc + "\t 1");
            if (index == -1) {
                tagList.add(map);
                LvTags.setAdapter(adapter);
                tv_count.setText("" + adapter.getCount());
            } else {
                int tagcount = Integer.parseInt(tagList.get(index).get("tagCount"), 10) + 1;
                map.put("tagCount", String.valueOf(tagcount));
                tagList.set(index, map);
            }
            total=total+1;
            tv_total.setText(String.valueOf(total));
            adapter.notifyDataSetChanged();
        }
    }
    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (strEPC==null || strEPC.isEmpty()) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagUii");
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    private void inventory(){
        String uii= mContext.uhf.inventorySingleTag();
        if(uii!=null){
            UHFTAGInfo uhftagInfo=new UHFTAGInfo();
            uhftagInfo.setEPC(mContext.uhf.convertUiiToEPC(uii));
            Message msg = handler.obtainMessage(FLAG_UHFINFO);
            msg.obj = uhftagInfo;
            handler.sendMessage(msg);
        }
    }
    private synchronized  void getUHFInfoEx(){
        String strResult="";
        long begintime= System.currentTimeMillis();
        while (!isExit) {
            ArrayList<UHFTAGInfo> list = mContext.uhf.readTagFromBuffer();
            if (list != null) {
                for (int k = 0; k < list.size() && !isExit; k++) {
                    UHFTAGInfo info = list.get(k);
                    Message msg = handler.obtainMessage(FLAG_UHFINFO);
                    msg.obj = strResult + "EPC:" + info.getEPC() + "@N/A";
                    handler.sendMessage(msg);
                }
                if (list.size() == 0) {
                    if(System.currentTimeMillis()-begintime>1000*1) {
                        return;
                    }
                }
            } else {
                if(System.currentTimeMillis()-begintime>1000*1) {
                    return;
                }
            }
        }
    }
}
