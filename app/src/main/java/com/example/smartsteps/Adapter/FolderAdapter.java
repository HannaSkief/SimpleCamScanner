package com.example.smartsteps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Contianer;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    List<Contianer> contianerList;
    Context context;
    ContainerItemClick activity;

    public interface ContainerItemClick{

        public void onContainerItemClick(Contianer contianer);
    }


    public FolderAdapter(List<Contianer> contianerList, Context context,Context activity) {
        this.contianerList = contianerList;
        this.context = context;
        this.activity=(ContainerItemClick)activity;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgContainer;
        TextView tvName,tvDate;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            imgContainer=itemView.findViewById(R.id.imgContainer);
            tvName=itemView.findViewById(R.id.tvName);
            tvDate=itemView.findViewById(R.id.tvDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.onContainerItemClick((Contianer)itemView.getTag());
                }
            });

        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(this.context).inflate(R.layout.folder_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.itemView.setTag(contianerList.get(position));
        holder.tvName.setText(contianerList.get(position).getName());
        holder.tvDate.setText(contianerList.get(position).getCreatedAt());

        holder.imgContainer.setImageResource(contianerList.get(position).getType().equals("folder")?
                R.drawable.ic_folder :
                R.drawable.ic_files);

    }

    @Override
    public int getItemCount() {
        return contianerList==null?0:contianerList.size();
    }

}
