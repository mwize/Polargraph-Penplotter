package de.mwize.elements;

import java.awt.*;
import java.util.ArrayList;

public class Rectangle {

    Point startPoint;
    Double width;
    Double height;
    Double cornerRadiusX;
    Double cornerRadiusY;

    public Rectangle(Point startPoint, Double cornerRadiusX, Double cornerRadiusY, Double width, Double height, double scale) {
        this.startPoint = new Point((int) Math.round(startPoint.x), (int) Math.round(startPoint.y));
        this.cornerRadiusX = cornerRadiusX;
        this.cornerRadiusY = cornerRadiusY;
        this.width = width;
        this.height = height;
    }

    public ArrayList<Point> getCoordinates()   {
        ArrayList<Point> points = new ArrayList<>();
        if (cornerLength() != 0)    {
            for (double i = 0; i < Math.PI/2.0; i += (((Math.PI)/2.0) / cornerLength())) {
                points.add(new Point((int) ((width+startPoint.x + cornerRadiusX * Math.cos(i)) - cornerRadiusX), (int) ((height + startPoint.y + cornerRadiusY * Math.sin(i)) - cornerRadiusY)));
            }

            for (double i = Math.PI/2.0+(((Math.PI)/2.0) / cornerLength()); i < Math.PI; i += (((Math.PI)/2.0) / cornerLength())) {
                points.add(new Point((int) ((startPoint.x + cornerRadiusX * Math.cos(i)) + cornerRadiusX), (int) ((height + startPoint.y + cornerRadiusY * Math.sin(i)) - cornerRadiusY)));
            }

            for (double i = Math.PI+(((Math.PI)/2.0) / cornerLength()); i < (Math.PI + Math.PI / 2.0); i += (((Math.PI)/2.0) / cornerLength())) {
                points.add(new Point((int) ((startPoint.x + cornerRadiusX * Math.cos(i)) + cornerRadiusX), (int) ((startPoint.y + cornerRadiusY * Math.sin(i)) + cornerRadiusY)));
            }

            for (double i = Math.PI + Math.PI / 2.0+(((Math.PI)/2.0) / cornerLength()); i < (Math.PI * 2.0); i += (((Math.PI)/2.0) / cornerLength())) {
                points.add(new Point((int) (((startPoint.x + cornerRadiusX * Math.cos(i)) + width) - cornerRadiusX), (int) ((startPoint.y + cornerRadiusY * Math.sin(i)) + cornerRadiusY)));
            }
            points.add(new Point((int) ((width+startPoint.x + cornerRadiusX * Math.cos(0)) - cornerRadiusX), (int) ((height+startPoint.y + cornerRadiusY * Math.sin(0)) - cornerRadiusY)));
        }   else {
            points.add(startPoint);
            points.add(new Point((int) Math.round(startPoint.x+width), startPoint.y));
            points.add(new Point((int) Math.round(startPoint.x+width), (int) Math.round(startPoint.y+height)));
            points.add(new Point((startPoint.x), (int) Math.round(startPoint.y+height)));
            points.add(startPoint);
        }
        return points;
    }

    int cornerLength()    {
        return (int) Math.round(Math.PI*Math.sqrt(2*(Math.pow(cornerRadiusX,2) + Math.pow(cornerRadiusY, 2)))); //https://mathespass.at/formeln/ellipse-formeln-und-eigenschaften
    }


}
