package converters;
import actor.Actor;
import actor.ActorsAwards;
import fileio.SerialInputData;
import video.Movie;
import video.Show; // Serial
import video.Season;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static converters.MovieInputConverter.filterActorsByName;

public final class ShowInputConverter {

    private ShowInputConverter() {}

    private static List<Show> shows = new ArrayList<>(); // showsConvertedToObjects (serialsConvToObj)

    public static void convert(List<SerialInputData> showsData) {
        for(SerialInputData show: showsData) {
            String title = show.getTitle();
            int year = show.getYear();
            ArrayList<String> genres = show.getGenres();

            // Convert String Actors to Object Actors
            ArrayList<Actor> actorsObj = new ArrayList<>();
            ArrayList<String> actorsString = show.getCast();
            for(String actorString: actorsString) {
                Actor currActor = new Actor(actorString);
                actorsObj.add(currActor);
            }

            // In first iteration of conversion, create the actor only with name
            ArrayList<Season> seasonsObj = new ArrayList<>();
            ArrayList<entertainment.Season> seasons = show.getSeasons();
            int numberOfSeasons = show.getNumberSeason();
            for(entertainment.Season season: seasons) {
                int currentSeason = season.getCurrentSeason();
                int duration = season.getDuration();
                List<Double> ratings = season.getRatings();

                Season currSeason = new Season(currentSeason, duration, ratings);
                seasonsObj.add(currSeason);
            }

            Show showObj = new Show(title, year, actorsObj, genres, numberOfSeasons, seasonsObj);
            shows.add(showObj);
        }
    }

    public static List<Show> getObjShows() {
        return shows;
    }

}
