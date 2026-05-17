package com.iptvcoco.app.model

data class M3UAccount(
    val url: String = "",
    val username: String? = null,
    val password: String? = null,
    val type: AccountType? = AccountType.M3U
) {
    enum class AccountType {
        M3U,
        XTREAM
    }
}
