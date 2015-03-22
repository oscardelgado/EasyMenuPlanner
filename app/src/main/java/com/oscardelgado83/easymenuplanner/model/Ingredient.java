package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by oscar on 15/03/15.
 */
@Table(name = "Ingredients")
public class Ingredient extends Model {

    @Column (index = true)
    public String name;

    @Column
    public Course course;
}
