package com.travelbloom.model;

public class WeatherData {

    private double temperature;
    private double windSpeed;
    private int weatherCode;

    public WeatherData() {
    }

    public WeatherData(
            double temperature,
            double windSpeed,
            int weatherCode) {

        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }
}