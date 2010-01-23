package org.fosdem.util;

import android.content.Context;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class UIUtil {
	public static void showToast(Context ctx,String message){
		Toast toast=Toast.makeText(ctx, message,Toast.LENGTH_SHORT);
		toast.show();
    }
	
	public static final LayoutParams WRAPPED = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	public static final LayoutParams FILL_BOTH = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	public static final LayoutParams FILL_HORIZONTAL = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
	public static final LayoutParams FILL_VERTICAL = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT);
}
