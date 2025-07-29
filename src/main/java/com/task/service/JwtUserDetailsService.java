package com.task.service;

import com.task.constants.MainConstants;
import com.task.dto.ResetPasswordForm;
import com.task.entity.User;
import com.task.repo.UserRepository;
import com.task.utils.Utils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@AllArgsConstructor
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User loadUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("email not found"));
    }

    public void resetUserPassword(ResetPasswordForm passwordForm) throws Exception {
//        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
//        Pattern pattern2 = Pattern.compile(pattern);
//        Matcher matcher = pattern2.matcher(passwordForm.getNewPassword());
//
//        if (!matcher.matches())
//            throw new Exception("Passwords must contain at least 8 characters, including UPPERCASE, lowercase letters, numbers, and special characters");

        User jwtUser = null;
        if (Utils.isNotEmpty(passwordForm.getEmail()))
            jwtUser = loadUserByEmail(passwordForm.getEmail());

        if (jwtUser == null)
            throw new Exception("Invalid credentials");

        if (isAccountSuspended(jwtUser.getStatusId()))
            throw new LockedException("account is suspended");

        if (isAccountLocked(jwtUser))
            throw new LockedException("Account is temporarily locked, please try again after 30 minutes");

        if (Utils.isNotEmpty(passwordForm.getNewPassword())) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, 6);
            jwtUser.setPasswordExpiryDate(calendar.getTime());
            jwtUser.setPassword(passwordEncoder.encode(passwordForm.getNewPassword()));
        }

        if (Utils.isNotEmpty(passwordForm.getEmail())) {
            jwtUser.setEmail(passwordForm.getEmail());
        }

        if (Utils.isNotEmpty(passwordForm.getMobile())) {
            jwtUser.setMobileNumber(passwordForm.getMobile());
        }

        userRepository.save(jwtUser);

    }

    public boolean isAccountSuspended(long statusId) {
        return statusId == MainConstants.ACCOUNT_SUSPENDED;
    }

    public boolean isAccountLocked(User jwtUser) {
        if (jwtUser.getStatusId() == MainConstants.ACCOUNT_LOCKED) {
//            if (!unLockUserAccount(jwtUser)) {
//                return true;
//            }
        }
        return false;
    }

//    private boolean unLockUserAccount(User jwtUser) {
//
//        Calendar currentCal = Calendar.getInstance();
//        Calendar lockedCal = Calendar.getInstance();
//        Date lockedDate = jwtUser.getLastLockedTime() != null ? jwtUser.getLastLockedTime()
//                : jwtUser.getSecondLockedTime() != null ? jwtUser.getSecondLockedTime()
//                : jwtUser.getFirstLockedTime() != null ? jwtUser.getFirstLockedTime() : null;
//        lockedCal.setTime(lockedDate);
//        lockedCal.add(Calendar.MINUTE, 5);
//
//        if (currentCal.before(lockedCal)) {
//            return false;
//        }
//
//        jwtUser.setStatusId((long) MainConstants.ACCOUNT_UNLOCKED);
//        jwtUser.setFirstLockedTime(null);
//        jwtUser.setSecondLockedTime(null);
//        jwtUser.setLastLockedTime(null);
//        jwtUser.setNumberOfLocks(0L);
//        jwtUser.setNumberOfFailedLogin(0L);
//        jwtUser.setUserId(jwtUser.getUserId());
//
//        userRepository.save(jwtUser);
//        return true;
//
//    }

}

