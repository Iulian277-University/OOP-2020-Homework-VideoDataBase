package converters;

import actor.ActorsAwards;
import fileio.ActorInputData;
import actor.Actor;
import video.Movie;
import video.Show;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public final class ActorInputConverter {

    private ActorInputConverter() {}

    private static ArrayList<Actor> actors = new ArrayList<>();

    public static List<Movie> filterMoviesByTitle(String title, List<Movie> movies) {
        // Filter movies using streams
        List<Movie> moviesFiltered = movies.stream().
                filter(movie -> movie.getTitle().equals(title)).
                collect(Collectors.toList());

        // If it's not a movie that we are searching for
        if(moviesFiltered.isEmpty()) {
            return Collections.emptyList();
        }
        return moviesFiltered;
    }

    public static List<Show> filterShowsByTitle(String title, List<Show> shows) {
        // Filter serial using streams
        List<Show> showsFiltered = shows.stream().
                filter(show -> show.getTitle().equals(title)).
                collect(Collectors.toList());

        // If it's not a serial that we are searching for
        if(showsFiltered.isEmpty()) {
            return Collections.emptyList();
        }
        return showsFiltered;
    }

    public static void convert(List<ActorInputData> actorsData, List<Movie> movies, List<Show> shows) {
        for(ActorInputData actor: actorsData) {
            String name = actor.getName();
            String careerDescription = actor.getCareerDescription();
            ArrayList<String> filmography = actor.getFilmography();
            Map<ActorsAwards, Integer> awards = actor.getAwards();

            ArrayList<Movie> moviesPlayed = new ArrayList<>();
            ArrayList<Show> showsPlayed = new ArrayList<>();

            // For each video (Movie or Show)
            for(String videoTitle: filmography) {
                // Search in the MovieObj list
                List<Movie> moviesFiltered = filterMoviesByTitle(videoTitle, movies);

                // Search in the ShowObj list
                List<Show> showsFiltered = filterShowsByTitle(videoTitle, shows);

                // The empty list was checked in the filter function above
                moviesPlayed.addAll(moviesFiltered);
                showsPlayed.addAll(showsFiltered);
            }

            Actor actorObj = new Actor(name, careerDescription, moviesPlayed, showsPlayed, awards);
            actors.add(actorObj);
        }
    }

    public static List<Actor> getObjActors() {
        return actors;
    }

}
