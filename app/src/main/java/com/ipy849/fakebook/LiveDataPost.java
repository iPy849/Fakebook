package com.ipy849.fakebook;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.orhanobut.logger.Logger;


public class LiveDataPost extends ViewModel {
    public MutableLiveData<Post> postData = new MutableLiveData<>();

    public LiveDataPost() {
        postData.setValue(new Post());
    }

    public MutableLiveData<Post> getPostData() {
        if (postData == null)
            return new MutableLiveData<Post>();
        return postData;
    }

    public void LoadPost(DatabaseReference realTimeDatabaseReference) {
        realTimeDatabaseReference.setValue(postData.getValue().toHashMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Logger.d("Se ha cargado la publicación con éxito");
                postData.setValue(new Post());
            }
        });
    }
}
