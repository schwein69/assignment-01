package multithreaded;

import multithreaded.Barrier.Barrier;
import multithreaded.Model.Boid;
import multithreaded.Model.BoidsModel;

import java.util.List;

public class BoidWorker extends Thread {
    private final BoidsModel model;//Need for import variables
    private final List<Boid> boids;
    private final Barrier barrierVel;
    private final Barrier barrierPos;
    private final Barrier barrierSync;


    public BoidWorker(BoidsModel model, List<Boid> boids, Barrier barrierVel, Barrier barrierPos, Barrier barrierSync) {
        this.model = model;
        this.boids = boids;
        this.barrierVel = barrierVel;
        this.barrierPos = barrierPos;
        this.barrierSync = barrierSync;
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted()) {
                //System.out.println("Worker " + Thread.currentThread().getName() + " detected interrupt. Exiting...");
                return; // Exit thread properly
            }
            /*Wait everybody*/
            try {
                barrierSync.hitAndWaitAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            /*Update velocity first*/
            for (Boid boid : this.boids) {
                boid.updateVelocity(this.model);
            }
            /*Wait everybody*/
            try {
                barrierVel.hitAndWaitAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            /*Update position then*/
            for (Boid boid : this.boids) {
                boid.updatePos(this.model);
            }
            /*Wait everybody*/
            try {
                barrierPos.hitAndWaitAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
