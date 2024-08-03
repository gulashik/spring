package ru.gulash.example.mapstruct;


import org.mapstruct.*;
import ru.gulash.example.dto.UserDto;
import ru.gulash.example.entity.UserEntity;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

// todo декларативно описываем маппинг
@Mapper(
    //componentModel = "spring" - создает Spring bean
    componentModel = "spring",
    // как MapStruct будет реагировать, если в целевом объекте остаются свойства, которые не были явно сопоставлены
    // ReportingPolicy.IGNORE - Игнорирует необработанные целевые свойства.
    // ReportingPolicy.WARN - Генерирует предупреждение во время компиляции для каждого необработанного целевого свойства.
    // ReportingPolicy.ERROR - Приводит к ошибке компиляции, если есть необработанные целевые свойства.
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserMapper {
//----------------------------------------------
    // ignore = true - не маппимм
    @Mapping(target = "birthday", ignore = true)

    // expression = выражение
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    //  @Mapping(target = "isActive", expression = "java(user.isEnabled() && user.getLastLogin() != null)")

    // source -> target сопоставление имён
    @Mapping(source = "FName", target = "firstName")

    // Констатное значение
    @Mapping(target = "code", constant = "entityCode")

    // Если придёт null то будет defaultValue строковое
    @Mapping(target = "phoneNumber", defaultValue = "-")

    // Вложенные поля
    @Mapping(source = "country.code", target = "countryCode")

    // Остальное маппим по явному совпадению имён
    UserEntity toEntity(UserDto userDto);

//----------------------------------------------
    // Какстомная логика получения поля
    @Mapping(source = "birthday", target = "age", dateFormat = "yyyy-MM-dd", qualifiedByName = "calculateAge")
    // Если придёт null то будет результат выражения
    @Mapping(target = "phoneNumber", defaultExpression = "java(userEntity.getFirstName() + \" has no number\")")
    @Mapping(target = "country", expression = "java(new Country(\"empty\",\"empty\",\"empty\"))")
    // ---
    @Mapping(source = "firstName", target = "FName")
    @Mapping(target = "code", constant = "dtoCode")
    UserDto toDto(UserEntity userEntity);

//----------------------------------------------
    List<UserDto> toDtoList(List<UserEntity> userEntities);

//----------------------------------------------
    // обновляет существующую сущность данными из DTO
    @BeanMapping(
        // Частичное обновление сущности
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(source = "FName", target = "firstName")
    @Mapping(target = "birthday", ignore = true)
    @Mapping(source = "country.code", target = "countryCode")
    void updateEntity(@MappingTarget UserEntity entity, UserDto updateDto);

//----------------------------------------------
// Кастомная логика
//----------------------------------------------
    // Метод для вычисления возраста (можно пометить @Named, если используется QualifiedByName)
    @Named("calculateAge")
    default int calculateAge(String birthDate) {
        LocalDate birth = LocalDate.parse(birthDate);
        return Period.between(birth, LocalDate.now()).getYears();
    }

}