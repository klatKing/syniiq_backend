package com.syniiq.syniiq_backend.websocket;

/**
 * Message envoyé aux clients.
 * action : CREATED, UPDATED ou DELETED
 * data   : l'objet complet (CREATED / UPDATED) ou l'id (DELETED)
 */
public record RealtimeEvent(String action, Object data) {
}