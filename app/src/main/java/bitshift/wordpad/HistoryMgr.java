package bitshift.wordpad;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bronson on 14/11/13.
 */
public class HistoryMgr implements Document.DocumentListener
{
    static private HistoryMgr mSingleInstance;
    static final private int MAX_HISTORY_SIZE = 10;
    private List<String> mRecentDocuments = new ArrayList<String>(); //instead of string array, use a list array, then convert to string array when needed

    HistoryMgr()
    {
        mSingleInstance = this;
        load();
        //Document.instance().addDocumentChangeListener(this);
    }

    static HistoryMgr instance()
    {
        return mSingleInstance;
    }

    private ArrayList<HistoryMgrListener> mListeners = new ArrayList<HistoryMgrListener> ();

    // custom listener interface (interface is a group of related methods with empty bodies)
    // empty shell, this gets overriden by mainactivity
    public interface HistoryMgrListener
    {
        void onHistoryChanged();
    }

    public void addHistoryChangeListener(HistoryMgrListener listener)
    {
        mListeners.add(listener);
    }

    public void removeHistoryChangeListener(HistoryMgrListener listener)
    {
        mListeners.remove(listener);
    }

    void notifyHistoryChanged()
    {
        for (HistoryMgrListener listener : mListeners)
            listener.onHistoryChanged();
    }

    public List<String> recentDocuments()
    {
        return mRecentDocuments;
    }

    public void update()
    {
        // get filename
        File f = Document.instance().file();

        if (f == null)
            return;

        String docFile = f.toString();

        // remove from history list current
        int size = recentDocuments().size();
        if (size != 0)
        {
            for (int i = recentDocuments().size(); i > 0; --i) // loop in reverse to fix issues
            {
                String recent = recentDocuments().get(i - 1); // remove 1 as size is +1 (silly 0 based arrays)
                if (recent.equalsIgnoreCase("") || recent.equalsIgnoreCase(docFile))
                    recentDocuments().remove(i - 1);
            }
        }

        // add to list
        recentDocuments().add(0, docFile);

        // remove anything over max history size
        for (int i = recentDocuments().size(); i > MAX_HISTORY_SIZE; --i)
            recentDocuments().remove(i - MAX_HISTORY_SIZE);

        save();

        notifyHistoryChanged();
    }

    // gets the last used document
    public File lastDocument()
    {
        if (recentDocuments().size() != 0)
        {
            String filename = recentDocuments().get(0);
            if (!filename.equals(""))
                return new File(filename);
        }

        return null;
    }

    public void clear()
    {
        SettingsMgr.instance().putBoolean("key_clear_history", false);

        String key = "key_recent_documents";
        SettingsMgr.instance().putInt(key + "_size", 0);

        for(int i = 0; i < recentDocuments().size(); ++i)
            SettingsMgr.instance().putString(key + "_" + i, "");

        mRecentDocuments = new ArrayList<String>();

        notifyHistoryChanged();
    }

    // saves history to settings
    public void save()
    {
		String key = "key_recent_documents";
        SettingsMgr.instance().putInt(key + "_size", recentDocuments().size());

		for(int i = 0; i < recentDocuments().size(); ++i)
            SettingsMgr.instance().putString(key + "_" + i, recentDocuments().get(i));
    }

    // loads history from settings
    public void load()
    {
		String key = "key_recent_documents";
		int size = SettingsMgr.instance().getInt(key + "_size", 0);

		for(int i = 0; i < size; ++i)
			recentDocuments().add(SettingsMgr.instance().getString(key + "_" + i, null));
    }

    @Override
    public void onDocumentBodyChanged()
    {
    }

    @Override
    public void onDocumentSaved()
    {
        update();
    }

    @Override
    public void onDocumentOpened()
    {
        update();
    }

}
