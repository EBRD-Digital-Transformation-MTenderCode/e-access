package com.procurement.access.exception

class EmptyStringException(val path: String) : RuntimeException(path)