package org.gualsh.demo.openfeign.config;

import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Если нужен кастомный Decoder
 */
public class CustomXmlDecoder implements Decoder {

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.body() == null) {
            return null;
        }

        try (InputStream inputStream = response.body().asInputStream()) {
            String xmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Проверяем Content-Type
            String contentType = response.headers().get("Content-Type")
                .stream().findFirst().orElse("");

            if (!contentType.contains("application/xml")) {
                throw new DecodeException(response.status(),
                    "Expected XML content type", response.request());
            }

            // Преобразуем XML в объект
            return convertFromXml(xmlContent, type);

        } catch (Exception e) {
            throw new DecodeException(response.status(),
                "Failed to decode XML response", response.request(), e);
        }
    }

    private Object convertFromXml(String xml, Type type) throws Exception {
        // JAXB unmarshalling
        return "";
    }
}
