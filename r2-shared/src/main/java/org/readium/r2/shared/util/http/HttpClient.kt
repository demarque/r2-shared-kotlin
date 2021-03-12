/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.shared.util.http

import org.readium.r2.shared.util.mediatype.MediaType
import java.util.*

/**
 * An HTTP client performs HTTP requests.
 *
 * You may provide a custom implementation, or use the [DefaultHttpClient] one which relies on
 * native APIs.
 */
interface HttpClient {

    /**
     * Fetches the resource from the given [request].
     */
    suspend fun fetch(request: HttpRequest): HttpTry<HttpFetchResponse>

    /**
     * Downloads a resource progressively.
     *
     * Useful in the context of streaming media playback.
     *
     * @param request Request to the downloaded resource.
     * @param range If provided, issue a byte range request.
     * @param receiveResponse Callback called when receiving the initial response, before consuming
     *        its body. You can also access it in the completion block after consuming the data.
     * @param consumeData Callback called for each chunk of data received. Callers are responsible
     *        to accumulate the data if needed.
     */
    suspend fun progressiveDownload(
        request: HttpRequest,
        range: LongRange? = null,
        receiveResponse: ((HttpResponse) -> Void)? = null,
        consumeData: (chunk: ByteArray, progress: Double?) -> Unit,
    ): HttpTry<HttpResponse>

}

data class HttpFetchResponse(
    val response: HttpResponse,
    val body: ByteArray,
)

/**
 * Represents a successful HTTP response received from a server.
 *
 * @param headers HTTP response headers, indexed by their name.
 * @param mediaType Media type sniffed from the `Content-Type` header and response body. Falls back
 *        on `application/octet-stream`.
 */
data class HttpResponse(
    val headers: Map<String, List<String>>,
    val mediaType: MediaType,
) {

    /**
     * Finds the first value of the first header matching the given name.
     * In keeping with the HTTP RFC, HTTP header field names are case-insensitive.
     */
    fun valueForHeader(name: String): String? =
        valuesForHeader(name).firstOrNull()

    /**
     * Finds all the values of the first header matching the given name.
     * In keeping with the HTTP RFC, HTTP header field names are case-insensitive.
     */
    fun valuesForHeader(name: String): List<String> {
        @Suppress("NAME_SHADOWING")
        val name = name.toLowerCase(Locale.ROOT)
        for ((n, v) in headers) {
            if (name == n.toLowerCase(Locale.ROOT)) {
                return v
            }
        }
        return emptyList()
    }

    /**
     * Indicates whether this server supports byte range requests.
     */
    val acceptsByteRanges: Boolean get() {
        return valueForHeader("Accept-Ranges")?.toLowerCase(Locale.ROOT) == "bytes"
            || valueForHeader("Content-Range")?.toLowerCase(Locale.ROOT)?.startsWith("bytes") == true
    }

    /**
     * The expected content length for this response, when known.
     *
     * Warning: For byte range requests, this will be the length of the chunk, not the full
     * resource.
     */
    val contentLength: Long? get() =
        valueForHeader("Content-Length")
            ?.toLongOrNull()
            ?.takeIf { it >= 0 }

}