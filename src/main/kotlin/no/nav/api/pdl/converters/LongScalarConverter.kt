package no.nav.api.pdl.converters

import com.expediagroup.graphql.client.converter.ScalarConverter

class LongScalarConverter : ScalarConverter<Long> {
    override fun toJson(value: Long): String = value.toString()
    override fun toScalar(rawValue: Any): Long = rawValue.toString().toLong()
}
