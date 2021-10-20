package fi.dvv.digiid.poc.domain.model

import java.io.Serializable

data class UserProfile (val name: String, val certificatePEM: String) : Serializable {
    companion object {
        val TEST_USER_1 = UserProfile("Sanna Testaaja", "")
        val TEST_USER_2 = UserProfile("Pekka Testaaja", "")
    }
}