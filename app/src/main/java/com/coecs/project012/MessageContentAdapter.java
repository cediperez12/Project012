package com.coecs.project012;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageContentAdapter extends ArrayAdapter<Conversation.Message> {
    private Uri fromUserProfile;
    private Uri toUserProfile;

    private User fromUser;
    private User toUser;

    private List<Conversation.Message> conversationContent;

    private Context context;

    private Alert alert;

    public MessageContentAdapter(Context context,List<Conversation.Message> conversationContent) {
        super(context,R.layout.conversation_list_item,conversationContent);
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
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Conversation.Message m = getItem(position);
        final boolean statusViewer =  false;

        ViewHolder holder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_layout_item,parent,false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        if(toUser.getUid().equals(m.getSenderUid())){
            //To user is the sender.
            holder.fromLayout.setVisibility(View.GONE);
            if(m.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                //Message is a Location Viewer
                holder.toLayout.setVisibility(View.GONE);
                holder.txtvViewLocationLayout.setText("My Location");
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Alert(context).showErrorMessage("Notification","This is your own location.");
                    }
                });
            }else{
                //Message is not a Location Viewer
                holder.viewLocationLayout.setVisibility(View.GONE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(m.getDatetimeSent());

                //Setup Message
                holder.civTo.setImageURI(toUserProfile);
                holder.txtvToMessage.setText(m.getMessageContent());
                holder.txtvToStatus.setText(m.getMessageStatus() + " - " + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.YEAR));
            }
        }else{
            //From user is the sender
            holder.toLayout.setVisibility(View.GONE);
            if(m.getMessageType().equals(Conversation.MESSAGE_TYPE_VIEW_LOCATION)){
                holder.fromLayout.setVisibility(View.GONE);
                holder.txtvViewLocationLayout.setText("View Persons Location");
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Intent to Location Viewer
                    }
                });
            }else{
                holder.txtvViewLocationLayout.setVisibility(View.GONE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(m.getDatetimeSent());

                //Setup Message
                holder.civFrom.setImageURI(fromUserProfile);
                holder.txtvFromMessage.setText(m.getMessageContent());
                holder.txtvToStatus.setText(m.getMessageStatus() + " - " + calendar.get(Calendar.DATE));
            }
        }

        return convertView;
    }

    public static class ViewHolder{

        public CircleImageView civFrom,civTo;
        public TextView txtvFromMessage,txtvToMessage;
        public TextView txtvFromStatus,txtvToStatus;
        public TextView txtvViewLocationLayout;

        public RelativeLayout fromLayout,toLayout,viewLocationLayout;

        public ViewHolder(View itemView) {
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
