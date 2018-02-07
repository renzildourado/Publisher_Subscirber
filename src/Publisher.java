/**
 * This file is a part of the PubSubAgent project
 * Course: Distributed Systems
 *
 * @author Darryl Pinto (dp6417)
 * @author Ketan Joshi (ksj4205)
 * @author Renzil Dourado  (rd9012)
 */

public interface Publisher {
    /*
     * publish an event of a specific topic with title and content
     */
    public void publish(Event event);

    /*
     * advertise new topic
     */
    public void advertise(Topic newTopic);
}
