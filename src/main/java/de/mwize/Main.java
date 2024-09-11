package de.mwize;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Main {


    private static final int PIN_ENABLE2 = 12;

    private static final int PIN_STEP2 = 19;
    private static final int PIN_DIR2 = 13;

    private static final int PIN_DIR = 24;
    private static final int PIN_ENABLE = 4;

    private static final int PIN_STEP = 18;

    static int width = 540;

    static Point startPoint = new Point(width/2, 70);





    static Point currentPoint = startPoint;

    static String filePath = "/home/pi/Desktop/svg3.svg";

    static Context pi4j = Pi4J.newAutoContext();
    static DigitalOutput step = pi4j.digitalOutput().create(PIN_STEP);


    static DigitalOutput enableMotor1 = pi4j.digitalOutput().create(PIN_ENABLE);

    static DigitalOutput enableMotor2 = pi4j.digitalOutput().create(PIN_ENABLE2);
    static DigitalOutput step2 = pi4j.digitalOutput().create(PIN_STEP2);

    static DigitalOutput dir = pi4j.digitalOutput().create(PIN_DIR);
    static DigitalOutput dir2 = pi4j.digitalOutput().create(PIN_DIR2);

    static float currentLeftLength = (float) ((Math.sqrt(Math.pow(startPoint.x, 2) + Math.pow(startPoint.y, 2)))*25);
    static float currentRightLength = (float) (Math.sqrt((Math.pow(width-startPoint.x,2) +     Math.pow(startPoint.y, 2)))*25);

    int mmPerSecond = 100;
    public static double fullStepLeft = 0;
    public static double fullStepRight = 0;

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println(radiusLengthForPoint(startPoint.x, startPoint.y, 100 , 100, 0, 0, 0) +" "+ startPoint);
        System.out.println(Point.distance(startPoint.x, startPoint.y, 100 , 100));
        dir.low();
        dir2.low();
//        var pi4jServo = Pi4J.newAutoContext();
//        Pwm pwm = pi4jServo.create(Pwm.newConfigBuilder(pi4jServo)
//                .address(1)
//                .pwmType(PwmType.HARDWARE)
//                .provider("linuxfs-pwm")
//                .initial(0)
//                .shutdown(0)
//                .build());

        ArrayList<ArrayList<Point>> drawings = SVGParser.parseSVG(filePath);
//        move(currentPoint, new Point(100, 100), 0, 0, width,  0);
//        currentPoint = new Point(100, 100);
//        move(new Point(100, 100), new Point(400, 100), 0, 0, width,  0);
//        currentPoint = new Point(400, 100);
//        move(new Point(400, 100), new Point(400, 100), 0, 0, width,  0);
//        currentPoint = new Point(400, 100);
//        move(new Point(400, 100), new Point(400, 500), 0, 0, width,  0);
//        currentPoint = new Point(400, 500);
//        move(new Point(400, 500), new Point(100, 500), 0, 0, width,  0);
//        currentPoint = new Point(100, 500);
//        move(new Point(100, 500), new Point(100, 100), 0, 0, width,  0);
//        currentPoint = new Point(100, 100);
//        move(new Point(100, 100), startPoint, 0, 0, width,  0);
//        currentPoint = startPoint;
        for (ArrayList<Point> drawing : drawings) {
            for (Point point : drawing) {
                System.out.println(point.x + " " + point.y);
            }
        }

        move(currentPoint, new Point(drawings.get(0).get(0).x, drawings.get(0).get(0).y + 70), 0, 0, width,  0);
        currentPoint = new Point(drawings.get(0).get(0).x, drawings.get(0).get(0).y + 70);
        for (ArrayList<Point> drawing : drawings) {
//            pwm.on(20); //herunterfahren des Stiftes
            Thread.sleep(1000);
            for (int i = 1; i < drawing.size(); i++) {

                move(currentPoint, new Point(drawing.get(i).x, drawing.get(i).y+70), 0, 0, width,  0);
                currentPoint = new Point(drawing.get(i).x, drawing.get(i).y+70);

            }
//            pwm.on(5); //Absetzen des Stiftes
            Thread.sleep(1000);


        }
        move(currentPoint, startPoint, 0, 0, width,  0);
        currentPoint = startPoint;
    }



    public static void move(Point currentPoint, Point newPoint, int leftMotorPositionX, int leftMotorPositionY, int rightMotorPositionX, int rightMotorPositionY) {
        enableMotor1.high();
        enableMotor2.high();

//        for (int i = 0; i < 250; i++) {
//            stepLeftTest(1, 200, 1);
//        }
//        for (int i = 0; i < 250; i++) {
//            stepRightTest(1, 200, 1);
//        }
//        enableMotor1.low();
//        enableMotor2.low();
//        System.exit(0);


        double length = (int) Point.distance(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y);
        int timeForFullLength = (int) (length*(10000)); //Time in microseconds for the full move (1 second per 10cm | 10000 microseconds per mm)

        for (int part = 0; part < length; part++) {
//            double delayLeftMotor = calculateAvgDelayBetweenPoints(currentPoint, newPoint, leftMotorPositionX, leftMotorPositionY, timeForFullLength, part/length, (part+1)/length);
//            double delayRightMotor = calculateAvgDelayBetweenPoints(currentPoint, newPoint, rightMotorPositionX, rightMotorPositionY, timeForFullLength, part/length, (part+1)/length);
            double stepsLeft = (radiusLengthForPoint(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, leftMotorPositionX, leftMotorPositionY, (part+1)/length) - currentLeftLength);
            double stepsRight = (radiusLengthForPoint(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, rightMotorPositionX, rightMotorPositionY, (part+1)/length) - currentRightLength);
            currentLeftLength += (float) stepsLeft;
            currentRightLength += (float) stepsRight;
            System.out.println(stepsLeft + " " +stepsRight );

            int directionLeft = 1;
            int directionRight = 1;
            if (Math.abs(stepsLeft) >= Math.abs(stepsRight))    {
                if (stepsLeft < 0)  directionLeft = -1;
                if (stepsRight < 0)  directionRight = -1;
                stepsRight = Math.abs(stepsRight);
                stepsLeft = Math.abs(stepsLeft);
                for (int i = 0; i < stepsLeft; i++) {
                    stepLeftTest(directionLeft, 200, 1);
                    stepRightTest(directionRight, 200, stepsRight / stepsLeft);
                }

            }   else {
                if (stepsLeft < 0)  directionLeft = -1;
                stepsLeft = Math.abs(stepsLeft);
                if (stepsRight < 0)  directionRight = -1;
                stepsRight = Math.abs(stepsRight);
                for (int i = 0; i < stepsRight; i++) {
                    stepRightTest(directionRight, 200, 1);
                    stepLeftTest(directionLeft, 200, stepsLeft / stepsRight);
                    System.out.println("Yo " + stepsLeft / stepsRight + " " + directionLeft+" " + fullStepLeft);
                }

            }


//            stepLeft(stepsLeft, (int) delayLeftMotor);
//            stepRight(stepsRight, (int) delayRightMotor);




        }
        Main.currentPoint = newPoint;
        enableMotor1.low();
        enableMotor2.low();

    }
    public static double radiusLengthForPoint(int x1, int y1, int x2, int y2, int motorPositionX, int motorPositionY, double t)   {
        double v = t * (x2 - x1) + x1 - motorPositionX;
        double v2 = t * (y2 - y1) + y1 - motorPositionY;
        return Math.sqrt(v*v + v2*v2) * 25;
    }

    public static double delayPerStepInMicrosecondsPerStep(double x1, double y1, double x2, double y2, int motorPositionX, int motorPositionY, double timeForFullLength, double t)   {
//        return ((-motorPositionX+x)/Math.sqrt(Math.pow((y-motorPositionY), 2)+Math.pow((x-motorPositionX), 2)) * 25) * timeInMsPerPart;
        double xt = t * (x2 - x1) + x1 - motorPositionX;
        double yt = t * (y2 - y1) + y1 - motorPositionY;
        //return 1/(((((x2-x1))* v +(y2-y1)* v1)/Math.sqrt((v*v) - (v1*v1))/timeInMS)*25);
        System.out.println("time: " +(1/((Math.sqrt (xt*xt + yt*yt)/((x2-x1)*xt + (y2-y1)*yt))*25))/timeForFullLength);
        System.out.println("time2: " +timeForFullLength/(Math.sqrt(xt*xt+yt*yt)*((x2-x1)*xt+(y2-y1)*yt)*25));
        return (timeForFullLength/((Math.sqrt (xt*xt + yt*yt)/((x2-x1)*xt + (y2-y1)*yt))*25));
    }
    public static double calculateAvgDelayBetweenPoints(Point currentPoint, Point newPoint, int motorPositionX, int motorPositionY, double timeInMs, double t, double nextT) {
//        double delayInMicroseconds = (((((speedForPointInStepsInMs(x1, y1, motorPositionX, motorPositionY, timeInMsPerPart) + speedForPointInStepsInMs(x2, y2, motorPositionX, motorPositionY, timeInMsPerPart))/2))));
        //System.out.println(currentPoint + " "+t + " time " + delayPerStepInMicrosecondsPerStep(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, motorPositionX, motorPositionY, timeInMs, t));
        System.out.println("t: "+t+" nextT: "+nextT+" avgdelay: " + (delayPerStepInMicrosecondsPerStep(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, motorPositionX, motorPositionY, timeInMs, t) + delayPerStepInMicrosecondsPerStep(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, motorPositionX, motorPositionY, timeInMs, nextT))/2 + "r: " + radiusLengthForPoint(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, motorPositionX, motorPositionY, t));
        return (delayPerStepInMicrosecondsPerStep(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, motorPositionX, motorPositionY, timeInMs, t) + delayPerStepInMicrosecondsPerStep(currentPoint.x, currentPoint.y, newPoint.x, newPoint.y, motorPositionX, motorPositionY, timeInMs, nextT))/2;
    }

    public static void stepLeft(double steps, int delay)  {
        float direction = 1;
        if (steps < 0)  {
            direction = -1;
            steps = Math.abs(steps);
            Main.dir.high();
        }
        delay = Math.abs(delay);

        for (int i = 0; i < steps; i++) {
            Main.step.high();
            Main.microPause(delay/2);

            Main.step.low();
            Main.microPause(delay/2);
        }
        if (direction == -1) {
            Main.dir.low();
        }
    }

    public static void stepLeftTest(int direction, int delay, double scale)  {
        System.out.println("scale: "+scale + " " + fullStepLeft);
        if (direction == 1)  {
            Main.dir.high();
        }
        delay = Math.abs(delay);
        fullStepLeft += scale;
        if (fullStepLeft >= 1) {
            System.out.println("Stepped Left");
            Main.step.high();
            Main.microPause(delay);

            Main.step.low();
            Main.microPause(delay);
            fullStepLeft -= 1;

        }
        if (direction == 1) {
            Main.dir.low();
        }
    }
    public static void stepRightTest(int direction, int delay, double scale)  {


        if (direction == -1)  {
            Main.microPause(200);
            Main.dir2.high();
        }
        delay = Math.abs(delay);

        fullStepRight += scale;
        if (fullStepRight >= 1) {
            Main.step2.high();
            Main.microPause(delay);

            Main.step2.low();
            Main.microPause(delay);
            fullStepRight -= 1;

        }

        Main.dir2.low();
    }

    public static void stepRight(double steps, int delay)  {
        Main.dir2.high();

        if (steps < 0)  {
            Main.microPause(200);
            Main.dir2.low();
            steps = Math.abs(steps);
        }
        delay = Math.abs(delay);
        for (int i = 0; i < steps; i++) {
            Main.step2.high();
            Main.microPause(delay/2);

            Main.step2.low();
            Main.microPause(delay/2);
        }

        Main.dir2.low();
    }

    public static void microPause(long pauseInMicros) {
        long toTime = System.nanoTime()+pauseInMicros*1000;
        while(toTime>System.nanoTime());
    }

    //        ArrayList<Point> points2 = new ArrayList<>();
//        for (double t = 0; t < 1; t += 1.0 / Math.round(Point.distance(currentPoint.x, currentPoint.y, startPoint.x, startPoint.y))) {
//            int x = (int) Math.round(currentPoint.x + (startPoint.x - currentPoint.x) * t);
//            int y = (int) Math.round(currentPoint.y + (startPoint.y - currentPoint.y) * t);
//            points2.add(new Point(x, y));
//        }
//
//        for (Point point : points2) {
//            steps1 = Math.round((Math.sqrt(Math.pow(point.y, 2) + Math.pow(point.x, 2)) -      Math.sqrt(Math.pow(currentPoint.x, 2) + Math.pow(currentPoint.y, 2))) * 25);
//            steps2 = Math.round((Math.sqrt(Math.pow(point.y, 2) + Math.pow(width-point.x,2)) - Math.sqrt(Math.pow(width-currentPoint.x, 2) + Math.pow(currentPoint.y, 2))) * 25);
//            currentPoint = point;
//            System.out.println(steps1 + " " +steps2);
//            StepThread stepThread = new StepThread(step, enableMotor1, dir, 5, false, (int) steps1);
//            StepThread stepThread2 = new StepThread(step2, enableMotor2, dir2, 5, true, (int) steps2);
//            stepThread.start();
//            stepThread2.start();
//            if (steps1 > steps2) {
//                Thread.sleep((long) (Math.abs(steps1)*3));
//            }   else {
//                Thread.sleep((long) (Math.abs(steps2)*3));
//            }
//        }



}

