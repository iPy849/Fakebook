package com.ipy849.fakebook;

import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        private final ImageButton postOption;

        public ViewHolder(View view) {
            super(view);
            postLayout = (ConstraintLayout) view.findViewById(R.id.feed_pos);
            username = (TextView) view.findViewById(R.id.feed_post_user_name);
            text = (TextView) view.findViewById(R.id.feed_post_text);
            imageContent = (ImageView) view.findViewById(R.id.feed_post_photo);
            postOption = (ImageButton) view.findViewById(R.id.feed_post_options);
        }

        public void AdjustContent(Post post){
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

            // Menu
            postOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                    MenuInflater inflater = popupMenu.getMenuInflater();

                    // Eventos de cada botón de menu
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("posts/" + String.valueOf(post.getId()));
                            switch (menuItem.getItemId()){
                                // Evento de editar
                                case R.id.feed_post_options_edit:{
                                    // Será más fácil hacer un
                                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                    builder.setTitle("¿Quieres actualizar la publicación?");
                                    EditText input = new EditText(view.getContext());
                                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                                    builder.setView(input);

                                    builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            reference.child("caption").setValue(input.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(view.getContext(), "Publicación actualizada con éxito", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });
                                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                                    builder.show();
                                    return true;
                                }
                                // Evento de Borrar
                                case R.id.feed_post_options_delete:{
                                    reference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(view.getContext(), "Publicación borrada", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    return true;
                                }
                                default:{
                                    return false;
                                }
                            }
                        }
                    });

                    // Inflar menu
                    inflater.inflate(R.menu.feed_post_menu, popupMenu.getMenu());
                    popupMenu.show();
                }
            });
        }
    }

}
