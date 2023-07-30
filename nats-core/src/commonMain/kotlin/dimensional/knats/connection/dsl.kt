package dimensional.knats.connection

import dimensional.knats.annotations.InternalNatsApi

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
