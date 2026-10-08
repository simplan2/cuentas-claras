package com.simplan.cuentasclaras.domain.usecase.quickamount

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import com.simplan.cuentasclaras.domain.repository.QuickAmountRepository
import kotlinx.coroutines.flow.Flow

sealed interface GetQuickAmountsUseCase {
    suspend operator fun invoke(): Result<List<QuickAmount>>
    fun observe(): Flow<Result<List<QuickAmount>>>
}

sealed interface GetEnabledQuickAmountsUseCase {
    suspend operator fun invoke(): Result<List<QuickAmount>>
    fun observe(): Flow<Result<List<QuickAmount>>>
}

sealed interface GetQuickAmountUseCase {
    suspend operator fun invoke(id: Long): Result<QuickAmount>
}

sealed interface CreateQuickAmountUseCase {
    suspend operator fun invoke(quickAmount: QuickAmount): Result<Long>
}

sealed interface UpdateQuickAmountUseCase {
    suspend operator fun invoke(quickAmount: QuickAmount): Result<Unit>
}

sealed interface DeleteQuickAmountUseCase {
    suspend operator fun invoke(id: Long): Result<Unit>
}

sealed interface ReorderQuickAmountsUseCase {
    suspend operator fun invoke(quickAmountIds: List<Long>): Result<Unit>
}

interface QuickAmountUseCases {
    suspend fun getAll(): Result<List<QuickAmount>>
    fun observeAll(): Flow<Result<List<QuickAmount>>>
    suspend fun getEnabled(): Result<List<QuickAmount>>
    fun observeEnabled(): Flow<Result<List<QuickAmount>>>
    suspend fun getById(id: Long): Result<QuickAmount>
    suspend fun insert(quickAmount: QuickAmount): Result<Long>
    suspend fun update(quickAmount: QuickAmount): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
    suspend fun reorder(ids: List<Long>): Result<Unit>
}

class GetQuickAmountsUseCaseImpl(private val repository: QuickAmountRepository) : GetQuickAmountsUseCase {
    override suspend fun invoke(): Result<List<QuickAmount>> = repository.getAll()
    override fun observe(): Flow<Result<List<QuickAmount>>> = repository.observeAll()
}

class GetEnabledQuickAmountsUseCaseImpl(private val repository: QuickAmountRepository) : GetEnabledQuickAmountsUseCase {
    override suspend fun invoke(): Result<List<QuickAmount>> = repository.getEnabled()
    override fun observe(): Flow<Result<List<QuickAmount>>> = repository.observeEnabled()
}

class GetQuickAmountUseCaseImpl(private val repository: QuickAmountRepository) : GetQuickAmountUseCase {
    override suspend fun invoke(id: Long): Result<QuickAmount> = repository.getById(id)
}

class CreateQuickAmountUseCaseImpl(private val repository: QuickAmountRepository) : CreateQuickAmountUseCase {
    override suspend fun invoke(quickAmount: QuickAmount): Result<Long> = repository.insert(quickAmount)
}

class UpdateQuickAmountUseCaseImpl(private val repository: QuickAmountRepository) : UpdateQuickAmountUseCase {
    override suspend fun invoke(quickAmount: QuickAmount): Result<Unit> = repository.update(quickAmount)
}

class DeleteQuickAmountUseCaseImpl(private val repository: QuickAmountRepository) : DeleteQuickAmountUseCase {
    override suspend fun invoke(id: Long): Result<Unit> = repository.delete(id)
}

class ReorderQuickAmountsUseCaseImpl(private val repository: QuickAmountRepository) : ReorderQuickAmountsUseCase {
    override suspend fun invoke(quickAmountIds: List<Long>): Result<Unit> = repository.reorder(quickAmountIds)
}

class QuickAmountUseCasesImpl(
    private val getQuickAmountsUseCase: GetQuickAmountsUseCase,
    private val getEnabledQuickAmountsUseCase: GetEnabledQuickAmountsUseCase,
    private val getQuickAmountUseCase: GetQuickAmountUseCase,
    private val createQuickAmountUseCase: CreateQuickAmountUseCase,
    private val updateQuickAmountUseCase: UpdateQuickAmountUseCase,
    private val deleteQuickAmountUseCase: DeleteQuickAmountUseCase,
    private val reorderQuickAmountsUseCase: ReorderQuickAmountsUseCase
) : QuickAmountUseCases {

    override suspend fun getAll(): Result<List<QuickAmount>> = getQuickAmountsUseCase()
    override fun observeAll(): Flow<Result<List<QuickAmount>>> = getQuickAmountsUseCase.observe()
    override suspend fun getEnabled(): Result<List<QuickAmount>> = getEnabledQuickAmountsUseCase()
    override fun observeEnabled(): Flow<Result<List<QuickAmount>>> = getEnabledQuickAmountsUseCase.observe()
    override suspend fun getById(id: Long): Result<QuickAmount> = getQuickAmountUseCase(id)
    override suspend fun insert(quickAmount: QuickAmount): Result<Long> = createQuickAmountUseCase(quickAmount)
    override suspend fun update(quickAmount: QuickAmount): Result<Unit> = updateQuickAmountUseCase(quickAmount)
    override suspend fun delete(id: Long): Result<Unit> = deleteQuickAmountUseCase(id)
    override suspend fun reorder(ids: List<Long>): Result<Unit> = reorderQuickAmountsUseCase(ids)
}