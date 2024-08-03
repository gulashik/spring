package ru.gulash.beansscopesdemo.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Scope(scopeName = "prototype")
// @Scope(scopeName = "prototype", proxyMode = ScopedProxyMode.INTERFACES) - новый бин каждое обращение к нему из-за обращения к proxy
// потому что по умолчанию ScopedProxyMode.DEFAULT т.е. не обрарачивается в Proxy
// и обращение к proxy не инициализирует запрос getBean из контекста, а т.к. prototype следует создание
@Service("prototypeGreetingService")
public class PrototypeGreetingServiceImpl extends AbstractGreetingServiceImpl {
}
