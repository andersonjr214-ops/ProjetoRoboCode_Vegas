package Vegas_Robo2.java;

import robocode.*;
import java.awt.Color;

public class Vegas_Robo2 extends AdvancedRobot {

    private double enemyEnergy = 100;
    private double moveDirection = 1;
    private boolean enemyDetected = false;

    public void run() {
        setColors(Color.RED, Color.BLACK, Color.DARK_GRAY);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            turnRadarRight(360); // radar SEMPRE procura
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        enemyDetected = true;

        double distance = e.getDistance();
        double enemyBearing = e.getBearing();
        double absBearing = getHeading() + enemyBearing;

        double changeInEnergy = enemyEnergy - e.getEnergy();
        enemyEnergy = e.getEnergy();

        if (changeInEnergy > 0 && distance < 400) {
            evasiveMove();
        } else {
            strafeMovement(e);
        }

        smartAim(e);

        double firePower = calcFirePower(distance);
        if (getGunHeat() == 0) {
            fire(firePower);
        }

        double radarTurn = normalizeBearing(absBearing - getRadarHeading());
        setTurnRadarRight(radarTurn * 2);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        enemyDetected = false; // <<< CORREÇÃO IMPORTANTE
    }

    private void strafeMovement(ScannedRobotEvent e) {
        double distance = e.getDistance();
        double angle = e.getBearing() + 90 - (25 * moveDirection);
        setTurnRight(angle);
        setAhead((distance / 5 + 100) * moveDirection);

        if (Math.random() < 0.1) {
            moveDirection = -moveDirection;
        }

        wallAvoid();
    }

    private void evasiveMove() {
        moveDirection = -moveDirection;
        setTurnRight(60 * moveDirection);
        setAhead(150 + Math.random() * 100);
    }

    private void smartAim(ScannedRobotEvent e) {
        double angle = getHeading() + e.getBearing() - getGunHeading();
        setTurnGunRight(normalizeBearing(angle));
    }

    private double calcFirePower(double distance) {
        if (distance < 150) return 3;
        if (distance < 300) return 2;
        return 1;
    }

    private void wallAvoid() {
        double m = 80;
        if (getX() < m || getY() < m || getX() > getBattleFieldWidth() - m || getY() > getBattleFieldHeight() - m) {
            setTurnRight(45);
            setAhead(100);
        }
    }

    private double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}
