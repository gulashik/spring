package org.gulash.demo.starter.postgres;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Превращает «сырые» {@link AdditionalDataSourceConfigurationException} в красивый
 * блок диагностики на старте Spring Boot приложения.
 *
 * <h2>Зачем</h2>
 * Без FailureAnalyzer пользователь стартера увидит «портянку» стектрейса. С ним —
 * чёткое сообщение «что сломано», «где сломано» и «что сделать».
 *
 * <h2>Как Spring Boot его находит</h2>
 * Через файл {@code META-INF/spring.factories} с ключом
 * {@code org.springframework.boot.diagnostics.FailureAnalyzer}. Это <strong>один из
 * немногих случаев</strong>, когда {@code spring.factories} в Boot 3 всё ещё нужен —
 * для FailureAnalyzer'ов, FailureAnalysisReporter'ов и подобной диагностики;
 * для авто-конфигов используется {@code .imports}.
 *
 * <h2>Пример вывода</h2>
 * <pre>
 * ***************************
 * APPLICATION FAILED TO START
 * ***************************
 *
 * Description:
 *   Невалидная конфигурация дополнительного источника 'dictionary' (стартер additional-sources-postgres):
 *   не задан jdbc-url; пример: jdbc:postgresql://host:5432/db
 *
 * Action:
 *   Проверьте секцию 'app.datasources.dictionary' в application.yml. ...
 * </pre>
 */
public class AdditionalDataSourceFailureAnalyzer
        extends AbstractFailureAnalyzer<AdditionalDataSourceConfigurationException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, AdditionalDataSourceConfigurationException cause) {
        String name = cause.getDataSourceName();
        String description = """
                Невалидная конфигурация дополнительного источника '%s' (стартер additional-sources-postgres):
                %s""".formatted(name, cause.getMessage());

        String action = """
                Проверьте секцию 'app.datasources.%s' в application.yml.
                Минимально необходимые поля:
                  jdbc-url:  jdbc:postgresql://<host>:<port>/<db>
                  username:  <user>
                  password:  <password>
                Подробности в README стартера: additional-sources-postgres/README.md""".formatted(name);

        return new FailureAnalysis(description, action, cause);
    }
}
