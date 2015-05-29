package com.kelevra.navi;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by kord on 21.04.2015.
 */

public class MainMenu {
    private static Map<Integer, List<MenuItems>> groupedMenuItems;

    public static void loadModel(Context context) {

        groupedMenuItems = new HashMap<Integer, List<MenuItems>>();

        List<MenuItems> menuGroup1 = new ArrayList<MenuItems>();
        menuGroup1.add(new MenuItems(0, 6, R.drawable.abc_ic_search_api_mtrl_alpha, context.getString(R.string.label_operative_information_short)));
        groupedMenuItems.put(R.string.label_group_1, menuGroup1);
        List<MenuItems> menuGroup2 = new ArrayList<MenuItems>();
        menuGroup2.add(new MenuItems(1, 1, R.drawable.ic_launcher, context.getString(R.string.label_city_routes)));
        menuGroup2.add(new MenuItems(2, 2, R.drawable.ic_launcher, context.getString(R.string.label_suburban_routes)));
        menuGroup2.add(new MenuItems(3, 3, R.drawable.ic_launcher, context.getString(R.string.label_intercity_routes)));
        menuGroup2.add(new MenuItems(4, 4, R.drawable.ic_launcher, context.getString(R.string.label_international_routes)));
        menuGroup2.add(new MenuItems(5, 5, R.drawable.ic_launcher, context.getString(R.string.label_holiday_routes)));
        groupedMenuItems.put(R.string.label_group_2, menuGroup2);
    }

    public static MenuItems getByPosition(int position) {
        Collection<List<MenuItems>> groups = groupedMenuItems.values();
        for (List<MenuItems> group : groups) {
            for (MenuItems item : group) {
                if (item.getPosition() == position) {
                    return item;
                }
            }
        }
        return null;
    }

    public static int getSize() {
        int size = 0;
        Collection<List<MenuItems>> groups = groupedMenuItems.values();
        for (List<MenuItems> group : groups) {
            size += group.size();
        }
        return size;
    }

    public static int getGroupIdByPosition(int position) {
        Set<Map.Entry<Integer, List<MenuItems>>> kvSet = groupedMenuItems.entrySet();
        for (Map.Entry<Integer, List<MenuItems>> entry : kvSet) {
            for (MenuItems item : entry.getValue()) {
                if (item.getPosition() == position) {
                    return entry.getKey();
                }
            }
        }
        return 0;
    }
}
