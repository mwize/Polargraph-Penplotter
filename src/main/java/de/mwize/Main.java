package de.mwize;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class Main {

    // GPIO pin numbers – loaded from config.properties
    private static int PIN_ENABLE;
    private static int PIN_STEP;
    private static int PIN_DIR;
    private static int PIN_ENABLE2;
    private static int PIN_STEP2;
    private static int PIN_DIR2;

    static int width;
    static int height;
    static Point startPoint;
    static Point currentPoint;
    static int stepsPerMM;

    // Pi4J context and GPIO pins – initialised in main()
    static Context pi4j;
    static DigitalOutput step;
    static DigitalOutput step2;
    static DigitalOutput enableMotor1;
    static DigitalOutput enableMotor2;
    static DigitalOutput dir;
    static DigitalOutput dir2;

    static float currentLeftLength;
    static float currentRightLength;

    static double fullStepLeft = 0;
    static double fullStepRight = 0;

    public static void main(String[] args) throws InterruptedException, IOException {

        // Load hardware configuration
        Properties config = loadConfig();
        width      = Integer.parseInt(config.getProperty("width",      "540"));
        height     = Integer.parseInt(config.getProperty("height",     "780"));
        stepsPerMM = Integer.parseInt(config.getProperty("stepsPerMM", "25"));

        int startX = Integer.parseInt(config.getProperty("startX", String.valueOf(width / 2)));
        int startY = Integer.parseInt(config.getProperty("startY", "70"));

        PIN_ENABLE  = Integer.parseInt(config.getProperty("pin.left.enable",  "4"));
        PIN_STEP    = Integer.parseInt(config.getProperty("pin.left.step",    "18"));
        PIN_DIR     = Integer.parseInt(config.getProperty("pin.left.dir",     "24"));
        PIN_ENABLE2 = Integer.parseInt(config.getProperty("pin.right.enable", "12"));
        PIN_STEP2   = Integer.parseInt(config.getProperty("pin.right.step",   "19"));
        PIN_DIR2    = Integer.parseInt(config.getProperty("pin.right.dir",    "13"));

        // SVG file: first CLI argument, then config file, then built-in default
        String filePath = args.length > 0
                ? args[0]
                : config.getProperty("svg.file", "/home/pi/Desktop/svg3.svg");

        startPoint   = new Point(startX, startY);
        currentPoint = startPoint;

        currentLeftLength  = (float) (Math.sqrt(Math.pow(startPoint.x, 2) + Math.pow(startPoint.y, 2)) * stepsPerMM);
        currentRightLength = (float) (Math.sqrt(Math.pow(width - startPoint.x, 2) + Math.pow(startPoint.y, 2)) * stepsPerMM);

        // Initialise GPIO
        pi4j         = Pi4J.newAutoContext();
        step         = pi4j.digitalOutput().create(PIN_STEP);
        step2        = pi4j.digitalOutput().create(PIN_STEP2);
        enableMotor1 = pi4j.digitalOutput().create(PIN_ENABLE);
        enableMotor2 = pi4j.digitalOutput().create(PIN_ENABLE2);
        dir          = pi4j.digitalOutput().create(PIN_DIR);
        dir2         = pi4j.digitalOutput().create(PIN_DIR2);

        System.out.println(radiusLengthForPoint(startPoint.x, startPoint.y, 100, 100, 0, 0, 0) + " " + startPoint);
        System.out.println(Point.distance(startPoint.x, startPoint.y, 100, 100));
        dir.low();
        dir2.low();


        ArrayList<ArrayList<Point>> drawings = SVGParser.parseSVG(filePath);
        moveToStartPoint(drawings.get(0));


        for (ArrayList<Point> drawing : drawings) {
            drawPoints(drawing);
        }

        move(currentPoint, startPoint, 0, 0, width, 0);
        currentPoint = startPoint;
    }

    private static void moveToStartPoint(ArrayList<Point> drawing) throws InterruptedException {
        move(currentPoint, new Point(drawing.get(0).x, drawing.get(0).y + 70), 0, 0, width, 0);
        currentPoint = new Point(drawing.get(0).x, drawing.get(0).y + 70);
    }

    private static void drawPoints(ArrayList<Point> drawing) throws InterruptedException {
        // pwm.on(20); // pen down
        Thread.sleep(1000);
        for (int i = 1; i < drawing.size(); i++) {
            move(currentPoint, new Point(drawing.get(i).x, drawing.get(i).y + 70), 0, 0, width, 0);
            currentPoint = new Point(drawing.get(i).x, drawing.get(i).y + 70);
        }
        // pwm.on(5); // pen up
        Thread.sleep(1000);
    }

    static void move(Point currentPoint, Point newPoint, int leftMotorPositionX, int leftMotorPositionY, int rightMotorPositionX, int rightMotorPositionY) {
        enableMotors(true);
        double length = Point.distance(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y);

        for (int part = 0; part < length; part++) {
            double stepsLeft = calculateSteps(currentPoint, newPoint, leftMotorPositionX, leftMotorPositionY, part, length, true);
            double stepsRight = calculateSteps(currentPoint, newPoint, rightMotorPositionX, rightMotorPositionY, part, length, false);

            updateMotorSteps(stepsLeft, stepsRight);
        }
        enableMotors(false);
    }

    private static double calculateSteps(Point currentPoint, Point newPoint, int motorPositionX, int motorPositionY, int part, double length, boolean isLeft) {
        double steps = (radiusLengthForPoint(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, motorPositionX, motorPositionY, (part + 1) / length) - (isLeft ? currentLeftLength : currentRightLength));
        if (isLeft) currentLeftLength += (float) steps;
        else currentRightLength += (float) steps;
        return steps;
    }

    private static void updateMotorSteps(double stepsLeft, double stepsRight) {
        int directionLeft = stepsLeft < 0 ? -1 : 1;
        int directionRight = stepsRight < 0 ? -1 : 1;

        stepsLeft = Math.abs(stepsLeft);
        stepsRight = Math.abs(stepsRight);

        if (stepsLeft >= stepsRight) {
            executeSteps(directionLeft, stepsLeft, directionRight, stepsRight, true);
        } else {
            executeSteps(directionRight, stepsRight, directionLeft, stepsLeft, false);
        }
    }

    private static void executeSteps(int directionPrimary, double stepsPrimary, int directionSecondary, double stepsSecondary, boolean defaultDirection) {
        for (int i = 0; i < stepsPrimary; i++) {
            if (defaultDirection) {
                stepLeftTest(directionPrimary, 200, 1);
                stepRightTest(directionSecondary, 200, stepsSecondary / stepsPrimary);
            } else {
                stepRightTest(directionPrimary, 200, 1);
                stepLeftTest(directionSecondary, 200, stepsSecondary / stepsPrimary);
            }
        }
    }

    public static double radiusLengthForPoint(int x1, int y1, int x2, int y2, int motorPositionX, int motorPositionY, double t) {
        double dx = t * (x2 - x1) + x1 - motorPositionX;
        double dy = t * (y2 - y1) + y1 - motorPositionY;
        return Math.sqrt(dx * dx + dy * dy) * stepsPerMM;
    }

    public static void stepLeftTest(int direction, int delay, double scale) {
        adjustDirection(dir, direction);
        handleSteps(scale, delay, fullStepLeft, () -> fullStepLeft--, step);
    }

    public static void stepRightTest(int direction, int delay, double scale) {
        adjustDirection(dir2, direction);
        handleSteps(scale, delay, fullStepRight, () -> fullStepRight--, step2);
    }

    private static void adjustDirection(DigitalOutput directionPin, int direction) {
        if (direction == 1) directionPin.high();
        else directionPin.low();
    }

    private static void handleSteps(double scale, int delay, double fullStep, Runnable decrementStep, DigitalOutput stepperMotor) {
        fullStep += scale;
        if (fullStep >= 1) {
            stepMotor(delay, stepperMotor);
            decrementStep.run();
        }
    }

    private static void stepMotor(int delay, DigitalOutput stepperMotor) {
        stepperMotor.high();
        microPause(delay);
        stepperMotor.low();
        microPause(delay);
    }

    public static void microPause(long pauseInMicros) {
        long endTime = System.nanoTime() + pauseInMicros * 1000;
        while (System.nanoTime() < endTime);
    }

    private static void enableMotors(boolean enable) {
        if (enable) {
            enableMotor1.high();
            enableMotor2.high();
        } else {
            enableMotor1.low();
            enableMotor2.low();
        }
    }

    private static Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream in = Main.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                config.load(in);
            } else {
                System.out.println("config.properties not found on classpath. " +
                        "Using built-in defaults (width=540, height=780, stepsPerMM=25, BCM pins: left=4/18/24, right=12/19/13).");
            }
        } catch (IOException e) {
            System.err.println("Failed to load config.properties: " + e.getMessage() + ". Continuing with built-in defaults.");
        }
        return config;
    }
}
