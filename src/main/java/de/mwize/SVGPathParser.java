package de.mwize;

import de.mwize.elements.CubicBezierCurve;
import de.mwize.elements.QuadraticBezierCurve;

import java.awt.*;
import java.util.ArrayList;

public class SVGPathParser {
    public static String exampleData = "M788.1 340.9c-5.8 4.5-108.2 62.2-108.2 190.5 0 148.4 130.3 200.9 134.2 202.2-.6 3.2-20.7 71.9-68.7 141.9-42.8 61.6-87.5 123.1-155.5 123.1s-85.5-39.5-164-39.5c-76.5 0-103.7 40.8-165.9 40.8s-105.6-57-155.5-127C46.7 790.7 0 663 0 541.8c0-194.4 126.4-297.5 250.8-297.5 66.1 0 121.2 43.4 162.7 43.4 39.5 0 101.1-46 176.3-46 28.5 0 130.9 2.6 198.3 99.2zm-234-181.5mc31.1-36.9 53.1-88.1 53.1-139.3 0-7.1-.6-14.3-1.9-20.1-50.6 1.9-110.8 33.7-147.1 75.8-28.5 32.4-55.1 83.6-55.1 135.5 0 7.8 1.3 15.6 1.9 18.1 3.2.6 8.4 1.3 13.6 1.3 45.4 0 102.5-30.4 135.5-71.3z";
    //public static String exampleData = "M10 80 C40 10 100 10 95 80 s55 70 85 20z";
    public static ArrayList<Object> readPathData(String pathData, double scale) {
        ArrayList<Object> drawings = new ArrayList<>();
        String[] pathDataParts = pathData.split("(?=[a-zA-Z])"); //split between letters
        Point currentPosition = new Point(0, 0);
        Point startPoint = null;
        Point currentLastControlPoint = null;
        for (String pathDataPart : pathDataParts) {
            ArrayList<String> coordinates = getStrings(pathDataPart);
            coordinates.replaceAll(s -> String.valueOf((Double.parseDouble(s) * scale)));

            switch (pathDataPart.charAt(0)) {
                case 'M' -> {
                    currentPosition = new Point((int) Math.round(Double.parseDouble(coordinates.get(0))), (int) Math.round(Double.parseDouble(coordinates.get(1))));
                    startPoint = currentPosition;
                    currentLastControlPoint = currentPosition;
                }
                case 'm' ->{
                    currentPosition = new Point((int) Math.round(Double.parseDouble(coordinates.get(0))) + currentPosition.x, (int) Math.round(Double.parseDouble(coordinates.get(1))) + currentPosition.y);
                    startPoint = currentPosition;
                    currentLastControlPoint = currentPosition;

                    ArrayList<String> points = new ArrayList<>();

                    for (int i = 2; i < coordinates.size(); i++) {
                        points.add(coordinates.get(i));
                    }
                    if (points.size() > 1) {
                        ArrayList<de.mwize.elements.Line> lines = de.mwize.elements.Line.controlPointCoordsToLines(currentPosition, points, true);
                        drawings.addAll(lines);
                        currentLastControlPoint = lines.get(lines.size() - 1).getEndPoint();
                        currentPosition = lines.get(lines.size() - 1).getEndPoint();
                    }

                }
                case 'L' -> {
                    ArrayList<de.mwize.elements.Line> lines = de.mwize.elements.Line.controlPointCoordsToLines(currentPosition, coordinates, false);
                    drawings.addAll(lines);
                    currentLastControlPoint = lines.get(lines.size()-1).getEndPoint();
                    currentPosition = lines.get(lines.size()-1).getEndPoint();
                }
                case 'l' -> {
                    ArrayList<de.mwize.elements.Line> lines = de.mwize.elements.Line.controlPointCoordsToLines(currentPosition, coordinates, true);
                    drawings.addAll(lines);
                    currentLastControlPoint = lines.get(lines.size()-1).getEndPoint();
                    currentPosition = lines.get(lines.size()-1).getEndPoint();
                }
                case 'H' -> {
                    ArrayList<de.mwize.elements.Line> lines = de.mwize.elements.Line.controlPointCoordsToHorizontalLines(currentPosition, coordinates, false);
                    drawings.addAll(lines);
                    currentLastControlPoint = lines.get(lines.size()-1).getEndPoint();
                    currentPosition = lines.get(lines.size()-1).getEndPoint();
                }
                case 'h' -> {
                    ArrayList<de.mwize.elements.Line> lines = de.mwize.elements.Line.controlPointCoordsToHorizontalLines(currentPosition, coordinates, true);
                    drawings.addAll(lines);
                    currentLastControlPoint = lines.get(lines.size()-1).getEndPoint();
                    currentPosition = lines.get(lines.size()-1).getEndPoint();
                }
                case 'V' -> {
                    ArrayList<de.mwize.elements.Line> lines = de.mwize.elements.Line.controlPointCoordsToVerticalLines(currentPosition, coordinates, false);
                    drawings.addAll(lines);
                    currentLastControlPoint = lines.get(lines.size()-1).getEndPoint();
                    currentPosition = lines.get(lines.size()-1).getEndPoint();
                }
                case 'v' -> {
                    ArrayList<de.mwize.elements.Line> lines = de.mwize.elements.Line.controlPointCoordsToVerticalLines(currentPosition, coordinates, true);
                    drawings.addAll(lines);
                    currentLastControlPoint = lines.get(lines.size()-1).getEndPoint();
                    currentPosition = lines.get(lines.size()-1).getEndPoint();
                }
                case 'Z', 'z' -> {
                    drawings.add(new de.mwize.elements.Line(currentPosition, startPoint, false));
                    currentLastControlPoint = startPoint;
                    currentPosition = startPoint;
                    startPoint = null;
                }
                case 'Q' -> {
                    ArrayList<QuadraticBezierCurve> curves = QuadraticBezierCurve.controlPointCoordsToQuadraticBeziers(currentPosition, coordinates, false);
                    drawings.addAll(curves);
                    currentLastControlPoint = curves.get(curves.size()-1).getControlPoint1();
                    currentPosition = curves.get(curves.size()-1).getEndPoint();
                }
                case 'q' -> {
                    ArrayList<QuadraticBezierCurve> curves = QuadraticBezierCurve.controlPointCoordsToQuadraticBeziers(currentPosition, coordinates, true);
                    drawings.addAll(curves);
                    currentLastControlPoint = curves.get(curves.size()-1).getControlPoint1();
                    currentPosition = curves.get(curves.size()-1).getEndPoint();
                }
                case 'T' -> {
                    ArrayList<QuadraticBezierCurve> curves = QuadraticBezierCurve.controlPointCoordsToSmoothQuadraticBeziers(currentLastControlPoint, currentPosition, coordinates, false);
                    drawings.addAll(curves);
                    currentLastControlPoint = curves.get(curves.size()-1).getControlPoint1();
                    currentPosition = curves.get(curves.size()-1).getEndPoint();
                }
                case 't' -> {
                    ArrayList<QuadraticBezierCurve> curves = QuadraticBezierCurve.controlPointCoordsToSmoothQuadraticBeziers(currentLastControlPoint, currentPosition, coordinates, true);
                    drawings.addAll(curves);
                    currentLastControlPoint = curves.get(curves.size()-1).getControlPoint1();
                    currentPosition = curves.get(curves.size()-1).getEndPoint();
                }
                case 'C' -> {
                    ArrayList<CubicBezierCurve> curves = CubicBezierCurve.controlPointCoordsToCubicBeziers(currentPosition, coordinates, false, scale);
                    drawings.addAll(curves);
                    currentLastControlPoint = curves.get(curves.size()-1).getControlPoint2();
                    currentPosition = curves.get(curves.size()-1).getEndPoint();
                }
                case 'c' -> {
                    ArrayList<CubicBezierCurve> curves = CubicBezierCurve.controlPointCoordsToCubicBeziers(currentPosition, coordinates, true, scale);
                    drawings.addAll(curves);
                    currentLastControlPoint = curves.get(curves.size()-1).getControlPoint2();
                    currentPosition = curves.get(curves.size()-1).getEndPoint();
                }
                case 'S' -> {
                    ArrayList<CubicBezierCurve> curves = CubicBezierCurve.controlPointCoordsToSmoothCubicBeziers(currentLastControlPoint, currentPosition, coordinates, false, scale);
                    drawings.addAll(curves);
                    currentLastControlPoint = curves.get(curves.size()-1).getControlPoint2();
                    currentPosition = curves.get(curves.size()-1).getEndPoint();
                }
                case 's' -> {
                    ArrayList<CubicBezierCurve> curves = CubicBezierCurve.controlPointCoordsToSmoothCubicBeziers(currentLastControlPoint, currentPosition, coordinates, true, scale);
                    drawings.addAll(curves);
                    currentLastControlPoint = curves.get(curves.size()-1).getControlPoint2();
                    currentPosition = curves.get(curves.size()-1).getEndPoint();
                }

                default -> {
                }
            }
        }
        return drawings;

    }

    private static ArrayList<String> getStrings(String pathDataPart) {
        ArrayList<String> coordinates = new ArrayList<>();
        if (pathDataPart.length() != 1) {
            if (pathDataPart.charAt(1) == ' ') pathDataPart = pathDataPart.charAt(0) + pathDataPart.substring(2);
            pathDataPart = pathDataPart.replace(" -", "-");
            String[] coordinatesNotSplitByPoints = pathDataPart.substring(1).split("\\s+|(?=-)|, |,"); //split between spaces, minusses and commas
            for (int i = 0; i < coordinatesNotSplitByPoints.length; i++) {
                String[] coords = coordinatesNotSplitByPoints[i].split("\\.");
                if (coords.length > 1) {
                    coordinates.add(coords[0] + "." + coords[1]);
                    for (int j = 2; j < coords.length; j++) {
                        coordinates.add("." + coords[j]);
                    }
                } else {
                    coordinates.add(coords[0]);
                }


            }
        }
        return coordinates;
    }





}
