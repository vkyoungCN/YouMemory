package com.vkyoungcn.learningtools.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.SingleItem;

import java.util.ArrayList;
import java.util.List;

public class ItemsOfMissionAdapter extends RecyclerView.Adapter<ItemsOfMissionAdapter.ViewHolder> {
    private static final String TAG = "ItemsOfMissionAdapter";

    private List<SingleItem> items = new ArrayList<>();
//    private String ItemTableNameSuffix = "";
    private Context context;

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView singleItemId;
        private final TextView itemName;
        private final TextView extraField1;
        private final TextView extraField2;
        private final TextView available;

        private ViewHolder(View itemView) {
            super(itemView);
            singleItemId = itemView.findViewById(R.id.rv_id_itemOfMission);
            itemName = itemView.findViewById(R.id.rv_name_itemOfMission);
            extraField1 = itemView.findViewById(R.id.rv_ex1_itemOfMission);
            extraField2 = itemView.findViewById(R.id.rv_ex2_itemOfMission);
            available = itemView.findViewById(R.id.rv_available_itemOfMission);
        }

        private TextView getSingleItemId() {
            return singleItemId;
        }

        private TextView getItemName() {
            return itemName;
        }

        private TextView getExtraField1() {
            return extraField1;
        }

        private TextView getExtraField2() {
            return extraField2;
        }

        public TextView getAvailable() {
            return available;
        }
    }

    public ItemsOfMissionAdapter(List<SingleItem> items, Context context) {
        this.items = items;
//        ItemTableNameSuffix = itemTableNameSuffix;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_items_of_mission,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemsOfMissionAdapter.ViewHolder holder, int position) {
        SingleItem item = items.get(position);

        holder.getSingleItemId().setText(String.valueOf(item.getId()));
        holder.getItemName().setText(item.getName());
        holder.getExtraField1().setText(item.getExtending_list_1());
        holder.getExtraField2().setText(item.getExtending_list_2());
        holder.getAvailable().setText(String.valueOf(!item.isChose()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
