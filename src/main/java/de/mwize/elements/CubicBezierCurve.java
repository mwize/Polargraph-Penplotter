package de.mwize.elements;

import de.mwize.utils.BezierUtils;

import java.awt.*;
import java.util.ArrayList;

public class CubicBezierCurve {
    private Point startPoint;
    private Point endPoint;
    private Point controlPoint1;
    private Point controlPoint2;
    private Boolean isRelative;
    public CubicBezierCurve(Point startPoint, Point controlPoint1, Point controlPoint2, Point endPoint, Boolean isRelative)   {
        this.startPoint = new Point((int) Math.round(startPoint.x), (int) Math.round(startPoint.y));
        this.controlPoint1 = new Point((int) Math.round(controlPoint1.x), (int) Math.round(controlPoint1.y));
        this.controlPoint2 = new Point((int) Math.round(controlPoint2.x), (int) Math.round(controlPoint2.y));
        this.endPoint = new Point((int) Math.round(endPoint.x), (int) Math.round(endPoint.y));
        this.isRelative = isRelative;

    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public Point getControlPoint1() {
        return controlPoint1;
    }

    public Point getControlPoint2() {
        return controlPoint2;
    }
    public Boolean isRelative() {
        return isRelative;
    }

    public ArrayList<Point> getControlPoints()  {
        ArrayList<Point> controlPoints = new ArrayList<>();
        controlPoints.add(startPoint);
        controlPoints.add(controlPoint1);
        controlPoints.add(controlPoint2);
        controlPoints.add(endPoint);
        return controlPoints;
    }

    public static ArrayList<CubicBezierCurve> controlPointCoordsToCubicBeziers(Point currentLocation, ArrayList<String> coordinates, Boolean isRelative, Double scale)   {
        ArrayList<CubicBezierCurve> cubicBezierCurves = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i+=6) {
            if (isRelative) {
                cubicBezierCurves.add(new CubicBezierCurve(
                        currentLocation,
                        new Point(currentLocation.x + (int) Math.round(Double.parseDouble(coordinates.get(i))), currentLocation.y + (int) Math.round(Double.parseDouble(coordinates.get(i + 1)))),
                        new Point(currentLocation.x + (int) Math.round(Double.parseDouble(coordinates.get(i + 2))), currentLocation.y + (int) Math.round(Double.parseDouble(coordinates.get(i + 3)))),
                        new Point(currentLocation.x + (int) Math.round(Double.parseDouble(coordinates.get(i + 4))), currentLocation.y + (int) Math.round(Double.parseDouble(coordinates.get(i + 5)))),
                        true
                ));
            } else {
                cubicBezierCurves.add(new CubicBezierCurve(
                        currentLocation,
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i))), (int) Math.round(Double.parseDouble(coordinates.get(i + 1)))),
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i + 2))), (int) Math.round(Double.parseDouble(coordinates.get(i + 3)))),
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i + 4))), (int) Math.round(Double.parseDouble(coordinates.get(i + 5)))),
                        false
                ));
            }
            currentLocation = cubicBezierCurves.get(cubicBezierCurves.size()-1).endPoint;

        }
        return cubicBezierCurves;
    }

    public ArrayList<Point> calculateCoordsFromControlPoints() {
        ArrayList<Point> controlPoints = getControlPoints();

        double t = .5;
        while (Math.round(BezierUtils.length(controlPoints, t) / 1.1) != Math.round(BezierUtils.length(controlPoints, t / 2) / 1.1)) {
            t /= 2;
        }
        ArrayList<Point> output = new ArrayList<>();
        for (double i = 0; i < 1; i += t) {
            Point p = BezierUtils.bezier(controlPoints, i);
            if (output.isEmpty()) {
                output.add(p);
            } else {
                Point lastElement = (output.get(output.size() - 1));
                if (!((lastElement.x == p.x) && (lastElement.y == p.y))) {
                    output.add(p);
                }
            }
        }
        output.add(BezierUtils.bezier(controlPoints, 1));
        return output;
    }


    public static ArrayList<CubicBezierCurve> controlPointCoordsToSmoothCubicBeziers(Point lastControlPoint, Point currentPosition, ArrayList<String> coordinates, Boolean isRelative, Double scale)   {
        ArrayList<CubicBezierCurve> smoothCubicBezierCurves = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i+=4) {
            if (isRelative) {
                smoothCubicBezierCurves.add(new CubicBezierCurve(
                        currentPosition,
                        new Point(2*currentPosition.x - lastControlPoint.x, 2 * currentPosition.y - lastControlPoint.y),
                        new Point(currentPosition.x + (int) Math.round(Double.parseDouble(coordinates.get(i))), currentPosition.y + (int) Math.round(Double.parseDouble(coordinates.get(i+1)))),
                        new Point(currentPosition.x + (int) Math.round(Double.parseDouble(coordinates.get(i+2))), currentPosition.y + (int) Math.round(Double.parseDouble(coordinates.get(i+3)))),
                        true
                ));
            }   else {
                smoothCubicBezierCurves.add(new CubicBezierCurve(
                        currentPosition,
                        new Point(2 * currentPosition.x - lastControlPoint.x, 2 * currentPosition.y - lastControlPoint.y),
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i))), (int) Math.round(Double.parseDouble(coordinates.get(i+1)))),
                        new Point((int) Math.round(Double.parseDouble(coordinates.get(i+2))), (int) Math.round(Double.parseDouble(coordinates.get(i+3)))),
                        true
                ));
            }
            lastControlPoint = smoothCubicBezierCurves.get(smoothCubicBezierCurves.size()-1).getControlPoint2();
            currentPosition = smoothCubicBezierCurves.get(smoothCubicBezierCurves.size()-1).endPoint;

        }
        return smoothCubicBezierCurves;
    }




}
