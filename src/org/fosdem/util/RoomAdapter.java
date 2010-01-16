/**
 * 
 */
package org.fosdem.util;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.pojo.Room;

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
public class RoomAdapter extends ArrayAdapter<Room> {

	private ArrayList<Room> items;

	public RoomAdapter(Context context, int textViewResourceId, ArrayList<Room> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.room_list, null);
		}
		Room room = items.get(position);
		if (room != null) {
			TextView text1 = (TextView) v.findViewById(R.id.text1);
			if (text1 != null) {
				text1.setText(room.getName());
			}
		}
		return v;
	}
	
}
