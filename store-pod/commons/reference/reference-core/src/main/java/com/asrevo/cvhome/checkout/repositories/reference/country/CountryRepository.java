package com.asrevo.cvhome.checkout.repositories.reference.country;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.checkout.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface CountryRepository extends JpaRepository<Country, CountryIsoCode> {

    @Query("""
            select c from Country c
            left join fetch c.descriptions cd
            where c.isoCode=?1""")
    Country findByIsoCode(CountryIsoCode code);

    @Query("""
            select c from Country c
            left join fetch c.descriptions cd
            left join fetch c.zones cz
            left join fetch cz.descriptions
            where cd.languageCode=?1""")
    List<Country> listByLanguage(LanguageCode code);

    @Query("""
            select distinct c from Country c
            left join fetch c.descriptions cd
            left join fetch c.zones cz
            left join fetch cz.descriptions
            where cd.languageCode=?1
            """)
    List<Country> listCountryZonesByLanguage(LanguageCode code);

}
