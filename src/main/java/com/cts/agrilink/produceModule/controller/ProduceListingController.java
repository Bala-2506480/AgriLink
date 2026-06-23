package com.cts.agrilink.produceModule.controller;

import com.cts.agrilink.identityAccess.model.UserDetails;
import com.cts.agrilink.produceModule.entity.ProduceListing;
import com.cts.agrilink.produceModule.service.ProduceListingService;
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
public class ProduceListingController {

    private final ProduceListingService listingService;

    // POST - Create Listing (farmerId taken from JWT)
    @PostMapping("/createListing/v1.0")
    public ResponseEntity<Map<String, String>> createListing(
            @RequestBody ProduceListing listing,
            @AuthenticationPrincipal UserDetails currentUser) {

        listing.setFarmerId(currentUser.getUserId().longValue());
        listingService.createListing(listing);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Listing created successfully"));
    }

    // GET - Get All Listings
    @GetMapping("/getAllListings/v1.0")
    public ResponseEntity<List<ProduceListing>> getAllListings() {
        return ResponseEntity.ok(listingService.getAllListings());
    }

    // GET - Get Listing By ID
    @GetMapping("/getListingById/v1/{id}")
    public ResponseEntity<ProduceListing> getListingById(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListingById(id));
    }

    // GET - Get My Listings (Farmer's own)
    @GetMapping("/getMyListings/v1.0")
    public ResponseEntity<List<ProduceListing>> getMyListings(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(listingService.getListingsByFarmer(currentUser.getUserId().longValue()));
    }

    // GET - Get Listings By Status
    @GetMapping("/getListingsByStatus/v1.0")
    public ResponseEntity<List<ProduceListing>> getListingsByStatus(
            @RequestParam ProduceListing.ListingStatus status) {
        return ResponseEntity.ok(listingService.getListingsByStatus(status));
    }

    // PUT - Update Listing
    @PutMapping("/updateListing/v1/{id}")
    public ResponseEntity<Map<String, String>> updateListing(
            @PathVariable Long id,
            @RequestBody ProduceListing listing,
            @AuthenticationPrincipal UserDetails currentUser) {

        boolean isAdmin = currentUser.getRole().getRoleName().equals("AgriLinkAdmin");
        listingService.updateListing(id, listing, currentUser.getUserId().longValue(), isAdmin);
        return ResponseEntity.ok(Map.of("message", "Listing updated successfully"));
    }

    // PUT - Update Listing Status
    @PutMapping("/updateListingStatus/v1/{id}")
    public ResponseEntity<Map<String, String>> updateListingStatus(
            @PathVariable Long id,
            @RequestParam ProduceListing.ListingStatus status,
            @AuthenticationPrincipal UserDetails currentUser) {

        boolean isAdmin = currentUser.getRole().getRoleName().equals("AgriLinkAdmin");
        listingService.updateListingStatus(id, status, currentUser.getUserId().longValue(), isAdmin);
        return ResponseEntity.ok(Map.of("message", "Listing status updated successfully"));
    }

    // DELETE - Delete Listing
    @DeleteMapping("/deleteListing/v1/{id}")
    public ResponseEntity<Map<String, String>> deleteListing(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        boolean isAdmin = currentUser.getRole().getRoleName().equals("AgriLinkAdmin");
        listingService.deleteListing(id, currentUser.getUserId().longValue(), isAdmin);
        return ResponseEntity.ok(Map.of("message", "Listing deleted successfully"));
    }
}
