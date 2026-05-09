### rebuild
```
./gradlew clean build 
```
### run
```bash
./gradlew clean build bootRun 
```

### stop
```bash
pkill -f 'org.gulash.demo.dwh.DwhApplication'
```

### actuator
```bash
clear
curl http://localhost:8080/actuator/health/db | jq
```