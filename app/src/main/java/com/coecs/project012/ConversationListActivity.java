package com.coecs.project012;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConversationListActivity extends AppCompatActivity {
    //Activity Components
    private Toolbar toolbar;
    private RecyclerView recyclerView;

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

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        conversations = FirebaseDatabase.getInstance().getReference("conversations");
        userConversationLists = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("conversationIds");
    }
}
