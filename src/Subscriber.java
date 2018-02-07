/**
 * This file is a part of the PubSubAgent project
 * Course: Distributed Systems
 *
 * @author Darryl Pinto (dp6417)
 * @author Ketan Joshi (ksj4205)
 * @author Renzil Dourado  (rd9012)
 */

public interface Subscriber {
    /*
     * subscribe to a topic
     */
    public void subscribe(Topic topic);

    /*
     * subscribe to a topic with matching keywords
     */
    public void subscribe(String keyword);

    /*
     * unsubscribe from a topic
     */
    public void unsubscribe(Topic topic);

    /*
     * unsubscribe to all subscribed topics
     */
    public void unsubscribe();

    /*
     * show the list of topics current subscribed to
     */
    public void listSubscribedTopics();

}
