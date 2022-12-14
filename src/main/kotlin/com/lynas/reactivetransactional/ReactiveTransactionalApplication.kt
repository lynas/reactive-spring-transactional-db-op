package com.lynas.reactivetransactional

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@SpringBootApplication
class ReactiveTransactionalApplication

fun main(args: Array<String>) {
    runApplication<ReactiveTransactionalApplication>(*args)
}

@Service
class AdminUserService(val adminUserRepo: AdminUserRepo) {

    suspend fun saveAdminUser(adminUser: AdminUser) {
		throw RuntimeException("Test")
        adminUserRepo.save(adminUser).awaitFirst()
    }

}

@Service
class AppUserService(val appUserRepo: AppUserRepo) {
    suspend fun saveAppUser(appUser: AppUser) {
        appUserRepo.save(appUser).awaitFirst()
    }
}

@Service
class AllUserService(
    val appUserService: AppUserService,
    val adminUserService: AdminUserService
) {

    @Transactional
    suspend fun saveAdminAndAppUser() {
        appUserService.saveAppUser(AppUser().also {
            it.name = "Name ${System.currentTimeMillis()}"
            it.tag = "Tag ${System.currentTimeMillis()}"
        })
        adminUserService.saveAdminUser(AdminUser().also {
            it.tag = "Tag ${System.currentTimeMillis()}"
        })
    }

}


@Table(name = "app_user")
class AppUser(
    @Id
    @JvmField
    val id: String = UUID.randomUUID().toString()
) : Persistable<String> {


    @Column
    lateinit var name: String

    @Column
    lateinit var tag: String


    override fun isNew(): Boolean {
        return this.id.isNotBlank()
    }

    override fun getId(): String {
        return this.id
    }
}

@Table(name = "admin_user")
class AdminUser(
    @Id
    @JvmField
    val id: String = UUID.randomUUID().toString()
) : Persistable<String> {


    @Column
    lateinit var tag: String

    override fun isNew(): Boolean {
        return this.id.isNotBlank()
    }

    override fun getId(): String {
        return this.id
    }
}

interface AppUserRepo : ReactiveCrudRepository<AppUser, String>
interface AdminUserRepo : ReactiveCrudRepository<AdminUser, String>

@RestController
class DemoController(val allUserService: AllUserService) {

    @GetMapping("/save")
    suspend fun demo() {
        allUserService.saveAdminAndAppUser()
    }
}