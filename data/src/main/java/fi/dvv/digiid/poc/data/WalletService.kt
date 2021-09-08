package fi.dvv.digiid.poc.data

import fi.dvv.digiid.poc.vc.VerifiablePresentation
import retrofit2.http.POST
import retrofit2.http.Path

interface WalletService {
    @POST("/coreid/{satu}")
    suspend fun getCoreID(@Path("satu") satu: String): VerifiablePresentation
}