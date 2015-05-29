package com.kelevra.navi;

/**
 * Created by sharlukovich on 28.05.2015.
 */
public class MenuItems {
    private int position;
    private int infoType;
    private int iconFile;
    private String name;

    public MenuItems(int position, int infoType, int iconFile, String name) {
        this.position = position;
        this.infoType = infoType;
        this.iconFile = iconFile;
        this.name = name;

    }

    public int getInfoType() {
        return infoType;
    }

    public int getIconFile() {
        return iconFile;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
}