/**
 * 
 */
package org.fosdem.util;

import java.util.ArrayList;

import net.spamt.CCCC2010.R;
import org.fosdem.pojo.Track;

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
public class TrackAdapter extends ArrayAdapter<Track> {
	
	private ArrayList<Track> items;

	public TrackAdapter(Context context, int textViewResourceId, ArrayList<Track> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.track_list, null);
		}
		Track track = items.get(position);
		if (track != null) {
			TextView text1 = (TextView) v.findViewById(R.id.text1);
			TextView textSmall = (TextView) v.findViewById(R.id.text_small);
			
			if (text1 != null && textSmall != null) {
				text1.setText(track.getName());
				textSmall.setText(track.getType());
			}
		}
		return v;
	}
	
}
