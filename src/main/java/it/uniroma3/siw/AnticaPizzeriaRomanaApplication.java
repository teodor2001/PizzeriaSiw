package it.uniroma3.siw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import it.uniroma3.siw.config.XmlPropertySourceFactory;
@SpringBootApplication
@PropertySource(value = "classpath:secrets.xml", factory = XmlPropertySourceFactory.class, ignoreResourceNotFound = true)
public class AnticaPizzeriaRomanaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnticaPizzeriaRomanaApplication.class, args);
	}

}
