package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
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

public class ChatActivity extends AppCompatActivity implements LocationListener {
    //Activity Components
    private Toolbar toolbar;
    private LinearLayout linearLayoutChatLists;
    private ScrollView scChatList;
    private EditText etxtMessageContent;
    private ImageButton btnSendMessage;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.message_menu_send_loc:
                //Send Location
                sendLocation();
                break;
            case R.id.message_menu_add_rev:
                //Create new Review
                createReview();
                break;

            case R.id.message_menu_profile:
                Intent intent = new Intent(this,ProfielActivity.class);
                intent.putExtra("USER_ID",fromUser.getUid());
                startActivity(intent);
                finish();
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    private void sendLocation(){
        try {//Find Restrictions
            //Create Dialog for confirmation
            AlertDialog confirmationDialog = new AlertDialog.Builder(ChatActivity.this)
                    .setTitle("Notification")
                    .setMessage("This act will give save your location and will be seen by other user for them to contact you. Are you sure you want to save your location?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                if(ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                    ActivityCompat.requestPermissions(ChatActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
                                }else{
                                    LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

                                    boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                                    boolean gpsConnection = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                                    if(network){
                                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,ChatActivity.this);

                                        if(locationManager != null){
                                            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                            if(location != null){
                                                //Create Message
                                                User.Location localLoc = new User.Location();
                                                localLoc.setLat(location.getLatitude());
                                                localLoc.setLng(location.getLongitude());
                                                Conversation.Message message = new Conversation.Message(currentUser.getUid(),Calendar.getInstance().getTimeInMillis(),localLoc);
                                                FirebaseDatabase.getInstance().getReference("conversation").child(conversationId).child("convo").push().setValue(message);
                                            }else{
                                                throw new Exception("Failed to fetch your location. Please try again later.");
                                            }
                                        }
                                    }else if(gpsConnection){
                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,ChatActivity.this);

                                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                        if(location != null){
                                            //Create Message
                                            User.Location localLoc = new User.Location();
                                            localLoc.setLat(location.getLatitude());
                                            localLoc.setLng(location.getLongitude());
                                            Conversation.Message message = new Conversation.Message(currentUser.getUid(),Calendar.getInstance().getTimeInMillis(),localLoc);
                                            FirebaseDatabase.getInstance().getReference("conversation").child(conversationId).child("convo").push().setValue(message);
                                        }else{
                                            throw new Exception("Failed to fetch your location. Please try again later.");
                                        }
                                    }else{
                                        throw new Exception("Please turn on your GPS/Location and Internet Connection");
                                    }
                                }
                            }catch (Exception ex){
                                alert.showErrorMessage("Notification",ex.getMessage());
                            }
                        }
                    })
                    .create();

            //Show dialog
            confirmationDialog.show();
        }catch (Exception ex){
            alert.showErrorMessage("Notification",ex.getMessage());
        }
    }

    private void createReview(){
        if(fromUser.getReviews() != null){
            //Check if you already reviewed the person
            User.Review rev = fromUser.getReviews().get(currentUser.getUid());

            if(rev != null){
                //You already have a review for this person.
                reviewDialog(rev);
            }else{
                //You did not reviewed the person yet.
                reviewDialog();
            }
        }else{
            reviewDialog();
        }
    }

    private void reviewDialog(User.Review review){
        //Create Dialog
        View view = getLayoutInflater().inflate(R.layout.create_review_layout,null,false);
        final EditText etxtReviewContent = view.findViewById(R.id.etxt_review_content);
        final RatingBar ratingBar = view.findViewById(R.id.review_rating_bar);

        etxtReviewContent.setText(review.getReviewContent());
        ratingBar.setRating(review.getStars());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Change the review for this person.")
                .setView(view)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        User.Review newReview = new User.Review(currentUser.getUid(),etxtReviewContent.getText().toString().trim(),(int)ratingBar.getRating(),Calendar.getInstance().getTimeInMillis());

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(fromUser.getUid()).child("reviews");
                        reference.child(currentUser.getUid()).setValue(newReview).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    FirebaseDatabase.getInstance().getReference(fromUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            fromUser = dataSnapshot.getValue(User.class);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                    }
                })
                .create();

        //Show Dialog
        dialog.show();
    }

    private void reviewDialog(){
        View view = getLayoutInflater().inflate(R.layout.create_review_layout,null,false);
        final EditText etxtReviewContent = view.findViewById(R.id.etxt_review_content);
        final RatingBar ratingBar = view.findViewById(R.id.review_rating_bar);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Change the review for this person.")
                .setView(view)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        User.Review newReview = new User.Review(currentUser.getUid(),etxtReviewContent.getText().toString().trim(),(int)ratingBar.getRating(),Calendar.getInstance().getTimeInMillis());

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(fromUser.getUid()).child("reviews");
                        reference.child(currentUser.getUid()).setValue(newReview).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    FirebaseDatabase.getInstance().getReference(fromUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            fromUser = dataSnapshot.getValue(User.class);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                    }
                })
                .create();

        //Show Dialog
        dialog.show();
    }

    private void AddNewMessage(Conversation.Message message, User[] userLists, Uri[] userProfile){
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

//            Log.d(m.getSenderUid(),m.getMessageContent());

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
                final Conversation.Message locationMessage = m;
                //From user is the sender
                toLayout.setVisibility(View.GONE);
                if(m.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                    fromLayout.setVisibility(View.GONE);
                    txtvViewLocationLayout.setText("View Persons Location");
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Intent to Location Viewer
                            Intent intent = new Intent(getApplicationContext(),NavigateLocation.class);
                            intent.putExtra("LAT",locationMessage.getLocation().getLat());
                            intent.putExtra("LON",locationMessage.getLocation().getLng());
                            intent.putExtra("USERNAME",fromUser.getFirstName());
                            startActivity(intent);
                            finish();
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
    protected void onDestroy() {
        super.onDestroy();
        linearLayoutChatLists.removeAllViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        linearLayoutChatLists.removeAllViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
