package ru.gulash.example.mapstruct;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.gulash.example.dto.UserDto;
import ru.gulash.example.entity.UserEntity;
import ru.gulash.example.info.Country;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-11-17T14:07:17+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    private final DateTimeFormatter dateTimeFormatter_yyyy_MM_dd_0159776256 = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );

    @Override
    public UserEntity toEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        if ( userDto.getFName() != null ) {
            userEntity.setFirstName( userDto.getFName() );
        }
        if ( userDto.getPhoneNumber() != null ) {
            userEntity.setPhoneNumber( userDto.getPhoneNumber() );
        }
        else {
            userEntity.setPhoneNumber( "-" );
        }
        String code = userDtoCountryCode( userDto );
        if ( code != null ) {
            userEntity.setCountryCode( code );
        }
        if ( userDto.getId() != null ) {
            userEntity.setId( userDto.getId() );
        }
        if ( userDto.getLastName() != null ) {
            userEntity.setLastName( userDto.getLastName() );
        }
        if ( userDto.getEmail() != null ) {
            userEntity.setEmail( userDto.getEmail() );
        }
        if ( userDto.getDescription() != null ) {
            userEntity.setDescription( userDto.getDescription() );
        }

        userEntity.setCreatedAt( java.time.LocalDateTime.now() );
        userEntity.setCode( "entityCode" );

        return userEntity;
    }

    @Override
    public UserDto toDto(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        if ( userEntity.getBirthday() != null ) {
            userDto.setAge( calculateAge( dateTimeFormatter_yyyy_MM_dd_0159776256.format( userEntity.getBirthday() ) ) );
        }
        if ( userEntity.getPhoneNumber() != null ) {
            userDto.setPhoneNumber( userEntity.getPhoneNumber() );
        }
        else {
            userDto.setPhoneNumber( userEntity.getFirstName() + " has no number" );
        }
        if ( userEntity.getFirstName() != null ) {
            userDto.setFName( userEntity.getFirstName() );
        }
        if ( userEntity.getId() != null ) {
            userDto.setId( userEntity.getId() );
        }
        if ( userEntity.getLastName() != null ) {
            userDto.setLastName( userEntity.getLastName() );
        }
        if ( userEntity.getEmail() != null ) {
            userDto.setEmail( userEntity.getEmail() );
        }
        if ( userEntity.getDescription() != null ) {
            userDto.setDescription( userEntity.getDescription() );
        }
        if ( userEntity.getCreatedAt() != null ) {
            userDto.setCreatedAt( userEntity.getCreatedAt() );
        }

        userDto.setCountry( new Country("empty","empty","empty") );
        userDto.setCode( "dtoCode" );

        return userDto;
    }

    @Override
    public List<UserDto> toDtoList(List<UserEntity> userEntities) {
        if ( userEntities == null ) {
            return null;
        }

        List<UserDto> list = new ArrayList<UserDto>( userEntities.size() );
        for ( UserEntity userEntity : userEntities ) {
            list.add( toDto( userEntity ) );
        }

        return list;
    }

    @Override
    public void updateEntity(UserEntity entity, UserDto updateDto) {
        if ( updateDto == null ) {
            return;
        }

        if ( updateDto.getFName() != null ) {
            entity.setFirstName( updateDto.getFName() );
        }
        String code = userDtoCountryCode( updateDto );
        if ( code != null ) {
            entity.setCountryCode( code );
        }
        if ( updateDto.getId() != null ) {
            entity.setId( updateDto.getId() );
        }
        if ( updateDto.getLastName() != null ) {
            entity.setLastName( updateDto.getLastName() );
        }
        if ( updateDto.getEmail() != null ) {
            entity.setEmail( updateDto.getEmail() );
        }
        if ( updateDto.getDescription() != null ) {
            entity.setDescription( updateDto.getDescription() );
        }
        if ( updateDto.getCreatedAt() != null ) {
            entity.setCreatedAt( updateDto.getCreatedAt() );
        }
        if ( updateDto.getCode() != null ) {
            entity.setCode( updateDto.getCode() );
        }
        if ( updateDto.getPhoneNumber() != null ) {
            entity.setPhoneNumber( updateDto.getPhoneNumber() );
        }
    }

    private String userDtoCountryCode(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }
        Country country = userDto.getCountry();
        if ( country == null ) {
            return null;
        }
        String code = country.code();
        if ( code == null ) {
            return null;
        }
        return code;
    }
}
