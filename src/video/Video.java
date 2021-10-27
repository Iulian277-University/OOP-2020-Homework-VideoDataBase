package video;

import actor.Actor;
import java.util.ArrayList;

public class Video {

    /** Attributes */
    private String title;
    private int year;
    private ArrayList<Actor> actors;
    private ArrayList<String> genres;

    private Integer favoriteCount;


    /** Constructors */
    public Video() {
    }

    public Video(String title, int year, ArrayList<Actor> actors, ArrayList<String> genres) {
        this.title = title;
        this.year = year;
        this.actors = actors;
        this.genres = genres;
    }

    /** Getters */
    public String getTitle() {
        return title;
    }

    public ArrayList<Actor> getActors() {
        return actors;
    }

    public int getYear() {
        return year;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    /** Setters */
    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
}
