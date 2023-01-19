package com.example.proyecto_m8_uf2;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NewPostFragment extends Fragment {

    Button publishButton;
    EditText postConentEditText;
    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        publishButton = view.findViewById(R.id.publishButton);
        postConentEditText = view.findViewById(R.id.postContentEditText);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                publicar();
            }
        });

        navController = Navigation.findNavController(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void publicar() {
        String postContent = postConentEditText.getText().toString();

        if(TextUtils.isEmpty(postContent)){
            postConentEditText.setError("Required");
            return;
        }

        publishButton.setEnabled(false);

        guardarEnFirestore(postContent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void guardarEnFirestore(String postContent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String foto;

        if(user.getPhotoUrl() != null){
            foto = user.getPhotoUrl().toString();
        }else{
            foto = null;
        }
        Post post = new Post(user.getUid(), user.getDisplayName(), foto, postContent, Timestamp.now());

        FirebaseFirestore.getInstance().collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        navController.popBackStack();
                    }
                });
    }
}