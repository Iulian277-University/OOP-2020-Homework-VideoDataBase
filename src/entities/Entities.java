package entities;

import actor.Actor;
import user.User;
import video.Movie;
import video.Show;

import java.util.List;

public class Entities {

    /** Entities - Attributes */
    private List<Actor> actors;
    private List<User> users;
    private List<Movie> movies;
    private List<Show> shows;

    /** Constructors */
    public Entities() {
        this.actors = null;
        this.users = null;
        this.movies = null;
        this.shows = null;
    }

    public Entities(List<Actor> actors, List<User> users, List<Movie> movies, List<Show> shows) {
        this.actors = actors;
        this.users = users;
        this.movies = movies;
        this.shows = shows;
    }

    /** Getters */
    public List<Actor> getActors() {
        return actors;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public List<Show> getShows() {
        return shows;
    }

}
