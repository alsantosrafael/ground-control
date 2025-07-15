package com.platform.groundcontrol.application.configuration

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.*

@Component
class MDCFilter : Filter {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        try {
            val httpRequest = request as? HttpServletRequest
            val requestId = httpRequest?.getHeader("X-Request-Id") ?: UUID.randomUUID().toString()
            MDC.put("requestId", requestId)

            MDC.put("method", httpRequest?.method ?: "UNKNOWN")
            MDC.put("uri", httpRequest?.requestURI ?: "UNKNOWN")

            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}