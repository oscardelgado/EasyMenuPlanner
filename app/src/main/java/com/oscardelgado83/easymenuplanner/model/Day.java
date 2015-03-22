package com.oscardelgado83.easymenuplanner.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

import java.util.Date;

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
}
