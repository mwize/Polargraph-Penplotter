package de.mwize.elements;

import java.awt.*;
import java.util.ArrayList;

public class Ellipse {
    Point position;

    Double radiusX;
    Double radiusY;

    public Ellipse(Point position, Double radiusX, Double radiusY, double scale) {
        this.position = new Point((int) Math.round(position.x), (int) Math.round(position.y));
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    public ArrayList<Point> getCoordinates()   {
        ArrayList<Point> points = new ArrayList<>();
        int length = length();
        for (double i = 0; i < length; i += (2.0 * Math.PI) / length) {
            points.add(new Point((int) (position.x + radiusX * Math.cos(i)), (int) (position.y + radiusY * Math.sin(i))));
        }
        return points;
    }

    int length()    {
        return (int) Math.round(Math.PI*Math.sqrt(2*(Math.pow(radiusX,2) + Math.pow(radiusY, 2))));
    }

}
