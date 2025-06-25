package org.gualsh.demo.openfeign.config;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Если нужен кастомный Encoder
 * */
public class CustomXmlEncoder implements Encoder {

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) {
        if (object == null) {
            return;
        }

        try {
            // Преобразуем объект в XML
            String xmlContent = convertToXml(object);

            // Устанавливаем Content-Type
            template.header("Content-Type", "application/xml");

            // Устанавливаем тело запроса
            template.body(xmlContent.getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new EncodeException("Failed to encode object to XML", e);
        }
    }

    private String convertToXml(Object object) {
        // Логика преобразования в XML (JAXB, XStream, etc.)
        return "";
    }
}
