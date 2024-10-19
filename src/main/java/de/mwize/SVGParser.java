package de.mwize;

import de.mwize.elements.*;
import de.mwize.elements.Polygon;
import de.mwize.elements.Rectangle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SVGParser {
    static final int width = Main.width, height = Main.height-80;
    static Point startPoint = new Point(0, 0);
    static double scaleFactor;

    public static ArrayList<ArrayList<Point>> parseSVG(String filePath) throws IOException {
        Document html = Jsoup.parse(new File(filePath));
        Element svg = html.getElementsByTag("svg").get(0);
        calculateScaleFactors(svg);

        ArrayList<ArrayList<Point>> pointsByDrawings = new ArrayList<>();
        parseElements(svg.children(), pointsByDrawings);
        return pointsByDrawings;
    }

    private static void calculateScaleFactors(Element svg) {
        if (svg.hasAttr("width")) {
            double svgWidth = parseSize(svg.attr("width"));
            double svgHeight = parseSize(svg.attr("height"));
            scaleFactor = Math.min(width / svgWidth, height / svgHeight);
        }
        if (svg.hasAttr("viewBox")) {
            String[] viewBox = svg.attr("viewBox").split(" ");
            startPoint = new Point((int) Double.parseDouble(viewBox[0]), (int) Double.parseDouble(viewBox[1]));
            scaleFactor = Math.min(width / Double.parseDouble(viewBox[2]), height / Double.parseDouble(viewBox[3]));
        }
    }

    private static double parseSize(String size) {
        return size.endsWith("px") ? Double.parseDouble(size.replace("px", "")) : Double.parseDouble(size);
    }

    private static void parseElements(ArrayList<Element> elements, ArrayList<ArrayList<Point>> pointsByDrawings) {
        for (Element e : elements) {
            switch (e.tagName()) {
                case "path" -> pointsByDrawings.addAll(parsePath(e.attr("d")));
                case "rect" -> pointsByDrawings.add(parseRect(e));
                case "circle" -> pointsByDrawings.add(parseCircle(e));
                case "ellipse" -> pointsByDrawings.add(parseEllipse(e));
                case "line" -> pointsByDrawings.add(parseLine(e));
                case "polygon" -> pointsByDrawings.add(Polygon.readPolygonData(e.attr("points"), scaleFactor));
                case "g" -> parseElements(e.children(), pointsByDrawings);
            }
        }
    }

    private static ArrayList<ArrayList<Point>> parsePath(String d) {
        ArrayList<ArrayList<Point>> points = new ArrayList<>();
        for (Object drawing : SVGPathParser.readPathData(d, scaleFactor)) {
            if (drawing instanceof CubicBezierCurve curve) points.add(curve.calculateCoordsFromControlPoints());
            else if (drawing instanceof QuadraticBezierCurve curve) points.add(curve.calculateCoordsFromControlPoints());
            else if (drawing instanceof Line line) points.add(line.getControlPoints(scaleFactor));
        }
        return points;
    }

    private static ArrayList<Point> parseRect(Element e) {
        double x = parseAttr(e, "x"), y = parseAttr(e, "y");
        double width = parseAttr(e, "width"), height = parseAttr(e, "height");
        double rx = parseAttr(e, "rx"), ry = parseAttr(e, "ry");
        return new Rectangle(new Point((int) x, (int) y), rx, ry, width, height, scaleFactor).getCoordinates();
    }

    private static ArrayList<Point> parseCircle(Element e) {
        double cx = parseAttr(e, "cx"), cy = parseAttr(e, "cy"), r = parseAttr(e, "r");
        return new Circle(new Point((int) cx, (int) cy), r, scaleFactor).getCoordinates();
    }

    private static ArrayList<Point> parseEllipse(Element e) {
        double cx = parseAttr(e, "cx"), cy = parseAttr(e, "cy");
        double rx = parseAttr(e, "rx"), ry = parseAttr(e, "ry");
        return new Ellipse(new Point((int) cx, (int) cy), rx, ry, scaleFactor).getCoordinates();
    }

    private static ArrayList<Point> parseLine(Element e) {
        Point p1 = new Point((int) parseAttr(e, "x1"), (int) parseAttr(e, "y1"));
        Point p2 = new Point((int) parseAttr(e, "x2"), (int) parseAttr(e, "y2"));
        ArrayList<Point> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        return points;
    }

    private static double parseAttr(Element e, String attr) {
        return e.hasAttr(attr) ? Double.parseDouble(e.attr(attr)) * scaleFactor : 0;
    }
}
