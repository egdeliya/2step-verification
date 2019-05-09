## 2step-verification

[![Build Status](https://travis-ci.com/egdeliya/2step-verification.svg?token=wFxdXQB6FhLzkWHEHfW3&branch=master)](https://travis-ci.com/egdeliya/2step-verification)

Двухфакторная авторизация - апи, которое по пользователю определяет, логинлся ли он раньше с этого устройства, да -> ok, 
не логинился - шлём смску (дёргаем стороннее апи), если код совпадает, то ок, нет - идёт в бан на час

### Требования к коду

* [ ] все бд и сервисы подняты в докере
* [ ] метрики кол-ва и времени исполнения запросов в statsd+graphite+grafana
* [ ] ошибки и ворнинги в ELK
* [ ] походы в БД должны быть с глобальным (и возможно локальным) кэшом в redis
* [ ] апи и компоненты (elk, statsd, redis и пр) поднимается через docker-compose
* [x] БД - cassandra
* [x] (python 3.6+ с typehint'ами или scala) и с неблокирующим походом во все внешние хранилища
* [x] unit-test'ы с моками на походы во внешние сервисы
* [ ] скрипт инициализации БД
* [ ] проверка нагрузки Jmeter/Яндекс танк/ Locust
* [x] код на github

### Зависимости 

[`sbt`](https://www.scala-sbt.org/1.0/docs/Setup.html)

[`build.sbt`](https://github.com/egdeliya/2step-verification/blob/master/build.sbt) +  [`project/Dependencies.scala`](https://github.com/egdeliya/2step-verification/blob/master/project/Dependencies.scala)

### Публичное API

Пользователи могут

* зарегистрироваться в системе

 `POST /register`
```json
    {
      "phoneNumber": "some phonenumber",
      "password": "secure password"
    }
```
    
Ответ:  

```HTTP/1.1 201 OK```

### Запуск приложения
 
 Заполнить конфигурационный файл [`application.conf`](https://github.com/egdeliya/2step-verification/blob/master/src/main/resources/application_example.conf)
 
 ```
  docker-compose up
  sbt "runMain ApplicationApp"
 ```
 
 Приложение запустится на [`http://localhost:8080`](http://localhost:8080)
 
 ### Запуск тестов
 
 ```sbt test```
 
### Непрерывная интеграция

[`Travis`](https://travis-ci.org/egdeliya/2step-verification)

### Измерение покрытия кода

[`CodeCov`](https://codecov.io/gh/egdeliya/2step-verification/branch/master)

[license]: LICENSE
