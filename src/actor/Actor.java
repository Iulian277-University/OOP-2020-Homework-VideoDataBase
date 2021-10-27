package actor;

import video.Movie;
import video.Show;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Actor {
    private String name;
    private String careerDescription;
    private ArrayList<Movie> moviesPlayed;
    private ArrayList<Show> showsPlayed;
    private Map<ActorsAwards, Integer> awards;
    private Double averageRating;


    /** Constructors */
    public Actor(String name) {
        this.name = name;
    }

    public Actor(String name, String careerDescription,
                 ArrayList<Movie> moviesPlayed, ArrayList<Show> showsPlayed,
                 Map<ActorsAwards, Integer> awards) {
        this.name = name;
        this.careerDescription = careerDescription;
        this.moviesPlayed = moviesPlayed;
        this.showsPlayed = showsPlayed;
        this.awards = awards;
    }

    /** Getters */
    public String getName() {
        return name;
    }

    public String getCareerDescription() {
        return careerDescription;
    }

    public Map<ActorsAwards, Integer> getAwards() {
        return awards;
    }

    public ArrayList<Movie> getMoviesPlayed() {
        return moviesPlayed;
    }

    public ArrayList<Show> getShowsPlayed() {
        return showsPlayed;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getNumberOfAllAwards() {
        return this.awards.values().stream().reduce(0, Integer::sum);
    }

    /** Setters */
    public void addAwards(Map<ActorsAwards, Integer> awards) {
        if(this.awards == null) {
            this.awards = awards;
        } else {
            if(awards != null) {
                this.awards.putAll(awards);
            }
        }
    }

    public void setCareerDescription(String careerDescription) {
        this.careerDescription = careerDescription;
    }

    public void addMoviesPlayed(ArrayList<Movie> moviesPlayed) {
        if(moviesPlayed != null) {
            for (Movie moviePlayed : moviesPlayed) {
                if (!this.moviesPlayed.contains(moviePlayed)) {
                    this.moviesPlayed.add(moviePlayed);
                }
            }
        }
    }

    public void addShowsPlayed(ArrayList<Show> showsPlayed) {
        if(showsPlayed != null) {
            for(Show showPlayed: showsPlayed) {
                if(!this.showsPlayed.contains(showPlayed)) {
                    this.showsPlayed.add(showPlayed);
                }
            }
        }
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}
