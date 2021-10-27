package video;

import actor.Actor;
import java.util.ArrayList;
import java.util.List;

public class Movie extends Video {

    /** Attributes */
    private Integer duration;
    private List<Double> ratings;

    private Double averageRating;
    private Integer numberOfViews;

    /** Constructors */
    public Movie() {
    }

    public Movie(String title, int year, int duration,
                 ArrayList<Actor> actors, ArrayList<String> genres) {
        super(title, year, actors, genres);
        this.duration = duration;
        this.ratings = new ArrayList<Double>(); // When we construct the movieObj for the first time
    }

    /** Getters */
    public List<Double> getRatings() {
        return ratings;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    @Override
    public Integer getFavoriteCount() {
        return super.getFavoriteCount();
    }

    @Override
    public int getYear() {
        return super.getYear();
    }

    @Override
    public ArrayList<String> getGenres() {
        return super.getGenres();
    }

    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    public ArrayList<Actor> getActors() {
        return super.getActors();
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getNumberOfViews() {
        return numberOfViews;
    }

    /** Setters */
    public void addRating(Double rating) {
        this.ratings.add(rating);
    }

    public void setAverageRating(Double rating) {
        this.averageRating = rating;
    }

    public void setNumberOfViews(Integer numberOfViews) {
        this.numberOfViews = numberOfViews;
    }

    @Override
    public void setFavoriteCount(Integer favoriteCount) {
        super.setFavoriteCount(favoriteCount);
    }
}
