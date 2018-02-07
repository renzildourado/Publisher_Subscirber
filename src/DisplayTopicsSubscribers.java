import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This file is a part of the PubSubAgent project
 * Course: Distributed Systems
 *
 * @author Darryl Pinto (dp6417)
 * @author Ketan Joshi (ksj4205)
 * @author Renzil Dourado  (rd9012)
 */


/**
 * This class is used to display list of Topics
 * and Subscribers in Event Manager
 */
public class DisplayTopicsSubscribers implements Runnable {

    /**
     * show the list of subscriber for a specified topic
     *
     * @param topic Name of Topic to subscribe to
     */
    private void showSubscribers(Topic topic) {

        try {
            if (EventManager.topicSubscriber.get(topic.getName()).isEmpty()) {
                System.out.printf("No user is subscribed to %s\n", topic.getName());
                return;
            }
            ArrayList<String> subscribers = EventManager.topicSubscriber.get(topic.getName());
            StringBuilder sb = new StringBuilder("===============================================\n");
            sb.append(String.format("List of Subscribers for %s:\n%s\n", topic.getName(), subscribers.toString()));
            sb.append("===============================================");
            System.out.println(sb);
        } catch (NullPointerException e) {
            System.out.println("Invalid Key Entered: Check spelling or key not present");

        }
    }

    /**
     * show the list of topic
     *
     * @return Topics present or not
     */
    private boolean showTopics() {
        try {
            if (EventManager.topicSubscriber.keySet().isEmpty()) {
                System.out.println("NO TOPICS IN THE SYSTEM");
                return false;
            } else {
                StringBuilder sb = new StringBuilder("===============================================\n");
                sb.append("List of Topics:\n");
                int i = 0;
                for (String topic : EventManager.topicSubscriber.keySet()) {
                    sb.append(++i).append(". ").append(topic).append("\n");
                }
                sb.append("===============================================");
                System.out.println(sb);
                return true;
            }

        } catch (NullPointerException e) {
            System.out.println("Invalid Key Entered: Check spelling or key not present");
            return false;
        }

    }

    @Override
    public void run() {


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("****Press ls for list of Topics");
            try {
                String ls = br.readLine();
                if (ls.equalsIgnoreCase("ls")) {
                    boolean topicPresent = this.showTopics();
                    if (topicPresent) {
                        System.out.println("Enter name of the Topic:");
                        String topicName = br.readLine();
                        this.showSubscribers(new Topic(0, topicName));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
