package com.coecs.project012;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListsAdapter extends RecyclerView.Adapter<MessageListsAdapter.ConversationViewHolder> {

    private Context context;
    private List<String> conversationIds;

    public MessageListsAdapter(Context context, List<String> conversationIds) {
        this.context = context;
        this.conversationIds = conversationIds;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.conversation_list_item,parent,false);
        MessageListsAdapter.ConversationViewHolder holder = new ConversationViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ConversationViewHolder holder, int position) {
        //Fetch Conversation ID
        final String id = conversationIds.get(position);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("conversations").child(id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Conversation convo = dataSnapshot.getValue(Conversation.class);

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                List<String> users = convo.getUsersId();
                users.remove(currentUser.getUid());
                String otherUser = users.get(0);

                //Fetch user Data
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(otherUser);
                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        Conversation.Message lastMessage = convo.getLastMessage();

                        //Fill in User Data
                        holder.mainText.setText(user.getFirstName() + " " + user.getLastName());
                        holder.subText.setText(lastMessage.getMessageContent());
                        holder.statusText.setText(lastMessage.getMessageStatus());
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context,ChatActivity.class);
                                intent.putExtra("CONVERSATION_ID",id);
                                context.startActivity(intent);
                            }
                        });

                        //Fetch user image
                        final File file;
                        try{
                            file = File.createTempFile("image","png");
                            StorageReference displayPhotoStorage = FirebaseStorage.getInstance().getReference(user.getProfileImagePath());
                            displayPhotoStorage.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                    holder.civProfilePhoto.setImageURI(Uri.fromFile(file));
                                }
                            });
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                        if(lastMessage.getMessageStatus().equals(Conversation.STATUS_SENT)){
                            holder.subText.setTypeface(holder.subText.getTypeface(), Typeface.BOLD);
                            holder.statusText.setTypeface(holder.statusText.getTypeface(), Typeface.BOLD);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        databaseError.toException().printStackTrace();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationIds.size();
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView civProfilePhoto;
        private TextView mainText;
        private TextView subText;
        private TextView statusText;
        private View view;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
            civProfilePhoto = itemView.findViewById(R.id.coversation_list_item_other_user_profile);
            mainText = itemView.findViewById(R.id.conversation_list_item_txtv_other_name);
            subText = itemView.findViewById(R.id.conversation_list_item_txtv_last_message);
            statusText = itemView.findViewById(R.id.conversation_list_item_txtv_status);
        }
    }
}
