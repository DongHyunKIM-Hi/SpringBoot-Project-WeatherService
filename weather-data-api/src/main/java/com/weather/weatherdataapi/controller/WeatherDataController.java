package com.weather.weatherdataapi.controller;

import com.weather.weatherdataapi.model.dto.CoordinateDto;
import com.weather.weatherdataapi.model.dto.requestdto.ScoreRequestDto;
import com.weather.weatherdataapi.model.dto.responsedto.ReverseGeocodingResponseDto;
import com.weather.weatherdataapi.model.dto.responsedto.ScoreResultResponseDto;
import com.weather.weatherdataapi.model.dto.responsedto.WeatherDataResponseDto;
import com.weather.weatherdataapi.model.entity.BigRegion;
import com.weather.weatherdataapi.model.entity.SmallRegion;
import com.weather.weatherdataapi.model.entity.UserPreference;
import com.weather.weatherdataapi.model.entity.info.AirPollutionInfo;
import com.weather.weatherdataapi.model.entity.info.CoronaInfo;
import com.weather.weatherdataapi.repository.BigRegionRepository;
import com.weather.weatherdataapi.repository.SmallRegionRepository;
import com.weather.weatherdataapi.repository.UserPreferenceRepository;
import com.weather.weatherdataapi.service.*;
import com.weather.weatherdataapi.util.openapi.air_pollution.AirKoreaStationUtil;
import com.weather.weatherdataapi.util.openapi.geo.kakao.KakaoGeoApi;
import com.weather.weatherdataapi.util.openapi.geo.kakao.transcoord.KakaoGeoTranscoordResponseDocument;
import com.weather.weatherdataapi.util.openapi.geo.naver.ReverseGeoCodingApi;
import com.weather.weatherdataapi.util.openapi.living_health.LivingHealthApi;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
public class WeatherDataController {

    private final WeatherService openApiService;
    private final LivingHealthService livingHealthWeatherService;
    private final ScoreService scoreService;
    private final LivingHealthApi livingHealthWeatherApiCall;
    private final ReverseGeoCodingApi reverseGeoCoding;
    private final CoronaService coronaService;
    private final AirPollutionService airPollutionService;
    private final AirKoreaStationUtil airKoreaStationUtil;
    private final KakaoGeoApi kakaoGeoOpenApi;
    private final BigRegionRepository bigRegionRepository;
    private final SmallRegionRepository smallRegionRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    // 식별자 있는 기상 데이터
    @GetMapping("/api/weather/data")
    public WeatherDataResponseDto getAllWeatherData(
            @RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude, @RequestHeader("token") String token) throws ParseException, IOException {

        CoordinateDto coordinateDto = new CoordinateDto(longitude, latitude);
        ReverseGeocodingResponseDto address = reverseGeoCoding.reverseGeocoding(coordinateDto);

        // 해당 시/구 주소를 가진 Region 객체 가져오기
        BigRegion currentBigRegion = bigRegionRepository.findByBigRegionName(address.getBigRegion());
        SmallRegion currentSmallRegion = smallRegionRepository.findByBigRegionAndSmallRegionName(currentBigRegion, address.getSmallRegion());

        // OPEN API 호출
        openApiService.callApi(currentSmallRegion);
        livingHealthWeatherService.getLivingHealthInfoByBigRegion(currentBigRegion);
        airPollutionService.getInfoByRegion(currentSmallRegion);
        CoronaInfo coronaLocal = coronaService.getInfoByBigRegion(currentBigRegion);
        int coronaTotalNewCaseCount = coronaService.getTotalNewCaseCount(coronaLocal.getDate());

        // 식별값으로 DB에서 유저 선호도 불러오기
        UserPreference userPreference = new UserPreference();
        if (token != "") {
            userPreference = userPreferenceRepository.findByIdentification(token);
        } else {
            userPreference = new UserPreference("default");
        }

        // 클라이언트에서 보내준 사용자 선호도 수치를 담은 ScoreRequestDto 객체 생성
        ScoreRequestDto scoreRequestDto = ScoreRequestDto.builder()
                .tempRange(userPreference.getTemp())
                .rainPerRange(userPreference.getRainPer())
                .weatherRange(userPreference.getWeather())
                .humidityRange(userPreference.getHumidity())
                .windRange(userPreference.getWind())
                .pm10Range(userPreference.getPm10())
                .pm25Range(userPreference.getPm25())
                .coronaRange(userPreference.getCorona())
                .uvRange(userPreference.getUv())
                .pollenRiskRange(userPreference.getPollenRisk())
                .asthmaRange(userPreference.getAsthma())
                .foodPoisonRange(userPreference.getFoodPoison())
                .build();

        // 날씨 수치들을 100점으로 반환한 점수를 담는 객체 생성
        ScoreResultResponseDto scoreResultResponseDto = new ScoreResultResponseDto();
        livingHealthWeatherService.livingHealthWthIdxConvertToScore(scoreResultResponseDto, currentBigRegion);
        airPollutionService.calculateScore(scoreResultResponseDto, currentSmallRegion.getAirPollutionInfoList().get(0));
        scoreResultResponseDto.setCoronaResult(coronaService.calculateScore(coronaTotalNewCaseCount));
        openApiService.weekInfoConvertToScore(scoreResultResponseDto, currentSmallRegion); // 주간날씨 점수 반환

        int calculatedScore = scoreService.getCalculatedScore(scoreRequestDto, scoreResultResponseDto);
        WeatherDataResponseDto responseDto = new WeatherDataResponseDto(currentBigRegion, currentSmallRegion, coronaLocal, coronaTotalNewCaseCount, calculatedScore);
        return responseDto;

    }

    @GetMapping("/api/corona/data")
    public CoronaInfo getCorona(CoordinateDto coordinateDto) {
        return coronaService.getLatestInfoByBigRegion(coordinateDto);
    }

    @GetMapping("/api/air_pollution/data")
    public AirPollutionInfo getAirPollution(@RequestParam("longitude") String longitude, @RequestParam("latitude") String latitude) throws ParseException {
        CoordinateDto coordinateDto = new CoordinateDto(longitude, latitude);
        ReverseGeocodingResponseDto reverseGeocodingResponseDto = reverseGeoCoding.reverseGeocoding(coordinateDto);

        BigRegion bigRegion = bigRegionRepository.findByBigRegionName(reverseGeocodingResponseDto.getBigRegion());
        SmallRegion smallRegion = smallRegionRepository.findByBigRegionAndSmallRegionName(bigRegion, reverseGeocodingResponseDto.getSmallRegion());

        AirPollutionInfo airPollution = airPollutionService.fetchAndStoreAirPollutionInfoUsingOpenApi(smallRegion);

        return airPollution;
    }

    @GetMapping("/api/transcoord")
    public String getTranscoord(@RequestParam String x, @RequestParam String y) {
        try {
            KakaoGeoTranscoordResponseDocument document = kakaoGeoOpenApi.convertWGS84ToWTM(x, y);
            return document.getX() + "," + document.getY();
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}
