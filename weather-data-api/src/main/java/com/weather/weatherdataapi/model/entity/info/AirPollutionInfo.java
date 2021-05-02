package com.weather.weatherdataapi.model.entity.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.weather.weatherdataapi.model.entity.Region;
import com.weather.weatherdataapi.model.entity.SmallRegion;
import com.weather.weatherdataapi.model.entity.Timestamped;
import com.weather.weatherdataapi.util.openapi.air_pollution.airkorea.AirKoreaAirPollutionItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AirPollutionInfo extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "air_pollution_id")
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id")
    private Region region;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "small_region_id")
    private SmallRegion smallRegion;

    @Column
    private LocalDateTime dateTime;

    @Column
    private Integer pm10Value;

    @Column
    private Integer pm25Value;

    public AirPollutionInfo(AirKoreaAirPollutionItem item, Region region) {
        this.region = region;
        this.dateTime = LocalDateTime.parse(item.getDataTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.pm10Value = Integer.parseInt(item.getPm10Value());
        this.pm25Value = Integer.parseInt(item.getPm25Value());
    }

}
