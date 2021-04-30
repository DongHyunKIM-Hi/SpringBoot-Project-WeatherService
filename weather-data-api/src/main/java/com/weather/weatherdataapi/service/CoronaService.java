package com.weather.weatherdataapi.service;

import com.weather.weatherdataapi.model.entity.Corona;
import com.weather.weatherdataapi.repository.CoronaRepository;
import com.weather.weatherdataapi.util.openapi.corona.ICoronaInfo;
import com.weather.weatherdataapi.util.openapi.corona.ICoronaItem;
import com.weather.weatherdataapi.util.openapi.corona.gov.GovCoronaOpenApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CoronaService {

    private final CoronaRepository coronaRepository;

    private final GovCoronaOpenApi govCoronaOpenApi;

    public List<Corona> fetchAndStoreCoronaInfoUsingOpenApi() throws Exception {
        ICoronaInfo info = govCoronaOpenApi.getInfo();

        List<Corona> coronaList = new ArrayList<>(info.getItemList().size());

        coronaRepository.deleteAll();

        for (int i = 0; i < info.getItemList().size(); i++) {
            ICoronaItem item = info.getItemList().get(i);

            Corona corona = new Corona(item);
            coronaRepository.save(corona);

            coronaList.add(corona);
        }

        return coronaList;
    }
}
