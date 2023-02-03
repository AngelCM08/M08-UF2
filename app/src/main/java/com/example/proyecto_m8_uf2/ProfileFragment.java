package com.example.proyecto_m8_uf2;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.net.URL;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    ImageView photoImageView;
    TextView displayNameTextView, emailTextView;
    FirebaseUser user;
    public AppViewModel appViewModel;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        photoImageView = view.findViewById(R.id.photoImageView);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);

        user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        displayNameTextView.setText(documentSnapshot.get("profileName").toString());
                        if (documentSnapshot.get("profilePhoto") == null) {
                            Glide.with(getContext()).load(R.drawable.user).circleCrop().into(photoImageView);
                        } else {
                            Glide.with(getContext()).load(documentSnapshot.get("profilePhoto")).circleCrop().into(photoImageView);
                        }
                        view.findViewById(R.id.photoImageView).setOnClickListener(v -> galeria.launch("image/*"));
                        appViewModel.mediaSeleccionado.observe(getViewLifecycleOwner(), media -> {
                            if (media.uri != null) {
                                Glide.with(getContext()).load(media.uri).into((ImageView) view.findViewById(R.id.photoImageView));
                                pujaIguardarEnFirestore(media.uri);
                            }
                        });
                    } else {
                        displayNameTextView.setText(user.getDisplayName());
                        emailTextView.setText(user.getEmail());

                        Glide.with(requireView()).load(user.getPhotoUrl()).into(photoImageView);
                    }

                });
    }

    @Override
    public void onStop() {
        super.onStop();
        appViewModel.setMediaSeleccionado(null, null);
    }

    protected void pujaIguardarEnFirestore(final Uri mediaUri) {
        FirebaseStorage.getInstance().getReference("usersPhoto/" +
                        UUID.randomUUID())
                .putFile(mediaUri)
                .continueWithTask(task ->
                        task.getResult().getStorage().getDownloadUrl())
                .addOnSuccessListener(url ->
                        FirebaseFirestore.getInstance().collection("users")
                                .document(user.getUid()).update("profilePhoto", url.toString())
                );
    }

    protected final ActivityResultLauncher<String> galeria =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> appViewModel.setMediaSeleccionado(uri, "image"));
}