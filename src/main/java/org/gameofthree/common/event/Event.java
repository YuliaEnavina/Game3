package org.gameofthree.common.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private static final ObjectMapper om = new ObjectMapper();

    private EventType eventType;
    private Object payload;

    public static Event fromString(String message) throws JsonProcessingException {
        return om.readValue(message, Event.class);
    }

    @SneakyThrows
    public String toStringValue() {
        return om.writeValueAsString(this);
    }

}
