package com.rtosProject2;

/**
 * Generic class to pass messages between processes. Payload is of type T. To carry multiple data items in
 * a message, either put them in a list or if different types create a data transfer class or a tuple.
 * @param <T>
 */
public class Message <T> {

    public enum PayloadType {
        Test,
        Position
    }

    private final PayloadType _payloadType;
    private final T _payload;

    public Message (PayloadType type, T payload) {
        _payloadType = type;
        _payload = payload;
    }

    public PayloadType getPayloadType() {
        return _payloadType;
    }

    public T getPayload() {
        return _payload;
    }
}
