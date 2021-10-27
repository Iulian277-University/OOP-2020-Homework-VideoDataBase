package actions;

import entities.Entities;
import fileio.ActionInputData;
import common.Constants;
import fileio.Writer;
import org.json.simple.JSONArray;
import user.User;

import converters.UserInputConverter;
import video.Movie;
import video.Season;
import video.Show;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public final class ProcessCommand {

    private ProcessCommand()  {}

    public static void command(ActionInputData action, Entities entities,
                               JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {
        switch(action.getType()) {
            case Constants.FAVORITE -> favorite(action, entities, jsonArrayOutput, fileWriter);
            case Constants.VIEW -> view(action, entities, jsonArrayOutput, fileWriter);
            case Constants.RATING -> rating(action, entities, jsonArrayOutput, fileWriter);
            default -> throw new IllegalStateException("Unexpected value: " + action.getType());
        }
    }


    /** FAVORITE Command */
    public static List<User> filterUsersByName(String username, List<User> users) {
        // Is the username unique?
        // Filter users using streams
        return users.stream().
                filter(user -> user.getUsername().equals(username)).
                collect(Collectors.toList());
    }

    public static void favorite(ActionInputData action, Entities entities,
                                JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        String username = action.getUsername();
        List<User> usersFilteredByName = filterUsersByName(username, entities.getUsers());
        User userWanted = usersFilteredByName.get(0); // Supposing that the username is unique

        String videoTitle = action.getTitle();

        // For movie
        boolean actionDone = false;
        Movie movie = UserInputConverter.filterMoviesByTitle(videoTitle, entities.getMovies());
        if(userWanted.getMoviesHistory().containsKey(movie) &&
            !(userWanted.getMoviesFavorite().contains(movie))) {

            userWanted.getMoviesFavorite().add(movie);
            actionDone = true;
        }

        // For show
        Show show = UserInputConverter.filterShowsByTitle(videoTitle, entities.getShows());
        if(userWanted.getShowsHistory().containsKey(show) &&
                !(userWanted.getShowsFavorite().contains(show))) {

            userWanted.getShowsFavorite().add(show);
            actionDone = true;
        }

        // Print to output
        if(actionDone) {
            jsonArrayOutput.add(fileWriter.writeFile(
                    action.getActionId(), "", "success -> " + videoTitle + " was added as favourite"));
        } else {
            if(userWanted.getMoviesFavorite().contains(movie) || userWanted.getShowsFavorite().contains(show)) {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(), "", "error -> " + videoTitle + " is already in favourite list"));
            } else {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(), "", "error -> " + videoTitle + " is not seen"));
            }

        }
    }


    /** VIEW Command */
    public static void view(ActionInputData action, Entities entities,
                            JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        String username = action.getUsername();
        List<User> usersFilteredByName = filterUsersByName(username, entities.getUsers());
        User userWanted = usersFilteredByName.get(0); // Supposing that the username is unique

        String videoTitle = action.getTitle();

        // Check if the historyMap contains the videoTitle: Increment the number of views

        // For movie
        Movie movie = UserInputConverter.filterMoviesByTitle(videoTitle, entities.getMovies());
        if(movie != null) {
            if(userWanted.getMoviesHistory().containsKey(movie)) {
                userWanted.getMoviesHistory().put(movie, userWanted.getMoviesHistory().get(movie) + 1);
            } else {
                userWanted.getMoviesHistory().put(movie, 1); // Mark the movie as viewed (for the first time)
            }

            jsonArrayOutput.add(fileWriter.writeFile(
                    action.getActionId(),
                    "",
                    "success -> " + movie.getTitle() +
                            " was viewed with total views of " +
                            userWanted.getMoviesHistory().get(movie)));
        }

        // For show
        Show show = UserInputConverter.filterShowsByTitle(videoTitle, entities.getShows());
        if(show != null) {
            if(userWanted.getShowsHistory().containsKey(show)) {
                userWanted.getShowsHistory().put(show, userWanted.getShowsHistory().get(show) + 1);
            } else {
                userWanted.getShowsHistory().put(show, 1); // Mark the show as viewed (for the first time)
            }

            jsonArrayOutput.add(fileWriter.writeFile(
                    action.getActionId(),
                    "",
                    "success -> " + show.getTitle() +
                            " was viewed with total views of " +
                            userWanted.getShowsHistory().get(show)));
        }
    }


    /** RATING Command */
    public static void rating(ActionInputData action, Entities entities,
                              JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {

        String username = action.getUsername();
        List<User> usersFilteredByName = filterUsersByName(username, entities.getUsers());
        User userWanted = usersFilteredByName.get(0); // Supposing that the username is unique

        String videoTitle = action.getTitle();

        // Check if the historyMap contains the videoTitle: Mark the rating

        // For movie
        boolean actionDone = false;
        boolean ratedIt = false;
        Movie movie = UserInputConverter.filterMoviesByTitle(videoTitle, entities.getMovies());
        if(movie != null) {
            // If the user had viewed the movie AND didn't rate it yet
            if(userWanted.getMoviesHistory().containsKey(movie)) {
                if(!(userWanted.getMoviesRating().containsKey(movie))) {

                    movie.addRating(action.getGrade()); // Mark the grade on movie rating list
                    userWanted.addMovieRating(movie, action.getGrade()); // Mark the grade on user movie rating list
                    actionDone = true;
                } else {
                    ratedIt = true;
                }
            }
        }

        // For show
        Show show = UserInputConverter.filterShowsByTitle(videoTitle, entities.getShows());
        if(show != null) {
            // If the user had viewed the show AND didn't rate that specific season yet
            Season season = show.getSeasonFromIndex(action.getSeasonNumber());
            if(userWanted.getShowsHistory().containsKey(show)) {
                if(!(userWanted.getSeasonRating().containsKey(season))) {
                    season.addRating(action.getGrade());
                    userWanted.addSeasonRating(season, action.getGrade());
                    actionDone = true;
                } else {
                    ratedIt = true;
                }

            }
        }

        if(actionDone) {
            jsonArrayOutput.add(fileWriter.writeFile(
                    action.getActionId(),
                    "",
                    "success -> " + videoTitle +
                            " was rated with " + action.getGrade()
                            + " by " + username));
        } else {
            if(ratedIt) {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(),
                        "",
                        "error -> " + videoTitle + " has been already rated"));
            } else {
                jsonArrayOutput.add(fileWriter.writeFile(
                        action.getActionId(),
                        "",
                        "error -> " + videoTitle + " is not seen"));
            }
        }
    }
}
