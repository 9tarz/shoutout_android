package helper;

/**
 * Created by nullnil on 10/12/15.
 */
public class Post {
    public String text;
    public String username;
    public long timestamp;
    public String image_url;


    public Post() {
    }

    public Post(String text, String username, long timestamp, String image_url) {
        this.text = text;
        this.username = username;
        this.timestamp = timestamp;
        this.image_url = image_url;
    }
}
