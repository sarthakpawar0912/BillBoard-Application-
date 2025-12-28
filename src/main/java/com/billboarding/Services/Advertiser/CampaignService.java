package com.billboarding.Services.Advertiser;

import com.billboarding.DTO.Advertiser.CampaignDTO;
import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Advertiser.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository repo;

    public Campaign create(User advertiser, CampaignDTO dto) {
        Campaign campaign = Campaign.builder()
                .name(dto.getName())
                .advertiser(advertiser)
                .billboards(dto.getBillboards())
                .budget(dto.getBudget())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .cities(dto.getCities())
                .build();
        return repo.save(campaign);
    }

    public List<Campaign> getMyCampaigns(User advertiser) {
        return repo.findByAdvertiser(advertiser);
    }

    public Campaign pause(Long id) {
        Campaign c = repo.findById(id).orElseThrow();
        c.setStatus(com.billboarding.ENUM.CampaignStatus.PAUSED);
        return repo.save(c);
    }

    public Campaign resume(Long id) {
        Campaign c = repo.findById(id).orElseThrow();
        c.setStatus(com.billboarding.ENUM.CampaignStatus.ACTIVE);
        return repo.save(c);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
