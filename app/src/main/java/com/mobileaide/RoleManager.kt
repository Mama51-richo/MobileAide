package com.mobileaide

import android.content.Context

class RoleManager(context: Context) {

    private val preferences = context.getSharedPreferences("mobile_aide_roles", Context.MODE_PRIVATE)

    enum class Role {
        OWNER,
        ADMIN,
        OPERATOR,
        VIEWER
    }

    fun getRole(): Role {
        val name = preferences.getString("selected_role", Role.OWNER.name) ?: Role.OWNER.name
        return Role.valueOf(name)
    }

    fun setRole(role: Role) {
        preferences.edit().putString("selected_role", role.name).apply()
    }
}
