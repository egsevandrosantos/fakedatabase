package com.example.fakedatabase.controller;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class GenericController {
    @GetMapping("/generic/{model}")
    @ResponseBody
    public ResponseEntity<String> getGeneric(
            @PathVariable String model,
            @RequestParam(required = false) Integer firstResult,
            @RequestParam(required = false) Integer maxResult,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false, defaultValue = "ASC") String order,
            @RequestParam(required = false) String where // "prop=value,..."
    ) {
        String modelCapitalize = StringUtils.capitalize(model);
        try {
            Path path = Paths.get("src/main/java/com/example/fakedatabase/db/" + modelCapitalize + ".json");
            String lines = String.join("", Files.readAllLines(path));
            JSONArray db = new JSONArray(lines);

            if (StringUtils.isNotBlank(where)) {
                JSONArray finalDb = db;
                String[] propsValues = where.split(",");
                int[] indexValids = IntStream.range(0, db.length())
                        .filter(_index -> {
                            List<Boolean> validations = new ArrayList<>();
                            for (String propValue : propsValues) {
                                String prop = propValue.split("=")[0];
                                String value = propValue.split("=")[1];
                                JSONObject obj = finalDb.getJSONObject(_index);
                                validations.add(obj.get(prop).equals(value));
                            }
                            return validations.stream().allMatch(val -> val);
                        })
                        .toArray();
                for (int i = db.length() - 1, l = 0; i >= l; i--) {
                    int index = i;
                    OptionalInt findedIndex = Arrays.stream(indexValids).filter(indexValid -> indexValid == index).findFirst();
                    if (findedIndex.isEmpty()) {
                        finalDb.remove(i);
                    }
                }
            }

            if (StringUtils.isNotBlank(orderBy)) {
                List<JSONObject> objects = new ArrayList<>();
                JSONArray finalDb = db;
                IntStream.range(0, db.length()).forEach(index -> objects.add(finalDb.getJSONObject(index)));
                objects.sort((o1, o2) -> {
                    String orderBy1 = (String) o1.get("orderBy");
                    String orderBy2 = (String) o2.get("orderBy");
                    if (orderBy1 == null) {
                        return -1;
                    }
                    if (orderBy2 == null) {
                        return 1;
                    }
                    return orderBy1.compareTo(orderBy2);
                });
                if ("DESC".equalsIgnoreCase(order)) {
                    List<JSONObject> objectsReversed = IntStream.range(0, objects.size()).map(i -> objects.size() - 1 - i).mapToObj(objects::get).collect(Collectors.toList());
                    db = new JSONArray(objectsReversed);
                } else {
                    db = new JSONArray(objects);
                }
            }

            if (firstResult != null && firstResult > 0) {
                List<JSONObject> objects = new ArrayList<>();
                JSONArray finalDb = db;
                List<JSONObject> finalObjects = objects;
                IntStream.range(0, db.length()).forEach(index -> finalObjects.add(finalDb.getJSONObject(index)));
                if (firstResult > objects.size()) {
                    objects = new ArrayList<>();
                } else {
                    objects = objects.subList(firstResult, objects.size());
                }
                db = new JSONArray(objects);
            }

            if (maxResult != null && maxResult > 0) {
                List<JSONObject> objects = new ArrayList<>();
                JSONArray finalDb = db;
                List<JSONObject> finalObjects = objects;
                IntStream.range(0, db.length()).forEach(index -> finalObjects.add(finalDb.getJSONObject(index)));
                objects = objects.subList(0, Math.min(maxResult, objects.size()));
                db = new JSONArray(objects);
            }

            return ResponseEntity.ok().body(db.toString());
        } catch (IOException e) {
            return ResponseEntity.ok().body(new JSONArray().toString());
        }
    }

    @PostMapping("/generic/{model}")
    @ResponseBody
    public ResponseEntity<String> postGeneric(@PathVariable String model, @RequestBody String bodyStr) throws IOException, URISyntaxException {
        String modelCapitalize = StringUtils.capitalize(model);
        Path path = Paths.get("src/main/java/com/example/fakedatabase/db/" + modelCapitalize + ".json");
        JSONArray db;
        JSONObject body = new JSONObject(bodyStr);
        try {
            String lines = String.join("", Files.readAllLines(path));
            db = new JSONArray(lines);
        } catch (IOException e) {
            db = new JSONArray();
        }

        String id = "1";
        if (db.length() > 0) {
            JSONObject last = db.getJSONObject(db.length() - 1);
            id = String.valueOf(Integer.parseInt(String.valueOf(last.get("id"))) + 1);
        }
        body.put("id", id);

        db.put(body);
        Files.write(path, db.toString().getBytes());
        return ResponseEntity.created(new URI("http://localhost:8080/generic/" + model + "/" + id)).build();
    }

    @GetMapping("/generic/{model}/{id}")
    @ResponseBody
    public ResponseEntity<String> getSpecificGeneric(@PathVariable String model, @PathVariable String id) {
        String modelCapitalize = StringUtils.capitalize(model);
        try {
            Path path = Paths.get("src/main/java/com/example/fakedatabase/db/" + modelCapitalize + ".json");
            String lines = String.join("", Files.readAllLines(path));
            JSONArray db = new JSONArray(lines);
            OptionalInt index = IntStream.range(0, db.length())
                    .filter(_index -> {
                        JSONObject object = db.getJSONObject(_index);
                        return id.equals(object.get("id"));
                    })
                    .findFirst();
            if (index.isPresent()) {
                return ResponseEntity.ok(db.getJSONObject(index.getAsInt()).toString());
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/generic/{model}/{id}")
    @ResponseBody
    public ResponseEntity<String> putGeneric(@PathVariable String model, @PathVariable String id, @RequestBody String bodyStr) {
        String modelCapitalize = StringUtils.capitalize(model);
        try {
            Path path = Paths.get("src/main/java/com/example/fakedatabase/db/" + modelCapitalize + ".json");
            String lines = String.join("", Files.readAllLines(path));
            JSONArray db = new JSONArray(lines);
            OptionalInt index = IntStream.range(0, db.length())
                    .filter(_index -> {
                        JSONObject object = db.getJSONObject(_index);
                        return id.equals(object.get("id"));
                    })
                    .findFirst();
            if (index.isPresent()) {
                JSONObject body = new JSONObject(bodyStr);
                db.put(index.getAsInt(), body);
                Files.write(path, db.toString().getBytes());
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/generic/{model}/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteGeneric(@PathVariable String model, @PathVariable String id) {
        String modelCapitalize = StringUtils.capitalize(model);
        try {
            Path path = Paths.get("src/main/java/com/example/fakedatabase/db/" + modelCapitalize + ".json");
            String lines = String.join("", Files.readAllLines(path));
            JSONArray db = new JSONArray(lines);
            OptionalInt index = IntStream.range(0, db.length())
                    .filter(_index -> {
                        JSONObject object = db.getJSONObject(_index);
                        return id.equals(object.get("id"));
                    })
                    .findFirst();
            if (index.isPresent()) {
                db.remove(index.getAsInt());
                Files.write(path, db.toString().getBytes());
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}