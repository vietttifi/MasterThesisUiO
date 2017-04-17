package com.sensordroid;

/**
 * Created by sveinpg on 28.01.16.
 */
public class Wrapper {
    private String name;
    private String id;
    private boolean selected = false;

    public Wrapper(String name, String id, boolean selected) {
        this.name = name;
        this.id = id;
        this.selected = selected;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId(){
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelected(){
        return this.selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    @Override
    public boolean equals(Object a){
        if (a instanceof Wrapper){
            Wrapper obj = (Wrapper) a;
            return obj.getId().compareTo(this.getId()) == 0;
        }

        return false;
    }
}
