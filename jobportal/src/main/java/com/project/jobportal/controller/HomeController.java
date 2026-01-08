package com.project.jobportal.controller;

import com.project.jobportal.services.NbpExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@Slf4j
public class HomeController {
    private final NbpExchangeRateService nbpExchangeRateService;

    @Autowired
    public HomeController(NbpExchangeRateService nbpExchangeRateService) {
        this.nbpExchangeRateService = nbpExchangeRateService;
    }

    @Value("${spring.datasource.url}")
    private String dataBaseUrl;

    @Operation(summary = "Main page 'index.html'", description = "Return main page 'index.html'")
    @ApiResponse(responseCode = "200", description = "Page loaded successfully", content = @Content(mediaType = "text/html"))
    @GetMapping("/")
    public String home(@Parameter(hidden = true) Model model) {
        log.info("Accessing home page ('/')");

        getCurrencyRate(model);

        log.info("Current profile database URL: {}", dataBaseUrl); // используем {} вместо конкатенации строк
        System.out.println("Current profile use database url: " + dataBaseUrl);
        return "index";
    }

    //      получение курсов валют и передача в модель
    private void getCurrencyRate(Model model) {
        Optional<Double> euroRate = nbpExchangeRateService.getCurrencyExchangeRate("eur");
        Optional<Double> usdRate = nbpExchangeRateService.getCurrencyExchangeRate("usd");
        Optional<Double> gbpRate = nbpExchangeRateService.getCurrencyExchangeRate("gbp");

        if (euroRate.isPresent()) {
            model.addAttribute("euroRateToPln", String.format("%.2f", euroRate.get()));
            model.addAttribute("usdRateToPln", String.format("%.2f", usdRate.get()));
            model.addAttribute("gbpRateToPln", String.format("%.2f", gbpRate.get()));

            System.out.println("euroRateToPln: " + euroRate.get());
            System.out.println("usdRateToPln: " + usdRate.get());
            System.out.println("gbpRateToPln: " + gbpRate.get());

            log.info("Currency rates loaded successfully: EUR={}, USD={}, GBP={}",
                    euroRate.get(), usdRate.get(), gbpRate.get());
        } else {
            model.addAttribute("euroRateToPln", "N/A");
            model.addAttribute("usdRateToPln", "N/A");
            model.addAttribute("gbpRateToPln", "N/A");

            System.out.println("euroRateToPln: " + "N/A");
            System.out.println("usdRateToPln: " + "N/A");
            System.out.println("gbpRateToPln: " + "N/A");
            log.info(">>> Current currency rates: : N/A");
        }
    }
}
