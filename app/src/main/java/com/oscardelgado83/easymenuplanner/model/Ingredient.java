package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

/**
 * Created by oscar on 15/03/15.
 */
@Table(name = "Ingredients")
public class Ingredient extends Model implements Serializable {

    @Column (index = true)
    public String name;

    @Override
    public String toString() {
        return name;
    }
}
