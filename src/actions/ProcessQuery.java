package actions;

import actor.Actor;
import common.Constants;
import entities.Entities;
import fileio.ActionInputData;
import fileio.Writer;
import org.json.simple.JSONArray;
import user.User;
import video.Movie;
import video.Season;
import video.Show;

import utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ProcessQuery {

    private ProcessQuery() {}

    /** Process Query */
    public static void query(ActionInputData action, Entities entities,
                             JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        switch(action.getObjectType()) {
            case Constants.ACTORS:
                switch(action.getCriteria()) {
                    case Constants.AVERAGE -> averageActors(action, entities, jsonArrayOutput, fileWriter);
                    case Constants.AWARDS -> awardsActors(action, entities, jsonArrayOutput, fileWriter);
                    case Constants.FILTER_DESCRIPTIONS -> filterDescriptionActors(action, entities, jsonArrayOutput, fileWriter);
                    default -> throw new IllegalStateException("Unexpected value: " + action.getCriteria());
                }
                break;
            case Constants.MOVIES:
                switch(action.getCriteria()) {
                    case Constants.RATINGS -> ratingMovies(action, entities, jsonArrayOutput, fileWriter);
                    case Constants.FAVORITE -> favoriteMovies(action, entities, jsonArrayOutput, fileWriter);
                    case Constants.LONGEST -> longestMovies(action, entities, jsonArrayOutput, fileWriter);
                    case Constants.MOST_VIEWED -> mostViewedMovies(action, entities, jsonArrayOutput, fileWriter);
                    default -> throw new IllegalStateException("Unexpected value: " + action.getCriteria());
                }
                break;
            case Constants.SHOWS:
                switch(action.getCriteria()) {
                    case Constants.RATINGS -> ratingShows(action, entities, jsonArrayOutput, fileWriter);
                    case Constants.FAVORITE -> favoriteShows(action, entities, jsonArrayOutput, fileWriter);
                    case Constants.LONGEST -> longestShows(action, entities, jsonArrayOutput, fileWriter);
                    case Constants.MOST_VIEWED -> mostViewedShows(action, entities, jsonArrayOutput, fileWriter);
                    default -> throw new IllegalStateException("Unexpected value: " + action.getCriteria());
                }
                break;
            case Constants.USERS:
                switch(action.getCriteria()) {
                    case Constants.NUM_RATINGS -> ratingUsers(action, entities, jsonArrayOutput, fileWriter);
                    default -> throw new IllegalStateException("Unexpected value: " + action.getCriteria());
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + action.getObjectType());
        }
    }

    /** Actors */
    // We need to re-compute the averageRating for each actor every time we use the averageActors method
    public static void computeAverageRatingActors(List<Actor> actors) {
        for(Actor actor: actors) {
            double averageRating;

            // Compute the movieRating
            double sumOfMovieRatings = 0.0;
            int numberOfMovieRatings = 0;
            double averageOfMovieRatings = 0.0;

            for(Movie moviePlayed: actor.getMoviesPlayed()) {
                List<Double> movieRatings = moviePlayed.getRatings();
                for(Double movieRating: movieRatings) {
                    if(movieRating != 0) {
                        numberOfMovieRatings++;
                        sumOfMovieRatings += movieRating;
                    }
                }
            }

            if(numberOfMovieRatings != 0) {
                averageOfMovieRatings = sumOfMovieRatings / numberOfMovieRatings;
            }


            // Compute the showRating
            double sumOfSeasonRatings = 0.0;
            int numberOfSeasonRatings = 0;
            double averageOfShowRatings = 0.0;

            for(Show showPlayed: actor.getShowsPlayed()) {
                for(Season season: showPlayed.getSeasons()) {
                    List<Double> seasonRatings = season.getRatings();
                    for(Double seasonRating: seasonRatings) {
                        sumOfSeasonRatings += seasonRating;
                    }
                }
                numberOfSeasonRatings = showPlayed.getNumberOfSeasons();
            }
            if(numberOfSeasonRatings != 0) {
                averageOfShowRatings = sumOfSeasonRatings / numberOfSeasonRatings;
            }


            // Compute the final average rating for that actor
            if(numberOfMovieRatings == 0 && numberOfSeasonRatings == 0) {
                averageRating = 0.0;
            } else if(numberOfMovieRatings == 0) {
                averageRating = averageOfShowRatings;
            } else if(numberOfSeasonRatings == 0) {
                averageRating = averageOfMovieRatings;
            } else {
                averageRating = (averageOfMovieRatings + averageOfShowRatings) / 2.0;
            }

            actor.setAverageRating(averageRating);
        }
    }


    public static void averageActors(ActionInputData action, Entities entities,
                                     JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Actor> actors = entities.getActors();

        computeAverageRatingActors(actors);

        List<Actor> sortedActorsByRating = new ArrayList<>(actors);

        String sortType = action.getSortType();

        if(sortType.equals(Constants.ASC)) {
            sortedActorsByRating.sort((Actor a1, Actor a2) -> a1.getName().compareTo(a2.getName()));
            sortedActorsByRating.sort((Actor a1, Actor a2) -> a1.getAverageRating().compareTo(a2.getAverageRating()));
        }

        if(sortType.equals(Constants.DESC)) {
            sortedActorsByRating.sort((Actor a1, Actor a2) -> a2.getName().compareTo(a1.getName()));
            sortedActorsByRating.sort((Actor a1, Actor a2) -> a2.getAverageRating().compareTo(a1.getAverageRating()));
        }

        sortedActorsByRating.removeIf(actor -> actor.getAverageRating().equals(0.0));

        StringBuilder actorsListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < sortedActorsByRating.size(); ++i) {

                if(actorsListString.length() > 0) {
                    actorsListString.append(", ");
                }
                actorsListString.append(sortedActorsByRating.get(i).getName());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + actorsListString.toString() + "]"));

    }


    public static void awardsActors(ActionInputData action, Entities entities,
                                    JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Actor> actors = entities.getActors();

        List<String> awards = action.getFilters().get(3);
        String sortType = action.getSortType();

        Map<Actor, Integer> filteredActorsByAwards = new HashMap<>();
        for(Actor actor: actors) {
            boolean hasAllAwards = true;
            int numberOfAwards = 0;

            // Check for all awards
            for(String award: awards) {
                if(!actor.getAwards().containsKey(Utils.stringToAwards(award))) {
                    hasAllAwards = false;
                    break;
                }
            }
            numberOfAwards = actor.getNumberOfAllAwards();

            if(hasAllAwards) {
                filteredActorsByAwards.put(actor, numberOfAwards);
            }
        }

        // Sort using lambda (comparator)
        TreeMap<Actor, Integer> treeMap = new TreeMap<>((o1, o2) -> {
            if(sortType.equals(Constants.DESC)) {
                if(o1.getNumberOfAllAwards() < o2.getNumberOfAllAwards()) {
                    return 1;
                }

                if(o1.getNumberOfAllAwards() > o2.getNumberOfAllAwards()) {
                    return -1;
                }

                return o2.getName().compareTo(o1.getName());
            } else {
                if(o1.getNumberOfAllAwards() < o2.getNumberOfAllAwards()) {
                    return -1;
                }

                if(o1.getNumberOfAllAwards() > o2.getNumberOfAllAwards()) {
                    return 1;
                }

                return o1.getName().compareTo(o2.getName());
            }
        });
        treeMap.putAll(filteredActorsByAwards);

        StringBuilder actorsListString = new StringBuilder();
        for(Map.Entry<Actor, Integer> entry: treeMap.entrySet()) {
            if(actorsListString.length() > 0) {
                actorsListString.append(", ");
            }
            actorsListString.append(entry.getKey().getName());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + actorsListString.toString() + "]"));
    }


    public static void filterDescriptionActors(ActionInputData action, Entities entities,
                                               JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Actor> actors = entities.getActors();

        List<String> filterWords = action.getFilters().get(2);
        String sortType = action.getSortType();

        List<Actor> filteredActors = new ArrayList<>();

        for(Actor actor: actors) {
            boolean containsAllWords = true;

            for(String word: filterWords) {
                // Check case-insensitive && and whole-match
                if(!Pattern.compile(".*\\b" + Pattern.quote(word) + "\\b.*", Pattern.CASE_INSENSITIVE).
                        matcher(actor.getCareerDescription().toLowerCase())
                        .find()) {
                    containsAllWords = false;
                    break;
                }
            }

            if(containsAllWords) {
                filteredActors.add(actor);
            }
        }

        if(sortType.equals(Constants.ASC)) {
            filteredActors.sort((Actor a1, Actor a2) -> a1.getName().compareTo(a2.getName()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredActors.sort((Actor a1, Actor a2) -> a2.getName().compareTo(a1.getName()));
        }

        StringBuilder actorsListString = new StringBuilder();
        for(Actor filteredActor: filteredActors) {
            if(actorsListString.length() > 0) {
                actorsListString.append(", ");
            }
            actorsListString.append(filteredActor.getName());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + actorsListString.toString() + "]"));

    }



    /** Movies */

    public static List<Movie> filterMoviesByTitle(String title, List<Movie> movies) {
        return movies.stream()
                .filter(movie -> movie.getTitle().equals(title))
                .collect(Collectors.toList());
    }

    public static List<Movie> filterMoviesByYear(int year, List<Movie> movies) {
        return movies.stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }

    public static List<Movie> filterMoviesByGenre(String genre, List<Movie> movies) {
        return movies.stream()
                .filter(movie -> movie.getGenres().
                        stream().
                        anyMatch(g -> g.contains(genre)))
                .collect(Collectors.toList());
    }

    public static double calculateAverageOfList(List<Double> ratings) {
        return ratings.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);
    }

    public static void ratingMovies(ActionInputData action, Entities entities,
                                    JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Movie> movies = entities.getMovies();

        // Set average rating for each movie
        for(Movie movie: movies) {
            movie.setAverageRating(calculateAverageOfList(movie.getRatings()));
        }

        // Filters
        String year =  action.getFilters().get(0).get(0);
        String genre =  action.getFilters().get(1).get(0);

        List<Movie> filteredMovies = new ArrayList<>(movies);
        if(year != null && genre == null) {
            // Filter only by year
            filteredMovies = filterMoviesByYear(Integer.parseInt(year), movies);
        }

        if(genre != null && year == null) {
            // Filter only by genre
            filteredMovies = filterMoviesByGenre(genre, movies);
        }

        if(year != null && genre != null) {
            // Filter by year and genre
            filteredMovies = filterMoviesByYear(Integer.parseInt(year), movies);
            filteredMovies = filterMoviesByGenre(genre, filteredMovies);
        }


        // Exclude movies with 0.0 averageRating
        filteredMovies.removeIf(movie -> movie.getAverageRating().equals(0.0));


        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredMovies.sort((Movie m1, Movie m2) -> m1.getTitle().compareTo(m2.getTitle()));
            filteredMovies.sort((Movie m1, Movie m2) -> m1.getAverageRating().compareTo(m2.getAverageRating()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredMovies.sort((Movie m1, Movie m2) -> m2.getTitle().compareTo(m1.getTitle()));
            filteredMovies.sort((Movie m1, Movie m2) -> m2.getAverageRating().compareTo(m1.getAverageRating()));
        }

        // List the first N movies
        StringBuilder moviesListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredMovies.size(); ++i) {
            if(moviesListString.length() > 0) {
                moviesListString.append(", ");
            }
            moviesListString.append(filteredMovies.get(i).getTitle());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + moviesListString.toString() + "]"));
    }


    public static int countStarsForMovie(Movie movie, List<User> users) {
        int starsCounter = 0;
        for(User user: users) {
            List<Movie> favoriteMovies = user.getMoviesFavorite();

            if(favoriteMovies.contains(movie)) {
                starsCounter++;
            }
        }

        return starsCounter;
    }

    public static void favoriteMovies(ActionInputData action, Entities entities,
                                      JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {
        List<Movie> movies = entities.getMovies();

        // Filters
        String year =  action.getFilters().get(0).get(0);
        String genre =  action.getFilters().get(1).get(0);

        List<Movie> filteredMovies = new ArrayList<>(movies);
        if(year != null && genre == null) {
            // Filter only by year
            filteredMovies = filterMoviesByYear(Integer.parseInt(year), filteredMovies);
        }

        if(genre != null && year == null) {
            // Filter only by genre
            filteredMovies = filterMoviesByGenre(genre, filteredMovies);
        }

        if(year != null && genre != null) {
            // Filter by year and genre
            filteredMovies = filterMoviesByYear(Integer.parseInt(year), filteredMovies);
            filteredMovies = filterMoviesByGenre(genre, filteredMovies);
        }


        // For each movie, compute and set the number of stars from users (favoriteCount)
        for(Movie movie: movies) {
            movie.setFavoriteCount(countStarsForMovie(movie, entities.getUsers()));
        }

        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredMovies.sort((Movie m1, Movie m2) -> m1.getTitle().compareTo(m2.getTitle()));
            filteredMovies.sort((Movie m1, Movie m2) -> m1.getFavoriteCount().compareTo(m2.getFavoriteCount()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredMovies.sort((Movie m1, Movie m2) -> m2.getTitle().compareTo(m1.getTitle()));
            filteredMovies.sort((Movie m1, Movie m2) -> m2.getFavoriteCount().compareTo(m1.getFavoriteCount()));
        }

        // List the first N movies
        StringBuilder moviesListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredMovies.size(); ++i) {
            if(filteredMovies.get(i).getFavoriteCount() == 0) {
                continue;
            }

            if(moviesListString.length() > 0) {
                moviesListString.append(", ");
            }
            moviesListString.append(filteredMovies.get(i).getTitle());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + moviesListString.toString() + "]"));
    }


    public static void longestMovies(ActionInputData action, Entities entities,
                                     JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Movie> movies = entities.getMovies();

        // Filters
        String year =  action.getFilters().get(0).get(0);
        String genre =  action.getFilters().get(1).get(0);

        List<Movie> filteredMovies = new ArrayList<>(movies);
        if(year != null && genre == null) {
            // Filter only by year
            filteredMovies = filterMoviesByYear(Integer.parseInt(year), filteredMovies);
        }

        if(genre != null && year == null) {
            // Filter only by genre
            filteredMovies = filterMoviesByGenre(genre, filteredMovies);
        }

        if(year != null && genre != null) {
            // Filter by year and genre
            filteredMovies = filterMoviesByYear(Integer.parseInt(year), filteredMovies);
            filteredMovies = filterMoviesByGenre(genre, filteredMovies);
        }


        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredMovies.sort((Movie m1, Movie m2) -> m1.getTitle().compareTo(m2.getTitle()));
            filteredMovies.sort((Movie m1, Movie m2) -> m1.getDuration().compareTo(m2.getDuration()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredMovies.sort((Movie m1, Movie m2) -> m2.getTitle().compareTo(m1.getTitle()));
            filteredMovies.sort((Movie m1, Movie m2) -> m2.getDuration().compareTo(m1.getDuration()));
        }

        // List the first N movies
        StringBuilder moviesListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredMovies.size(); ++i) {
            if(moviesListString.length() > 0) {
                moviesListString.append(", ");
            }
            moviesListString.append(filteredMovies.get(i).getTitle());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + moviesListString.toString() + "]"));
    }


    public static void mostViewedMovies(ActionInputData action, Entities entities,
                                        JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Movie> movies = entities.getMovies();

        // Filters
        String year =  action.getFilters().get(0).get(0);
        String genre =  action.getFilters().get(1).get(0);

        List<Movie> filteredMovies = new ArrayList<>(movies);
        if(year != null && genre == null) {
            // Filter only by year
            filteredMovies = filterMoviesByYear(Integer.parseInt(year), filteredMovies);
        }

        if(genre != null && year == null) {
            // Filter only by genre
            filteredMovies = filterMoviesByGenre(genre, filteredMovies);
        }

        if(year != null && genre != null) {
            // Filter by year and genre
            filteredMovies = filterMoviesByYear(Integer.parseInt(year), filteredMovies);
            filteredMovies = filterMoviesByGenre(genre, filteredMovies);
        }

        // Set number of views for each movie
        List<User> users = entities.getUsers();
        for(Movie movie: filteredMovies) {
            int numberOfViews = 0;
            // Iterate through each user map of historyMovies <Movie, Integer>
            for(User user: users) {
                if(user.getMoviesHistory().containsKey(movie)) {
                    numberOfViews += user.getMoviesHistory().get(movie);
                }
            }

            movie.setNumberOfViews(numberOfViews);
        }

        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredMovies.sort((Movie m1, Movie m2) -> m1.getTitle().compareTo(m2.getTitle()));
            filteredMovies.sort((Movie m1, Movie m2) -> m1.getNumberOfViews().compareTo(m2.getNumberOfViews()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredMovies.sort((Movie m1, Movie m2) -> m2.getTitle().compareTo(m1.getTitle()));
            filteredMovies.sort((Movie m1, Movie m2) -> m2.getNumberOfViews().compareTo(m1.getNumberOfViews()));
        }

        // List the first N movies
        StringBuilder moviesListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredMovies.size(); ++i) {
            if(filteredMovies.get(i).getNumberOfViews() == 0) {
                continue;
            }

            if(moviesListString.length() > 0) {
                moviesListString.append(", ");
            }
            moviesListString.append(filteredMovies.get(i).getTitle());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + moviesListString.toString() + "]"));
    }



    /** Shows (Serials) */
    public static List<Show> filterShowsByTitle(String title, List<Show> shows) {
        return shows.stream()
                .filter(show -> show.getTitle().equals(title))
                .collect(Collectors.toList());
    }

    public static List<Show> filterShowsByYear(int year, List<Show> shows) {
        return shows.stream()
                .filter(show -> show.getYear() == year)
                .collect(Collectors.toList());
    }

    public static List<Show> filterShowsByGenre(String genre, List<Show> shows) {
        return shows.stream()
                .filter(show -> show.getGenres().
                        stream().
                        anyMatch(g -> g.contains(genre)))
                .collect(Collectors.toList());
    }

    public static void ratingShows(ActionInputData action, Entities entities,
                                   JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Show> shows = entities.getShows();

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

        // Filters
        String year =  action.getFilters().get(0).get(0);
        String genre =  action.getFilters().get(1).get(0);

        if(year != null && genre == null) {
            // Filter only by year
            filteredShows = filterShowsByYear(Integer.parseInt(year), filteredShows);
        }

        if(genre != null && year == null) {
            // Filter only by genre
            filteredShows = filterShowsByGenre(genre, filteredShows);
        }

        if(year != null && genre != null) {
            // Filter by year and genre
            filteredShows = filterShowsByYear(Integer.parseInt(year), filteredShows);
            filteredShows = filterShowsByGenre(genre, filteredShows);
        }

        // Exclude shows with 0.0 averageRating
        filteredShows.removeIf(show -> show.getAverageRating().equals(0.0));

        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredShows.sort((Show s1, Show s2) -> s1.getTitle().compareTo(s2.getTitle()));
            filteredShows.sort((Show s1, Show s2) -> s1.getAverageRating().compareTo(s2.getAverageRating()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredShows.sort((Show s1, Show s2) -> s2.getTitle().compareTo(s1.getTitle()));
            filteredShows.sort((Show s1, Show s2) -> s2.getAverageRating().compareTo(s1.getAverageRating()));
        }

        // List the first N shows
        StringBuilder showsListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredShows.size(); ++i) {
            if(showsListString.length() > 0) {
                showsListString.append(", ");
            }
            showsListString.append(filteredShows.get(i).getTitle());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + showsListString.toString() + "]"));
    }


    public static int countStarsForShow(Show show, List<User> users) {
        int starsCounter = 0;
        for(User user: users) {
            List<Show> favoriteShows = user.getShowsFavorite();

            if(favoriteShows.contains(show)) {
                starsCounter++;
            }
        }

        return starsCounter;
    }

    public static void favoriteShows(ActionInputData action, Entities entities,
                                     JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Show> shows = entities.getShows();

        // Filters
        String year =  action.getFilters().get(0).get(0);
        String genre =  action.getFilters().get(1).get(0);

        List<Show> filteredShows = new ArrayList<>(shows);
        if(year != null && genre == null) {
            // Filter only by year
            filteredShows = filterShowsByYear(Integer.parseInt(year), filteredShows);
        }

        if(genre != null && year == null) {
            // Filter only by genre
            filteredShows = filterShowsByGenre(genre, filteredShows);
        }

        if(year != null && genre != null) {
            // Filter by year and genre
            filteredShows = filterShowsByYear(Integer.parseInt(year), filteredShows);
            filteredShows = filterShowsByGenre(genre, filteredShows);
        }


        // For each show, compute and set the number of stars from users (favoriteCount)
        for(Show show: shows) {
            show.setFavoriteCount(countStarsForShow(show, entities.getUsers()));
        }

        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredShows.sort((Show s1, Show s2) -> s1.getTitle().compareTo(s2.getTitle()));
            filteredShows.sort((Show s1, Show s2) -> s1.getFavoriteCount().compareTo(s2.getFavoriteCount()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredShows.sort((Show s1, Show s2) -> s2.getTitle().compareTo(s1.getTitle()));
            filteredShows.sort((Show s1, Show s2) -> s2.getFavoriteCount().compareTo(s1.getFavoriteCount()));
        }

        // List the first N shows
        StringBuilder showsListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredShows.size(); ++i) {
            if(filteredShows.get(i).getFavoriteCount() == 0) {
                continue;
            }

            if(showsListString.length() > 0) {
                showsListString.append(", ");
            }
            showsListString.append(filteredShows.get(i).getTitle());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + showsListString.toString() + "]"));
    }


    public static void longestShows(ActionInputData action, Entities entities,
                                    JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Show> shows = entities.getShows();

        // Filters
        String year =  action.getFilters().get(0).get(0);
        String genre =  action.getFilters().get(1).get(0);

        List<Show> filteredShows = new ArrayList<>(shows);
        if(year != null && genre == null) {
            // Filter only by year
            filteredShows = filterShowsByYear(Integer.parseInt(year), filteredShows);
        }

        if(genre != null && year == null) {
            // Filter only by genre
            filteredShows = filterShowsByGenre(genre, filteredShows);
        }

        if(year != null && genre != null) {
            // Filter by year and genre
            filteredShows = filterShowsByYear(Integer.parseInt(year), filteredShows);
            filteredShows = filterShowsByGenre(genre, filteredShows);
        }


        // For each show, compute and set the totalDuration
        for(Show show: shows) {
            int totalDuration = 0;
            List<Season> seasons = show.getSeasons();
            for(Season season: seasons) {
                totalDuration += season.getDuration();
            }

            show.setTotalDuration(totalDuration);
        }

        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredShows.sort((Show s1, Show s2) -> s1.getTitle().compareTo(s2.getTitle()));
            filteredShows.sort((Show s1, Show s2) -> s1.getTotalDuration().compareTo(s2.getTotalDuration()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredShows.sort((Show s1, Show s2) -> s2.getTitle().compareTo(s1.getTitle()));
            filteredShows.sort((Show s1, Show s2) -> s2.getTotalDuration().compareTo(s1.getTotalDuration()));
        }

        // List the first N shows
        StringBuilder showsListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredShows.size(); ++i) {
            if(showsListString.length() > 0) {
                showsListString.append(", ");
            }
            showsListString.append(filteredShows.get(i).getTitle());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + showsListString.toString() + "]"));
    }


    public static void mostViewedShows(ActionInputData action, Entities entities,
                                       JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<Show> shows = entities.getShows();

        // Filters
        String year =  action.getFilters().get(0).get(0);
        String genre =  action.getFilters().get(1).get(0);

        List<Show> filteredShows = new ArrayList<>(shows);
        if(year != null && genre == null) {
            // Filter only by year
            filteredShows = filterShowsByYear(Integer.parseInt(year), filteredShows);
        }

        if(genre != null && year == null) {
            // Filter only by genre
            filteredShows = filterShowsByGenre(genre, filteredShows);
        }

        if(year != null && genre != null) {
            // Filter by year and genre
            filteredShows = filterShowsByYear(Integer.parseInt(year), filteredShows);
            filteredShows = filterShowsByGenre(genre, filteredShows);
        }


        // For each show, compute and set the numberOfViews
        List<User> users = entities.getUsers();
        for(Show show: shows) {
            int numberOfViews = 0;
            // Iterate through each user map of historyShows <Show, Integer>
            for(User user: users) {
                if(user.getShowsHistory().containsKey(show)) {
                    numberOfViews += user.getShowsHistory().get(show);
                }
            }

            show.setNumberOfViews(numberOfViews);
        }

        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredShows.sort((Show s1, Show s2) -> s1.getTitle().compareTo(s2.getTitle()));
            filteredShows.sort((Show s1, Show s2) -> s1.getNumberOfViews().compareTo(s2.getNumberOfViews()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredShows.sort((Show s1, Show s2) -> s2.getTitle().compareTo(s1.getTitle()));
            filteredShows.sort((Show s1, Show s2) -> s2.getNumberOfViews().compareTo(s1.getNumberOfViews()));
        }

        // List the first N shows
        StringBuilder showsListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredShows.size(); ++i) {
            if(filteredShows.get(i).getNumberOfViews() == 0) {
                continue;
            }

            if(showsListString.length() > 0) {
                showsListString.append(", ");
            }
            showsListString.append(filteredShows.get(i).getTitle());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + showsListString.toString() + "]"));
    }



    /** Users */
    public static void ratingUsers(ActionInputData action, Entities entities,
                                   JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        List<User> users = entities.getUsers();

        List<User> filteredUsers = new ArrayList<>(users);
        // For each user, compute and set the numberOfRatings
        for(User user: filteredUsers) {
            int numberOfMoviesRated = user.getMoviesRating().size();
            int numberOfSeasonsRated = user.getSeasonRating().size();
            user.setNumberOfRatings(numberOfMoviesRated + numberOfSeasonsRated);
        }

        String sortType = action.getSortType();
        // Sort ASC or Desc
        if(sortType.equals(Constants.ASC)) {
            filteredUsers.sort((User u1, User u2) -> u1.getUsername().compareTo(u2.getUsername()));
            filteredUsers.sort((User u1, User u2) -> u1.getNumberOfRatings().compareTo(u2.getNumberOfRatings()));
        }

        if(sortType.equals(Constants.DESC)) {
            filteredUsers.sort((User u1, User u2) -> u2.getUsername().compareTo(u1.getUsername()));
            filteredUsers.sort((User u1, User u2) -> u2.getNumberOfRatings().compareTo(u1.getNumberOfRatings()));
        }

        // Exclude users with 0 ratings given
        filteredUsers.removeIf(user -> user.getNumberOfRatings().equals(0));

        // List the first N users
        StringBuilder usersListString = new StringBuilder();
        for(int i = 0; i < action.getNumber() && i < filteredUsers.size(); ++i) {
            if(usersListString.length() > 0) {
                usersListString.append(", ");
            }
            usersListString.append(filteredUsers.get(i).getUsername());
        }

        jsonArrayOutput.add(fileWriter.writeFile(
                action.getActionId(),
                "",
                "Query result: [" + usersListString.toString() + "]"));
    }
}