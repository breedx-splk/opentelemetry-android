/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.android.session

import java.time.Instant

interface Session {
    fun getId(): String

    fun getsStartTimestamp(): Long

    companion object {
        val NONE = DefaultSession("", -1)
    }

    data class DefaultSession(private val id: String, private val timestamp: Long): Session {
        override fun getId(): String {
            return id;
        }

        override fun getsStartTimestamp(): Long {
            return timestamp
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DefaultSession

            return id == other.id
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }


    }
}
