package com.ipy849.fakebook;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    final private List<Post> posts;

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
                this.imageContent.setVisibility(View.VISIBLE);
                Glide.with(this.imageContent.getContext()).load(post.getContent()).into(this.imageContent);
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
