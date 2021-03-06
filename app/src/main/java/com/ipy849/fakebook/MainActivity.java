package com.ipy849.fakebook;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.google.firebase.database.ChildEventListener;
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
    private Pair<byte[], String> toLoadImageData; // Datos de la im??gen a cargarse, <info en bits, local path>
    private long lastClickTime = 0;
    private LiveDataPost newPostData;
    private int nextPostIndex;
    final private FirebaseDatabase firebaseRealtimeDatabase = FirebaseDatabase.getInstance();


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
        newPostData = new ViewModelProvider(this).get(LiveDataPost.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.addLogAdapter(new AndroidLogAdapter());

        Logger.d("Agarrando referencias a los elementos del layout");
        // Toolbar por ActionBar
        toolbar = (MaterialToolbar) findViewById(R.id.top_appBar);
        setSupportActionBar(toolbar);

        // Elementos de publicaci??n de posts
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
        DatabaseReference postDatabaseReference = firebaseRealtimeDatabase.getReference("posts");

        /*
         Entiendo que esto es un listener general para cuando cambia el estado de la referencia
         pero es ineficiente cargar todos los datos otra vez, encontr?? en la documentaci??n un listener
         para los hijos de la referencia y maneja un crud completo y error, supongo que ser??a la mejor
         manera.
         */
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) return;

                ArrayList<HashMap<String, String>> receivedData = (ArrayList<HashMap<String, String>>) snapshot.getValue();
                ArrayList<Post> postsData = new ArrayList<>();

                for (int i = 0; i < receivedData.size(); i++) {
                    HashMap<String, String> data = receivedData.get(i);
                    Post post = new Post();
                    post.setUser(data.get("name"));
                    post.setCaption(data.get("caption"));
                    post.setContent(data.get("content"));
                    post.setId(i);
                    postsData.add(post);
                }

                nextPostIndex = receivedData.size();

                // Recycler View
                postFeed = (RecyclerView) findViewById(R.id.recycler_post_feed);
                postFeed.setHasFixedSize(true);
                postFeed.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                postFeed.setAdapter(new PostAdapter(postsData));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Logger.e("Post Load Cancelled", error.toException());
            }
        };

        postDatabaseReference.addValueEventListener(postListener);
    }



    void LaunchLoadImageActivity() {
        Logger.d("Se va a lanzar la activity de selecci??n de recursos");
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
         * El tutorial oficial de firebase dice que use "putBytes" pero la documentaci??n dice que
         * use putStream o putFile para ahorrar memoria.
         */

        UploadTask uploadTask = firebaseStorageImageReference.putBytes(toLoadImageData.first);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                sendPostButton.setEnabled(false);
                return firebaseStorageImageReference.getDownloadUrl();
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Logger.v("Imagen cargada con exito: " + uri.toString());
                newPostData.getPostData().getValue().setContent(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logger.e("Fall?? la carga de la image :(");
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                sendPostButton.setEnabled(true);
                toLoadImageData = null;
            }
        });
    }


    public void SendPost(){
        Logger.d("Voy a postear");
        newPostData.getPostData().getValue().setUser("Usuario de esta app");
        String postText = newPostContent.getText().toString();
        if(TextUtils.isEmpty(postText)){
            Toast.makeText(this, "Debes escribir en la publicaci??n", Toast.LENGTH_LONG).show();
            return;
        }
        newPostData.getPostData().getValue().setCaption(postText);

        DatabaseReference postDatabaseReference = firebaseRealtimeDatabase.getReference("posts/" + String.valueOf(nextPostIndex));
        newPostData.LoadPost(postDatabaseReference);
        CloseImagePreview();
        newPostContent.setText("");
        newPostContent.clearFocus();
    }

    public void CloseImagePreview(){
        loadedImageLayout.setVisibility(View.GONE);
        toLoadImageData = null;
    }


    @Override
    public void onClick(View view) {
        // Controla que no se hagan clicks cada menos de 200ms
        if (SystemClock.elapsedRealtime() - lastClickTime < 200) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        Logger.d("Me tocaste");

        int id = view.getId();
        if (id == 0) return;

        //
        switch (id) {
            case R.id.add_post_search_image: {
                LaunchLoadImageActivity();
                break;
            }
            case R.id.add_post_close_loaded_image_layout: {
                CloseImagePreview();
                break;
            }
            case R.id.add_post_post:{
                SendPost();
                break;
            }
            default:
                return;
        }

        // Esconde el teclado luego de cualquier interacci??n
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(newPostContent.getApplicationWindowToken(),  0);
    }

    // Evento de creaci??n del men?? del Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }


    // Interacci??n click con los elements del menu del Toolbar
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
                Toast.makeText(this, "Vas a a escanear un c??digo QR", Toast.LENGTH_SHORT).show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}