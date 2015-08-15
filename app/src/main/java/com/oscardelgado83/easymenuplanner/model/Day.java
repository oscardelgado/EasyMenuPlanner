package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by oscar on 15/03/15.
 */
@Table(name = "Days")
public class Day extends Model {

    private final String[] dayNames = new DateFormatSymbols().getShortWeekdays();
    private final int firstDay = Calendar.getInstance().getFirstDayOfWeek();

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
        int indexWithCurrentOrder = (int) (((getId() - 1) + firstDay - 1) % (dayNames.length - 1) + 1);
        return dayNames[indexWithCurrentOrder];
    }
}
