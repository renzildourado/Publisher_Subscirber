import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This file is a part of the PubSubAgent project
 * Course: Distributed Systems
 *
 * @author Darryl Pinto (dp6417)
 * @author Ketan Joshi (ksj4205)
 * @author Renzil Dourado  (rd9012)
 */

/**
 * class PortThread is used to support multiple ports using Multi-Threading
 */
public class PortThread implements Runnable {

    ServerSocket mysocket;

    public PortThread(ServerSocket mysocket) {
        this.mysocket = mysocket;

    }

    /**
     * Run method of PortThread
     */
    @Override
    public void run() {

        listen();

    }

    /**
     * listen method is used to listen for clients on ports
     */
    private void listen() {
        ExecutorService threadPool = Executors.newFixedThreadPool(20);

        while (true) {
            try {
                Socket newClient = mysocket.accept();
                threadPool.execute(new ClientThread(newClient, mysocket));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
