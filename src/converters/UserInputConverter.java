package converters;

import fileio.UserInputData;
import user.User;
import video.Movie;
import video.Season;
import video.Show;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class UserInputConverter {

    private UserInputConverter() {}

    private static ArrayList<User> users = new ArrayList<>();

    public static Movie filterMoviesByTitle(String title, List<Movie> movies) {
        // Filter movies using streams
        List<Movie> moviesFiltered = movies.stream().
                filter(movie -> movie.getTitle().equals(title)).
                collect(Collectors.toList());

        // If it's not a movie what we are searching for
        if(moviesFiltered.isEmpty()) {
            return null;
        }
        return moviesFiltered.get(0); // Supposing that the title of the movie is unique
    }

    public static Show filterShowsByTitle(String title, List<Show> shows) {
        // Filter serial using streams
        List<Show> showsFiltered = shows.stream().
                filter(show -> show.getTitle().equals(title)).
                collect(Collectors.toList());

        // If it's not a serial what we are searching for
        if(showsFiltered.isEmpty()) {
            return null;
        }
        return showsFiltered.get(0); // Supposing that the title of the serial is unique
    }

    public static void convert(List<UserInputData> usersData, List<Movie> movies, List<Show> shows) {
        for(UserInputData user: usersData) {
            String username = user.getUsername();
            String subscriptionType = user.getSubscriptionType();

            Map<String, Integer> historyString = user.getHistory();

            Map<Movie, Integer> moviesHistory = new HashMap<>();
            Map<Show, Integer> showsHistory = new HashMap<>();

            Map<Movie, Double> moviesRating = new HashMap<>();
            Map<Season, Double> seasonsRating = new HashMap<>();


            // For each video (Movie or Show)
            for(String videoTitle: historyString.keySet()) {
                // Search in the MovieObj list
                Movie movie = filterMoviesByTitle(videoTitle, movies);

                // Search in the ShowObj list
                Show show = filterShowsByTitle(videoTitle, shows);

                if(movie != null) {
                    moviesHistory.put(movie, user.getHistory().get(videoTitle));
                }
                else { // implies serial != null
                    showsHistory.put(show, user.getHistory().get(videoTitle));
                }
            }


            ArrayList<String> favoriteMoviesString = user.getFavoriteMovies();
            ArrayList<Movie> favoriteMovies = new ArrayList<>();
            ArrayList<Show> favoriteShows = new ArrayList<>();
            for(String favoriteVideoTitle: favoriteMoviesString) {
                // Search in the MovieObj list
                Movie movie = filterMoviesByTitle(favoriteVideoTitle, movies);

                // Search in the ShowObj list
                Show show = filterShowsByTitle(favoriteVideoTitle, shows);

                if(movie != null) {
                    favoriteMovies.add(movie);
                }
                else { // implies serial != null
                    favoriteShows.add(show);
                }
            }

            User userObj = new User(username, subscriptionType,
                    moviesHistory, showsHistory,
                    favoriteMovies, favoriteShows,
                    moviesRating, seasonsRating);
            users.add(userObj);
        }
    }

    public static List<User> getObjUsers() {
        return users;
    }

}
