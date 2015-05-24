package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

/**
 * Created by oscar on 15/03/15.
 */
@Table(name = "Days")
public class Day extends Model {

    @Column (index = true)
    public Date date;

    @Column(onDelete = Column.ForeignKeyAction.SET_NULL)
    public Course firstCourse;

    @Column(onDelete = Column.ForeignKeyAction.SET_NULL)
    public Course secondCourse;

    public static List<Day> findAll() {
        return new Select().from(Day.class).orderBy("date ASC").execute();
    }

    @Override
    public String toString() {
        return "Day{" +
                "date=" + date +
                ", firstCourse=" + firstCourse +
                ", secondCourse=" + secondCourse +
                '}';
    }
}
