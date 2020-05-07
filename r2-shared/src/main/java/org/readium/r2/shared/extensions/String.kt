/*
 * Module: r2-shared-kotlin
 * Developers: Mickaël Menu
 *
 * Copyright (c) 2020. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared.extensions

import org.joda.time.DateTime
import java.security.MessageDigest
import java.util.*

fun String.iso8601ToDate(): Date? =
    try {
        DateTime(this).toDate()
    } catch (e: Exception) {
        null
    }

internal enum class HashAlgorithm(val key: String) {
    MD5("MD5"),
    SHA256("SHA-256")
}

internal fun String.hash(algorithm: HashAlgorithm): String =
    MessageDigest
        .getInstance(algorithm.key)
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
