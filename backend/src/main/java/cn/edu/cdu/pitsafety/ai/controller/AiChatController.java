package cn.edu.cdu.pitsafety.ai.controller;

import cn.edu.cdu.pitsafety.ai.dto.AiChatRequest;
import cn.edu.cdu.pitsafety.ai.dto.ChatRequest;
import cn.edu.cdu.pitsafety.ai.service.DeepSeekService;
import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.monitor.mapper.DataAxialForceMapper;
import cn.edu.cdu.pitsafety.monitor.mapper.DataSteelTemperatureMapper;
import cn.edu.cdu.pitsafety.monitor.mapper.DataTotalStationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final DeepSeekService deepSeekService;
    private final DataTotalStationMapper totalStationMapper;
    private final DataAxialForceMapper axialForceMapper;
    private final DataSteelTemperatureMapper steelTempMapper;

    @PostMapping("/chat")
    public Mono<Result<String>> chat(@RequestBody AiChatRequest request) {
        var history = request.getHistory() != null
                ? request.getHistory().stream()
                        .map(m -> ChatRequest.Message.builder().role(m.getRole()).content(m.getContent()).build())
                        .collect(Collectors.toList())
                : null;
        return deepSeekService.chat(request.getMessage(), history)
                .map(Result::success)
                .defaultIfEmpty(Result.error(500, "AI 服务响应异常"));
    }

    @PostMapping("/maintenance")
    public Mono<Result<String>> maintenanceAssistant(@RequestBody AiChatRequest request) {
        var history = request.getHistory() != null
                ? request.getHistory().stream()
                        .map(m -> ChatRequest.Message.builder().role(m.getRole()).content(m.getContent()).build())
                        .collect(Collectors.toList())
                : null;
        return deepSeekService.maintenanceAssistant(request.getMessage(), history)
                .map(Result::success)
                .defaultIfEmpty(Result.error(500, "AI 服务响应异常"));
    }

    @PostMapping("/health-monitor")
    public Mono<Result<String>> healthMonitor(@RequestBody AiChatRequest request) {
        var history = request.getHistory() != null
                ? request.getHistory().stream()
                        .map(m -> ChatRequest.Message.builder().role(m.getRole()).content(m.getContent()).build())
                        .collect(Collectors.toList())
                : null;
        return deepSeekService.healthMonitor(request.getMessage(), history)
                .map(Result::success)
                .defaultIfEmpty(Result.error(500, "AI 服务响应异常"));
    }

    @PostMapping("/predict")
    public Mono<Result<String>> predict(@RequestBody AiChatRequest request) {
        var history = request.getHistory() != null
                ? request.getHistory().stream()
                        .map(m -> ChatRequest.Message.builder().role(m.getRole()).content(m.getContent()).build())
                        .collect(Collectors.toList())
                : null;
        return deepSeekService.predict(request.getMessage(), history)
                .map(Result::success)
                .defaultIfEmpty(Result.error(500, "AI 服务响应异常"));
    }

    @GetMapping("/predict-auto")
    public Mono<Result<String>> predictAuto(
            @RequestParam String sensorCode,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        List<Map<String, String>> list = fetchAndFormat(sensorCode, startTime, endTime);
        if (list.isEmpty()) {
            return Mono.just(Result.error(400, "所选时间范围内无数据"));
        }

        String prompt = buildPrompt(sensorCode, startTime, endTime, list);
        return deepSeekService.predict(prompt, null)
                .map(Result::success)
                .defaultIfEmpty(Result.error(500, "AI 服务响应异常"));
    }

    private List<Map<String, String>> fetchAndFormat(String sensorCode, String startTime, String endTime) {
        List<Map<String, String>> result = new ArrayList<>();

        if (sensorCode.startsWith("FRHY")) {
            totalStationMapper.findBySensorAndTime(sensorCode, startTime, endTime)
                    .forEach(d -> {
                        Map<String, String> m = new LinkedHashMap<>();
                        m.put("time", fmt(d.getCollectTime()));
                        m.put("deltaX", String.valueOf(d.getDeltaX()));
                        m.put("deltaY", String.valueOf(d.getDeltaY()));
                        m.put("deltaH", String.valueOf(d.getDeltaH()));
                        m.put("totalX", String.valueOf(d.getTotalX()));
                        m.put("totalY", String.valueOf(d.getTotalY()));
                        m.put("totalH", String.valueOf(d.getTotalH()));
                        m.put("temperature", String.valueOf(d.getTemperature()));
                        result.add(m);
                    });
        } else if (sensorCode.matches("^(SP1|4P1|4P2)$")) {
            axialForceMapper.findBySensorAndTime(sensorCode, startTime, endTime)
                    .forEach(d -> {
                        Map<String, String> m = new LinkedHashMap<>();
                        m.put("time", fmt(d.getCollectTime()));
                        m.put("wforce", String.valueOf(d.getWForce()));
                        m.put("fposition", String.valueOf(d.getFPosition()));
                        m.put("temperature", String.valueOf(d.getTemperature()));
                        result.add(m);
                    });
        } else {
            steelTempMapper.findBySensorAndTime(sensorCode, startTime, endTime)
                    .forEach(d -> {
                        Map<String, String> m = new LinkedHashMap<>();
                        m.put("time", fmt(d.getCollectTime()));
                        m.put("temperature", String.valueOf(d.getTemperature()));
                        m.put("measureVal", String.valueOf(d.getMeasureVal()));
                        result.add(m);
                    });
        }
        return result;
    }

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    private String buildPrompt(String sensorCode, String startTime, String endTime,
                                List<Map<String, String>> dataList) {
        // 整点采样
        List<Map<String, String>> sampled = new ArrayList<>();
        String lastHour = "";
        for (Map<String, String> row : dataList) {
            String time = row.get("time");
            if (time.length() >= 13) {
                String hour = time.substring(0, 13);
                if (!hour.equals(lastHour)) {
                    sampled.add(row);
                    lastHour = hour;
                }
            }
        }
        if (sampled.size() > 100) {
            int step = sampled.size() / 100 + 1;
            List<Map<String, String>> reduced = new ArrayList<>();
            for (int i = 0; i < sampled.size(); i += step) {
                reduced.add(sampled.get(i));
            }
            sampled = reduced;
        }

        boolean isAxial = sensorCode.matches("^(SP1|4P1|4P2)$");
        boolean isStation = sensorCode.startsWith("FRHY");

        StringBuilder sb = new StringBuilder();
        sb.append("请分析传感器 ").append(sensorCode)
          .append(" 在 ").append(startTime).append(" ~ ").append(endTime)
          .append(" 期间的监测数据趋势，预测未来可能出现的问题。\n\n");
        sb.append("历史数据（按小时采样，共").append(sampled.size()).append("个时间点）：\n");

        for (Map<String, String> row : sampled) {
            sb.append(row.get("time"));
            if (isAxial) {
                sb.append(" | 轴力:").append(row.get("wforce")).append("kN");
                sb.append(" | 位置:").append(row.get("fposition"));
                sb.append(" | 温度:").append(row.get("temperature")).append("℃");
            } else if (isStation) {
                sb.append(" | ΔX:").append(row.get("deltaX")).append("mm");
                sb.append(" | ΔY:").append(row.get("deltaY")).append("mm");
                sb.append(" | ΔH:").append(row.get("deltaH")).append("mm");
                sb.append(" | ∑X:").append(row.get("totalX")).append("mm");
                sb.append(" | ∑Y:").append(row.get("totalY")).append("mm");
                sb.append(" | 温度:").append(row.get("temperature")).append("℃");
            } else {
                sb.append(" | 温度:").append(row.get("temperature")).append("℃");
                sb.append(" | 测量值:").append(row.get("measureVal"));
            }
            sb.append("\n");
        }
        sb.append("\n请按趋势识别→模式匹配→预测推演→风险预警的结构进行分析。");
        return sb.toString();
    }
}
