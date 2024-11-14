package com.tkiet.eduquest.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tkiet.eduquest.R;
import com.tkiet.eduquest.ui.account.VideoModel;
import com.tkiet.eduquest.ui.account.EditVideoActivity;

import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<com.tkiet.eduquest.ui.home.VideosAdapter.VideoViewHolder> {

    private Context context;
    private List<VideoModel> videoList;

    // Modify constructor to accept List<VideoModel>
    public VideosAdapter(Context context, List<VideoModel> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public com.tkiet.eduquest.ui.home.VideosAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new com.tkiet.eduquest.ui.home.VideosAdapter.VideoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull com.tkiet.eduquest.ui.home.VideosAdapter.VideoViewHolder holder, int position) {
        VideoModel video = videoList.get(position);
        holder.title.setText(video.getTitle());
        holder.description.setText(video.getDescription());
        Glide.with(context).load(video.getThumbnailUrl()).into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (video.getVideoId() != null) {
                Intent intent = new Intent(context, ShowVideoActivity.class);
                intent.putExtra("VIDEO_ID", video.getVideoId());
                Log.d("VideosAdapter", "Video ID: " + video.getVideoId());
                context.startActivity(intent);
            } else {
                Log.e("VideosAdapter", "Video ID is null for position: " + position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView title, description;
        ImageView thumbnail;

        public VideoViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.videoTitle);
            description = itemView.findViewById(R.id.videoDescription);
            thumbnail = itemView.findViewById(R.id.videoThumbnail);
        }
    }
}