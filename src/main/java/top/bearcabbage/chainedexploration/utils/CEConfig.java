package top.bearcabbage.chainedexploration.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import top.bearcabbage.chainedexploration.ChainedExploration;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CEConfig extends ChainedExploration {
    private final Path filePath;
    private JsonObject jsonObject;
    private final Gson gson;

    public CEConfig(Path filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.notExists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
                try (FileWriter writer = new FileWriter(filePath.toFile())) {
                    writer.write("{}");
                }
            }

        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
        load();
    }

    public void load() {
        try (FileReader reader = new FileReader(filePath.toFile())) {
            this.jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            this.jsonObject = new JsonObject();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    public void set(String key, int value) {
        jsonObject.addProperty(key, value);
    }

    public void set(String key, double value) {
        jsonObject.addProperty(key, value);
    }

    public void set(String key, boolean value) {
        jsonObject.addProperty(key, value);
    }

    public void set(String key, Map<String, Vector<Integer>> value) {
        jsonObject.add(key, gson.toJsonTree(value));
    }

    public void set(String key, List<Object> value) {
        jsonObject.add(key, gson.toJsonTree(value));
    }

    public void set(String key, Vector<Object> value) {
        jsonObject.add(key, gson.toJsonTree(value));
    }

    public void set(String key, Object value) {
        jsonObject.add(key, gson.toJsonTree(value));
    }

    public void set(String key, String value) {
        jsonObject.addProperty(key, value);
    }

    public int getInt(String key) {
        return jsonObject.get(key).getAsInt();
    }

    public double getDouble(String key) {
        return jsonObject.get(key).getAsDouble();
    }

    public boolean getBoolean(String key) {
        return jsonObject.get(key).getAsBoolean();
    }

    public Map<String, Vector<Integer>> getMap(String key) {
        return gson.fromJson(jsonObject.get(key), Map.class);
    }

    public List<Object> getList(String key) {
        return gson.fromJson(jsonObject.get(key), List.class);
    }

    public Vector<Object> getVector(String key) {
        return gson.fromJson(jsonObject.get(key), Vector.class);
    }

    public String getString(String key) {
        return gson.fromJson(jsonObject.get(key), String.class);
    }

    public <T> T get(String key, Class<T> clazz) {
        return gson.fromJson(jsonObject.get(key), clazz);
    }

    public void close() {
        save();
    }
}
