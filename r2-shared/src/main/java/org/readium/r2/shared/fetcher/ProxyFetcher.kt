/*
 * Module: r2-shared-kotlin
 * Developers: Quentin Gliosca
 *
 * Copyright (c) 2020. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared.fetcher

import org.readium.r2.shared.publication.Link

/** Delegates the creation of a [Resource] to a [closure]. */
class ProxyFetcher(val closure: (Link) -> Resource) : Fetcher {

    override suspend fun links(): List<Link> = emptyList()

    override fun get(link: Link): Resource = closure(link)

    override suspend fun close() {}
}

