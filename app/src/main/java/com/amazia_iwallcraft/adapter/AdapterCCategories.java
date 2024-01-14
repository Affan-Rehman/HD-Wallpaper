package com.amazia_iwallcraft.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.amazia_iwallcraft.items.ItemSubCat;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.items.ItemCat;
import com.amazia_iwallcraft.utils.Methods;

import java.util.ArrayList;


public class AdapterCCategories extends RecyclerView.Adapter {

    Context context;
    Methods methods;
    ArrayList<ItemCat> arrayList;
    int selectedPos = -1;

    private class MyViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout cl_sub_cat;
        RoundedImageView iv_sub_cat;
        TextView tv_sub_cat;

        private MyViewHolder(View view) {
            super(view);
            cl_sub_cat = view.findViewById(R.id.cl_sub_cat);
            iv_sub_cat = view.findViewById(R.id.iv_sub_cat);
            tv_sub_cat = view.findViewById(R.id.tv_sub_cat_title);
        }
    }

    public AdapterCCategories(Context context, ArrayList<ItemCat> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
        methods = new Methods(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sub_categories, parent, false);
        return new AdapterCCategories.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        ((AdapterCCategories.MyViewHolder) holder).tv_sub_cat.setText(arrayList.get(position).getName());
        Picasso.get()
                .load(arrayList.get(position).getImage())
                .resize(methods.getImageThumbWidth(context.getString(R.string.sub_categories)), methods.getImageThumbHeight(context.getString(R.string.sub_categories)))
                .placeholder(R.drawable.placeholder_cat)
                .into(((AdapterCCategories.MyViewHolder) holder).iv_sub_cat);

        if(selectedPos != holder.getAbsoluteAdapterPosition()) {
            ((AdapterCCategories.MyViewHolder) holder).cl_sub_cat.setBackgroundResource(R.drawable.bg_sub_cat_round);
        } else {
            ((AdapterCCategories.MyViewHolder) holder).cl_sub_cat.setBackgroundResource(R.drawable.bg_gradient_round);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getID(int pos) {
        return arrayList.get(pos).getId();
    }

    public String setSelected(int position) {
        if(position != selectedPos) {
            int oldPos = selectedPos;
            selectedPos = position;
            notifyItemChanged(selectedPos);
            notifyItemChanged(oldPos);
            return arrayList.get(selectedPos).getId();
        } else {
            int oldPos = selectedPos;
            selectedPos = -1;
            notifyItemChanged(oldPos);
            return "";
        }
    }

    public int getSelected() {
        return selectedPos;
    }}