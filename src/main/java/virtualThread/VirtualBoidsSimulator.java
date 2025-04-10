package virtualThread;

import multithreaded.Model.Boid;
import multithreaded.Model.BoidsModel;
import multithreaded.Simulator;
import multithreaded.View.BoidsView;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VirtualBoidsSimulator implements Simulator {
    private final BoidsModel model;
    private Optional<BoidsView> view;
    private final Lock lock;
    private final Condition restartCondition;

    private static final int FRAMERATE = 50;
    private int framerate;
    private boolean running = false;


    public VirtualBoidsSimulator(BoidsModel model) {
        this.lock = new ReentrantLock();
        this.model = model;
        this.restartCondition = lock.newCondition();
        this.view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    @Override
    public void startSimulator() {
        try {
            lock.lock();
            this.running = true;
            restartCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void suspendSimulator() {
        try {
            lock.lock();
            this.running = !this.running;
            if (this.running) {
                restartCondition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void resetSimulator() {
        try {
            lock.lock();
            this.running = false;
            model.reset();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void runSimulation() throws InterruptedException {
        if (this.running) return;

        while (true) {
            try {
                this.lock.lock();
                while (!this.running) {
                    try {
                        System.out.println("Aspetto");
                        restartCondition.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                this.lock.unlock();
            }

            var t0 = System.currentTimeMillis();    
            var listVelocity = new ArrayList<Thread>();
            var listPosition = new ArrayList<Thread>();
            for (Boid boid : this.model.getBoids()) {
                Thread v = Thread.ofVirtual().unstarted(() -> {
                    try {
                        boid.updateVelocity(this.model);
                    } catch (Exception ex) {
                    }
                });
                v.start();
                listVelocity.add(v);
            }

            listVelocity.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            for (Boid boid : this.model.getBoids()) {
                Thread p = Thread.ofVirtual().unstarted(() -> {
                    try {
                        boid.updatePos(this.model);
                    } catch (Exception ex) {
                    }
                });
                p.start();
                listPosition.add(p);
            }

            listPosition.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                System.out.println("Computation Time: " + (t1 - t0) + " ms");
                var dtElapsed = t1 - t0;
                var frameratePeriod = 1000 / FRAMERATE;

                if (dtElapsed < frameratePeriod) {
                    try {
                        Thread.sleep(frameratePeriod - dtElapsed);
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





