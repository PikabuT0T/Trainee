package com.example.traine;


import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;


import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class ViewHolder extends RecyclerView.ViewHolder {
    TextView videoNameView, videoDurationView;
    ImageView thumbnail;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        thumbnail = itemView.findViewById(R.id.thumbnail);
        videoNameView = itemView.findViewById(R.id.video_name);
        videoDurationView = itemView.findViewById(R.id.video_duration);
    }

    public void bindData(Member member) {
        videoNameView.setText(member.getVideoName());
        videoDurationView.setText(member.getVideoDuration());

        try {
            Picasso.get()
                    .load(Uri.parse(member.getVideoPreviewImage()))
                    .placeholder(R.drawable.ic_profile)
                    .resize(140, 140)
                    .into(thumbnail);
        } catch (Exception e) {
            //thumbnail.setImageResource(R.drawable.ic_error); // Assuming ic_error is a drawable resource
        }
    }

}

//public class ViewHolder extends RecyclerView.ViewHolder {
//
//    SimpleExoPlayer simpleExoPlayer;
//    PlayerView playerView;
//    TextView videoNameView, videoSizeView, videoDurationView;
//    ImageView thumbnail, menu_more;
//
//    public ViewHolder(@NonNull View itemView) {
//
//        super(itemView);
//        thumbnail = itemView.findViewById(R.id.thumbnail);
//        menu_more = itemView.findViewById(R.id.video_menu_more);
//        videoNameView = itemView.findViewById(R.id.video_name);
////        videoSizeView = itemView.findViewById(R.id.video_size);
//        videoDurationView = itemView.findViewById(R.id.video_duration);
//
//    }
//
//    public void setPlayerView(Application application,String videoUri, String videoName, String videoPreviewImageUri, String videoDuration){
//        videoDurationView.setText(videoDuration);
//        videoNameView.setText(videoName);
//        //thumbnail.setImageURI(Uri.parse(videoPreviewImageUri));
//
//
//        try{
//            Uri image = Uri.parse(videoPreviewImageUri);
//            Picasso.get()
//                    .load(image)
//                    .placeholder(R.drawable.ic_profile)
//                    .resize(140, 140)
//                    .into(thumbnail);
//        }catch (Exception e){
//            //Toast.makeText(ViewHolder.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//
//
//        Uri video = Uri.parse(videoUri);
//        simpleExoPlayer = new SimpleExoPlayer.Builder(application).build();
//        MediaItem mediaItem = MediaItem.fromUri(video);
//
//        simpleExoPlayer.setMediaItem(mediaItem);
//        simpleExoPlayer.prepare();
//
//    }
//}
