package executor;

import multithreaded.Model.*;
import multithreaded.Simulator;
import multithreaded.View.*;

import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorBoidsSimulator implements Simulator {
    private final BoidsModel model;
    private Optional<BoidsView> view;
    private final Lock lock;
    private final Condition restartCondition;
    private CustomExecutor exec;

    private static final int FRAMERATE = 50;
    private int framerate;
    private boolean running = false;
    private boolean resetting = false;


    public ExecutorBoidsSimulator(BoidsModel model) {
        this.lock = new ReentrantLock();
        this.model = model;
        this.restartCondition = lock.newCondition();
        this.view = Optional.empty();
        this.exec = new CustomExecutor(this.model.getBoids(), this.model.getProc(), this.model);
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
            restartCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void resetSimulator() {
        try {
            lock.lock();
            this.running = false;
            this.resetting = true;
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
                        if (resetting) {
                            this.exec = new CustomExecutor(this.model.getBoids(), this.model.getProc(), this.model);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                this.lock.unlock();
            }

            var t0 = System.currentTimeMillis();

            exec.computeVelocity().forEach(f -> {
                try {
                    f.get(); // wait all updates done
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            exec.computePosition().forEach(f -> {
                try {
                    f.get(); // wait all updates done
                } catch (Exception e) {
                    e.printStackTrace();
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





