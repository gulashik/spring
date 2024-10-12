### Смотрим todo-ки

#### Зависимости
[build.gradle.kts](build.gradle.kts)

#### Тесты
@MockkBean - отдельная зависимость NINJA-SQUAD</br>
[UserControllerTest.kt](src%2Ftest%2Fkotlin%2Fcom%2Fsprboot%2Fmockkexample%2Fkotlinmockkexample%2Fcontroller%2FUserControllerTest.kt)

clearMocks(userService) - Удалить все следы взаимодействия с моками (например, вызовы методов, проверки состояний и т.д.) между тестами.</br>
[UserControllerTest.kt](src%2Ftest%2Fkotlin%2Fcom%2Fsprboot%2Fmockkexample%2Fkotlinmockkexample%2Fcontroller%2FUserControllerTest.kt)

mockk<X>(), spyk<X>()</br>
mockk - [AnswersTest.kt](src%2Ftest%2Fkotlin%2Fcom%2Fsprboot%2Fmockkexample%2Fkotlinmockkexample%2Fanswer%2FAnswersTest.kt)</br>
spyk - [CalculatorTest.kt](src%2Ftest%2Fkotlin%2Fcom%2Fsprboot%2Fmockkexample%2Fkotlinmockkexample%2Fcalc%2FCalculatorTest.kt)

every </br>
every + return + andThen</br>
[UserControllerTest.kt](src%2Ftest%2Fkotlin%2Fcom%2Fsprboot%2Fmockkexample%2Fkotlinmockkexample%2Fcontroller%2FUserControllerTest.kt)</br>
every + answer</br>
[AnswersTest.kt](src%2Ftest%2Fkotlin%2Fcom%2Fsprboot%2Fmockkexample%2Fkotlinmockkexample%2Fanswer%2FAnswersTest.kt)

slot - используется для захвата аргументов, переданных в замоканый метод</br>
[SlotTest.kt](src%2Ftest%2Fkotlin%2Fcom%2Fsprboot%2Fmockkexample%2Fkotlinmockkexample%2Fargs%2FSlotTest.kt)

verify, verifySequence</br>
[UserControllerTest.kt](src%2Ftest%2Fkotlin%2Fcom%2Fsprboot%2Fmockkexample%2Fkotlinmockkexample%2Fcontroller%2FUserControllerTest.kt)



