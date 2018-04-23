package com.vkyoungcn.learningtools.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.vkyoungcn.learningtools.fragments.SingleItemInitLearningFragment;
import com.vkyoungcn.learningtools.fragments.SingleItemRePickingFragment;
import com.vkyoungcn.learningtools.models.SingleItem;

import java.util.List;

public class LearningViewPrAdapter extends FragmentStatePagerAdapter {
//    private static final String TAG = "LearningViewPrAdapter";
    private List<SingleItem> singleItems;

    private int learningType = TYPE_INIT_LEARNING;//学习类型，影响将加载的fg类型
    public static final int TYPE_INIT_LEARNING = 101;//初学
    public static final int TYPE_RE_PICKING = 102;//复习
    public static final int TYPE_EXAMINING = 103;//纯测验


    public LearningViewPrAdapter(FragmentManager fm, List<SingleItem> singleItems, int type) {
        super(fm);
        this.singleItems = singleItems;
        this.learningType = type;
    }

    @Override
    public Fragment getItem(int position) {
        switch (learningType){
            case TYPE_INIT_LEARNING:
                return SingleItemInitLearningFragment.newInstance(singleItems.get(position));

            case TYPE_RE_PICKING:
                //复习
                return SingleItemRePickingFragment.newInstance(singleItems.get(position));
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
