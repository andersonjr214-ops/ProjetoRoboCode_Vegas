package Vegas_Robo1.java;

import robocode.*;
import java.awt.*;

/**
 * Radar travado, movement anti-travamento, metralhadora e wall-avoid.
 */
public class Vegas_Robo1 extends AdvancedRobot {

    private double enemyBearing;
    private double enemyDistance;
    private double enemyHeading;
    private double enemyVelocity;
    private boolean enemyDetected = false;

    public void run() {
        setColors(Color.BLACK, new Color(150, 0, 120), Color.RED); // Corpo, arma (roxo), radar (vermelho)

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Loop principal — radar sempre procurando
        while (true) {
            if (!enemyDetected) {
                // varredura ampla quando sem alvo
                setTurnRadarRight(360);
            } else {
                // pequeno giro contínuo para manter o radar responsivo
                setTurnRadarRight(20);
            }
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        enemyDetected = true;

        enemyBearing = e.getBearing();
        enemyDistance = e.getDistance();
        enemyHeading = e.getHeading();
        enemyVelocity = e.getVelocity();

        // ***** RADAR TRAVADO (lock) *****
        double absoluteBearing = getHeading() + enemyBearing;
        double radarTurn = normalize(absoluteBearing - getRadarHeading());
        setTurnRadarRight(radarTurn * 2); // força lock

        // ***** MOVIMENTO: distância e evasão *****
        if (enemyDistance > 220) {
            setAhead(120); // pressiona avançando
        } else if (enemyDistance < 140) {
            setBack(80); // recua se muito perto
        } else {
            // circula lateralmente quando na faixa média
            setTurnRight(90 - (enemyDistance / 20.0));
            setAhead(80);
        }

        // evitar paredes sem travar o fluxo
        wallAvoid();

        // ***** MIRA (lead simplificado) *****
        // tentativa simples de lead: compensa gunTurn por velocidade inimiga
        double leadComp = enemyVelocity / 3.0;
        double gunTurn = getHeading() + enemyBearing - getGunHeading() + leadComp;
        setTurnGunRight(normalize(gunTurn));

        // ***** METRALHADORA (dispara frequentemente) *****
        if (getGunHeat() == 0 && getEnergy() > 0.2) {
            // firePower escalonado por distância (mais forte de perto)
            double firePower = Math.min(3.0, Math.max(0.6, 400.0 / Math.max(100.0, enemyDistance)));
            setFire(firePower);
        }

        execute();
    }

    // evita travar quando inimigo morre
    public void onRobotDeath(RobotDeathEvent e) {
        enemyDetected = false;
    }

    public void onHitByBullet(HitByBulletEvent e) {
        // reação rápida: pequena manobra lateral
        setTurnRight(30);
        setAhead(80);
    }

    public void onHitWall(HitWallEvent e) {
        // escape da parede
        setBack(100);
        setTurnRight(60);
        setAhead(80);
    }

    public void onHitRobot(HitRobotEvent e) {
        // afasta e reposiciona
        setBack(60);
        setTurnRight(45);
        setAhead(60);
    }

    // wall avoidance suave
    private void wallAvoid() {
        double margin = 70;
        double x = getX(), y = getY(), w = getBattleFieldWidth(), h = getBattleFieldHeight();
        if (x < margin || x > w - margin || y < margin || y > h - margin) {
            setTurnRight(45);
            setAhead(100);
        }
    }

    // Força de tiro básica (mantida por compatibilidade)
    private double calcFirePower(double distance) {
        if (distance < 150) return 3;
        if (distance < 300) return 2;
        return 1;
    }

    // Normaliza ângulo para -180..180
    private double normalize(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}
