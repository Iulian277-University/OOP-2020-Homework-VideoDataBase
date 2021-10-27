package main;

import actions.ProcessAction;
import actor.Actor;
import checker.Checkstyle;
import checker.Checker;
import common.Constants;
import entities.Entities;
import fileio.*;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import converters.*;

import user.User;
import video.*;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * Call the main checker and the coding style checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(Constants.TESTS_PATH);
        Path path = Paths.get(Constants.RESULT_PATH);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        File outputDirectory = new File(Constants.RESULT_PATH);

        Checker checker = new Checker();
        checker.deleteFiles(outputDirectory.listFiles());

        for (File file : Objects.requireNonNull(directory.listFiles())) {

            String filepath = Constants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getAbsolutePath(), filepath);
            }
        }

        checker.iterateFiles(Constants.RESULT_PATH, Constants.REF_PATH, Constants.TESTS_PATH);
        Checkstyle test = new Checkstyle();
        test.testCheckstyle();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        InputLoader inputLoader = new InputLoader(filePath1);
        Input input = inputLoader.readData();

        /** Convert to my classes */
        // Movies
        MovieInputConverter.convert(input.getMovies());
        List<Movie> movies = MovieInputConverter.getObjMovies();

        // Shows
        ShowInputConverter.convert(input.getSerials());
        List<Show> shows = ShowInputConverter.getObjShows();

        // Actors
        ActorInputConverter.convert(input.getActors(), movies, shows);
        List<Actor> actors = ActorInputConverter.getObjActors();

        // Users
        UserInputConverter.convert(input.getUsers(), movies, shows);
        List<User> users = UserInputConverter.getObjUsers();

        // Encapsulate users, actors, movies, shows
        Entities entities = new Entities(actors, users, movies, shows);


        // Create the writer to the output file
        Writer fileWriter = new Writer(filePath2);
        JSONArray jsonArrayOutput = new JSONArray();

        // Process actions
        List<ActionInputData> actions = input.getCommands();
        ProcessAction.process(actions, entities, jsonArrayOutput, fileWriter);

        // Close the output file
        fileWriter.closeJSON(jsonArrayOutput);

        // Clear everything
        movies.clear();
        shows.clear();
        actors.clear();
        users.clear();

        // Extra protection measure - Bad usage
        // System.gc();
        // Runtime.getRuntime().gc();
    }
}
