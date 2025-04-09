package JPF.multithreaded.Barrier;

import multithreaded.Barrier.Barrier;

public class BarrierImpl implements Barrier {

    private final int nTotal;
    private int nArrived = 0;
    private int generation = 0; /*Tracking if they are on same round generation*/

    public BarrierImpl(int nTotal) {
        this.nTotal = nTotal;
    }

    @Override
    public synchronized void hitAndWaitAll() throws InterruptedException {
        if (generation >= 9) {
            return;
        }
        int actualGeneration = this.generation;
        this.nArrived++;
        if (this.nArrived == this.nTotal) {
            this.nArrived = 0;
            this.generation++;
            notifyAll();
        } else {
            while (this.generation == actualGeneration) {
                try {
                    wait();
                    if (generation >= 9) {
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

    }
}
