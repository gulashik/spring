# jakarta свалка

Зависимости</br>
[pom.xml](pom.xml)

@NotNull - проверяем на nullable</br>
@Valid - нужна валидация внутри</br>
[ParentEntity.java](src%2Fmain%2Fjava%2Fru%2Fgulash%2Fvalidation%2Fentity%2FParentEntity.java)

Методы validate, validateProperty, validateValue - получаем множество нарушений</br>
[RunnerClass.java](src%2Fmain%2Fjava%2Fru%2Fgulash%2Fvalidation%2FRunnerClass.java)

Атрибуты</br>
[RunnerClass.java](src%2Fmain%2Fjava%2Fru%2Fgulash%2Fvalidation%2FRunnerClass.java)

Группы валидации</br>
[ParentReasonOfValidation.java](src%2Fmain%2Fjava%2Fru%2Fgulash%2Fvalidation%2Fentity%2Fgroup%2FParentReasonOfValidation.java)</br>
[DefaultReasonOfValidation.java](src%2Fmain%2Fjava%2Fru%2Fgulash%2Fvalidation%2Fentity%2Fgroup%2FDefaultReasonOfValidation.java)

Тесты</br>
Сами создаём экземпляр [ValidatorUnitTest.java](src%2Ftest%2Fjava%2Fru%2Fgulash%2Fvalidation%2FValidatorUnitTest.java)</br>
Полный контекст [ValidationSpringBootTest.java](src%2Ftest%2Fjava%2Fru%2Fgulash%2Fvalidation%2FValidationSpringBootTest.java)</br>