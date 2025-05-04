package ru.gulash.actuatordemo.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import ru.gulash.actuatordemo.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Пользовательский контрибьютор информации для эндпоинта /actuator/info.
 * Добавляет информацию о времени запуска и статистике пользователей.
 */
@Component
@RequiredArgsConstructor
public class CustomInfoContributor implements InfoContributor {

    private final UserRepository userRepository;
    private final LocalDateTime startupTime = LocalDateTime.now();

    /**
     * Вызывается при запросе к /actuator/info
     * для добавления информации в ответ
     *
     * @param builder билдер информации
     */
    @Override
    public void contribute(Info.Builder builder) {
        // Добавляем информацию о времени запуска
        builder.withDetail("startup", startupTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Добавляем информацию о статистике пользователей
        Map<String, Object> usersStats = new HashMap<>();
        usersStats.put("total", userRepository.count());
        usersStats.put("active", userRepository.findByActive(true).size());
        usersStats.put("inactive", userRepository.findByActive(false).size());

        builder.withDetail("users", usersStats);

        // Добавляем информацию о среде выполнения
        Map<String, Object> runtimeInfo = new HashMap<>();
        runtimeInfo.put("processors", Runtime.getRuntime().availableProcessors());
        runtimeInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
        runtimeInfo.put("maxMemory", Runtime.getRuntime().maxMemory());
        runtimeInfo.put("totalMemory", Runtime.getRuntime().totalMemory());

        builder.withDetail("runtime", runtimeInfo);
    }
}
