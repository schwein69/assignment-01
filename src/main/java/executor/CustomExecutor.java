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

    /* public void resetExecutor() {
         // Shutdown the old executor in the background
         new Thread(() -> {
             this.executor.shutdownNow(); // Interrupt running tasks
             try {
                 if (!this.executor.awaitTermination(1, TimeUnit.SECONDS)) {
                     System.err.println("Old executor did not terminate cleanly");
                 }
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
         }).start();
     }*/
    public void shutdown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

}