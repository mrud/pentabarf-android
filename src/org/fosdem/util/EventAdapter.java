/**
 * 
 */
package org.fosdem.util;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.pojo.Event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class EventAdapter extends ArrayAdapter<Event> {

	public static final String LOG_TAG= EventAdapter.class.getName();
	private ArrayList<Event> items;

	public EventAdapter(Context context, int textViewResourceId, ArrayList<Event> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.event_list, null);
		}
		Event event = items.get(position);
		if (event != null) {
			TextView title = (TextView) v.findViewById(R.id.event_title);
			TextView speaker = (TextView) v.findViewById(R.id.event_speakers);
			TextView room = (TextView) v.findViewById(R.id.event_room);
			TextView time = (TextView) v.findViewById(R.id.event_time);
			// TODO set other values like time, speaker,...

			title.setText(event.getTitle());
			speaker.setText(event.getPersonsNames());
			room.setText(event.getRoom());
			time.setText(StringUtil.datesToString(event.getStart(), event.getDuration()));
			
		}
		return v;
	}
	
}
