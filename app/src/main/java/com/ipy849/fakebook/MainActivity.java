package com.ipy849.fakebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MaterialToolbar toolbar;
    private ImageButton loadPostImageButton;
    private RecyclerView postFeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Toolbar por ActionBar
        toolbar = (MaterialToolbar) findViewById(R.id.top_appBar);
        setSupportActionBar(toolbar);

        // Elementos
        loadPostImageButton = (ImageButton) findViewById(R.id.add_post_search_image);
        loadPostImageButton.setOnClickListener(this);

        // Recycler View
        postFeed = (RecyclerView) findViewById(R.id.recycler_post_feed);
        postFeed.setHasFixedSize(true);
        postFeed.setLayoutManager(new LinearLayoutManager(this));
        postFeed.setAdapter(new PostAdapter(Post.GenerateContent(10)));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == 0) return;

        switch (id){
            case R.id.add_post_search_image: {
                Toast.makeText(this, "Vas a cargar una foto", Toast.LENGTH_SHORT).show();
                return;
            }
            default:
                return;
        }
    }

    // Evento de creación del menú del Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }


    // Interacción click con los elements del menu del Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.top_appBar_messenger_button:{
                Toast.makeText(this, "Vas a entrar a Messenger", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.top_appBar_search_button: {
                Toast.makeText(this, "Vas a a buscar a una persona", Toast.LENGTH_SHORT).show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}