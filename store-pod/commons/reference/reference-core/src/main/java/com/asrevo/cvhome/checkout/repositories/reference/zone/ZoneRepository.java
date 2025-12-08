package com.asrevo.cvhome.checkout.repositories.reference.zone;

import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ZoneRepository extends JpaRepository<Zone, ZoneCode> {

	Zone findByCode(ZoneCode code);

	@Query("""
			select z from Zone z
			left join fetch z.descriptions zd
			where zd.languageCode=?1""")
	List<Zone> listByLanguage(LanguageCode id);

	@Query("""
			select z from Zone z
			left join fetch z.descriptions zd
			where z.country=?1 and zd.languageCode=?2""")
	List<Zone> listByLanguageAndCountry(CountryIsoCode isoCode, LanguageCode code);

}
