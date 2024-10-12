package ru.gulash.spring.mongock.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import ru.gulash.spring.domain.Person;
import ru.gulash.spring.repostory.PersonRepository;

@ChangeLog /*todo @ChangeLog - лог изменений аналог файлов наката flyway*/
public class DatabaseChangelog {
    /* todo @ChangeSet - методы с миграциями*/
    @ChangeSet(order = "001", id = "dropDb", author = "stvort", runAlways = true)
    public void dropDb(MongoDatabase db) {
        db.drop();
    }

    @ChangeSet(order = "002", id = "insertLermontov", author = "ydvorzhetskiy")
    public void insertLermontov(MongoDatabase db) {
        MongoCollection<Document> myCollection = db.getCollection("persons");
        var doc = new Document().append("first_name", "Lermontov").append("email", "Lermontov@gmail.com");
        myCollection.insertOne(doc);
    }

    @ChangeSet(order = "003", id = "insertPushkin", author = "stvort")
    public void insertPushkin(PersonRepository repository) {
        repository.save(new Person("Pushkin","Pushkin@mail.ru"));
    }
}
