смотрим todo

POM родительский
[pom.xml](pom.xml)

POM по модулю
[pom.xml](greeting-app%2Fpom.xml)

Пропы по модулю
[application.yml](greeting-app%2Fsrc%2Fmain%2Fresources%2Fapplication.yml), [AppProps.java](greeting-app%2Fsrc%2Fmain%2Fjava%2Fru%2Fgulash%2Fgreetingapp%2Fconfig%2FAppProps.java)

Главный класс
[GreetingAppApplication.java](greeting-app%2Fsrc%2Fmain%2Fjava%2Fru%2Fgulash%2Fgreetingapp%2FGreetingAppApplication.java)

ApplicationRunner(то что будет запущено т.к. implements ApplicationRunner.run)
[AppRunner.java](greeting-app%2Fsrc%2Fmain%2Fjava%2Fru%2Fgulash%2Fgreetingapp%2Frunner%2FAppRunner.java)

---

Интернализация
опции
[application.yml](greeting-app%2Fsrc%2Fmain%2Fresources%2Fapplication.yml)

Файлы на нужном языке 

default file
[messages.properties](greeting-app%2Fsrc%2Fmain%2Fresources%2Fmessages.properties) 

языки
[messages_en_US.properties](greeting-app%2Fsrc%2Fmain%2Fresources%2Fmessages_en_US.properties), 
[messages_ru_RU.properties](greeting-app%2Fsrc%2Fmain%2Fresources%2Fmessages_ru_RU.properties)

использование MessageSource
[AppRunner.java](greeting-app%2Fsrc%2Fmain%2Fjava%2Fru%2Fgulash%2Fgreetingapp%2Frunner%2FAppRunner.java)