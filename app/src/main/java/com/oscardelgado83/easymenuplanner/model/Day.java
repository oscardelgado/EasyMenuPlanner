package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.EMPApplication;

import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.List;

/**
 * Created by oscar on 15/03/15.
 */
@Table(name = "Days")
public class Day extends Model {

    private final String[] dayNames = new DateFormatSymbols().getShortWeekdays();

    @Column (index = true)
    public Date date;

    @Column(onDelete = Column.ForeignKeyAction.SET_NULL)
    public Course firstCourse;

    @Column(onDelete = Column.ForeignKeyAction.SET_NULL)
    public Course secondCourse;

    @Column(onDelete = Column.ForeignKeyAction.SET_NULL)
    public Course dinner;

    @Column(onDelete = Column.ForeignKeyAction.SET_NULL)
    public Course dinnerSecondCourse;

    @Column(onDelete = Column.ForeignKeyAction.SET_NULL)
    public Course breakfast;

    public static List<Day> findAll() {
        return new Select().from(Day.class).orderBy(
                "(Id + 7 - " + EMPApplication.USER_WEEK_START_DAY + ") % 7 ASC")
                .execute();
    }

    @Override
    public String toString() {
        return dayNames[getId().intValue()];
    }
}
