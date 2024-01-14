package com.amazia_iwallcraft.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.amazia_iwallcraft.utils.Constant;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.items.ItemWallpaper;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class AdapterWallpaperWelcome extends RecyclerView.Adapter<AdapterWallpaperWelcome.MyViewHolder> {

    ArrayList<ItemWallpaper> arrayList;
    Context context;
    SharedPref sharedPref;
    Methods methods;

    public AdapterWallpaperWelcome(Context context, ArrayList<ItemWallpaper> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
        methods = new Methods(context);
        sharedPref = new SharedPref(context);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout rootlayout;
        View vieww;
        RoundedImageView my_image_view;

        private MyViewHolder(View view) {
            super(view);
            rootlayout = view.findViewById(R.id.rootlayout);
            my_image_view = view.findViewById(R.id.iv_wallpaper);
            vieww = view.findViewById(R.id.view_wall);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wall_welcome, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        int placeholder;
        if(arrayList.get(position).getType().equals(Constant.TAG_PORTRAIT)) {
            placeholder = R.drawable.ic_placeholder_portrait;
        } else if(arrayList.get(position).getTitle().equals(Constant.TAG_LANDSCAPE)) {
            placeholder = R.drawable.ic_placeholder_landscape;
        } else {
            placeholder = R.drawable.ic_placeholder_square;
        }

        Picasso.get()
                .load(arrayList.get(position).getImage())
                .placeholder(placeholder)
                .resize(methods.getImageThumbWidth(arrayList.get(holder.getAbsoluteAdapterPosition()).getType()), methods.getImageThumbHeight(arrayList.get(holder.getAbsoluteAdapterPosition()).getType()))
                .centerCrop()
                .into(holder.my_image_view);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}