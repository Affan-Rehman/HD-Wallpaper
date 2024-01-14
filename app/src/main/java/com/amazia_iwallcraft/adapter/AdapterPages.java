package com.amazia_iwallcraft.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazia_iwallcraft.wallpaper.AboutActivity;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.wallpaper.WebViewActivity;
import com.amazia_iwallcraft.items.ItemPage;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPages extends RecyclerView.Adapter<AdapterPages.MyViewHolder> {

    Context context;
    ArrayList<ItemPage> arrayList;

    public AdapterPages(Context context, ArrayList<ItemPage> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        View view_pages;

        MyViewHolder(View view) {
            super(view);
            tv_title = view.findViewById(R.id.tv_pages);
            view_pages = view.findViewById(R.id.view_pages);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pages, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_title.setText(arrayList.get(position).getTitle());
        holder.tv_title.setOnClickListener(v -> {
            if(arrayList.get(holder.getAbsoluteAdapterPosition()).getId().equals("1")) {
                Intent intent = new Intent(context, AboutActivity.class);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra("item", arrayList.get(holder.getAbsoluteAdapterPosition()));
                context.startActivity(intent);
            }
        });

        if(position == arrayList.size()-1) {
            holder.view_pages.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}