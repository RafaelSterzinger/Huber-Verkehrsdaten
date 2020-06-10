package com.example.huber.live;

import com.example.huber.entity.DepartureTime;
import com.example.huber.entity.DirectionInfo;
import com.example.huber.entity.LiveEntry;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GetLiveEntryDeserializer implements JsonDeserializer<List<LiveEntry>> {

    @Override
    public List<LiveEntry> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<LiveEntry> entries = new ArrayList<>();

        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray monitors = jsonObject.get("data").getAsJsonObject().get("monitors").getAsJsonArray();


        for (JsonElement itemsJsonElement : monitors) {
            JsonObject lines = itemsJsonElement.getAsJsonObject().get("lines").getAsJsonArray().get(0).getAsJsonObject();
            JsonArray departure = lines.get("departures").getAsJsonObject().getAsJsonArray("departure");
            ArrayList<DepartureTime> departureTimes = new ArrayList<>();
            for (JsonElement departureTime : departure) {
                JsonObject departureTimeJsonObject = departureTime.getAsJsonObject();
                departureTimes.add(new DepartureTime(
                        departureTimeJsonObject.get("countdown").getAsInt(),
                        departureTimeJsonObject.get("timePlanned").getAsString(),
                        departureTimeJsonObject.get("timeReal").getAsString()
                ));
            }

            DirectionInfo directionInfo = new DirectionInfo(
                    lines.get("direction").getAsCharacter(),
                    lines.get("lineId").getAsInt(),
                    lines.get("name").getAsString(),
                    lines.get("realtimeSupport").getAsBoolean(),
                    lines.get("richtungsId").getAsInt(),
                    lines.get("towards").getAsString(),
                    lines.get("type").getAsString());

            JsonObject locationStop = itemsJsonElement.getAsJsonObject().get("locationStop").getAsJsonObject();
            JsonArray coordinates = locationStop.get("geometry").getAsJsonObject().getAsJsonArray("coordinates");
            LatLng geometry = new LatLng(coordinates.get(0).getAsDouble(), coordinates.get(1).getAsDouble());

            JsonObject properties = locationStop.getAsJsonObject("properties");
            directionInfo.setRbl(properties.getAsJsonObject("attributes").get("rbl").getAsInt());

            LiveEntry liveEntry = new LiveEntry(
                    properties.get("name").getAsInt(),
                    properties.get("title").getAsString(),
                    geometry,
                    directionInfo,
                    departureTimes);

            entries.add(liveEntry);
        }



        return entries;
    }
}
