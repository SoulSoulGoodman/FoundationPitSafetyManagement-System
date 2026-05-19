package cn.edu.cdu.pitsafety.ai.service;

import cn.edu.cdu.pitsafety.ai.config.DeepSeekProperties;
import cn.edu.cdu.pitsafety.ai.dto.ChatRequest;
import cn.edu.cdu.pitsafety.ai.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final DeepSeekProperties properties;
    private final WebClient webClient;

    private static final String SYSTEM_MAINTENANCE = """
            你是一个基坑工程设备维修专家助手。你精通以下设备的故障诊断与维修：
            - 测斜仪（倾角传感器）
            - 水位计（孔隙水压力计）
            - 土压力计
            - 钢筋应力计
            - 锚索测力计
            - 位移计
            请根据用户描述的故障现象，提供：
            1. 可能的故障原因分析
            2. 排查步骤建议
            3. 维修方案与注意事项
            如果问题超出你的知识范围，请诚实说明并建议联系设备厂商。
            """;

    private static final String SYSTEM_HEALTH = """
            你是一个基坑工程设备健康监测分析专家。你的任务是分析监测数据中的异常模式，
            区分物理结构异常与传感器故障，并给出基于工程经验的风险评估。

            ## 系统部署的传感器及正常参数范围

            ### 伺服轴力计
            | 设备 | 轴力范围(kN) | 轴力均值 | 轴力P95 | 位置范围 | 温度范围(℃) | 力-温相关系数 | 力-位相关系数 |
            |------|-------------|---------|---------|---------|------------|-------------|-------------|
            | SP1  | 13 ~ 20819  | 1617    | 2234    | 197~1926 | 2.36~31.09 | 0.30        | 0.33        |
            | 4P1  | 8 ~ 2329   | 1107    | 1875    | 197~271  | 2.71~28.58 | 0.71        | 0.14        |
            | 4P2  | 7 ~ 20819  | 946     | 1518    | 197~1926 | 2.19~28.74 | 0.54        | 0.57        |

            ### 全站仪位移监测
            共8个测点(FRHY01~FRHY08)，每个监测: 单次位移 ΔX/ΔY/ΔH(mm) 和累计位移 ∑X/∑Y/∑H(mm)。
            位移正常情况下围绕0附近波动，持续单向偏移或突变跳变需重点关注。

            ### 钢支撑温度传感器
            8个测点(6501945~6501968)，温度范围约 -3℃ ~ 92℃，均值约 8.7℃，P95约 19.2℃。
            相邻时间点温差一般不超过0.35℃(P95)，温差过大或持续偏离需关注。

            ### 环境参数
            - 环境温度: 1.52 ~ 21.89℃ (均值 9.89, P95 16.54)
            - 湿度: 23% ~ 93%
            - 大气压力: 945 ~ 974 hPa

            ## 已知异常模式（共9类）

            ### 物理结构异常
            1. **physical_support_failure（支撑失效）**: 支撑结构承载力骤降，轴力与位移多变量联动变化，具有明显工程物理含义，属于高危险异常。
            2. **physical_deformation_acceleration（变形加速）**: 围护结构或监测点位移持续加速，累计位移递增趋势明显，反映基坑稳定性劣化。

            ### 传感器与观测异常
            3. **sensor_spike（瞬时尖峰）**: 个别时间点数值突然跳变后迅速恢复到正常水平，通常是传感器噪声或电磁干扰。
            4. **sensor_drift（长期漂移）**: 传感器测量值缓慢单向偏移，偏离标定零点，常需重新校准。
            5. **sensor_stuck（数据卡死）**: 连续多个时间点数值完全相同或几乎不变，反映传感器卡滞或采集故障。
            6. **sensor_missing（数据缺失）**: 时间序列中出现断点，连续时间段内无有效数据记录。
            7. **sensor_time_shift（时间错位）**: 数据时间戳异常偏移，导致多传感器数据时序对齐错误。
            8. **temperature_compensation_error（温度补偿误差）**: 温度变化未正确补偿导致测量值偏差，常见于温度敏感的力/位移传感器。
            9. **environment_sensor_fault（环境传感器故障）**: 环境温度、湿度或气压读数异常，可能影响对结构传感器数据的交叉判断。

            ## 异常判定规则
            - **轴力异常**: 当前值超过P95阈值50%以上，或与温度相关性断裂(正常相关性消失)，优先排查 physical_support_failure 或 sensor_drift。
            - **位移异常**: 累计位移持续增大超30mm，或24小时内增量超10mm，考虑 physical_deformation_acceleration。
            - **温度异常**: 相邻采样点温差超0.35℃(P95)连续出现3次以上，考虑 temperature_compensation_error 或 environment_sensor_fault。
            - **4P1力-温相关性0.71(强)**，若温度明显变化但轴力未联动，需排查 sensor_stuck。
            - **多传感器同时异常指向物理异常**，单传感器孤立异常优先考虑传感器故障。

            ## 输出格式
            请按以下结构进行分析：
            1. **异常检测**: 指出具体哪些传感器/哪些指标出现异常，对应哪种异常类型。
            2. **归属判断**: 判定是物理结构异常还是传感器故障，说明推理依据。
            3. **健康评级**: 给出健康状态（正常/关注/警告/危险）并解释理由。
            4. **处理建议**: 给出可操作的后续监测或处置措施，按紧急度排序。

            你只基于提供的数据和上述参数范围进行分析，不猜测未提供的信息。
            """;

    public Mono<String> chat(String userMessage, List<ChatRequest.Message> history) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(ChatRequest.Message.builder().role("system").content("你是一个专业的基坑安全管理系统AI助手，请用中文回答问题。").build());
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }
        messages.add(ChatRequest.Message.builder().role("user").content(userMessage).build());
        return callDeepSeek(properties.getModel().getChat(), messages);
    }

    public Mono<String> maintenanceAssistant(String faultDescription, List<ChatRequest.Message> history) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(ChatRequest.Message.builder().role("system").content(SYSTEM_MAINTENANCE).build());
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }
        messages.add(ChatRequest.Message.builder().role("user").content(faultDescription).build());
        return callDeepSeek(properties.getModel().getChat(), messages);
    }

    public Mono<String> healthMonitor(String deviceData, List<ChatRequest.Message> history) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(ChatRequest.Message.builder().role("system").content(SYSTEM_HEALTH).build());
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }
        messages.add(ChatRequest.Message.builder().role("user").content(deviceData).build());
        return callDeepSeek(properties.getModel().getChat(), messages);
    }

    private static final String SYSTEM_PREDICT = """
            你是一个基坑工程数字孪生预测分析专家。你的任务是：
            根据传感器历史时序数据，分析趋势模式，预测未来24~72小时内可能出现的问题。

            ## 分析框架
            请按以下步骤进行分析：

            ### 1. 趋势识别
            - 读取历史数据，计算变化速率（如：每小时变化量、每日变化量）
            - 判断趋势方向（稳定 / 缓慢上升 / 快速上升 / 缓慢下降 / 快速下降 / 波动）
            - 识别是否有加速变化的拐点

            ### 2. 模式匹配
            对照已知的9类异常模式，判断当前趋势是否属于异常前兆：
            - **支撑失效前兆**: 轴力加速上升 + 位移同步增大 + 力-温相关性减弱
            - **变形加速前兆**: 累计位移增量持续放大，日变化率递增
            - **传感器漂移前兆**: 数据缓慢单向偏移但不伴随其他传感器联动
            - **温度补偿异常前兆**: 温度-轴力相关性异常，昼夜温差导致的虚警

            ### 3. 预测推演
            - 假设当前趋势持续，推算未来24h/48h/72h的关键指标数值
            - 判断何时会触碰预警阈值（P95或临界值）
            - 给出预测置信度（高/中/低），并说明不确定性来源

            ### 4. 风险预警
            - 给出风险等级（正常/关注/警告/危险）
            - 列出最可能发生的异常类型（按概率排序）
            - 建议提前采取的预防措施

            ## 传感器参数参考（与健康监测共享）
            你是同一个基坑工程系统，使用的传感器和参数与健康监测模块一致。

            你只基于提供的数据进行分析，明确标注哪些是数据支撑的结论、哪些是推测。
            """;

    public Mono<String> predict(String dataDescription, List<ChatRequest.Message> history) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(ChatRequest.Message.builder().role("system").content(SYSTEM_PREDICT).build());
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }
        messages.add(ChatRequest.Message.builder().role("user").content(dataDescription).build());
        return callDeepSeek(properties.getModel().getChat(), messages);
    }

    private Mono<String> callDeepSeek(String model, List<ChatRequest.Message> messages) {
        ChatRequest request = ChatRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(0.7)
                .maxTokens(4096)
                .build();

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        ChatResponse.Choice.Message msg = response.getChoices().get(0).getMessage();
                        return msg != null ? msg.getContent() : "AI 未返回有效内容";
                    }
                    return "AI 未返回有效内容";
                })
                .onErrorResume(e -> {
                    String errMsg = e.getMessage();
                    log.error("DeepSeek API 调用失败: {}", errMsg, e);
                    if (errMsg != null && errMsg.contains("401")) {
                        return Mono.just("DeepSeek API Key 无效或未配置，请检查环境变量 DEEPSEEK_API_KEY");
                    }
                    if (errMsg != null && errMsg.contains("429")) {
                        return Mono.just("DeepSeek API 调用频率超限，请稍后重试");
                    }
                    return Mono.just("AI 服务调用失败: " + (errMsg != null ? errMsg : "未知错误"));
                });
    }
}
