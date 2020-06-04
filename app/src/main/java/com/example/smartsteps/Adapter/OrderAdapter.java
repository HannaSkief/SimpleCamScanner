package com.example.smartsteps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Images;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    List<Images> imagesList;
    Context context;

    public OrderAdapter(List<Images> imagesList, Context context) {
        this.imagesList = imagesList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView tvImagePriority;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image=itemView.findViewById(R.id.image);
            tvImagePriority=itemView.findViewById(R.id.tvImagePriority);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.order_image_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.itemView.setTag(imagesList.get(position));


        File file=new File(imagesList.get(position).getSecondPath());
        if(file.exists()){
            Picasso.get().load(file).centerCrop().fit().into(holder.image);
            holder.tvImagePriority.setText(String.valueOf(imagesList.get(position).getPriority()));

        }else{

        }
    }

    @Override
    public int getItemCount() {
        return imagesList==null?0:imagesList.size();
    }

}
