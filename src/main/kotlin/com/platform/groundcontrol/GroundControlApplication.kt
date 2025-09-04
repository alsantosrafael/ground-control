package com.platform.groundcontrol

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class GroundControlApplication

fun main(args: Array<String>) {
	runApplication<GroundControlApplication>(*args)
}
