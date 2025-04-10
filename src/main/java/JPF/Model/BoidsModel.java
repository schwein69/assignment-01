package JPF.Model;

import java.util.ArrayList;
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


    public BoidsModel(BoidsModel other) {
        this.boids = new ArrayList<>();
        for (Boid b : other.boids) {
            this.boids.add(new Boid(b.getPos(), b.getVel()));
        }

        this.separationWeight = other.separationWeight;
        this.alignmentWeight = other.alignmentWeight;
        this.cohesionWeight = other.cohesionWeight;
        this.width = other.width;
        this.height = other.height;
        this.maxSpeed = other.maxSpeed;
        this.perceptionRadius = other.perceptionRadius;
        this.avoidRadius = other.avoidRadius;
        this.nBoids = other.nBoids;
        this.nProc = other.nProc;
    }

    private void initialize() {
        this.boids = new ArrayList<>();
        for (int i = 0; i < this.nBoids; i++) {
            P2d pos = new P2d(-width / 2 + Math.random() * width, -height / 2 + Math.random() * height);
            V2d vel = new V2d(Math.random() * maxSpeed / 2 - maxSpeed / 4, Math.random() * maxSpeed / 2 - maxSpeed / 4);
            this.boids.add(new Boid(pos, vel));
        }
    }

    public List<Boid> getBoids() {
        return new ArrayList<>(this.boids);
    }

    public double getMinX() {
        return -width / 2;
    }

    public double getMaxX() {
        return width / 2;
    }

    public double getMinY() {
        return -height / 2;
    }

    public double getMaxY() {
        return height / 2;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
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

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAvoidRadius() {
        return avoidRadius;
    }

    public double getPerceptionRadius() {
        return perceptionRadius;
    }

    public synchronized void reset() {
        initialize();
    }

    public int getProc() {
        return nProc;
    }
}

