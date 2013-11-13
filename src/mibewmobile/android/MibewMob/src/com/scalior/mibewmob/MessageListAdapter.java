package com.scalior.mibewmob;

import java.util.Arrays;
import java.util.List;

import com.scalior.mibewmob.model.ChatMessage;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
//import android.widget.ImageView;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<ChatMessage> {

	private List<ChatMessage> items;
	private Context context;
	//private ImageTagFactory mImageTagFactory;
	//private Loader mImageLoader; 
	
	public MessageListAdapter(Context context, int textViewResourceId, List<ChatMessage> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
		
		//mImageTagFactory = ImageTagFactory.newInstance(context, R.drawable.bg_img_loading);
		//mImageTagFactory.setErrorImageId(R.drawable.bg_img_notfound);
		
		//mImageLoader = ((KlippitApplication)((FragmentActivity)context).getApplication())
			//			.getImageManager().getLoader();
	}
	
	public MessageListAdapter(Context context, int textViewResourceId, ChatMessage[] items) {
		super(context, textViewResourceId, items);
		this.items = Arrays.asList(items);
		this.context = context;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ChatMessagesViewHolder viewHolder; 
	
        View myView = convertView;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.chatbubbleitem, null);
            
            viewHolder = new ChatMessagesViewHolder();
            
            if (viewHolder != null) {
            	viewHolder.tvMessage = (TextView)myView.findViewById(R.id.message);
            	myView.setTag(viewHolder);
            }
            
        }
        else
        {
        	viewHolder = (ChatMessagesViewHolder)myView.getTag();
        }
    
        ChatMessage messageItem = items.get(position);
        
        if (messageItem != null) {
            viewHolder.tvMessage.setText("("+ messageItem.getOperatorGuid() + ": " +
            							 messageItem.getThreadID() + ") " + messageItem.getMessage());
            if (messageItem.getOperatorGuid() != 0) {
            	// This is a message from an operator
            	// TODO: We have to use a separate color for the current
            	// 		 operator versus another operator
            	viewHolder.tvMessage.setBackgroundResource(R.drawable.bubble_green);
            	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
            	
            	params.gravity = Gravity.RIGHT;
            	viewHolder.tvMessage.setLayoutParams(params);
            } else {
            	viewHolder.tvMessage.setBackgroundResource(R.drawable.bubble_yellow);
            	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
            	
            	params.gravity = Gravity.LEFT;
            	viewHolder.tvMessage.setLayoutParams(params);
            }
        }
        
        return myView;
    }
	
     private class ChatMessagesViewHolder {
        TextView tvMessage;
    }
}