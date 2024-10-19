package de.mwize;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    //left stepper motor
    private static final int PIN_ENABLE = 4;
    private static final int PIN_STEP = 18;
    private static final int PIN_DIR = 24;

    //right stepper motor
    private static final int PIN_ENABLE2 = 12;
    private static final int PIN_STEP2 = 19;
    private static final int PIN_DIR2 = 13;
    
    static int width = 540;
    static int height = 780;
    static Point startPoint = new Point(width / 2, 70);
    static Point currentPoint = startPoint;
    static String filePath = "/home/pi/Desktop/svg3.svg"; //path of the svg file

    static int stepsPerMM = 25; //The number of steps the stepper motor has to turn to move the timing belt by 1 mm


    static Context pi4j = Pi4J.newAutoContext();
    static DigitalOutput step = pi4j.digitalOutput().create(PIN_STEP);
    static DigitalOutput step2 = pi4j.digitalOutput().create(PIN_STEP2);
    static DigitalOutput enableMotor1 = pi4j.digitalOutput().create(PIN_ENABLE);
    static DigitalOutput enableMotor2 = pi4j.digitalOutput().create(PIN_ENABLE2);
    static DigitalOutput dir = pi4j.digitalOutput().create(PIN_DIR);
    static DigitalOutput dir2 = pi4j.digitalOutput().create(PIN_DIR2);

    static float currentLeftLength = (float) (Math.sqrt(Math.pow(startPoint.x, 2) + Math.pow(startPoint.y, 2)) * stepsPerMM);
    static float currentRightLength = (float) (Math.sqrt(Math.pow(width - startPoint.x, 2) + Math.pow(startPoint.y, 2)) * stepsPerMM);

    static double fullStepLeft = 0;
    static double fullStepRight = 0;

    public static void main(String[] args) throws InterruptedException, IOException {

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
}
