package com.procurement.access.infrastructure.handler.v2.base

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.application.service.tryDeserialization
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.lib.functional.Result
import com.procurement.access.utils.toJson

abstract class AbstractHistoricalHandler<R : Any>(
    private val transform: Transform,
    private val historyRepositoryOld: HistoryRepositoryOld,
    private val historyRepositoryNew: HistoryRepositoryNew,
    private val logger: Logger
) : AbstractHandler<ApiResponseV2>(logger = logger) {

    override fun handle(descriptor: CommandDescriptor): ApiResponseV2 {

        val history = historyRepositoryNew.getHistory(descriptor.id, action)
            .onFailure { return responseError(fail = it.reason, version = version, id = descriptor.id) }
            ?: historyRepositoryOld.getHistory(descriptor.id, action)
                .onFailure { return responseError(fail = it.reason, version = version, id = descriptor.id) }

        if (history != null) {
            return history.tryDeserialization<ApiResponseV2.Success>(transform)
                .onFailure {
                    return responseError(
                        id = descriptor.id,
                        version = version,
                        fail = Fail.Incident.ParsingIncident() //TODO
                    )
                }
        }

        return when (val result = execute(descriptor)) {
            is Result.Success -> ApiResponseV2.Success(id = descriptor.id, version = version, result = result.value)
                .also {
                    logger.info("'${action.key}' has been executed. Result: '${toJson(it)}'")
                    historyRepositoryNew.saveHistory(descriptor.id, action, toJson(it))
                }
            is Result.Failure -> responseError(id = descriptor.id, version = version, fail = result.reason)
        }
    }

    abstract fun execute(descriptor: CommandDescriptor): Result<R, Fail>
}
