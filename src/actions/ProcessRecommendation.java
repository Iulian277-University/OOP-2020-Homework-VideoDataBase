package actions;

import common.Constants;
import entities.Entities;
import fileio.ActionInputData;
import fileio.Writer;
import org.json.simple.JSONArray;
import user.User;
import video.Movie;
import video.Season;
import video.Show;

import java.io.IOException;
import java.util.*;

import static actions.ProcessQuery.*;

public final class ProcessRecommendation {

    private ProcessRecommendation() {}

    public static void recommendation(ActionInputData action, Entities entities,
                                      JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        switch(action.getType()) {
            case Constants.STANDARD -> standard(action, entities, jsonArrayOutput, fileWriter);
            case Constants.BEST_UNSEEN -> bestUnseen(action, entities, jsonArrayOutput, fileWriter);
            case Constants.POPULAR -> popular(action, entities, jsonArrayOutput, fileWriter);
            case Constants.FAVORITE -> favorite(action, entities, jsonArrayOutput, fileWriter);
            case Constants.SEARCH -> search(action, entities, jsonArrayOutput, fileWriter);
            default -> throw new IllegalStateException("Unexpected value: " + action.getType());
        }
    }


    /**  Methods for all users */
    private static void standard(ActionInputData action, Entities entities,
                                 JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        String usernameString = action.getUsername();
        List<User> usersFilteredByUsername = ProcessCommand.filterUsersByName(usernameString, entities.getUsers());
        User user = usersFilteredByUsername.get(0);

        List<Movie> movies = entities.getMovies();
        List<Show> shows = entities.getShows();

        for(Movie movie: movies) {
            if(!user.getMoviesHistory().containsKey(movie)) {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(),
                        "",
                        "StandardRecommendation result: " + movie.getTitle()));
                return;
            }
        }

        for(Show show: shows) {
            if(!user.getShowsHistory().containsKey(show)) {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(),
                        "",
                        "StandardRecommendation result: " + show.getTitle()));
                return;
            }
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "StandardRecommendation cannot be applied!"));
    }

    private static void bestUnseen(ActionInputData action, Entities entities,
                                   JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        String usernameString = action.getUsername();
        List<User> usersFilteredByUsername = ProcessCommand.filterUsersByName(usernameString, entities.getUsers());
        User user = usersFilteredByUsername.get(0);


        List<Movie> movies = entities.getMovies();
        List<Show> shows = entities.getShows();


        // Set average rating for each movie
        for(Movie movie: movies) {
            movie.setAverageRating(calculateAverageOfList(movie.getRatings()));
        }

        // Set average rating for each show
        List<Show> filteredShows = new ArrayList<>();
        for(Show show: shows) {
            List<Season> seasons = show.getSeasons();

            // If at least one season has at least on rating
            boolean hasRatings = false;
            for(Season season: seasons) {
                if(!season.getRatings().isEmpty()) {
                    hasRatings = true;
                    break;
                }
            }
            // If a show doesn't have any rating, go to the next show
            if(!hasRatings) {
                continue;
            }

            double ratingSum = 0.0;
            int numberOfSeasons = show.getNumberOfSeasons();
            for(Season season: seasons) {
                ratingSum += calculateAverageOfList(season.getRatings());
            }
            double averageRating = ratingSum / numberOfSeasons;
            show.setAverageRating(averageRating);

            filteredShows.add(show);
        }

        List<Movie> filteredMovies = new ArrayList<>(movies);

        // Sort the videos DESC based on averageRating
        filteredMovies.sort((Movie m1, Movie m2) -> m2.getAverageRating().compareTo(m1.getAverageRating()));
        filteredShows.sort((Show s1, Show s2) -> s2.getAverageRating().compareTo(s1.getAverageRating()));

        // Return the best_unseen video (movie/show)
        for(Movie movie: filteredMovies) {
            if(!user.getMoviesHistory().containsKey(movie)) {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(),
                        "",
                        "BestRatedUnseenRecommendation result: " + movie.getTitle()));
                return;
            }
        }

        for(Show show: filteredShows) {
            if(!user.getShowsHistory().containsKey(show)) {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(),
                        "",
                        "BestRatedUnseenRecommendation result: " + show.getTitle()));
                return;
            }
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "BestRatedUnseenRecommendation cannot be applied!"));
    }


    /** Methods only for premium users */
    private static void popular(ActionInputData action, Entities entities,
                                JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        String usernameString = action.getUsername();
        List<User> users = entities.getUsers();
        List<User> usersFilteredByUsername = ProcessCommand.filterUsersByName(usernameString, users);
        User user = usersFilteredByUsername.get(0);

        // If the user isn't premium
        if(!user.getSubscriptionType().equals(Constants.PREMIUM)) {
            jsonArrayOutput.add(fileWriter.writeFile(
                    action.getActionId(),
                    "",
                    "PopularRecommendation cannot be applied!"));
            return;
        }


        List<Movie> movies = entities.getMovies();
        List<Show> shows = entities.getShows();

        // Compute the all-genres viewed as a string list
        List<String> allGenres = new ArrayList<>();
        for(Movie movie: movies) {
            for(String genre: movie.getGenres()) {
                if(!allGenres.contains(genre)) {
                    allGenres.add(genre);
                }
            }
        }
        for(Show show: shows) {
            for(String genre: show.getGenres()) {
                if(!allGenres.contains(genre)) {
                    allGenres.add(genre);
                }
            }
        }

        // For each movie, compute and set the numberOfViews
        for(Movie movie: movies) {
            int numberOfViews = 0;
            // Iterate through each user map of historyMovies <Movie, Integer>
            for(User currUser: users) {
                if(currUser.getMoviesHistory().containsKey(movie)) {
                    numberOfViews += currUser.getMoviesHistory().get(movie);
                }
            }

            movie.setNumberOfViews(numberOfViews);
        }

        // For each show, compute and set the numberOfViews
        for(Show show: shows) {
            int numberOfViews = 0;
            // Iterate through each user map of historyShows <Show, Integer>
            for(User currUser: users) {
                if(currUser.getShowsHistory().containsKey(show)) {
                    numberOfViews += currUser.getShowsHistory().get(show);
                }
            }

            show.setNumberOfViews(numberOfViews);
        }

        // Genre <-> Number of views in that genre
        Map<String, Integer> genresTop = new HashMap<>();

        // For each genre
        for(String genre: allGenres) {
            // Movies
            for(Movie movie: movies) {
                if(movie.getGenres().contains(genre)) {
                    if(!genresTop.containsKey(genre)) {
                        if(movie.getNumberOfViews() == null) {
                            genresTop.put(genre, 0);
                        } else {
                            genresTop.put(genre, movie.getNumberOfViews());
                        }
                    } else {
                        if(movie.getNumberOfViews() == null) {
                            genresTop.put(genre, genresTop.get(genre));
                        } else {
                            genresTop.put(genre, genresTop.get(genre) + movie.getNumberOfViews());
                        }
                    }
                }
            }

            // Shows
            for(Show show: shows) {
                if(show.getGenres().contains(genre)) {
                    if(!genresTop.containsKey(genre)) {
                        if(show.getNumberOfViews() == null) {
                            genresTop.put(genre, 0);
                        } else {
                            genresTop.put(genre, show.getNumberOfViews());
                        }
                    } else {
                        if(show.getNumberOfViews() == null) {
                            genresTop.put(genre, genresTop.get(genre));
                        } else {
                            genresTop.put(genre, genresTop.get(genre) + show.getNumberOfViews());
                        }
                    }
                }
            }
        }

        // Sort genresTop DESC by value
        Map<String, Integer> sortedGenresTop = new LinkedHashMap<>();
        genresTop.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(genre -> sortedGenresTop.put(genre.getKey(), genre.getValue()));


        for(var entry: sortedGenresTop.entrySet()) {
            String genre = entry.getKey();

            // Check for movies
            for(Movie movie: movies) {
                if(movie.getGenres().contains(genre) && !user.getMoviesHistory().containsKey(movie)) {
                    jsonArrayOutput.add(fileWriter.writeFile(
                            action.getActionId(),
                            "",
                            "PopularRecommendation result: " + movie.getTitle()));
                    return;
                }
            }

            // Check for shows
            for(Show show: shows) {
                if(show.getGenres().contains(genre) && !user.getShowsHistory().containsKey(show)) {
                    jsonArrayOutput.add(fileWriter.writeFile(
                            action.getActionId(),
                            "",
                            "PopularRecommendation result: " + show.getTitle()));
                    return;
                }
            }
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "PopularRecommendation cannot be applied!"));
    }

    private static void favorite(ActionInputData action, Entities entities,
                                 JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        String usernameString = action.getUsername();
        List<User> users = entities.getUsers();
        List<User> usersFilteredByUsername = ProcessCommand.filterUsersByName(usernameString, users);
        User user = usersFilteredByUsername.get(0);

        // If the user isn't premium
        if(!user.getSubscriptionType().equals(Constants.PREMIUM)) {
            jsonArrayOutput.add(fileWriter.writeFile(
                    action.getActionId(),
                    "",
                    "FavoriteRecommendation cannot be applied!"));
            return;
        }

        List<Movie> movies = entities.getMovies();
        List<Show> shows = entities.getShows();

        // Count stars for movies
        for(Movie movie: movies) {
            movie.setFavoriteCount(countStarsForMovie(movie, users));
        }
        // Count stars for shows
        for(Show show: shows) {
            show.setFavoriteCount(countStarsForShow(show, users));
        }

        // <Movie/Show, Integer>: Put together movies and shows
        Map<String, Integer> topFavoriteVideos = new HashMap<>();
        for(Movie movie: movies) {
            topFavoriteVideos.put(movie.getTitle(), movie.getFavoriteCount());
        }
        for(Show show: shows) {
            topFavoriteVideos.put(show.getTitle(), show.getFavoriteCount());
        }

        // Sort topFavoriteVideos DESC by value
        Map<String, Integer> sortedTopFavoriteVideos = new LinkedHashMap<>();
        topFavoriteVideos.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(video -> sortedTopFavoriteVideos.put(video.getKey(), video.getValue()));


        for(var entry: sortedTopFavoriteVideos.entrySet()) {
            // Is the video a movie or a show?
            List<Movie> bestMovies = filterMoviesByTitle(entry.getKey(), movies);
            Movie bestMovie = null;
            if(!bestMovies.isEmpty()) {
                bestMovie = bestMovies.get(0);
            }

            List<Show> bestShows = filterShowsByTitle(entry.getKey(), shows);
            Show bestShow = null;
            if(!bestShows.isEmpty()) {
                bestShow = bestShows.get(0);
            }

            if(bestMovie == null && bestShow == null) {
                return; // Will never reach this return if everything was good before
            }

            if(bestMovie != null && !user.getMoviesHistory().containsKey(bestMovie)) {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(),
                        "",
                        "FavoriteRecommendation result: " + bestMovie.getTitle()));
                return;
            }

            if(bestShow != null && !user.getShowsHistory().containsKey(bestShow)) {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(),
                        "",
                        "FavoriteRecommendation result: " + bestShow.getTitle()));
                return;
            }
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "FavoriteRecommendation cannot be applied!"));
    }

    private static void search(ActionInputData action, Entities entities,
                               JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        String usernameString = action.getUsername();
        List<User> users = entities.getUsers();
        List<User> usersFilteredByUsername = ProcessCommand.filterUsersByName(usernameString, users);
        User user = usersFilteredByUsername.get(0);
        String genreWanted = action.getGenre();

        // If the user isn't premium
        if(!user.getSubscriptionType().equals(Constants.PREMIUM)) {
            jsonArrayOutput.add(fileWriter.writeFile(
                    action.getActionId(),
                    "",
                    "SearchRecommendation cannot be applied!"));
            return;
        }


        List<Movie> movies = entities.getMovies();
        // Set average rating for each movie
        for(Movie movie: movies) {
            movie.setAverageRating(calculateAverageOfList(movie.getRatings()));
        }

        // Filter movies by genre and unwatched from user
        List<Movie> filteredMovies = new ArrayList<>();
        for(Movie movie: movies) {
            if(movie.getGenres().contains(genreWanted) && !user.getMoviesHistory().containsKey(movie)) {
                filteredMovies.add(movie);
            }
        }

        List<Show> shows = entities.getShows();
        // Set average rating for each show
        List<Show> firstFilteredShows = new ArrayList<>();
        for(Show show: shows) {
            List<Season> seasons = show.getSeasons();

            // If at least one season has at least on rating
            boolean hasRatings = false;
            for(Season season: seasons) {
                if(!season.getRatings().isEmpty()) {
                    hasRatings = true;
                    break;
                }
            }
            // If a show doesn't have any rating, go to the next show
            if(!hasRatings) {
                show.setAverageRating(0.0);
                firstFilteredShows.add(show);
                continue;
            }

            double ratingSum = 0.0;
            int numberOfSeasons = show.getNumberOfSeasons();
            for(Season season: seasons) {
                ratingSum += calculateAverageOfList(season.getRatings());
            }
            double averageRating = ratingSum / numberOfSeasons;
            show.setAverageRating(averageRating);

            firstFilteredShows.add(show);
        }

        // Filter shows by genre and unwatched from user
        List<Show> filteredShows = new ArrayList<>();
        for(Show show: firstFilteredShows) {
            if(show.getGenres().contains(genreWanted) && !user.getShowsHistory().containsKey(show)) {
                filteredShows.add(show);
            }
        }

        // Sort Movies ASC on rating and titles
        filteredMovies.sort((Movie m1, Movie m2) -> m1.getTitle().compareTo(m2.getTitle()));
        filteredMovies.sort((Movie m1, Movie m2) -> m1.getAverageRating().compareTo(m2.getAverageRating()));

        // Sort Shows ASC on rating and titles
        filteredShows.sort((Show s1, Show s2) -> s1.getTitle().compareTo(s2.getTitle()));
        filteredShows.sort((Show s1, Show s2) -> s1.getAverageRating().compareTo(s2.getAverageRating()));


        if(filteredMovies.isEmpty() && filteredShows.isEmpty()) {
            jsonArrayOutput.add(fileWriter.writeFile(
                    action.getActionId(),
                    "",
                    "SearchRecommendation cannot be applied!"));
            return;
        }

        StringBuilder videosListString = new StringBuilder();
        int i = 0;
        int j = 0;
        while(i < filteredMovies.size() && j < filteredShows.size()) {
            if(videosListString.length() > 0) {
                videosListString.append(", ");
            }

            if (filteredMovies.get(i).getAverageRating() < filteredShows.get(j).getAverageRating()) {
                videosListString.append(filteredMovies.get(i).getTitle());
                i++;
            } else if(filteredMovies.get(i).getAverageRating() > filteredShows.get(j).getAverageRating()) {
                videosListString.append(filteredShows.get(j).getTitle());
                j++;
            } else { // Equals rating -> Compare by title
                if(filteredMovies.get(i).getTitle().compareTo(filteredShows.get(j).getTitle()) < 0) {
                    videosListString.append(filteredMovies.get(i).getTitle());
                    i++;
                } else {
                    videosListString.append(filteredShows.get(j).getTitle());
                    j++;
                }
            }
        }

        while(i < filteredMovies.size()) {
            if(videosListString.length() > 0) {
                videosListString.append(", ");
            }
            videosListString.append(filteredMovies.get(i).getTitle());
            i++;
        }

        while(j < filteredShows.size()) {
            if(videosListString.length() > 0) {
                videosListString.append(", ");
            }
            videosListString.append(filteredShows.get(j).getTitle());
            j++;
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "SearchRecommendation result: [" + videosListString.toString() + "]"));

    }
}
