package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    //Activity Components
    private Toolbar toolbar;
    private RecyclerView recyclerViewchatView;
    private EditText etxtMessageContent;
    private Button btnSendMessage;

    private DatabaseReference conversationContentReference;
    private DatabaseReference conversationReference;
    private DatabaseReference fromUserProfileReference;
    private DatabaseReference toUserProfileReference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private Conversation conversation;
    private List<Conversation.Message> messages;
    private User fromUser;
    private User toUser;

    private Uri fromUserProfilePhoto;
    private Uri toUserProfilePhoto;

    private String conversationId;

    private Alert alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
    }

    private void init(){
        toolbar = findViewById(R.id.chat_toolbar);
        recyclerViewchatView = findViewById(R.id.recyclerview_chat_view);
        etxtMessageContent = findViewById(R.id.etxt_chat_message_content);
        btnSendMessage = findViewById(R.id.btn_chat_send_message);

        conversationId = getIntent().getExtras().getString("CONVERSATION_ID");

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(conversationId);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Create Alert
        alert = new Alert(this);

        //Load Current User
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        //Load the users Data
        conversationReference = FirebaseDatabase.getInstance().getReference("conversations").child(conversationId);
        conversationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                conversation = dataSnapshot.getValue(Conversation.class);

                if(conversation.getUsersId().get(0).equals(currentUser.getUid())){
                    toUserProfileReference = FirebaseDatabase.getInstance().getReference("users").child(conversation.getUsersId().get(0));
                    fromUserProfileReference = FirebaseDatabase.getInstance().getReference("users").child(conversation.getUsersId().get(1));
                }else{
                    toUserProfileReference = FirebaseDatabase.getInstance().getReference("users").child(conversation.getUsersId().get(1));
                    fromUserProfileReference = FirebaseDatabase.getInstance().getReference("users").child(conversation.getUsersId().get(0));
                }

                toUserProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        toUser = dataSnapshot.getValue(User.class);

                        final File file;
                        try{
                            file = File.createTempFile("image","png");
                            StorageReference reference = FirebaseStorage.getInstance().getReference().child(toUser.getProfileImagePath());
                            reference.getFile(file)
                                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                toUserProfilePhoto = Uri.fromFile(file);
                                            }else{
                                                task.getException().toString();
                                            }
                                        }
                                    });
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                        fromUserProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                fromUser = dataSnapshot.getValue(User.class);

                                final File file;
                                try{
                                    file = File.createTempFile("image","png");
                                    StorageReference reference = FirebaseStorage.getInstance().getReference().child(fromUser.getProfileImagePath());
                                    reference.getFile(file)
                                            .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                                    try{
                                                        fromUserProfilePhoto = Uri.fromFile(file);
                                                    }catch (Exception ex){
                                                        ex.printStackTrace();
                                                    }
                                                }
                                            });
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }

                                getSupportActionBar().setTitle(fromUser.getFirstName() + " " + fromUser.getLastName());

                                conversationContentReference = conversationReference.child("conversationContent");
                                conversationContentReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        messages = new ArrayList<>();
                                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                                            Conversation.Message message = ds.getValue(Conversation.Message.class);

                                            if(!message.getSenderUid().equals(currentUser.getUid()) && message.getMessageStatus().equals(Conversation.STATUS_SENT)){
                                                message.setMessageStatus(Conversation.STATUS_SEEN);
                                            }

                                            messages.add(message);
                                        }

                                        MessageContentAdapter adapter = new MessageContentAdapter(ChatActivity.this,messages);
                                        adapter.setFromUser(fromUser);
                                        adapter.setToUser(toUser);
                                        adapter.setFromUserProfile(fromUserProfilePhoto);
                                        adapter.setToUserProfile(toUserProfilePhoto);

                                        recyclerViewchatView.setHasFixedSize(true);
                                        recyclerViewchatView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                                        recyclerViewchatView.setAdapter(adapter);
                                        recyclerViewchatView.scrollToPosition(adapter.getItemCount() -1);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void clickSendMessage(View view){
        try{
            //Fetch the data
            String messageContent = etxtMessageContent.getText().toString().trim();

            //Check if it is empty.
            if(!messageContent.isEmpty()){
                Conversation.Message message = new Conversation.Message(messageContent,currentUser.getUid(), Calendar.getInstance().getTimeInMillis());

                //Put up as the last message
                conversation.setLastMessage(message);
                conversationReference.setValue(conversation);

                //Send the message
                messages.add(message);
                conversationContentReference.setValue(messages);
            }
        }catch (Exception ex){
            alert.showErrorMessage("Notification",ex.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }
}
