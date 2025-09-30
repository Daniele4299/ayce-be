package com.db.ayce.be.service;

import java.time.LocalDate;
import java.util.List;

import com.db.ayce.be.dto.ProductSalesDto;
import com.db.ayce.be.dto.SessionDeltaDto;
import com.db.ayce.be.dto.TotaliDto;

public interface StatisticheService {
    TotaliDto calcolaTotali(String period, LocalDate from, LocalDate to);

    Integer contaSessioni(String period, LocalDate from, LocalDate to);

    List<ProductSalesDto> prodottiPiùVenduti(String period, LocalDate from, LocalDate to, int limit);

    List<ProductSalesDto> prodottiMenoVenduti(String period, LocalDate from, LocalDate to, int limit);

    SessionDeltaDto deltaSessione(Long sessioneId);

    // utilità: totali per singola sessione (usata internamente e può essere esposta)
    TotaliDto calcolaTotaliPerSessioneId(Long sessioneId);
}
