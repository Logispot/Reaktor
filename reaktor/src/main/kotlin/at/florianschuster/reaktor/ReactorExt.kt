package at.florianschuster.reaktor

import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Maps changes from a [State] Observable and only emits those that are distinct from their immediate predecessors.
 */
fun <State : Any, SubState : Any> Observable<State>.changesFrom(mapper: (State) -> SubState): Observable<out SubState> {
    return map(mapper::invoke)
        .distinctUntilChanged()
}

/**
 * Creates an empty [Mutation] that performs a side effect that does not change the [State] of the [Reactor].
 */
fun <Mutation : Any> Reactor<*, Mutation, *>.emptyMutation(action: (() -> Unit)? = null): Observable<Mutation> =
    Completable.fromAction { action?.invoke() }.toObservable()