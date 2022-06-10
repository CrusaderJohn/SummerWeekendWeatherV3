// John Jamieson
// 2022/05/24

package ca.johnjamieson.weather;

import org.json.*; // https://mvnrepository.com/artifact/org.json/json
import java.net.*;
import java.io.*;
import java.util.*;

record WeatherDay(boolean missed, int year, int month, int day, Number temp, double rain){}
record WeatherAvg(int month, int day, Number temp, double rain){}

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            int location = 6115811; //6158355;
            double tempSum = 0;
            double rainSum = 0;
            double tempAvg;
            double rainAvg;
            int days = 0;
            int count = 0;
            // Creates an array (days in a year) of arrays (years)
            ArrayList<ArrayList<WeatherDay>> multipleYears = new ArrayList<>();
            // Create array (days in a year) for holding the averages
            ArrayList<WeatherAvg> avgYear = new ArrayList<>();

            int numberYears = 20;
            for (int i = 1; i <= numberYears; i++) {
                multipleYears.add(runProgram(calendar.get(Calendar.YEAR) - i, location));
            }


            for (int i = 1; i < 13; i++)
            {
                for (int j = 1; j < 32; j++)
                {
                    // Loop through each year
                    for (ArrayList<WeatherDay> x : multipleYears) {
                        // Loop through each day in a year
                        for (WeatherDay y : x) {
                            if (!y.missed() && y.day() == j && y.month() == i)
                            {
                                days++;
                                tempSum = tempSum + y.temp().doubleValue();
                                rainSum = rainSum + y.rain();
                            }
                        }
                    }
                    if (days > 0)
                    {
                        tempAvg = tempSum / days;
                        rainAvg = rainSum / days;
                        avgYear.add(new WeatherAvg(i,j,tempAvg,rainAvg));
                        System.out.println("Month: " + i);
                        System.out.println("Day: " + j);
                        System.out.println("Number of days: " + days);
                        System.out.println("Average Temp  : " + tempAvg);
                        System.out.println("Average Rain  : " + rainAvg);
                        System.out.println();
                        count++;
                    }
                    days = 0;
                    tempSum = 0;
                    rainSum = 0;
                }
            }
            System.out.println("Day Count (should be 366): " + count);
            System.out.println();
            double lowestTemp = 100;
            int lowestMonth = 100;
            int lowestDay = 100;

            double highestTemp = -100;
            int highestMonth = 100;
            int highestDay = 100;
            for (WeatherAvg day : avgYear)
            {
                if (day.temp().doubleValue() < lowestTemp)
                {
                    lowestTemp = day.temp().doubleValue();
                    lowestMonth = day.month();
                    lowestDay = day.day();
                }
                if (day.temp().doubleValue() > highestTemp)
                {
                    highestTemp = day.temp().doubleValue();
                    highestMonth = day.month();
                    highestDay = day.day();
                }
            }

            System.out.println("Coldest Day");
            System.out.println("Month: " + lowestMonth);
            System.out.println("Day: " + lowestDay);
            System.out.println("Average Temp  : " + lowestTemp);
            System.out.println();

            System.out.println("Warmest Day");
            System.out.println("Month: " + highestMonth);
            System.out.println("Day: " + highestDay);
            System.out.println("Average Temp  : " + highestTemp);
        }
        catch (Exception e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static ArrayList<WeatherDay> runProgram(int year, int location)
    {
        ArrayList<WeatherDay> arrayYear = new ArrayList<>();
        try
        {
            URL weatherURL = new URL("https://api.weather.gc.ca/collections/climate-daily/items?f=json&LOCAL_YEAR="+year+"&CLIMATE_IDENTIFIER="+location); // &LOCAL_MONTH=12&LOCAL_DAY=31
            URLConnection weatherConnection = weatherURL.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(weatherConnection.getInputStream()));
            StringBuilder stringJSON = new StringBuilder();

            String streamString;
            while ((streamString = reader.readLine()) != null)
            {
                stringJSON.append(streamString);
            }

            JSONObject weatherJSON = new JSONObject(stringJSON.toString());
            JSONArray weatherDays = weatherJSON.getJSONArray("features");

            for (int i = 0; i < weatherDays.length(); i++)
            {
                JSONObject weatherDay = weatherDays.getJSONObject(i);
                JSONObject weatherDetails = weatherDay.getJSONObject("properties");
                if (weatherDetails.get("MEAN_TEMPERATURE") != JSONObject.NULL && weatherDetails.get("TOTAL_PRECIPITATION") != JSONObject.NULL)
                {
                    arrayYear.add(new WeatherDay(false,weatherDetails.getInt("LOCAL_YEAR"),weatherDetails.getInt("LOCAL_MONTH"),weatherDetails.getInt("LOCAL_DAY"),weatherDetails.getNumber("MEAN_TEMPERATURE"),weatherDetails.getDouble("TOTAL_PRECIPITATION")));
                }
                else
                {
                    arrayYear.add(new WeatherDay(true,weatherDetails.getInt("LOCAL_YEAR"),weatherDetails.getInt("LOCAL_MONTH"),weatherDetails.getInt("LOCAL_DAY"),0,0));
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return arrayYear;
    }
}

