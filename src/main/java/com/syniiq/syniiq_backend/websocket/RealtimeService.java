package com.syniiq.syniiq_backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(String topic, String action, Object data) {
        messagingTemplate.convertAndSend("/topic/" + topic, new RealtimeEvent(action, data));
    }
}