package de.mwize;

import de.mwize.elements.CubicBezierCurve;
import de.mwize.elements.QuadraticBezierCurve;
import de.mwize.elements.Line;

import java.awt.*;
import java.util.ArrayList;

public class SVGPathParser {

    public static ArrayList<Object> readPathData(String pathData, double scale) {
        ArrayList<Object> drawings = new ArrayList<>();
        String[] pathDataParts = pathData.split("(?=[a-zA-Z])"); // Split between letters
        Point currentPosition = new Point(0, 0);
        Point startPoint = null;
        Point currentLastControlPoint = null;

        for (String pathDataPart : pathDataParts) {
            ArrayList<String> coordinates = parseCoordinates(pathDataPart, scale);

            switch (pathDataPart.charAt(0)) {
                case 'M', 'm' -> {
                    currentPosition = calculateNewPosition(pathDataPart.charAt(0), coordinates, currentPosition);
                    startPoint = currentPosition;
                    currentLastControlPoint = currentPosition;
                }
                case 'L', 'l', 'H', 'h', 'V', 'v' -> {
                    drawings.addAll(Line.controlPointCoordsToLines(currentPosition, coordinates, isRelative(pathDataPart.charAt(0))));
                    updateCurrentPositionAndControlPoint(drawings, currentPosition, currentLastControlPoint);
                }
                case 'Z', 'z' -> {
                    drawings.add(new Line(currentPosition, startPoint, false));
                    currentPosition = startPoint;
                    currentLastControlPoint = startPoint;
                }
                case 'Q', 'q', 'T', 't' -> {
                    ArrayList<QuadraticBezierCurve> curves = QuadraticBezierCurve.controlPointCoordsToQuadraticBeziers(currentPosition, coordinates, isRelative(pathDataPart.charAt(0)));
                    drawings.addAll(curves);
                    updateCurrentPositionAndControlPoint(curves, currentPosition, currentLastControlPoint);
                }
                case 'C', 'c', 'S', 's' -> {
                    ArrayList<CubicBezierCurve> curves = CubicBezierCurve.controlPointCoordsToCubicBeziers(currentPosition, coordinates, isRelative(pathDataPart.charAt(0)), scale);
                    drawings.addAll(curves);
                    updateCurrentPositionAndControlPoint(curves, currentPosition, currentLastControlPoint);
                }
                default -> throw new UnsupportedOperationException("Unsupported path command: " + pathDataPart.charAt(0));
            }
        }
        return drawings;
    }

    private static ArrayList<String> parseCoordinates(String pathDataPart, double scale) {
        ArrayList<String> coordinates = new ArrayList<>();
        if (pathDataPart.length() > 1) {
            String data = pathDataPart.substring(1).replace(" -", "-").replaceAll("\\s+|(?=-)|, |,", " ");
            String[] coordinateParts = data.split(" ");
            for (String coord : coordinateParts) {
                coordinates.add(String.valueOf(Double.parseDouble(coord) * scale));
            }
        }
        return coordinates;
    }

    private static Point calculateNewPosition(char command, ArrayList<String> coordinates, Point currentPosition) {
        int x = (int) Math.round(Double.parseDouble(coordinates.get(0)));
        int y = (int) Math.round(Double.parseDouble(coordinates.get(1)));
        if (command == 'm') {
            return new Point(currentPosition.x + x, currentPosition.y + y);
        }
        return new Point(x, y);
    }

    private static void updateCurrentPositionAndControlPoint(ArrayList<?> shapes, Point currentPosition, Point currentLastControlPoint) {
        if (!shapes.isEmpty() && shapes.get(shapes.size() - 1) instanceof Line lastLine) {
            currentPosition.setLocation(lastLine.getEndPoint());
            currentLastControlPoint.setLocation(lastLine.getEndPoint());
        } else if (!shapes.isEmpty() && shapes.get(shapes.size() - 1) instanceof QuadraticBezierCurve lastCurve) {
            currentPosition.setLocation(lastCurve.getEndPoint());
            currentLastControlPoint.setLocation(lastCurve.getControlPoint1());
        } else if (!shapes.isEmpty() && shapes.get(shapes.size() - 1) instanceof CubicBezierCurve lastCurve) {
            currentPosition.setLocation(lastCurve.getEndPoint());
            currentLastControlPoint.setLocation(lastCurve.getControlPoint2());
        }
    }

    private static boolean isRelative(char command) {
        return Character.isLowerCase(command);
    }
}
