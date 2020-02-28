package com.coecs.project012;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return conversationIds.size();
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder{

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
