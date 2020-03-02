package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private RecyclerView recyclerView;
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
                Map<String,Message> map = (Map)dataSnapshot.getValue();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String key = ds.getKey();
                    Message message = ds.getValue(Message.class);
                    map.put(key,message);
                }

                if(map != null){
                    txtvEmptyList.setVisibility(View.GONE);
                    List<String> listOfConversationIds = sortMap(map);

                    MessageListsAdapter adapter = new MessageListsAdapter(ConversationListActivity.this,listOfConversationIds);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ConversationListActivity.this));
                    recyclerView.setAdapter(adapter);
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

    private List<String> sortMap(Map<String, Message> map){
        List<String> sortedConversationList = new ArrayList<>();

        Map<String,Long> mapWithDate = new HashMap<String,Long>();
        for(Map.Entry<String, Message> m : map.entrySet()){
            mapWithDate.put(m.getKey(),m.getValue().getDatetimeSent());
        }

        List<Map.Entry<String,Long>> list = new ArrayList<>(mapWithDate.entrySet());
        for(int j,i = 0; i<list.size(); i++){
            Map.Entry entry = list.get(i);
            j = i-1;
            while((j >= 0) && ((long)list.get(j).getValue() > (long)entry.getValue())){
                list.remove(j + 1);
                list.add(j+1,list.get(j));
            }
            list.remove(j+1);
            list.add(j+1,entry);
        }

        for(Map.Entry<String,Long> m : list){
            sortedConversationList.add(m.getKey());
        }

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
