package at.florianschuster.counterexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import at.florianschuster.reaktor.ReactorView
import at.florianschuster.reaktor.android.ViewModelReactor
import at.florianschuster.reaktor.android.bind
import at.florianschuster.reaktor.android.viewModelReactor
import at.florianschuster.reaktor.changesFrom
import at.florianschuster.reaktor.consume
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.visibility
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_counter.*
import java.util.concurrent.TimeUnit


private const val layout: Int = R.layout.activity_counter

class CounterActivity : AppCompatActivity(), ReactorView<CounterReactor> {
    override val reactor: CounterReactor by viewModelReactor()
    override val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        bind(reactor)
    }

    override fun bind(reactor: CounterReactor) {
        //action
        btnIncrease.clicks()
            .map { CounterReactor.Action.Increase }
            .consume(reactor)
            .let(disposables::add)

        btnDecrease.clicks()
            .map { CounterReactor.Action.Decrease }
            .consume(reactor)
            .let(disposables::add)

        //state
        reactor.state.changesFrom { it.value }
            .map { "$it" }
            .bind(tvValue::setText)
            .let(disposables::add)

        reactor.state.changesFrom { it.loading }
            .bind(progressLoading.visibility())
            .let(disposables::add)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}


class CounterReactor(
    initialState: State = State()
) : ViewModelReactor<CounterReactor.Action, CounterReactor.Mutation, CounterReactor.State>(initialState) {
    sealed class Action {
        object Increase : Action()
        object Decrease : Action()
    }

    sealed class Mutation {
        object IncreaseValue : Mutation()
        object DecreaseValue : Mutation()
        data class SetLoading(val loading: Boolean) : Mutation()
    }

    data class State(
        val value: Int = 0,
        val loading: Boolean = false
    )

    override fun mutate(action: Action): Observable<out Mutation> = when (action) {
        is Action.Increase -> Observable.concat(
            Observable.just(Mutation.SetLoading(true)),
            Observable.just(Mutation.IncreaseValue).delay(500, TimeUnit.MILLISECONDS),
            Observable.just(Mutation.SetLoading(false))
        )
        is Action.Decrease -> Observable.concat(
            Observable.just(Mutation.SetLoading(true)),
            Observable.just(Mutation.DecreaseValue).delay(500, TimeUnit.MILLISECONDS),
            Observable.just(Mutation.SetLoading(false))
        )
    }

    override fun reduce(state: State, mutation: Mutation): State = when (mutation) {
        is Mutation.IncreaseValue -> state.copy(value = state.value + 1)
        is Mutation.DecreaseValue -> state.copy(value = state.value - 1)
        is Mutation.SetLoading -> state.copy(loading = mutation.loading)
    }
}