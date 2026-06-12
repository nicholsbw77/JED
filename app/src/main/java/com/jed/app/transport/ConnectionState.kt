package com.jed.app.transport

/** Connection state shared by every adapter transport. */
enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
}
