package com.weather.weatherdataapi.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.weather.weatherdataapi.model.dto.requestdto.ScoreRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class UserPreference {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_preference_id")
    private Long id;

    @Column
    private String identification;

    @Column
    private int temp;

    @Column
    private int rainPer;

    @Column
    private int weather;

    @Column
    private int humidity;

    @Column
    private int wind;

    @Column
    private int pm10;

    @Column
    private int pm25;

    @Column
    private int corona;

    @Column
    private int uv;

    @Column
    private int pollenRisk;

    @Column
    private int asthma;

    @Column
    private int foodPoison;

    public UserPreference(String identification, ScoreRequestDto scoreRequestDto) {
        this.identification = identification;
        this.temp = scoreRequestDto.getTempRange();
        this.rainPer = scoreRequestDto.getRainPerRange();
        this.weather = scoreRequestDto.getWeatherRange();
        this.humidity = scoreRequestDto.getHumidityRange();
        this.wind = scoreRequestDto.getWindRange();
        this.pm10 = scoreRequestDto.getPm10Range();
        this.pm25 = scoreRequestDto.getPm25Range();
        this.corona = scoreRequestDto.getCoronaRange();
        this.uv = scoreRequestDto.getUvRange();
        this.pollenRisk = scoreRequestDto.getPollenRiskRange();
        this.asthma = scoreRequestDto.getAsthmaRange();
        this.foodPoison = scoreRequestDto.getFoodPoisonRange();
    }

    public void updateUserPreference(ScoreRequestDto scoreRequestDto) {
        this.temp = scoreRequestDto.getTempRange();
        this.rainPer = scoreRequestDto.getRainPerRange();
        this.weather = scoreRequestDto.getWeatherRange();
        this.humidity = scoreRequestDto.getHumidityRange();
        this.wind = scoreRequestDto.getWindRange();
        this.pm10 = scoreRequestDto.getPm10Range();
        this.pm25 = scoreRequestDto.getPm25Range();
        this.corona = scoreRequestDto.getCoronaRange();
        this.uv = scoreRequestDto.getUvRange();
        this.pollenRisk = scoreRequestDto.getPollenRiskRange();
        this.asthma = scoreRequestDto.getAsthmaRange();
        this.foodPoison = scoreRequestDto.getFoodPoisonRange();
    }

}