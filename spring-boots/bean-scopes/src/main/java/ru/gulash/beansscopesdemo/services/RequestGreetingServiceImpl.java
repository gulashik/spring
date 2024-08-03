package ru.gulash.beansscopesdemo.services;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
// или @RequestScope только тут proxyMode = ScopedProxyMode.TARGET_CLASS
@Service("requestGreetingService")
public class RequestGreetingServiceImpl extends AbstractGreetingServiceImpl {
}
