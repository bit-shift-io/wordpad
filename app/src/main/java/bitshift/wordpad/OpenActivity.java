package bitshift.wordpad;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class OpenActivity extends ListActivity implements AdapterView.OnItemLongClickListener
{
	private String mCurrentDir;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open);
	    getDirListing(SettingsMgr.instance().documentFolder());
	}
	
	private void getDirListing(String path) 
	{
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

		//if (!path.equals(Environment.getExternalStorageDirectory().toString())) // uncoment this if we get bugs
			fileList.add(null); // null implies to put the up level

		if (files != null) {
			Arrays.sort(files, filecomparator);
			for (File file : files) {
				if (!file.isHidden() && file.canRead())
					fileList.add(file);
			}
		}
		
		FileAdapter adapter = new FileAdapter(this, fileList);
		setListAdapter(adapter);

        // add long click (other method instead of via findbyid
        ListView lv = super.getListView();
        lv.setOnItemLongClickListener(this);
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
            // Prepare data intent
            Intent data = new Intent();
            data.putExtra("file", file.getAbsolutePath());
            setResult(RESULT_OK, data);
			finish();
		}
	}


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        final View adapView = adapterView;
        final FileAdapter adapter = (FileAdapter) adapterView.getAdapter();
        final File file = adapter.getItem(position);

        // infalte the builder
        LayoutInflater inflater = LayoutInflater.from(adapterView.getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(adapterView.getContext()); // this has to be adapterview context dunno why?
        View dialogView = inflater.inflate(R.layout.dialog_file_edit, null);
        builder.setView(dialogView);

        // now populate the builder(final allows us to use it in the onclick methods
        final EditText fileLabel = (EditText) dialogView.findViewById(R.id.et_file_name);
        fileLabel.setText(file.getName());

        final CheckBox cbDelete = (CheckBox) dialogView.findViewById(R.id.cb_delete);

        builder.setPositiveButton(R.string._apply, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                if (cbDelete.isChecked()) // delete
                {
                    file.delete();
                }
                else // rename this file
                {
                    String newName = file.getAbsolutePath().replace(file.getName(),fileLabel.getText().toString());
                    Log.i("wordpad",newName);
                    file.renameTo(new File(newName));
                }
                getDirListing(mCurrentDir);
            }
        });
        builder.setNegativeButton(R.string._cancel, null);
        //builder.setOnDismissListener(this); // add listener
        builder.show();

        return true;
    }

}
