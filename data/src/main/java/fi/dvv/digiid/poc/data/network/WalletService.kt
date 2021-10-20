package fi.dvv.digiid.poc.data.network

import fi.dvv.digiid.poc.vc.VerifiablePresentation
import retrofit2.http.POST
import retrofit2.http.Path

interface WalletService {
    @POST("/api/0.1/coreid/{satu}")
    suspend fun getCoreID(@Path("satu") satu: String): VerifiablePresentation
}