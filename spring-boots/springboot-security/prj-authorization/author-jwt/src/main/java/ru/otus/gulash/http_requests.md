
### Получаем JWT
```shell
clear

BASIC_AUTHOR=$(printf 'user:user' | base64)
echo $BASIC_AUTHOR

AUTHORIZATION_BASIC_HEADER="Authorization: Basic $BASIC_AUTHOR"
echo $AUTHORIZATION_BASIC_HEADER

JWT=$(curl --location 'http://localhost:8080/token' \
--header 'Accept: */*' \
--header 'Content-Type: application/json' \
--header 'Cache-Control: no-cache' \
--header $AUTHORIZATION_BASIC_HEADER)
echo $JWT
```

### Формируем Header "Authorization: Bearer"
```shell
clear

echo "\\n------JWT------\\n $JWT \\n---------------"

AUTHORIZATION_BEARER_HEADER="Authorization: Bearer $JWT"
echo $AUTHORIZATION_BEARER_HEADER
```

### Используем JWT
```shell
clear

echo ------
curl --location 'http://localhost:8080/hello' --header $AUTHORIZATION_BEARER_HEADER 

echo ------
curl --location 'http://localhost:8080/userinfo' --header $AUTHORIZATION_BEARER_HEADER

```