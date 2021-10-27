package user;

import video.Movie;
import video.Show;
import video.Season;

import java.util.ArrayList;
import java.util.Map;

public class User {

    /** Attributes */
    private String username;
    private String subscriptionType;

    private final Map<Movie, Integer> moviesHistory;
    private final Map<Show, Integer> showsHistory;

    private final ArrayList<Movie> moviesFavorite;
    private final ArrayList<Show> showsFavorite;

    private final Map<Movie, Double> moviesRating;
    private final Map<Season, Double> seasonsRating;

    private Integer numberOfRatings;

    /** Constructors */
    public User(String username, String subscriptionType,
                Map<Movie, Integer> moviesHistory, Map<Show, Integer> showsHistory,
                ArrayList<Movie> moviesFavorite, ArrayList<Show> showsFavorite,
                Map<Movie, Double> moviesRating, Map<Season, Double> seasonsRating) {

        this.username = username;
        this.subscriptionType = subscriptionType;

        this.moviesHistory = moviesHistory;
        this.showsHistory = showsHistory;

        this.moviesFavorite = moviesFavorite;
        this.showsFavorite = showsFavorite;

        this.moviesRating = moviesRating;
        this.seasonsRating = seasonsRating;
    }


    /** Getters */
    public String getUsername() {
        return username;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public Map<Movie, Integer> getMoviesHistory() {
        return this.moviesHistory;
    }

    public Map<Show, Integer> getShowsHistory() {
        return showsHistory;
    }

    public ArrayList<Movie> getMoviesFavorite() {
        return moviesFavorite;
    }

    public ArrayList<Show> getShowsFavorite() {
        return showsFavorite;
    }

    public Map<Movie, Double> getMoviesRating() {
        return moviesRating;
    }

    public Map<Season, Double> getSeasonRating() {
        return seasonsRating;
    }

    public Integer getNumberOfRatings() {
        return numberOfRatings;
    }

    /** Setters */
    public void addMovieRating(Movie movie, Double rating) {
        this.moviesRating.put(movie, rating);
    }

    public void addSeasonRating(Season season, Double rating) {
        this.seasonsRating.put(season, rating);
    }

    public void setNumberOfRatings(Integer numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

}
