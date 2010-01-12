/**
 * 
 */
package org.fosdem.schedules;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class RoomListActivity extends ListActivity  {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fillData();
	}
	
	
	public void fillData() {
		String[] room_a = { "foo", "bar" };
		// fetch rooms of the selected day
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, room_a));
		// TODO chri - adapt layout to get right arrow to show there are next actions
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String roomName = (String) getListView().getItemAtPosition(position);
        
        // TODO load list of Events in Room
    }
    
    
	
}
