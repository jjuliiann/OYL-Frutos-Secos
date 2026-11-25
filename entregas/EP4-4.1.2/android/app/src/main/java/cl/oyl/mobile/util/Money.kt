package cl.oyl.mobile.util

fun money(amount: Int): String = "$" + "%,d".format(java.util.Locale.US, amount).replace(',', '.')
