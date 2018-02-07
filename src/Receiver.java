import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * This file is a part of the PubSubAgent project
 * Course: Distributed Systems
 *
 * @author Darryl Pinto (dp6417)
 * @author Ketan Joshi (ksj4205)
 * @author Renzil Dourado  (rd9012)
 */

public class Receiver implements Runnable {
    private ObjectInputStream input;

    public Receiver(ObjectInputStream input) {
        this.input = input;
    }


    /**
     * This is the start point for the thread. This thread is
     * responsible for listening continuously on the client side
     * and receiving any information provided by the EventManager
     */
    @Override
    public void run() {

        while (true) {
            try {
                String choice = this.input.readUTF();

                //This switch case tells the client what it is going to receive and
                //calls the appropriate function
                switch (choice) {
                    case "Topic":
                        this.receiveAdvertisements();
                        break;
                    case "Event":
                        this.receiveEvents();
                        break;
                    case "getAllTopics":
                        this.getAllTopics();
                        break;
                    case "getSubscribedTopics":
                        this.receiveSubscribedTopics();
                        break;
                    case "getAllKeywords":
                        this.getAllKeywords();
                        break;
                    default:
                        System.out.println("Error");
                        break;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * This method receives all the topics the user is subscribed
     * to from the EventManager and displays it
     */
    private void receiveSubscribedTopics() {

        try {
            Object obj = this.input.readObject();
            ArrayList<String> subscribedTopics;
            subscribedTopics = new ArrayList<>((ArrayList<String>) obj);

            StringBuilder sb = new StringBuilder("====================== Subscribed Topics =========================\n");

            for (String topicNames : subscribedTopics) {
                sb.append(topicNames).append("\n");
            }

            sb.append("================================================================\n");

            System.out.println(sb);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method receives all keywords from the EventManager
     * and displays it
     */
    private void getAllKeywords() {
        synchronized (input) {
            //receive arraylistt of strings
            try {
                Object obj = input.readObject();
                ArrayList<String> allKeywords = (ArrayList<String>) obj;

                StringBuilder keyword_string = new StringBuilder("======================All KEYWORDS=========================\n");
                int i = 0;
                for (String keyword : allKeywords) {
                    keyword_string.append("").append(++i).append(". ").append(keyword).append("\n");
                }
                keyword_string.append("====================================================================\n");
                System.out.println(keyword_string);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            input.notify();
        }

    }


    /**
     * This method receives the names of all the topics
     * advertised ever from the EventManager and displays it
     */
    private void getAllTopics() {

        synchronized (input) {
            //receive arraylistt of strings
            try {
                Object obj = input.readObject();
                ArrayList<String> allTopics = (ArrayList<String>) obj;

                StringBuilder topics_string = new StringBuilder("======================All Topics=========================\n");
                int i = 0;
                for (String topic : allTopics) {
                    topics_string.append("").append(++i).append(". ").append(topic).append("\n");
                }
                topics_string.append("====================================================================\n");
                System.out.println(topics_string);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            input.notify();
        }

    }


    /**
     * This method receives any new Advertisement advertised by any user
     * and sent to everyone by the EventManager
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void receiveAdvertisements() throws IOException, ClassNotFoundException {

        //Implement receiving advertisements here
        Object obj = this.input.readObject();
        Topic topic = (Topic) obj;
        System.out.println("===============================================\n" +
                "**New Advertisement Received :\nName:" + topic.getName() + "\nKeywords: " + topic.getKeywords() +
                "\n===============================================");

    }

    /**
     * This method receives any new Events published by any user
     * and sent to everyone by the EventManager
     */
    private void receiveEvents() {

        try {
            Object obj = this.input.readObject();
            Event event = (Event) obj;

            System.out.println("===============================================\n" +
                    "**New Event Received :\n" + "Title: " + event.getTitle() + "\nContent: " + event.getContent() +
                    "\n===============================================");


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}
