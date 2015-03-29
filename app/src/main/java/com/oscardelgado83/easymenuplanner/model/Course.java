package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * Created by oscar on 15/03/15.
 */
@Table(name = "Courses")
public class Course extends Model {

    @Column (index = true)
    public String name;

    public List<Ingredient> getIngredients() {
        return getMany(Ingredient.class, "course");
    }

    @Override
    public String toString() {
        return name;
    }
}
