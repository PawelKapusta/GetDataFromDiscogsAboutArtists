package com.company;
import java.net.URL;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import org.json.*;

public class Main {
    public static String getNeededDataFromServer(String httpAddress) throws IOException{
        BufferedReader br = null;
        String returned = null, readerLine = null;
        try {
            StringBuilder sb = new StringBuilder();
            URL address = new URL(httpAddress);
            HttpURLConnection connection = (HttpURLConnection) address.openConnection();
            connection.setRequestProperty("User-Agent", "getDataAboutGivenMusicGroupAndArtists");
            if (connection.getResponseCode() != 200) {
                System.out.println("Błąd HTTP " + connection.getResponseCode());
                if(connection.getResponseCode() == 429){
                    System.out.println("Jest to błąd tzw. rate limit, zbyt szybko chcesz pozyskać znowu dane, " +
                            "spróbuj ponownie za ok. 1 - 2 min, wtedy serwer powinien zacząć odpowiadać!");
                }else if(connection.getResponseCode() == 401){
                    System.out.println("Sprawdź podany przez Ciebie adres URL w kodzie!");
                }
                System.exit(0);
            }
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((readerLine = br.readLine()) != null) {
                sb.append(readerLine).append("\n");
            }
            returned = sb.toString();
        }catch (IOException e){
            System.out.println("Bląd z: " + e.getMessage());
            System.exit(0);
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Błąd z: " + e.getMessage());
                    System.out.println("Błąd zamknięcia odczytu z serwera!");
                    System.exit(0);
                }
            }
        }
        return returned;
    }


    public static void main(String[] args) {
        String musicGroup = "";
        if (args.length == 0) {
            System.out.println("Brak podanej nazwy zespołu lub jego kodu z platformy Discogs");
            System.out.println("Przykładowo można wpisać zespół: Budka Suflera lub jej odpowiadający kod: a359282");
            System.exit(0);
        }else if(args.length > 1){
           for (int index = 0; index < args.length; index++){
               musicGroup += args[index];
           }
            }else{
            musicGroup = args[0];
        }

        try {
            Map<LinkedList<String>, String> groupsAndArtists = new HashMap<>();
            Map<String, LinkedList<String>> mapResult = new HashMap<>();
            LinkedList<String> artistList = new LinkedList<>();
            String dataFromServer = getNeededDataFromServer("https://api.discogs.com/database/search?q="
                    + musicGroup +"&type=artist&token=ZRplFPnNZchVRSYXRQbbRuNHRnMDyFefVLimlFuB");
            JSONObject jsonObject = new JSONObject(dataFromServer);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            dataFromServer = getNeededDataFromServer("https://api.discogs.com/artists/" + (int) jsonArray.getJSONObject(0).get("id"));
            jsonObject = new JSONObject(dataFromServer);
            String myBand = jsonObject.get("name").toString();
            jsonArray = jsonObject.getJSONArray("members");

            for (int index = 0; index < jsonArray.length(); index++) {
                LinkedList<String> listGroups = new LinkedList<>();
                String artist = jsonArray.getJSONObject(index).get("name").toString();
                dataFromServer = getNeededDataFromServer("https://api.discogs.com/artists/" +  (int) jsonArray.getJSONObject(index).get("id"));
                jsonObject = new JSONObject(dataFromServer);
                JSONArray groupsArray = jsonObject.getJSONArray("groups");
                for (int secondIndex = 0; secondIndex < groupsArray.length(); secondIndex++) {
                    listGroups.add(groupsArray.getJSONObject(secondIndex).get("name").toString());
                }
                groupsAndArtists.put(listGroups, artist);
            }


            for (var first : groupsAndArtists.entrySet()) {
                for (var second : groupsAndArtists.entrySet()) {
                    if (!second.getValue().equals(first.getValue())) {
                        var searchingList = new LinkedList<>(first.getKey());
                        searchingList.retainAll(second.getKey());
                            for (var currentGroup : searchingList) {
                                if (!currentGroup.equals(myBand)){
                                    if (!mapResult.containsKey(currentGroup)) {
                                        artistList.add(first.getValue());
                                        artistList.add(second.getValue());
                                        mapResult.put(currentGroup, artistList);
                                        artistList = new LinkedList<>();
                                    } else {
                                        if (!mapResult.get(currentGroup).contains(first.getValue())) {
                                            mapResult.get(currentGroup).add(first.getValue());
                                        }
                                        if (!mapResult.get(currentGroup).contains(second.getValue())) {
                                            mapResult.get(currentGroup).add(second.getValue());
                                        }
                                    }
                                }
                        }
                    }
                }
            }


            System.out.println();
            System.out.println("Wspólne zespoły:");
            for (var band : mapResult.entrySet()) {
                System.out.println();
                System.out.println("Zespół: " + band.getKey());
                System.out.println("Grali w nim razem: ");
                System.out.println(band.getValue());
            }
        }catch (MalformedURLException e) {
            System.out.println("Błąd z: " + e.getMessage());
            System.out.println("Sprawdź podany przez Ciebie adres URL w kodzie!");
            System.exit(0);
        }catch(IOException e){
            System.out.println("Błąd z: " + e.getMessage());
            System.out.println("Jest to błąd tzw. rate limit, zbyt szybko chcesz pozyskać znowu dane, " +
                    "spróbuj ponownie za ok. 1 - 2 min, wtedy serwer powinien zacząć odpowiadać!");
            System.exit(0);
        }

    }









}











































