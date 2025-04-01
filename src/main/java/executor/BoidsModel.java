package executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoidsModel {

    private List<Boid> boids;
    private double separationWeight;
    private double alignmentWeight;
    private double cohesionWeight;
    private final double width;
    private final double height;
    private final double maxSpeed;
    private final double perceptionRadius;
    private final double avoidRadius;
    private final int nBoids;
    private final int nProc;

    public BoidsModel(int nboids,
                      double initialSeparationWeight,
                      double initialAlignmentWeight,
                      double initialCohesionWeight,
                      double width,
                      double height,
                      double maxSpeed,
                      double perceptionRadius,
                      double avoidRadius, int nProc) {
        separationWeight = initialSeparationWeight;
        alignmentWeight = initialAlignmentWeight;
        cohesionWeight = initialCohesionWeight;
        this.nBoids = nboids;
        this.width = width;
        this.height = height;
        this.maxSpeed = maxSpeed;
        this.perceptionRadius = perceptionRadius;
        this.avoidRadius = avoidRadius;
        this.nProc = nProc;
        initialize();

    }

    private void initialize() {
        this.boids = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < this.nBoids; i++) {
            P2d pos = new P2d(-width / 2 + Math.random() * width, -height / 2 + Math.random() * height);
            V2d vel = new V2d(Math.random() * maxSpeed / 2 - maxSpeed / 4, Math.random() * maxSpeed / 2 - maxSpeed / 4);
            this.boids.add(new Boid(pos, vel));
        }
    }

    public synchronized List<Boid> getSublist(int from, int to) {
        return this.boids.subList(from, to);
    }

    public synchronized List<Boid> getBoids() {
        return boids;
    }

    public synchronized double getMinX() {
        return -width / 2;
    }

    public synchronized double getMaxX() {
        return width / 2;
    }

    public synchronized double getMinY() {
        return -height / 2;
    }

    public synchronized double getMaxY() {
        return height / 2;
    }

    public synchronized double getWidth() {
        return width;
    }

    public synchronized double getHeight() {
        return height;
    }

    public synchronized void setSeparationWeight(double value) {
        this.separationWeight = value;
    }

    public synchronized void setAlignmentWeight(double value) {
        this.alignmentWeight = value;
    }

    public synchronized void setCohesionWeight(double value) {
        this.cohesionWeight = value;
    }

    public synchronized double getSeparationWeight() {
        return separationWeight;
    }

    public synchronized double getCohesionWeight() {
        return cohesionWeight;
    }

    public synchronized double getAlignmentWeight() {
        return alignmentWeight;
    }

    public synchronized double getMaxSpeed() {
        return maxSpeed;
    }

    public synchronized double getAvoidRadius() {
        return avoidRadius;
    }

    public synchronized double getPerceptionRadius() {
        return perceptionRadius;
    }

    public synchronized void reset() {
        initialize();
    }

    public int getProc() {
        return nProc;
    }
}

