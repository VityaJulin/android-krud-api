package com.example.exception

import java.lang.RuntimeException

class ConfigurationException(message: String): RuntimeException(message) {
}