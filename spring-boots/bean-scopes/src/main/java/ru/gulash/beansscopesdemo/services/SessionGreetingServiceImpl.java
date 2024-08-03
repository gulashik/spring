package ru.gulash.beansscopesdemo.services;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
// или @SessionScope только тут proxyMode = ScopedProxyMode.TARGET_CLASS
@Service("sessionGreetingService")
public class SessionGreetingServiceImpl extends AbstractGreetingServiceImpl {
}
