package com.alamkanak.weekview.sample.util

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import com.alamkanak.weekview.sample.data.EventsRepository
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM

sealed class GenericAction {

    data class LoadEvents(
        val yearMonths: List<YearMonth>,
        val clearExisting: Boolean,
    ) : GenericAction()

    data class FinishDrag(
        val id: Long,
        val newStartTime: LocalDateTime,
        val newEndTime: LocalDateTime,
    ) : GenericAction()
}

data class GenericViewState(
    val entities: List<CalendarEntity> = emptyList(),
)

sealed class GenericEffect {
    data class ShowSnackbar(val message: String, val undoAction: () -> Unit) : GenericEffect()
}

class GenericViewModel(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _viewState = MutableLiveData<GenericViewState>()
    val viewState: LiveData<GenericViewState> = _viewState

    private val _effects = MutableLiveData<Event<GenericEffect>>()
    val effects: LiveData<Event<GenericEffect>> = _effects

    private val currentEntities: List<CalendarEntity>
        get() = _viewState.value?.entities.orEmpty()

    fun handleAction(action: GenericAction) {
        when (action) {
            is GenericAction.LoadEvents -> {
                if (action.clearExisting) {
                    clearEvents()
                }
                fetchEvents(action.yearMonths)
            }
            is GenericAction.FinishDrag -> {
                handleDrag(
                    id = action.id,
                    newStartTime = action.newStartTime,
                    newEndTime = action.newEndTime,
                )
            }
        }
    }

    private fun clearEvents() {
        if (_viewState.value != null) {
            _viewState.value = _viewState.value?.copy(entities = emptyList())
        }
    }

    private fun fetchEvents(yearMonths: List<YearMonth>) {
        eventsRepository.fetch(yearMonths = yearMonths) { entities ->
            val existingEntities = _viewState.value?.entities.orEmpty()
            _viewState.value = GenericViewState(entities = existingEntities + entities)
        }
    }

    private fun handleDrag(id: Long, newStartTime: LocalDateTime, newEndTime: LocalDateTime) {
        val existingEntity = currentEntities
            .filterIsInstance<CalendarEntity.Event>()
            .first { it.id == id }

        val newEntity = existingEntity.copy(
            startTime = newStartTime,
            endTime = newEndTime,
        )

        updateEntity(newEntity)
        postDragNotification(existingEntity, newEntity)
    }

    private fun postDragNotification(
        existingEntity: CalendarEntity.Event,
        updatedEntity: CalendarEntity.Event,
    ) {
        val newDateTime = updatedEntity.startTime.format(DateTimeFormatter.ofLocalizedDateTime(MEDIUM))

        val action = GenericEffect.ShowSnackbar(
            message = "Moved ${updatedEntity.title} to $newDateTime",
            undoAction = { updateEntity(existingEntity) },
        )
        _effects.postEvent(action)
    }

    private fun updateEntity(newEntity: CalendarEntity.Event) {
        val updatedEntities = currentEntities.map { entity ->
            if (entity is CalendarEntity.Event && entity.id == newEntity.id) {
                newEntity
            } else {
                entity
            }
        }

        _viewState.value = GenericViewState(entities = updatedEntities)
    }

    class Factory(private val eventsRepository: EventsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenericViewModel::class.java)) {
                return GenericViewModel(eventsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.simpleName}")
        }
    }
}

fun ComponentActivity.genericViewModel(): Lazy<GenericViewModel> {
    val factoryPromise = {
        GenericViewModel.Factory(eventsRepository = EventsRepository(context = this))
    }
    return ViewModelLazy(GenericViewModel::class, { viewModelStore }, factoryPromise)
}

fun Fragment.genericViewModel(): Lazy<GenericViewModel> {
    val factoryPromise = {
        GenericViewModel.Factory(eventsRepository = EventsRepository(context = requireContext()))
    }
    return ViewModelLazy(GenericViewModel::class, { viewModelStore }, factoryPromise)
}
