package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

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

    public List<CourseIngredient> getNotCheckedIngredients() {
        return new Select().from(CourseIngredient.class)
                .join(Ingredient.class).on("CourseIngredients.ingredient = Ingredients.Id")
                .where("CourseIngredients.course = ?", this.getId())
                .and("Ingredients.checked = 0")
                .execute();
    }

    public int getNotCheckedIngredientsCount() {
        return new Select().from(CourseIngredient.class)
                .join(Ingredient.class).on("CourseIngredients.ingredient = Ingredients.Id")
                .where("CourseIngredients.course = ?", this.getId())
                .and("Ingredients.checked = 0")
                .count();
    }

    @Override
    public String toString() {
        return "Course{" +
                "name='" + name + '\'' +
                ", courseType=" + courseType +
                '}';
    }
}
