package com.ipy849.fakebook;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // UI
    private MaterialToolbar toolbar;
    private ImageButton loadPostImageButton;
    private RecyclerView postFeed;
    private TextInputEditText newPostContent; // add_post_text_input
    private ImageButton sendPostButton; // add_post_post
    private RelativeLayout loadedImageLayout; // add_post_preview_loaded_image_layout
    private ImageView loadedPhoto; // add_post_loaded_image
    private FloatingActionButton closeLoadedImageLayoutButton; // add_post_close_loaded_image_layout

    // Data
    private Pair<byte[], String> toLoadImageData; // Datos de la imágen a cargarse, <info en bits, local path>
    private long lastClickTime = 0;


    ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    loadedPhoto.setImageURI(result);

                    try {
                        Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), result);
                        ByteArrayOutputStream outputImageBinaries = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.PNG, 100, outputImageBinaries);
                        toLoadImageData = new Pair<>(outputImageBinaries.toByteArray(), "post_images/post_image_" + UUID.randomUUID() + ".png");
                        Logger.d("Se cargaron los datos de la imagen correctamente");
                        LoadImageFirebaseStorage();
                    } catch (Exception e) {
                        Logger.e(e.toString());
                        finish();
                    }

                    loadedImageLayout.setVisibility(View.VISIBLE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.addLogAdapter(new AndroidLogAdapter());

        Logger.d("Agarrando referencias a los elementos del layout");
        // Toolbar por ActionBar
        toolbar = (MaterialToolbar) findViewById(R.id.top_appBar);
        setSupportActionBar(toolbar);

        // Elementos de publicación de posts
        newPostContent = (TextInputEditText) findViewById(R.id.add_post_text_input);

        loadPostImageButton = (ImageButton) findViewById(R.id.add_post_search_image);
        loadPostImageButton.setOnClickListener(this);
        sendPostButton = (ImageButton) findViewById(R.id.add_post_post);
        sendPostButton.setOnClickListener(this);
        loadedImageLayout = (RelativeLayout) findViewById(R.id.add_post_preview_loaded_image_layout);
        loadedPhoto = (ImageView) findViewById(R.id.add_post_loaded_image);
        closeLoadedImageLayoutButton = (FloatingActionButton) findViewById(R.id.add_post_close_loaded_image_layout);
        closeLoadedImageLayoutButton.setOnClickListener(this);

        FetchRecyclerViewData();
    }

    void FetchRecyclerViewData(){
        FirebaseDatabase firebaseRealtimeDatabase = FirebaseDatabase.getInstance();
        DatabaseReference postDatabaseReference = firebaseRealtimeDatabase.getReference("posts");
        postDatabaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    return;
                }

                ArrayList<HashMap<String, String>> receivedData = (ArrayList<HashMap<String, String>>) task.getResult().getValue();
                ArrayList<Post> postsData = new ArrayList<>();

                for (HashMap<String, String> data: receivedData) {
                    Post post = new Post();
                    post.setUser(data.get("name"));
                    post.setCaption(data.get("caption"));
                    post.setContent(data.get("content"));
                    postsData.add(post);
                }

                // Recycler View
                postFeed = (RecyclerView) findViewById(R.id.recycler_post_feed);
                postFeed.setHasFixedSize(true);
                postFeed.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                postFeed.setAdapter(new PostAdapter(postsData));
            }
        });
    }



    void LaunchLoadImageActivity() {
        Logger.d("Se va a lanzar la activity de selección de recursos");
        activityResultLauncher.launch("image/*");
    }

    void LoadImageFirebaseStorage(){
        if(toLoadImageData == null){
            Logger.d("No image to upload");
            return;
        }

        Logger.d("Se va a cargar una imagen a Storage en Firebase");
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference firebaseStorageImageReference = firebaseStorage.getReference(toLoadImageData.second);
        /*
         * El tutorial oficial de firebase dice que use "putBytes" pero la documentación dice que
         * use putStream o putFile para ahorrar memoria.
         */

        UploadTask uploadTask = firebaseStorageImageReference.putBytes(toLoadImageData.first);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return firebaseStorageImageReference.getDownloadUrl();
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Logger.v("Imagen cargada con exito: " + uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logger.e("Falló la carga de la image :(");
            }
        });
    }

    /*
    public void SendPost(){
        Logger.e(newPostContent.getText().toString());
        while (toLoadImageData != null && imageDownloadUri == null){}
        Logger.e(imageDownloadUri == null ?"":imageDownloadUri.toString());
    }
    */

    @Override
    public void onClick(View view) {
        // Controla que no se hagan clicks cada menos de 200ms
        if (SystemClock.elapsedRealtime() - lastClickTime < 200) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        Logger.d("entro");

        int id = view.getId();
        if (id == 0) return;

        //
        switch (id) {
            case R.id.add_post_search_image: {
                LaunchLoadImageActivity();
                break;
            }
            case R.id.add_post_close_loaded_image_layout: {
                loadedImageLayout.setVisibility(View.GONE);
                toLoadImageData = null;
                break;
            }
            case R.id.add_post_post:{
                Logger.d("Que pedo pedo");
                break;
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
        switch (id) {
            case R.id.top_appBar_messenger_button: {
                Toast.makeText(this, "Vas a entrar a Messenger", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.top_appBar_search_button: {
                Toast.makeText(this, "Vas a a buscar a una persona", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.top_appBar_scanner_button: {
                Toast.makeText(this, "Vas a a escanear un código QR", Toast.LENGTH_SHORT).show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}