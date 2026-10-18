package com.kyawzay.blueconnect.domain.usecase

import com.kyawzay.blueconnect.data.remote.GeminiRemoteApiService

class SynthesizeAiSignalUseCase(
    private val apiService: GeminiRemoteApiService
) {
    suspend operator fun invoke(brand: String, model: String, button: String): Result<String> {
        return try {
            val hex = apiService.findIrProtocolFromAi(brand, model, button)
            if (hex != null) {
                Result.success(hex)
            } else {
                Result.failure(NoSuchElementException("No IR protocol found for $brand $model $button"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
