package bitshift.wordpad;

import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastMgr extends Application
{
    static private ToastMgr mSingleInstance;
    private Context mContext;

    public ToastMgr(Context context)
    {
        mSingleInstance = this;
        mContext = context;
    }
    
    public static ToastMgr instance()
    {
        return mSingleInstance;
    }

	void displayToast(int resourceID)
	{
		Toast toast = Toast.makeText(mContext, resourceID, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();	
	}
}
