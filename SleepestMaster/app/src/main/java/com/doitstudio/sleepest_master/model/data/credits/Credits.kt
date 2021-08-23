package com.doitstudio.sleepest_master.model.data.credits

import com.doitstudio.sleepest_master.model.data.Info
import com.doitstudio.sleepest_master.model.data.Websites
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Credits site model for handling the different sites
 */
data class CreditsSites(
    val authors:List<CreditsAuthors>,
    val site: Websites,
    val name:String,
    val url:String,
){

    companion object{

        /**
         * Create a new site
         */
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

/**
 * Credits author model for handling the different authors of a site
 */
data class CreditsAuthors(
    val author:String,
    val usage: Info
){

    companion object{

        /**
         * Create a new author
         */
        fun createCreditsAuthors(site:Websites) : List<CreditsAuthors>{
            /**
             * The actual used authors and sites
             */
            return when(site) {
                Websites.FLATICON -> listOf(
                    CreditsAuthors("Eucalyp", Info.SLEEP),
                    CreditsAuthors("Freepik", Info.SLEEP)
                )
                Websites.LOTTIEFILES -> listOf(
                    CreditsAuthors("asjadnaqvi", Info.SLEEP),
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
