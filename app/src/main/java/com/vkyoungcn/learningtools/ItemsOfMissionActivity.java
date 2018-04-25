package com.vkyoungcn.learningtools;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.vkyoungcn.learningtools.adapter.ItemsOfMissionAdapter;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.models.SingleItem;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ItemsOfMissionActivity extends AppCompatActivity {
    private static final String TAG = "ItemsOfMissionActivity";
    private ArrayList<SingleItem> items;
    private String tableNameSuffix;
    private Mission targetMission;
    public static final int MESSAGE_ITEMS_FETCHED = 5501;

    private Handler handler = new AllItemsActivityHandler(this);

    private RecyclerView mRv;
    private FrameLayout fltMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_of_mission);
        tableNameSuffix = getIntent().getStringExtra("MissionTableSuffix");
        targetMission = getIntent().getParcelableExtra("Mission");

        mRv = findViewById(R.id.rv_itemsOfMission);//等待线程加载Items完成后再处理
        fltMask= findViewById(R.id.flt_mask_itemsOfMission);//数据加载完成后取消遮罩

        new Thread(new prepareItemsRunnable()).start();





    }


    final static class AllItemsActivityHandler extends Handler {
        private final WeakReference<ItemsOfMissionActivity> activity;

        private AllItemsActivityHandler(ItemsOfMissionActivity activity) {
            this.activity = new WeakReference<ItemsOfMissionActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ItemsOfMissionActivity itemsOfMissionActivity = activity.get();
            if(itemsOfMissionActivity != null){
                itemsOfMissionActivity.handleMessage(msg);
            }

        }};

    private class prepareItemsRunnable implements Runnable{
        @Override
        public void run() {
            YouMemoryDbHelper memoryDbHelper = YouMemoryDbHelper.getInstance(ItemsOfMissionActivity.this);
            items = (ArrayList<SingleItem>)memoryDbHelper.getAllItems(tableNameSuffix);

            Message message = new Message();
            message.what = MESSAGE_ITEMS_FETCHED;
            message.obj = items;

            handler.sendMessage(message);
        }
    }



    void handleMessage(Message message){
        switch (message.what){
            case MESSAGE_ITEMS_FETCHED:
                mRv.setLayoutManager(new LinearLayoutManager(this));
                ItemsOfMissionAdapter itemsOfMissionAdapter = new ItemsOfMissionAdapter(items,this);
                mRv.setAdapter(itemsOfMissionAdapter);
                fltMask.setVisibility(View.GONE);
        }

    }

}
