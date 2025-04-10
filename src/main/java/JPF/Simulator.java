package JPF;

public interface Simulator {
    void runSimulation() throws InterruptedException;

    void startSimulator();

    void suspendSimulator();

    void resetSimulator();
}
