package multithreaded;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 25;
    private int framerate;
    private volatile boolean running = false;
    private volatile boolean suspended = false;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public synchronized void suspendRunning() {
        this.suspended = true;
    }

    public synchronized void reset() {
        this.suspended = false;
        this.running = false;
       /* if (simulationThread != null) {
            simulationThread.interrupt();
        }*/
    }

    public synchronized void runSimulation() {
        if (this.running) return;
        this.running = true;
        this.suspended = false;
        while (this.running) {
            var t0 = System.currentTimeMillis();
            var boids = model.getBoids();

            List<Thread> threads = new ArrayList<>();

            for (Boid boid : boids) {
                Thread thread = new BoidThread(boid, model);
                threads.add(thread);
                thread.start();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
    		/*
    		for (Boid boid : boids) {
                boid.update(model);
            }
            */

                /*
                 * Improved correctness: first update velocities...
                 */
                for (Boid boid : boids) {
                    boid.updateVelocity(model);
                }

                /*
                 * ..then update positions
                 */
                for (Boid boid : boids) {
                    boid.updatePos(model);
                }


                if (view.isPresent()) {
                    view.get().update(framerate);
                    var t1 = System.currentTimeMillis();
                    var dtElapsed = t1 - t0;
                    var framratePeriod = 1000 / FRAMERATE;

                    if (dtElapsed < framratePeriod) {
                        try {
                            Thread.sleep(framratePeriod - dtElapsed);
                        } catch (Exception ex) {
                        }
                        framerate = FRAMERATE;
                    } else {
                        framerate = (int) (1000 / dtElapsed);
                    }
                }

            }
        }
    }
