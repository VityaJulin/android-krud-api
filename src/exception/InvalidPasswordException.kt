package com.example.exception

import java.lang.RuntimeException

class InvalidPasswordException(message: String) : RuntimeException(message)
