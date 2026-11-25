package cl.oyl.mobile.data.mapper
import cl.oyl.mobile.model.auth.Account
import cl.oyl.mobile.data.local.entity.Session
fun Account.asSession() = Session(id = id, alias = alias, name = name, role = role,
    email = email.orEmpty(), phone = phone.orEmpty(), address = address.orEmpty())
