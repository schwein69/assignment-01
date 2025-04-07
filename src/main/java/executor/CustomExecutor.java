package executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import multithreaded.Model.*;

public class CustomExecutor {
    private final BoidsModel model;
    private List<Boid> boids;
    private ExecutorService executor;

    public CustomExecutor(List<Boid> boids, int nProc, BoidsModel model) {
        this.boids = boids;
        this.executor = Executors.newFixedThreadPool(nProc);
        this.model = model;
    }

    public List<Future<?>> computeVelocity() {
        List<Future<?>> velocityResults = new ArrayList<>();
        if (executor.isShutdown()) return null;
        for (Boid boid : boids) {
            try {
                Future<?> res = executor.submit(() -> boid.updateVelocity(this.model));
                velocityResults.add(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return velocityResults;
    }

    public List<Future<?>> computePosition() {
        List<Future<?>> positionResults = new ArrayList<>();
        if (executor.isShutdown()) return null;
        for (Boid boid : boids) {
            try {
                Future<?> res = executor.submit(() -> boid.updatePos(this.model));
                positionResults.add(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return positionResults;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

}