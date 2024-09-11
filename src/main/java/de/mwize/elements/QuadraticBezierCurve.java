package de.mwize.elements;

import de.mwize.utils.BezierUtils;

import java.awt.*;
import java.util.ArrayList;

public class QuadraticBezierCurve {
    private Point startPoint;
    private Point controlPoint1;
    private Point endPoint;
    private Boolean isRelative;
    public QuadraticBezierCurve(Point startPoint, Point controlPoint1, Point endPoint, Boolean isRelative)   {
        this.startPoint = new Point((int) Math.round(startPoint.x), (int) Math.round(startPoint.y));
        this.controlPoint1 = new Point((int) Math.round(controlPoint1.x), (int) Math.round(controlPoint1.y));
        this.endPoint = new Point((int) Math.round(endPoint.x), (int) Math.round(endPoint.y));
        this.isRelative = isRelative;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getControlPoint1() {
        return controlPoint1;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public Boolean isRelative() {
        return isRelative;
    }

    public ArrayList<Point> getControlPoints()  {
        ArrayList<Point> controlPoints = new ArrayList<>();
        controlPoints.add(startPoint);
        controlPoints.add(controlPoint1);
        controlPoints.add(endPoint);
        return controlPoints;
    }

    public ArrayList<Point> calculateCoordsFromControlPoints() {
        ArrayList<Point> controlPoints = getControlPoints();

        double t = .5;
        while (Math.round(BezierUtils.length(controlPoints, t)) != Math.round(BezierUtils.length(controlPoints, t / 2))) {
            System.out.println("Q: " + Math.round(BezierUtils.length(controlPoints, t)) +" "+ Math.round(BezierUtils.length(controlPoints, t / 2)));
            t /= 2;
        }
        ArrayList<Point> output = new ArrayList<>();
        for (double i = 0; i < 1; i += t) {
            Point p = BezierUtils.bezier(controlPoints, i);

            if (output.isEmpty()) {
                output.add(p);
            } else {
                Point lastElement = (output.get(output.size() - 1));
                output.add(p);
                if (!((lastElement.x == p.x) && (lastElement.y == p.y))) {
                    output.add(p);
                }
            }
        }
        output.add(BezierUtils.bezier(controlPoints, 1));
        return output;
    }


    public static ArrayList<QuadraticBezierCurve> controlPointCoordsToQuadraticBeziers(Point currentLocation, ArrayList<String> coordinates, Boolean isRelative)   {
        ArrayList<QuadraticBezierCurve> quadraticBezierCurves = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i+=4) {
            if (isRelative) {
                quadraticBezierCurves.add(new QuadraticBezierCurve(
                        currentLocation,
                        new Point(currentLocation.x + (int) Math.round(Double.parseDouble(coordinates.get(i))), currentLocation.y + (int) Math.round(Double.parseDouble(coordinates.get(i + 1)))),
                        new Point(currentLocation.x + (int) Math.round(Double.parseDouble(coordinates.get(i + 2))), currentLocation.y + (int) Math.round(Double.parseDouble(coordinates.get(i + 3)))),
                        true
                ));
            } else {
                quadraticBezierCurves.add(new QuadraticBezierCurve(
                        currentLocation,
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i))), (int) Math.round(Double.parseDouble(coordinates.get(i + 1)))),
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i + 2))), (int) Math.round(Double.parseDouble(coordinates.get(i + 3)))),
                        false
                ));
            }
            currentLocation = quadraticBezierCurves.get(quadraticBezierCurves.size()-1).endPoint;

        }
        return quadraticBezierCurves;
    }

    public static ArrayList<QuadraticBezierCurve> controlPointCoordsToSmoothQuadraticBeziers(Point lastControlPoint, Point currentPosition, ArrayList<String> coordinates, Boolean isRelative)   {
        ArrayList<QuadraticBezierCurve> smoothCubicBezierCurves = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i+=2) {
            if (isRelative) {
                smoothCubicBezierCurves.add(new QuadraticBezierCurve(
                        currentPosition,
                        new Point(2*currentPosition.x - lastControlPoint.x, 2 * currentPosition.y - lastControlPoint.y),
                        new Point(currentPosition.x + (int) Math.round(Double.parseDouble(coordinates.get(i))), currentPosition.y + (int) Math.round(Double.parseDouble(coordinates.get(i+1)))),
                        true
                ));
            }   else {
                smoothCubicBezierCurves.add(new QuadraticBezierCurve(
                        currentPosition,
                        new Point(2 * currentPosition.x - lastControlPoint.x, 2 * currentPosition.y - lastControlPoint.y),
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i))), (int) Math.round(Double.parseDouble(coordinates.get(i+1)))),
                        true
                ));
            }
            lastControlPoint = smoothCubicBezierCurves.get(smoothCubicBezierCurves.size()-1).getControlPoint1();
            currentPosition = smoothCubicBezierCurves.get(smoothCubicBezierCurves.size()-1).endPoint;

        }
        return smoothCubicBezierCurves;
    }


}
