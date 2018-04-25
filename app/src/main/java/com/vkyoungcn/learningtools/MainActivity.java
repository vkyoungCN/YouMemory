package com.vkyoungcn.learningtools;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.adapter.AllMissionRvAdapter;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.util.ArrayList;

/*
* 首页。
* 上部预留横向图片式广告位（间隔滚动式）
* 下方是任务列表；点击可进入新Activity查看任务情况。
* */
public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //从数据库获取数据源：所有Mission的标题（name字段）
//        YouMemoryDbHelper memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

        RecyclerView allMissionRecyclerView = (RecyclerView) findViewById(R.id.all_missions_rv);

        ArrayList<Mission> allMissions = getIntent().getParcelableArrayListExtra("All_Missions");
        if(allMissions == null){
            Toast.makeText(this, "没有任务信息", Toast.LENGTH_SHORT).show();
            allMissions = new ArrayList<>();
        }

        allMissionRecyclerView.setHasFixedSize(true);//暂时只有固定数量的任务，可以设fix。
        allMissionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allMissionRecyclerView.setAdapter(new AllMissionRvAdapter(allMissions,this));

    }
}
