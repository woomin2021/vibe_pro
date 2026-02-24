package com.example.back.service;

import com.example.back.dto.IndexDto;
import com.example.back.dto.NewsDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BriefingService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

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

    public List<NewsDto> getNews() {
        try {
            String url = "https://news.google.com/rss/search?q=미국+증시&hl=ko&gl=KR&ceid=KR:ko";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseNewsXml(response.body());
            }
        } catch (Exception e) {
            // 에러 발생 시 빈 리스트 반환
        }
        return new ArrayList<>();
    }

    public List<String> getAiSummary() {
        try {
            List<NewsDto> news = getNews();
            if (news.isEmpty()) {
                return List.of("요약할 뉴스가 없습니다.");
            }

            String newsTitles = news.stream()
                    .map(NewsDto::title)
                    .collect(Collectors.joining("\n"));

            String prompt = "다음은 오늘 미국 증시 뉴스 제목들이야.\n\n" + newsTitles + "\n\n이 뉴스들을 바탕으로 오늘 미국 증시의 핵심 동향을 정확히 3개의 문장으로 요약해 줘.\n" +
                    "주의사항:\n" +
                    "1. 반드시 3줄(3개의 문장)로 작성할 것.\n" +
                    "2. 인사말, 번호, 마크다운(예: **, #) 기호를 절대 사용하지 말 것.\n" +
                    "3. 각 문장은 줄바꿈(\n)으로 구분할 것.";

            // Gemini API 요청 바디 구성
            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode contentsArray = rootNode.putArray("contents");
            ObjectNode contentNode = contentsArray.addObject();
            ArrayNode partsArray = contentNode.putArray("parts");
            partsArray.addObject().put("text", prompt);

            String requestBody = objectMapper.writeValueAsString(rootNode);
            // 1.5 모델이 종료되어서 2.5 모델로 올려줍니다
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                JsonNode candidates = responseJson.path("candidates");
                
                if (candidates.isArray() && !candidates.isEmpty()) {
                    String summaryText = candidates.get(0)
                            .path("content").path("parts").get(0).path("text").asText();

                    return Arrays.stream(summaryText.split("\n"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .limit(3)
                            .collect(Collectors.toList());
                } else {
                    System.err.println("Gemini API Response has no candidates: " + response.body());
                }
            } else {
                System.err.println("Gemini API Error Status: " + response.statusCode());
                System.err.println("Gemini API Error Body: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of("AI 요약을 불러올 수 없습니다.");
    }

    private List<NewsDto> parseNewsXml(String xml) {
        List<NewsDto> newsList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            NodeList items = document.getElementsByTagName("item");
            // 최대 5개 추출
            for (int i = 0; i < Math.min(items.getLength(), 5); i++) {
                Element item = (Element) items.item(i);
                String title = getElementValue(item, "title");
                String link = getElementValue(item, "link");
                String pubDate = getElementValue(item, "pubDate");
                newsList.add(new NewsDto(title, link, pubDate));
            }
        } catch (Exception e) {
            // 파싱 에러 시 빈 리스트 반환
        }
        return newsList;
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
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
