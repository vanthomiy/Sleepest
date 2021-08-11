package com.doitstudio.sleepest_master.model.data.credits

import com.doitstudio.sleepest_master.model.data.Websites
import java.time.LocalDate
import java.time.LocalDateTime

data class CreditsSites(
    val authors:List<CreditsAuthors>,
    val site: Websites,
    val name:String,
    val url:String,
){

    companion object{

        fun createCreditSites() : List<CreditsSites>{
            val list =  mutableListOf<CreditsSites>()

            Websites.values().forEach { site ->
                list.add(CreditsSites(
                    authors = CreditsAuthors.createCreditsAuthors(site),
                    site = site,
                    name = Websites.getName(site),
                    url = Websites.getWebsite(site)
                ))
            }

            return list
        }
    }
}

data class CreditsAuthors(
    val author:String,
    val date:LocalDate
){

    companion object{

        fun createCreditsAuthors(site:Websites) : List<CreditsAuthors>{

            return when(site) {
                Websites.FLATICON -> listOf(
                    CreditsAuthors("aut1", LocalDate.of(2021, 8, 10)),
                    CreditsAuthors("aut2", LocalDate.of(2021, 8, 10))
                )
                Websites.LOTTIEFILES -> listOf(
                    CreditsAuthors("aut3", LocalDate.of(2021, 8, 10)),
                    CreditsAuthors("aut4", LocalDate.of(2021, 8, 10))
                )
                else -> listOf()
            }
        }
    }
}
