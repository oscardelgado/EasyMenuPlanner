package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by oscar on 4/04/15.
 */
@Table(name = "CourseIngredients")
public class CourseIngredient extends Model {

    @Column(onDelete = Column.ForeignKeyAction.CASCADE)
    public Course course;

    @Column(onDelete = Column.ForeignKeyAction.CASCADE)
    public Ingredient ingredient;
}
