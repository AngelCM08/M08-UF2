package com.example.proyecto_m8_uf2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class HomeFragment extends Fragment {
    NavController navController;
    public AppViewModel appViewModel;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
            }
        });
        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        Query query = FirebaseFirestore.getInstance().collection("posts").limit(50).orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();

        postsRecyclerView.setAdapter(new PostsAdapter(options));
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
    }

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {super(options);}

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Post post) {
            Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
            holder.authorTextView.setText(post.author);
            holder.contentTextView.setText(post.content);
            holder.dateTextView.setText(post.date.toDate().toString());
            final String AuthUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Paperera de publicacions pròpies
            if(Objects.equals(post.uid, AuthUid)){
                holder.retweetImageView.setVisibility(View.GONE);

                holder.trashImageView.setVisibility(View.VISIBLE);
                holder.trashImageView.setOnClickListener(view -> {
                    eliminarPost(post);
                });
            }else {
                holder.trashImageView.setVisibility(View.GONE);

                holder.retweetImageView.setVisibility(View.VISIBLE);
                holder.retweetImageView.setOnClickListener(view -> {
                    retweetPost(post);
                });
            }

            // Gestion de likes
            final String postKey = getSnapshots().getSnapshot(position).getId();
            if(post.likes.containsKey(AuthUid))
                holder.likeImageView.setImageResource(R.drawable.like_on);
            else
                holder.likeImageView.setImageResource(R.drawable.like_off);
            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
            holder.likeImageView.setOnClickListener(view -> {
                FirebaseFirestore.getInstance().collection("posts")
                        .document(postKey)
                        .update("likes."+AuthUid, post.likes.containsKey(AuthUid) ?
                                FieldValue.delete() : true);
            });

            // Miniatura de media
            if (post.mediaUrl != null) {
                holder.mediaImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.audio).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
            } else {
                holder.mediaImageView.setVisibility(View.GONE);
            }

        }

        private void eliminarPost(Post post) {
            FirebaseFirestore.getInstance().collection("posts").document(post.postId)
                    .delete();
        }

        private void retweetPost(Post post) {
            Task<DocumentSnapshot> copiedPost = FirebaseFirestore.getInstance().collection("posts").document(post.postId).get();
            copiedPost.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Map<String, Object> data = documentSnapshot.getData();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Post post = new Post(
                            user.getUid(),
                            user.getDisplayName(),
                            (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null),
                            (data.get("content") != null ? data.get("content").toString() : null),
                            (data.get("mediaUrl") != null ? data.get("mediaUrl").toString() : null),
                            (data.get("mediaType") != null ? data.get("mediaType").toString() : null),
                            Timestamp.now()
                    );

                    FirebaseFirestore.getInstance().collection("posts")
                            .add(post)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    appViewModel.setMediaSeleccionado(null, null);
                                    documentReference.update("postId", documentReference.getId());
                                }
                            });
                }
            });
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView authorPhotoImageView, likeImageView, mediaImageView, trashImageView, retweetImageView;
            TextView authorTextView, contentTextView, numLikesTextView, dateTextView;

            PostViewHolder(@NonNull View itemView) {
                super(itemView);

                authorPhotoImageView = itemView.findViewById(R.id.photoImageView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                trashImageView = itemView.findViewById(R.id.trashImageView);
                retweetImageView = itemView.findViewById(R.id.retweetImageView);
            }
        }
    }
}