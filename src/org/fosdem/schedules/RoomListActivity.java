/**
 * 
 */
package org.fosdem.schedules;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class RoomListActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fillData();
	}
	
	
	public void fillData() {
		String[] room_a = { "foo", "bar" };

		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, room_a));

	}
	
}
