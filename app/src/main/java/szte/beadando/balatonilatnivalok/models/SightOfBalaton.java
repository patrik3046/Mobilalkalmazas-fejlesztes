package szte.beadando.balatonilatnivalok.models;

import java.util.ArrayList;
import java.util.List;

public class SightOfBalaton {
    private String id;
    private String name;
    private String location;
    private String description;
    private String imageLink;
    private String authorId;
    private int votes;
    private List<String> votedUsersId;

    public SightOfBalaton(){}

    public SightOfBalaton(String name, String location, String description, String imagePath, String authorId, int votes) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.imageLink = imagePath;
        this.authorId = authorId;
        this.votes = votes;
        this.votedUsersId = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getImageLink() {
        return imageLink;
    }

    public String getAuthorId() {
        return authorId;
    }

    public int getVotes() {return votes;}

    public List<String> getVotedUsersId() {
        return votedUsersId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setVotedUsersId(List<String> votedUsersId) {
        this.votedUsersId = votedUsersId; }
}
