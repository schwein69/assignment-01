package executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class BoidsSimulator {
    protected AtomicInteger completedWorkers;
    private BoidsModel model;
    private Optional<BoidsView> view;
    private final Lock lock;
    private final Condition Completed;
    private final Condition restartCondition;
    private List<BoidWorker> workers;

    private static final int FRAMERATE = 50;
    private int framerate;
    private volatile boolean running = false;
    private volatile boolean suspended = false;
    private volatile boolean restart;


    public BoidsSimulator(BoidsModel model, Lock lock, Condition completed, Condition restartCondition) {
        this.restartCondition = restartCondition;
        this.completedWorkers = new AtomicInteger(0);
        this.model = model;
        this.Completed = completed;
        this.view = Optional.empty();
        this.lock = lock;

    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void suspendSimulation() {
        this.suspended = !this.suspended;
        if (!this.suspended) { // If resuming, signal all waiting threads
            lock.lock();
            try {
                restartCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    public void resetSimulation() {
        this.running = false;
        this.completedWorkers = new AtomicInteger(0);
        for (BoidWorker worker : this.workers) {
            worker.interrupt();
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.workers.clear();
        model.reset();
    }

    public void runSimulation() throws InterruptedException {
        if (this.running) return;
        this.running = true;
        this.suspended = false;
        this.workers = new ArrayList<>();
        this.restart = true;

        var nBoids = model.getBoids().size();
        var nProc = model.getProc();
        int batchSize = nBoids / nProc;
        for (int i = 0; i < nProc; i++) {
            int startIdx = i * batchSize;
            int endIdx = (i == nProc - 1) ? nBoids : (i + 1) * batchSize;
            System.out.println("Faccio partire thread : " + i + " da " + startIdx + " a " + endIdx);
            BoidWorker worker = new BoidWorker(model, startIdx, endIdx, lock, Completed, restartCondition, restart, completedWorkers);
            this.workers.add(worker);
            worker.start();
        }
        while (this.running) {
            var t0 = System.currentTimeMillis();
            this.lock.lock();
            try {
                while (this.suspended) { // Pause loop if simulation is suspended
                    restartCondition.await();
                }
                System.out.println("Waiting for workers...");
                this.Completed.await();
                System.out.println("Received signal from workers");


                if (view.isPresent()) {
                    view.get().update(framerate);
                    var t1 = System.currentTimeMillis();
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
                //Thread.sleep(5000);
                this.restartCondition.signalAll();

            } finally {
                lock.unlock();
            }
        }

    }

}

