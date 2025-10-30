package com.achievemeaalk.freedjf.domain.usecase

import com.achievemeaalk.freedjf.domain.repository.InsightsRepository
import com.achievemeaalk.freedjf.ui.dashboard.FinancialInsight
import javax.inject.Inject

class GenerateInsightsUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    suspend operator fun invoke(): List<FinancialInsight> {
        return insightsRepository.generateInsights()
    }
}
