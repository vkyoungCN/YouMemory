package com.vkyoungcn.learningtools;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.models.SingleItem;


/**
 * 用于单项Item的复习学习
 * 此时，默认显示英文+音标，点击翻面后显示汉译；
 * 需要点击翻面后并输入正确的拼写才能翻页。
 */
public class SingleItemRePickingFragment extends Fragment {
    private static final String TAG = "SingleItemRePickingFrag";
    private static final String SINGLE_ITEM = "single_item";

    private SingleItem singleItem;
    private Boolean pageSlidingAvailable = false;//只有在翻面后输入了正确的拼写后才可翻页。（此时可以再翻回正面）

//    private OnFragmentInteractionListener mListener;

    public SingleItemRePickingFragment() {
        // Required empty public constructor
    }

    public static SingleItemRePickingFragment newInstance(SingleItem singleItem) {
        SingleItemRePickingFragment fragment = new SingleItemRePickingFragment();
        Bundle args = new Bundle();
        args.putParcelable(SINGLE_ITEM, singleItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            singleItem = getArguments().getParcelable(SINGLE_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_single_item_re_picking, container, false);
        TextView tvName = (TextView)rootView.findViewById(R.id.tv_name_singleItemLearning_re);
        TextView tv_ext1 = rootView.findViewById(R.id.tv_ext1_singleItemLearning_re);
        TextView tv_ext2 = rootView.findViewById(R.id.tv_ext2_singleItemLearning_re);


        tvName.setText(singleItem.getName());
//        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),"fonts/GentiumPlus_I.ttf");
//        Log.i(TAG, "onCreateView: typeface: "+typeface.toString());
//        tv_ext1.setTypeface(typeface);
        tv_ext1.setText(singleItem.getExtending_list_1());
        tv_ext2.setText(singleItem.getExtending_list_2());



        return rootView;
    }

/*
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    */
/**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     *//*

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
*/
}
