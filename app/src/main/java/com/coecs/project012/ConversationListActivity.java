package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.coecs.project012.Conversation.*;

public class ConversationListActivity extends AppCompatActivity {
    //Activity Components
    private Toolbar toolbar;
    private ScrollView recyclerView;
    private LinearLayout linearlayoutChatList;
    private TextView txtvEmptyList;

    //Database
    private DatabaseReference conversations;
    private DatabaseReference userConversationLists;
    private FirebaseUser currentUser;
    private User currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        init();
    }

    private void init(){
        toolbar = findViewById(R.id.chat_list_toolbar);
        recyclerView = findViewById(R.id.recylerView_conversation_list);
        txtvEmptyList = findViewById(R.id.txtv_empty_list_notif);
        linearlayoutChatList = findViewById(R.id.linearLayoutConversationList);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        conversations = FirebaseDatabase.getInstance().getReference("conversations");
        userConversationLists = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("conversationIds");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Messages");

        //Fill in recylcerView
        userConversationLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                linearlayoutChatList.removeAllViews();
                Map<String,Message> map = (Map)dataSnapshot.getValue();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String key = ds.getKey();
                    Message message = ds.getValue(Message.class);
                    Log.d(key,Long.toString(message.getDatetimeSent()));
                    if(message.getDatetimeSent() != 0){
                        map.put(key,message);
                    }
                }

                if(map != null){
                    txtvEmptyList.setVisibility(View.GONE);
                    List<String> listOfConversationIds = sortMap(map);

                    //Setup Messages
                    SetupConversationLists(listOfConversationIds);
                }else{
                    txtvEmptyList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }

    private void SetupConversationLists(List<String> listOfConversationIds){
        for(int i = 0; i<listOfConversationIds.size(); i++){
            Log.d("Listing","Listing Data");
            final String conversationId = listOfConversationIds.get(i);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("conversations").child(conversationId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Conversation convo = dataSnapshot.getValue(Conversation.class);

                    final Message lastMessage = convo.getLastMessage();
                    convo.getUsersId().remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    String otherUserId = convo.getUsersId().get(0);

                    DatabaseReference referenceOtherUser = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
                    referenceOtherUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            View view = getLayoutInflater().inflate(R.layout.conversation_list_item,null,false);

                            final CircleImageView civProfilePhoto = view.findViewById(R.id.coversation_list_item_other_user_profile);
                            TextView mainText = view.findViewById(R.id.conversation_list_item_txtv_other_name);
                            TextView subText = view.findViewById(R.id.conversation_list_item_txtv_last_message);
                            TextView statusText = view.findViewById(R.id.conversation_list_item_txtv_status);

                            final File file;
                            try{
                                file = File.createTempFile("image","png");
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference(user.getProfileImagePath());
                                storageReference.getFile(file)
                                        .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                                civProfilePhoto.setImageURI(Uri.fromFile(file));
                                            }
                                        });

                                mainText.setText(user.getFirstName() + " " + user.getLastName());
                                String subtext = lastMessage.getMessageContent().length() > 14 ? lastMessage.getMessageContent().substring(0,14) + "..." : lastMessage.getMessageContent();
                                subText.setText(subtext);
                                statusText.setText(lastMessage.getMessageStatus());
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }

                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(ConversationListActivity.this,ChatActivity.class);
                                    intent.putExtra("CONVERSATION_ID",conversationId);
                                    startActivity(intent);
                                }
                            });

                            linearlayoutChatList.addView(view);
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
    }

    private List<String> sortMap(Map<String, Message> map){
        List<String> sortedConversationList = new ArrayList<>();

        Map<String,Long> mapWithDate = new HashMap<String,Long>();
        for(Map.Entry<String, Message> m : map.entrySet()){
            mapWithDate.put(m.getKey(),m.getValue().getDatetimeSent());
        }

        List<Map.Entry<String,Long>> list = new ArrayList<>(mapWithDate.entrySet());
        for(int j,i = 0; i<list.size(); i++){
            Log.d("Sorting","Sorting MAP");
            Map.Entry entry = list.get(i);
            j = i-1;
            while((j >= 0) && ((long)list.get(j).getValue() < (long)entry.getValue())){
                Log.d("Inside","Sorting");
                list.remove(j + 1);
                list.add(j+1,list.get(j));
                j = j -1;
            }
            list.remove(j+1);
            list.add(j+1,entry);
        }

        for(Map.Entry<String,Long> m : list){
            sortedConversationList.add(m.getKey());
        }
        Log.d("SORTING","DONE SORTING");
        return sortedConversationList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.convo_list_refresh:
                //Refresh
                break;

            case R.id.convo_list_search:
                //Search
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation_list_menu,menu);
        return true;
    }
}
