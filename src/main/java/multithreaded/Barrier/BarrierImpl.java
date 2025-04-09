package multithreaded.Barrier;

public class BarrierImpl implements Barrier {

    private final int nTotal;
    private int nArrived = 0;
    private int generation = 0; /*Tracking if they are on same round generation*/

    public BarrierImpl(int nTotal) {
        this.nTotal = nTotal;
    }

    @Override
    public synchronized void hitAndWaitAll() throws InterruptedException {
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
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

    }
}
