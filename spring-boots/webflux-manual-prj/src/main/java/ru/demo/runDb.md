```shell
podman stop pg-docker && \
podman run --rm --name pg-docker \
-e POSTGRES_PASSWORD=pwd \
-e POSTGRES_USER=usr \
-e POSTGRES_DB=demoDB \
-p 5430:5432 \
-d \
postgres:15
```