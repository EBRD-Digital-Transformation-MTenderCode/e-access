package com.procurement.access.infrastructure.exception

class CoefficientException(coefficient: String, description: String = "") :
    RuntimeException("Incorrect coefficient: '$coefficient'. $description")