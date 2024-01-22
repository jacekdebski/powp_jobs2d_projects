package edu.kis.powp.jobs2d.command.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import edu.kis.powp.jobs2d.command.utils.entities.JsonCommandList;

import java.util.logging.Logger;

public class JsonCommandParser {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static JsonCommandList parse(JsonReader reader) {
        Gson gson = new Gson();
        JsonCommandList commandList;
        try {
            commandList = gson.fromJson(reader, JsonCommandList.class);
        } catch (JsonSyntaxException e) {
            logger.warning("Invalid json format");
            return null;
        }
        return commandList;
    }
}
