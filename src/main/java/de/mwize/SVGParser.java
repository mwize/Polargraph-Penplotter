package de.mwize;

import de.mwize.elements.Circle;
import de.mwize.elements.CubicBezierCurve;
import de.mwize.elements.Ellipse;
import de.mwize.elements.QuadraticBezierCurve;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SVGParser {
    static Point offsetXY = new Point(0, 80);
    static int width = 540;
    static int height = 780-80;

    static Point startPoint = new Point(0, 0);



    static double scaleFactor;


    public static ArrayList<ArrayList<Point>> parseSVG(String filePath) throws IOException {


        File input = new File(filePath);
        Document html = Jsoup.parse(input);
        Element svg = html.getElementsByTag("svg").get(0);
        if (svg.hasAttr("width")) {

            String svgWidth = svg.attr("width").replace("px", "");
            String svgHeight = svg.attr("height").replace("px", "");
            if (!svgWidth.equals("100%")) {

                scaleFactor = width / Double.parseDouble(svgWidth);
                if (scaleFactor > height / Double.parseDouble(svgHeight)) {
                    scaleFactor = height / Double.parseDouble(svgHeight);
                }
            }

        }
        if (svg.hasAttr("viewbox")) {
            String[] viewbox = svg.attr("viewbox").split(" ");
            startPoint = new Point((int) Math.round(Double.parseDouble(viewbox[0])), (int) Math.round(Double.parseDouble(viewbox[1])));

            scaleFactor = width / Double.parseDouble(viewbox[2]);
            if (scaleFactor > height / Double.parseDouble(viewbox[3])) {
                scaleFactor = height / Double.parseDouble(viewbox[3]);
            }
        }

        ArrayList<ArrayList<Point>> pointsByDrawings = new ArrayList<>();
        ArrayList<Element> svgElements = svg.children();
        for (int i = 0; i < svgElements.size(); i++) {
            Element e = svgElements.get(i);
            if (e.tagName().equals("path")) {
                ArrayList<Object> drawings = SVGPathParser.readPathData(e.attr("d"), scaleFactor);
                for (Object drawing : drawings) {
                    if (drawing.getClass().equals(CubicBezierCurve.class)) {
//                        ArrayList<Point> points = ((CubicBezierCurve) drawing).calculateCoordsFromControlPoints(scale);
                        pointsByDrawings.add(((CubicBezierCurve) drawing).calculateCoordsFromControlPoints());
//                                drawPointsConnectedByLines(g, points);
                    } else if (drawing.getClass().equals(QuadraticBezierCurve.class)) {
//                        ArrayList<Point> points = ((QuadraticBezierCurve) drawing).calculateCoordsFromControlPoints(scale);
                        pointsByDrawings.add(((QuadraticBezierCurve) drawing).calculateCoordsFromControlPoints());
//                                drawPointsConnectedByLines(g, points);
                    } else if (drawing.getClass().equals(de.mwize.elements.Line.class)) {
//                        ArrayList<Point> points = ((Line) drawing).getControlPoints(scale);
                        pointsByDrawings.add(((de.mwize.elements.Line) drawing).getControlPoints(scaleFactor));
//                                drawPointsConnectedByLines(g, points);
                    } else {
                        throw new IllegalStateException("Unexpected value: " + drawing.getClass());
                    }
                }
            } else if (e.tagName().equals("rect")) {
                double width = 0.0;
                double height = 0.0;
                double x = 0.0;
                double y = 0.0;
                double rx = 0.0;
                double ry = 0.0;
                if (e.hasAttr("width")) width = Double.parseDouble(e.attr("width")) * scaleFactor;
                if (e.hasAttr("height")) height = Double.parseDouble(e.attr("height")) * scaleFactor;
                if (e.hasAttr("x")) x = Double.parseDouble(e.attr("x")) * scaleFactor;
                if (e.hasAttr("y")) y = Double.parseDouble(e.attr("y")) * scaleFactor;
                if (e.hasAttr("rx")) rx = Double.parseDouble(e.attr("rx")) * scaleFactor;
                if (e.hasAttr("ry")) ry = Double.parseDouble(e.attr("ry")) * scaleFactor;

                de.mwize.elements.Rectangle rect = new de.mwize.elements.Rectangle(new Point((int) Math.round(x), (int) Math.round(y)), rx, ry, width, height, scaleFactor);
                pointsByDrawings.add(rect.getCoordinates());
//                        drawPointsConnectedByLines(g, rect.getCoordinates());
            } else if (e.tagName().equals("circle")) {
                double cx = 0.0;
                double cy = 0.0;
                double r = 0.0;

                if (e.hasAttr("cx")) cx = Double.parseDouble(e.attr("cx")) * scaleFactor;
                if (e.hasAttr("cy")) cy = Double.parseDouble(e.attr("cy")) * scaleFactor;
                if (e.hasAttr("r")) r = Double.parseDouble(e.attr("r")) * scaleFactor;

                Circle circle = new Circle(new Point((int) Math.round(cx), (int) Math.round(cy)), r, scaleFactor);
                pointsByDrawings.add(circle.getCoordinates());
//                        drawPointsConnectedByLines(g, circle.getCoordinates());
            } else if (e.tagName().equals("ellipse")) {
                double cx = 0.0;
                double cy = 0.0;
                double rx = 0.0;
                double ry = 0.0;

                if (e.hasAttr("cx")) cx = Double.parseDouble(e.attr("cx")) * scaleFactor;
                if (e.hasAttr("cy")) cy = Double.parseDouble(e.attr("cy")) * scaleFactor;
                if (e.hasAttr("rx")) rx = Double.parseDouble(e.attr("rx")) * scaleFactor;
                if (e.hasAttr("ry")) ry = Double.parseDouble(e.attr("ry")) * scaleFactor;

                Ellipse ellipse = new Ellipse(new Point((int) Math.round(cx), (int) Math.round(cy)), rx, ry, scaleFactor);
                pointsByDrawings.add(ellipse.getCoordinates());
//                        drawPointsConnectedByLines(g, ellipse.getCoordinates());
            } else if (e.tagName().equals("line")) {
                double x1 = 0.0;
                double y1 = 0.0;
                double x2 = 0.0;
                double y2 = 0.0;

                if (e.hasAttr("x1")) x1 = Double.parseDouble(e.attr("x1")) * scaleFactor;
                if (e.hasAttr("y1")) y1 = Double.parseDouble(e.attr("y1")) * scaleFactor;
                if (e.hasAttr("x2")) x2 = Double.parseDouble(e.attr("x2")) * scaleFactor;
                if (e.hasAttr("y2")) y2 = Double.parseDouble(e.attr("y2")) * scaleFactor;
                ArrayList<Point> points = new ArrayList<>();
                points.add(new Point((int) Math.round(x1), (int) Math.round(y1)));
                points.add(new Point((int) Math.round(x2), (int) Math.round(y2)));
                pointsByDrawings.add(points);
//                        drawPointsConnectedByLines(g, points);
            } else if (e.tagName().equals("polygon")) {
                pointsByDrawings.add(de.mwize.elements.Polygon.readPolygonData(e.attr("points"), scaleFactor));
//                        drawPointsConnectedByLines(g, Polygon.readPolygonData(e.attr("points"), scale));
            } else if (e.tagName().equals("g")) {
                svgElements.addAll(e.children());
            }
        }

//            }
//        };
//        frame.add(panel);
//        frame.setVisible(true);

        return pointsByDrawings;
    }


//    private static void drawPointsConnectedByLines(Graphics g, ArrayList<Point> points) {
//        for (int i = 0; i < (points.size() - 1); i++) {
//            g.drawLine((points.get(i).x), (points.get(i).y), (points.get(i + 1).x), (points.get(i + 1).y));
//        }
//
//    }
}
