package com.example.fakedatabase.service;

import com.example.fakedatabase.model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    public User authenticateUser(String email, String password) throws UsernameNotFoundException {
        Path path = Paths.get("src/main/java/com/example/fakedatabase/db/UsersForAuthenticate.json");
        try {
            String lines = String.join("", Files.readAllLines(path));
            JSONArray db = new JSONArray(lines);
            OptionalInt index = IntStream.range(0, db.length())
                    .filter(_index -> db.getJSONObject(_index).get("email").equals(email) && db.getJSONObject(_index).get("password").equals(password))
                    .findFirst();
            User user = null;
            if (index.isPresent()) {
                JSONObject userJson = db.getJSONObject(index.getAsInt());
                user = new User(String.valueOf(userJson.get("id")), (String) userJson.get("email"));
            }

            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }
            return user;
        } catch (IOException e) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }

    public User authenticateUserFromId(String id) {
        Path path = Paths.get("src/main/java/com/example/fakedatabase/db/UsersForAuthenticate.json");
        try {
            String lines = String.join("", Files.readAllLines(path));
            JSONArray db = new JSONArray(lines);
            OptionalInt index = IntStream.range(0, db.length())
                    .filter(_index -> db.getJSONObject(_index).get("id").equals(id))
                    .findFirst();
            User user = null;
            if (index.isPresent()) {
                JSONObject userJson = db.getJSONObject(index.getAsInt());
                user = new User(String.valueOf(userJson.get("id")), (String) userJson.get("email"));
            }

            if (user == null) {
                throw new UsernameNotFoundException("User not found with id: " + id);
            }
            return user;
        } catch (IOException e) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}