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

public class ItemsOfSingleGroupAdapter extends RecyclerView.Adapter<ItemsOfSingleGroupAdapter.ViewHolder> {
//    private static final String TAG = "ItemsOfSingleGroupAdapt";

    private List<SingleItem> items = new ArrayList<>();
//    private String ItemTableNameSuffix = "";
    private Context context;

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView singleItemId;
        private final TextView itemName;
        private final TextView extraField1;
        private final TextView extraField2;

        private ViewHolder(View itemView) {
            super(itemView);
            singleItemId = itemView.findViewById(R.id.rv_id_itemOfGroup);
            itemName = itemView.findViewById(R.id.rv_name_itemOfGroup);
            extraField1 = itemView.findViewById(R.id.rv_ex1_itemOfGroup);
            extraField2 = itemView.findViewById(R.id.rv_ex2_itemOfGroup);
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
    }

    public ItemsOfSingleGroupAdapter(List<SingleItem> items, String itemTableNameSuffix, Context context) {
        this.items = items;
//        ItemTableNameSuffix = itemTableNameSuffix;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_items_of_group,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemsOfSingleGroupAdapter.ViewHolder holder, int position) {
        SingleItem item = items.get(position);

        holder.getSingleItemId().setText(String.valueOf(item.getId()));
        holder.getItemName().setText(item.getName());
        holder.getExtraField1().setText(item.getExtending_list_1());
        holder.getExtraField2().setText(item.getExtending_list_2());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
