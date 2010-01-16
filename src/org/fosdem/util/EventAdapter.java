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
			v = vi.inflate(R.layout.simple_list_tab_indicator, null);
		}
		Event event = items.get(position);
		if (event != null) {
			TextView text1 = (TextView) v.findViewById(R.id.text1);
			if (text1 != null) {
				text1.setText(event.getTitle());
			}
		}
		return v;
	}
	
}
