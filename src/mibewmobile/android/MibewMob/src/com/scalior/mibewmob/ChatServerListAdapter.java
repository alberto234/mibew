package com.scalior.mibewmob;

import java.util.Arrays;
import java.util.List;

import com.scalior.mibewmob.model.ChatServer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
//import android.widget.ImageView;
import android.widget.TextView;

public class ChatServerListAdapter extends ArrayAdapter<ChatServer> {

	private List<ChatServer> items;
	private Context context;
	//private ImageTagFactory mImageTagFactory;
	//private Loader mImageLoader; 
	
	public ChatServerListAdapter(Context context, int textViewResourceId, List<ChatServer> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
		
		//mImageTagFactory = ImageTagFactory.newInstance(context, R.drawable.bg_img_loading);
		//mImageTagFactory.setErrorImageId(R.drawable.bg_img_notfound);
		
		//mImageLoader = ((KlippitApplication)((FragmentActivity)context).getApplication())
			//			.getImageManager().getLoader();
	}
	
	public ChatServerListAdapter(Context context, int textViewResourceId, ChatServer[] items) {
		super(context, textViewResourceId, items);
		this.items = Arrays.asList(items);
		this.context = context;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ChatServersViewHolder viewHolder; 
	
        View myView = convertView;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.serverlistitem, null);
            
            viewHolder = new ChatServersViewHolder();
            
            if (viewHolder != null) {
            	// Being a not so serious Java expert, this piece of code has thrown me off.
            	// So for next time:
            	// viewHolder holds a reference to the TextViews associated to the passed in view
            	// Further down, the references are used to populate the TextViews
            	viewHolder.tvName = (TextView)myView.findViewById(R.id.server_name);
            	viewHolder.tvURL = (TextView)myView.findViewById(R.id.server_url);
            	// viewHolder.tvShortDescription = (TextView)myView.findViewById(R.id.ChatServerdescription);
            	//viewHolder.ivLogo = (ImageView)myView.findViewById(R.id.server_logo);
            	myView.setTag(viewHolder);
            }
            
        }
        else
        {
        	viewHolder = (ChatServersViewHolder)myView.getTag();
        }
    
        ChatServer serverItem = items.get(position);
        
        // Here is where the TextView references above are being populated.
        // Without this pattern, one would have had to do a findViewById each time
        // this view is queried. Now, the findViewById is called only the first time
        // this view needs to be shown.
        if (serverItem != null) {
            viewHolder.tvName.setText(serverItem.getName());
            viewHolder.tvURL.setText(serverItem.getURL());
            // viewHolder.tvShortDescription.setText(serverItem.getDescription());
            //ImageTag tag = mImageTagFactory.build(serverItem.getLogoURL(), context);
            //viewHolder.ivLogo.setTag(tag);
            //mImageLoader.load(viewHolder.ivChatServerPicture);*/
        }
        
        // myView has been indirectly populated through the viewHolder references.
        return myView;
    }
	
     private class ChatServersViewHolder {
        TextView tvName;
        TextView tvURL;
        //TextView tvShortDescription;
        // ImageView ivLogo;
    }
	
}