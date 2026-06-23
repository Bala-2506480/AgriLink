package com.cts.agrilink.produceModule.service;

import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.produceModule.entity.ProduceListing;
import com.cts.agrilink.produceModule.entity.ProduceSale;
import com.cts.agrilink.produceModule.repository.ProduceListingRepository;
import com.cts.agrilink.produceModule.repository.ProduceSaleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduceSaleService {

    private final ProduceSaleRepository saleRepository;
    private final ProduceListingRepository listingRepository;

    // POST - Create Sale
    @Transactional
    public ProduceSale createSale(ProduceSale sale) {
        ProduceListing listing = listingRepository
                .findById(sale.getListing().getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        BigDecimal total = sale.getQuantitySoldKg().multiply(sale.getAgreedPricePerKg());
        sale.setTotalAmount(total);
        sale.setPaymentStatus(ProduceSale.PaymentStatus.Pending);

        listing.setStatus(ProduceListing.ListingStatus.Sold);
        listingRepository.save(listing);

        return saleRepository.save(sale);
    }

    // GET - Get All Sales
    public List<ProduceSale> getAllSales() {
        return saleRepository.findAll();
    }

    // GET - Get Sale By ID
    public ProduceSale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
    }

    // GET - Get Sales By Buyer
    public List<ProduceSale> getSalesByBuyer(Long buyerId) {
        return saleRepository.findByBuyerId(buyerId);
    }

    // PUT - Update Sale
    @Transactional
    public ProduceSale updateSale(Long id, ProduceSale updatedSale) {
        ProduceSale existing = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));

        existing.setQuantitySoldKg(updatedSale.getQuantitySoldKg());
        existing.setAgreedPricePerKg(updatedSale.getAgreedPricePerKg());
        BigDecimal total = updatedSale.getQuantitySoldKg().multiply(updatedSale.getAgreedPricePerKg());
        existing.setTotalAmount(total);
        return saleRepository.save(existing);
    }

    // PUT - Update Payment Status
    @Transactional
    public ProduceSale updatePaymentStatus(Long id, ProduceSale.PaymentStatus status) {
        ProduceSale existing = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        existing.setPaymentStatus(status);
        return saleRepository.save(existing);
    }

    // DELETE - Delete Sale
    @Transactional
    public void deleteSale(Long id) {
        saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        saleRepository.deleteById(id);
    }
}
