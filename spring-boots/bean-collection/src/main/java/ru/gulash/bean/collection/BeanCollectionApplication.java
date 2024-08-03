package ru.gulash.bean.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.gulash.bean.collection.config.BeanCls;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class BeanCollectionApplication implements ApplicationRunner {

	@Autowired // Просто указываем List и Тип Бина
	List<BeanCls> beanClsList;

	@Autowired // Просто указываем Map <String, ТипБина>
	Map<String, BeanCls> beanClsMap;

	public static void main(String[] args) {
		SpringApplication.run(BeanCollectionApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		// Список бинов
		// [BeanCls[className=Bean-1], BeanCls[className=Bean-2], BeanCls[className=Bean-3]]
		System.out.println(beanClsList);

		// Map<имяБина, Бин>
		// {getBean1=BeanCls[className=Bean-1], getBean2=BeanCls[className=Bean-2], getBean3=BeanCls[className=Bean-3]}
		System.out.println(beanClsMap);
	}
}
