package multithreaded;

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
    private final Condition notCompleted;
    private final List<BoidWorker> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private volatile boolean running = false;
    private volatile boolean suspended = false;


    public BoidsSimulator(BoidsModel model, Lock lock, Condition notCompleted) {
        this.completedWorkers = new AtomicInteger(0);
        this.model = model;
        this.notCompleted = notCompleted;
        this.view = Optional.empty();
        this.lock = lock;

        var nBoids = model.getBoids().size();
        var nProc = model.getProc();
        int batchSize = nBoids / nProc;
        for (int i = 0; i < nProc; i++) {
            int startIdx = i * batchSize;
            int endIdx = (i == nProc - 1) ? nBoids : (i + 1) * batchSize;
            System.out.println("Faccio partire thread : " + i + " da " + startIdx + " a " + endIdx);
            BoidWorker worker = new BoidWorker(model, startIdx, endIdx, lock, notCompleted, completedWorkers);
            this.workers.add(worker);
        }

    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public synchronized void suspendSimulation() {
        this.suspended = !this.suspended;
    }

    public synchronized void resetSimulation() {
        this.running = false;
        this.completedWorkers = new AtomicInteger(0);
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().contains("BoidThread") || thread.getName().contains("Thread")) {
                thread.interrupt();
            }
        }
        model.reset();
    }

    public synchronized void runSimulation() throws InterruptedException {
        if (this.running) return;
        this.running = true;
        this.suspended = false;


        new Thread(() -> {
            try {
                while (this.running) {
                    if (!this.suspended) {
                        var t0 = System.currentTimeMillis();

                        for (BoidWorker worker : workers) {
                            new Thread(worker).start();
                        }

                        this.lock.lock();
                        try {
                            System.out.println("waiting");
                            this.notCompleted.await();
                            System.out.println("Ricevuto segnale");
                            //Thread.sleep(5000);
                        } finally {
                            this.lock.unlock();
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }).start();


    }
}
