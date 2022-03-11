package com.ipy849.fakebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private List<Post> posts;

    public PostAdapter(List<Post> posts){
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fakebook_feed_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("PRUEBA", Integer.toString(position));
        holder.AdjustContent(this.posts.get(position));
    }

    @Override
    public int getItemCount() {
        return this.posts.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ConstraintLayout postLayout;
        private final TextView username;
        private final TextView text;
        private final ImageView imageContent;

        public ViewHolder(View view) {
            super(view);
            postLayout = (ConstraintLayout) view.findViewById(R.id.feed_pos);
            username = (TextView) view.findViewById(R.id.feed_post_user_name);
            text = (TextView) view.findViewById(R.id.feed_post_text);
            imageContent = (ImageView) view.findViewById(R.id.feed_post_photo);
        }

        public void AdjustContent(Post post)  {
            post.print();

            this.username.setText(post.getUser());

            // Comprobar contenido
            if(post.getCaption() == null && post.getContent() == null)
                postLayout.setVisibility(View.GONE);

            // Comprobar media
            if (post.getContent() != null){
                Thread imageLoadThread = new Thread(() -> {
                    try {
                        URL newurl = new URL(post.getContent());
                        this.imageContent.setImageBitmap(BitmapFactory.decodeStream(newurl.openConnection().getInputStream()));
                    } catch (IOException e){
                        Log.d("PRUEBA", e.getMessage());

                    } catch (Exception e){
                        Log.d("PRUEBA", e.getMessage());

                    }
                    Log.d("PRUEBA", "SALIENDO");
                });
                imageLoadThread.start();

            } else {
                // this.videoContent.setVisibility(View.GONE);
                this.imageContent.setVisibility(View.GONE);
            }

            // Comprobar texto
            if (post.getCaption() != null){
                this.text.setText(post.getCaption());
            } else {
                // this.videoContent.setVisibility(View.GONE);
                this.imageContent.setVisibility(View.GONE);
            }
        }
    }

}
