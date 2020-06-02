package bitshift.wordpad;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class FileAdapter extends ArrayAdapter<File>
{
	public FileAdapter(Context context, List<File> items)
	{
        super(context, R.layout.file_row, items); // assigned are context, the resource id, and a list<resolveinfo>
	}
     
    @Override
    public View getView(int position, View view, ViewGroup parent) 
    {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		view = inflater.inflate(R.layout.file_row, null);

		TextView label = (TextView) view.findViewById(R.id.label);
		TextView date = (TextView) view.findViewById(R.id.date);
		ImageView icon = (ImageView) view.findViewById(R.id.image);
		
		File file = super.getItem(position);
		if (file != null)
		{
			label.setText(file.getName());

			if (file.isDirectory())
				icon.setImageResource(R.drawable.ic_menu_folder);
			else
			{
				String theDate = new SimpleDateFormat().format(new Date(file.lastModified()));
				date.setText(theDate);
				icon.setImageResource(R.drawable.ic_menu_file);
			}
				
		}
		else
		{
			label.setText("..");
			icon.setImageResource(R.drawable.ic_menu_back);	
		}
      
      return(view);
    }
	
}
