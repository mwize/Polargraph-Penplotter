package de.mwize.elements;

import java.awt.*;
import java.util.ArrayList;

public class Circle {

    Point position;

    Double radius;

    public Circle(Point position, Double radius, double scale) {
        this.position = new Point((int) Math.round(position.x), (int) Math.round(position.y));
        this.radius = radius;
    }

    public ArrayList<Point> getCoordinates()   {
        ArrayList<Point> points = new ArrayList<>();
        for (double i = 0; i < length(); i += (2.0 * Math.PI) / length()) {
            points.add(new Point((int) (position.x + radius * Math.cos(i)), (int) (position.y + radius * Math.sin(i))));
        }
        return points;
    }

    int length()    {
        return (int) Math.round((2 * radius * Math.PI));
    }

}
