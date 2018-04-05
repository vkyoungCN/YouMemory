package com.vkyoungcn.learningtools;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.vkyoungcn.learningtools.adapter.AllMissionRvAdapter;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private RecyclerView allMissionRecyclerView;
    private YouMemoryDbHelper memoryDbHelper;
    private ArrayList<Mission> allMissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //从数据库获取数据源：所有Mission的标题（name字段）
        memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());
        allMissions = (ArrayList<Mission>) memoryDbHelper.getAllMissions();
//        Log.i(TAG+"0000", "onCreate: allMissionTitle = "+allMissionTitles);

        //找到RecyclerView控件……
        allMissionRecyclerView = (RecyclerView) findViewById(R.id.all_missions_rv);
        allMissionRecyclerView.setHasFixedSize(true);

        allMissionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.i(TAG, "onCreate: setLayoutManager for Rv.");
        allMissionRecyclerView.setAdapter(new AllMissionRvAdapter(allMissions,this));

    }
}
