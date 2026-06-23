package com.cts.agrilink.produceModule.repository;

import com.cts.agrilink.produceModule.entity.ProduceSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProduceSaleRepository extends JpaRepository<ProduceSale, Long> {

    List<ProduceSale> findByBuyerId(Long buyerId);

    List<ProduceSale> findByListing_ListingId(Long listingId);

    List<ProduceSale> findByPaymentStatus(ProduceSale.PaymentStatus paymentStatus);
}
