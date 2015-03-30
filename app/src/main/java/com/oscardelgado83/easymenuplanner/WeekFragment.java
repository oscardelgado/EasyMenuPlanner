package com.oscardelgado83.easymenuplanner;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.oscardelgado83.easymenuplanner.model.Course;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
* Created by oscar on 23/03/15.
*/
public class WeekFragment extends Fragment {

    @InjectView(R.id.day1)
    TableRow tableRow1;

    @InjectView(R.id.day2)
    TableRow tableRow2;

    @InjectView(R.id.day3)
    TableRow tableRow3;

    @InjectView(R.id.day4)
    TableRow tableRow4;

    @InjectView(R.id.day5)
    TableRow tableRow5;

    @InjectView(R.id.day6)
    TableRow tableRow6;

    @InjectView(R.id.day7)
    TableRow tableRow7;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        ButterKnife.inject(this, view);

        TableRow[] allTableRows =  {tableRow1, tableRow2, tableRow3, tableRow4, tableRow5, tableRow6, tableRow7};
        for(TableRow tr : allTableRows) {
            Course course = getRandomCourse();

            TextView tvA = (TextView) tr.findViewById(R.id.textViewA);
            tvA.setText(course.name);

            Course course2 = getRandomCourse();

            TextView tvB = (TextView) tr.findViewById(R.id.textViewB);
            tvB.setText(course2.name);

            Button btnLeftA = (Button) tr.findViewById(R.id.buttonLeftA);
            btnLeftA.setOnClickListener(courseBtnClickListener(tvA));

            Button btnRightA = (Button) tr.findViewById(R.id.buttonRightA);
            btnRightA.setOnClickListener(courseBtnClickListener(tvA));

            Button btnLeftB = (Button) tr.findViewById(R.id.buttonLeftB);
            btnLeftB.setOnClickListener(courseBtnClickListener(tvB));

            Button btnRightB = (Button) tr.findViewById(R.id.buttonRightB);
            btnRightB.setOnClickListener(courseBtnClickListener(tvB));
        }

        return view;
    }

    private View.OnClickListener courseBtnClickListener(final TextView tv) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(getRandomCourse().name);
            }
        };
    }

    private Course getRandomCourse() {
        return new Select()
                        .from(Course.class)
    //                    .where("Category = ?", category.getId())
                        .orderBy("RANDOM()")
                        .executeSingle();
    }

//    @OnClick({R.id.buttonLeftA, R.id.buttonRightA, R.id.buttonLeftB, R.id.buttonRightB})
//    public void changeCourseClickListener(Button b) {
//        TextView tv = null;
//        if (b.equals(btnLeftA)) {
//            tv = (TextView) tableRow1.findViewById(R.id.textViewA);
//        } else if (b.equals(btnLeftB)) {
//            tv = (TextView) tableRow1.findViewById(R.id.textViewA);
//        } else if (b.equals(btnRigthA)) {
//            tv = (TextView) tableRow1.findViewById(R.id.textViewB);
//        } else if (b.equals(btnRightB)) {
//            tv = (TextView) tableRow1.findViewById(R.id.textViewB);
//        }
//        tv.setText(getRandomCourse().name);
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
