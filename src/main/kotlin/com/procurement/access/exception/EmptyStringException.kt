package com.procurement.access.exception

class EmptyStringException(val attributeName: String) : RuntimeException(attributeName)