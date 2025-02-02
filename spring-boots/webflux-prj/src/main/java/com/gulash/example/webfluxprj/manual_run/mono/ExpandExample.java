package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ExpandExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        recursionExample();
        hierarchyExample();
    }

    private static void recursionExample() {
        Mono.just("A")
            // todo expand, expandDeep  - рекурсивно генерирует элементы, для каждого элемента генерирует новый Publisher

            // todo Направление обработки
            // todo expandDeep - если нужно приоритетно обрабатывать В ГЛУБИНУ структуры.
            //      Берём "a" - генерируем все элементы, потом "b" - генерируем все элементы
            // todo expand - если нужно приоритетно обрабатывать НА ОДНОМ УРОВНЕ ПЕРЕД ПЕРЕХОДОМ К СЛЕДУЮЩЕМУ.
            //      Берём "a" - генерируем один элемент(aa), потом "b" - генерируем один элемент(bb)
            //      Берём "aa" - генерируем следующий один элемент(aaa), потом "bb" - генерируем следующий один элемент(bbb)
            //      и так по одному уровню за раз(один уровень = элементы потока)
            //  Mono.empty() - заканчивает генерацию
            .expandDeep(
            //.expand(
                (String s) -> {
                    System.out.println("s: " + s);
                    return s.length() < 4 // todo условие где можно выйти из рекурсии
                        ? Mono.just(s + "@") // todo ВОЗВРАЩАЕМ и получаем НА ВХОД В следующую итерацию
                        : Mono.empty(); // todo Mono.empty() - заканчивает рекурсию
                }
            )
            .subscribe(System.out::println);
    /*
    -- expand(breadth first) - перебор на одно уровне
        A
        s: A
        A@
        s: A@ <- один уровень это А
        A@@
        s: A@@
        s: B@@
        A@@@
        s: A@@@
    -- expandDeep(deep first) - законченный перебор каждого элемента
        A
        s: A <- один элемент пока не закончим это А
        A@
        s: A@
        A@@
        s: A@@
        A@@@
        s: A@@@ <- закончили текущий элемент это А
    */
    }

    private static void hierarchyExample() {
        class Node {
            String name;
            List<Node> children;

            Node(String name, Node... children) {
                this.name = name;
                this.children = List.of(children);
            }
        }

        Node root = new Node("root",
            new Node("child1",
                new Node("child1.1"),
                new Node("child1.2")
            ),
            new Node("child2")
        );

        Mono.just(root)
            // todo пример с иерархией
            //.expandDeep(node -> Flux.fromIterable(node.children))
            .expand(node -> Flux.fromIterable(node.children))
            .map(node -> node.name)
            .subscribe(System.out::println);
    }

    // ожидалка окончания Disposable
    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> {
                while (!disposable.isDisposed()) {
                }
            }
        );
    }
}
