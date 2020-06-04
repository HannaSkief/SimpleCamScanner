package com.example.smartsteps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartsteps.Common.Common;
import com.example.smartsteps.R;
import com.example.smartsteps.Room.Images;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    List<Images> imagesList;
    Context context;
    ImageClicked activity;

    public FileAdapter(List<Images> imagesList, Context context,Context activity) {
        this.imagesList = imagesList;
        this.context = context;
        this.activity=(ImageClicked)activity;
    }

    public interface ImageClicked{
        public void EditImage();
        public void deleteImage();
        public void openImage();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image,imgMore;
        TextView tvImagePriority,tvImageDate;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.image);
            imgMore=itemView.findViewById(R.id.imgMore);
            tvImageDate=itemView.findViewById(R.id.tvImageDate);
            tvImagePriority=itemView.findViewById(R.id.tvImagePriority);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Common.selected_image=(Images)itemView.getTag();
                    activity.openImage();
                }
            });

            imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Common.selected_image=(Images)itemView.getTag();
                    showPopUpMenuOnMoreClicked(imgMore);

                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.file_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.itemView.setTag(imagesList.get(position));

        File file=new File(imagesList.get(position).getSecondPath());
        File file1=new File(imagesList.get(position).getPath());
        if(file.exists()){
            Picasso.get().load(file).centerCrop().fit().into(holder.image);

        }else if (file1.exists()){
            Picasso.get().load(file1).centerCrop().fit().into(holder.image);

        }
        else
        {
            holder.image.setImageResource(R.drawable.ic_image_black);
        }

        holder.tvImagePriority.setText(String.valueOf(imagesList.get(position).getPriority()));
        holder.tvImageDate.setText(imagesList.get(position).getAddedAt());



    }

    @Override
    public int getItemCount() {
        return imagesList==null?0:imagesList.size();
    }

    private void showPopUpMenuOnMoreClicked(ImageView more){
        PopupMenu popupMenu=new PopupMenu(context,more);
        popupMenu.getMenuInflater().inflate(R.menu.file_more_option_menu,popupMenu.getMenu());
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){

                    case R.id.moreCrop:activity.EditImage();return true;
                    case R.id.moreDelete:activity.deleteImage();return true;
                }

                return false;
            }
        });
    }

}
