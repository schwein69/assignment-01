package executor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoidsSimulation {

    final static int N_BOIDS = 1500;

    final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1000;
    final static int ENVIRONMENT_HEIGHT = 1000;
    static final double MAX_SPEED = 4.0;
    static final double PERCEPTION_RADIUS = 50.0;
    static final double AVOID_RADIUS = 20.0;

    final static int SCREEN_WIDTH = 800;
    final static int SCREEN_HEIGHT = 800;
    private static Lock lock;
    private static Condition completed;
    private static Condition restartCondition;
    private final static int nProcessors = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws InterruptedException {
        lock = new ReentrantLock();
        completed = lock.newCondition();
        restartCondition = lock.newCondition();
        System.out.println("numero thread " + nProcessors);
        var model = new BoidsModel(
                N_BOIDS,
                SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED,
                PERCEPTION_RADIUS,
                AVOID_RADIUS, nProcessors);
        var sim = new BoidsSimulator(model, lock, completed, restartCondition);
        var view = new BoidsView(model, SCREEN_WIDTH, SCREEN_HEIGHT, sim);

        sim.attachView(view);
        //sim.runSimulation();
    }
}
