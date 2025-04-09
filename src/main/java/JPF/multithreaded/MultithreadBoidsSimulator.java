package JPF.multithreaded;

import multithreaded.Barrier.Barrier;
import multithreaded.Barrier.BarrierImpl;
import multithreaded.BoidWorker;
import multithreaded.Model.Boid;
import multithreaded.Model.BoidsModel;
import multithreaded.Simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultithreadBoidsSimulator implements Simulator {
    private Barrier barrierVel, barrierPos, barrierSync;
    private final BoidsModel model;
    //private Optional<BoidsView> view;
    private final Lock lock;
    private final Condition restartCondition;
    private List<BoidWorker> workers;


    /* private static final int FRAMERATE = 50;
     private int framerate;*/
    private boolean running = false;
    private boolean resetting = false;


    public MultithreadBoidsSimulator(BoidsModel model) {
        this.lock = new ReentrantLock();
        this.model = model;
        this.restartCondition = lock.newCondition();
        //this.view = Optional.empty();
    }

    /*public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }*/

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
            this.resetting = true;
            for (BoidWorker worker : this.workers) {
                worker.interrupt();
            }

            for (BoidWorker worker : this.workers) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            this.workers.clear();
            model.reset();
        } finally {
            lock.unlock();
        }


    }

    private void initialization() {
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

    @Override
    public void runSimulation() throws InterruptedException {
        int iterations = 10;
        if (this.running) return;

        initialization();

        for (int i = 0; i < iterations; i++) {
            System.out.println("Iterazione n: " + i);
            /*try {
                this.lock.lock();
                while (!this.running) {
                    try {
                        restartCondition.await();
                        if (resetting) {
                            initialization();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                this.lock.unlock();
            }*/

            //var t0 = System.currentTimeMillis();

            try {
                barrierSync.hitAndWaitAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                barrierPos.hitAndWaitAll();
                System.out.println("Sono il main ho toccato io");
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var frameratePeriod = 1000 / FRAMERATE;
                System.out.println("Computation Time: " + (t1 - t0) + " ms");
                if (dtElapsed < frameratePeriod) {
                    try {
                        Thread.sleep(frameratePeriod - dtElapsed);
                    } catch (Exception ex) {
                    }
                    framerate = FRAMERATE;
                } else {
                    framerate = (int) (1000 / dtElapsed);
                }
            }*/
        }
        for (BoidWorker worker : this.workers) {
            worker.join();
        }


    }

}





