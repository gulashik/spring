package com.gulash.example.assertjdemo.collections;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;


record Person (String name, Integer age){}
record Person2 (String name, String attr1, Integer age){}
record Person3 (String name, List<String> hobbies, Integer age){}

public class ListAssertTest {
    @Test
    void testListContents() {

        // todo extracting и map - извлекает значения из объектов коллекции для дальнейшего сравнения.
        List<Person> people = List.of(
            new Person("Alice", 30),
            new Person("Bob", 40)
        );
        Assertions.assertThat(people)
            // todo extracting по ЛЯМБДЕ
            .extracting(Person::name)
            .contains("Alice", "Bob");
        Assertions.assertThat(people)
            // todo extracting по ИМЕНИ_ПОЛЯ + УКАЗАНИЕ ТИПА(опционально)
            .extracting("name", String.class)
            .contains("Alice", "Bob");
        Assertions.assertThat(people)
            // todo extracting + TUPLE - извлекаем нужный кортеж свойств
            .extracting("name", "age")
                .contains(
                    Tuple.tuple("Alice", 30),
                    Tuple.tuple("Bob", 40)
                );
        Assertions.assertThat(people)
            // todo map
            .map(Person::name)
            .contains("Alice", "Bob");
        List<Person3> people2 = List.of(
            new Person3("Alice", List.of("one","two"), 30),
            new Person3("Bob", List.of("three","four"), 40)
        );

        Assertions.assertThat(people2)
            // todo flatExtracting - используется для извлечения значений из вложенных коллекций.
            .flatExtracting(Person3::hobbies)
            .contains("one", "four");

        // todo usingRecursiveFieldByFieldElementComparator - сравнивает элементы коллекции на основе их полей, игнорируя equals()
        List<Person> list1 = List.of(new Person("Alice", 30));
        List<Person2> list2 = List.of(new Person2("Alice", "someattr1", 30));

        Assertions.assertThat(list1)
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(list2);

        // todo isIn, isNotIn - проверка элемента на вхождение
        Assertions.assertThat("three")
            .isIn(List.of("one", "two", "three", "four"))
                // аналогично
                .matches(s -> List.of("one", "two", "three", "four").contains(s))
            .isNotIn(List.of("one", "two"))
                // аналогично
                .matches(s -> !List.of("one", "two").contains(s));

        // todo filteredOn - фильтрация коллекций
        List<Person> listForFilter = List.of(
            new Person("One", 10),
            new Person("Two", 20),
            new Person("Three", 30),
            new Person("Four", 40)
        );
        // todo Информационно
        //  к вложенным полям обращаемся через ТОЧКУ "address.street"
        // todo Фильтрация ИМЯ_ПОЛЯ + РАВЕНСТВО, NOT, IN, NOT IN - операции сравнения
        Assertions.assertThat(listForFilter)
            .filteredOn("age"/*todo имя поля*/,10/*todo равенство*/)
            .extracting(Person::name)
            .isEqualTo(List.of("One"));
        Assertions.assertThat(listForFilter)
            .filteredOn("age"/*todo имя поля*/, AssertionsForClassTypes.not(10)/*todo NOT не равенство*/)
            .extracting(Person::name)
            .isEqualTo(List.of("Two", "Three", "Four"));
        Assertions.assertThat(listForFilter)
            .filteredOn("age"/*todo имя поля*/, AssertionsForClassTypes.notIn(20, 30)/*todo NOT IN не входит в*/)
            .extracting(Person::name)
            .isEqualTo(List.of("One", "Four"));
        Assertions.assertThat(listForFilter)
            .filteredOn("age"/*todo имя поля*/, AssertionsForClassTypes.in(20, 30)/*todo IN входит в*/)
            .extracting(Person::name)
            .isEqualTo(List.of("Two", "Three"));

        // todo Фильтрация ЛЯМБДА
        Assertions.assertThat(listForFilter)
            .filteredOn((person) -> List.of(10,20).contains(person.age())/*todo лямбда*/)
            .extracting(Person::name)
            .isEqualTo(List.of("One", "Two"));

        // todo Фильтрация singleElement - коллекция после фильтрации содержит только один элемент аналог hasSize(1).first()
        Assertions.assertThat(listForFilter)
            .filteredOn("age"/*todo имя поля*/,10/*todo равенство*/)
            .singleElement() // todo только один элемент
            .extracting(Person::name)
            .isEqualTo("One");

        // todo contains - содержит ли коллекция указанные элементы.
        Assertions.assertThat(List.of("A", "B", "C"))
            .contains("A", "C");

        // todo containsAll - проверяет, что коллекция содержит все элементы другой коллекции.
        Assertions.assertThat(List.of("A", "B", "C"))
            .containsAll(List.of("A", "C"));

        // todo containsExactly - содержит только указанные элементы в том же порядке.
        Assertions.assertThat(List.of("A", "B", "C"))
            .containsExactly("A", "B", "C");

        // todo containsExactlyInAnyOrder - коллекция содержит все указанные элементы, независимо от порядка.
        Assertions.assertThat(List.of("A", "B", "C"))
            .containsExactlyInAnyOrder("C", "A", "B");

        // todo containsOnlyOnce - указанные элементы присутствуют в коллекции только один раз.
        Assertions.assertThat(List.of("A", "B", "C"))
            .containsOnlyOnce("A", "B");

        // todo doesNotContain - коллекция не содержит указанные элементы
        Assertions.assertThat(List.of("A", "B", "C"))
            .doesNotContain("D", "E");

        // todo containsSequence - коллекция содержит элементы в указанной последовательности
        Assertions.assertThat(List.of("A", "B", "C", "D"))
            .containsSequence("B", "C");

        // todo containsSubsequence - коллекция содержит указанные элементы, даже если они идут с промежутками
        Assertions.assertThat(List.of("A", "B", "C", "D"))
            .containsSubsequence("A", "C");

        // todo isEmpty, isNotEmpty - проверяют, пустая ли коллекция.
        Assertions.assertThat(Collections.emptyList())
            .isEmpty();

        Assertions.assertThat(List.of("A"))
            .isNotEmpty();

        // todo hasSize - проверяет размер коллекции.
        Assertions.assertThat(List.of("A", "B", "C"))
            .hasSize(3);

        // todo allMatch, anyMatch, noneMatch - проверяют, что все, хотя бы один или не один элемент удовлетворяют указанному предикату.
        Assertions.assertThat(List.of(2, 4, 6))
            .allMatch(i -> i % 2 == 0);
        Assertions.assertThat(List.of(2, 4, 6))
            .anyMatch(i -> i == 4);
        Assertions.assertThat(List.of(2, 4, 6))
                .noneMatch(i -> i >= 40);

        // todo isSubsetOf - проверяет, что коллекция содержит все элементы другой коллекции.
        Assertions.assertThat(List.of("A", "B"))
            .isSubsetOf("A", "B", "C");
    }
}
