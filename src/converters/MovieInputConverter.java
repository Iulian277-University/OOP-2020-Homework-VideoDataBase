package converters;

import actor.Actor;
import fileio.MovieInputData;
import video.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MovieInputConverter {

    private MovieInputConverter() {}

    private static List<Movie> movies = new ArrayList<>(); // moviesConvertedToObjects

    public static Actor filterActorsByName(String name, List<Actor> actors) {
        // Filter actors using streams
        List<Actor> actorsFiltered = actors.stream().
                filter(actor -> actor.getName().equals(name)).
                collect(Collectors.toList());

        // If it's not a movie what we are searching for
        if(actorsFiltered.isEmpty()) {
            return null;
        }
        return actorsFiltered.get(0); // Supposing that the name of the actor is unique
    }


    public static void convert(List<MovieInputData> moviesData) {
        for(MovieInputData movie: moviesData) {
            int duration = movie.getDuration();
            int year = movie.getYear();
            String title = movie.getTitle();
            ArrayList<String> genres = movie.getGenres();

            // In first iteration of conversion, create the actor only with name
            ArrayList<Actor> actors = new ArrayList<>();
            ArrayList<String> actorsString = movie.getCast();
            for(String actorString: actorsString) {
                Actor currActor = new Actor(actorString);
                actors.add(currActor);
            }

            Movie movieObj = new Movie(title, year, duration, actors, genres);
            movies.add(movieObj);
        }
    }


    public static List<Movie> getObjMovies() {
        return movies;
    }

}
