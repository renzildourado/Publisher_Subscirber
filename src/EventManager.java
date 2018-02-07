import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This file is a part of the PubSubAgent project
 * Course: Distributed Systems
 *
 * @author Darryl Pinto (dp6417)
 * @author Ketan Joshi (ksj4205)
 * @author Renzil Dourado  (rd9012)
 */

public class EventManager {

    //used for registration (subscriber - key ,Topic - value (InString))
    public static ConcurrentHashMap<String, ArrayList<String>> subscriberTopics = new ConcurrentHashMap<>();

    // used for publish(Events) 	(topics - key, List of subscribers - value)
    public static ConcurrentHashMap<String, ArrayList<String>> topicSubscriber = new ConcurrentHashMap<>();

    //used for advertising keyword , topic : possibility of overwriting
    public static ConcurrentHashMap<String, String> keywordTopic = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, List<String>> topicKeyword = new ConcurrentHashMap<>();

    //	Subscriber - key, ClientThread - value
    public static ConcurrentHashMap<String, ClientThread> subscriberThreadMap = new ConcurrentHashMap<>();

    // USerName - key, List of missed Topics - value
    public static ConcurrentHashMap<String, ArrayList<Topic>> offlineTopics = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, ArrayList<Event>> offlineEvents = new ConcurrentHashMap<>();

    private static ServerSocket[] serverSocks = new ServerSocket[10];

    /*
     * notify all subscribers of new event
     */
    public static void notifySubscribers(Event event) {

        String eventTopicName = event.getTopic().getName();

        if (EventManager.topicSubscriber.containsKey(eventTopicName)) {
            ArrayList<String> subscribers = EventManager.topicSubscriber.get(eventTopicName);

            for (String subscriber : subscribers) {
                ClientThread thread = EventManager.subscriberThreadMap.get(subscriber);

                try {
                    thread.sendEvent(event);
            } catch (SocketException e) {

                    System.out.println(thread.user_name + " is offline. Caching the Event:" + event.getTitle());
                    ArrayList<Event> eventList = new ArrayList<>();

                    if (EventManager.offlineEvents.containsKey(thread.user_name)) {
                        eventList = EventManager.offlineEvents.get(thread.user_name);
                    }

                    eventList.add(event);
                    EventManager.offlineEvents.put(thread.user_name, eventList);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * This is the main method. This method starts the execution of the
     * program and spawns a new thread which if required displays
     * all the subscribers of a topic
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        new Thread(new DisplayTopicsSubscribers()).start();
        new EventManager().startService();
    }

    /**
     * This method advertises a new topic to every subscriber of that
     * topic
     * @param newtopic This is the new topic object to be advertised
     */
    public static void advertiseTopic(Topic newtopic) {
        for (String user_name :
                EventManager.subscriberThreadMap.keySet()) {
            ClientThread thread = EventManager.subscriberThreadMap.get(user_name);

            try {
                thread.sendAdvertisement(newtopic);
            } catch (SocketException e) {
                System.out.println(thread.user_name + " is offline. Caching the Topic:" + newtopic.getName());
                ArrayList<Topic> topicList = new ArrayList<>();

                if (EventManager.offlineTopics.containsKey(user_name)) {
                    topicList = EventManager.offlineTopics.get(user_name);
                }

                topicList.add(newtopic);
                EventManager.offlineTopics.put(user_name, topicList);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /*
     * Receives a request on port 6000 and calls the redirect function
     */
    private void startService() throws IOException {


        ServerSocket serverSock = new ServerSocket(6000);

        //creating new sockets
        for (int i = 0; i < 10; i++) {
            serverSocks[i] = new ServerSocket(6001 + i);

            new Thread(new PortThread(serverSocks[i])).start();
        }
        // receive initial connection and assign port numbers
        redirectToNewPort(serverSock);

    }

    /**
     * This method redirects the request received on port 6000
     * to a different port
     * @param serverSock This is the socket object
     * @throws IOException
     */
    private void redirectToNewPort(ServerSocket serverSock) throws IOException {

        Random rand = new Random();

        while (true) {

            Socket socket = serverSock.accept();

            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            int random_integer = rand.nextInt(10) + 6001;

            System.out.println("1 connection received, redirecting to port:" + random_integer);
            String portNum = "" + random_integer;
            out.writeUTF(portNum);
            out.flush();

        }

    }


}