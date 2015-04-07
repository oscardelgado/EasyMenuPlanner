package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

/**
 * Created by oscar on 15/03/15.
 */
public class Day extends Model {

    @Column (index = true)
    public Date date;

    @Column
    public Course firstCourse;

    @Column
    public Course secondCourse;

    public List<Day> findAll() {
        return new Select().from(Day.class).orderBy("date ASC").execute();
    }
}
