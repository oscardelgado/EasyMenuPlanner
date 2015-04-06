package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscar on 15/03/15.
 */
@Table(name = "Courses")
public class Course extends Model {

    public enum CourseType {
        FIRST, SECOND, NONE;
    };

    @Column (index = true)
    public String name;

    @Column
    public CourseType courseType;

    public List<CourseIngredient> getIngredients() {
        return new Select().from(CourseIngredient.class)
                .where("course = ?", this.getId())
                .execute();
    }

    public void removeAllIngredients() {
        new Delete().from(CourseIngredient.class)
                .where("course = ?", this.getId())
                .execute();
    }

    @Override
    public String toString() {
        return name;
    }
}
