package com.cts.agrilink.produceModule.controller;

import com.cts.agrilink.identityAccess.model.UserDetails;
import com.cts.agrilink.produceModule.entity.ProduceSale;
import com.cts.agrilink.produceModule.service.ProduceSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agriLink/produceSales")
@RequiredArgsConstructor
public class ProduceSaleController {

    private final ProduceSaleService saleService;

    // POST - Create Sale (buyerId taken from JWT)
    @PostMapping("/createSale/v1.0")
    public ResponseEntity<Map<String, String>> createSale(
            @RequestBody ProduceSale sale,
            @AuthenticationPrincipal UserDetails currentUser) {

        sale.setBuyerId(currentUser.getUserId().longValue());
        saleService.createSale(sale);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Sale created successfully"));
    }

    // GET - Get All Sales
    @GetMapping("/getAllSales/v1.0")
    public ResponseEntity<List<ProduceSale>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    // GET - Get Sale By ID
    @GetMapping("/getSaleById/v1/{id}")
    public ResponseEntity<ProduceSale> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    // GET - Get My Sales (Buyer's own)
    @GetMapping("/getMySales/v1.0")
    public ResponseEntity<List<ProduceSale>> getMySales(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(saleService.getSalesByBuyer(currentUser.getUserId().longValue()));
    }

    // PUT - Update Sale
    @PutMapping("/updateSale/v1/{id}")
    public ResponseEntity<Map<String, String>> updateSale(
            @PathVariable Long id,
            @RequestBody ProduceSale sale) {
        saleService.updateSale(id, sale);
        return ResponseEntity.ok(Map.of("message", "Sale updated successfully"));
    }

    // PUT - Update Payment Status
    @PutMapping("/updatePaymentStatus/v1/{id}")
    public ResponseEntity<Map<String, String>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam ProduceSale.PaymentStatus status) {
        saleService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Payment status updated successfully"));
    }

    // DELETE - Delete Sale
    @DeleteMapping("/deleteSale/v1/{id}")
    public ResponseEntity<Map<String, String>> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return ResponseEntity.ok(Map.of("message", "Sale deleted successfully"));
    }
}
