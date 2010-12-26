package org.fosdem.views;

import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.util.UIUtil;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class FavoriteButton extends ImageView implements OnClickListener {

	protected Event event;

	protected boolean isFavorite = false;
	protected boolean initialized = false;

	public FavoriteButton(Context context){
		super(context);
	}
	
	public void setEvent(Event event){
		this.event = event;
		initialize();
	}
	
	public FavoriteButton(Context context, Event event) {
		super(context);
		this.event = event;
		
		initialize();
		setEvent(event);
		this.setOnClickListener(this);
	}
	
	public FavoriteButton(Context context,AttributeSet attributeSet){
		super(context,attributeSet);
		this.setImageResource(R.drawable.btn_star_big_off);
		this.setOnClickListener(this);
	}

	protected void initialize() {
		//this.setLayoutParams(UIUtil.WRAPPED);
		Log.v(getClass().getName(),"Initialize");

		DBAdapter adapter = new DBAdapter(getContext());
		adapter.open();
		isFavorite = adapter.isFavorite(event);
		Log.v(getClass().getName(),isFavorite?"Is a favorite":"Isn't a favorite");
		adapter.close();

		setImageResource();

	}

	protected void setImageResource() {
		if (isFavorite) {
			this.setImageResource(R.drawable.btn_star_big_on);
		} else
			this.setImageResource(R.drawable.btn_star_big_off);
	}

	public void onClick(View v) {

		DBAdapter db = new DBAdapter(getContext());
		db.open();
		if (isFavorite) {
			// Unmark
			db.deleteBookmark(event.getId());
			UIUtil.showToast(this.getContext(), this.getContext().getString(
					net.spamt.CCCC2010.R.string.favorites_event_removed));
		} else {
			// Mark
			db.addBookmark(event);
			UIUtil.showToast(this.getContext(), this.getContext().getString(
					net.spamt.CCCC2010.R.string.favorites_event_added));
		}
		db.close();
		isFavorite = !isFavorite;

		setImageResource();

	}

}
