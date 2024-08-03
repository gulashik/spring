package com.sprboot.ann.condition.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Component
public class ShowMeBeans {
    private final List<Person> who = null;
}
