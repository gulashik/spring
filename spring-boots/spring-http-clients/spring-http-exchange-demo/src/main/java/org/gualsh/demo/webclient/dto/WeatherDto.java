package org.gualsh.demo.webclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO для данных о погоде из OpenWeatherMap API.
 *
 * @author Demo
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDto implements Serializable {

    /**
     * Название города.
     */
    private String name;

    /**
     * Основные погодные данные.
     */
    private MainWeatherDto main;

    /**
     * Описание погоды.
     */
    private List<WeatherDescriptionDto> weather;

    /**
     * Данные о ветре.
     */
    private WindDto wind;

    /**
     * Видимость в метрах.
     */
    private Integer visibility;

    /**
     * Время восхода солнца (Unix timestamp).
     */
    private SysDto sys;

    /**
     * DTO для основных погодных параметров.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MainWeatherDto implements Serializable {

        /**
         * Текущая температура в Кельвинах.
         */
        private Double temp;

        /**
         * Ощущается как температура в Кельвинах.
         */
        @JsonProperty("feels_like")
        private Double feelsLike;

        /**
         * Минимальная температура в Кельвинах.
         */
        @JsonProperty("temp_min")
        private Double tempMin;

        /**
         * Максимальная температура в Кельвинах.
         */
        @JsonProperty("temp_max")
        private Double tempMax;

        /**
         * Атмосферное давление в гПа.
         */
        private Integer pressure;

        /**
         * Влажность в процентах.
         */
        private Integer humidity;
    }

    /**
     * DTO для описания погодных условий.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherDescriptionDto implements Serializable {

        /**
         * Идентификатор погодных условий.
         */
        private Integer id;

        /**
         * Группа погодных параметров.
         */
        private String main;

        /**
         * Описание погоды.
         */
        private String description;

        /**
         * Иконка погоды.
         */
        private String icon;
    }

    /**
     * DTO для данных о ветре.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WindDto implements Serializable {

        /**
         * Скорость ветра в м/с.
         */
        private Double speed;

        /**
         * Направление ветра в градусах.
         */
        private Integer deg;

        /**
         * Порывы ветра в м/с.
         */
        private Double gust;
    }

    /**
     * DTO для системной информации.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SysDto implements Serializable {

        /**
         * Код страны.
         */
        private String country;

        /**
         * Время восхода солнца (Unix timestamp).
         */
        private Long sunrise;

        /**
         * Время заката солнца (Unix timestamp).
         */
        private Long sunset;
    }
}