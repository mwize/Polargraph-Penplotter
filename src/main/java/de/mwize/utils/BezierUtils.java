package de.mwize.utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BezierUtils {

    public static Point bezier(List<Point> pointList, double t) {
        if (pointList.size() == 1) {
            return pointList.get(0);
        } else {
            List<Point> tempPoints = new ArrayList<>();
            for (int i = 0; i < pointList.size() - 1; i++) {
                tempPoints.add(new Point(
                        (int) Math.round(t * (pointList.get(i + 1).x
                                - pointList.get(i).x) + pointList.get(i).x),
                        (int) Math.round(t * (pointList.get(i + 1).y
                                - pointList.get(i).y) + pointList.get(i).y)));
            }
            return bezier(tempPoints, t);
        }
    }



    public static double length(ArrayList<Point> pointList, double stepLength)   {
        double sum = 0;
        Point last = null;
        for (double i = 0; (i) < 1; i+=stepLength) {
            Point p = bezier(pointList, i);
            Point p2 = bezier(pointList, i+stepLength);
            sum += Point.distance(p.x, p.y, p2.x, p2.y);
            last = p2;
        }
        sum += Point.distance(last.x, last.y, pointList.get(pointList.size()-1).x, pointList.get(pointList.size()-1).y);
        return sum;
    }



}


