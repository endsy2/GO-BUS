package com.busapp.bookingservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotInitializer {

    private final ApplicationContext applicationContext;
    private final TelegramConfig telegramConfig;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!telegramConfig.isEnabled()) {
            log.info("Telegram bot is disabled");
            return;
        }

        try {
            // Small delay to ensure all beans are fully initialized
            Thread.sleep(2000);
            
            // Get the bean as LongPollingBot interface to avoid proxy issues
            LongPollingBot bot = applicationContext.getBean(LongPollingBot.class);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            log.info("Telegram bot registered successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to initialize Telegram bot: {}", e.getMessage(), e);
        }
    }
}
