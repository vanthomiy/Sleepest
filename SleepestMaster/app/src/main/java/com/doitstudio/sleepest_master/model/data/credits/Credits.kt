package com.doitstudio.sleepest_master.model.data.credits

import com.doitstudio.sleepest_master.model.data.Info
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
    val usage: Info
){

    companion object{

        fun createCreditsAuthors(site:Websites) : List<CreditsAuthors>{

            return when(site) {
                Websites.FLATICON -> listOf(
                    CreditsAuthors("Eucalyp", Info.SLEEP),
                    CreditsAuthors("aut2", Info.SLEEP)
                )
                Websites.LOTTIEFILES -> listOf(
                    CreditsAuthors("asjadnaqvi", Info.SLEEP),
                    CreditsAuthors("arpangupta", Info.SLEEP),
                    CreditsAuthors("batman", Info.SLEEP),
                    CreditsAuthors("motionstk.studio", Info.SLEEP),
                    CreditsAuthors("Beecher", Info.SLEEP),
                    CreditsAuthors("lpdesign", Info.ALARM),
                    CreditsAuthors("DarkMuzishn", Info.SETTINGS)
                )
                else -> listOf()
            }
        }
    }
}
