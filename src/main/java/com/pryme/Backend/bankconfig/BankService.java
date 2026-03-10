package com.pryme.Backend.bankconfig;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;

    @Cacheable(cacheNames = "banks:all")
    @Transactional(readOnly = true)
    public List<BankResponse> getAll() {
        return bankRepository.findAll().stream().map(BankResponse::from).toList();
    }


    @Cacheable(cacheNames = "banks:partners")
    @Transactional(readOnly = true)
    public List<PartnerBankResponse> getActivePartners() {
        return bankRepository.findTop15ByIsActiveTrueOrderByBankNameAsc()
                .stream()
                .map(PartnerBankResponse::from)
                .toList();
    }

    @CacheEvict(cacheNames = {"banks:all", "banks:recommendation", "banks:partners"}, allEntries = true)
    @Transactional
    public BankResponse create(BankRequest request) {
        if (bankRepository.existsByBankNameIgnoreCase(request.bankName())) {
            throw new ConflictException("Bank with same name already exists");
        }

        Bank bank = new Bank();
        bank.setBankName(request.bankName().trim());
        bank.setLogoUrl(request.logoUrl().trim());
        bank.setActive(request.isActive());

        return BankResponse.from(bankRepository.save(bank));
    }

    @CacheEvict(cacheNames = {"banks:all", "banks:recommendation", "banks:partners"}, allEntries = true)
    @Transactional
    public BankResponse update(UUID id, BankRequest request) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found: " + id));

        bank.setBankName(request.bankName().trim());
        bank.setLogoUrl(request.logoUrl().trim());
        bank.setActive(request.isActive());

        return BankResponse.from(bankRepository.save(bank));
    }

    @CacheEvict(cacheNames = {"banks:all", "banks:recommendation", "banks:partners"}, allEntries = true)
    @Transactional
    public BankResponse toggleVisibility(UUID id, boolean active) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found: " + id));
        bank.setActive(active);
        return BankResponse.from(bankRepository.save(bank));
    }

    @CacheEvict(cacheNames = {"banks:all", "banks:recommendation", "banks:partners"}, allEntries = true)
    @Transactional
    public void delete(UUID id) {
        if (!bankRepository.existsById(id)) {
            throw new NotFoundException("Bank not found: " + id);
        }
        bankRepository.deleteById(id);
    }
}
