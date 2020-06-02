package bitshift.wordpad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by Bronson on 14/11/13.
 */
public class HistoryAdapter extends ArrayAdapter<String>
{

    public HistoryAdapter(Context context)
    {
        super(context, R.layout.navigation_item); // assigned are context, the resource id, and a list<resolveinfo>
    }

    @Override
    public int getCount()
    {
        return HistoryMgr.instance().recentDocuments().size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.navigation_item, null);

        TextView label = (TextView) view.findViewById(R.id.tv_menu_label);
        String name = getItem(position);
        String filename = name.substring((name.lastIndexOf("/") + 1), name.length()); //we want only filename from the path
        label.setText(filename);

        ImageView icon = (ImageView) view.findViewById(R.id.iv_menu_icon);
        icon.setImageResource(R.drawable.ic_menu_file);

        return view;
    }

    @Override
    public String getItem(int position)
    {
        return HistoryMgr.instance().recentDocuments().get(position);
    }


}
