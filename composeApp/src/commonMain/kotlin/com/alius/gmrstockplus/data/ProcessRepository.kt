package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Process

interface ProcessRepository {
    suspend fun listarProcesos(): List<Process>
}

expect fun getProcessRepository(plantId: String): ProcessRepository