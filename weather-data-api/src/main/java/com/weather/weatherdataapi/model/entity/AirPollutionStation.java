package com.weather.weatherdataapi.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class AirPollutionStation extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "air_pollution_station_id")
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id")
    private Region region;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "small_region_id")
    private SmallRegion smallRegion;

    @Column(name = "station_name")
    private String stationName;

    public AirPollutionStation(Region region, String stationName) {
        this.region = region;
        this.stationName = stationName;
    }
}
