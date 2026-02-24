package com.example.back.service;

import com.example.back.dto.IndexDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class BriefingService {

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat percentFormat = new DecimalFormat("+#,##0.00%;-#,##0.00%");

    public List<IndexDto> getIndices() {
        List<IndexDto> indices = new ArrayList<>();
        
        // 대상 지수 목록 (이름, 야후 파이낸스 티커)
        String[][] targetIndices = {
            {"나스닥", "^IXIC"},
            {"S&P 500", "^GSPC"},
            {"다우존스", "^DJI"}
        };

        for (String[] target : targetIndices) {
            indices.add(fetchYahooFinance(target[0], target[1]));
        }

        return indices;
    }

    private IndexDto fetchYahooFinance(String name, String ticker) {
        try {
            String encodedTicker = URLEncoder.encode(ticker, StandardCharsets.UTF_8);
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + encodedTicker;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode meta = root.path("chart").path("result").get(0).path("meta");

                double currentPrice = meta.path("regularMarketPrice").asDouble();
                double previousClose = meta.path("previousClose").asDouble();

                // 등락률 계산: ((현재가 - 전일종가) / 전일종가)
                double changeRate = (currentPrice - previousClose) / previousClose;

                String value = priceFormat.format(currentPrice);
                String changeRateStr = percentFormat.format(changeRate);

                return new IndexDto(name, value, changeRateStr);
            } else {
                return new IndexDto(name, "데이터 일시 오류", "데이터 일시 오류");
            }

        } catch (Exception e) {
            return new IndexDto(name, "데이터 일시 오류", "데이터 일시 오류");
        }
    }
}
