package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    //Activity Components
    private Toolbar toolbar;
    private LinearLayout linearLayoutChatLists;
    private ScrollView scChatList;
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
        linearLayoutChatLists = findViewById(R.id.linear_layout_chat_lists);
        scChatList = findViewById(R.id.scChatList);
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

                final String toUserId,fromUserId;

                final User[] users = new User[2];
                final Uri[] usersProfile = new Uri[2];

                if(currentUser.getUid().equals(conversation.getUsersId().get(0))){
                    toUserId = conversation.getUsersId().get(0);
                    fromUserId = conversation.getUsersId().get(1);
                }else{
                    toUserId = conversation.getUsersId().get(1);
                    fromUserId = conversation.getUsersId().get(0);
                }

                final DatabaseReference toUserReference = FirebaseDatabase.getInstance().getReference("users").child(toUserId);
                final DatabaseReference fromUserReference = FirebaseDatabase.getInstance().getReference("users").child(fromUserId);

                toUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        users[0] = dataSnapshot.getValue(User.class);
                        toUser = users[0];

                        final File file;
                        try{
                            file = File.createTempFile("image","png");
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference(users[0].getProfileImagePath());
                            storageReference.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){
                                        usersProfile[0] = Uri.fromFile(file);
                                    }

                                    fromUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            users[1] = dataSnapshot.getValue(User.class);
                                            fromUser = users[1];

                                            getSupportActionBar().setTitle(fromUser.getFirstName());

                                            final File file2;
                                            try{
                                                file2 = File.createTempFile("image","png");
                                                StorageReference reference = FirebaseStorage.getInstance().getReference(users[1].getProfileImagePath());
                                                reference.getFile(file2).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                                        if(task.isSuccessful()){
                                                            usersProfile[1] = Uri.fromFile(file2);
                                                        }

                                                        final DatabaseReference referenceForConversation = FirebaseDatabase.getInstance().getReference("conversation").child(conversationId).child("convo");
                                                        referenceForConversation.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                Log.d("CHAT EVENT LISTENER","RUNNING");
                                                                linearLayoutChatLists.removeAllViews();
                                                                messages = new ArrayList<>();
                                                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                                    messages.add(ds.getValue(Conversation.Message.class));
                                                                }
                                                                Log.d(Integer.toString(messages.size()),Long.toString(dataSnapshot.getChildrenCount()));
                                                                SetupChatList(messages,users,usersProfile);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                databaseError.toException().printStackTrace();
                                                            }
                                                        });
                                                    }
                                                });
                                            }catch (Exception ex){
                                                ex.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
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

    private void AddNewMessage(Conversation.Message message, User[] userLists,  Uri[] userProfile){
        View itemView = LayoutInflater.from(this).inflate(R.layout.chat_layout_item,null,false);

        CircleImageView civFrom,civTo;
        TextView txtvFromMessage,txtvToMessage;
        TextView txtvFromStatus,txtvToStatus;
        TextView txtvViewLocationLayout;

        RelativeLayout fromLayout,toLayout,viewLocationLayout;

        fromLayout = itemView.findViewById(R.id.layout_from_messages);
        civFrom = itemView.findViewById(R.id.chat_civ_head_from);
        txtvFromMessage = itemView.findViewById(R.id.chat_txtv_message_content_from);
        txtvFromStatus = itemView.findViewById(R.id.chat_txtv_message_status);

        toLayout = itemView.findViewById(R.id.layout_to_messages);
        civTo = itemView.findViewById(R.id.chat_civ_head_to);
        txtvToMessage = itemView.findViewById(R.id.chat_txtv_message_content_to);
        txtvToStatus = itemView.findViewById(R.id.txtv_to_message_status);

        viewLocationLayout = itemView.findViewById(R.id.chat_layout_view_location);
        txtvViewLocationLayout = itemView.findViewById(R.id.chat_view_location_textv);

        Conversation.Message m = message;
        if(userLists[0].getUid().equals(m.getSenderUid())){
            //To user is the sender.
            fromLayout.setVisibility(View.GONE);
            if(m.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                //Message is a Location Viewer
                toLayout.setVisibility(View.GONE);
                txtvViewLocationLayout.setText("My Location");
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Alert(ChatActivity.this).showErrorMessage("Notification","This is your own location.");
                    }
                });
            }else{
                //Message is not a Location Viewer
                viewLocationLayout.setVisibility(View.GONE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(m.getDatetimeSent());

                //Setup Message
                civTo.setImageURI(userProfile[0]);
                txtvToMessage.setText(m.getMessageContent());
                txtvToStatus.setText(m.getMessageStatus() + " - " + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.YEAR));
            }
        }else{
            //From user is the sender
            toLayout.setVisibility(View.GONE);
            if(m.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                fromLayout.setVisibility(View.GONE);
                txtvViewLocationLayout.setText("View Persons Location");
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Intent to Location Viewer
                    }
                });
            }else{
                txtvViewLocationLayout.setVisibility(View.GONE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(m.getDatetimeSent());

                //Setup Message
                civFrom.setImageURI(userProfile[1]);
                txtvFromMessage.setText(m.getMessageContent());
                txtvFromStatus.setText(m.getMessageStatus() + " - " + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.YEAR));
            }
        }

        linearLayoutChatLists.addView(itemView);
    }

    private void SetupChatList(List<Conversation.Message> messages,User[] userLists,  Uri[] userProfile){
        for(Conversation.Message m : messages){
            //Setup View
            View itemView = LayoutInflater.from(this).inflate(R.layout.chat_layout_item,null,false);

            CircleImageView civFrom,civTo;
            TextView txtvFromMessage,txtvToMessage;
            TextView txtvFromStatus,txtvToStatus;
            TextView txtvViewLocationLayout;

            RelativeLayout fromLayout,toLayout,viewLocationLayout;

            fromLayout = itemView.findViewById(R.id.layout_from_messages);
            civFrom = itemView.findViewById(R.id.chat_civ_head_from);
            txtvFromMessage = itemView.findViewById(R.id.chat_txtv_message_content_from);
            txtvFromStatus = itemView.findViewById(R.id.chat_txtv_message_status);

            toLayout = itemView.findViewById(R.id.layout_to_messages);
            civTo = itemView.findViewById(R.id.chat_civ_head_to);
            txtvToMessage = itemView.findViewById(R.id.chat_txtv_message_content_to);
            txtvToStatus = itemView.findViewById(R.id.txtv_to_message_status);

            viewLocationLayout = itemView.findViewById(R.id.chat_layout_view_location);
            txtvViewLocationLayout = itemView.findViewById(R.id.chat_view_location_textv);

            Log.d(m.getSenderUid(),m.getMessageContent());

            if(userLists[0].getUid().equals(m.getSenderUid())){
                //To user is the sender.
                fromLayout.setVisibility(View.GONE);
                if(m.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                    //Message is a Location Viewer
                    toLayout.setVisibility(View.GONE);
                    txtvViewLocationLayout.setText("My Location");
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Alert(ChatActivity.this).showErrorMessage("Notification","This is your own location.");
                        }
                    });
                }else{
                    //Message is not a Location Viewer
                    viewLocationLayout.setVisibility(View.GONE);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(m.getDatetimeSent());

                    //Setup Message
                    civTo.setImageURI(userProfile[0]);
                    txtvToMessage.setText(m.getMessageContent());
                    txtvToStatus.setText(m.getMessageStatus() + " - " + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.YEAR));
                }
            }else{
                //From user is the sender
                toLayout.setVisibility(View.GONE);
                if(m.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                    fromLayout.setVisibility(View.GONE);
                    txtvViewLocationLayout.setText("View Persons Location");
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Intent to Location Viewer
                        }
                    });
                }else{
                    txtvViewLocationLayout.setVisibility(View.GONE);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(m.getDatetimeSent());

                    //Setup Message
                    civFrom.setImageURI(userProfile[1]);
                    txtvFromMessage.setText(m.getMessageContent());
                    txtvFromStatus.setText(m.getMessageStatus() + " - " + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.YEAR));
                }
            }

            linearLayoutChatLists.addView(itemView);
        }

        scChatList.post(new Runnable() {
            @Override
            public void run() {
                scChatList.scrollTo(0,linearLayoutChatLists.getBottom());
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

                //Send message
                FirebaseDatabase.getInstance().getReference("conversation").child(conversationId).child("convo").push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        etxtMessageContent.setText("");
                    }
                });

                //Put up the last message in the users data
                fromUser.getConversationIds().put(conversationId,message);
                fromUserProfileReference = FirebaseDatabase.getInstance().getReference("users").child(fromUser.getUid());
                fromUserProfileReference.child("conversationIds").setValue(fromUser.getConversationIds());

                toUser.getConversationIds().put(conversationId,message);
                toUserProfileReference = FirebaseDatabase.getInstance().getReference("users").child(toUser.getUid());
                toUserProfileReference.child("conversationIds").setValue(toUser.getConversationIds());
            }
        }catch (Exception ex){
            alert.showErrorMessage("Notification",ex.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        linearLayoutChatLists.removeAllViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        linearLayoutChatLists.removeAllViews();
    }


}
