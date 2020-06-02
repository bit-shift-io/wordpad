/*
To do:
- search feature
- open - document properties
- multithread the save history? and other parts(document updates) ui needs to be seperate from code... asynctask?
- gdrive support - https://github.com/gsuitedevs/android-samples/tree/master/drive/quickstart - to help hide oxylabs integration


** ChangeLog **
Build 23
- Fixed settings not saving
- Added check for and request permissions if required before opening and saving

Build 22
- Fixed menu text colour
- Added option to hide drawer menu buttons

Build 21
- Fixed state saving after canceling out of the save dialog
- Added long click in the open dialog to allow rename and delete of files

Build 20
- Note: UNINSTALL then REINSTALL due to the amount of changes in this build!
- Optimised for kit kat - android 4.4
- Removed volume scrolling
- Added navigation drawer interface
- Cleaned opening and closing of keyboard when using menus
- Fullscreen mode has returned!
- Optimised and cleaned code, so there may be some new bugs

Build 19
- Document path can now be set to alternate storage device

Build 18
- line numbers update when opening a new document
- line numbers calculated correctly for word wrapped documents
- fixed text jittering when scrolling
- disabled spell check in the save activity
- added auto selecting filename to save activity
- overhauled the action bar code
 
Build 17
- Launching from intents should be more robust
- Intents now work with different scheme types

Build 16
- Allowed wordpad to open any text file, not just plain text
- Fixed intents to accept files or text
- Sharing into wordpad should now work again properly

Build 15
- Polish language files (thanks to Rafal M)
- Fixed auto-save continually saving
- Cursor position saved on screen rotation and restored states
- Fixed auto-save not to save when opening documents
- Using exit will hide the keyboard for neater exit

Build 14
- Fixed cursor position moving when saving
- Made the action bar more resilient

Build 13
- Fixed bug when selecting items from history list
- Fixed bug for char set selection not working
- Fixed action bar to autohide
- General cleanup
- Fixed bug where settings were causing document to become unsavable
- Wordpad now remember states, use exit clears state data

Build 12
- Using volume rocker to scroll doesn't lose cursor position
- Volume rocker scrolling scrolls completely to the top now
- Back button will now ask to save before exiting
- Made the text area easier to select
- Change the way the action bar works, to be more user friendly
- Updated history code to be more reliable and faster
- Added autosave back in, it will save after 2 seconds of idle time if enabled
- Added some debug info when opening and saving files
- Added support for files with large sizes (tested 10mb)
- Opening extremely huge, massive, large files will cause out of memory error, and notify the user

Build 11
- Allow user to navigate to other folders
- Rearranged preferences screen
- Added font options
- Added line number color option
- Added charset support (utf-8 default encoding)

Build 10
- Fix possible crash when loading previously opened document that no longer exists
- Saved state between screen rotations and open/closing the app
- Added more padding for line numbers
- Line numbers are now 6 lines ahead of the documents line count

Build 9
- Fixed bug with cursors position changing on screen rotation
- Rewrote the save and open screens
- Removed auto-save feature for now due to possible overwriting document bug
- General code cleanup, so a few new bugs are expected
- Removed fullscreen mode unfortunately due to android bug
- Recent/history list now working
- added save and overwrite dialogs
- various other fixes

Build 8
- Restructured settings
- Fixed bug which prevented scrolling
- Added background color when clicking buttons for more user feedback
- Background now defaults to black
- Fixed some of the sharing bugs (more to fix)

Build 7
- Added checks to settings to prevent crashes
- disabled sharing due to bugs
- added line numbers

Build 6
- added alert for user when saved
- changed autosave to default disabled
- added padding to move text away from the edge of the screen a little
- added read only mode
- added screen awake option

Build 5
- fixed crash error with font size

Build 4
- placeholder widget
- settings added fullscreen 
- settings added spell check 
- settings added word wrap
- settings added auto save
- settings added read only
- settings added document folder
- settings added theme settings (font color, size, background color)

Build 3
- Fixed share feature causing crash
- Fixed document area to fit the application hence fixing scrolling and selecting issues

Build 2
- Fixed crash caused by missing directory

*/
package bitshift.wordpad;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements SettingsMgr.SettingsMgrListener, KeyboardMgr.KeyboardMgrListener, DrawerLayout.DrawerListener, View.OnClickListener, HistoryMgr.HistoryMgrListener, DocumentViewMgr.DocumentViewMgrListener
{
    static private MainActivity mSingleInstance;

    // managers
    private static SettingsMgr mSettingsMgr;
    private static HistoryMgr mHistoryMgr;
    private static NavigationMgr mNavigationMgr;
    private static KeyboardMgr mKeyboardMgr;
    private static DocumentViewMgr mDocumentViewMgr;
    private static ToastMgr mToastMgr;

    private static Document mDocument;
    private static EditText mDocumentView;
    private static TextView mNumberView;
    private static ScrollView mScrollView;
    private static View mBackgroundView;

    private boolean mSaveStateOverride = false; // use this to override save state for other activitys (settings, save, open etc)

    // for intents
    final static int REQUEST_CODE_OPEN = 0;
    final static int REQUEST_CODE_SAVEAS = 1;
    final static int REQUEST_CODE_SETTINGS = 2;

	final Handler mSaveHandler = new Handler();
	final Runnable mDelaySave = new Runnable()
	{ 
	   public void run()  
	   { 
		   saveDocument(null);
	   } 
	};

    // drawer views
    private static DrawerLayout mDrawerLayout;
    private static ListView mDrawerList;
    private static ListView mDrawerAltList;

    // drawer buttons
    ImageButton mDrawerButtonLeft;
    ImageButton mDrawerButtonRight;

    static MainActivity instance()
    {
        return mSingleInstance;
    }

	// INITIAL CREATION
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Log.i("wordpad", "create");

        // views
        mDocumentView = (EditText) findViewById(R.id.et_document);
        mNumberView = (TextView) findViewById(R.id.tv_numbers);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mBackgroundView = findViewById(R.id.background);

        // navigation views
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerAltList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.right_drawer);

        // setup managers
        mToastMgr = new ToastMgr(getApplicationContext());
        mKeyboardMgr = new KeyboardMgr(findViewById(R.id.background));
        mSettingsMgr = new SettingsMgr(getApplicationContext());
        mHistoryMgr = new HistoryMgr();
        mNavigationMgr = new NavigationMgr();
        mDocument = new Document(this);
        mDocumentViewMgr = new DocumentViewMgr(mDocumentView, mNumberView);

        createNavigation();
        createHistoryNavigation();
        createListeners();

        updateSettings();

        // how are we launching? Priority - 1. Intent, 2. Previous State, 3. Last document, 4. New
        boolean intent = intent();
        if (!intent) // else
        {
            boolean restore = restoreTempState();

            if (!restore) // if resume state = true then onResume will run after onCreate()
            {
                if (mSettingsMgr.openLastDocument()) // check if there is a document before opening, intenet may create one already
                {
                    File f = mHistoryMgr.lastDocument();
                    if (f!= null && f.exists())
                        mDocument.open(f);
                    else
                        newDocument();
                }
                else
                    newDocument();
            }
        }

        // clear the resume state
        mSettingsMgr.setResumeState(false);
	}

	boolean intent()
	{
		// intent filters
		Intent intent = getIntent();
	    String action = intent.getAction(); // cant switch a string
	    String scheme = intent.getScheme();

	    if (action.equals(Intent.ACTION_VIEW))  // get a file
	    {
		    Uri data = intent.getData();

	    	if (ContentResolver.SCHEME_CONTENT.equals(scheme))  // handle as content uri
	    	{
	    		try 
	    		{
	                ContentResolver cr = getContentResolver();
	                InputStream istream = cr.openInputStream(data);
	                if(istream != null) 
	                {
		                mDocument.open(istream);
		                return true; 	                	
	                }
				} 
	    		catch (FileNotFoundException e){e.printStackTrace();}
	    		return false;
	    	} 
	    	else  // handle as file uri
	    	{
	    		String path = data.getPath();
				File file = new File(path);
				mDocument.open(file);
				return true;	    	    
	    	}
	    }

	    if (action.equals(Intent.ACTION_SEND)) // get text
	    {
	    	Bundle extras = intent.getExtras();
	    	if (extras != null)
	    	{
                mDocument.open(extras);
		    	return true;
	    	}
	    }
	    return false;
	}

    @Override
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config); // TODO: move this to keyboar Mgr, on layout change??
        mKeyboardMgr.hideKeyboard(); // need to recaululate new keyboard height, so hack here :)
        mKeyboardMgr.restoreKeyboard();// need to recaululate new keyboard height, so hack here :)
        //updateFullScreenLayout(); - this will get called when we reopen the keyboard :)
    }

    int getScreenHeight()
    {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    void saveChangesDialog(final Bundle bundle)
    {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(R.string._save)
                .setMessage(R.string._save_changes)
                .setNegativeButton(R.string._no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // set last save length so it doesnt call the save dialog again
                        mDocument.setLastSaveLength(mDocument.body().length());
                        postSaveChangesDialog(bundle);
                    }
                })
                .setPositiveButton(R.string._yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        saveDocument(bundle);
                    }
                })
                .show();
    }

    void postSaveChangesDialog(Bundle bundle)
    {
        if (bundle == null || !bundle.containsKey("action"))
            return;

        String action = bundle.getString("action");

        if (action.equals("new"))
            newDocument();

        if (action.equals("open"))
            openDocument();

        if (action.equals("exit"))
            exitApp();

        if (action.equals("history"))
            selectHistoryItem(bundle.getString("selection"));
    }

	void exitApp()
	{
        mKeyboardMgr.hideKeyboard();

		if (mDocument.changedSinceSave())
        {
            Bundle b = new Bundle();
            b.putString("action", "exit");
            saveChangesDialog(b);
        }
		else
		{
            mSettingsMgr.setResumeState(false);
            //finish(); -- dont use this, it pauses the app!
			System.exit(0);
		}
	}

    void openSettings()
    {
        mKeyboardMgr.hideKeyboard();
        mSaveStateOverride = true;
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }

	void openDocument()
	{
        if (!ensureExternalStoragePermissionsGranted())
            return;

		 if (mDocument.changedSinceSave())
         {
             Bundle b = new Bundle();
             b.putString("action", "open");
             saveChangesDialog(b);
         }
		 else		 
		 {
            mKeyboardMgr.hideKeyboard();
            mSaveStateOverride = true;
            Intent intent = new Intent(this, OpenActivity.class);
            startActivityForResult(intent, REQUEST_CODE_OPEN);
		 }
	}
	
	void newDocument()
	{
		 if (mDocument.changedSinceSave())
         {
             Bundle b = new Bundle();
             b.putString("action", "new");
             saveChangesDialog(b);
         }
		 else	
		 {
             mDocument.clear();
             mKeyboardMgr.showKeyboard();
		 }
	}

    // bundle for post save action, else null
	void saveAsDocument(Bundle bundle)
	{
        if (!ensureExternalStoragePermissionsGranted())
            return;

        mKeyboardMgr.hideKeyboard();
        mSaveStateOverride = true;
		Intent intent = new Intent(this, SaveActivity.class);
        intent.putExtra("bundle", bundle);
        startActivityForResult(intent, REQUEST_CODE_SAVEAS);
	}

    // bundle for post save action, else null
	void saveDocument(Bundle bundle)
	{
        if (!ensureExternalStoragePermissionsGranted())
            return;

		if (mDocument.file() == null) // have we been saved before? if not, use the save as dialog
			saveAsDocument(bundle);
		else
        {
            mDocument.save();
            postSaveChangesDialog(bundle);
        }
	}

    void shareDocument()
    {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, mDocument.fileName()); // heading as doc name
        sharingIntent.putExtra(Intent.EXTRA_TEXT, mDocument.body()); // body as the document
        startActivity(Intent.createChooser(sharingIntent, "Share Document"));
    }


    // Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.ib_drawer_left:
            {
                openDrawer(mDrawerAltList);
                break;
            }
            case R.id.ib_drawer_right:
            {
                openDrawer(mDrawerList);
                break;
            }
        }
    }

    // Listeners
	void createListeners()
	{
        mDocument.addDocumentChangeListener(mDocumentViewMgr);
        mDocument.addDocumentChangeListener(mHistoryMgr);
        mDocumentViewMgr.addDocumentChangeListener(this);
        mKeyboardMgr.addKeyboardChangeListener(this);
        mSettingsMgr.addSettingsChangeListener(this);
        mSettingsMgr.addSettingsChangeListener(mDocumentViewMgr);
        mDrawerLayout.setDrawerListener(this);
        mHistoryMgr.addHistoryChangeListener(this);

        mDrawerButtonLeft = (ImageButton) findViewById(R.id.ib_drawer_left);
        mDrawerButtonLeft.setOnClickListener(this);

        mDrawerButtonRight = (ImageButton) findViewById(R.id.ib_drawer_right);
        mDrawerButtonRight.setOnClickListener(this);
	}
	
	void destroyListeners()
	{
        mDocument.removeDocumentChangeListener(mDocumentViewMgr);
        mDocument.removeDocumentChangeListener(mHistoryMgr);
        mDocumentViewMgr.removeDocumentChangeListener(this);
        mKeyboardMgr.removeKeyboardChangeListener(this);
        mSettingsMgr.removeSettingsChangeListener(this);
        mSettingsMgr.removeSettingsChangeListener(mDocumentViewMgr);
        mDrawerLayout.setDrawerListener(null);
        mHistoryMgr.removeHistoryChangeListener(this);
        mDrawerButtonLeft.setOnClickListener(null);
        mDrawerButtonRight.setOnClickListener(null);
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
        switch(keyCode) // change from false to not allow android to continue doing what it does
        {
            case KeyEvent.KEYCODE_MENU:
            {
                if (isDrawOpen(mDrawerList))
                {
                    closeDrawer(mDrawerList);
                    return true;
                }
                else
                {
                    openDrawer(mDrawerList);
                    return true;
                }
            }
            case KeyEvent.KEYCODE_BACK:
            {
                if (isDrawOpen(mDrawerList))
                {
                    closeDrawer(mDrawerList);
                    return true;
                }
                else
                {
                    exitApp();
                    return true;
                }
            }
        }
	    return super.onKeyDown(keyCode, event);
	}

    boolean isFullScreen()
    {
        return (getWindow().getAttributes().flags &WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
    }

    void updateFullScreenLayout()
    {
        // resize the user view to fit fulscreen
        if (isFullScreen() && mKeyboardMgr.isKeyboardOpen())
        {
            Log.i("wordpad","update fullscreen layout");
            int height = getScreenHeight();

            mScrollView.setTop(0);
            mScrollView.setBottom(height - mKeyboardMgr.getLastKeyboardHeightInPx());

            mBackgroundView.setTop(0);
            mBackgroundView.setBottom(height - mKeyboardMgr.getLastKeyboardHeightInPx());
        }
    }

    void toggleFullScreen()
    {
        Window window = getWindow();
        if (isFullScreen())
        {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else
        {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE); // navigation fade out
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); // hide navigation
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); // | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    void selectNavigationItem(NavigationItem item)
    {
        switch (item.Id())
        {
            case R.id.menu_new:
                newDocument();
                break;

            case R.id.menu_open:
                openDocument();
                break;

            case R.id.menu_save:
                saveDocument(null);
                break;

            case R.id.menu_saveas:
                saveAsDocument(null);
                break;

            case R.id.menu_share:
            {
                shareDocument();
                break;
            }

            case R.id.menu_fullscreen:
                toggleFullScreen();
                break;

            case R.id.menu_settings:
            {
                openSettings();
                break;
            }

            case R.id.menu_exit:
            {
                exitApp();
                break;
            }
        }
    }

    private void createNavigation()
    {
        // setup the navigation drawer
        NavigationItemAdapter adapter = new NavigationItemAdapter(getApplicationContext());
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                NavigationItem item = (NavigationItem) adapterView.getItemAtPosition(position);
                mDrawerLayout.closeDrawer(mDrawerList);
                selectNavigationItem(item);
            }
        });
    }

    private void createHistoryNavigation()
    {
        // setup the history drawer
        HistoryAdapter history = new HistoryAdapter(getApplicationContext());
        mDrawerAltList.setAdapter(history);
        mDrawerAltList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                String item = (String) adapterView.getItemAtPosition(position);
                mDrawerLayout.closeDrawer(mDrawerAltList);
                selectHistoryItem(item);
            }
        });
    }

    void selectHistoryItem(String item)
    {
        mKeyboardMgr.hideKeyboard();
        if (mDocument.changedSinceSave())
        {
            Bundle b = new Bundle();
            b.putString("action","history");
            b.putString("selection", item);
            saveChangesDialog(b);
        }
        else
        {
            File newFile = new File(item);
            mDocument.open(newFile);
        }
    }

    void updateSettings()
    {
        // keep screen on
        if (mSettingsMgr.screenAwake())
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // background color
        Drawable drawable = new ColorDrawable(mSettingsMgr.backgroundColor());
        mBackgroundView.setBackgroundDrawable(drawable);

        // recent documents
        if (mSettingsMgr.clearHistory())
            mHistoryMgr.clear();

        // button visibility
        String buttonVisibility = mSettingsMgr.drawerButtonVisibility();
        mDrawerButtonLeft.setVisibility(buttonVisibility.contains("left") ? View.VISIBLE : View.GONE);
        mDrawerButtonRight.setVisibility(buttonVisibility.contains("right") ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSettingsChanged(SharedPreferences sharedPreferences, String key)
    {
        updateSettings();
    }

    @Override
    public void onKeyboardOpened(int keyboardHeightInPx)
    {
        updateFullScreenLayout();
    }

    @Override
    public void onKeyboardClosed()
    {
    }

    void openDrawer(View view)
    {
        mDrawerLayout.openDrawer(view);
    }

    void closeDrawer(View view)
    {
        mDrawerLayout.closeDrawer(view);

    }

    boolean isDrawOpen(View view)
    {
        return mDrawerLayout.isDrawerOpen(view);
    }

    @Override
    public void onDrawerSlide(View view, float v)
    {
    }

    @Override
    public void onDrawerOpened(View view)
    {
        mKeyboardMgr.hideKeyboard();
    }

    @Override
    public void onDrawerClosed(View view)
    {
        mKeyboardMgr.restoreKeyboard();
    }

    @Override
    public void onDrawerStateChanged(int i)
    {
    }

    // saved instance state stuff
    // stores settings and a temp copy of this document
    boolean saveTempState()
    {
        if (mDocument.body().length() == 0 || mSaveStateOverride)
        {
            Log.i("wordpad", "disable resume state");
            mSettingsMgr.setResumeState(false);
            return false;
        }

        Log.i("wordpad", "saving resume state");
        mSettingsMgr.setResumeState(true);

        mSettingsMgr.putBoolean("document_saved", mDocument.saved());
        mSettingsMgr.putInt("document_save_length", mDocument.lastSaveLength());
        mSettingsMgr.putInt("document_selection_start", mDocumentView.getSelectionStart());
        mSettingsMgr.putInt("document_selection_end", mDocumentView.getSelectionStart());
        mSettingsMgr.putBoolean("keyboard_open",mKeyboardMgr.isKeyboardOpen());

        mDocument.writeTempFile();

        File f = mDocument.file();
        if (f != null)
            mSettingsMgr.putString("doc_file", mDocument.file().getPath());
        else
            mSettingsMgr.putString("doc_file", null);

        return true;
    }

    boolean restoreTempState()
    {
        if (mSettingsMgr.resumeState() && !mSaveStateOverride)
        {
            mDocument.setSaved(mSettingsMgr.getBoolean("document_saved", false));
            mDocument.setLastSaveLength(mSettingsMgr.getInt("document_save_length", 0));

            mDocument.readTempFile();
            //mDocumentViewMgr.setText(mDocument.body());

            String f = mSettingsMgr.getString("document_file", null);
            if (f != null)
                mDocument.setFile(f);

            int start = mSettingsMgr.getInt("document_selection_start", 0);
            int end = mSettingsMgr.getInt("document_selection_end", 0);
            if (start > mDocumentView.length())
                start = mDocumentView.length();
            if (end > mDocumentView.length())
                end = mDocumentView.length();

            mDocumentView.setSelection(start, end);

            if (mSettingsMgr.getBoolean("keyboard_open", false))
                mKeyboardMgr.showKeyboard();

            return true;
        }
        else
            return false;
    }

    @Override
    public void onStop()
    {
        Log.i("wordpad","stop");
        super.onStop();
        saveTempState();
        mKeyboardMgr.hideKeyboard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK)
        {
            mSaveStateOverride = false; // set this back
            return;
        }

        switch (requestCode)
        {
            case REQUEST_CODE_OPEN:
            {
                if (data.hasExtra("file"))
                {
                    File newFile = new File(data.getStringExtra("file"));
                    mDocument.open(newFile);
                }
                break;
            }
            case REQUEST_CODE_SAVEAS:
            {
                if (data.hasExtra("file"))
                {
                    mDocument.setFile(data.getStringExtra("file"));
                    mDocument.save();
                }
                break;
            }
            case REQUEST_CODE_SETTINGS:
            {
                break;
            }
        }

        // post bundle action
        if (data.hasExtra("bundle"))
            postSaveChangesDialog(data.getBundleExtra("bundle"));

        // set this back
        mSaveStateOverride = false;

    }

    @Override
    public void onHistoryChanged()
    {
        createHistoryNavigation();
    }

    @Override
    public void onEditTextChanged()
    {
        if (mSettingsMgr.autoSave() && mDocument.body().length() > 0)
        {
            mSaveHandler.removeCallbacks(mDelaySave);
            mSaveHandler.postDelayed(mDelaySave, 2000);
        }
    }

    // return true if we have permissions
    // return false if we need to query the user for a permission
    public boolean ensureExternalStoragePermissionsGranted() {
        final List<String> permissionsList = new ArrayList<String>();

        int check = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (check != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        check = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (check != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1024);
            return false;
        }

        return true;
    }
}


