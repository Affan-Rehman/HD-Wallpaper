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

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.items.ItemCat;
import com.amazia_iwallcraft.utils.Methods;

import java.util.ArrayList;


public class AdapterCategories extends RecyclerView.Adapter {

    Context context;
    ArrayList<ItemCat> arrayList;
    ArrayList<ItemCat> filteredArrayList;
    NameFilter filter;
    int width, height;
    Methods methods;

    private class MyViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout cl_cat;
        RoundedImageView imageView;
        TextView textView_cat;

        private MyViewHolder(View view) {
            super(view);
            cl_cat = view.findViewById(R.id.cl_cat);
            imageView = view.findViewById(R.id.iv_cat);
            textView_cat = view.findViewById(R.id.tv_cat_title);
        }
    }

    public AdapterCategories(Context context, ArrayList<ItemCat> arrayList) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        methods = new Methods(context);
        width = methods.getColumnWidth(2, 8);
        height = (int) (width/1.5);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_categories, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        ((MyViewHolder) holder).cl_cat.setLayoutParams(params);

        ((MyViewHolder) holder).textView_cat.setText(arrayList.get(position).getName());
        Picasso.get()
                .load(arrayList.get(position).getImage())
                .resize(methods.getImageThumbWidth(context.getString(R.string.categories)), methods.getImageThumbHeight(context.getString(R.string.categories)))
                .centerCrop()
                .placeholder(R.drawable.placeholder_cat)
                .into(((MyViewHolder) holder).imageView);
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

    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<ItemCat> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getName();
                    if (nameList.toLowerCase().contains(constraint))
                        filteredItems.add(filteredArrayList.get(i));
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = filteredArrayList;
                    result.count = filteredArrayList.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            arrayList = (ArrayList<ItemCat>) results.values;
            notifyDataSetChanged();
        }
    }
}