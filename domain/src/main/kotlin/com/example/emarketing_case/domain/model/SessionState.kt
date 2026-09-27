package com.example.emarketing_case.domain.model

sealed interface SessionState {
    /** The stored session has not yet been resolved. */
    data object Unknown : SessionState

    /** A usable session is available; storing tokens alone does not establish this state. */
    data object Authenticated : SessionState

    data object Unauthenticated : SessionState
}
