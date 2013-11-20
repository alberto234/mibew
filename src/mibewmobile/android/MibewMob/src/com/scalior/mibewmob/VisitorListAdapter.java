package com.scalior.mibewmob;

import java.util.Arrays;
import java.util.List;

import com.scalior.mibewmob.model.ChatThread;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
//import android.widget.ImageView;
import android.widget.TextView;

public class VisitorListAdapter extends ArrayAdapter<ChatThread> {

	private List<ChatThread> items;
	private Context context;
	//private ImageTagFactory mImageTagFactory;
	//private Loader mImageLoader; 
	
	public VisitorListAdapter(Context context, int textViewResourceId, List<ChatThread> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
		
		//mImageTagFactory = ImageTagFactory.newInstance(context, R.drawable.bg_img_loading);
		//mImageTagFactory.setErrorImageId(R.drawable.bg_img_notfound);
		
		//mImageLoader = ((KlippitApplication)((FragmentActivity)context).getApplication())
			//			.getImageManager().getLoader();
	}
	
	public VisitorListAdapter(Context context, int textViewResourceId, ChatThread[] items) {
		super(context, textViewResourceId, items);
		this.items = Arrays.asList(items);
		this.context = context;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ChatThreadsViewHolder viewHolder; 
	
        View myView = convertView;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.visitorlistitem, null);
            
            viewHolder = new ChatThreadsViewHolder();
            
            if (viewHolder != null) {
            	viewHolder.tvName = (TextView)myView.findViewById(R.id.guest_name);
            	viewHolder.tvMessage = (TextView)myView.findViewById(R.id.initial_message);
            	// viewHolder.tvShortDescription = (TextView)myView.findViewById(R.id.ChatThreaddescription);
            	//viewHolder.ivLogo = (ImageView)myView.findViewById(R.id.server_logo);
            	myView.setTag(viewHolder);
            }
            
        }
        else
        {
        	viewHolder = (ChatThreadsViewHolder)myView.getTag();
        }
    
        ChatThread visitorItem = items.get(position);
        
        if (visitorItem != null) {
            viewHolder.tvName.setText(visitorItem.getGuestName());
            String state;
            if (visitorItem.getState() == ChatThread.STATE_CLOSED) {
            	state = "(Closed: " + visitorItem.getThreadID() + ") ";
            } else {
              	state = "(Not closed: " + visitorItem.getThreadID() + ") ";
            }
        
            viewHolder.tvMessage.setText(state + visitorItem.getInitialMessage());
            // viewHolder.tvShortDescription.setText(serverItem.getDescription());
            //ImageTag tag = mImageTagFactory.build(serverItem.getLogoURL(), context);
            //viewHolder.ivLogo.setTag(tag);
            //mImageLoader.load(viewHolder.ivChatThreadPicture);*/
        }
        
        return myView;
    }
	
     private class ChatThreadsViewHolder {
        TextView tvName;
        TextView tvMessage;
        //TextView tvShortDescription;
        // ImageView ivLogo;
    }
}