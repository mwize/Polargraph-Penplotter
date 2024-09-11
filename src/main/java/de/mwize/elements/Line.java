package de.mwize.elements;

import java.awt.*;
import java.util.ArrayList;

public class Line {
    private Point startPoint;
    private Point endPoint;
    private Boolean isRelative;
    public Line(Point startPoint, Point endPoint, Boolean isRelative)   {
        this.startPoint = new Point((int) Math.round(startPoint.x), (int) Math.round(startPoint.y));
        this.endPoint = new Point((int) Math.round(endPoint.x), (int) Math.round(endPoint.y));
        this.isRelative = isRelative;
    }


    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public Boolean isRelative() {
        return isRelative;
    }

    public ArrayList<Point> getControlPoints(double scale)  {
        ArrayList<Point> controlPoints = new ArrayList<>();
        controlPoints.add(startPoint);
        controlPoints.add(endPoint);
        return controlPoints;
    }

    public static ArrayList<Line> controlPointCoordsToLines(Point currentLocation, ArrayList<String> coordinates, Boolean isRelative)  {
        ArrayList<Line> lines = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i+=2) {
            if (isRelative) {
                lines.add(new Line(
                        currentLocation,
                        new Point(currentLocation.x + (int) Math.round(Double.parseDouble(coordinates.get(i))), currentLocation.y + (int) Math.round(Double.parseDouble(coordinates.get(i + 1)))),
                        true
                ));
            } else {
                lines.add(new Line(
                        currentLocation,
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i))), (int) Math.round(Double.parseDouble(coordinates.get(i + 1)))),
                        false
                ));
            }
            currentLocation = lines.get(lines.size()-1).getEndPoint();

        }
        return lines;
    }

    public static ArrayList<Line> controlPointCoordsToHorizontalLines(Point currentLocation, ArrayList<String> coordinates, Boolean isRelative)  {
        ArrayList<Line> lines = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++) {
            if (isRelative) {
                lines.add(new Line(
                        currentLocation,
                        new Point(currentLocation.x + (int) Math.round(Double.parseDouble(coordinates.get(i))), currentLocation.y),
                        true
                ));
            } else {
                lines.add(new Line(
                        currentLocation,
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i))), currentLocation.y),
                        false
                ));
            }
            currentLocation = lines.get(lines.size()-1).getEndPoint();

        }
        return lines;
    }

    public static ArrayList<Line> controlPointCoordsToVerticalLines(Point currentLocation, ArrayList<String> coordinates, Boolean isRelative)  {
        ArrayList<Line> lines = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++) {
            if (isRelative) {
                lines.add(new Line(
                        currentLocation,
                        new Point(currentLocation.x, currentLocation.y+(int) Math.round(Double.parseDouble(coordinates.get(i)))),
                        true
                ));
            } else {
                lines.add(new Line(
                        currentLocation,
                        new Point(currentLocation.x, (int) Math.round(Double.parseDouble(coordinates.get(i)))),
                        false
                ));
            }
            currentLocation = lines.get(lines.size()-1).getEndPoint();

        }
        return lines;
    }
}
