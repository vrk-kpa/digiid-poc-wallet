package fi.dvv.digiid.poc.domain.model

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Locked(val failed: Boolean = false) : AuthState()
    data class Unlocked(val profile: UserProfile) : AuthState()
}