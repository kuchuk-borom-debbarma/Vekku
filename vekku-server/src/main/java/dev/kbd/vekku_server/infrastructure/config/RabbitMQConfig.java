package dev.kbd.vekku_server.infrastructure.config;

import dev.kbd.vekku_server.content.api.ContentEvents;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${vekku.rabbitmq.queue}")
    private String queueName;

    @Value("${vekku.rabbitmq.exchange}")
    private String exchange;

    @Value("${vekku.rabbitmq.routingkey}")
    private String routingKey;

    public static final String CONTENT_CREATION_QUEUE = "content.creation.queue";

    @Bean
    public Queue queue() {
        return new Queue(queueName);
    }

    @Bean
    public Queue contentCreationQueue() {
        return new Queue(CONTENT_CREATION_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public Binding contentCreationBinding(Queue contentCreationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(contentCreationQueue).to(exchange).with(ContentEvents.CONTENT_CREATED);
    }

    @Bean
    public MessageConverter converter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
