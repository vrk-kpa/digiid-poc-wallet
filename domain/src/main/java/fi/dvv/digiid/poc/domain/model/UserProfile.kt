package fi.dvv.digiid.poc.domain.model

import java.io.Serializable

data class UserProfile (val name: String, val certificatePEM: String) : Serializable