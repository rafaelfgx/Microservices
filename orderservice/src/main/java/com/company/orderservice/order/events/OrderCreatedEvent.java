package com.company.orderservice.order.events;

import java.util.UUID;

public record OrderCreatedEvent(UUID id) {
}
