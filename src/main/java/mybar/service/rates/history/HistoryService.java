package mybar.service.rates.history;

import mybar.domain.History;
import mybar.repository.rates.RatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Service
public class HistoryService {

    private final RatesRepository ratesRepository;

    @Autowired
    public HistoryService(RatesRepository ratesRepository) {
        this.ratesRepository = ratesRepository;
    }

    public List<History> getHistoryForPeriod(LocalDate startDate, LocalDate endDate) {

        startDate = startDate != null ? startDate : Year.parse("2008").atDay(1);
        endDate = endDate != null ? endDate : LocalDate.now();

        return ratesRepository.getRatedCocktailsForPeriod(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

}