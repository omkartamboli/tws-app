package com.trading.app.tradingapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TradingAppApplication {

	private static ConfigurableApplicationContext context;

	private static final Logger LOGGER = LoggerFactory.getLogger(TradingAppApplication.class);

	public static void main(String[] args) {
		context = SpringApplication.run(TradingAppApplication.class, args);
	}

	public static void restart() {
		ApplicationArguments args = context.getBean(ApplicationArguments.class);

		Thread thread = new Thread(() -> {
			LOGGER.info("Closing current spring-boot application context ....... ");
			context.close();
            try {
				LOGGER.info("Waiting for 45 seconds for TWS to restore and then we will restart the spring-boot application.");
                Thread.sleep(45000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            LOGGER.info("Restarting the application now");
			context = SpringApplication.run(TradingAppApplication.class, args.getSourceArgs());
		});

		thread.setDaemon(false);
		thread.start();
	}
}
