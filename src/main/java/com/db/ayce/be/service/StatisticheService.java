package com.db.ayce.be.service;

import java.time.LocalDateTime;
import java.util.List;

import com.db.ayce.be.dto.ProductSalesDto;
import com.db.ayce.be.dto.SessionDeltaDto;
import com.db.ayce.be.dto.TotaliDto;

public interface StatisticheService {
    TotaliDto calcolaTotali(String period, LocalDateTime from, LocalDateTime to);

    Integer contaSessioni(String period, LocalDateTime from, LocalDateTime to);

    List<ProductSalesDto> prodottiPiùVenduti(String period, LocalDateTime from, LocalDateTime to, int limit);

    List<ProductSalesDto> prodottiMenoVenduti(String period, LocalDateTime from, LocalDateTime to, int limit);

    SessionDeltaDto deltaSessione(Long sessioneId);

    // utilità: totali per singola sessione (usata internamente e può essere esposta)
    TotaliDto calcolaTotaliPerSessioneId(Long sessioneId);
}
