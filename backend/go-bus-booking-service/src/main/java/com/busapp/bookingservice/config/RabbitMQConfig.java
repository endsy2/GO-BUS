package com.busapp.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    
    // Single queue for all notifications
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    
    // Routing keys (still useful for message metadata)
    public static final String BOOKING_CREATED_KEY = "booking.created";
    public static final String BOOKING_CANCELLED_KEY = "booking.cancelled";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
    public static final String REFUND_PROCESSED_KEY = "payment.refunded";
    public static final String ADMIN_ACTION_KEY = "admin.action";
    public static final String CUSTOM_ALERT_KEY = "notification.custom";
    
    // Dead Letter Queue
    public static final String DLQ_EXCHANGE = "notification.dlx";
    public static final String DLQ_QUEUE = "notification.dlq";
    public static final String DLQ_ROUTING_KEY = "notification.failed";

    /**
     * Message converter for JSON serialization
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Listener container factory with JSON converter
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false); // Don't requeue failed messages
        return factory;
    }

    // ── Exchanges ──────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLQ_EXCHANGE, true, false);
    }

    // ── Queues ─────────────────────────────────────────────────────────────────

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    // ── Bindings ───────────────────────────────────────────────────────────────

    /**
     * Bind single queue to all notification routing keys using wildcard
     * This catches all messages with routing keys starting with "notification."
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with("notification.#");  // Wildcard: matches all notification.* keys
    }

    /**
     * Also bind specific routing keys for backward compatibility
     */
    @Bean
    public Binding bookingCreatedBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(BOOKING_CREATED_KEY);
    }

    @Bean
    public Binding bookingCancelledBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(BOOKING_CANCELLED_KEY);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding refundProcessedBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(REFUND_PROCESSED_KEY);
    }

    @Bean
    public Binding adminActionBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(ADMIN_ACTION_KEY);
    }

    @Bean
    public Binding customAlertBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(CUSTOM_ALERT_KEY);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }
}
