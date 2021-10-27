package video;
import actor.Actor;

import java.util.ArrayList;

// Show == Serial
public class Show extends Video {

    /** Attributes */
    private int numberOfSeasons;
    private ArrayList<Season> seasons;

    private Integer totalDuration;
    private Double averageRating;
    private Integer numberOfViews;

    /** Constructors */
    public Show(String title, int year, ArrayList<Actor> actors, ArrayList<String> genres,
                int numberOfSeasons, ArrayList<Season> seasons) {
        super(title, year, actors, genres);
        this.numberOfSeasons = numberOfSeasons;
        this.seasons = seasons;
    }

    /** Getters */
    @Override
    public String getTitle() {
        return super.getTitle();
    }

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    @Override
    public ArrayList<Actor> getActors() {
        return super.getActors();
    }

    public ArrayList<Season> getSeasons() {
        return seasons;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public Season getSeasonFromIndex(Integer index) {
        if(seasons.size() >= index) {
            return seasons.get(index - 1); // Indexing from 0
        }
        return null; // Index out of bounds
    }

    @Override
    public ArrayList<String> getGenres() {
        return super.getGenres();
    }

    public Integer getNumberOfViews() {
        return numberOfViews;
    }

    /** Setters */
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

    public void setNumberOfViews(Integer numberOfViews) {
        this.numberOfViews = numberOfViews;
    }
}
