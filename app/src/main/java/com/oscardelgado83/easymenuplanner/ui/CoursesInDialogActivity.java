package com.oscardelgado83.easymenuplanner.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.oscardelgado83.easymenuplanner.R;

public class CoursesInDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_in_dialog);
        getSupportFragmentManager().getFragments().get(0).getActivity()
                .setTitle(getString(R.string.select_course));

        // TODO: change listeners ? (at least the simple click)
        // TODO: if mantain long-press, change style of CAB
        // TODO: back icon on dialog
        // TODO (optional): search
        // TODO (optional): alphabetic scroll
    }
}
