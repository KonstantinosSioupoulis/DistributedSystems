package com.example.ourmessenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.MyViewHolder> {
    private ArrayList<MultimediaFile> history;

    public recyclerAdapter(ArrayList<MultimediaFile> history){
        this.history=history;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView userName;
        private ImageView photo;
        private TextView text;

        public MyViewHolder(final View view){
            super(view);
            userName=view.findViewById(R.id.chat_userName);
            photo=view.findViewById(R.id.chat_image);
            //text=view.findViewById(R.id.text_content);
        }

    }

    @NonNull
    @Override
    public recyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_items,parent,false);
        return new MyViewHolder(itemView);
    }

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean isTextFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("text");
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerAdapter.MyViewHolder holder, int position) {
        String name=history.get(position).getProfileName();
        holder.userName.setText("by " + name);
        Bitmap bitmap=null;
        String filePath;
        if (isImageFile(history.get(position).getPath())) {
            InputStream inputStream = new ByteArrayInputStream(history.get(position).getMultimediaFileChunk());
            bitmap = BitmapFactory.decodeStream(inputStream);
            holder.photo.setImageBitmap(bitmap);
            //holder.text.setText(history.get(position).getMultimediaFileName());
        }
        if(isVideoFile(history.get(position).getPath())){
            filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/ourMessenger/"+history.get(position).getMultimediaFileName();
            if (history.get(position).getHasExtra()){
                filePath+=" ("+history.get(position).getExtra()+")";
            }
            filePath+="."+history.get(position).getFileType();
            bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
            holder.photo.setImageBitmap(bitmap);
            holder.userName.setText("by "+name+"\n(playable)");
            //holder.text.setText(history.get(position).getMultimediaFileName()+"(playable)");
        }

        if(isTextFile(history.get(position).getPath())){
            holder.photo.setVisibility(View.GONE);
            holder.userName.setText("by "+name+":"+new String(history.get(position).getMultimediaFileChunk(), StandardCharsets.UTF_8));
        }


    }

    @Override
    public int getItemCount() {
        return history.size();
    }
}
