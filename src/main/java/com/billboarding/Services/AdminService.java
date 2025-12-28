package com.billboarding.Services;

import com.billboarding.ENUM.KycStatus;
import com.billboarding.Entity.User;
import com.billboarding.Notification.EmailNotificationService;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private  final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Get all owners/advertisers where KYC is pending
     */
    public List<User> getPendingKycUsers(){
        return userRepository.findByKycStatus(KycStatus.PENDING);
    }


    /**
     * Approve user KYC
     */
    public void approveKyc(Long userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User Not found"));

        user.setKycStatus(KycStatus.APPROVED);
       userRepository.save(user);

       emailNotificationService.sendKycApprovedEmail(user);

    }
    /**
     * Reject user KYC with reason
     */
    public void rejectKyc(Long userId,String reason){
        User user= userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));
        user.setKycStatus(KycStatus.REJECTED);
        userRepository.save(user);

        emailNotificationService.sendKycRejectedEmail(user,reason);
    }



}
