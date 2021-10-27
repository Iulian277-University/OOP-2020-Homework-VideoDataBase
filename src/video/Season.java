package video;

import java.util.List;

public class Season {

    /** Attributes */
    private int currentSeason;
    private int duration;
    private List<Double> ratings;

    /** Constructors */
    public Season(int currentSeason, int duration, List<Double> ratings) {
        this.currentSeason = currentSeason;
        this.duration = duration;
        this.ratings = ratings;
    }

    /** Getters */
    public int getDuration() {
        return duration;
    }

    public List<Double> getRatings() {
        return ratings;
    }

    /** Setters */
    public void addRating(Double rating) {
        this.ratings.add(rating);
    }

}
