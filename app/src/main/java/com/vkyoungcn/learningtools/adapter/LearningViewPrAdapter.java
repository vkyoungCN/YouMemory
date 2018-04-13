package com.vkyoungcn.learningtools.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.vkyoungcn.learningtools.SingleItemInitLearningFragment;
import com.vkyoungcn.learningtools.models.SingleItem;

import java.util.List;

public class LearningViewPrAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "LearningViewPrAdapter";
    private List<SingleItem> singleItems;

    private int learningType = TYPE_INIT_LEARNING;//学习类型，影响将加载的fg类型
    public static final int TYPE_INIT_LEARNING = 1;//初学
    public static final int TYPE_RE_PICKING = 2;//复习
    public static final int TYPE_EXAMINING = 3;//纯测验



    public LearningViewPrAdapter(FragmentManager fm, List<SingleItem> singleItems, int type) {
        super(fm);
//        Log.i(TAG, "LearningViewPrAdapter: b");
        this.singleItems = singleItems;
        this.learningType = type;
    }

    @Override
    public SingleItemInitLearningFragment getItem(int position) {
        switch (learningType){
            case TYPE_INIT_LEARNING:
                //在此，需要根据传入的position动态生成新fg实例
                return SingleItemInitLearningFragment.newInstance(singleItems.get(position));

            case TYPE_RE_PICKING:
                return null;
            case TYPE_EXAMINING:
                return null;
           default:
               return null;
        }

    }

    @Override
    public int getCount() {
        return singleItems.size();
    }
}
