package fi.dvv.digiid.poc.wallet

import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @RelaxedMockK
    private lateinit var credentialsRepository: CredentialsRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `temporary example test to see that everything works`() {
        runBlocking { credentialsRepository.authorize("42") }
        coVerify(exactly = 1) { credentialsRepository.authorize("42") }
    }
}