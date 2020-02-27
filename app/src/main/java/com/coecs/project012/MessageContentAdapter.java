package com.coecs.project012;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageContentAdapter extends RecyclerView.Adapter<MessageContentAdapter.ViewHolder> {
    private Uri fromUserProfile;
    private Uri toUserProfile;

    private User fromUser;
    private User toUser;

    private List<Conversation.Message> conversationContent;

    private Context context;

    private Alert alert;

    public MessageContentAdapter() {
    }

    public MessageContentAdapter(Context context,List<Conversation.Message> conversationContent) {
        alert = new Alert(context);
        this.context = context;
        this.conversationContent = conversationContent;
    }

    public Uri getFromUserProfile() {
        return fromUserProfile;
    }

    public void setFromUserProfile(Uri fromUserProfile) {
        this.fromUserProfile = fromUserProfile;
    }

    public Uri getToUserProfile() {
        return toUserProfile;
    }

    public void setToUserProfile(Uri toUserProfile) {
        this.toUserProfile = toUserProfile;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View item = inflater.inflate(R.layout.chat_layout_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(item);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Conversation.Message message = conversationContent.get(position);

        if(message.getSenderUid().equals(toUser.getUid())){
            //To User
            holder.fromLayout.setVisibility(View.GONE);

            if(message.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                //Message Type is Requires View Location
                //Remove From and To Message Layout
                holder.toLayout.setVisibility(View.GONE);

                holder.txtvViewLocationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Show an alert that this is your location.
                        alert.showErrorMessage("Notification","This is your own location.");
                    }
                });
            }else{
                holder.viewLocationLayout.setVisibility(View.GONE);

                holder.civTo.setImageURI(toUserProfile);
                holder.txtvToMessage.setText(message.getMessageContent());
                holder.txtvToStatus.setText(message.getMessageStatus());

                holder.txtvToMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(holder.txtvToStatus.getText().toString().equals(message.getMessageStatus())){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(message.getDatetimeSent());
                            holder.txtvToStatus.setText(calendar.toString());
                        }else{
                            holder.txtvToStatus.setText(message.getMessageStatus());
                        }
                    }
                });
            }
        }else{
            //From User
            holder.toLayout.setVisibility(View.GONE);
            if(message.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                //Message Type is Requires View Location
                //Remove From and To Message Layout
                holder.fromLayout.setVisibility(View.GONE);

                holder.txtvViewLocationLayout.setText("View this persons Location.");
                holder.txtvViewLocationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Show an alert that this is your location.
                        Intent intent = new Intent();
                        intent.putExtra("LOCATION_VIEW_LAT",message.getLocation().getLat());
                        intent.putExtra("LOCATION_VIEW_LON",message.getLocation().getLng());
                        context.startActivity(intent);
                    }
                });
            }else{
                holder.viewLocationLayout.setVisibility(View.GONE);

                holder.civFrom.setImageURI(fromUserProfile);
                holder.txtvFromMessage.setText(message.getMessageContent());
                holder.txtvFromStatus.setText(message.getMessageStatus());

                holder.txtvFromMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(holder.txtvFromStatus.getText().toString().equals(message.getMessageStatus())){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(message.getDatetimeSent());
                            holder.txtvFromStatus.setText(calendar.toString());
                        }else{
                            holder.txtvFromStatus.setText(message.getMessageStatus());
                        }
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return conversationContent.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView civFrom,civTo;
        public TextView txtvFromMessage,txtvToMessage;
        public TextView txtvFromStatus,txtvToStatus;
        public TextView txtvViewLocationLayout;

        public RelativeLayout fromLayout,toLayout,viewLocationLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
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
        }
    }

}
