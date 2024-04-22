package nats.core.connection

import nats.core.NatsServer
import nats.core.annotations.InternalNatsApi
import naibu.ext.intoOrNull

/**
 * Whether this connection is currently connected.
 */
@OptIn(InternalNatsApi::class)
public val Connection.isConnected: Boolean get() = state.value is ConnectionState.Connected

/**
 * Whether this connection has been detached.
 */
@OptIn(InternalNatsApi::class)
public val Connection.isDetached: Boolean get() = state.value == ConnectionState.Detached

/**
 * The [NATS server][NatsServer] that we're currently connected to.
 */
@OptIn(InternalNatsApi::class)
public val Connection.connectedServer: NatsServer? get() = state.value.intoOrNull<ConnectionState.Connected>()?.server
