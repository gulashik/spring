import org.gradle.api.initialization.resolve.RepositoriesMode

/*
 * settings.gradle.kts проекта dwh.
 *
 * Что важно: подключаем mavenLocal() в dependencyResolutionManagement, чтобы
 * Gradle мог найти стартер additional-sources-postgres, опубликованный соседним
 * проектом командой `./gradlew publishToMavenLocal`.
 *
 * mavenLocal() ставим ПЕРВЫМ — иначе Gradle сначала пойдёт на Maven Central
 * и (правильно) не найдёт там {@code 0.0.1-SNAPSHOT}, что замедлит сборку.
 *
 * Best-practice (важно знать):
 *   - В рабочих проектах mavenLocal() обычно ставят за Nexus/Artifactory, и
 *     зависимость стартера хранится во внутреннем репозитории. Сценарий
 *     «через mavenLocal» — учебный/локальный.
 */
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal() // здесь живёт наш стартер после publishToMavenLocal
        mavenCentral()
    }
}

rootProject.name = "dwh"
