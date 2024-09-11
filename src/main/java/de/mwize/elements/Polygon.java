package de.mwize.elements;

import java.awt.*;
import java.util.ArrayList;

public class Polygon {



    public static ArrayList<Point> readPolygonData(String pathData, Double scale) {
        ArrayList<String> coordinates = getStrings(pathData);
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < coordinates.size()-1; i+=2) {
            points.add(new Point((int) Math.round(Double.parseDouble(coordinates.get(i)) * scale), (int) Math.round(Double.parseDouble(coordinates.get(i+1)) * scale)));
        }
        points.add(points.get(0));
        return points;
    }



    private static ArrayList<String> getStrings(String pathDataPart) {
        ArrayList<String> coordinates = new ArrayList<>();
        if (pathDataPart.length() != 1) {
            pathDataPart = pathDataPart.replace(" -", "-");
            String[] coordinatesNotSplitByPoints = pathDataPart.split("\\s+|(?=-)|, |,"); //split between spaces or minuses
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
