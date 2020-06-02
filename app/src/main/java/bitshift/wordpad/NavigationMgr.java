package bitshift.wordpad;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bronson on 14/11/13.
 */
public class NavigationMgr
{
    static private NavigationMgr mSingleInstance;

    public NavigationMgr()
    {
        mSingleInstance = this;
        createMenuList();
    }

    public static NavigationMgr instance()
    {
        return mSingleInstance;
    }

    List<NavigationItem> mMenuList = new ArrayList<NavigationItem>();

    public List<NavigationItem> menuList()
    {
        return mMenuList;
    }

    public void createMenuList()
    {
        // add id's to values/ids.xml
        addMenuItem(R.id.menu_new, R.string._new, R.drawable.ic_menu_new);
        addMenuItem(R.id.menu_open, R.string._open, R.drawable.ic_menu_folder);
        addMenuItem(R.id.menu_save, R.string._save, R.drawable.ic_menu_save);
        addMenuItem(R.id.menu_saveas, R.string._save_as, R.drawable.ic_menu_saveas);
        addMenuItem(); // spacer
        addMenuItem(R.id.menu_share, R.string._share, R.drawable.ic_menu_share);
        addMenuItem(R.id.menu_fullscreen, R.string._fullscreen, R.drawable.ic_menu_fullscreen);
        addMenuItem(R.id.menu_settings, R.string._settings, R.drawable.ic_menu_settings);
        addMenuItem(); // spacer
        addMenuItem(R.id.menu_exit, R.string._exit, R.drawable.ic_menu_exit);
    }

    public void addMenuItem(int id, int label, int icon)
    {
        NavigationItem item = new NavigationItem(id, label, icon);
        mMenuList.add(item);
    }

    public void addMenuItem(int id, int label, int icon, NavigationItem.ItemType type)
    {
        NavigationItem item = new NavigationItem(id, label, icon, type);
        mMenuList.add(item);
    }

    public void addMenuItem()
    {
        NavigationItem item = new NavigationItem();
        mMenuList.add(item);
    }
}
