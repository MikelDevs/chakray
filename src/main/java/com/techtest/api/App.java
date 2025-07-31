package com.techtest.api;

import static spark.Spark.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.lang.reflect.Field;

import com.google.gson.Gson;

public class App {
    static List<User> users = new ArrayList<>();

    public static boolean isRfcFormat(String rfc) {
        return rfc != null && rfc.matches("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");
    }

    // i dont know what is the "AndresFormat" but i assume it is a phone format
    public static boolean isPhoneFormat(String phone) {
        if (phone == null)
            return false;
        if (!phone.matches("^\\+\\d{1,3}(\\s?\\d+)+$"))
            return false;

        String digits = String.join("", Arrays.copyOfRange(phone.split(" "), 1, phone.split(" ").length))
                .replaceAll("\\D", "");
        System.out.println(String.join("", Arrays.copyOfRange(phone.split(" "), 1, phone.split(" ").length))
                .replaceAll("\\D", ""));
        return digits.length() == 10;
    }

    public static Comparator<User> getComparator(String sortedBy) {
        switch (sortedBy) {
            case "email":
                return Comparator.comparing(u -> u.email);
            case "id":
                return Comparator.comparing(u -> u.id);
            case "name":
                return Comparator.comparing(u -> u.name);
            case "phone":
                return Comparator.comparing(u -> u.phone);
            case "tax_id":
                return Comparator.comparing(u -> u.tax_id);
            case "created_at":
                return Comparator.comparing(u -> u.created_at);
            default:
                return Comparator.comparing(u -> u.id);
        }
    }

    public static String getFValue(User user, String prop) {
        try {
            Field field = user.getClass().getDeclaredField(prop);
            field.setAccessible(true);
            if (prop.equals("phone")) {
                String value = String.valueOf(field.get(user));
                return value.replaceAll(" ", "").replaceAll("\\+", "");
            } else {
                return String.valueOf(field.get(user));
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Predicate<User> getPredicate(String filter) {
        if (filter == null || filter.isEmpty()) {
            return u -> true;
        }
        String[] parts = filter.split(" ");
        String prop = parts[0];
        String operator = parts[1];
        String value = parts[2];
        switch (operator) {
            case "co":
                return u -> {
                    String fValue = getFValue(u, prop);
                    return fValue.contains(value);
                };
            case "eq":
                return u -> {
                    String fValue = getFValue(u, prop);
                    return fValue.equals(value);
                };
            case "sw":
                return u -> {
                    String fValue = getFValue(u, prop);
                    System.out.println("Comparing: " + fValue + " starts with " + value);
                    return fValue.startsWith(value);
                };
            case "ew":
                return u -> {
                    String fValue = getFValue(u, prop);
                    return fValue.endsWith(value);
                };
            default:
                return u -> true;
        }
    }

    public static void main(String[] args) {
        List<Address> addresses = new ArrayList<>();

        // ADDING DEFAULT DATA
        addresses.add(new Address(1, "Address 1", "Street 1", "Country 1"));
        users.add(new User(
                UUID.randomUUID(),
                "juan.perez@example.com",
                "juan perez",
                "+52 74 859 374 58",
                "7c4a8d09ca3762af61e59520943dc26494f8941b",
                "PEGJ900514ABC",
                Instant.ofEpochMilli(1678828800000L).atZone(ZoneId.of("Indian/Antananarivo"))
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                addresses));
        addresses.clear();
        addresses.add(new Address(1, "Address 1", "Street 1", "Country 1"));
        addresses.add(new Address(2, "Address 2", "Street 2", "Country 2"));
        users.add(new User(
                UUID.randomUUID(),
                "maria.lopez@example.com",
                "maria lopez",
                "+52 67 984 139 59",
                "7c4a8d09ca3762af61e59520943dc26494f8941b",
                "LOSM851123JKL",
                Instant.ofEpochMilli(1677801600000L).atZone(ZoneId.of("Indian/Antananarivo"))
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                addresses));

        addresses.clear();
        addresses.add(new Address(1, "Address 1", "Street 1", "Country 1"));
        addresses.add(new Address(2, "Address 2", "Street 2", "Country 2"));
        users.add(new User(
                UUID.randomUUID(),
                "miguel.valencia@gmail.com",
                "miguel valencia",
                "+52 66 249 643 40",
                "l/ZK1pGpqpqPMTKb4b0bfA==",
                "VALM900514XYZ",
                Instant.ofEpochMilli(1677715200000L).atZone(ZoneId.of("Indian/Antananarivo"))
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                addresses));

        port(8000);

        get("users", (req, res) -> {
            res.type("application/json");
            String sortedBy = req.queryParams("sortedBy");
            String filter = req.queryParams("filter");

            List<User> filteredUsers = users.stream().filter(getPredicate(filter)).collect(Collectors.toList());
            List<User> sortedUsers = new ArrayList<>(filteredUsers);

            sortedUsers.sort(getComparator(sortedBy != null ? sortedBy : "id"));

            List<User> usersWOPassword = sortedUsers.stream().map(user -> {
                User userWOPassword = new User(
                        user.id,
                        user.email,
                        user.name,
                        user.phone,
                        null,
                        user.tax_id,
                        user.created_at,
                        user.addresses);
                return userWOPassword;
            }).collect(Collectors.toList());

            return (new Gson()).toJson(usersWOPassword);
        });

        post("users", (req, res) -> {
            res.type("application/json");
            User newUser = new Gson().fromJson(req.body(), User.class);
            List<User> isTaxUnique = users.stream()
                    .filter(user -> user.tax_id.equals(newUser.tax_id))
                    .collect(Collectors.toList());
            if (!isTaxUnique.isEmpty()) {
                res.status(400);
                return "tax_id must be unique";
            }
            if (!isRfcFormat(newUser.tax_id)) {
                res.status(400);
                return "tax_id must be in RFC format";
            }
            if (!isPhoneFormat(newUser.phone)) {
                res.status(400);
                return "phone must be in Andres format";
            }
            newUser.id = UUID.randomUUID();
            newUser.created_at = Instant.now().atZone(ZoneId.of("Indian/Antananarivo"))
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
            newUser.password = Encrypt.encrypt(newUser.password);
            users.add(newUser);
            return (new Gson()).toJson(newUser);
        });

        put("users/:id", (req, res) -> {
            res.type("application/json");
            UUID id = UUID.fromString(req.params(":id"));
            User newUser = new Gson().fromJson(req.body(), User.class);
            newUser.id = id;
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).id.equals(id)) {
                    newUser.created_at = users.get(i).created_at;
                    users.set(i, newUser);
                    return (new Gson()).toJson(newUser);
                }
            }
            res.status(404);
            return "id not found";
        });

        delete("users/:id", (req, res) -> {
            res.type("application/json");
            UUID id = UUID.fromString(req.params(":id"));
            boolean removed = users.removeIf(user -> user.id.equals(id));
            return (removed ? "deleted" : "id not found");
        });

        post("login", (req, res) -> {
            res.type("application/json");
            User userLogin = new Gson().fromJson(req.body(), User.class);
            String tax_id = userLogin.tax_id;
            String password = userLogin.password;
            for (User user : users) {
                if (user.tax_id.equals(tax_id) && user.password.equals(Encrypt.encrypt(password))) {
                    return "Welcome " + user.name + "!";
                }
            }
            res.status(401);
            return "invalid credentials";
        });
    }
}
