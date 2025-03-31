package multithreaded;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class BoidWorker implements Runnable {
    private final BoidsModel model;//Need for import variables
    private final int startIdx;
    private final int endIdx;
    private final Condition notCompleted;
    private final Lock lock;
    private AtomicInteger counter;

    public BoidWorker(BoidsModel model, int startIdx, int endIdx, Lock lock, Condition notCompleted, AtomicInteger counter) {
        this.model = model;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.notCompleted = notCompleted;
        this.lock = lock;
        this.counter = counter;
    }

    private List<Boid> getNearbyBoids(Boid boid) {
        var list = new ArrayList<Boid>();
        for (Boid other : model.getBoids()) {
            if (other != boid) {
                P2d otherPos = other.getPos();
                double distance = boid.getPos().distance(otherPos);
                if (distance < model.getPerceptionRadius()) {
                    list.add(other);
                }
            }
        }
        return list;
    }

    private V2d calculateAlignment(List<Boid> nearbyBoids, Boid boid) {
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
            return new V2d(avgVx - boid.getVel().x(), avgVy - boid.getVel().y()).getNormalized();
        } else {
            return new V2d(0, 0);
        }
    }

    private V2d calculateCohesion(List<Boid> nearbyBoids, Boid boid) {
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
            return new V2d(centerX - boid.getPos().x(), centerY - boid.getPos().y()).getNormalized();
        } else {
            return new V2d(0, 0);
        }
    }

    private V2d calculateSeparation(List<Boid> nearbyBoids, BoidsModel model, Boid boid) {
        double dx = 0;
        double dy = 0;
        int count = 0;
        for (Boid other : nearbyBoids) {
            P2d otherPos = other.getPos();
            double distance = boid.getPos().distance(otherPos);
            if (distance < model.getAvoidRadius()) {
                dx += boid.getPos().x() - otherPos.x();
                dy += boid.getPos().y() - otherPos.y();
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


        for (Boid boid : model.getSublist(this.startIdx, this.endIdx)) {
            /* change velocity vector according to separation, alignment, cohesion */
            var pos = boid.getPos();
            var vel = boid.getVel();

            List<Boid> nearbyBoids = getNearbyBoids(boid);

            V2d separation = calculateSeparation(nearbyBoids, model, boid);
            V2d alignment = calculateAlignment(nearbyBoids, boid);
            V2d cohesion = calculateCohesion(nearbyBoids, boid);

            vel = vel.sum(alignment.mul(model.getAlignmentWeight()))
                    .sum(separation.mul(model.getSeparationWeight()))
                    .sum(cohesion.mul(model.getCohesionWeight()));

            /* Limit speed to MAX_SPEED */

            double speed = vel.abs();

            if (speed > model.getMaxSpeed()) {
                vel = vel.getNormalized().mul(model.getMaxSpeed());
            }
            boid.setVel(vel);

            /* Update position */

            pos = pos.sum(vel);

            /* environment wrap-around */

            if (pos.x() < model.getMinX()) pos = pos.sum(new V2d(model.getWidth(), 0));
            if (pos.x() >= model.getMaxX()) pos = pos.sum(new V2d(-model.getWidth(), 0));
            if (pos.y() < model.getMinY()) pos = pos.sum(new V2d(0, model.getHeight()));
            if (pos.y() >= model.getMaxY()) pos = pos.sum(new V2d(0, -model.getHeight()));

            boid.setPos(pos);
        }
        System.out.println("Ho finito:da " + this.startIdx + " a " + this.endIdx);
        this.lock.lock();
        try {
            if (this.counter.incrementAndGet() == model.getProc()) {  // Last worker signals
                this.counter.setRelease(0);
                notCompleted.signal();
                System.out.println("SEGNAL SENT");
            }else{
                System.out.println("im waiting");
            }
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void run() {
        update(model);
    }
}
