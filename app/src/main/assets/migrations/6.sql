ALTER TABLE Courses ADD COLUMN dinnerSecondCourse INTEGER;
UPDATE Courses SET dinner = firstCourse;
UPDATE Courses SET dinnerSecondCourse = secondCourse;

ALTER TABLE Days ADD COLUMN dinnerSecondCourse INTEGER;