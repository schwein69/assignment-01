package multithreaded;

import java.util.ArrayList;
import java.util.List;

public class BoidThread extends Thread {
    private final Boid boid;
    private final BoidsModel model;//Nees to import variables

    BoidThread(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    private List<Boid> getNearbyBoids(BoidsModel model) {
        var list = new ArrayList<Boid>();
        for (Boid other : model.getBoids()) {
            if (other != this.boid) {
                P2d otherPos = other.getPos();
                double distance = this.boid.getPos().distance(otherPos);
                if (distance < model.getPerceptionRadius()) {
                    list.add(other);
                }
            }
        }
        return list;
    }

    private V2d calculateAlignment(List<Boid> nearbyBoids) {
        double avgVx = 0;
        double avgVy = 0;
        if (nearbyBoids.size() > 0) {
            for (Boid other : nearbyBoids) {
                V2d otherVel = other.getVel();
                avgVx += otherVel.x();
                avgVy += otherVel.y();
            }
            avgVx /= nearbyBoids.size();
            avgVy /= nearbyBoids.size();
            return new V2d(avgVx - this.boid.getVel().x(), avgVy - this.boid.getVel().y()).getNormalized();
        } else {
            return new V2d(0, 0);
        }
    }

    private V2d calculateCohesion(List<Boid> nearbyBoids) {
        double centerX = 0;
        double centerY = 0;
        if (nearbyBoids.size() > 0) {
            for (Boid other : nearbyBoids) {
                P2d otherPos = other.getPos();
                centerX += otherPos.x();
                centerY += otherPos.y();
            }
            centerX /= nearbyBoids.size();
            centerY /= nearbyBoids.size();
            return new V2d(centerX - this.boid.getPos().x(), centerY - this.boid.getPos().y()).getNormalized();
        } else {
            return new V2d(0, 0);
        }
    }

    private V2d calculateSeparation(List<Boid> nearbyBoids, BoidsModel model) {
        double dx = 0;
        double dy = 0;
        int count = 0;
        for (Boid other : nearbyBoids) {
            P2d otherPos = other.getPos();
            double distance = this.boid.getPos().distance(otherPos);
            if (distance < model.getAvoidRadius()) {
                dx += this.boid.getPos().x() - otherPos.x();
                dy += this.boid.getPos().y() - otherPos.y();
                count++;
            }
        }
        if (count > 0) {
            dx /= count;
            dy /= count;
            return new V2d(dx, dy).getNormalized();
        } else {
            return new V2d(0, 0);
        }
    }

    public void update(BoidsModel model) {

        /* change velocity vector according to separation, alignment, cohesion */
        var pos = this.boid.getPos();
        var vel = this.boid.getVel();

        List<Boid> nearbyBoids = getNearbyBoids(model);

        V2d separation = calculateSeparation(nearbyBoids, model);
        V2d alignment = calculateAlignment(nearbyBoids);
        V2d cohesion = calculateCohesion(nearbyBoids);

        vel = vel.sum(alignment.mul(model.getAlignmentWeight()))
                .sum(separation.mul(model.getSeparationWeight()))
                .sum(cohesion.mul(model.getCohesionWeight()));

        /* Limit speed to MAX_SPEED */

        double speed = vel.abs();

        if (speed > model.getMaxSpeed()) {
            vel = vel.getNormalized().mul(model.getMaxSpeed());
        }
        this.boid.setVel(vel); //TODO Updated vel

        /* Update position */

        pos = pos.sum(vel);

        /* environment wrap-around */

        if (pos.x() < model.getMinX()) pos = pos.sum(new V2d(model.getWidth(), 0));
        if (pos.x() >= model.getMaxX()) pos = pos.sum(new V2d(-model.getWidth(), 0));
        if (pos.y() < model.getMinY()) pos = pos.sum(new V2d(0, model.getHeight()));
        if (pos.y() >= model.getMaxY()) pos = pos.sum(new V2d(0, -model.getHeight()));

        this.boid.setPos(pos);
    }

    @Override
    public void run() {
        update(model);
    }
}
