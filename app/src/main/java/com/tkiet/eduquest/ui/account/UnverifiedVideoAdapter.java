package com.tkiet.eduquest.ui.account;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tkiet.eduquest.R;

import java.util.List;

public class UnverifiedVideoAdapter extends RecyclerView.Adapter<UnverifiedVideoAdapter.VideoViewHolder> {

    private Context context;
    private List<VideoModel> videoList;
    private OnVideoActionListener listener;

    public interface OnVideoActionListener {
        void onAccept(String videoId);
        void onReject(String videoId, String videoUrl);
    }

    public UnverifiedVideoAdapter(Context context, List<VideoModel> videoList, OnVideoActionListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video2, parent, false);
        return new VideoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoModel video = videoList.get(position);

        holder.title.setText(video.getTitle());
        holder.description.setText(video.getDescription());

        // Set video URI
        holder.videoView.setVideoURI(Uri.parse(video.getVideoUrl()));

        // Create and set up MediaController inside VideoView only
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(holder.videoView);
        holder.videoView.setMediaController(mediaController);

        // Show first frame only (don't auto-play)
        holder.videoView.seekTo(1);

        // Play/Pause on click
        holder.videoView.setOnClickListener(v -> {
            if (!holder.videoView.isPlaying()) {
                holder.videoView.start();
            } else {
                holder.videoView.pause();
            }
            mediaController.show();  // Show controls on click
        });

        // Accept Button Click
        holder.acceptButton.setOnClickListener(v -> listener.onAccept(video.getVideoId()));

        // Reject Button Click
        holder.rejectButton.setOnClickListener(v -> listener.onReject(video.getVideoId(), video.getVideoUrl()));
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView title, description;
        VideoView videoView;
        Button acceptButton, rejectButton;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.videoTitle);
            description = itemView.findViewById(R.id.videoDescription);
            videoView = itemView.findViewById(R.id.videoView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }

}
