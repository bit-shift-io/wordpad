package bitshift.wordpad;

import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

/*
Hack the makes it possible to see if keyboard is open or closed
It measures the root view, windows zie and the status bar and will calulate if the keyboard is open
 */
public class KeyboardMgr implements ViewTreeObserver.OnGlobalLayoutListener
{
    static private KeyboardMgr mSingleInstance;
    private final View  mActivityRootView;
    private int         mLastKeyboardHeightInPx;
    private boolean     mIsKeyboardOpen = false;
    private boolean     mKeyboardWasOpen = false;

    public KeyboardMgr(View activityRootView)
    {
        mSingleInstance = this;
        mActivityRootView = activityRootView;
        mActivityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    static KeyboardMgr instance()
    {
        return mSingleInstance;
    }

    private ArrayList<KeyboardMgrListener> mListeners = new ArrayList<KeyboardMgrListener> ();

    // custom listener interface (interface is a group of related methods with empty bodies)
    public interface KeyboardMgrListener
    {
        void onKeyboardOpened(int keyboardHeightInPx);
        void onKeyboardClosed();
    }

    public void addKeyboardChangeListener(KeyboardMgrListener listener)
    {
        mListeners.add(listener);
    }

    public void removeKeyboardChangeListener(KeyboardMgrListener listener)
    {
        mListeners.remove(listener);
    }

    private void notifyKeyboardOpened(int keyboardHeightInPx)
    {
        this.mLastKeyboardHeightInPx = keyboardHeightInPx;

        for (KeyboardMgrListener listener : mListeners)
            listener.onKeyboardOpened(keyboardHeightInPx);
    }

    private void notifyKeyboardClosed()
    {
        for (KeyboardMgrListener listener : mListeners)
            listener.onKeyboardClosed();
    }




    @Override
    public void onGlobalLayout()
    {
        final Rect r = new Rect();
        //r will be populated with the coordinates of your view that area still visible.
        mActivityRootView.getWindowVisibleDisplayFrame(r);

        final int heightDiff = mActivityRootView.getRootView().getHeight() - (r.bottom - r.top);
        if (!mIsKeyboardOpen && heightDiff > 100)
        { // if more than 100 pixels, its probably a keyboard...
            mIsKeyboardOpen = true;
            notifyKeyboardOpened(heightDiff);
        }
        else if (mIsKeyboardOpen && heightDiff < 100)
        {
            mIsKeyboardOpen = false;
            notifyKeyboardClosed();
        }
    }

    public void setIsKeyboardOpened(boolean isKeyboardOpen)
    {
        mIsKeyboardOpen = isKeyboardOpen;
    }

    public boolean isKeyboardOpen()
    {
        return mIsKeyboardOpen;
    }

    /**
     * Default value is zero (0)
     * @return last saved keyboard height in px
     */
    public int getLastKeyboardHeightInPx()
    {
        return mLastKeyboardHeightInPx;
    }

    void hideKeyboard()
    {
        mKeyboardWasOpen = false;
        if (isKeyboardOpen())
        {
            mKeyboardWasOpen = true;
            InputMethodManager imm = (InputMethodManager)mActivityRootView.getContext().getSystemService(InputMethodService.INPUT_METHOD_SERVICE); // this works great!
            imm.hideSoftInputFromWindow(mActivityRootView.getWindowToken(), 0);
            DocumentViewMgr.instance().clearFocus();
        }
    }

    void showKeyboard()
    {
        InputMethodManager imm =  (InputMethodManager)mActivityRootView.getContext().getSystemService(InputMethodService.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT); // works great!
        DocumentViewMgr.instance().requestFocus();
    }

    void restoreKeyboard()
    {
        if (mKeyboardWasOpen)
        {
            mKeyboardWasOpen = false;
            showKeyboard();
        }
    }

}
