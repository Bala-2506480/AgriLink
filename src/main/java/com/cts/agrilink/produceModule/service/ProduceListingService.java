package com.cts.agrilink.produceModule.service;

import com.cts.agrilink.exception.ForbiddenException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.produceModule.entity.ProduceListing;
import com.cts.agrilink.produceModule.repository.ProduceListingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduceListingService {

    private final ProduceListingRepository listingRepository;

    // POST - Create Listing
    @Transactional
    public ProduceListing createListing(ProduceListing listing) {
        listing.setStatus(ProduceListing.ListingStatus.Available);
        return listingRepository.save(listing);
    }

    // GET - Get All Listings
    public List<ProduceListing> getAllListings() {
        return listingRepository.findAll();
    }

    // GET - Get Listing By ID
    public ProduceListing getListingById(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + id));
    }

    // GET - Get Listings By Farmer
    public List<ProduceListing> getListingsByFarmer(Long farmerId) {
        return listingRepository.findByFarmerId(farmerId);
    }

    // GET - Get Listings By Status
    public List<ProduceListing> getListingsByStatus(ProduceListing.ListingStatus status) {
        return listingRepository.findByStatus(status);
    }

    // PUT - Update Listing
    @Transactional
    public ProduceListing updateListing(Long id, ProduceListing updatedListing,
                                        Long requestingUserId, boolean isAdmin) {
        ProduceListing existing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + id));

        if (!isAdmin && !existing.getFarmerId().equals(requestingUserId)) {
            throw new ForbiddenException("You can only update your own listings");
        }

        existing.setQuantityKg(updatedListing.getQuantityKg());
        existing.setAskingPricePerKg(updatedListing.getAskingPricePerKg());
        if (updatedListing.getStatus() != null) existing.setStatus(updatedListing.getStatus());
        if (updatedListing.getQualityGrade() != null) existing.setQualityGrade(updatedListing.getQualityGrade());
        if (updatedListing.getHarvestDate() != null) existing.setHarvestDate(updatedListing.getHarvestDate());
        return listingRepository.save(existing);
    }

    // PUT - Update Listing Status
    @Transactional
    public ProduceListing updateListingStatus(Long id, ProduceListing.ListingStatus status,
                                              Long requestingUserId, boolean isAdmin) {
        ProduceListing existing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + id));

        if (!isAdmin && !existing.getFarmerId().equals(requestingUserId)) {
            throw new ForbiddenException("You can only update your own listings");
        }

        existing.setStatus(status);
        return listingRepository.save(existing);
    }

    // DELETE - Delete Listing
    @Transactional
    public void deleteListing(Long id, Long requestingUserId, boolean isAdmin) {
        ProduceListing existing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + id));

        if (!isAdmin && !existing.getFarmerId().equals(requestingUserId)) {
            throw new ForbiddenException("You can only delete your own listings");
        }

        listingRepository.deleteById(id);
    }
}
