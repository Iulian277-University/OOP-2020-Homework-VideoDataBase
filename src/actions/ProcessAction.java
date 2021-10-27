package actions;

import common.Constants;
import entities.Entities;
import fileio.ActionInputData;
import fileio.Writer;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.util.List;

public final class ProcessAction {

    private ProcessAction() {}

    public static void process(List<ActionInputData> actions, Entities entities,
                               JSONArray jsonArrayOutput, Writer fileWriter) throws IOException {
        for(ActionInputData action: actions) {

            switch(action.getActionType()) {
                case Constants.COMMAND ->
                        ProcessCommand.command(action, entities, jsonArrayOutput, fileWriter);
                case Constants.QUERY ->
                        ProcessQuery.query(action, entities, jsonArrayOutput, fileWriter);
                case Constants.RECOMMENDATION ->
                        ProcessRecommendation.recommendation(action, entities, jsonArrayOutput, fileWriter);
                default -> throw new IllegalStateException("Unexpected value: " + action.getActionType());
            }

        }
    }
}
