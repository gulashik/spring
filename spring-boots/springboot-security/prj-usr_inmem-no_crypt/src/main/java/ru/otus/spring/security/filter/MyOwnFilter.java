package ru.otus.spring.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class MyOwnFilter extends GenericFilterBean /*todo наследуемся*/{
    // todo реализация
    @Override
    public void doFilter(
        ServletRequest servletRequest,
        ServletResponse servletResponse,
        FilterChain filterChain
    ) throws IOException, ServletException {

        // todo заполняем параметры запроса
        var requestWrapper = new HttpServletRequestWrapper((HttpServletRequest) servletRequest) {
            @Override
            public String[] getParameterValues(String name) {

                if ("SpecialValueExpect".equals(name)) {
                    return new String[]{"My dirty secret"};
                } else if ("SpecialValueExpect2".equals(name)) {
                    return new String[]{"My dirty secret2"};
                }
                return super.getParameterValues(name);
            }
        };

        filterChain.doFilter(requestWrapper, servletResponse);
    }
}
