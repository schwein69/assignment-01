package multithreaded;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoidsSimulator {
    private Barrier barrierVel, barrierPos, barrierSync;
    private final BoidsModel model;
    private Optional<BoidsView> view;
    private final Lock lock;
    private final Condition restartCondition;
    private List<BoidWorker> workers;


    private static final int FRAMERATE = 50;
    private int framerate;
    private boolean running = false;
    private boolean resetting = false;


    public BoidsSimulator(BoidsModel model) {
        this.lock = new ReentrantLock();
        this.model = model;
        this.restartCondition = lock.newCondition();
        this.view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void startSimulator() {
        try {
            lock.lock();
            this.running = true;
            restartCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void suspendSimulation() {
        try {
            lock.lock();
            this.running = !this.running;
            restartCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }


    public void resetSimulation() {
        try {
            lock.lock();
            this.running = false;
            this.resetting = true;
            for (BoidWorker worker : this.workers) {
                worker.interrupt(); // Interrupt worker threads
            }

            for (BoidWorker worker : this.workers) {
                try {
                    worker.join(); // Ensure they have stopped before proceeding
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve the interrupt status
                }
            }
            this.workers.clear();
            model.reset();
        } finally {
            lock.unlock();
        }


    }

    private void initializzation() {
        var nBoids = model.getBoids().size();
        var nProc = model.getProc();
        int batchSize = nBoids / nProc;
        var boids = model.getBoids();
        this.workers = new ArrayList<>();
        this.barrierPos = new BarrierImpl(nProc + 1);
        this.barrierSync = new BarrierImpl(nProc + 1);
        this.barrierVel = new BarrierImpl(nProc);

        for (int i = 0; i < nProc; i++) {
            int startIdx = i * batchSize;
            int endIdx = (i == nProc - 1) ? nBoids : (i + 1) * batchSize;
            List<Boid> subList = boids.subList(startIdx, endIdx);
            BoidWorker worker = new BoidWorker(model, subList, barrierVel, barrierPos, barrierSync);
            this.workers.add(worker);
            worker.start();
        }
    }

    public void runSimulation() throws InterruptedException {
        if (this.running) return;

        initializzation();

        while (true) {
            try {
                this.lock.lock();
                while (!this.running) {
                    try {
                        System.out.println("Aspetto");
                        restartCondition.await();
                        if (resetting) {
                            initializzation();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                this.lock.unlock();
            }

            var t0 = System.currentTimeMillis();

            try {
                barrierSync.hitAndWaitAll();
                barrierPos.hitAndWaitAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


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
        }
    }

}





