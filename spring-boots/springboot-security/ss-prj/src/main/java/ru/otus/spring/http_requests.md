

```shell
clear 

curl http://localhost:8080/public
```
```shell
clear

B64=$(printf user:password | base64)
echo "converted credential $B64"
curl http://localhost:8080/authenticated \
-H "Authorization: Basic $B64"
```
