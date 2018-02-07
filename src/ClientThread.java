import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This file is a part of the PubSubAgent project
 * Course: Distributed Systems
 *
 * @author Darryl Pinto (dp6417)
 * @author Ketan Joshi (ksj4205)
 * @author Renzil Dourado  (rd9012)
 */

/**
 * Class Event thread is used to communicate with each client individually
 */
public class ClientThread implements Runnable {

    public String user_name; //user name of client
    private ServerSocket serverSocket; //socket of the port
    private Socket client; // socket of the client
    private ObjectInputStream objinput;
    private ObjectOutputStream objoutput;

    // ServerSocket is serverSocket on portThread

    /**
     * Constructor of client thread
     * @param client : socket of client
     * @param serverSocket : socket of the port
     * @throws IOException
     */
    public ClientThread(Socket client, ServerSocket serverSocket) throws IOException {

        this.client = client;
        this.serverSocket = serverSocket;

        this.objinput = new ObjectInputStream(this.client.getInputStream());
        this.objoutput = new ObjectOutputStream(this.client.getOutputStream());
        this.user_name = "";

    }

    @Override
    public void run() {

        try {
            //user name of the client
            user_name = objinput.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("User logged in: " + user_name);

        try {
            //checks if the user is already present
            if (EventManager.subscriberTopics.containsKey(user_name)) {

                objoutput.writeUTF("Logged In:" + user_name);
                objoutput.flush();

                //sends offline topics and events if there exist

                objoutput.reset();
                objoutput.writeObject(EventManager.offlineTopics.get(user_name));
                objoutput.flush();

                objoutput.reset();
                objoutput.writeObject(EventManager.offlineEvents.get(user_name));
                objoutput.flush();

                EventManager.offlineTopics.put(user_name, new ArrayList<>());
                EventManager.offlineEvents.put(user_name, new ArrayList<>());


            } else {
                // if its a new user then register it

                EventManager.subscriberTopics.put(user_name, new ArrayList<>());
                objoutput.writeUTF("You are Registered:" + user_name);
                objoutput.flush();

                EventManager.offlineTopics.put(user_name, new ArrayList<>());
                EventManager.offlineEvents.put(user_name, new ArrayList<>());

            }
            EventManager.subscriberThreadMap.put(user_name, this);

            communicate();
        } catch (IOException e) {
            System.out.printf("%s: %s has disconnected\n",e.getMessage(),this.user_name);
        }

    }

    /**
     * This process starts the communication with the client
     * @throws IOException
     */
    private void communicate() throws IOException {

        while (true) {

            String input_string = this.objinput.readUTF();

            // switch case handles each request from the menu sent by user
            switch (input_string) {
                //sends list of all topics user has subscribed to
                case "getAllTopics":

                    objoutput.writeUTF("getAllTopics");
                    objoutput.flush();

                    ArrayList<String> arr = new ArrayList<>();

                    for (String key : EventManager.topicSubscriber.keySet()) {
                        arr.add(key);
                    }

                    objoutput.writeObject(arr);
                    objoutput.flush();

                    break;

                // sends list of keywords
                case "getAllKeywords":

                    objoutput.writeUTF("getAllKeywords");
                    objoutput.flush();

                    ArrayList<String> arrKeywords = new ArrayList<>();

                    for (String key : EventManager.keywordTopic.keySet()) {
                        arrKeywords.add(key);
                    }

                    objoutput.writeObject(arrKeywords);
                    objoutput.flush();

                    break;

                // case to subscribe to all the topics
                case "subscribeTopics":

                    String receivedUserName = objinput.readUTF();

                    while (true) {
                        // receive topics break when topicname == 3511
                        try {

                            Object obj = objinput.readObject();
                            Topic topic = (Topic) obj;

                            if (topic.getName().equals("3511"))
                                break;

                            // adding subscriber to topicsubsriber
                            ArrayList<String> temp = EventManager.topicSubscriber.get(topic.getName());
                            HashSet<String> isUserPresent = new HashSet<>(temp);

                            if (!isUserPresent.contains(receivedUserName)) {
                                temp.add(receivedUserName);
                                EventManager.topicSubscriber.put(topic.getName(), temp);

                                //adding topic to the subscriber topic
                                // check if the topic is already present or not
                                ArrayList<String> topicTemp = EventManager.subscriberTopics.get(receivedUserName);
                                topicTemp.add(topic.getName());
                                EventManager.subscriberTopics.put(receivedUserName, topicTemp);

                                System.out.println(receivedUserName + " is subscribed to topic: " + topic.getName());
                            }


                        } catch (NullPointerException e) {
                            System.out.println("Key not found or invalid spelling!!!");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                    break;

                // case for subscribing using keywords
                case "subscribeKeywords":

                    while (true) {

                        try {
                            String keyword = objinput.readUTF();

                            // 3513 is the agreed keyword to break the input list from client
                            if (keyword.equalsIgnoreCase("3513"))
                                break;

                            String topicName = EventManager.keywordTopic.get(keyword);

                            ArrayList<String> temp = EventManager.topicSubscriber.get(topicName);
                            HashSet<String> isUserPresent = new HashSet<>(temp);

                            if (!isUserPresent.contains(this.user_name)) {
                                temp.add(this.user_name);
                                EventManager.topicSubscriber.put(topicName, temp);

                                ArrayList<String> topicTemp = EventManager.subscriberTopics.get(this.user_name);
                                topicTemp.add(topicName);
                                EventManager.subscriberTopics.put(this.user_name, topicTemp);

                                System.out.printf("%s has been subscribed to %s using keyword %s\n",
                                        this.user_name, topicName, keyword);
                            }
                        } catch (NullPointerException e) {
                            System.out.println("Key not found or invalid spelling!!!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    break;

                // it fulfills the list of subscribed topics of the user
                case "getSubscribedTopics":

                    this.objoutput.writeUTF("getSubscribedTopics");
                    this.objoutput.flush();

                    ArrayList<String> temp = EventManager.subscriberTopics.get(this.user_name);
                    System.out.println("Request for List of Subscribed Topics by " + user_name + " Sending " + temp);

                    objoutput.reset();
                    this.objoutput.writeObject(temp);
                    this.objoutput.flush();

                    break;

                // this case is for advertising a topics
                case "advertise":
                    try {
                        Object obj = this.objinput.readObject();
                        Topic newtopic = (Topic) obj;


                        if (EventManager.topicSubscriber.containsKey(newtopic.getName())) {
                            System.out.println("Topic already present: " + newtopic.getName());
                        } else {
                            EventManager.topicSubscriber.put(newtopic.getName(), new ArrayList<>());
                            EventManager.advertiseTopic(newtopic);

                            EventManager.topicKeyword.put(newtopic.getName(), newtopic.getKeywords());

                            for (String keyword : newtopic.getKeywords()) {
                                EventManager.keywordTopic.put(keyword, newtopic.getName());
                            }
                        }

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;

                // it fulfills the request of unsubscribing
                case "unsubscribeTopic":

                    String userNametoUnsubscribe = objinput.readUTF();

                    while (true) {
                        try {


                            Object obj = objinput.readObject();
                            Topic topic = (Topic) obj;

                            // receive topic name = 3512 to break the loop
                            if (topic.getName().equals("3512"))
                                break;

                            //removed from the subscribed topic list of user
                            ArrayList<String> temparr = EventManager.subscriberTopics.get(userNametoUnsubscribe);
                            temparr.remove(topic.getName());
                            EventManager.subscriberTopics.put(userNametoUnsubscribe, temparr);

                            // removed from the list of subscriber for that specific topic
                            temparr = EventManager.topicSubscriber.get(topic.getName());
                            temparr.remove(userNametoUnsubscribe);
                            EventManager.topicSubscriber.put(topic.getName(), temparr);

                            System.out.println(userNametoUnsubscribe + " has unsubscribed" +
                                    " from the topic: " + topic.getName());

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        //error to be caught when topic name is not present
                        catch (NullPointerException e) {
                            System.out.println("Error : Invalid spelling !");
                        }

                    }


                    break;

                // this ase is to unsubscribe from all the topics
                case "unsubscribeAll":
                    String usernameAll = objinput.readUTF();

                    //removed all topics from subscriber topic
                    ArrayList<String> temptopics = EventManager.subscriberTopics.get(usernameAll);
                    EventManager.subscriberTopics.put(usernameAll, new ArrayList<>());

                    // removed one by one from topic subscriber
                    for (String s : temptopics) {
                        ArrayList<String> temptopiclist = EventManager.topicSubscriber.get(s);
                        temptopiclist.remove(usernameAll);
                        EventManager.topicSubscriber.put(s, temptopiclist);

                        System.out.println(usernameAll + " has unsubscribed from the topic: " + s);
                    }

                    break;


                //this is the case which is called when a new event is created
                case "Event":
                    try {
                        Object obj = objinput.readObject();
                        Event event = (Event) obj;
                        EventManager.notifySubscribers(event);

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;


            }
        }


    }

    /**
     * This method is called when a new topic is advertised
     * @param newtopic : topic is the topic to be advertised
     * @throws IOException
     */
    public void sendAdvertisement(Topic newtopic) throws IOException {

        this.objoutput.writeUTF("Topic");
        this.objoutput.flush();

        this.objoutput.writeObject(newtopic);
        this.objoutput.flush();

        System.out.printf("%s Advertised to %s\n",newtopic.getName(), this.user_name);

    }

    /**
     * this method is called when a new event is advertised
     * @param event
     * @throws IOException
     */
    public void sendEvent(Event event) throws IOException {
        this.objoutput.writeUTF("Event");
        this.objoutput.flush();

        this.objoutput.writeObject(event);
        this.objoutput.flush();

        System.out.printf("%s (%s) Published to %s",event.getTitle(), event.getTopic().getName(), this.user_name);
    }
}
