import org.gradle.api.initialization.resolve.RepositoriesMode

/*
 * settings.gradle.kts — корневой конфиг Gradle-проекта.
 *
 * Здесь объявляется ИМЯ проекта и подключаются модули. Стартер сделан как
 * один независимый одномодульный проект (намеренно, чтобы пользователь видел
 * минимально необходимое окружение для публикации стартера в Maven Local).
 *
 * Best-practice:
 *   - Имя проекта (rootProject.name) должно совпадать с artifactId, чтобы при
 *     публикации артефакт назывался предсказуемо.
 *   - pluginManagement и dependencyResolutionManagement (FAIL_ON_PROJECT_REPOS)
 *     гарантируют, что все плагины и зависимости берутся из централизованных
 *     репозиториев — никто из подмодулей не сможет «протащить» свой репозиторий.
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
        mavenCentral()
    }
}

rootProject.name = "additional-sources-postgres"
