package bitshift.wordpad;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class SaveActivity extends ListActivity  
{
	private Document mDocument;
	private EditText mNameEditText;
	private String mCurrentDir;
    private Bundle mBundle;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save);

        // store the bundle intent, it may be null
        Intent intent = getIntent();
        mBundle = intent.getBundleExtra("bundle");

		mNameEditText = (EditText) findViewById(R.id.editText_document_name);
		mDocument = Document.instance();
	    //mRoot = ""; //Environment.getExternalStorageDirectory().toString();
	
		// populate the document name field
		String fName;
		if (mDocument.file() == null) // we have no file name yet, so then we navigate to the documents folder
			fName = mDocument.fileName();
		else
			fName = mDocument.file().getName();
		
		mNameEditText.setText(fName);
		
		selectFileName();
		
		// button listener
		OnClickListener mClickListener = new OnClickListener() 
		{ 
			@Override
			public void onClick(View view) 
			{
            	final String fName = mNameEditText.getText().toString();
            	final File newFile = new File(mCurrentDir + "/" + fName);
            	File docFile = mDocument.file(); // could be null
            	
            	if ((newFile.exists() && !mDocument.saved()) || (!newFile.equals(docFile) && newFile.exists()))
            	{
            		new AlertDialog.Builder(view.getContext())
            		.setIcon(R.drawable.ic_launcher)
            		.setTitle(R.string._file_exists)
            		.setMessage(R.string._overwrite_existing_file)
            		.setNegativeButton(R.string._no, null)
            		.setPositiveButton(R.string._yes, new DialogInterface.OnClickListener() 
				    {
			            @Override
			            public void onClick(DialogInterface dialog, int id) 
			            {
                            fileSelected(newFile);
			            }
			        })
            		.show();	         		
            	}
            	else
            	{
                    fileSelected(newFile);
            	}
			} 
		}; 		
	    findViewById(R.id.button_save).setOnClickListener(mClickListener);
	
	    
	    String openPath;
		if (mDocument.file() != null) // if document has file, go to this directory instead
			openPath = mDocument.file().getParent(); // get path without filename
		else
			openPath = SettingsMgr.instance().documentFolder(); // mRoot +
		
	    getDirListing(openPath);	    
	}
	
	private void selectFileName() 
	{
		String fName = mNameEditText.getText().toString();
		mNameEditText.setSelection(0, fName.lastIndexOf("."));
		mNameEditText.requestFocus();
	}

	private void getDirListing(String path) 
	{
		// TODO: With android 6.0 and above, we must ask and check for permission each time....
		List<File> fileList = new ArrayList<File>();
		File newFile = new File(path);
		
		// check if folder exists, otherwise goto root!
		if (!newFile.exists())
		{
			getDirListing(Environment.getExternalStorageDirectory().toString());
			return;
		}
		mCurrentDir = path;
		File[] files = newFile.listFiles(); // list of files

		
		//if (!path.equals(Environment.getExternalStorageDirectory().toString())) // limit to sd card only? removed
		fileList.add(null); // null implies to level

		if (files != null)
		{
			Arrays.sort(files, filecomparator);

			for (File file : files) {
				if (!file.isHidden() && file.canRead())
					fileList.add(file);
			}
		}
		
		FileAdapter adapter = new FileAdapter(this, fileList);
		setListAdapter(adapter); 
    }	

    final Comparator<?super File> filecomparator = new Comparator<File>()
    {
		public int compare(File file1, File file2) 
		{
			if(file1.isDirectory())
			{
				if (file2.isDirectory())
					return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
				else
					return -1;
			}
			else 
			{
				if (file2.isDirectory())
					return 1;
				else
					return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
			}	
		} 	
	}; 
	
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) 
	{
		FileAdapter adapter = (FileAdapter) listView.getAdapter();
		File file = adapter.getItem(position);
		
		if (file == null) // top level
		{
			String up = mCurrentDir.substring(0, mCurrentDir.lastIndexOf("/"));
			getDirListing(up);
		}
		else if (file.isDirectory()) // directory
		{
			if(file.canRead())
				getDirListing(file.getPath());
		}
		else  // file
		{
			String fName = file.getName();
			mNameEditText.setText(fName);
			selectFileName();
		}
	}

    void fileSelected(File file)
    {
        // Prepare data intent
        Intent data = new Intent();
        data.putExtra("file", file.getAbsolutePath());
        data.putExtra("bundle", mBundle);
        setResult(RESULT_OK, data);
        finish();
    }


}
