package bitshift.wordpad;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

// extend to make accessable from all activities
// http://stackoverflow.com/questions/8573796/keeping-a-variable-value-across-all-android-activities
public class Document
{
    static private Document mSingleInstance;

    public Document(MainActivity activity)
    {
        mSingleInstance = this;
        mMainActivity = activity;
        TEMP_FILE = new File(mMainActivity.getFilesDir() + "/" + "temp.txt");
    }
    
    public static Document instance()
    {
        return mSingleInstance;
    }
    
    // member variables
	private File mFile = null;
	private Boolean mSaved = false;
	private Integer mLastSaveLength = 0;
    private String mBody = ""; // document content

	private MainActivity mMainActivity;
    private static File TEMP_FILE;


	private ArrayList<DocumentListener> mListeners = new ArrayList<DocumentListener> ();

    // custom listener interface (interface is a group of related methods with empty bodies)
    // empty shell, this gets overriden by mainactivity
    public interface DocumentListener
    {
        void onDocumentBodyChanged();
        void onDocumentSaved();
        void onDocumentOpened();
    }

    public void addDocumentChangeListener(DocumentListener listener)
    {
        mListeners.add(listener);
    }

    public void removeDocumentChangeListener(DocumentListener listener)
    {
        mListeners.remove(listener);
    }

    void notifyDocumentSaved()
    {
        for (DocumentListener listener : mListeners)
            listener.onDocumentSaved();
    }

    void notifyDocumentOpened()
    {
        for (DocumentListener listener : mListeners)
            listener.onDocumentOpened();
    }

    void notifyDocumentBodyChanged()
    {
        for (DocumentListener listener : mListeners)
            listener.onDocumentBodyChanged();
    }

	
	public void setLastSaveLength(Integer length)
	{
		mLastSaveLength = length;
	}

	public Integer lastSaveLength()
	{
		return mLastSaveLength;
	}	
	
	public Boolean changedSinceSave()
	{
        int last = lastSaveLength();
        int current = body().length();
        return last != current;
	}
	
	public void setSaved(Boolean saved)
	{
		mSaved = saved;
	}
	
	public void setFile(File file)
	{
		mFile = file;
	}
	
	public void setFile(String file)
	{
        setFile(new File(file));
	}	
	
	public void setBody(String body)
	{
		mBody = body;
	}

    // will also notify of body changed
    public void setBodyNotify(String body)
    {
        setBody(body);
        notifyDocumentBodyChanged();
    }

	// get data
	public Boolean saved()
	{
		return mSaved;
	}

	public File file()
	{
		return mFile;
	}
	
	public String charset()
	{
        return SettingsMgr.instance().charSet();
	}		

	public String fileName()
	{
		File f = file();
		if (f != null)
			return f.getName();
		else
		{
			Date cDate = new Date();
			String fDate = new SimpleDateFormat("yyyy.MM.dd").format(cDate);
			return fDate + ".txt";
		}
	}	

	public String body()
	{
		return mBody;
	}

    // clears the document
    public void clear()
    {
        mFile = null;
        setBodyNotify("");
        setSaved(false);
        setLastSaveLength(0);
    }

	public void open(InputStream istream)
	{
		if (istream == null)
		{
			ToastMgr.instance().displayToast(R.string._file_does_not_exist);
			return;			
		}

        mFile = null; //silent
        mSaved = true; // set saved silent
        mBody = ""; // clear old data before loading to save memory, but not need a notify here
		
		Log.i("wordpad", "opening file from input stream");
		
		//Read text from file
		StringBuilder text = new StringBuilder();
		try 
		{
			InputStreamReader is = new InputStreamReader(istream);
		    BufferedReader br = new BufferedReader(is);
		    String line;
		    while ((line = br.readLine()) != null) 
		    {
		        text.append(line);
		        text.append('\n');
		    }
		    br.close();
		}
		catch (IOException e) {e.printStackTrace();}
		
		
		setLastSaveLength(0);
		setBodyNotify(text.toString());

        notifyDocumentOpened();
	}

	// opens and re configures the entire document
	public void open(File file)
	{
		if (!file.exists()) // check file exists!
		{
            ToastMgr.instance().displayToast(R.string._file_does_not_exist);
			return;
		}
		
		setFile(file);
		mSaved = true; // set saved silent
		mBody = ""; // clear old data before loading to save memory, but not need a notify here

		Log.i("wordpad", ("opening file: " + fileName()));

		//Read text from file
		StringBuilder text = new StringBuilder();

		try 
		{
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charset());
		    BufferedReader br = new BufferedReader(is);
		    String line;
		    while ((line = br.readLine()) != null) 
		    {
		        text.append(line);
		        text.append('\n');
		    }
		    br.close();
		}
		catch (IOException e) {e.printStackTrace();}

		setLastSaveLength(text.length());
		setBodyNotify(text.toString());

		notifyDocumentOpened();
	}

    //opens from intent
    public void open(Bundle extras)
    {
        if (extras == null)
            return;

        Log.i("wordpad", "opening file from intent bundle");

        mFile = null; //silent
        mSaved = false; // set saved silent
        mBody = ""; // clear old data before loading to save memory, but not need a notify here
        setLastSaveLength(0);

        String subject = extras.getString(Intent.EXTRA_SUBJECT);
        String text = extras.getString(Intent.EXTRA_TEXT);

        String str = "";
        if (subject != null && subject.length() > 0 )
            str = subject + "\n";
        if (text != null && text.length() > 0)
            str = str + text + "\n";

        setBodyNotify(str); // put our text in the document
    }

	// save
	public void save()
	{
		if (mFile == null)
		{
			ToastMgr.instance().displayToast(R.string._document_not_saved);
			return;
		}
		
		Boolean written = writeFile(mFile);
		if (written)
		{
		    setSaved(true);
		    setLastSaveLength(body().length());
            ToastMgr.instance().displayToast(R.string._document_saved);

		    notifyDocumentSaved();
		}
		else
		{
            ToastMgr.instance().displayToast(R.string._document_not_saved);
			return;			
		}
	}
	
	Boolean writeFile(File file)
	{
	    try 
	    {
	    	if (!file.exists())
	    	{
		    	// make directorys if they dont exist
		    	String fName = file.getPath();
		    	String folderPath = fName.substring(0, fName.lastIndexOf("/")); //we want only folder path
		    	File tmpFile = new File(folderPath);
		    	tmpFile.mkdirs(); 
	    	}

	    	Log.i("wordpad", ("writing: " + file.toString()));
	    	
	    	OutputStream myOutput = new BufferedOutputStream(new FileOutputStream(file, false)); //true = append
	    	myOutput.write(body().getBytes(charset()));
	    	myOutput.flush();
	    	myOutput.close();
	    	return true;
	    } 
	    catch (FileNotFoundException e) 
	    {e.printStackTrace();} 
	    catch (IOException e) 
	    {e.printStackTrace();}	
	    
	    return false;
	}

    public void writeTempFile()
    {
        writeFile(TEMP_FILE);
    }

    public void readTempFile()
    {
        if (!TEMP_FILE.exists())
            return;

        Log.i("wordpad", ("reading: " + TEMP_FILE.toString()));

        //Read text from file
        StringBuilder text = new StringBuilder();
        try
        {
            InputStreamReader is = new InputStreamReader(new FileInputStream(TEMP_FILE), charset());
            BufferedReader br = new BufferedReader(is);
            String line;
            while ((line = br.readLine()) != null)
            {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {e.printStackTrace();}

        setBodyNotify(text.toString());
    }
	



}
