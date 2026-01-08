package com.project.jobportal.services;

import com.project.jobportal.dto.NbpExchangeRateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

//сервис будет делать вызов к API NBP
@Service
public class NbpExchangeRateService {

    private final WebClient webClient;

    // Внедряем WebClient с помощью Builder
    @Autowired
    public NbpExchangeRateService(WebClient.Builder webClientBuilder) {
        // Устанавливаем базовый URL для API NBP
        this.webClient = webClientBuilder.baseUrl("http://api.nbp.pl/api/").build();
    }

    // получение курса euro к pln
    public Optional<Double> getCurrencyExchangeRate(String currency) {
//        String uri = "exchangerates/rates/a/eur/?format=json";
        String uri = String.format("exchangerates/rates/a/%s/?format=json", currency);
        try {
            NbpExchangeRateDTO response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(NbpExchangeRateDTO.class) // Ожидаем, что ответ будет маппирован на наш DTO
                    .block(); // Блокируем, чтобы получить синхронный результат

            // Проверяем, что ответ не null и содержит курс
            if (response != null && response.getRates() != null && !response.getRates().isEmpty()) {
                // Возвращаем средний курс (mid)
                return Optional.of(response.getRates().get(0).getMid());
            }
        } catch (Exception e) {
            System.err.println("Error fetching NBP exchange rate: " + e.getMessage());
        }
        return Optional.empty();
    }
}