package com.sprboot.testcontext.jdbcpostgre;

import com.sprboot.testcontext.jdbcpostgre.config.PrimaryDataSourceConfig;
import com.sprboot.testcontext.jdbcpostgre.config.SecondaryDataSourceConfig;
import com.sprboot.testcontext.jdbcpostgre.service.AuthorDaoJdbc;
import com.sprboot.testcontext.jdbcpostgre.service.DailyActivityDaoJdbc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@DisplayName("Примеры по @JdbcTest")
@ActiveProfiles("test")
@JdbcTest // частично поднимается контекст
@Import( // добавляем нужные имплементации бинов
	{
		AuthorDaoJdbc.class,
		DailyActivityDaoJdbc.class,
		PrimaryDataSourceConfig.class, SecondaryDataSourceConfig.class // todo указываем конфиги по нужному конфиги по БД
	}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // запрещает замену data source
// Отключить обёртывание в транзакцию т.е. @BeforeTransaction и @AfterTransaction не будут работать
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JdbcPostgreApplicationTests {

	@Autowired // того чего Import сделали
	private AuthorDaoJdbc authorDao;
	@Autowired // того чего Import сделали
	private DailyActivityDaoJdbc dailyActivityDao;
	@Autowired
	private ApplicationContext applicationContext;

	@BeforeTransaction // Отключается через @Transactional(propagation = Propagation.NOT_SUPPORTED)
	void beforeTransaction() {
		System.out.println("beforeTransaction");
	}

	@AfterTransaction // Отключается через @Transactional(propagation = Propagation.NOT_SUPPORTED)
	void afterTransaction() {
		System.out.println("afterTransaction");
	}

	@DisplayName("Откатывается")
	@Transactional // включаем rollback по окончанию транзакции. Если на классе выключено @Transactional(propagation = Propagation.NOT_SUPPORTED)
	@Test
	void enableRollbackAtTheEndOfTransaction() {
		System.out.println("method actions");
		dailyActivityDao.newRecGenerate("@Transactional");
	}

	@DisplayName("Не откатывается транзакция")
	@Rollback(value=false) // или @Commit выключаем откат по rollback по окончанию транзакций
	@Test
	void disableRollbackAtTheEndOfTransaction() {
		System.out.println("method actions");
		dailyActivityDao.newRecGenerate("@Rollback(value=false)");
	}
}